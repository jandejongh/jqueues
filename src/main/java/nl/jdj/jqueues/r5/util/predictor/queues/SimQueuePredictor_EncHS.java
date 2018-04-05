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
package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.EncHS;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEvent;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite_Enc;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.DefaultSimQueuePrediction_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionInvalidInputException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePrediction_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link EncHS}.
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
public class SimQueuePredictor_EncHS
extends AbstractSimQueuePredictor_Composite_Enc<EncHS>
implements SimQueuePredictor<EncHS>
{
  
  final SimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_EncHS (final AbstractSimQueuePredictor encQueuePredictor)
  {
    super (encQueuePredictor);
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public String toString ()
  {
    return "Predictor[EncHS[?]]";
  }

  @Override
  public boolean isStartArmed
  (final EncHS queue,
   final SimQueueState<SimJob, EncHS> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return false;
  }

  @Override
  public SimQueuePrediction_SQ_SV
  predict_SQ_SV_ROEL_U
  (final EncHS queue, final Set<SimJQEvent> queueEvents)
   throws SimQueuePredictionException
  {
    if (queue == null || queue.getEncapsulatedQueue () == null)
      throw new IllegalArgumentException ();
    if (queueEvents == null)
      return new DefaultSimQueuePrediction_SQ_SV
                   (queue, Collections.EMPTY_MAP, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    final SimQueue encQueue = queue.getEncapsulatedQueue ();
    final Set<SimJQEvent> encQueueEvents = new LinkedHashSet<> ();
    int compSac = Integer.MAX_VALUE;
    final List<Map<Double, Boolean>> compSacLog = new ArrayList<>  ();
    for (SimJQEvent e : queueEvents)
    {
      if (e == null)
        throw new IllegalArgumentException ();
      if (e.getQueue () == encQueue)
        throw new IllegalArgumentException ();
      if (e.getQueue () == queue)
      {
        final double time = e.getTime ();
        final SimJob job = e.getJob ();
        if (e instanceof SimQueueEvent.QueueAccessVacation)
        {
          final boolean vacation = ((SimQueueEvent.QueueAccessVacation) e).getVacation ();
          encQueueEvents.add (new SimQueueEvent.QueueAccessVacation<> (encQueue, time, vacation));
        }
        else if (e instanceof SimJQEvent.Arrival)
        {
          if (! (job instanceof DefaultSimJob))
            throw new UnsupportedOperationException ();
          ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (encQueue, job.getServiceTime (queue));
          encQueueEvents.add (new SimJQEvent.Arrival<> (job, encQueue, time));
        }
        else if (e instanceof SimJQEvent.Revocation)
          // The interruptService argument is irrelevant; on an EncHS queue, real jobs are always in the waiting area,
          // hence a revocation cannot fail.
          // We must therefore revoke the delegate job unconditionally from the encapsulated queue.
          encQueueEvents.add (new SimJQEvent.Revocation<> (job, encQueue, time, true));
        else if (e instanceof SimQueueEvent.ServerAccessCredits)
        {
          // We must not pass the server-access credits events to the encapsulated queue, but process them ourselves.
          final int oldCredits = compSac;
          final int newCredits = ((SimQueueEvent.ServerAccessCredits) e).getCredits ();
          if (oldCredits == 0 && newCredits > 0)
            compSacLog.add (Collections.singletonMap (time, true));
          if (oldCredits > 0 && newCredits == 0)
            compSacLog.add (Collections.singletonMap (time, false));
          compSac = newCredits;
        }
        else
          throw new SimQueuePredictionInvalidInputException ();
      }
    }
    final SimQueuePrediction_SQ_SV encPrediction = this.encQueuePredictor.predict_SQ_SV_ROEL_U (encQueue, encQueueEvents);
    final Map<SimJob, JobQueueVisitLog<SimJob, SimQueue>> encVisitLogs = encPrediction.getVisitLogs ();
    final Map<SimJob, JobQueueVisitLog<SimJob, EncHS>> visitLogs = new HashMap<> ();
    for (final Entry<SimJob, JobQueueVisitLog<SimJob, SimQueue>> entry : encVisitLogs.entrySet ())
    {
      final SimJob j = entry.getKey ();
      if (j == null)
        throw new UnsupportedOperationException ();
      final JobQueueVisitLog<SimJob, SimQueue> jvl = entry.getValue ();
      if (jvl == null)
        throw new UnsupportedOperationException ();
      if (jvl.job != j || jvl.queue != encQueue)
        throw new RuntimeException ();
      visitLogs.put (j,
        new JobQueueVisitLog (j, queue,
          jvl.arrived, jvl.arrivalTime, jvl.sequenceNumber,
          // Ignore the start of the delegate job!
          false, Double.NaN,
          jvl.dropped, jvl.dropTime,
          jvl.revoked, jvl.revocationTime,
          jvl.departed, jvl.departureTime));
    }
    final List<Map<Double, Boolean>> encQavLog = encPrediction.getQueueAccessVacationLog ();
    final List<Map<Double, Boolean>> encSacLog = encPrediction.getServerAccessCreditsAvailabilityLog ();
    if (! encSacLog.isEmpty ())
      throw new IllegalStateException ();
    final List<Map<Double, Boolean>> encStaLog = encPrediction.getStartArmedLog ();
    return new DefaultSimQueuePrediction_SQ_SV<> (queue, visitLogs, encQavLog, compSacLog, Collections.EMPTY_LIST);
  }
  
  @Override
  public SimQueuePrediction_SQ_SV
  predict_SQ_SV_IOEL_U
  (final EncHS queue,
   final NavigableMap<Double, Set<SimJQEvent>> workloadEventsMap,
   final NavigableMap<Double, Set<SimJQEvent>> processedEventsMap)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }  

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final EncHS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, EncHS> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncHS>> visitLogsSet)
  throws SimQueuePredictionException, WorkloadScheduleException
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final EncHS queue,
   final SimQueueState<SimJob, EncHS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncHS>> visitLogsSet)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }
  
}