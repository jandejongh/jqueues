package nl.jdj.jqueues.r5.entity.queue.qos;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.preemptive.PreemptionStrategy;
import nl.jdj.jsimulation.r5.SimEventList;

/** The Priority-Queueing queueing discipline.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public class PQ<J extends SimJob, Q extends PQ, P extends Comparable>
extends AbstractPreemptiveSingleServerSimQueueQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a Priority Queue given an event list, preemption strategy, and QoS structure.
   *
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * @param qosClass           The Java class to use for QoS behavior, non-{@code null}.
   * @param defaultJobQoS      The default QoS value to use for non-QoS jobs, non-{@code null}. 
   *
   * @throws IllegalArgumentException If the event list or one or both QoS arguments is <code>null</code>.
   *
   */
  public PQ
  (final SimEventList eventList,
    final PreemptionStrategy preemptionStrategy,
    final Class<P> qosClass,
    final P defaultJobQoS)
  {
    super (eventList, preemptionStrategy, qosClass, defaultJobQoS);
  }
  
  /** Returns a new {@link PQ} object on the same {@link SimEventList} with the same preemption strategy and QoS structure.
   * 
   * @return A new {@link PQ} object on the same {@link SimEventList} with the same preemption strategy and QoS structure.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * @see #getQoSClass
   * @see #getDefaultJobQoS
   * 
   */
  @Override
  public PQ<J, Q, P> getCopySimQueue ()
  {
    return new PQ (getEventList (), getPreemptionStrategy (), getQoSClass (), getDefaultJobQoS ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "PQ[preemption strategy]".
   * 
   * @return "PQ[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "PQ[" + getPreemptionStrategy () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are no jobs present in the system.
   * 
   * @return True if there are no jobs present in the system.
   * 
   * @see #getNumberOfJobs
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getNumberOfJobs () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
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
  // STATE: ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job into {@link #jobQueue} (tail) and {@link #jobsQoSMap}.
   * 
   * @see #getAndCheckJobQoS
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (job == null || this.jobQueue.contains (job) || job.getQueue () != null)
      throw new IllegalArgumentException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (this.jobsBeingServed.keySet ().contains (job))
      throw new IllegalStateException ();
    this.jobQueue.add (job);
    final P qos = getAndCheckJobQoS (job);
    if (! this.jobsQoSMap.containsKey (qos))
      this.jobsQoSMap.put (qos, new LinkedHashSet<> ());
    this.jobsQoSMap.get (qos).add (job);
  }

  /** Performs sanity checks and invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (! this.jobsQoSMap.get (getAndCheckJobQoS (job)).contains (job))
      throw new IllegalStateException ();
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    super.removeJobFromQueueUponDrop (job, time);
  }

  /** Invokes {@link #rescheduleAfterDeparture}.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    rescheduleAfterDeparture (job, time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    super.removeJobFromQueueUponRevokation (job, time);
  }

  /** Invokes {@link #rescheduleAfterDeparture}.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    rescheduleAfterDeparture (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: SERVER ACCCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    super.removeJobFromQueueUponDeparture (departingJob, time);
  }

  /** Performs sanity checks and invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (this.jobQueue.contains (departedJob))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (departedJob))
      throw new IllegalStateException ();
    final P qos = getAndCheckJobQoS (departedJob);
    if (this.jobsQoSMap.containsKey (qos) && this.jobsQoSMap.get (qos).contains (departedJob))
      throw new IllegalStateException ();
    if (this.jobsBeingServed.keySet ().contains (departedJob))
      throw new IllegalStateException ();
    reschedule (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from internal administration and cancels a pending departure event for it.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #getAndCheckJobQoS
   * @see #getJobsQoSMap
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
    final P qos = getAndCheckJobQoS (exitingJob);
    if (! this.jobsQoSMap.containsKey (qos))
      throw new IllegalStateException ();
    if (! this.jobsQoSMap.get (qos).contains (exitingJob))
      throw new IllegalStateException ();
    this.jobsQoSMap.get (qos).remove (exitingJob);
    if (this.jobsQoSMap.get (qos).isEmpty ())
      this.jobsQoSMap.remove (qos);
    this.jobQueue.remove (exitingJob);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: RESCHEDULE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the job (if any) eligible for execution that has highest priority, respecting server-access credits.
   * 
   * <p>
   * This method effectively determines the job that <i>should</i> be in service in {@link PQ}
   * by examining (solely)
   * {@link #jobQueue},
   * {@link #jobsQoSMap},
   * {@link #jobsInServiceArea},
   * and {@link #hasServerAcccessCredits}.
   * 
   * <p>
   * Note that ties between executable jobs with equal QoS values are broken by the order in which the jobs appear
   * in an iterator over the applicable value set in {@link #jobsQoSMap}
   * (in the obvious sense that the job that first appears in the iterator is given highest priority).
   * 
   * <p>
   * This method does (some) sanity checks on {@link #jobsQoSMap} on the fly.
   * 
   * @return The job (if any) eligible for execution that has highest priority, respecting server-access credits;
   *         {@code null} if there is no such job.
   * 
   */
  protected final J getExecutableJobWithHighestPriority ()
  {
    for (final Set<J> jobsP: this.jobsQoSMap.values ())
      if (jobsP == null || jobsP.isEmpty ())
        throw new IllegalStateException ();
      else
        for (final J job : jobsP)
          if (job == null || ! this.jobQueue.contains (job))
            throw new IllegalStateException ();
          else if (this.jobsInServiceArea.contains (job) || hasServerAcccessCredits ())
            return job;
    return null;
  }
  
  /** Reschedules through assessment of which job to serve.
   * 
   * <p>
   * Repeatedly (until they match) confronts the job to serve as obtained through {@link #getExecutableJobWithHighestPriority}
   * with the job currently in service.
   * If there is a mismatch, it preempts the job in service, in favor of the job that is to be served,
   * starting (i.e., admitting it to the service area) the latter if needed.
   * If the newly executing job requests zero service time, it departs immediately.
   * 
   * @param time The (current) time.
   * 
   * @see #jobsBeingServed
   * @see #getExecutableJobWithHighestPriority
   * @see #preemptJob
   * @see #jobsInServiceArea
   * @see #takeServerAccessCredit
   * @see SimJob#getServiceTime
   * @see #remainingServiceTime
   * @see #startServiceChunk
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * @see #fireIfNewNoWaitArmed
   * 
   */
  protected final void reschedule (final double time)
  {
    if (this.jobsBeingServed.keySet ().size () > 1)
      throw new IllegalStateException ();
    final Set<J> startedJobs = new LinkedHashSet<> ();
    final Set<J> departedJobs = new LinkedHashSet<> ();
    J jobBeingServed = (this.jobsBeingServed.isEmpty () ? null : this.jobsBeingServed.keySet ().iterator ().next ());
    J jobToServe = getExecutableJobWithHighestPriority (); // Considers server-access credits!
    while (jobToServe != jobBeingServed)
    {
      if (this.jobsBeingServed.keySet ().size () > 1)
        throw new IllegalStateException ();
      if (jobBeingServed != null && jobToServe == null)
        throw new IllegalStateException ();
      if (jobBeingServed != null)
      {
        // Note that preemptJob may already reschedule in case of DROP and DEPART preemption policies (for instance)!
        preemptJob (time, jobBeingServed);
        jobBeingServed = null;
        jobToServe = getExecutableJobWithHighestPriority (); // Considers server-access credits!
        if (jobToServe == jobBeingServed)
          break;
      }
      if (jobToServe != null && ! this.jobsBeingServed.keySet ().contains (jobToServe))
      {
        if (! this.jobsInServiceArea.contains (jobToServe))
        {
          // Scheduling section; make sure we do not issue notifications.
          takeServerAccessCredit (false);
          startedJobs.add (jobToServe);
          this.jobsInServiceArea.add (jobToServe);
          final double jobServiceTime = jobToServe.getServiceTime (this);
          if (jobServiceTime < 0)
            throw new RuntimeException ();
          this.remainingServiceTime.put (jobToServe, jobServiceTime);          
          if (jobServiceTime == 0.0)
          {
            removeJobFromQueueUponDeparture (jobToServe, time);
            jobToServe.setQueue (null);
            departedJobs.add (jobToServe);
            jobToServe = null;
          }
        }
        if (jobToServe != null)
          startServiceChunk (time, jobToServe);
      }      
      jobBeingServed = (this.jobsBeingServed.isEmpty () ? null : this.jobsBeingServed.keySet ().iterator ().next ());
      jobToServe = getExecutableJobWithHighestPriority (); // Considers server-access credits!
    }
    // Notification section.
    for (J j : startedJobs)
      fireStart (time, j, (Q) this);
    if (! startedJobs.isEmpty ())
      fireIfOutOfServerAccessCredits (time);
    for (final J j : departedJobs)
      fireDeparture (time, j, (Q) this);
    fireIfNewNoWaitArmed (time, isNoWaitArmed ());
  }
  
}
