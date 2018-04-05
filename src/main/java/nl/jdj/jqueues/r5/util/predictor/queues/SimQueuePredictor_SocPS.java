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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.processorsharing.SocPS;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link SocPS}.
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
public class SimQueuePredictor_SocPS
extends AbstractSimQueuePredictor<SocPS>
{

  @Override
  public String toString ()
  {
    return "Predictor[SocPS]";
  }

  @Override
  public boolean isStartArmed (final SocPS queue, final SimQueueState<SimJob, SocPS> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return true;
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final SocPS queue,
   final SimQueueState<SimJob, SocPS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    final int numberOfJobsExecuting = queueState.getJobRemainingServiceTimeMap ().size ();
    if (numberOfJobsExecuting == 0)
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    double totalWork = 0;
    for (final double work_j: queueState.getJobRemainingServiceTimeMap ().values ())
      totalWork += work_j;
    if (totalWork < 0)
      throw new RuntimeException ();
    if (Double.isFinite (totalWork))
    {
      queueEventTypes.add (SimQueueSimpleEventType.DEPARTURE);
      return time + totalWork;
    }
    else
      return Double.NaN;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final SocPS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, SocPS> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, SocPS>> visitLogsSet)
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
      queueState.doArrivals (time, arrivals, visitLogsSet);
      if ((! queueState.isQueueAccessVacation ()) && queueState.getServerAccessCredits () >= 1)
        queueState.doStarts (time, arrivals);
    }
    else if (eventType == SimQueueSimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final boolean interruptService =
          workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).get (job);
        // Make sure we do not revoke an executing job without the interruptService flag.
        if (interruptService || ! queueState.getJobsInServiceArea ().contains (job))
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      int sac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, sac);
      while (queueState.getJobsInWaitingArea ().size () > 0 && sac > 0)
      {
        final Set<SimJob> starters = new LinkedHashSet<> ();
        final SimJob jobToStart = queueState.getJobsInWaitingAreaOrdered ().iterator ().next ();
        starters.add (jobToStart);
        queueState.doStarts (time, starters);
        if (sac < Integer.MAX_VALUE)
          sac--;
        // Check whether job did not already leave!
        if (queueState.getJobs ().contains (jobToStart))
        {
          final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (jobToStart);
          if (remainingServiceTime == 0.0)
            queueState.doExits (time, null, null, starters, null, visitLogsSet);
        }
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final SocPS queue,
   final SimQueueState<SimJob, SocPS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, SocPS>> visitLogsSet)
   throws SimQueuePredictionException    
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (queueEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType == SimQueueSimpleEventType.DEPARTURE)
    {
      final Set<SimJob> departures = new HashSet<> (queueState.getRemainingServiceMap ().firstEntry ().getValue ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }  
  
  @Override
  public void updateToTime (final SocPS queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    final double oldTime = queueState.getTime ();
    if (! Double.isNaN (oldTime))
    {
      final double dT = newTime - oldTime;
      if (dT < 0)
        throw new RuntimeException ();
      final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
      final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
      if (dT > 0 && ! rsTimeMap.isEmpty ())
      { 
        rsMap.clear ();
        double totalWork = 0;
        for (final double work_j: ((Map<SimJob, Double>) queueState.getJobRemainingServiceTimeMap ()).values ())
          totalWork += work_j;
        if (totalWork > 0)
          for (final SimJob job : new HashSet<> (rsTimeMap.keySet ()))
          {
            final double dS = dT * rsTimeMap.get (job) / totalWork;
            final double newRs = Math.max (rsTimeMap.get (job) - dS, 0);
            rsTimeMap.put (job, newRs);
            if (! rsMap.containsKey (newRs))
              rsMap.put (newRs, new ArrayList<> ());
            rsMap.get (newRs).add (job);
          }
      }
    }
    queueState.setTime (newTime);
  }

}