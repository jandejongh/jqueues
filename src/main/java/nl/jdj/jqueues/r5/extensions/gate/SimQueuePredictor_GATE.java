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
package nl.jdj.jqueues.r5.extensions.gate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.GATE;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleInvalidException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link GATE}.
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
public class SimQueuePredictor_GATE
extends AbstractSimQueuePredictor<GATE>
{

  @Override
  public String toString ()
  {
    return "Predictor[GATE]";
  }

  @Override
  public boolean isStartArmed (final GATE queue, final SimQueueState<SimJob, GATE> queueState)
  {
    return false;
  }

  /** Registers a new {@link SimQueueWithGateWorkloadScheduleHandler} at the object created by super method.
   * 
   */
  @Override
  public
  WorkloadSchedule_SQ_SV_ROEL_U
  createWorkloadSchedule_SQ_SV_ROEL_U
  (final GATE queue, final Set<SimJQEvent> workloadEvents)
  throws WorkloadScheduleException
  {
    final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule = super.createWorkloadSchedule_SQ_SV_ROEL_U (queue, workloadEvents);
    ((DefaultWorkloadSchedule) workloadSchedule).registerHandler (new SimQueueWithGateWorkloadScheduleHandler ());
    return workloadSchedule;
  }

  /** Registers a new {@link SimQueueWithGateStateHandler} at the object created by super method.
   * 
   */
  @Override
  public SimQueueState<SimJob, GATE> createQueueState (final GATE queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueWithGateStateHandler ());
    return queueState;
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final GATE queue, final SimQueueState<SimJob, GATE> queueState, final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    return Double.NaN;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final GATE queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, GATE> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, GATE>> visitLogsSet)
   throws SimQueuePredictionException, WorkloadScheduleException
  {
    if ( queue == null
      || workloadSchedule == null
      || queueState == null
      || workloadEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (workloadEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimQueueWithGateWorkloadScheduleHandler workloadScheduleHandler =
      (SimQueueWithGateWorkloadScheduleHandler)
        ((DefaultWorkloadSchedule) workloadSchedule).getHandler ("SimQueueWithGateHandler");
    final SimQueueWithGateStateHandler queueStateHandler =
      (SimQueueWithGateStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueWithGateHandler");
    final int oldPassages = queueStateHandler.getGatePassageCredits ();
    final SimEntitySimpleEventType.Member eventType = (workloadEventTypes.isEmpty ()
      ? null
      : workloadEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */
    }
    else if (eventType == SimQueueSimpleEventType.QUEUE_ACCESS_VACATION)
    {
      final boolean queueAccessVacation = workloadSchedule.getQueueAccessVacationMap_SQ_SV_ROEL_U ().get (time);
      queueState.setQueueAccessVacation (time, queueAccessVacation);
    }
    else if (eventType == SimQueueSimpleEventType.ARRIVAL)
    {
      final SimJob job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      final Set<SimJob> arrivals = new HashSet<> ();
      arrivals.add (job);
      // Abuse queueState.doArrivals for dropping arrivals upon queue-access vacation.
      if (queueState.isQueueAccessVacation ())
        queueState.doArrivals (time, arrivals, visitLogsSet);
      else if (oldPassages > 0)
      {
        // Departure.
        queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
        if (oldPassages < Integer.MAX_VALUE)
          queueStateHandler.setGatePassageCredits (time, oldPassages - 1);
      }
      else
        queueState.doArrivals (time, arrivals, visitLogsSet);        
    }
    else if (eventType == SimQueueSimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final Set<SimJob> revocations = new HashSet<> ();
        revocations.add (job);
        // Revocation.
        queueState.doExits (time, null, revocations, null, null, visitLogsSet);
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
    }
    else if (eventType == SimQueueWithGateSimpleEventType.GATE)
    {
      // XXX Already checked for right queue and ambiguities??
      int remainingPassages = workloadScheduleHandler.getGatePassageCreditsMap (queue).get (time).get (0);
      if (oldPassages == 0 && remainingPassages > 0 && ! queueState.getJobsInWaitingArea ().isEmpty ())
      {
        final Set<SimJob> departures = new LinkedHashSet<> ();
        final Iterator<SimJob> i_waiters = queueState.getJobsInWaitingAreaOrdered ().iterator ();
        while ((remainingPassages == Integer.MAX_VALUE || remainingPassages > 0) && i_waiters.hasNext ())
        {
          departures.add (i_waiters.next ());
          if (remainingPassages != Integer.MAX_VALUE)
            remainingPassages--;
        }
        // Departures.
        queueState.doExits (time, null, null, departures, null, visitLogsSet);
      }
      queueStateHandler.setGatePassageCredits (time, remainingPassages);      
    }
    else
      throw new WorkloadScheduleInvalidException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final GATE queue,
   final SimQueueState<SimJob, GATE> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, GATE>> visitLogsSet)
   throws SimQueuePredictionException
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (queueEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    throw new IllegalStateException ();
  }  
  
  @Override
  public void updateToTime (final GATE queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    queueState.setTime (newTime);
  }

}