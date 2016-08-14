package nl.jdj.jqueues.r5.entity.queue.preemptive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.util.collection.HashMapWithPreImageAndOrderedValueSet;
import nl.jdj.jsimulation.r5.SimEventList;

/** Partial implementation of a preemptive {@link SimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractPreemptiveSimQueue
  <J extends SimJob, Q extends AbstractPreemptiveSimQueue>
  extends AbstractSimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a preemptive queue given an event list and preemption strategy.
   *
   * <p>
   * The constructor registers a pre-update hook that updates the remaining service time.
   * 
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   * @see #updateRemainingServiceTime
   * 
   */
  protected AbstractPreemptiveSimQueue (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList);
    if (preemptionStrategy == null)
      this.preemptionStrategy = AbstractPreemptiveSimQueue.DEFAULT_PREEMPTION_STRATEGY;
    else
      this.preemptionStrategy = preemptionStrategy;
    registerPreUpdateHook (this::updateRemainingServiceTime);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PREEMPTION STRATEGY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The default preemption strategy, if none was specified upon construction.
   * 
   * The default is preemptive-resume ({@link PreemptionStrategy#RESUME}).
   * 
   */
  public static final PreemptionStrategy DEFAULT_PREEMPTION_STRATEGY = PreemptionStrategy.RESUME;
  
  /** The preemption strategy.
   * 
   * <p>
   * The preemption strategy is non-{@code null} and fixed upon construction; if cannot be modified.
   * 
   */
  private final PreemptionStrategy preemptionStrategy;
  
  /** Gets the preemption strategy.
   * 
   * <p>
   * The preemption strategy is non-{@code null} and fixed upon construction; if cannot be modified.
   * 
   * @return The preemption strategy.
   * 
   */
  public final PreemptionStrategy getPreemptionStrategy ()
  {
    return this.preemptionStrategy;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TOLERANCE IN REMAINING SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The tolerance for rounding errors in the remaining service time.
   * 
   */
  public final static double TOLERANCE_RST = 1.0E-9;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REMAINING SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The mapping from jobs in {@link #getJobsInServiceArea} to their respective remaining service times.
   * 
   * <p>
   * The key-set of this map must always be identical to {@link #getJobsInServiceArea}.
   * 
   * <p>
   * The special extensions to <code>TreeMap</code> allow for efficient  determination of the pre-images of
   * remaining service times.
   * 
   */
  protected final HashMapWithPreImageAndOrderedValueSet<J, Double> remainingServiceTime
    = new HashMapWithPreImageAndOrderedValueSet<> ();

  /** Updates the remaining service time of executing jobs in the service area.
   * 
   * <p>
   * This method is called as an pre-update hook, and not meant to be called from user code (in sub-classes).
   * It is left protected for {@code javadoc}.
   * 
   * @param newTime The new time.
   * 
   * @see #jobsBeingServed
   * @see #remainingServiceTime
   * @see AbstractPreemptiveSimQueue#TOLERANCE_RST
   * @see #registerPreUpdateHook
   * 
   */
  protected final void updateRemainingServiceTime (final double newTime)
  {
    for (final Map.Entry<J, Double> entry : this.jobsBeingServed.entrySet ())
    {
      final J job = entry.getKey ();
      final double dT = newTime - entry.getValue ();
      if (dT < 0)
        throw new IllegalStateException ();
      else if (dT > 0)
      {
        if (this.remainingServiceTime.get (job) < 0
        ||  this.remainingServiceTime.get (job) < dT - AbstractPreemptiveSimQueue.TOLERANCE_RST)
          throw new IllegalStateException ();
        else if (this.remainingServiceTime.get (job) < dT)
          this.remainingServiceTime.put (job, 0.0);
        else
          this.remainingServiceTime.put (job, this.remainingServiceTime.get (job) - dT);
        entry.setValue (newTime);
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS BEING SERVED
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The jobs currently being served by a server, mapped onto the start time of <i>this service chunk</i>.
   * 
   */
  protected final Map<J, Double> jobsBeingServed = new HashMap<> ();
  
  /** Gets the set of jobs currently being served by a server.
   * 
   * <p>
   * The assumption in this class is that a server can serve at most one job,
   * and that the full unit-capacity of a server is used to serve the
   * job in service (if any).
   * 
   * @return The set of jobs currently being served by a server.
   * 
   */
  public final Set<J> getJobsBeingServed ()
  {
    return this.jobsBeingServed.keySet ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method and removes all jobs from internal data structures.
   * 
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.remainingServiceTime.clear ();
    this.jobsBeingServed.clear ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PREEMPTION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Preempts a job in {@link #jobsBeingServed}, taking actions depending on the preemption
   *  strategy of this queue.
   * 
   * @param time The (current) time.
   * @param job  The job to preempt.
   * 
   * @throws IllegalArgumentException      If the job is {@code null} or not in {@link #jobsBeingServed}.
   * @throws UnsupportedOperationException If the preemption strategy is {@link PreemptionStrategy#REDRAW}
   *                                         of {@link PreemptionStrategy#CUSTOM}.
   * 
   * @see #jobsBeingServed
   * @see #remainingServiceTime
   * @see #getPreemptionStrategy
   * @see #drop
   * @see #cancelDepartureEvent
   * @see #getServiceTimeForJob
   * @see #depart
   * 
   */
  protected final void preemptJob (final double time, final J job)
  {
    if (job == null || ! this.jobsBeingServed.keySet ().contains (job))
      throw new IllegalArgumentException ();
    if (! this.remainingServiceTime.containsKey (job))
      throw new IllegalStateException ();
    switch (getPreemptionStrategy ())
    {
      case DROP:
        drop (job, time);
        break;
      case RESUME:
        this.jobsBeingServed.remove (job);
        cancelDepartureEvent (job);
        break;
      case RESTART:
        this.jobsBeingServed.remove (job);
        cancelDepartureEvent (job);
        this.remainingServiceTime.put (job, getServiceTimeForJob (job));
        break;
      case REDRAW:
        throw new UnsupportedOperationException ("PreemptionStrategy.REDRAW is not supported.");
      case DEPART:
        depart (time, job);      
        break;
      case CUSTOM:
        throw new UnsupportedOperationException ("PreemptionStrategy.CUSTOM is not supported yet.");        
      default:
        throw new RuntimeException ();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START SERVICE CHUNK
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Starts execution of a job in {@link #getJobsInServiceArea}, until it departs or until it is preempted.
   * 
   * <p>
   * The job to execute departs immediately if its remaining service time is zero.
   * 
   * @param time The (current) time.
   * @param job  The job to start executing.
   * 
   * @throws IllegalArgumentException If the job is {@code null}, not in the service area, or already being executed.
   * 
   * @see #jobsBeingServed
   * @see #remainingServiceTime
   * @see AbstractPreemptiveSimQueue#TOLERANCE_RST
   * @see #scheduleDepartureEvent
   * @see #depart
   * 
   */
  protected final void startServiceChunk (final double time, final J job)
  {
    if (job == null || this.jobsBeingServed.keySet ().contains (job))
      throw new IllegalArgumentException ();
    if (! this.remainingServiceTime.containsKey (job))
      throw new IllegalStateException ();
    this.jobsBeingServed.put (job, time);
    final double rs_job = this.remainingServiceTime.get (job);
    if (rs_job > 0 + AbstractPreemptiveSimQueue.TOLERANCE_RST)
      scheduleDepartureEvent (time + rs_job, job);
    else
      depart (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #removeJobFromQueueUponExit} (final implementation).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponExit (job, time);
  }

  /** Invokes {@link #rescheduleAfterExit} (final implementation).
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    rescheduleAfterExit (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #removeJobFromQueueUponExit} (final implementation).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    removeJobFromQueueUponExit (job, time);
  }

  /** Invokes {@link #rescheduleAfterExit} (final implementation).
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    rescheduleAfterExit (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #removeJobFromQueueUponExit} (final implementation).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponExit (departingJob, time);
  }
  
  /** Invokes {@link #rescheduleAfterExit} (final implementation).
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    rescheduleAfterExit (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes a job from the internal administration upon its exit from this queue (to be implemented by concrete subclasses).
   * 
   * <p>
   * The job may have been dropped, successfully revoked, or it may just depart.
   * 
   * <p>
   * This is the central entry point for
   * {@link #removeJobFromQueueUponDrop},
   * {@link #removeJobFromQueueUponRevokation},
   * {@link #removeJobFromQueueUponDeparture}.
   * Implementations must take into account the cancellation of departure (and other) events.
   * 
   * @param exitingJob The job leaving.
   * @param time       The (current) time.
   * 
   * @see #removeJobFromQueueUponDrop
   * @see #removeJobFromQueueUponRevokation
   * @see #removeJobFromQueueUponDeparture
   * @see #rescheduleAfterExit
   * @see #getDepartureEvents
   * 
   */
  protected abstract void removeJobFromQueueUponExit (J exitingJob, double time);
 
  /** Reschedules after a job has left this queue (to be implemented by concrete subclasses).
   * 
   * <p>
   * The job may have been dropped, successfully been revoked, or departed.
   * 
   * <p>
   * This is the central entry point for
   * {@link #rescheduleAfterDrop},
   * {@link #rescheduleAfterRevokation},
   * {@link #rescheduleAfterDeparture}.
   * Implementations must typically check whether to start a new service chunk {@link #startServiceChunk}.
   * 
   * @param time The (current) time.
   * 
   * @see #rescheduleAfterDrop
   * @see #rescheduleAfterRevokation
   * @see #rescheduleAfterDeparture
   * @see #removeJobFromQueueUponExit
   * @see #startServiceChunk
   * 
   */
  protected abstract void rescheduleAfterExit (double time);
  
}
