package nl.jdj.jqueues.r5.entity.queue.preemptive;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server preemptive Shortest-Remaining (Service) Time First (SRTF) queueing discipline.
 *
 * <p>
 * In SRTF, the job present (and admitted to the server in view of server-access credits) with
 * the minimum remaining service time is in service until completion.
 * In case of a tie between a job entering the service area and the job currently in service,
 * the job in service is <i>not</i> preempted.
 * In case of a tie between multiple jobs in the service area,
 * the jobs are served in arrival order.
 * 
 * <p>
 * This implementation admits waiting jobs to the service area (server) as soon as server-access credits are available,
 * irrespective of their remaining (i.c., required) service time.
 * Once admitted to the service area, they wait until being served exclusively by the (single) server.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SRTF<J extends SimJob, Q extends SRTF>
extends AbstractPreemptiveSingleServerSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive SRTF queue given an event list and preemption strategy.
   *
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public SRTF (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public SRTF<J, Q> getCopySimQueue ()
  {
    return new SRTF<> (getEventList (), getPreemptionStrategy ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SRTF[preemption strategy]".
   * 
   * @return "SRTF[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SRTF[" + getPreemptionStrategy () + "]";
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
  
  /** Inserts the job in the job queue maintaining non-decreasing (required) service-time ordering.
   * 
   * <p>
   * In case of ties, jobs are inserted in order of arrival.
   * 
   * @see #jobQueue
   * @see #getServiceTimeForJob
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    int newPosition = 0;
    while (newPosition < this.jobQueue.size ()
      && getServiceTimeForJob (this.jobQueue.get (newPosition)) <= getServiceTimeForJob (job))
      newPosition++;
    this.jobQueue.add (newPosition, job);    
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
    if (job == null || ! getJobsInWaitingArea ().contains (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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
  
  /** Returns <code>true</code>.
   * 
   * @return True.
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
   *  or if its (remaining) service time is strictly smaller than the remaining service of a job in execution,
   *  the latter of which is then preempted.
   * 
   * @see #jobsBeingServed
   * @see #remainingServiceTime
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
    final double jobServiceTime = this.remainingServiceTime.get (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    // Get the job currently being served; if any.
    final J jobBeingServed;
    if (! this.jobsBeingServed.isEmpty ())
    {
      if (this.jobsBeingServed.size () > 1)
        throw new IllegalStateException ();
      jobBeingServed = this.jobsBeingServed.keySet ().iterator ().next ();
    }
    else
      jobBeingServed = null;
    if (job == jobBeingServed)
      throw new IllegalStateException ();
    // Check whether job is eligible for (immediate) execution.
    if (jobBeingServed == null || jobServiceTime < this.remainingServiceTime.get (jobBeingServed))
    {
      // The job is eligible for immediate execution, hence we must preempt the job currently being executed.
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
  // EXIT (DEPARTURE / DROP / REVOKATION)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from the internal data structures and removes its departure event, if needed.
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
   *  with the minimum remaining service time.
   * 
   * @see #jobsBeingServed
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void rescheduleAfterExit (final double time)
  {
    if (this.jobsBeingServed.isEmpty () && ! this.jobsInServiceArea.isEmpty ())
    {
      final double sRST = this.remainingServiceTime.firstValue ();
      final Set<J> jobsWithSRST = this.remainingServiceTime.getPreImageForValue (sRST);
      if (jobsWithSRST.isEmpty ())
        throw new IllegalStateException ();
      startServiceChunk (time, jobsWithSRST.iterator ().next ());
    }
  }
  
}
