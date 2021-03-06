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
package org.javades.jqueues.r5.util.predictor.queues;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.job.DefaultSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent;
import org.javades.jqueues.r5.entity.jq.queue.composite.enc.Enc;
import org.javades.jqueues.r5.extensions.composite.SimQueueCompositeStateHandler;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.DefaultSimQueuePrediction_SQ_SV;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionInvalidInputException;
import org.javades.jqueues.r5.util.predictor.SimQueuePrediction_SQ_SV;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link Enc}.
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
public class SimQueuePredictor_Enc
extends AbstractSimQueuePredictor<Enc>
implements SimQueuePredictor<Enc>
{
  
  final SimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_Enc (final SimQueuePredictor encQueuePredictor)
  {
    if (encQueuePredictor == null)
      throw new IllegalArgumentException ();
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public String toString ()
  {
    return "Predictor[Enc[?]]";
  }

  @Override
  public SimQueueState<SimJob, Enc> createQueueState
  (final Enc queue,
   final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    final SimQueue encQueue = queue.getEncapsulatedQueue ();
    final DefaultSimQueueState encQueueState = (DefaultSimQueueState) this.encQueuePredictor.createQueueState (encQueue, isROEL);
    queueState.registerHandler (new SimQueueCompositeStateHandler (queue.getQueues (), Collections.singleton (encQueueState)));
    return queueState;
  }

  @Override
  public boolean hasServerAccessCredits
  (final Enc queue,
   final SimQueueState<SimJob, Enc> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    if (queueState == null)
      throw new IllegalArgumentException ();
    return this.encQueuePredictor.hasServerAccessCredits (queue.getEncapsulatedQueue (), queueStateHandler.getSubQueueState (0));
  }

  @Override
  public boolean isStartArmed
  (final Enc queue,
   final SimQueueState<SimJob, Enc> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    if (queueState == null)
      throw new IllegalArgumentException ();
    return this.encQueuePredictor.isStartArmed (queue.getEncapsulatedQueue (), queueStateHandler.getSubQueueState (0));
  }

  @Override
  public SimQueuePrediction_SQ_SV
  predict_SQ_SV_ROEL_U
  (final Enc queue, final Set<SimJQEvent> queueEvents)
   throws SimQueuePredictionException
  {
    if (queue == null || queue.getEncapsulatedQueue () == null)
      throw new IllegalArgumentException ();
    if (queueEvents == null)
      return new DefaultSimQueuePrediction_SQ_SV
                   (queue, Collections.EMPTY_MAP, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    final SimQueue encQueue = queue.getEncapsulatedQueue ();
    final Set<SimJQEvent> encQueueEvents = new LinkedHashSet<> ();
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
        {
          final boolean interruptService = ((SimJQEvent.Revocation) e).isInterruptService ();
          encQueueEvents.add (new SimJQEvent.Revocation<> (job, encQueue, time, interruptService));
        }
        else if (e instanceof SimQueueEvent.ServerAccessCredits)
        {
          final int credits = ((SimQueueEvent.ServerAccessCredits) e).getCredits ();
          encQueueEvents.add (new SimQueueEvent.ServerAccessCredits<> (encQueue, time, credits));
        }
        else
          throw new SimQueuePredictionInvalidInputException ();
      }
    }
    final SimQueuePrediction_SQ_SV encPrediction = this.encQueuePredictor.predict_SQ_SV_ROEL_U (encQueue, encQueueEvents);
    final Map<SimJob, JobQueueVisitLog<SimJob, SimQueue>> encVisitLogs = encPrediction.getVisitLogs ();
    final Map<SimJob, JobQueueVisitLog<SimJob, Enc>> visitLogs = new HashMap<> ();
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
          jvl.started, jvl.startTime,
          jvl.dropped, jvl.dropTime,
          jvl.revoked, jvl.revocationTime,
          jvl.departed, jvl.departureTime));
    }
    final List<Map<Double, Boolean>> encQavLog = encPrediction.getQueueAccessVacationLog ();
    final List<Map<Double, Boolean>> encSacLog = encPrediction.getServerAccessCreditsAvailabilityLog ();
    final List<Map<Double, Boolean>> encStaLog = encPrediction.getStartArmedLog ();
    return new DefaultSimQueuePrediction_SQ_SV<> (queue, visitLogs, encQavLog, encSacLog, encStaLog);
  }
  
  @Override
  public SimQueuePrediction_SQ_SV
  predict_SQ_SV_IOEL_U
  (final Enc queue,
   final NavigableMap<Double, Set<SimJQEvent>> workloadEventsMap,
   final NavigableMap<Double, Set<SimJQEvent>> processedEventsMap)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }  

  @Override
  public double getNextQueueEventTimeBeyond
  (final Enc queue,
   final SimQueueState<SimJob, Enc> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void updateToTime
  (final Enc queue,
   final SimQueueState queueState,
   final double newTime)
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final Enc queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, Enc> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, Enc>> visitLogsSet)
  throws SimQueuePredictionException, WorkloadScheduleException
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final Enc queue,
   final SimQueueState<SimJob, Enc> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, Enc>> visitLogsSet)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }
  
}