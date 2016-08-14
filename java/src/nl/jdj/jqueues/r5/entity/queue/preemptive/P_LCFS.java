package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server Preemptive Last-Come First-Served (P_LCFS) queueing discipline.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class P_LCFS<J extends SimJob, Q extends P_LCFS>
extends AbstractPreemptiveSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive LCFS queue with infinite buffer size given an event list and preemption strategy.
   *
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public P_LCFS (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, Integer.MAX_VALUE, 1, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link P_LCFS} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link P_LCFS} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public P_LCFS<J, Q> getCopySimQueue ()
  {
    return new P_LCFS<> (getEventList (), getPreemptionStrategy ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "P_LCFS[preemption strategy]".
   * 
   * @return "P_LCFS[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "P_LCFS[" + getPreemptionStrategy () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the head of the job queue.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (0, job);
  }

  /** Starts the arrived job if server-access credits are available.
   * 
   * @see #hasServerAcccessCredits
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }

  /** Starts jobs as long as there are server-access credits and jobs waiting.
   * 
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, getFirstJobInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code true}.
   * 
   * @return {@code true}.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job, after sanity checks, in the service area and administers its remaining service time.
   * 
   * @see #jobsInServiceArea
   * @see #getServiceTimeForJob
   * @see #remainingServiceTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || getJobsInServiceArea ().contains (job)
    || this.remainingServiceTime.containsKey (job))
      throw new IllegalArgumentException ();
    this.jobsInServiceArea.add (job);
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    this.remainingServiceTime.put (job, jobServiceTime);
  }

  /** Schedules the started job for immediate execution if it is the only job in the service area
   *  or if its arrival time is strictly smaller than the arrival time of a job in execution,
   *  the latter of which is then preempted.
   * 
   * @see #jobsBeingServed
   * @see #getFirstJobInServiceArea
   * @see #preemptJob
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job))
    || ! this.remainingServiceTime.containsKey (job))
      throw new IllegalArgumentException ();
    if (this.jobsBeingServed.containsKey (job))
      throw new IllegalStateException ();
    if (this.jobsBeingServed.size () > 1)
      throw new IllegalStateException ();
    if (getFirstJobInServiceArea () == job)
    {
      // The job is eligible for immediate execution, hence we must preempt the job currently being executed.
      final J jobBeingServed = (this.jobsBeingServed.isEmpty () ? null : this.jobsBeingServed.keySet ().iterator ().next ());
      if (jobBeingServed != null)
        preemptJob (time, jobBeingServed);
      // The preemption could have scheduled 'job' already, so make sure we check!
      if (this.jobsBeingServed.isEmpty ())
        startServiceChunk (time, job);
      else if (this.jobsBeingServed.size () > 1)
        throw new IllegalStateException ();
      else if (this.jobsBeingServed.keySet ().iterator ().next () != job)
        throw new IllegalStateException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return super.getServiceTimeForJob (job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXIT (DEPARTURE / DROP / REVOKATION)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from internal administration and cancels any pending departure event for it.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit (final J exitingJob, final double time)
  {
    if (exitingJob == null || ! this.jobQueue.contains (exitingJob))
      throw new IllegalArgumentException ();
    if (this.jobsInServiceArea.contains (exitingJob))
    {
      if (! this.remainingServiceTime.containsKey (exitingJob))
        throw new IllegalStateException ();
      this.remainingServiceTime.remove (exitingJob);
      if (this.jobsBeingServed.containsKey (exitingJob))
      {
        // Note: getDepartureEvents requires its argument to be present in this.jobQueue!
        if (! getDepartureEvents (exitingJob).isEmpty ())
        {
          if (getDepartureEvents (exitingJob).size () > 1)
            throw new IllegalStateException ();
          cancelDepartureEvent (exitingJob);
        }
        this.jobsBeingServed.remove (exitingJob);
      }
      this.jobsInServiceArea.remove (exitingJob);
    }
    this.jobQueue.remove (exitingJob);
  }
  
  /** If there are jobs in the service area but none in execution,
   *  starts a service-chunk for the job in the service area
   *  that arrived last at the queue.
   * 
   * @see #jobsBeingServed
   * @see #jobsInServiceArea
   * @see #getFirstJobInServiceArea
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void rescheduleAfterExit (final double time)
  {
    if (this.jobsBeingServed.isEmpty () && ! this.jobsInServiceArea.isEmpty ())
      startServiceChunk (time, getFirstJobInServiceArea ());
  }
  
}
