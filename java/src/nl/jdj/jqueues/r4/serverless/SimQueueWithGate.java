package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** A {@link SimQueue} with the notion of a single gate
 *  that can be opened (optionally for a limited number of passages) and closed.
 * 
 * <p>
 * Typically, but not necessarily, used to let {@link SimJob}s pass when the gate is open,
 * and let them wait while the gate is closed.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see GATE
 * 
 */
public interface SimQueueWithGate<J extends SimJob, Q extends SimQueueWithGate>
extends SimQueue<J, Q>
{
  
  /** Opens the gate (without limits on the number of passages).
   * 
   * @param time The current time.
   * 
   */
  public void openGate (double time);
  
  /** Closes the gate.
   * 
   * @param time The current time.
   * 
   */
  public void closeGate (double time);
  
  /** Opens the gate for a limited number of remaining passages.
   * 
   * <p>
   * Note that setting the remaining number of passages to zero effectively closes the gate,
   * and setting it to {@link Integer#MAX_VALUE} open it without limits on the number of passages.
   * 
   * <p>
   * If a {@link SimQueue} does not support this operation, it is to consider every strictly positive value
   * as {@link Integer#MAX_VALUE}, effectively opening the gate without limits on the number of passages.
   * 
   * @param time             The current time.
   * @param numberOfPassages The remaining number of passages to allow (will override, not add to, any previous value),
   *                         with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If the number of passages passed is strictly negative.
   * 
   */
  public void openGate (double time, int numberOfPassages);
  
}
