package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimQueue;

/** A regained-server-access-credits notification.
 *
 */
public class SimQueueRegainedServerAccessCreditsNotification
extends AbstractSimQueueNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A regained-server-access-credits notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueRegainedServerAccessCreditsNotification (final SimQueue queue)
  {
    super (queue);
    setName ("@" + getTime () + ": REGAINED_SAC" + "@" + queue);
  }

}
