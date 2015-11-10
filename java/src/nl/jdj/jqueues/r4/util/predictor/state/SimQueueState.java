package nl.jdj.jqueues.r4.util.predictor.state;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r4.SimEntity;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jsimulation.r4.SimEventList;

/** A representation of the state of a {@link SimQueue} while or as if being processed by an event list.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueState<J extends SimJob, Q extends SimQueue>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the queue, non-{@code null}.
   * 
   * <p>
   * The queue to which this {@link SimQueueState} refers; cannot be changed.
   * 
   * @return The queue, non-{@code null}.
   * 
   */
  public Q getQueue ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets the state, after which it reflects the "real" queue state after a reset.
   * 
   * <p>
   * Mimics {@link SimEventList#reset}.
   * Note that the corresponding {@link SimQueue} method is {@link SimEntity#resetEntity}.
   * 
   * <p>
   * Implementations must set the time to {@link Double#NaN}, effectively meaning
   * "left of" {@link Double#NEGATIVE_INFINITY}.
   * 
   * @see SimQueue#resetEntity
   * 
   */
  public void reset ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the current time at the queue.
   * 
   * @return The current time at the queue.
   * 
   */
  public double getTime ();
  
  /** Sets the current time at the queue.
   * 
   * <p>
   * You cannot set back the time (only through {@link #reset}).
   * 
   * @param time The new time.
   * 
   * @throws IllegalArgumentException If the time is in the past, or equal to {@link Double#NaN}.
   * 
   * @see #reset
   * 
   */
  public void setTime (double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns whether or not the queue is on queue-access vacation.
   * 
   * <p>
   * Mimics {@link SimQueue#isQueueAccessVacation}.
   * 
   * @return Whether or not the queue is on queue-access vacation.
   * 
   */
  public boolean isQueueAccessVacation ();
  
  /** Starts a queue-access vacation.
   * 
   * <p>
   * The time cannot be in the past.
   * 
   * @param time The time the vacation starts.
   * 
   * <p>
   * Mimics {@link SimQueue#startQueueAccessVacation}.
   * 
   * @throws IllegalArgumentException If time is in the past.
   * 
   */
  public void startQueueAccessVacation (double time);
  
  /** Stops a queue-access vacation.
   * 
   * <p>
   * The time cannot be in the past.
   * 
   * @param time The time the vacation stops.
   * 
   * <p>
   * Mimics {@link SimQueue#stopQueueAccessVacation}.
   * 
   * @throws IllegalArgumentException If time is in the past.
   * 
   */
  public void stopQueueAccessVacation (double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a map of time onto jobs that arrived at that time, and are still present in the queue.
   * 
   * <p>
   * Within the list, the jobs are in arrival order.
   * Any job can only be in one of the value lists.
   * The value lists only holds jobs present in the system,
   * and, vice versa, any job present in the system must be in exactly one of the value lists.
   * Null keys or values are not allowed.
   * 
   * @return A map of time onto jobs that arrived at that time, and are still present in the queue.
   * 
   */
  public NavigableMap<Double, List<J>> getJobArrivalsMap ();
  
  /** Gets a map from jobs present in the queue onto their arrival times.
   * 
   * <p>
   * Every job present in the queue must have an entry,
   * and, vice versa, every entry refers to a job actually present in the queue.
   * Null keys or values are not allowed.
   * 
   * @return A map from jobs present in the queue onto their arrival times.
   * 
   */
  public Map<J, Double> getArrivalTimesMap ();
  
  /** Get the set of all jobs currently residing at the queue, either waiting or executing.
   * 
   * <p>
   * Mimics {@link SimQueue#getJobs}.
   * 
   * <p>
   * The default implementation returns the key-set of {@link #getArrivalTimesMap}.
   * 
   * @return The set of all jobs residing at the queue, non-{@code null}.
   * 
   */
  public default Set<J> getJobs ()
  {
    return getArrivalTimesMap ().keySet ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS WAITING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets a set holding all the jobs waiting at the queue (in no particular order).
   * 
   * <p>
   * Mimics {@link SimQueue#getJobsWaiting}.
   * 
   * <p>
   * The default implementation returns the set difference of
   * {@link #getJobs} and {@link #getJobsExecuting}.
   * 
   * @return A set holding all the jobs waiting at the queue (in no particular order).
   * 
   */
  public default Set<J> getJobsWaiting ()
  {
    final Set<J> jobsWaiting = new HashSet<> ();
    jobsWaiting.addAll (getJobs ());
    jobsWaiting.removeAll (getJobsExecuting ());
    return jobsWaiting;
  }

  /** Gets a set holding all the jobs waiting at the queue, in order of arrival.
   * 
   * <p>
   * The default implementation returns a {@link LinkedHashSet}.
   * 
   * @return A set holding all the jobs waiting at the queue, in order of arrival.
   * 
   */
  public default Set<J> getJobsWaitingOrdered ()
  {
    final Set<J> jobsWaitingOrdered = new LinkedHashSet<> ();
    final Set<J> jobsExecuting = getJobsExecuting ();
    for (final List<J> l : this.getJobArrivalsMap ().values ())
      for (final J job : l)
        if (! jobsExecuting.contains (job))
          jobsWaitingOrdered.add (job);
    return jobsWaitingOrdered;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the server-access credits.
   * 
   * <p>
   * Mimics {@link SimQueue#getServerAccessCredits}.
   * 
   * @return The server-access credits.
   * 
   */
  public int getServerAccessCredits ();
  
  /** Sets the server-access credits.
   * 
   * <p>
   * Mimics {@link SimQueue#setServerAccessCredits}.
   * 
   * <p>
   * The time cannot be in the past.
   * 
   * @param time    The time to set the credits.
   * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If time is in the past, or credits is (strictly) negative.
   * 
   */
  public void setServerAccessCredits (double time, int credits);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS EXECUTING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets a map from all jobs executing at this queue onto their start times.
   * 
   * @return A map from all jobs executing at this queue onto their start times.
   * 
   */
  public Map<J, Double> getStartTimesMap ();
  
  /** Returns a map of time onto jobs that started at that time, and are still present (and thus executing) in the queue.
   * 
   * <p>
   * Within the list, the jobs are in start order.
   * Any job can only be in one of the value lists.
   * The value lists only holds jobs currently executing in the system,
   * and, vice versa, any job executing in the system must be in exactly one of the value lists.
   * Null keys or values are not allowed.
   * 
   * @return A map of time onto jobs that started at that time, and are still present (and thus executing) in the queue.
   * 
   */
  public NavigableMap<Double, Set<J>> getJobsExecutingMap ();
  
  /** Get the set of jobs currently being executed at the queue (i.e., not waiting).
   * 
   * <p>
   * Mimics {@link SimQueue#getJobsExecuting}.
   * 
   * @return The set of jobs currently being executed at the queue (i.e., not waiting).
   * 
   */
  public default Set<J> getJobsExecuting ()
  {
    return getStartTimesMap ().keySet ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS EXECUTING - REMAINING SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a map of <i>remaining service time</i> onto jobs that started, <i>have</i> exactly that remaining service time,
   * and are still present (and thus executing) in the queue.
   * 
   * <p>
   * This map has no equivalence in the bare {@link SimQueue}, but the concept is used in,
   * for instance, processor-sharing queues.
   * Implementations must maintain the aggregate value set of the map returned to the set of jobs currently executing,
   * and must set the initial key for each job to the result returned from
   * {@link SimJob#getServiceTime}.
   * 
   * <p>
   * Within the list, the jobs are in start order.
   * Any job can only be in one of the value lists.
   * The value lists only holds jobs currently executing in the system,
   * and, vice versa, any job executing in the system must be in exactly one of the value lists.
   * Null keys or values are not allowed.
   * 
   * @return A map of <i>remaining service time</i> onto jobs that started, <i>have</i> exactly that remaining service time,
   *         and are still present (and thus executing) in the queue.
   * 
   */
  public NavigableMap<Double, List<J>> getRemainingServiceMap ();
  
  /** Gets a map from all jobs executing onto their remaining service times.
   * 
   * <p>
   * This map has no equivalence in the bare {@link SimQueue}, but the concept is used in,
   * for instance, processor-sharing queues.
   * Implementations must maintain the key set of the map returned to the set of jobs currently executing,
   * and must set the initial value for each job to the result returned from
   * {@link SimJob#getServiceTime}.
   * 
   * <p>
   * Values must be non-negative (but do not have to be decreasing in time!).
   * 
   * @return A map from all jobs executing onto their remaining service times (non-negative).
   * 
   */
  public Map<J, Double> getJobRemainingServiceTimeMap ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Adds jobs as arrivals to the queue.
   * 
   * <p>
   * The arrival time cannot be in the past.
   * 
   * <p>
   * If the queue is on queue-access vacation, the jobs are dropped (i.e., not added to this state object).
   * 
   * @param time      The arrival time.
   * @param arrivals  The jobs that arrive.
   * @param visitLogs An optional set of visit log to which entries for dropped jobs (due to queue-access vacations)
   *                  are added (if the set is non-{@code null}).
   * 
   * @throws IllegalArgumentException If the arrival time is in the past,
   *                                  the set is {@code null} or has {@code null} members,
   *                                  or contains jobs already present at the queue.
   * 
   * @see #isQueueAccessVacation
   * 
   */
  public void doArrivals (double time, Set<J> arrivals, Set<JobQueueVisitLog<J, Q>> visitLogs);
  
  /** Starts jobs at the queue.
   * 
   * <p>
   * The start time cannot be in the past.
   * 
   * <p>
   * This method updates the server-access credits, and throw an exception if there are not sufficient server-access credits
   * to start all jobs offered.
   * 
   * @param time     The start time.
   * @param starters The jobs that start.
   * 
   * @throws IllegalArgumentException If the start time is in the past, the set is {@code null} or has {@code null} members,
   *                                  or contains jobs not present at the queue or jobs already started,
   *                                  or there are insufficient server-access credits.
   * 
   * @see #getServerAccessCredits
   * 
   */
  public void doStarts (double time, Set<J> starters);
  
  /** Removes jobs from the queue.
   * 
   * <p>
   * The removal time cannot be in the past.
   * 
   * @param time        The removal time.
   * @param drops       The jobs that drop, may be {@code null}.
   * @param revocations The jobs that are revoked, may be {@code null}.
   * @param departures  The jobs that depart, may be {@code null}.
   * @param stickers    The jobs that stick (never leave), may be {@code null}.
   * @param visitLogs   An optional set of visit log to which entries for the leaving jobs
   *                    are added (if the set is non-{@code null}).
   * 
   * @throws IllegalArgumentException If the removal time is in the past, a non-{@code null} set has {@code null} members,
   *                                  the sets are not pairwise disjoint,
   *                                  or any non-{@code null} set contains jobs not present at the queue,
   *                                  or if effectively no job at all is presented.
   * 
   */
  public void doExits (double time,
    Set<J> drops, Set<J> revocations, Set<J> departures, Set<J>stickers,
    Set<JobQueueVisitLog<J, Q>> visitLogs);
  
}
