package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimQueue;

/** A lost-no-wait-armed notification.
 *
 */
public class SimQueueLostNoWaitArmedNotification
extends AbstractSimQueueNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A lost-no-wait-armed notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueLostNoWaitArmedNotification (final SimQueue queue)
  {
    super (queue);
    setName ("@" + getTime () + ": LOST_NWA" + "@" + queue);
  }

}
