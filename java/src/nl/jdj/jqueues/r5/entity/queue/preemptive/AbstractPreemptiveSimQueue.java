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
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  protected AbstractPreemptiveSimQueue (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList);
    if (preemptionStrategy == null)
      this.preemptionStrategy =AbstractPreemptiveSimQueue.DEFAULT_PREEMPTION_STRATEGY;
    else
      this.preemptionStrategy = preemptionStrategy;
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
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method and removes all jobs from internal data structures.
   * 
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * 
   */
  @Override
  public void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.remainingServiceTime.clear ();
    this.jobsBeingServed.clear ();
  }

  /** Preempts a job in {@link #jobsBeingServed}, taking actions depending on the preemption
   * strategy of this queue.
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
   * @see SimJob#getServiceTime
   * @see #cancelDepartureEvent
   * @see #drop
   * @see #depart
   * 
   */
  protected final void preemptJob (final double time, final J job)
  {
    if (job == null || ! this.jobsBeingServed.keySet ().contains (job))
      throw new IllegalArgumentException ();
    if (! this.remainingServiceTime.containsKey (job))
      throw new IllegalStateException ();
    boolean mustDrop = false;
    boolean mustDepart = false;
    double newRemainingServiceTime = 0.0;
    switch (getPreemptionStrategy ())
    {
      case DROP:
        mustDrop = true;
        break;
      case RESUME:
        newRemainingServiceTime = this.remainingServiceTime.get (job) - (time - this.jobsBeingServed.get (job));
        break;
      case RESTART:
        newRemainingServiceTime = job.getServiceTime (this);
        break;
      case REDRAW:
        throw new UnsupportedOperationException ("PreemptionStrategy.REDRAW is not supported.");
      case DEPART:
        mustDepart = true;
        break;
      case CUSTOM:
        throw new UnsupportedOperationException ("PreemptionStrategy.CUSTOM is not supported yet.");        
      default:
        throw new RuntimeException ();
    }
    if (mustDrop)
    {
      // Manually remove the job here, as if it was not being served.
      this.jobsBeingServed.remove (job);
      if (! getDepartureEvents (job).isEmpty ())
        cancelDepartureEvent (job);
      drop (job, time);
    }
    else if (mustDepart)
    {
      // Manually remove the job here, as if it was not being served.
      this.jobsBeingServed.remove (job);
      if (! getDepartureEvents (job).isEmpty ())
        cancelDepartureEvent (job);
      depart (time, job, true);
    }
    else
    {
      this.remainingServiceTime.put (job, newRemainingServiceTime);
      this.jobsBeingServed.remove (job);
      cancelDepartureEvent (job);
    }
  }
  
  /** Starts execution of a job in {@link #getJobsInServiceArea}, until it departs or until it is preempted.
   * 
   * @param time The (current) time.
   * @param job  The job to start executing.
   * 
   * @throws IllegalArgumentException If the job is {@code null}, not in the service area, or already being executed.
   * 
   * @see #jobsBeingServed
   * @see #remainingServiceTime
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void startServiceChunk (final double time, final J job)
  {
    if (job == null || this.jobsBeingServed.keySet ().contains (job))
      throw new IllegalArgumentException ();
    if (! this.remainingServiceTime.containsKey (job))
      throw new IllegalStateException ();
    this.jobsBeingServed.put (job, time);
    scheduleDepartureEvent (time + this.remainingServiceTime.get (job), job);
  }

  /** Invokes {@link #removeJobFromQueueUponExit} (default implementation).
   * 
   */
  @Override
  protected void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponExit (job, time);
  }

  /** Invokes {@link #removeJobFromQueueUponExit} (default implementation),
   * unless the job is in the service area and the {@code interruptService} flag is set to {@code false}.
   * 
   */
  @Override
  protected boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (this.jobsInServiceArea.contains (job) && ! interruptService)
      return false;
    removeJobFromQueueUponExit (job, time);
    return true;
  }

  /** Invokes {@link #removeJobFromQueueUponExit} (default implementation).
   * 
   */
  @Override
  protected void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponExit (departingJob, time);
  }
  
  /** Takes appropriate actions upon a job leaving the queue (to be implemented by concrete subclasses).
   * 
   * <p>
   * The job may have been dropped, successfully revoked, or it may just depart.
   * 
   * <p>
   * This is the default central entry point for
   * {@link #removeJobFromQueueUponDrop},
   * {@link #removeJobFromQueueUponRevokation},
   * {@link #removeJobFromQueueUponDeparture}.
   * Implementations must take into account the cancellation of departure events.
   * 
   * @param exitingJob The job leaving.
   * @param time       The (current) time.
   * 
   * @see #getDepartureEvents
   * 
   */
  protected abstract void removeJobFromQueueUponExit (J exitingJob, double time);
  
}
