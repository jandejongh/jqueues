package nl.jdj.jqueues.r2;

import nl.jdj.jqueues.r2.stat.AbstractSimQueueStat;

/** A listener to state changes of one or multiple {@link SimQueue}s.
 *
 * This interface specifies callback methods for the most basic events that can change the state of a {@link SimQueue}.
 * More sophisticated queue types will require an extension to this interface in order to capture all relevant state-changing
 * events.
 * 
 * @param <J> The type of {@link SimJobs}s supported.
 * @param <Q> The type of {@link SimQueues}s supported.
 * 
 */
public interface SimQueueListener<J extends SimJob, Q extends SimQueue>
{
 
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
  public void update (double t, Q queue);
  
  /** Notification of the arrival of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void arrival (double t, J job, Q queue);
  
  /** Notification of the start of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void start (double t, J job, Q queue);
  
  /** Notification of the drop of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void drop (double t, J job, Q queue);
  
  /** Notification of the revocation of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void revocation (double t, J job, Q queue);
  
  /** Notification of the departure of a job at a queue.
   * 
   * @param t The (current) time.
   * @param job The job.
   * @param queue The queue.
   * 
   */
  public void departure (double t, J job, Q queue);
  
}
