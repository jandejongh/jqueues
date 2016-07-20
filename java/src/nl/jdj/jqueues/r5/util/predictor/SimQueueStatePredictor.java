package nl.jdj.jqueues.r5.util.predictor;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** An object capable of predicting aspects of the state of one or more {@link SimQueue}s.
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimQueuePredictor
 * 
 */
public interface SimQueueStatePredictor<Q extends SimQueue>
{
  
  /** Checks whether a given state represents a queue-state vacation on given queue.
   * 
   * <p>
   * The default implementation returns {@code queueState.isQueueAccessVacation ()}.
   * 
   * @param queue      The queue.
   * @param queueState The queue state, non-{@code null}.
   * 
   * @return True if the state represents a queue-access vacation at given queue.
   * 
   * @throws IllegalArgumentException If any of the arguments is {@code null}.
   * 
   */
  public default boolean isQueueAccessVacation (Q queue, SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return queueState.isQueueAccessVacation ();
  }
  
}
