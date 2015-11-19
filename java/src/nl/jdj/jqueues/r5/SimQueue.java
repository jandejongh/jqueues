package nl.jdj.jqueues.r5;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueueBase;
import nl.jdj.jsimulation.r5.SimEventList;


/** A (generic) queueing system capable of hosting and optionally serving jobs ({@link SimJob}s).
 *
 * <p> A {@link SimQueue} is an abstraction of a <i>queueing system</i> from queueing theory.
 * 
 * <p>
 * The following {@code javadoc} section aims at concisely specifying the {@link SimQueue} interface.
 * The assumptions and constraints in the sequel should not be interpreted as "agreed upon in the field",
 * but motivations for them are not given in order to keep the section at (hopefully) pleasant length.
 * 
 * <p>
 * A {@link SimQueue} accepts so-called <i>jobs</i> (in our case {@link SimJob}s) for a <i>visit</i>.
 * Each job can visit at most one queue at a time,
 * and while it is visiting a queue,
 * it cannot initiate <i>another</i> visit to that same queue.
 * 
 * <p>
 * A visit is initiated by the <i>arrival</i> of a job at a queue, see {@link #arrive}.
 * 
 * <p>
 * During a visit, a job is either in the queue's so-called <i>waiting area</i>,
 * in which it always waits,
 * or in the queue's <i>service area</i>,
 * in which the job <i>can</i> receive <i>service</i> from
 * the servers in the service area.
 * At the start of a visit, a job is either put
 * in the waiting area or directly into the service area
 * (or <i>dropped</i> immediately).
 * A job can move at most once from the waiting into the service area,
 * but not in reverse direction.
 * Entering the service area of a queue is called <i>starting</i> the job.
 * 
 * <p>
 * In itself, a {@link SimQueue} makes no assumption whatsoever about the server structure,
 * except for the fact that <i>only jobs in the service area can receive service</i>.
 * But other than that, there may be any number (including zero and infinity) of servers,
 * and the number of servers may change in time in the {@link SimQueue} interface.
 * This flexibility, however, comes at the expense of the absence of server-structure methods
 * on the (bare) {@link SimQueue} interface.
 * 
 * Also note that jobs in the service area do <i>not</i> have to be served all the time
 * (although many sub-interfaces/sub-classes impose this requirement).
 * 
 * <p>
 * A visit can end in three different ways:
 * <ul>
 * <li>a <i>departure</i> (the visit ends normally),
 * <li>a <i>drop</i> (the queue cannot complete the visit, e.g., because of limited buffer space or vacation),
 * <li>a <i>revocation</i> (the job is removed upon external request).
 * </ul>
 * If a visit ends, the job is said to <i>exit</i> (<i>depart from</i>; <i>be dropped at</i>; <i>be revoked at</i>) the queue.
 * Each way to exit the queue can be from the waiting area or from the service area
 * (but sub-classes may restrict the possibilities).
 * 
 * <p>
 * If a visit never ends, the job (or the visit) is named <i>sticky</i>;
 * again this can be at the waiting area or the service area.
 * 
 * <p>
 * Each {@link SimQueue} must support the notions of <i>queue-access vacations</i> during which all jobs are dropped upon arrival,
 * and of <i>server-access credits</i> that limit the remaining number of jobs that can be started
 * (i.e., moved from the waiting area into the service area).
 * 
 * <p>
 * The <i>state</i> of a {@link SimQueue} includes at least the set of jobs present (and in which area each resides),
 * its queue-access vacation state and its remaining number of server-access credits.
 * In addition, the so-called {@code noWaitArmed} state has to be maintained.
 * If a queue is in {@code noWaitArmed} state, any job will start service immediately or exit immediately upon arrival,
 * <i>assuming</i> the absence of a queue access vacation and at least one server-access credit
 * (i.e., ignoring the actual state settings for these features).
 * See {@link #isNoWaitArmed}.
 * 
 * <p>
 * Each {@link SimQueue} (and {@link SimJob} for that matter) must notify all state changes,
 * see {@link SimEntityListener} and its sub-interfaces.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * @see SimEntity
 * @see SimJob
 * @see SimQueueListener
 * @see AbstractSimQueueBase
 * @see AbstractSimQueue
 * 
 */
public interface SimQueue<J extends SimJob, Q extends SimQueue>
extends SimEntity<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a functional copy of this {@link SimQueue}.
   *
   * <p>
   * The new object has the same (concrete) type as the original, but starts without jobs and without external listeners.
   * Its initial state must be as if {@link #resetEntity} was invoked on the queue.
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
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Get the set of jobs currently visiting this queue.
   *
   * @return The set of jobs currently visiting the queue, non-{@code null}.
   * 
   * @see #getNumberOfJobs
   * @see #getJobsInWaitingArea
   * @see #getJobsInServiceArea
   * 
   */
  public Set<J> getJobs ();

  /** Gets the number of jobs currently visiting the queue.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobs ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs currently visiting the queue, zero or positive.
   * 
   * @see #getJobs
   * @see #getNumberOfJobsInWaitingArea
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  public int getNumberOfJobs ();
  
  /** Get the set of jobs in the waiting area.
   *
   * @return The set of jobs in the waiting area, non-{@code null}.
   * 
   * @see #getNumberOfJobsInWaitingArea
   * @see #getJobs
   * @see #getJobsInServiceArea
   * 
   */
  public default Set<J> getJobsInWaitingArea ()
  {
    final Set<J> set = new LinkedHashSet<> (getJobs ());
    set.removeAll (getJobsInServiceArea ());
    return set;
  }

  /** Gets the number of jobs in the waiting area.
   * 
   * <p>
   * Typically, this method is more efficient than {@code getJobsInWaitingArea ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs in the waiting area.
   * 
   * @see #getJobsInWaitingArea
   * @see #getNumberOfJobs
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  public default int getNumberOfJobsInWaitingArea ()
  {
    return getNumberOfJobs () - getNumberOfJobsInServiceArea ();
  }

  /** Get the set of jobs in the service area.
   *
   * @return The set of jobs in the service area, non-{@code null}.
   * 
   * @see #getNumberOfJobsInServiceArea
   * @see #getJobs
   * @see #getJobsInWaitingArea
   * 
   */
  public Set<J> getJobsInServiceArea ();

  /** Gets the number of jobs in the service area.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobsInServiceArea ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs in the service area.
   * 
   * @see #getJobsInServiceArea
   * @see #getNumberOfJobs
   * @see #getNumberOfJobsInWaitingArea
   * 
   */
  public int getNumberOfJobsInServiceArea ();
  
  /** Returns whether or not the queue is on queue-access vacation.
   * 
   * @return Whether or not the queue is on queue-access vacation.
   * 
   * @see #setQueueAccessVacation
   * 
   */
  public boolean isQueueAccessVacation ();
  
  /** Gets the (remaining) server-access credits.
   *
   * The value {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @return The remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   */
  public int getServerAccessCredits ();
  
  /** Returns whether the next arriving job is guaranteed to suffer zero-waiting time before starting service or exiting.
   * 
   * <p>
   * The return value is <i>independent</i> of queue-access and server-access vacations.
   * 
   * @return True if the next arriving job is guaranteed to suffer zero-waiting time before starting service or exiting,
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

  /** Starts or ends a queue-access vacation.
   * 
   * <p>
   * During a queue-access vacation, all {@link SimJob}s will be dropped immediately upon arrival.
   * 
   * @param time  The time at which to start or end the queue-access vacation, i.c., the current time.
   * @param start Whether to start ({@code true}) or end ({@code false}) the vacation.
   * 
   * @see #arrive
   * @see #isQueueAccessVacation
   * 
   */
  public void setQueueAccessVacation (double time, boolean start);
  
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
   * @param time The time at which the job arrives, i.c., the current time.
   * @param job  The job.
   * 
   * @see #isQueueAccessVacation
   *
   */
  public void arrive (double time, J job);

  /** Revocation (attempt) of a job at a queue.
   *
   * <p>
   * If the job is not currently present at this {@link SimQueue}, {@code false} is returned.
   * 
   * @param time             The time at which the request is issued, i.c., the current time.
   * @param job              The job to be revoked from the queue.
   * @param interruptService Whether to allow interruption of the job's
   *                           service if already started.
   *                         If {@code false}, revocation will only succeed if the
   *                           job has not received any service yet.
   *
   * @return True if revocation succeeded (returns {@code false} if the job is not present).
   *
   */
  public boolean revoke (double time, J job, boolean interruptService);

  /** Sets the server-access credits.
   * 
   * @param time    The time at which to set the credits, i.c., the current time.
   * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If credits is (strictly) negative.
   * 
   */
  public void setServerAccessCredits (double time, int credits);
  
}
