package nl.jdj.jqueues.r5.entity.job.visitslogging;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.job.selflistening.DefaultSelfListeningSimJob;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link DefaultSimJob} that logs its {@link SimQueue} visits with {@link JobQueueVisitLog}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class DefaultVisitsLoggingSimJob<J extends DefaultVisitsLoggingSimJob, Q extends SimQueue>
extends DefaultSelfListeningSimJob<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VISIT LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The visit logs; indexed by arrival time.
   * 
   * <p>
   * For each arrival time, the consecutive visits are logged as {@link JobQueueVisitLog},
   * bearing in mind that a job can visit multiple queues in sequence at the same instant.
   * Visit counts start with zero.
   * 
   */
  private final TreeMap<Double, TreeMap<Integer, JobQueueVisitLog>> visitLogs
    = new TreeMap<> ();
  
  /** Returns the {@link JobQueueVisitLog}s; indexed by arrival time.
   * 
   * <p>
   * For each arrival time, the consecutive visits are logged as {@link JobQueueVisitLog},
   * bearing in mind that a job can visit multiple queues in sequence at the same instant.
   * Visit counts start with zero.
   * 
   * <p>
   * A reference to the internal data is returned; it should not be altered.
   * 
   * @return The {@link JobQueueVisitLog}s; indexed by arrival time.
   * 
   */
  public final TreeMap<Double, TreeMap<Integer, JobQueueVisitLog>> getVisitLogs ()
  {
    return this.visitLogs;
  }

  /** Clears the visit logs gathered thus far.
   * 
   */
  public final void reset ()
  {
    this.visitLogs.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Creates a new {@link DefaultVisitsLoggingSimJob}.
   * 
   * @see DefaultSimJob#DefaultSimJob For detailed explanation on the parameters.
   * 
   */
  public DefaultVisitsLoggingSimJob (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    super (eventList, name, requestedServiceTimeMap);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimJobListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public final void notifyUpdate (final double time, final SimEntity entity)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public final void notifyStateChanged (final double time, final SimEntity entity)
  {
    /* EMPTY */
  }

  @Override
  public final void notifyArrival (final double time, final J job, final Q queue)
  {
    if (job == this)
    {
      final double arrivalTime = time;
      if (queue == null)
        throw new IllegalArgumentException ();
      if (! this.visitLogs.containsKey (arrivalTime))
        this.visitLogs.put (arrivalTime, new TreeMap<> ());
      final int newSeqKey = this.visitLogs.get (arrivalTime).isEmpty () ? 0
        : (this.visitLogs.get (arrivalTime).lastKey () + 1);
      final JobQueueVisitLog visitLog = new JobQueueVisitLog
        (this, queue,
          true,  arrivalTime,
          newSeqKey,
          false, Double.NaN,
          false, Double.NaN,
          false, Double.NaN,
          false, Double.NaN);
      this.visitLogs.get (arrivalTime).put (newSeqKey, visitLog);
    }
  }

  @Override
  public final void notifyStart (final double time, final J job, final Q queue)
  {
    if (job == this)
    {
      final double startTime = time;
      if (queue == null)
        throw new IllegalArgumentException ();
      if (this.visitLogs.isEmpty ())
        throw new IllegalStateException ();
      if (this.visitLogs.lastEntry ().getValue ().isEmpty ())
        throw new IllegalStateException ();
      final Entry<Integer, JobQueueVisitLog> oldVisitLogEntry
        = this.visitLogs.lastEntry ().getValue ().lastEntry ();
      final int oldVisitLogSequenceNumber = oldVisitLogEntry.getKey ();
      final JobQueueVisitLog oldVisitLog = oldVisitLogEntry.getValue ();
      if ((! oldVisitLog.arrived)
        || oldVisitLog.started
        || startTime < oldVisitLog.arrivalTime
        || oldVisitLog.dropped
        || oldVisitLog.revoked
        || oldVisitLog.departed
        || oldVisitLog.queue != queue
        || oldVisitLog.queue != getQueue ()
        || oldVisitLog.job != this)
        throw new IllegalStateException ();
      final double arrivalTime = oldVisitLog.arrivalTime;
      final int seqNo = oldVisitLog.sequenceNumber;
      final JobQueueVisitLog visitLog = new JobQueueVisitLog
        (this, queue,
          true,  arrivalTime,
          seqNo,
          true,  startTime,
          false, Double.NaN,
          false, Double.NaN,
          false, Double.NaN);
      this.visitLogs.lastEntry ().getValue ().put (oldVisitLogSequenceNumber, visitLog);
    }
  }

  @Override
  public final void notifyDrop (final double time, final J job, final Q queue)
  {
    if (job == this)
    {
      final double dropTime = time;
      if (queue == null)
        throw new IllegalArgumentException ();
      if (this.visitLogs.isEmpty ())
        throw new IllegalStateException ();
      if (this.visitLogs.lastEntry ().getValue ().isEmpty ())
        throw new IllegalStateException ();
      final Entry<Integer, JobQueueVisitLog> oldVisitLogEntry
        = this.visitLogs.lastEntry ().getValue ().lastEntry ();
      final int oldVisitLogSequenceNumber = oldVisitLogEntry.getKey ();
      final JobQueueVisitLog oldVisitLog = oldVisitLogEntry.getValue ();
      if ((! oldVisitLog.arrived)
        || dropTime < oldVisitLog.arrivalTime
        || oldVisitLog.dropped
        || oldVisitLog.revoked
        || oldVisitLog.departed
        || oldVisitLog.queue != queue
        || oldVisitLog.job != this)
        throw new IllegalStateException ();
      final double arrivalTime = oldVisitLog.arrivalTime;
      final int seqNo = oldVisitLog.sequenceNumber;
      final boolean started = oldVisitLog.started;
      final double startTime = oldVisitLog.startTime;
      final JobQueueVisitLog visitLog = new JobQueueVisitLog
        (this, queue,
          true,    arrivalTime,
          seqNo,
          started, startTime,
          true,    dropTime,
          false,   Double.NaN,
          false,   Double.NaN);
      this.visitLogs.lastEntry ().getValue ().put (oldVisitLogSequenceNumber, visitLog);
    }
  }

  @Override
  public final void notifyRevocation (final double time, final J job, final Q queue)
  {
    if (job == this)
    {
      final double revocationTime = time;
      if (queue == null)
        throw new IllegalArgumentException ();
      if (this.visitLogs.isEmpty ())
        throw new IllegalStateException ();
      if (this.visitLogs.lastEntry ().getValue ().isEmpty ())
        throw new IllegalStateException ();
      final Entry<Integer, JobQueueVisitLog> oldVisitLogEntry
        = this.visitLogs.lastEntry ().getValue ().lastEntry ();
      final int oldVisitLogSequenceNumber = oldVisitLogEntry.getKey ();
      final JobQueueVisitLog oldVisitLog = oldVisitLogEntry.getValue ();
      if ((! oldVisitLog.arrived)
        || revocationTime < oldVisitLog.arrivalTime
        || oldVisitLog.dropped
        || oldVisitLog.revoked
        || oldVisitLog.departed
        || oldVisitLog.queue != queue
        || oldVisitLog.job != this)
        throw new IllegalStateException ();
      final double arrivalTime = oldVisitLog.arrivalTime;
      final int seqNo = oldVisitLog.sequenceNumber;
      final boolean started = oldVisitLog.started;
      final double startTime = oldVisitLog.startTime;
      final JobQueueVisitLog visitLog = new JobQueueVisitLog
        (this, queue,
          true,    arrivalTime,
          seqNo,
          started, startTime,
          false,   Double.NaN,
          true,    revocationTime,
          false,   Double.NaN);
      this.visitLogs.lastEntry ().getValue ().put (oldVisitLogSequenceNumber, visitLog);
    }
  }
  
  @Override
  public final void notifyDeparture (final double time, final J job, final Q queue)
  {
    if (job == this)
    {
      final double departureTime = time;
      if (queue == null)
        throw new IllegalArgumentException ();
      if (this.visitLogs.isEmpty ())
        throw new IllegalStateException ();
      if (this.visitLogs.lastEntry ().getValue ().isEmpty ())
        throw new IllegalStateException ();
      final Entry<Integer, JobQueueVisitLog> oldVisitLogEntry
        = this.visitLogs.lastEntry ().getValue ().lastEntry ();
      final int oldVisitLogSequenceNumber = oldVisitLogEntry.getKey ();
      final JobQueueVisitLog oldVisitLog = oldVisitLogEntry.getValue ();
      if ((! oldVisitLog.arrived)
        || departureTime < oldVisitLog.arrivalTime
        || oldVisitLog.dropped
        || oldVisitLog.revoked
        || oldVisitLog.departed
        || oldVisitLog.queue != queue
        || oldVisitLog.job != this)
        throw new IllegalStateException ();
      final double arrivalTime = oldVisitLog.arrivalTime;
      final int seqNo = oldVisitLog.sequenceNumber;
      final boolean started = oldVisitLog.started;
      final double startTime = oldVisitLog.startTime;
      final JobQueueVisitLog visitLog = new JobQueueVisitLog
        (this, queue,
          true,    arrivalTime,
          seqNo,
          started, startTime,
          false,   Double.NaN,
          false,   Double.NaN,
          true,    departureTime);
      this.visitLogs.lastEntry ().getValue ().put (oldVisitLogSequenceNumber, visitLog);
    }
  }
  
}
