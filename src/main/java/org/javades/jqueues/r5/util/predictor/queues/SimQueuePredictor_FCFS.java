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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link FCFS}.
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
public class SimQueuePredictor_FCFS
extends AbstractSimQueuePredictor<SimQueue>
{
  
  final boolean hasB; // Misleading; if false -> queue has unlimited waiting area.
  
  final int B;
  
  final boolean hasc;
  
  final int c;
  
  final boolean useLifo;
  
  protected SimQueuePredictor_FCFS (final boolean hasB, final int B, final boolean hasc, final int c, final boolean useLifo)
  {
    if (hasB && B < 0)
      throw new IllegalArgumentException ();
    if (hasc && c < 0)
      throw new IllegalArgumentException ();
    this.hasB = hasB;
    this.B = B;
    this.hasc = hasc;
    this.c = c;
    this.useLifo = useLifo;
  }
  
  protected SimQueuePredictor_FCFS (final boolean hasB, final int B, final boolean hasc, final int c)
  {
    this (hasB, B, hasc, c, false);
  }
  
  public SimQueuePredictor_FCFS ()
  {
    this (false, 0, true, 1);
  }

  @Override
  public String toString ()
  {
    return "Predictor[FCFS]";
  }

  @Override
  public boolean isStartArmed (final SimQueue queue, final SimQueueState<SimJob, SimQueue> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (this.hasc)
      return queueState.getJobsInServiceArea ().size () < this.c;
    else
      return true;
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    final int numberOfJobsInServiceArea = queueState.getJobRemainingServiceTimeMap ().size ();
    if (numberOfJobsInServiceArea == 0)
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    if (this.hasc && numberOfJobsInServiceArea > this.c)
      throw new IllegalStateException ();
    if (queueState.getStartTimesMap ().isEmpty ())
      throw new IllegalStateException ();
    if (this.hasc && queueState.getStartTimesMap ().size () > this.c)
      throw new IllegalStateException ();
    double minDepartureTime = Double.NaN;
    for (final Entry<SimJob, Double> jobInServiceAreaEntry : queueState.getStartTimesMap ().entrySet ())
    {
      final SimJob jobInServiceArea = jobInServiceAreaEntry.getKey ();
      final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (jobInServiceArea);
      if (Double.isFinite (remainingServiceTime))
      {
        final double departureTime = time + remainingServiceTime;
        if (departureTime < time)
          throw new RuntimeException ();
        if (Double.isNaN (minDepartureTime) || departureTime < minDepartureTime)
          minDepartureTime = departureTime;
      }
    }
    if (! Double.isNaN (minDepartureTime))
      queueEventTypes.add (SimQueueSimpleEventType.DEPARTURE);
    return minDepartureTime;
  }

  protected SimJob getJobToStart
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState)
   throws SimQueuePredictionException
  {
    if (this.useLifo)
    {
      final ArrayList<SimJob> waitingJobs = new ArrayList<> (queueState.getJobsInWaitingAreaOrdered ());
      return waitingJobs.get (waitingJobs.size () - 1);  
    }
    else
      return queueState.getJobsInWaitingAreaOrdered ().iterator ().next ();
  }
  
  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final SimQueue queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, SimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, SimQueue>> visitLogsSet)
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
      if (queueState.isQueueAccessVacation ())
        queueState.doArrivals (time, arrivals, visitLogsSet);
      else
      {
        if (this.hasB && queueState.getJobsInWaitingArea ().size () > this.B)
          throw new IllegalStateException ();
        if (this.hasB && queueState.getJobsInWaitingArea ().size () == this.B
          && ! ((queueState.getServerAccessCredits () > 0)
                && ((! this.hasc) || queueState.getJobsInServiceArea ().size () < this.c)))
        {
          if (this.useLifo && ! queueState.getJobsInWaitingArea ().isEmpty ())
          {
            // Drops the first arrival in the waiting area, and inserts the arriving job in the queue state.
            queueState.doArrivals (time, arrivals, visitLogsSet);
            final SimJob jobToDrop = queueState.getJobsInWaitingAreaOrdered ().iterator ().next ();
            queueState.doExits (time, Collections.singleton (jobToDrop), null, null, null, visitLogsSet);
          }
          else
            // Drop the arriving job.
            queueState.doExits (time, arrivals, null, null, null, visitLogsSet);
        }
        else
        {
          queueState.doArrivals (time, arrivals, visitLogsSet);
          if (((! this.hasc) || queueState.getJobsInServiceArea ().size () < this.c)
            && queueState.getServerAccessCredits () >= 1)
          {
            queueState.doStarts (time, arrivals);
            // Check whether job did not already leave!
            if (queueState.getJobs ().contains (job))
            {
              final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (job);
              if (remainingServiceTime == 0)
                queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
            }
          }
        }
      }
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
        final boolean isJobInServiceArea = queueState.getJobsInServiceArea ().contains (job);
        // Make sure we do not revoke a job in the service area without the interruptService flag.
        if (interruptService || ! isJobInServiceArea)
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
          if (isJobInServiceArea)
          {
            final int sac = queueState.getServerAccessCredits ();
            if (sac > 0 && queueState.getJobsInWaitingArea ().size () > 0)
            {
              final Set<SimJob> starters = new LinkedHashSet<> ();
              starters.add (getJobToStart (queue, queueState));
              queueState.doStarts (time, starters);
            }            
          }
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      int sac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, sac);
      while (queueState.getJobsInWaitingArea ().size () > 0
             && sac > 0
             && ((! this.hasc) || queueState.getJobsInServiceArea ().size () < this.c))
      {
        final Set<SimJob> starters = new LinkedHashSet<> ();
        final SimJob jobToStart = getJobToStart (queue, queueState);
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
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, SimQueue>> visitLogsSet)
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
      if (queueState.getJobsInServiceArea ().size () < 1)
        throw new IllegalStateException ();
      if (this.hasc && queueState.getJobsInServiceArea ().size () > this.c)
        throw new IllegalStateException ();
      final Set<SimJob> departures = new HashSet<> (queueState.getRemainingServiceMap ().firstEntry ().getValue ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      final int sac = queueState.getServerAccessCredits ();
      if (sac > 0 && queueState.getJobsInWaitingArea ().size () > 0)
      {
        final Set<SimJob> starters = new LinkedHashSet<> ();
        starters.add (getJobToStart (queue, queueState));
        queueState.doStarts (time, starters);
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }
  
  @Override
  public void updateToTime (final SimQueue queue, final SimQueueState queueState, final double newTime)
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
        final double dS = dT;
        for (final SimJob job : new HashSet<> (rsTimeMap.keySet ()))
        {
          final double newRs = rsTimeMap.get (job) - dS;
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