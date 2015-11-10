package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** A {@link SimQueue} with the notion of a single gate that can be opened and closed.
 * 
 * <p>
 * Typically, but not necessarily, used to let {@link SimJob}s pass when the gate is open,
 * and let them wait when the gate is closed.
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
  
  /** Opens the gate.
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
  
}
