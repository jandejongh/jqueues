package nl.jdj.jqueues.r5.entity.job.visitslogging;

import java.io.PrintStream;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** Job-centric record of a single queue visit.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class JobQueueVisitLog<J extends SimJob, Q extends SimQueue>
{
  
  /** The job.
   * 
   */
  public final J job;
  
  /** The queue the job visits.
   * 
   */
  public final Q queue;
  
  /** Whether the job arrived.
   * 
   */
  public final boolean arrived;

  /** The arrival time, in case the job arrived.
   * 
   */
  public final double arrivalTime;

  /** The sequence number of arrivals at the same time.
   * 
   */
  public final int sequenceNumber;
  
  /** Whether the job started.
   * 
   */
  public final boolean started;

  /** The start time in case the job started.
   * 
   */
  public final double startTime;

  /** Whether the job was dropped.
   * 
   */
  public final boolean dropped;
  
  /** The drop time in case the job was dropped.
   * 
   */
  public final double dropTime;

  /** Whether the job was successfully revoked.
   * 
   */
  public final boolean revoked;

  /** The revocation time in case the job was successfully revoked.
   * 
   */
  public final double revocationTime;
  
  /** Whether the job departed.
   * 
   */
  public final boolean departed;

  /** The departure time in case the job departed.
   * 
   */
  public final double departureTime;

  /** Creates a new job-centric record of a single queue visit.
   * 
   * @param job            The job.
   * @param queue          The queue the job visits.
   * @param arrived        Whether the job arrived.
   * @param arrivalTime    The arrival time, in case the job arrived.
   * @param sequenceNumber The sequence number of arrivals at the same time, starting with zero.
   * @param started        Whether the job started.
   * @param startTime      The start time in case the job started.
   * @param dropped        Whether the job was dropped.
   * @param dropTime       The drop time in case the job was dropped.
   * @param revoked        Whether the job was successfully revoked.
   * @param revocationTime The revocation time in case the job was successfully revoked.
   * @param departed       Whether the job departed.
   * @param departureTime  The departure time in case the job departed.
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>, or sanity checks fail in view of the allowable
   *                                  life cycles of a job visit (e.g., a job cannot start before it has arrived,
   *                                  it cannot both be dropped and be revoked, etc.).
   * 
   */
  public JobQueueVisitLog
  (final J job, final Q queue,
    final boolean arrived, final double arrivalTime,
    final int sequenceNumber,
    final boolean started, final double startTime,
    final boolean dropped, final double dropTime,
    final boolean revoked, final double revocationTime,
    final boolean departed, final double departureTime)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if ((! arrived) && (started || dropped || revoked || departed))
      throw new IllegalArgumentException ();
    if (arrived && ((dropped && revoked) || (dropped && departed) || (revoked && departed)))
      throw new IllegalArgumentException ();
    if (arrived && started && startTime < arrivalTime)
      throw new IllegalArgumentException ();
    if (arrived && dropped && dropTime < arrivalTime)
      throw new IllegalArgumentException ();
    if (arrived && revoked && revocationTime < arrivalTime)
      throw new IllegalArgumentException ();
    if (arrived && departed && departureTime < arrivalTime)
      throw new IllegalArgumentException ();
    if (arrived && started && dropped && dropTime < startTime)
      throw new IllegalArgumentException ();
    if (arrived && started && revoked && revocationTime < startTime)
      throw new IllegalArgumentException ();
    if (arrived && started && departed && departureTime < startTime)
      throw new IllegalArgumentException ();
    if (sequenceNumber < 0)
      throw new IllegalArgumentException ();
    this.job = job;
    this.queue = queue;
    this.arrived = arrived;
    this.arrivalTime = arrivalTime;
    this.sequenceNumber = sequenceNumber;
    this.started = started;
    this.startTime = startTime;
    this.dropped = dropped;
    this.dropTime = dropTime;
    this.revoked = revoked;
    this.revocationTime = revocationTime;
    this.departed = departed;
    this.departureTime = departureTime;
  }

  /** Tests this {@link JobQueueVisitLog} with a supplied one for equality.
   * 
   * <p>
   * All boolean and integer fields are simply tested for equality.
   * The various time fields are only compared if the corresponding condition is {@code true}, e.g.,
   * the {@link #startTime} fields are only checked if {@link #started}{@code == true} (on both objects).
   * 
   * @param object   The other {@link JobQueueVisitLog}; may be {@code null} or {@code this}, resulting in {@code false} and
   *                 {@code true}, respectively.
   * @param accuracy The absolute allowed deviation for the double comparisons; non-negative.
   * 
   * @return True if the supplied object is equal to this one (within the given accuracy).
   * 
   * @throws IllegalArgumentException If the accuracy is negative (zero is allowed).
   * 
   */
  public boolean equals (final JobQueueVisitLog object, final double accuracy)
  {
    if (accuracy < 0)
      throw new IllegalArgumentException ();
    if (object == null)
      return false;
    if (object == this)
      return true;
    if (this.job != object.job)
      return false;
    if (this.queue != object.queue)
      return false;
    if (this.arrived != object.arrived)
      return false;
    if (this.arrived && Math.abs (this.arrivalTime - object.arrivalTime) > accuracy)
      return false;
    if (this.sequenceNumber != object.sequenceNumber)
      return false;
    if (this.started != object.started)
      return false;
    if (this.started && Math.abs (this.startTime - object.startTime) > accuracy)
      return false;
    if (this.dropped != object.dropped)
      return false;
    if (this.dropped && Math.abs (this.dropTime - object.dropTime) > accuracy)
      return false;
    if (this.revoked != object.revoked)
      return false;
    if (this.revoked && Math.abs (this.revocationTime - object.revocationTime) > accuracy)
      return false;
    if (this.departed != object.departed)
      return false;
    if (this.departed && Math.abs (this.departureTime - object.departureTime) > accuracy)
      return false;
    return true;
  }

  /** Prints this visit log to the supplied stream.
   * 
   * @param out The stream, if {@code null}, {@code System.out} is used.
   * 
   */
  public void print (PrintStream out)
  {
    if (out == null)
      out = System.out;
    out.println ("Visit Log: job=" + this.job + "@queue=" + this.queue + ":");
    out.println ("  Arrived : " + this.arrived  + (this.arrived  ? ("@" + Double.toString (this.arrivalTime))    : "") + ".");
    out.println ("  SeqNo   : " + this.sequenceNumber + ".");
    out.println ("  Started : " + this.started  + (this.started  ? ("@" + Double.toString (this.startTime))      : "") + ".");
    out.println ("  Dropped : " + this.dropped  + (this.dropped  ? ("@" + Double.toString (this.dropTime))       : "") + ".");
    out.println ("  Revoked : " + this.revoked  + (this.revoked  ? ("@" + Double.toString (this.revocationTime)) : "") + ".");
    out.println ("  Departed: " + this.departed + (this.departed ? ("@" + Double.toString (this.departureTime))  : "") + ".");
  }
  
  /** Adds a dropped job at a queue to a set of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs   The set.
   * @param queue       The queue.
   * @param job         The job.
   * @param arrivalTime The arrival time.
   * @param started     Whether the job has already started.
   * @param startTime   The start time of the job, if started.
   * @param dropTime    The drop time.
   * 
   * @throws IllegalArgumentException If the set, queue, or job is {@code null},
   *                                  or if sanity checks on the time arguments fail.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void addDroppedJob
  (final Set<JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime,
   final double dropTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || dropTime < arrivalTime
      || (started && startTime < arrivalTime)
      || (started && dropTime < startTime))
      throw new IllegalArgumentException ();
    visitLogs.add (new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        true,  dropTime,
        false, Double.NaN,
        false, Double.NaN));
  }
  
  /** Adds a revoked job at a queue to a set of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs      The set.
   * @param queue          The queue.
   * @param job            The job.
   * @param arrivalTime    The arrival time.
   * @param started        Whether the job has already started.
   * @param startTime      The start time of the job, if started.
   * @param revocationTime The revocation time.
   * 
   * @throws IllegalArgumentException If the set, queue, or job is {@code null},
   *                                  or if sanity checks on the time arguments fail.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void addRevokedJob
  (final Set<JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime,
   final double revocationTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || revocationTime < arrivalTime
      || (started && startTime < arrivalTime)
      || (started && revocationTime < startTime))
      throw new IllegalArgumentException ();
    visitLogs.add (new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        false, Double.NaN,
        true, revocationTime,
        false, Double.NaN));
  }
  
  /** Adds a departed job at a queue to a set of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs     The set.
   * @param queue         The queue.
   * @param job           The job.
   * @param arrivalTime   The arrival time.
   * @param started       Whether the job has already started.
   * @param startTime     The start time of the job, if started.
   * @param departureTime The departure time.
   * 
   * @throws IllegalArgumentException If the set, queue, or job is {@code null},
   *                                  or if sanity checks on the time arguments fail.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void addDepartedJob
  (final Set<JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime,
   final double departureTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || (departureTime < arrivalTime)
      || (started && startTime < arrivalTime)
      || (started && startTime > departureTime))
      throw new IllegalArgumentException ();
    visitLogs.add (new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        false, Double.NaN,
        false, Double.NaN,
        true, departureTime));
  }
  
  /** Adds a sticky job (never leaves) at a queue to a set of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs   The set.
   * @param queue       The queue.
   * @param job         The job.
   * @param arrivalTime The arrival time.
   * @param started     Whether the job has already started.
   * @param startTime   The start time of the job, if started.
   * 
   * @throws IllegalArgumentException If the set, queue, or job is {@code null},
   *                                  or if sanity checks on the time arguments fail.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void addStickyJob
  (final Set<JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || (started && startTime < arrivalTime))
      throw new IllegalArgumentException ();
    visitLogs.add (new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        false, Double.NaN,
        false, Double.NaN,
        false, Double.NaN));
  }
  
}
