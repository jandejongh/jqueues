package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** A {@link SimQueue} with a gate that counts and limits the number of passages. 
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see GATE
 * 
 */
public interface SimQueueWithCountingGate<J extends SimJob, Q extends SimQueueWithCountingGate>
extends SimQueueWithGate<J, Q>
{
  
  /** Opens the gate for a limited number of remaining passages.
   * 
   * <p>
   * Note that setting the remaining number of passages to zero effectively closes the gate.
   * 
   * @param time             The current time.
   * @param numberOfPassages The remaining number of passages to allow (will override, not add to, any previous value).
   * 
   * @throws IllegalArgumentException If the number of passages passed is strictly negative.
   * 
   */
  public void openGate (double time, int numberOfPassages);
    
}
