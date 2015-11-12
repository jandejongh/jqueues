package nl.jdj.jqueues.r4.extensions.gate;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;

/** A representation of the state of a {@link SimQueueWithGate} while or as if being processed by an event list.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueWithGateState<J extends SimJob, Q extends SimQueueWithGate>
extends SimQueueState<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the remaining number of passage credits for the gate.
   * 
   * <p>
   * Mimics {@link SimQueueWithGate#getGatePassageCredits}.
   * 
   * @return The remaining number of passage credits for the gate; non-negative with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   */
  public int getGatePassageCredits ();
  
  /** Sets the remaining number of passage credits for the gate.
   * 
   * <p>
   * Mimics {@link SimQueueWithGate#openGate(double, int)}.
   * 
   * <p>
   * The time cannot be in the past.
   * 
   * @param time               The time to set the remaining number of passages.
   * @param gatePassageCredits The new remaining number of passage credits for the gate, non-negative,
   *                           with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If time is in the past, or the number of passage credits is (strictly) negative.
   * 
   */
  public void setGatePassageCredits (double time, int gatePassageCredits);
  
}
