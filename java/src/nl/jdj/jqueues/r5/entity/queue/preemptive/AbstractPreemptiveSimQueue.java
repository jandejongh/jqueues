package nl.jdj.jqueues.r5.entity.queue.preemptive;

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
    // XXX
    throw new UnsupportedOperationException ("Preemptive SimQueues are not implemented (yet).");
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
  
  /** The mapping from jobs executing (in {@link #jobsExecuting}) to their respective remaining service times.
   * 
   * <p>
   * The key-set of this map must always be identical to {@link #jobsExecuting}.
   * 
   * <p>
   * The special extensions to <code>TreeMap</code> allow for efficient  determination of the pre-images of
   * remaining service times.
   * 
   */
  protected final HashMapWithPreImageAndOrderedValueSet<J, Double> remainingServiceTime
    = new HashMapWithPreImageAndOrderedValueSet<> ();

}
