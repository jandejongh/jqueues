package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
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
 */
public class SimQueueCatchUpEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
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
  
}
