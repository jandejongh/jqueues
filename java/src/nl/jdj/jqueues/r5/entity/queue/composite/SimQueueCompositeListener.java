package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;

/** A listener to state changes of one or multiple {@link SimQueueComposite}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueCompositeListener<J extends SimJob, Q extends SimQueue>
extends SimQueueListener<J, Q>
{
 
  /** Notification of the pseudo-arrival of a job at a composite queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimQueue#arrive
   * 
   */
  void notifyPseudoArrival (double time, J job, Q queue);
  
  /** Notification of pseudo-the start of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  void notifyPseudoStart (double time, J job, Q queue);
  
  /** Notification of the pseudo-drop of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  void notifyPseudoDrop (double time, J job, Q queue);
  
  /** Notification of the (successful) pseudo-revocation of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  void notifyPseudoRevocation (double time, J job, Q queue);
  
  /** Notification of the pseudo-auto-revocation of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  void notifyPseudoAutoRevocation (double time, J job, Q queue);
  
  /** Notification of the pseudo-departure of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  void notifyPseudoDeparture (double time, J job, Q queue);
  
}
