package nl.jdj.jqueues.r5;

import nl.jdj.jqueues.r5.util.stat.AbstractSimQueueStat;

/** A listener to state changes of one or multiple {@link SimQueue}s.
 *
 * <p>
 * This interface specifies callback methods for the most basic events that can change the state of a {@link SimQueue}.
 * More sophisticated queue types will require an extension to this interface in order to capture all relevant state-changing
 * events.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueListener<J extends SimJob, Q extends SimQueue>
extends SimEntityListener<J, Q>
{
 
  /** Notification of a change of a {@link SimQueue} <code>noWaitArmed</code> state.
   * 
   * @param time        The (current) time.
   * @param queue       The queue.
   * @param noWaitArmed The new <code>noWaitArmed</code> state.
   * 
   * @see SimQueue#isNoWaitArmed
   * 
   */
  public void notifyNewNoWaitArmed (double time, Q queue, boolean noWaitArmed);
  
  /** Notification of the start of a queue-access vacation.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#startQueueAccessVacation()
   * @see SimQueue#startQueueAccessVacation(double)
   * 
   */
  public void notifyStartQueueAccessVacation (double time, Q queue);
  
  /** Notification of the end of a queue-access vacation.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#stopQueueAccessVacation
   * 
   */
  public void notifyStopQueueAccessVacation (double time, Q queue);

  /** Notification that a {@link SimQueue} has run out of server-access credits.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  public void notifyOutOfServerAccessCredits (double time, Q queue);
  
  /** Notification that a {@link SimQueue} has regained server-access credits.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   */
  public void notifyRegainedServerAccessCredits (double time, Q queue);
  
}
