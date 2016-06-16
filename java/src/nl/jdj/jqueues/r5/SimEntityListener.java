package nl.jdj.jqueues.r5;

import nl.jdj.jqueues.r5.util.stat.AbstractSimQueueStat;

/** A listener to one or multiple {@link SimEntity}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimEntityListener<J extends SimJob, Q extends SimQueue>
{
  
  /** Notification of a reset at a {@link SimEntity}.
   * 
   * @param entity The entity that has been reset.
   * 
   */
  public void notifyResetEntity (SimEntity entity);
  
  /** Notification of an immediate upcoming update at an entity.
   * 
   * An update at an entity is defined as a moment in time at which the entity is about to change its state.
   * The main advantage of an update notification is that you can inspect the entity right before it is about to change.
   * This leads the way to (efficiently) maintaining time-based statistics on the entity.
   * 
   * <p>
   * The general contract is that in between entity notifications, the entity does not change its state.
   * However, what exactly comprises the state of the entity has to be documented by concrete implementations.
   * 
   * <p>
   * Update notifications through {@link #notifyUpdate}
   * and state-change notifications through {@link #notifyStateChanged} are dual in the sense that
   * the former are issued <i>before</i> and the latter <i>after</i>
   * a state change.
   * 
   * <p>
   * Preferably, update and state-change notifications for a single entity must always alternate,
   * starting with a state-change event when starting processing the event list,
   * and each update notification for an entity must be followed by a state-change notification
   * with equal time for that entity (unless the event list is reset at that time).
   * 
   * <p>
   * Both types of notifications should <i>not</i> be sent upon construction or reset of the entity,
   * because in those cases, listeners do not have a proper notion of the "previous state",
   * should they choose to maintain that
   * (and, moreover, the entity enters a default state anyway).
   * 
   * @param time   The time of the update.
   * @param entity The entity that is about to be updated.
   * 
   * @see AbstractSimQueueStat
   * 
   */
  public void notifyUpdate (double time, SimEntity entity);
  
  /** Notification of a (any) state change of a {@link SimEntity}.
   * 
   * <p>
   * Because a {@link SimEntityListener} may not be able to capture <i>all</i> possible types of state changes
   * (because it does not know the full state structure of the entity),
   * this method allows a listener to capture <i>all</i> state changes,
   * regardless of the type of change.
   * 
   * @param time   The current time (the time of the state change).
   * @param entity The entity at which the state changed.
   * 
   * @see #notifyUpdate For more details on when to issue update and state-change notifications.
   * 
   */
  public void notifyStateChanged (double time, SimEntity entity);
    
  /** Notification of the arrival of a job at a queue.
   * 
   * The notification is issued immediately at the time a job arrives at a queue,
   * in other words, at a point where the queue does not even know yet about the existence of the job
   * (and vice versa, for that matter).
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimQueue#arrive
   * 
   */
  public void notifyArrival (double time, J job, Q queue);
  
  /** Notification of the start of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  public void notifyStart (double time, J job, Q queue);
  
  /** Notification of the drop of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  public void notifyDrop (double time, J job, Q queue);
  
  /** Notification of the (successful) revocation of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimQueue#revoke
   * 
   */
  public void notifyRevocation (double time, J job, Q queue);
  
  /** Notification of the departure of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  public void notifyDeparture (double time, J job, Q queue);
  
}
