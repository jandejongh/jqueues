package nl.jdj.jsimulation.v1.jobqueue;

import nl.jdj.jsimulation.v1.SimEventAction;

/** A {@link SimJob} represents an amount (finite) of work to be carried out by one
 *  or more {@link SimQueue}s.
 *
 * At any point in time, a {@link SimJob} can be visiting at most one
 * {@link SimQueue}.
 *
 */
public interface SimJob
{

  /** The queue this job is currently visiting.
   *
   * @return The {@link SimQueue} this {@link SimJob} is currently visiting or
   *         {@code null}.
   */
  public SimQueue getQueue ();

  /** The requested service time at given queue.
   *
   * This method is used by a {@link SimQueue} to query the requested service time,
   *   and appropriately schedule a departure event for the job.
   * It should not change during a visit to a {@link SimQueue}, and it is not
   *   manipulated by the queue being visited, in other words, it cannot be used
   *   to query the remaining service time of a job at a queue.
   * It is safe to change this in-between queue visits.
   *
   * @param queue The {@link SimQueue} for which the service time is requested,
   *              if {@code null}, the service time at the current queue is used,
   *              or zero if the job is not currently visiting a queue.
   * @return The service time at given queue.
   *
   * @throws IllegalArgumentException If the queue supplied cannot serve the job.
   *
   * @see #getQueue
   *
   */
  public double getServiceTime (SimQueue queue)
    throws IllegalArgumentException;

  /** Set the queue being visited.
   *
   * This method is for private use by {@link SimQueue}s and subclasses,
   * and should not be used elsewhere.
   * It is set by the {@link SimQueue} upon arrival of the {@link SimJob}.
   *
   * @param queue The queue being visited, may be null as a result of a departure event.
   *
   * @see SimQueue#arrive
   *
   * @throws IllegalStateException If the job is already visiting another {@link SimQueue},
   *                               and the supplied argument is not {@code null}.
   *
   */
  public void setQueue (SimQueue queue)
    throws IllegalStateException;

  /** The job-supplied action upon arrival at a queue.
   *
   * @return The job-supplied action upon arrival at a queue, or {@code null}.
   *
   * @see SimQueue#arrive
   *
   */
  public SimEventAction<SimJob>  getQueueArriveAction ();

  /** The job-supplied action upon revokation from a queue.
   *
   * @return The job-supplied action upon revokation from a queue, or {@code null}.
   *
   * @see SimQueue#revoke
   *
   */
  public SimEventAction<SimJob>  getQueueRevokeAction ();

  /** The job-supplied action upon starting service at a queue.
   *
   * @return The job-supplied action upon starting service at a queue, or {@code null}.
   *
   */
  public SimEventAction<SimJob>  getQueueStartAction ();

  /** The job-supplied action upon departure from a queue.
   *
   * @return The job-supplied action upon departure from a queue, or {@code null}.
   *
   */
  public SimEventAction<SimJob>  getQueueDepartAction ();

}
