package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimQueue;

/** A queue-access-vacation start notification.
 *
 */
public class SimQueueStartQueueAccessVacationNotification
extends AbstractSimQueueNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** An queue-access-vacation start notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueStartQueueAccessVacationNotification (final SimQueue queue)
  {
    super (queue);
    setName ("@" + getTime () + ": QAV_START" + "@" + queue);
  }

}
