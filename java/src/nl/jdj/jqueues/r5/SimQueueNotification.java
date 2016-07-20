package nl.jdj.jqueues.r5;

/** A notification from a {@link SimQueue}.
 * 
 * @see SimQueue
 *
 */
public interface SimQueueNotification
extends SimEntityNotification
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the {@link SimQueue} that issued the notification.
   * 
   * <p>
   * The default and only allowed implementation of this method is {@code return (SimQueue) getEntity ();}.
   * 
   * @return The result from {@link #getEntity}, cast to a {@link SimQueue}.
   *
   * @see #getEntity
   * 
   */
  default SimQueue getQueue ()
  {
    return (SimQueue) getEntity ();
  }
  
}
