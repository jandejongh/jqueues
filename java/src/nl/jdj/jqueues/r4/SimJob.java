package nl.jdj.jqueues.r4;

/** A {@link SimJob} represents an amount of work to be carried out by one
 *  or more {@link SimQueue}s.
 *
 * At any point in time, a {@link SimJob} can be visiting at most one
 * {@link SimQueue}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimJob<J extends SimJob, Q extends SimQueue>
extends SimEntity<J, Q>
{

  /** The queue this job is currently visiting.
   *
   * @return The {@link SimQueue} this {@link SimJob} is currently visiting or
   *         {@code null}.
   */
  public Q getQueue ();

  /** The requested service time at given queue.
   *
   * <p>
   * This method is used by a {@link SimQueue} to query the requested service time,
   *   and appropriately schedule a departure event for the job,
   *   but can be called anytime.
   * The returned value should not change during a visit to a {@link SimQueue},
   *   and it is not manipulated by the queue being visited, in other words,
   *   it cannot be used to query the remaining service time of a job at a queue.
   * 
   * <p>
   * It is safe to change this in-between queue visits.
   * However, the convention is that the method then returns the required service time at the <i>next</i> visit to
   * the queue.
   * For instance, many test and job-factory classes depend on this, as they often directly probe a non-visiting job for
   * its required service time at a queue.
   * Obviously, implementations must be prepared for invocations of this method while not visiting a queue.
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
  public double getServiceTime (Q queue)
    throws IllegalArgumentException;

  /** Set the queue being visited.
   *
   * <p>
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
  public void setQueue (Q queue)
    throws IllegalStateException;

}
