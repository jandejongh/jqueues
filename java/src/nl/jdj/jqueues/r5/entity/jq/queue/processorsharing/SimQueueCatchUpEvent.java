package nl.jdj.jqueues.r5.entity.jq.queue.processorsharing;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** A catch-up {@link SimEvent} at a queue.
 * 
 * <p>
 * Do not <i>ever</i> schedule this yourself unless for your own implementation;
 * it is for private use by {@link SimQueue} implementations.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see CUPS
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
public class SimQueueCatchUpEvent<J extends SimJob, Q extends SimQueue>
extends SimJQEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a catch-up event at a specific queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
   * 
   * @param queue       The queue at which catch-up occurs.
   * @param catchUpTime The scheduled catch-up time.
   * @param action      The {@link SimEventAction} to take; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>.
   * 
   */
  public SimQueueCatchUpEvent
  (final Q queue, final double catchUpTime, final SimEventAction<J> action)
  {
    super ("CatchUp@" + queue, catchUpTime, queue, null, action);
    if (action == null)
      throw new IllegalArgumentException ();
  }

  /** Throws an {@link UnsupportedOperationException}.
   * 
   * <p>
   * A {@link SimQueueCatchUpEvent} is a queue-internal event.
   * 
   * @throws UnsupportedOperationException Always.
   * 
   */
  @Override
  public final SimJQEvent<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
  {
    throw new UnsupportedOperationException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
