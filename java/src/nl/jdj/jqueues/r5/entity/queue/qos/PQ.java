package nl.jdj.jqueues.r5.entity.queue.qos;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.preemptive.PreemptionStrategy;
import nl.jdj.jsimulation.r5.SimEventList;

/** The Priority-Queueing queueing discipline with a single server and infinite buffer size.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public class PQ<J extends SimJob, Q extends PQ, P extends Comparable>
extends AbstractPreemptiveSimQueueQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a Priority Queue with infinite buffer size and a single server,
   *  given an event list, preemption strategy, and QoS structure.
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
    super (eventList, Integer.MAX_VALUE, 1, preemptionStrategy, qosClass, defaultJobQoS);
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
  // STATE: JOBS QoS MAP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final NavigableMap<P, Set<J>> jobsQoSMap = new TreeMap<> ();
  
  @Override
  public final NavigableMap<P, Set<J>> getJobsQoSMap ()
  {
    return this.jobsQoSMap;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clear {@link #jobsQoSMap}.
   * 
   * @see #jobsQoSMap
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.jobsQoSMap.clear ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job into {@link #jobQueue} (tail) and {@link #jobsQoSMap}.
   * 
   * @see  SimQueueQoSUtils#getAndCheckJobQoS
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
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (! this.jobsQoSMap.containsKey (qos))
      this.jobsQoSMap.put (qos, new LinkedHashSet<> ());
    this.jobsQoSMap.get (qos).add (job);
  }

  /** Starts the arrived job immediately if it is the executable job (respecting server-access credits) with highest priority.
   * 
   * @see #getExecutableJobWithHighestPriority
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
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (! this.jobsQoSMap.get (qos).contains (job))
      throw new IllegalStateException ();
    if (getExecutableJobWithHighestPriority () == job)
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER ACCCESS CREDITS
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

  /** Invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    reschedule ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are no jobs present in the service area.
   * 
   * @return True if there are no jobs present in the service area.
   * 
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return getNumberOfJobsInServiceArea () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Adds the job to the service area and updates {@link #remainingServiceTime}.
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

  /** Invokes {@link #reschedule}.
   * 
   * @see #remainingServiceTime
   * @see #reschedule
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job))
    || (! this.remainingServiceTime.containsKey (job)))
      throw new IllegalArgumentException ();
    final double jobServiceTime = this.remainingServiceTime.get (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    reschedule ();
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
  // EXIT
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
   * @see SimQueueQoSUtils#getAndCheckJobQoS
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
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (exitingJob, this);
    if (! this.jobsQoSMap.containsKey (qos))
      throw new IllegalStateException ();
    if (! this.jobsQoSMap.get (qos).contains (exitingJob))
      throw new IllegalStateException ();
    this.jobsQoSMap.get (qos).remove (exitingJob);
    if (this.jobsQoSMap.get (qos).isEmpty ())
      this.jobsQoSMap.remove (qos);
    this.jobQueue.remove (exitingJob);
  }
  
  /** Invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleAfterExit (final double time)
  {
    reschedule ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE
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
   * Repeatedly (until they match) confronts
   * the job to serve as obtained through {@link #getExecutableJobWithHighestPriority}
   * with the job currently in service (the only job in {@link #jobsBeingServed}).
   * If there is a mismatch, and if there is a job currently being served,
   * it preempts the latter job through {@link #preemptJob}, and recurs.
   * Otherwise, if there is a mismatch but no job is currently being served,
   * it starts {@link #getExecutableJobWithHighestPriority}
   * either by {@link #start} if the job is not already in {@link #jobsInServiceArea},
   * or by {@link #startServiceChunk} otherwise.
   * 
   * @see #jobsBeingServed
   * @see #getExecutableJobWithHighestPriority
   * @see #preemptJob
   * @see #jobsInServiceArea
   * @see #start
   * @see #startServiceChunk
   * 
   */
  protected final void reschedule ()
  {
    if (this.jobsBeingServed.keySet ().size () > 1)
      throw new IllegalStateException ();
    final J jobBeingServed = (this.jobsBeingServed.isEmpty () ? null : this.jobsBeingServed.keySet ().iterator ().next ());
    final J jobToServe = getExecutableJobWithHighestPriority (); // Considers server-access credits!
    if (jobBeingServed != null && jobToServe == null)
      throw new IllegalStateException ();
    if (jobBeingServed != jobToServe)
    {
      if (jobBeingServed != null)
      {
        // Note that preemptJob may already reschedule in case of DROP and DEPART preemption policies (for instance)!
        preemptJob (getLastUpdateTime (), jobBeingServed);
        reschedule ();
      }
      else
      {
        if (! this.jobsInServiceArea.contains (jobToServe))
          start (getLastUpdateTime (), jobToServe);
        else
          startServiceChunk (getLastUpdateTime (), jobToServe);
      }
    }
  }
  
}
