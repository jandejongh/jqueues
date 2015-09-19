package nl.jdj.jqueues.r4;

// Forcibly import NonPreemptiveQueue.NONE in order to keep javadoc happy...
import nl.jdj.jqueues.r4.NonPreemptiveQueue.NONE;
// Forcibly import SimEventList in order to keep javadoc happy...
import nl.jdj.jsimulation.r4.SimEventList;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventListListener;

/** A (generic) queueing system capable of serving jobs ({@link SimJob}s).
 *
 * <p> A {@link SimQueue} is an abstraction of a <i>queueing system</i> from queueing theory.
 * Such a system has an input accepting <i>jobs</i> (in our case {@link SimJob}s), each of which resides for a certain
 * amount of time in the system and eventually <i>departs</i> at its output.
 * The general notion is that jobs arrive at a queueing system
 * in order to receive some kind of service,
 * without actually being concerned about the actual type of service provided;
 * all that matters is a relative indication of the required <i>service time</i> from the queueing system,
 * and the resulting <i>sojourn time</i> in the system, which may be different from the job's required service time due
 * to other jobs requesting service from the same (usually finite-capacity) server,
 * in other words, due to server <i>contention</i>. The way in which the queueing system divides its serving capacity among
 * competing jobs and the order of service and relative priority given to them
 * is often referred to a its <i>queueing discipline</i> or <i>policy</i>.
 * 
 * <p> For many types of queueing systems with simple policies,
 * the internal structure can be seen as an area in which jobs that arrived <i>wait</i> until preceding jobs have finished,
 * and another area (the <i>server(s)</i>) that serves jobs (in turn) until completion.
 * A notorious example of this is the classic First-Come First Served (FCFS) queueing system
 * which serves jobs (a single one a a time) until completion in their order of arrival.
 * This gives rise to the idea that a queueing system
 * can be seen as
 * an area holding jobs awaiting service (the waiting area, or 'queue'),
 * and an area holding one or more jobs exclusively being served (the service area, or 'server(s)').
 * 
 * <p>
 * Unfortunately, this viewpoint is incomplete
 * in the sense that such a hard distinction between 'waiting' and 'being served exclusively' often cannot be made.
 * Several useful (idealized) policies
 * serve multiple jobs at once (like the Processor-Sharing (PS) policy,
 * sharing the server's capacity equally among all jobs present),
 * or switch the entire service capacity from one job to another with a certain service period (as in the Round-Robin (RR) policy).
 * 
 * <p>Therefore, in a {@link SimQueue},
 * the notion of 'waiting' is exclusively reserved
 * for the situation in which a job has arrived at a queueing system,
 * but has not yet received <i>any service at all</i>.
 * For policies like FCFS, this notion coincides with the classical viewpoint on queueing systems,
 * whereas for policies like PS,
 * this notion agrees with the general idea that arriving jobs do not have to wait before receiving service.
 * In our interface, it is (just) important to note that 'started jobs' do not necessarily have exclusive access to the server.
 * 
 * <p>
 * Note that with the current interface,
 * a {@link SimJob} <i>cannot</i> visit multiple {@link SimQueue}s simultaneously.
 * The {@link SimQueue} currently being visited by
 * a {@link SimJob} can be obtained from {@link SimJob#getQueue};
 * this must be maintained by {@link SimQueue} implementations of
 * {@link #arrive}.
 *
 * <p>
 * The life-cycle of a queue visit of a job thus is as follows.
 * {@link SimJob}s are offered for service through {@link #arrive}.
 * Depending on the queueing discipline, the job may be taking into service, in other words, start.
 * Between arrival and start, a job is said to be <i>waiting</i>.
 * After its start, a job is said to be <i>executing</i>.
 * Once the execution finishes, the job can eventually <i>depart</i> from the queue.
 * Note that this life-cycle is strict in the sense that only waiting jobs can start,
 * and only executing ('started') jobs can depart.
 * 
 * <p>
 * Once a job has been offered, {@link #revoke} tries to revoke the job,
 * if (still) possible and if supported by the queue discipline at all.
 * 
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
 * In general, the required service ('execution') time of the job during a queue visit
 * must be provided by each job through {@link SimJob#getServiceTime}.
 * It must remain constant during a queue visit (may may be changed in between visits).
 * Not all {@link SimQueue} implementations use the notion of service time (e.g., {@link NonPreemptiveQueue.NONE}),
 * but if they do,
 * the service time is to be interpreted as follows:
 * If a queue spends unit capacity on serving this and only this job, it will
 * leave the queue exactly after the requested service time has elapsed since its start.
 * Unless explicitly specified by the implementation, the default capacity of a server (or each server in case of a 
 * multi-server queue) is assumed to be unity throughout. Since the notion of variable-capacity servers is not that common,
 * it has not been incorporated into this interface.
 * Although queues are not allowed to increase the requested service time of a job (e.g., to compensate overhead), they are allowed
 * to serve jobs at a rate lower than their capacity, or to take vacation periods.
 *
 * <p>
 * From release 3 onwards, a {@link SimQueue} supports two types of <i>vacations</i>:
 * <ul>
 * <li>During a <i>queue-access vacation</i>, access to the <code>SimQueue</i> is prohibited and
 *     all jobs are dropped immediately upon arrival,
 *     see {@link #startQueueAccessVacation()}, {@link #startQueueAccessVacation(double)},
 *         {@link #stopQueueAccessVacation} and {@link #isQueueAccessVacation}.
 *     A queue-access vacation affects the queue's behavior <i>only</i> upon arrivals.
 *     Note that the vacation may be for a given duration, or for undetermined time until explicitly stopped.
 * <li>During a <i>server-access vacation</i>, jobs are prohibited to <i>start</i>, i.e., there is no access
 *       for jobs waiting to the server. It does not affect jobs that have already started. Server-access vacations
 *       are actually somewhat more flexible through the notion of <i>server-access credits</i>, denoting the number of jobs
 *       still admissible to the server, see {@link #getServerAccessCredits}.
 *       A server-access vacation starts when there are no more server-access credits
 *       (due to jobs starting), and ends when credits are explicitly granted to the interface through
 *       {@link #setServerAccessCredits}.
 *       Note that by default, each <code>SimQueue</code> has infinite server-access credits.
 * </ul>
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
 * In addition, a {@link SimQueue} respects the various per job actions to be performed by
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
 * {@link #registerQueueListener}. The relevant methods of a {@link SimQueueListener} are invoked immediately after invocation of
 * the registered {@link SimEventAction}s, but before the jobs-specific actions.
 * Again, this order should not be relied upon.
 * 
 * <p>
 * If the {@link SimQueueListener} is also a {@link SimQueueVacationListener},
 * the {@link SimQueue} will also notify the start and end of the various vacation types,
 * see {@link SimQueueVacationListener} for more details.
 * 
 * <p>
 * Unlike the notification mechanisms for queue and job specific action, a {@link SimQueueListener} also get notifications
 * right <i>before</i> a state change in the queue occurs, e.g., right before a job departure.
 * Such notifications are named <i>updates</i>, see {@link SimQueueListener#update}.
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
 * queueing disciplines is provided in {@link NonPreemptiveQueue}.
 * All concrete subclasses of {@link NonPreemptiveQueue} take
 * the {@link SimEventList} as one of their arguments upon construction.
 * 
 * <p>
 * Implementations must listen to the underlying event list for resets, see {@link SimEventListListener#notifyEventListReset}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimJob
 * @see SimQueueListener
 * @see SimQueueVacationListener
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
