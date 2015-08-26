package nl.jdj.jqueues.r3;

// Forcibly import SimEventList in order to keep javadoc happy...
import nl.jdj.jsimulation.r3.SimEventList;
import nl.jdj.jsimulation.r3.SimEventAction;
import nl.jdj.jsimulation.r3.SimEventListListener;

/** A queue has one or more waiting lines for {@link SimJob}s
 *  and zero or more servers to serve them.
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
 * The lifetime of a queue visit of a job is as follows.
 * {@link SimJob}s are offered for service through {@link #arrive}.
 * Depending on the queueing discipline, the job may be taking into service, in other words, start.
 * Between arrival and start, a job is said to be <i>waiting</i>.
 * Once a job has been offered, {@link #revoke} tries to revoke the job,
 * if (still) possible and if supported by the queue discipline at all.
 * A queue may also choose to drop a job, whether in service or not.
 * Note the difference between a revocation (at the caller's discretion) and a drop (at the queue's discretion).
 * If a job is neither dropped nor revoked, receive sufficient service from the queue and depart from it (a departure).
 *
 * <p>
 * Despite the large number of freedom degrees for {@link SimQueue}s, there is also a number of (obvious) restrictions
 * on the behavior of a queue.
 * For instance, a job cannot depart from a queue before it has been started, and it cannot start, be dropped or be revoked
 * before having arrived.
 * It can only start once during a queue visit.
 * 
 * <p>
 * The required service time of the job during a queue visit
 * must be provided by each job through {@link SimJob#getServiceTime}.
 * It must remain constant during a queue visit (may may be changed in between visits).
 * This number is to be interpreted as follows: If a queue spends unit capacity on serving this and only this job, it will
 * leave the queue exactly after the requested service time has elapsed since its start.
 * Unless explicitly specified by the implementation, the default capacity of a server (or each server in case of a 
 * multi-server queue) is assumed to be unity throughout. Since the notion of variable-capacity servers is not that common,
 * it has not been incorporated into this interface.
 * Although queues are not allowed to increase the requested service time of a job (e.g., to compensate overhead), they are allowed
 * to serve jobs at a rate lower than their capacity, or to take vacation periods.
 *
 * <p>
 * From release 3 onwards, a {@link SimQueue} supports various types of <i>vacations</i>:
 * <ul>
 * <li>During a <i>queue-access vacation</i>, all jobs are dropped immediately upon arrival,
 *     see {@link #startQueueAccessVacation()}, {@link #startQueueAccessVacation(double)},
 *         {@link #stopQueueAccessVacation} and {@link #isQueueAccessVacation}.
 *     A queue-access vacation <i>only</i> affects the queue's behavior upon arrivals.
 * <li>
 * <li>
 * </ul>
 * Note that all types of vacation may be for a given duration, or for undetermined time until explicitly stopped.
 * 
 * 
 * <p>
 * A {@link SimQueue} supports registration and un-registration of
 * queue-specific {@link SimEventAction}s to be invoked for specific events,
 * in particular job arrivals ({@link #addArrivalAction} and {@link #removeArrivalAction}),
 * job service start events ({@link #addStartAction} and {@link #removeStartAction}),
 * job drop events ({@link #addDropAction} and {@link #removeDropAction}),
 * job revocation events ({@link #addRevocationAction} and {@link #removeRevocationAction})
 * and job departures ({@link #addDepartureAction} and {@link #removeDepartureAction}).
 *
 * <p>
 * A {@link SimQueue} respects the various per job actions to be performed by
 * the queue as specified by
 * {@link SimJob#getQueueArriveAction},
 * {@link SimJob#getQueueStartAction},
 * {@link SimJob#getQueueDropAction},
 * {@link SimJob#getQueueRevokeAction}, and
 * {@link SimJob#getQueueDepartAction}.
 *
 * <p>
 * All {@link SimEventAction}s described above are called only <i>after</i> the
 * {@link SimQueue} has updated all relevant fields in the
 * {@link SimQueue} and {@link SimJob} objects,
 * i.e., <i>after</i> both objects truly reflect the new state of the queue
 * and the job, respectively.
 * As a general rule, queue-registered (global) actions take precedence
 * over job-specific actions, in the sense that the former are called
 * before the latter.
 * However, we think it is bad practice to depend upon this behavior.
 *
 * <p>
 * A more convenient way to be notified of {@link SimQueue} events is by registering as a {@link SimQueueListener} through
 * {@link #registerQueueListener}. The relevant methods of a {@link SimQueueListener} are invoked immediately after invocation of the
 * registered {@link SimEventAction}s, but before the jobs-specific actions.
 * Again, this order should not be relied upon.
 * 
 * <p>
 * Unlike the notification mechanisms for queue and job specific action, a {@link SimQueueListener} also get notifications
 * right <i>before</i> a state change in the queue occurs, e.g., right before a job departure.
 * Such notifications are namend <i>updates</i>, see {@link SimQueueListener#update}.
 * 
 * <p>
 * If a job is successfully revoked, or if it is dropped, none of the departure actions are
 * called. Also, be aware that there is no guarantee that a start-service
 * or a departure event is ever called for a {@link SimJob} at all.
 *
 * <p>Although not explicitly enforced by this interface, typical {@link SimQueue}s should probably rely
 * on an underlying {@link SimEventList} for scheduling events in time and invoking the actions.
 * Implementations should clearly state how the interaction with a {@link SimEventList} works, for instance, whether or not the
 * caller is responsible for starting the processing of the event list.
 * 
 * <p>
 * A basic implementation of the most important non-preemptive
 * queueing disciplines is provided in {@link NonPreemptiveQueue}. All concrete subclasses of {@link NonPreemptiveQueue} take
 * the {@link SimEventList} as one of their arguments upon construction.
 * 
 * <p>
 * Implementations must listen to the underlying event list for resets, see {@link SimEventListListener#notifyEventListUpdate}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimJob
 * @see NonPreemptiveQueue
 *
 */
public interface SimQueue<J extends SimJob, Q extends SimQueue>
extends SimEventListListener
{
  
  /** Registers a listener to events related to this queue.
   * 
   * @param listener The listener; ignored if already registered or <code>null</code>.
   * 
   * @see #unregisterQueueListener
   * 
   */
  public void registerQueueListener (SimQueueListener<J, Q> listener);
  
  /** Unregisters a listener to events related to this queue.
   * 
   * @param listener The listener; ignored if not registered or <code>null</code>.
   * 
   * @see #registerQueueListener
   * 
   */
  public void unregisterQueueListener (SimQueueListener<J, Q> listener);

  /** Puts the queue in the empty state, removes all jobs without notifications, and reset its internal time to "undetermined".
   *
   * This method is used in order to restart a simulation.
   * By contract, a {@link SimQueue} must reset if its underlying {@link SimEventList} resets.
   * 
   */
  public void reset ();
  
  /** Arrival of a job at the queue.
   *
   * This methods should be called from the {@link SimEventList} as a result of scheduling the job arrival.
   * Implementations can rely on the fact that the time argument supplied is actually the current time in the simulation.
   * 
   * <p>
   * Do not use this method to schedule job arrivals on the event list!
   * 
   * Note that during a <i>queue-access vacation</i>, all jobs will be dropped upon arrival.
   * 
   * @param job  The job.
   * @param time The time at which the job arrives, i.c., the current time.
   * 
   * @see #isQueueAccessVacation
   *
   */
  public void arrive (J job, double time);

  /** Revocation of a job at a queue.
   *
   * @param job  The job to be revoked from the queue.
   * @param time The time at which the request is issued
   *               (i.e., the current time).
   * @param interruptService Whether to allow interruption of the job's
   *                           service if already started.
   *                         If false, revocation will only succeed if the
   *                           job has not received any service yet.
   *
   * @return True if revocation succeeded.
   *
   */
  public boolean revoke (J job, double time, boolean interruptService);

  /** Start a queue-access vacation of undetermined duration (i.e., until explicit ending the vacation).
   * 
   * During a queue-access vacation, all {@link SimJob}s will be dropped immediately upon arrival.
   * 
   * If the queue is already on queue-access vacation, this method makes the vacation period undetermined (if not so already).
   * 
   * @see #arrive
   * @see #startQueueAccessVacation(double)
   * @see #stopQueueAccessVacation
   * @see #isQueueAccessVacation
   * 
   */
  public void startQueueAccessVacation ();
  
  /** Start a queue-access vacation of given duration.
   * 
   * If the queue is already on queue-access vacation, this method will overrule previous 
   * settings as to the (remaining) duration of the vacation.
   * 
   * @see #arrive
   * @see #startQueueAccessVacation()
   * @see #stopQueueAccessVacation
   * @see #isQueueAccessVacation
   * 
   */
  public void startQueueAccessVacation (double duration);
  
  /** Immediately stops a queue-access vacation.
   * 
   * This method does nothing if the queue is not on queue-access vacation.
   * and overrules all settings as to the (remaining) duration of the vacation.
   * 
   * @see #startQueueAccessVacation()
   * @see #startQueueAccessVacation(double)
   * @see #isQueueAccessVacation
   * 
   */
  public void stopQueueAccessVacation ();
  
  /** Returns whether or not the queue is on queue-access vacation.
   * 
   * This method does nothing if the queue is not on queue-access vacation.
   * and overrules all settings as to the (remaining) duration of the vacation.
   * 
   * @see #startQueueAccessVacation()
   * @see #startQueueAccessVacation(double)
   * @see #stopQueueAccessVacation
   * 
   */
  public boolean isQueueAccessVacation ();
  
  /** Gets the (remaining) server-access credits.
   *
   * The value {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @return The remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   */
  public int getServerAccessCredits ();
  
  /** Sets the server-access credits.
   * 
   * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If credits is (strictly) negative.
   * 
   */
  public void setServerAccessCredits (int credits);
  
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

  /** Add an action to be invoked upon job drops.
   *
   * This method silently ignores actions that have already been registered.
   *
   * @param action The action to add.
   *
   */
  public void addDropAction (SimEventAction action);

  /** Remove an action to be invoked upon job drops.
   *
   * This method silently ignores actions that have not been registered.
   *
   * @param action The action to remove.
   *
   */
  public void removeDropAction (SimEventAction action);

  /** Add an action to be invoked upon (successful) job revocations.
   *
   * This method silently ignores actions that have already been registered.
   *
   * @param action The action to add.
   *
   */
  public void addRevocationAction (SimEventAction action);

  /** Remove an action to be invoked upon (successful) job revocations.
   *
   * This method silently ignores actions that have not been registered.
   *
   * @param action The action to remove.
   *
   */
  public void removeRevocationAction (SimEventAction action);

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

  /** Gets the number of jobs currently residing at the queue, either waiting or executing.
   *
   * @return The number of jobs at the queue, zero or positive.
   * 
   */
  public int getNumberOfJobs ();
  
  /** Gets the number of jobs currently being executed at the queue (i.e., not waiting).
   *
   * @return The number of jobs currently being executed at the queue (i.e., not waiting).
   * 
   */
  public int getNumberOfJobsExecuting ();
  
}
