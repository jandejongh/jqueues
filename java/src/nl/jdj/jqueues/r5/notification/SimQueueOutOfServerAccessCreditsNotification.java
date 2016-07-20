package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimQueue;

/** An out-of-server-access-credits notification.
 *
 */
public class SimQueueOutOfServerAccessCreditsNotification
extends AbstractSimQueueNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** An out-of-server-access-credits notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueOutOfServerAccessCreditsNotification (final SimQueue queue)
  {
    super (queue);
    setName ("@" + getTime () + ": OUT_OF_SAC" + "@" + queue);
  }

}
