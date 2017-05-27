package nl.jdj.jqueues.r5.util.predictor;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** An object capable of predicting aspects of the state of one or more {@link SimQueue}s.
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimQueuePredictor
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
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
  public default boolean isQueueAccessVacation (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return queueState.isQueueAccessVacation ();
  }
  
  /** Checks whether a given state represents a queue-state with server-access credits available.
   * 
   * <p>
   * The default implementation returns {@code queueState.getServerAccessCredits () > 0}.
   * 
   * @param queue      The queue.
   * @param queueState The queue state, non-{@code null}.
   * 
   * @return True if the state represents a queue-state with server-access credits available.
   * 
   * @throws IllegalArgumentException If any of the arguments is {@code null}.
   * 
   */
  public default boolean hasServerAccessCredits (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return queueState.getServerAccessCredits () > 0;
  }
  
  /** Checks whether a given state represents {@code StartArmed} on given queue.
   * 
   * @param queue      The queue.
   * @param queueState The queue state, non-{@code null}.
   * 
   * @return True if the state represents {@code StartArmed} at given queue.
   * 
   * @throws IllegalArgumentException If any of the arguments is {@code null}.
   * 
   */
  boolean isStartArmed (Q queue, SimQueueState<SimJob, Q> queueState);
  
}
