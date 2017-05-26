package nl.jdj.jqueues.r5.entity.jq;

import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A listener to one or multiple {@link SimQueue}s and/or {@link SimJob}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public interface SimJQListener<J extends SimJob, Q extends SimQueue>
extends SimEntityListener
{
  
  /** Notification of the arrival of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimQueue#arrive
   * 
   */
  public void notifyArrival (double time, J job, Q queue);
  
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
  
  /** Notification of the auto-revocation of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimQueue#getAutoRevocationPolicy
   * 
   */
  public void notifyAutoRevocation (double time, J job, Q queue);
  
  /** Notification of the start of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  public void notifyStart (double time, J job, Q queue);
  
  /** Notification of the departure of a job at a queue.
   * 
   * @param time  The (current) time.
   * @param job   The job.
   * @param queue The queue.
   * 
   */
  public void notifyDeparture (double time, J job, Q queue);
  
}
