package nl.jdj.jqueues.r4.extensions.gate;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.SimQueueListener;

/** A listener to state changes of a {@link SimQueueWithGate}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueWithGateListener<J extends SimJob, Q extends SimQueue>
extends SimQueueListener<J, Q>
{
 
  /** Notification of a change of in status of the gate of a {@link SimQueueWithGate}.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * @param open  Whether the gate opened ({@code true}) or closed ({@link false}).
   * 
   * @see SimQueueWithGate#openGate
   * @see SimQueueWithGate#closeGate
   * 
   */
  public void notifyNewGateStatus (double time, Q queue, boolean open);
  
}
