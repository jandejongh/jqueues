package nl.jdj.jqueues.r5.entity.queue.qos;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.AbstractEgalitarianProcessorSharingSimQueue;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.PS;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server {@link HOL_PS} queue serves all jobs in the service area simultaneously,
 *  equally distributing its service capacity, but only admits a single job of each QoS class to the service area.
 *
 * <p>
 * Head-of-the-Line Processor Sharing.
 * 
 * <p>
 * The Head-of-the-Line Processor-Sharing queueing system distributes its service capacity equally among the jobs in execution.
 * Jobs start immediately upon arrival, provided that there are server-access credits left,
 * AND no other job with the same QoS class is present (in execution).
 * Upon regaining server-access credits, (eligible) jobs are started in arrival order.
 * 
 * <p>
 * XXX We do not really want P to be Comparable here... Any class/interface should do, including Object.
 * This is a design failure in the QoS structure...
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 *
 * @see HOL
 * @see PS
 * 
 */
public class HOL_PS<J extends SimJob, Q extends HOL_PS, P extends Comparable>
extends AbstractEgalitarianProcessorSharingSimQueue<J, Q>
implements SimQueueQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server HOL-PS queue with infinite buffer size given an event list.
   *
   * @param eventList     The event list to use.
   * @param qosClass      The Java class to use for QoS behavior, non-{@code null}.
   * @param defaultJobQoS The default QoS value to use for non-QoS jobs, non-{@code null}. 
   * 
   * @throws IllegalArgumentException If any of the arguments is <code>null</code>.
   *
   */
  public HOL_PS (final SimEventList eventList, final Class<P> qosClass, final P defaultJobQoS)
  {
    super (eventList, Integer.MAX_VALUE, 1);
    if (qosClass == null || defaultJobQoS == null)
      throw new IllegalArgumentException ();
    this.qosClass = qosClass;
    this.defaultJobQoS = defaultJobQoS;
  }
  
  /** Returns a new {@link HOL_PS} object on the same {@link SimEventList} and the same QoS structure.
   * 
   * @return A new {@link HOL_PS} object on the same {@link SimEventList} and the same QoS structure.
   * 
   * @see #getEventList
   * @see #getQoSClass
   * @see #getDefaultJobQoS
   * 
   */
  @Override
  public HOL_PS<J, Q, P> getCopySimQueue ()
  {
    return new HOL_PS (getEventList (), getQoSClass (), getDefaultJobQoS ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "HOL-PS".
   * 
   * @return "HOL-PS".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "HOL-PS";
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Class<P> qosClass;

  @Override
  public final Class<? extends P> getQoSClass ()
  {
    return this.qosClass;
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoSClass (final Class<? extends P> qosClass)
  {
    SimQueueQoS.super.setQoSClass (qosClass);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code null}, since the QoS value of a queue has no meaning.
   * 
   * @return {@code null}.
   * 
   */
  @Override
  public final P getQoS ()
  {
    return null;
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoS (final P qos)
  {
    SimQueueQoS.super.setQoS (qos);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (DEFAULT) JOB QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final P defaultJobQoS;

  @Override
  public final P getDefaultJobQoS ()
  {
    return this.defaultJobQoS;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOB QoS MAP
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
  
  /** Calls super method and clears {@link #jobsQoSMap}.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.jobsQoSMap.clear ();
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the tail of {@link #jobQueue} and into {@link #jobsQoSMap}.
   * 
   * @see #arrive
   * @see #jobQueue
   * @see #jobsQoSMap
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (! this.jobsQoSMap.containsKey (qos))
      this.jobsQoSMap.put (qos, new LinkedHashSet<> ());
    this.jobsQoSMap.get (qos).add (job);
  }

  /** Starts the arrived job if server-access credits are available and there are no other jobs present with the same QoS value.
   * 
   * @see #hasServerAcccessCredits
   * @see SimQueueQoSUtils#getAndCheckJobQoS
   * @see #jobsQoSMap
   * @see #start
   * @see #jobsInServiceArea
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null || ! getJobsInWaitingArea ().contains (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
    {
      final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
      if (! this.jobsQoSMap.get (qos).contains (job))
        throw new IllegalArgumentException ();
      if (this.jobsQoSMap.get (qos).size () == 1)
        start (time, job);
      else if (! this.jobsInServiceArea.contains (this.jobsQoSMap.get (qos).iterator ().next ()))
        throw new IllegalStateException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not drop jobs.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not drop jobs.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the jobs from the internal data structures.
   * 
   * <p>
   * Core method for removing a job for both revocations and departures from {@link AbstractEgalitarianProcessorSharingSimQueue}.
   * 
   * <p>
   * Removes the job from {@link #jobQueue} and {@link #jobsQoSMap},
   * and if needed from {@link #jobsInServiceArea} and {@link #virtualDepartureTime}.
   * 
   * @see #revoke
   * @see #jobQueue
   * @see #jobsQoSMap
   * @see #jobsInServiceArea
   * @see #virtualDepartureTime
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (this.jobsInServiceArea.contains (job))
    {
      if (! this.virtualDepartureTime.containsKey (job))
        throw new IllegalStateException ();
      this.virtualDepartureTime.remove (job);
      this.jobsInServiceArea.remove (job);
    }
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (! this.jobsQoSMap.get (qos).contains (job))
      throw new IllegalArgumentException ();
    this.jobsQoSMap.get (qos).remove (job);
    if (this.jobsQoSMap.get (qos).isEmpty ())
      this.jobsQoSMap.remove (qos);
    this.jobQueue.remove (job);
  }

  /** Attempts to start a job of the same QoS value as the job that was revoked (or departed);
   *  calls {@link #rescheduleDepartureEvent} if that fails.
   * 
   * <p>
   * Core method for rescheduling after both revocations and departures ({@link #rescheduleAfterDeparture}).
   * 
   * @see #rescheduleAfterDeparture
   * @see SimQueueQoSUtils#getAndCheckJobQoS
   * @see #jobsQoSMap
   * @see #hasServerAcccessCredits
   * @see #jobsInServiceArea
   * @see #start
   * @see #rescheduleDepartureEvent
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (this.jobsQoSMap.containsKey (qos)
      && hasServerAcccessCredits ()
      // Next check is unnecessary for departure handling, but a revocation may have been from the waiting area!
      && ! this.jobsInServiceArea.contains (this.jobsQoSMap.get (qos).iterator ().next ()))
      start (time, this.jobsQoSMap.get (qos).iterator ().next ());
    else
      rescheduleDepartureEvent ();
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

  /** Starts jobs eligible to start in arrival order as long as there are server-access credits.
   * 
   * <p>
   * A job is eligible to start if it resides in the waiting area and the service area currently does not contain
   * a (another) job with the same QoS value.
   * 
   * @see #hasJobsInWaitingArea
   * @see #getJobsInWaitingArea
   * @see SimQueueQoSUtils#getAndCheckJobQoS
   * @see #jobsQoSMap
   * @see #jobsInServiceArea
   * @see #start
   * @see #hasServerAcccessCredits
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    boolean hasStartableJobs = hasJobsInWaitingArea ();
    while (hasStartableJobs)
    {
      boolean hasStartedJob = false;
      // Note: jobs are inserted in jobQueue in arrival order;
      // our super-class (AbstractSimQueue) preserves this ordering on jobsInWaitingArea.
      for (final J job : getJobsInWaitingArea ())
      {
        final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
        if (! this.jobsQoSMap.containsKey (qos))
          throw new IllegalStateException ();
        if (! this.jobsInServiceArea.contains (this.jobsQoSMap.get (qos).iterator ().next ()))
        {
          start (time, this.jobsQoSMap.get (qos).iterator ().next ());
          hasStartedJob = true;
          break;
        }
      }
      hasStartableJobs = hasStartedJob && hasServerAcccessCredits ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code> if there are no jobs in the queue.
   * 
   * @return True if there are no jobs in the queue.
   * 
   * @see #getNumberOfJobs
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return getNumberOfJobs () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #rescheduleAfterRevokation}.
   * 
   * 
   * @see #rescheduleAfterRevokation
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    rescheduleAfterRevokation (departedJob, time);
  }

}
