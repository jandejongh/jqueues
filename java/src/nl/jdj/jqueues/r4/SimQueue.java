package nl.jdj.jqueues.r4;

import nl.jdj.jqueues.r4.nonpreemptive.AbstractNonPreemptiveSimQueue;
import nl.jdj.jqueues.r4.serverless.SINK;
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
 * The life-cycle of a queue visit of a job thus is as follows.
 * {@link SimJob}s are offered for service through {@link #arrive}.
 * Depending on the queueing discipline, the job may be taking into service, in other words, start.
 * Between arrival and start, a job is said to be <i>waiting</i>.
 * After its start, a job is said to be <i>executing</i>.
 * Once the execution finishes, the job <i>departs</i> from the queue.
 * Note, however, that a job may also depart from the {@link SimQueue} without having started!
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
 * For instance,
 * <ul>
 * <li>a job cannot start, be dropped or be revoked before having arrived;
 * <li>a job can start at most once during a queue visit;
 * <li>a job can only <i>leave</i> the queueing system through departure (with or without being served),
 *     successful revocation or drop.
 * </ul>
 * 
 * <p>
 * Note that with the current interface,
 * a {@link SimJob} <i>cannot</i> visit multiple {@link SimQueue}s simultaneously.
 * The {@link SimQueue} currently being visited by
 * a {@link SimJob} can be obtained from {@link SimJob#getQueue};
 * this must be maintained by implementations of
 * {@link SimQueue#arrive}.
 *
 * <p>
 * In general, the required service ('execution') time of the job during a queue visit
 * must be provided by each job through {@link SimJob#getServiceTime}.
 * It must remain constant during a queue visit (but may be changed in between visits).
 * Not all {@link SimQueue} implementations use the notion of service time (e.g., {@link SINK}),
 * but if they do,
 * the service time is to be interpreted as follows:
 * If a queue spends unit capacity on serving this and only this job, it will
 * leave the queue exactly after the requested service time has elapsed since its start.
 * Unless explicitly specified by the implementation, the default capacity of a server (or each server in case of a 
 * multi-server queue) is assumed to be unity throughout.
 * Since the notion of variable-capacity servers is not that common,
 * it has not been incorporated into this interface.
 * Although queues are not allowed to increase the requested service time of a job (e.g., to compensate overhead), they are allowed
 * to serve jobs at a rate lower than their capacity, or to take vacation periods.
 *
 * <p>
 * Some queueing systems override the requested service time as (would be) obtained through {@link SimJob#getServiceTime},
 * and instead use a different source to obtain the service time.
 * Implementations are strongly encouraged to document the source of the job's service time,
 * if different from (default) requesting this at the job.
 * In any case, the requested service time of a job, irrespective of its source, has to remain constant during a job's visit to
 * a particular queue.
 * 
 * <p>
 * From release 3 onwards, a {@link SimQueue} supports two types of <i>vacations</i>:
 * <ul>
 * <li>During a <i>queue-access vacation</i>, access to the <i>SimQueue</i> is prohibited and
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
 * From release 5 onwards, each {@link SimQueue} maintains and reports changes to the state in which it <i>guarantees</i> that
 * the next arriving job will
 * <ul>
 * <li>start service immediately without waiting, or,
 * <li>departs immediately, without service and without waiting.
 * </ul>
 * If either condition is met, the queueing system is said to be in <i>noWaitArmed</i> state.
 * Note that this state setting ignores queue and server-access vacations.
 * 
 * <p>
 * A {@link SimQueue} respects the various per job actions to be performed by
 * the queue as specified by
 * <ul>
 * <li>{@link SimJob#getQueueArriveAction},
 * <li>{@link SimJob#getQueueStartAction},
 * <li>{@link SimJob#getQueueDropAction},
 * <li>{@link SimJob#getQueueRevokeAction}, and
 * <li>{@link SimJob#getQueueDepartAction}.
 * </ul>
 *
 * <p>
 * Except for arrival notifications, all {@link SimEventAction}s described above are called only <i>after</i> the
 * {@link SimQueue} has updated all relevant fields in the
 * {@link SimQueue} and {@link SimJob} objects,
 * i.e., <i>after</i> both objects truly reflect the new state of the queue
 * and the job, respectively.
 *
 * <p>
 * Arrival-related notifications are always issued immediately at the time a job arrives at a queue,
 * in other words, at a point where the queue does not even know yet about the existence of the job
 * (and vice versa, for that matter).
 * 
 * <p>
 * A convenient queue-centric way to be notified of {@link SimQueue} events is by registering as a {@link SimQueueListener} through
 * {@link #registerQueueListener}. The relevant methods of a {@link SimQueueListener} are invoked before the jobs-specific actions.
 * This order should not be relied upon though.
 * 
 * <p>
 * If the {@link SimQueueListener} is also a {@link SimQueueVacationListener},
 * the {@link SimQueue} will also notify the start and end of the various vacation types,
 * see {@link SimQueueVacationListener} for more details.
 * 
 * <p>
 * Unlike the notification mechanism for job specific actions, a {@link SimQueueListener} also get notifications
 * right <i>before</i> a state change in the queue occurs, e.g., right before a job departure.
 * Such notifications are named <i>updates</i>, see {@link SimQueueListener#notifyUpdate}.
 * 
 * <p>
 * If a job is successfully revoked, or if it is dropped, none of the departure events are fired.
 * Also, be aware that there is no guarantee that a start-service
 * or a departure event is ever called for a {@link SimJob} at all.
 *
 * <p>Although not explicitly enforced by this interface, typical {@link SimQueue}s should probably rely
 * on an underlying {@link SimEventList} for scheduling events in time and invoking the actions.
 * Implementations should clearly state how the interaction with a {@link SimEventList} works, for instance, whether or not the
 * caller is responsible for starting the processing of the event list.
 * 
 * <p>
 * Basic implementations of the most important non-preemptive
 * queueing disciplines are provided in the {@link AbstractNonPreemptiveSimQueue} class
 * and its concrete implementations in the same package.
 * 
 * <p>
 * Implementations must listen to the underlying event list for resets, see {@link SimEventListListener#notifyEventListReset}.
 *
 * <p>
 * Partial utility implementations of {@link SimQueue} are available in this package through 
 * {@link AbstractSimQueueBase} and its descendant {@link AbstractSimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimJob
 * @see SimQueueListener
 * @see SimQueueVacationListener
 * @see AbstractSimQueue
 * @see AbstractNonPreemptiveSimQueue
 *
 */
public interface SimQueue<J extends SimJob, Q extends SimQueue>
extends SimEventListListener
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a functional copy of this {@link SimQueue}.
   *
   * <p>
   * The new object has the same (concrete) type as the original, but starts without jobs and without external listeners.
   * Its initial state must be as if {@link #reset} was invoked on the queue.
   * 
   * <p>
   * Note that the semantics of this method are much less strict than the <code>Object.clone ()</code> method.
   * Typically, concrete classes will implement this by returning a <code>new (...)</code> object.
   * This way, we circumvent the problem of cloning objects with final (for good reasons) fields.
   * 
   * @return A functional copy of this {@link SimQueue}.
   * 
   * @throws UnsupportedOperationException If the operation is not supported yet; this should be considered a software error.
   * 
   */
  public SimQueue<J, Q> getCopySimQueue () throws UnsupportedOperationException;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a default, type-specific name for this {@link SimQueue}.
   * 
   * <p>
   * The string is used as a fallback return value for <code>Object.toString ()</code>
   * in case the user did not set an instance-specific name
   * through {@link #setName}.
   * 
   * <p>
   * To be overridden in subclasses.
   * 
   * @return A default, type-specific name for this {@link SimQueue}.
   * 
   * @see #setName
   * 
   */
  public String toStringDefault ();
  
  /** Sets the name of this {@link SimQueue}, to be returned by subsequent calls to <code>Object.toString ()</code>.
   * 
   * @param name The new name of this queue; if non-<code>null</code>, the string will be supplied by subsequent calls
   *               to <code>Object.toString ()</code>; otherwise, the type-specific default will be used for that.
   * 
   * @see #toStringDefault
   * 
   */
  public void setName (String name);
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the number of jobs currently residing at the queue, either waiting or executing.
   *
   * @return The number of jobs at the queue, zero or positive.
   * 
   * @see #getNumberOfJobsExecuting
   * @see #getNumberOfJobsWaiting
   * 
   */
  public int getNumberOfJobs ();
  
  /** Gets the number of jobs waiting (i.e., <i>not</i> executing).
   * 
   * @return The number of jobs waiting.
   * 
   * @see #getNumberOfJobs
   * @see #getNumberOfJobsExecuting
   * 
   */
  public default int getNumberOfJobsWaiting ()
  {
    return getNumberOfJobs () - getNumberOfJobsExecuting ();
  }

  /** Gets the number of jobs currently being executed at the queue (i.e., not waiting).
   *
   * @return The number of jobs currently being executed at the queue (i.e., not waiting).
   * 
   * @see #getNumberOfJobs
   * @see #getNumberOfJobsWaiting
   * 
   */
  public int getNumberOfJobsExecuting ();
  
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
  
  /** Returns whether the next arriving job is guaranteed to suffer zero-waiting time before starting service or departing.
   * 
   * The return value is <i>independent</i> of queue-access and server-access vacations.
   * 
   * @return True if the next arriving job is guaranteed to suffer zero-waiting time before starting service or departing,
   *         in the absence of queue-access and server-access vacations.
   * 
   * @see SimQueueListener#notifyNewNoWaitArmed
   * 
   */
  public boolean isNoWaitArmed ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
   * <p>
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
  
  /** Sets the server-access credits.
   * 
   * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If credits is (strictly) negative.
   * 
   */
  public void setServerAccessCredits (int credits);
  
}
