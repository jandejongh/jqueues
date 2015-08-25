package nl.jdj.jqueues.r3;

/** A listener to various vacation types of a {@link SimQueue}.
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

}
