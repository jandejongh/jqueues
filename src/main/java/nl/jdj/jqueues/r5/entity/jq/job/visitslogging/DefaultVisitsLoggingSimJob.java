/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.jdj.jqueues.r5.entity.jq.job.visitslogging;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.selflistening.DefaultSelfListeningSimJob;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link DefaultSimJob} that logs its {@link SimQueue} visits with {@link JobQueueVisitLog}s.
 *
 * <p>
 * Visit-logging jobs play a crucial role in the testing sub-system of {@link SimQueue}s.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class DefaultVisitsLoggingSimJob<J extends DefaultVisitsLoggingSimJob, Q extends SimQueue>
extends DefaultSelfListeningSimJob<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Creates a new {@link DefaultVisitsLoggingSimJob}.
   * 
   * @param eventList               The event list.
   * @param name                    The name.
   * @param requestedServiceTimeMap The requested service-time map.
   * 
   * @see DefaultSimJob#DefaultSimJob(nl.jdj.jsimulation.r5.SimEventList, java.lang.String, java.util.Map)
   *        For detailed explanation on the parameters.
   * 
   */
  public DefaultVisitsLoggingSimJob (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    super (eventList, name, requestedServiceTimeMap);
  }

  /** Creates a new {@link DefaultVisitsLoggingSimJob}.
   * 
   * @param eventList            The event list.
   * @param name                 The name.
   * @param requestedServiceTime The requested service-time.
   * 
   * @see DefaultSimJob#DefaultSimJob(nl.jdj.jsimulation.r5.SimEventList, java.lang.String, double)
   *        For detailed explanation on the parameters.
   * 
   */
  public DefaultVisitsLoggingSimJob (final SimEventList eventList, final String name, final double requestedServiceTime)
  {
    super (eventList, name, requestedServiceTime);
  }

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
   // XXX Why not use a simple ArrayList for the "value" part???
   // private final TreeMap<Double, List<JobQueueVisitLog>> visitLogs
    = new TreeMap<> ();
  
  /** Returns the {@link JobQueueVisitLog}s; indexed by arrival time.
   * 
   * <p>
   * For each arrival time, the consecutive visits are logged as {@link JobQueueVisitLog},
   * bearing in mind that a job can visit multiple queues in sequence at the same instant.
   * Visit counts start with zero.
   * 
   * <p>
   * A new map is created for the outer-level, but no deep-cloning is performed.
   * Hence changes to the visit logs (for instance) will "write though" to the visit logs of this job.
   * 
   * @return The {@link JobQueueVisitLog}s; indexed by arrival time.
   * 
   */
  public final TreeMap<Double, TreeMap<Integer, JobQueueVisitLog>> getVisitLogs ()
  {
    return new TreeMap<> (this.visitLogs);
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
  public final void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
  {
    /* EMPTY */
  }

  /** Performs sanity checks and administers the event in the visit logs.
   * 
   */
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

  /** Performs sanity checks and administers the event in the visit logs.
   * 
   */
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
        // Removed this check; a started job may no longer be present at the queue at the moment of notification!
        // || oldVisitLog.queue != getQueue ()
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

  /** Performs sanity checks and administers the event in the visit logs.
   * 
   */
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

  /** Performs sanity checks and administers the event in the visit logs.
   * 
   */
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
  
  /** Invokes {@link #notifyRevocation}.
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final J job, final Q queue)
  {
    notifyRevocation (time, job, queue);
  }
  
  /** Performs sanity checks and administers the event in the visit logs.
   * 
   */
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
