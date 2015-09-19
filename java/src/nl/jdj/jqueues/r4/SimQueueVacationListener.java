package nl.jdj.jqueues.r4;

/** A listener to various vacation types of a {@link SimQueue}.
 *
 * @see SimQueueListener
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueVacationListener<J extends SimJob, Q extends SimQueue>
extends SimQueueListener<J, Q>
{

  /** Notification of the start of a queue-access vacation.
   * 
   * @param t The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#startQueueAccessVacation()
   * @see SimQueue#startQueueAccessVacation(double)
   * 
   */
  public void notifyStartQueueAccessVacation (double t, Q queue);
  
  /** Notification of the end of a queue-access vacation.
   * 
   * @param t The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#stopQueueAccessVacation
   * 
   */
  public void notifyStopQueueAccessVacation (double t, Q queue);

  /** Notification that a {@link SimQueue} has run out of server-access credits.
   * 
   * @param t The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  public void notifyOutOfServerAccessCredits (double t, Q queue);
  
  /** Notification that a {@link SimQueue} has regained server-access credits.
   * 
   * @param t The (current) time.
   * @param queue The queue.
   * 
   */
  public void notifyRegainedServerAccessCredits (double t, Q queue);
  
}