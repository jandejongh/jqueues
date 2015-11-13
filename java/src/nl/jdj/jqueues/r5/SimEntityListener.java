package nl.jdj.jqueues.r5;

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
