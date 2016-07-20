package nl.jdj.jqueues.r5;

/** A {@link SimJob}-related notification from a {@link SimQueue}.
 * 
 * @see SimJob
 * @see SimQueue
 *
 */
public interface SimQueueJobNotification
extends SimQueueNotification
{
  
  /** Returns the {@link SimJob} to which the notification applies.
   * 
   * @return The {@link SimJob} to which the notification applies.
   *
   */
  SimJob getJob ();
  
}
