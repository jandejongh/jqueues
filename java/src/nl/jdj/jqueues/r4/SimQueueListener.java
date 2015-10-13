package nl.jdj.jqueues.r4;

import nl.jdj.jqueues.r4.stat.AbstractSimQueueStat;

/** A listener to state changes of one or multiple {@link SimQueue}s.
 *
 * This interface specifies callback methods for the most basic events that can change the state of a {@link SimQueue}.
 * More sophisticated queue types will require an extension to this interface in order to capture all relevant state-changing
 * events.
 * 
 * <p>
 * As of r5, a {@link SimQueueListener} is informed about changes in the <code>noWaitArmed</code> state of a {@link SimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueListener<J extends SimJob, Q extends SimQueue>
{
 
  /** Notification of a reset at a queue.
   * 
   * @param oldTime The (old!) time of the reset.
   * @param queue   The queue that has been reset.
   * 
   * @see SimQueue#reset
   * 
   */
  public void notifyReset (double oldTime, Q queue);
  
  /** Notification of an immediate upcoming update at a queue.
   * 
   * An update at a queue is defined as a moment in time at which the queue is about to change its state.
   * The main advantage of an update notification is that you can inspect the queue right before its about to change.
   * This leads the way to maintaining time-based statistics on the queue.
   * 
   * <p>Note that all the other notifications in this interface are only sent right after the event has taken place, and the queue
   * has obtained its new state, meaning that the old state of the queue is unavailable to the method.
   * 
   * <p>The general contract is that in between update notifications, the queue does not change its state.
   * However, what exactly comprises the state of the queue has to be documented by concrete implementations.
   * 
   * @param t The time of the update.
   * @param queue The queue that is about to be updated.
   * 
   * @see AbstractSimQueueStat
   * 
   */
  public void notifyUpdate (double t, Q queue);
  
  /** Notification of the arrival of a job at a queue.
   * 
   * The notification is issued immediately at the time a job arrives at a queue,
   * in other words, at a point where the queue does not even know yet about the existence of the job
   * (and vice versa, for that matter).
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   * @see SimQueue#arrive
   * 
   */
  public void notifyArrival (double t, J job, Q queue);
  
  /** Notification of the start of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void notifyStart (double t, J job, Q queue);
  
  /** Notification of the drop of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void notifyDrop (double t, J job, Q queue);
  
  /** Notification of the revocation of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   * @see SimQueue#revoke
   * 
   */
  public void notifyRevocation (double t, J job, Q queue);
  
  /** Notification of the departure of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void notifyDeparture (double t, J job, Q queue);
  
  /** Notification of a change of a {@link SimQueue} <code>noWaitArmed</code> state.
   * 
   * @param t The (current) time.
   * @param queue The queue.
   * @param noWaitArmed The new <code>noWaitArmed</code> state.
   * 
   * @see SimQueue#isNoWaitArmed
   * 
   */
  public void notifyNewNoWaitArmed (double t, Q queue, boolean noWaitArmed);
  
}
