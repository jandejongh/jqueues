package nl.jdj.jqueues.r4;

import nl.jdj.jqueues.r4.stat.AbstractSimQueueStat;

/** A listener to state changes of one or multiple {@link SimQueue}s.
 *
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
 
  /** Notification of an immediate upcoming update at a queue.
   * 
   * An update at a queue is defined as a moment in time at which the queue is about to change its state.
   * The main advantage of an update notification is that you can inspect the queue right before it is about to change.
   * This leads the way to maintaining time-based statistics on the queue.
   * 
   * <p>The general contract is that in between update notifications, the queue does not change its state.
   * However, what exactly comprises the state of the queue has to be documented by concrete implementations.
   * 
   * @param time  The time of the update.
   * @param queue The queue that is about to be updated.
   * 
   * @see AbstractSimQueueStat
   * 
   */
  public void notifyUpdate (double time, Q queue);
  
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
