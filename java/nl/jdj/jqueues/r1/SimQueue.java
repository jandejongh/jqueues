package nl.jdj.jsimulation.v1.jobqueue;

import nl.jdj.jsimulation.v1.SimEventAction;

/** A queue has one or more waiting lines for {@link SimJob}s
 *  and zero or more servers to serve them.
 *
 * <p>
 * {@link SimJob}s can be offered for service through {@link #arrive}.
 * Once a job has been offered, {@link #revoke} tries to revoke the job,
 * if (still) possible.
 *
 * <p>
 * The required service time of the job during a queue visit
 * must be provided by each job through {@link SimJob#getServiceTime}.
 *
 * <p>
 * Note that a {@link SimJob} <i>cannot</i> visit multiple {@link SimQueue}s
 * simultaneously.
 * The {@link SimQueue} currently being visited by
 * a {@link SimJob} can be obtained from {@link SimJob#getQueue};
 * this must be maintained by {@link SimQueue} implementations of
 * {@link #arrive}.
 *
 * <p>
 * A {@link SimQueue} supports registration and deregistration of
 * queue-specific {@link SimAction}s to be invoked for specific events,
 * in particular job arrivals ({@link #addArrivalAction} and {@link #removeArrivalAction}),
 * job service start events ({@link #addStartAction} and {@link #removeStartAction}),
 * and job departures ({@link #addDepartureAction} and {@link #removeDepartureAction}).
 *
 * <p>
 * A {@link SimQueue} respects the various per job actions to be performed by
 * the queue as specified by
 * {@link SimJob#getQueueArriveAction},
 * {@link SimJob#getQueueStartAction},
 * {@link SimJob#getQueueRevokeAction}, and
 * {@link SimJob#getQueueDepartAction}.
 *
 * <p>
 * All {@link SimAction}s described above are called only <i>after</i> the
 * {@link SimQueue} has updated all relevant fields in the
 * {@link SimQueue} and {@link SimJob} objects,
 * i.e., <i>after</i> both objects truely reflect the new state of the queue
 * and the job, respectively.
 * As a general rule, queue-registered (global) actions take precedence
 * over job-specific actions, in the sense that the former are called
 * before the latter.
 * However, we think it is bad practice to depend upon this behavior.
 *
 * <p>
 * If a job is succesfully revoked, none of the departure actions are
 * called. Also, be aware that there is no guarantee that a start-service
 * or a departure event is ever called for a {@link SimJob} at all.
 *
 * <p>
 * A basic implementation of the most important non-preemptive
 * queueing disciplines is provided in {@link NonPreemptiveQueue}.
 *
 * @see SimJob
 * @see NonPreemptiveQueue
 *
 */
public interface SimQueue
{

  /** Arrival of a job at the queue.
   *
   * @param job  The job.
   * @param time The time at which the job arrives.
   *
   */
  public void arrive (SimJob job, double time);

  /** Revokation of a job at a queue.
   *
   * @param job  The job to be revoked from the queue.
   * @param time The time at which the request is issued
   *               (i.e., the current time).
   * @param interruptService Whether to allow interruption of the job's
   *                           service if already started.
   *                         If false, revokation will only succeed if the
   *                           job has not received any service yet.
   *
   * @return True if revokation succeeded.
   *
   */
  public boolean revoke (SimJob job, double time, boolean interruptService);

  /** Add an action to be invoked upon job arrivals.
   *
   * This method silently ignores actions that have already been registered.
   *
   * @param action The action to add.
   *
   */
  public void addArrivalAction (SimEventAction action);

  /** Remove an action to be invoked upon job arrivals.
   *
   * This method silently ignores actions that have not been registered.
   *
   * @param action The action to remove.
   *
   */
  public void removeArrivalAction (SimEventAction action);

  /** Add an action to be invoked upon (re)starting servicing a job.
   *
   * This method silently ignores actions that have already been registered.
   *
   * @param action The action to add.
   *
   */
  public void addStartAction (SimEventAction action);

  /** Remove an action to be invoked upon (re)starting servicing a job.
   *
   * This method silently ignores actions that have not been registered.
   *
   * @param action The action to remove.
   *
   */
  public void removeStartAction (SimEventAction action);

  /** Add an action to be invoked upon job departures.
   *
   * This method silently ignores actions that have already been registered.
   *
   * @param action The action to add.
   *
   */
  public void addDepartureAction (SimEventAction action);

  /** Remove an action to be invoked upon job departures.
   *
   * This method silently ignores actions that have not been registered.
   *
   * @param action The action to remove.
   *
   */
  public void removeDepartureAction (SimEventAction action);

}
