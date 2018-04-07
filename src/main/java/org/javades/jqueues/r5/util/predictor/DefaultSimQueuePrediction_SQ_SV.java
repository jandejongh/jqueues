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
package org.javades.jqueues.r5.util.predictor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** A default implementation of a {@link SimQueuePrediction_SQ_SV}.
 *
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
public class DefaultSimQueuePrediction_SQ_SV<Q extends SimQueue>
implements SimQueuePrediction_SQ_SV<Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new prediction.
   * 
   * @param queue     The queue to which the prediction applies, non-{@code null}.
   * @param visitLogs The job-visit logs, non-{@code null}.
   * @param qavLog    The queue-access-vacation logs, non-{@code null}.
   * @param sacLog    The server-access-credits availability logs, non-{@code null}.
   * @param staLog    The {@code StartArmed} logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException If any of the arguments is {@code null} or improperly structured.
   * 
   */
  public DefaultSimQueuePrediction_SQ_SV
  (final Q queue,
   final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs,
   final List<Map<Double, Boolean>> qavLog,
   final List<Map<Double, Boolean>> sacLog,
   final List<Map<Double, Boolean>> staLog)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (visitLogs == null)
      throw new IllegalArgumentException ();
    if (qavLog == null)
      throw new IllegalArgumentException ();
    if (sacLog == null)
      throw new IllegalArgumentException ();
    if (staLog == null)
      throw new IllegalArgumentException ();
    if (visitLogs.containsKey (null) || visitLogs.containsValue (null))
      throw new IllegalArgumentException ();
    for (final Entry<SimJob, JobQueueVisitLog<SimJob, Q>> entry : visitLogs.entrySet ())
    {
      final SimJob job = entry.getKey ();
      final JobQueueVisitLog jqvl = entry.getValue ();
      if (jqvl.queue != queue)
        throw new IllegalArgumentException ();
      if (jqvl.job != job)
        throw new IllegalArgumentException ();
    }
    for (final Map<Double, Boolean> entry : qavLog)
      if (entry == null || entry.size () != 1)
        throw new IllegalArgumentException ();
    for (final Map<Double, Boolean> entry : sacLog)
      if (entry == null || entry.size () != 1)
        throw new IllegalArgumentException ();
    for (final Map<Double, Boolean> entry : staLog)
      if (entry == null || entry.size () != 1)
        throw new IllegalArgumentException ();
    this.queue = queue;
    this.visitLogs = visitLogs;
    this.qavLog = qavLog;
    this.sacLog = sacLog;
    this.staLog = staLog;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Q queue;
  
  @Override
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VISIT LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs;
  
  @Override
  public final Map<SimJob, JobQueueVisitLog<SimJob, Q>> getVisitLogs ()
  {
    return this.visitLogs;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QAV LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<Map<Double, Boolean>> qavLog;
  
  @Override
  public final List<Map<Double, Boolean>> getQueueAccessVacationLog ()
  {
    return this.qavLog;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SAC LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<Map<Double, Boolean>> sacLog;
  
  @Override
  public final List<Map<Double, Boolean>> getServerAccessCreditsAvailabilityLog ()
  {
    return this.sacLog;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STA LOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<Map<Double, Boolean>> staLog;
  
  @Override
  public final List<Map<Double, Boolean>> getStartArmedLog ()
  {
    return this.staLog;
  }
  
}
