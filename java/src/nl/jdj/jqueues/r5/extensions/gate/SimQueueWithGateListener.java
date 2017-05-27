package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueListener;

/** A listener to state changes of a {@link SimQueueWithGate}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public interface SimQueueWithGateListener<J extends SimJob, Q extends SimQueue>
extends SimQueueListener<J, Q>
{
 
  /** Notification of a change of in status (in terms of open/close) of the gate of a {@link SimQueueWithGate}.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * @param open  Whether the gate opened ({@code true}) or closed ({@code false}).
   * 
   * @see SimQueueWithGate#setGatePassageCredits
   * 
   */
  public void notifyNewGateStatus (double time, Q queue, boolean open);
  
}
