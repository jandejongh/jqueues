package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimQueue;

/** A gained-no-wait-armed notification.
 *
 */
public class SimQueueGainedNoWaitArmedNotification
extends AbstractSimQueueNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A gained-no-wait-armed notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueGainedNoWaitArmedNotification (final SimQueue queue)
  {
    super (queue);
    setName ("@" + getTime () + ": GAINED_NWA" + "@" + queue);
  }

}
