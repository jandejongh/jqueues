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
package org.javades.jqueues.r5.extensions.qos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.qos.PQ;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_Preemptive;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link PQ}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
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
public class SimQueuePredictor_PQ<J extends SimJob, Q extends PQ, P extends Comparable>
extends SimQueuePredictor_Preemptive<Q>
{

  @Override
  public String toString ()
  {
    return "Predictor[PQ[?]]";
  }

  @Override
  public boolean isStartArmed (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return queueState.getJobsInServiceArea ().isEmpty ();
  }

  /** Registers a new {@link SimQueueQoSStateHandler} at the object created by super method.
   *
   * @return The object created by the super method with a new registered {@link SimQueueQoSStateHandler}.
   * 
   */
  @Override
  public SimQueueState<SimJob, Q> createQueueState (final Q queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueQoSStateHandler<> (true));
    return queueState;
  }

  protected J getJobToExecute (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (queueState.getJobs ().isEmpty ()
      || (queueState.getServerAccessCredits () == 0 && queueState.getJobsInServiceArea ().isEmpty ()))
      return null;
    else
    {
      final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
        (SimQueueQoSStateHandler)
          ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
      queueStateHandler.updateJobsQoSMap ();
      for (final Set<J> jobsP : queueStateHandler.getJobsQoSMap ().values ())
        if (jobsP == null || jobsP.isEmpty ())
          throw new IllegalStateException ();
        else
          for (final J job : jobsP)
            if (queueState.getJobsInServiceArea ().contains (job) || queueState.getServerAccessCredits () > 0)
              return job;
      throw new IllegalStateException ();
    }
  }
  
  protected J getJobExecuting (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (queueState.getJobsInServiceArea ().isEmpty ())
      return null;
    else
    {
      final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
        (SimQueueQoSStateHandler)
          ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
      queueStateHandler.updateJobsQoSMap ();
      for (final Set<J> jobsP : queueStateHandler.getJobsQoSMap ().values ())
        if (jobsP == null || jobsP.isEmpty ())
          throw new IllegalStateException ();
        else
          for (final J job : jobsP)
            if (queueState.getJobsInServiceArea ().contains (job))
              return job;
      throw new IllegalStateException ();
    }
  }
  
  protected void reschedule
  (final Q queue,
    final SimQueueState<SimJob, Q> queueState,
    final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    J jobToExecute = getJobToExecute (queue, queueState);
    J jobExecuting = getJobExecuting (queue, queueState);
    while (jobExecuting != jobToExecute)
    {
      if (jobExecuting != null)
        preemptJob (queue, queueState, jobExecuting, visitLogsSet);
      if (jobToExecute != null)
      {
        if (! queueState.getJobsInServiceArea ().contains (jobToExecute))
        {
          final Set<SimJob> starters = new HashSet<> ();
          starters.add (jobToExecute);
          queueState.doStarts (time, starters);
          // Check whether job did not already leave!
          if (queueState.getJobs ().contains (jobToExecute))
          {
            final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (jobToExecute);
            if (remainingServiceTime == 0)
              queueState.doExits (time, null, null, starters, null, visitLogsSet);
          }
        }
      }
      jobToExecute = getJobToExecute (queue, queueState);
      jobExecuting = getJobExecuting (queue, queueState);  
    }
  }
  
  @Override
  public double getNextQueueEventTimeBeyond
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    if (queueState.getRemainingServiceMap ().isEmpty ())
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimJob executingJob = getJobExecuting (queue, queueState);
    final double rs = queueState.getJobRemainingServiceTimeMap ().get (executingJob);
    if (rs < 0)
      throw new RuntimeException ();
    if (Double.isFinite (rs))
    {
      queueEventTypes.add (SimQueueSimpleEventType.DEPARTURE);
      return time + rs;
    }
    else
      return Double.NaN;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final Q queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
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
      // Takes care of dropping jobs during queue-acces vacations!
      queueState.doArrivals (time, arrivals, visitLogsSet);
      if ((! queueState.isQueueAccessVacation ()) && queueState.getServerAccessCredits () >= 1)
        reschedule (queue, queueState, visitLogsSet);
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
          reschedule (queue, queueState, visitLogsSet);
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
      if (oldSac == 0 && newSac > 0 && ! queueState.getJobsInWaitingArea ().isEmpty ())
        reschedule (queue, queueState, visitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
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
      final Set<SimJob> departures = new HashSet<> ();
      final SimJob departingJob = getJobExecuting (queue, queueState);
      departures.add (departingJob);
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      reschedule (queue, queueState, visitLogsSet);
    }
    else
      throw new RuntimeException ();
    queueEventTypes.remove (eventType);
  }  
  
  @Override
  public void updateToTime (final Q queue, final SimQueueState queueState, final double newTime)
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
        final SimJob executingJob = getJobExecuting (queue, queueState);
        final double dS = dT;
        final double oldRs = rsTimeMap.get (executingJob);
        final double newRs =  oldRs - dS;
        rsTimeMap.put (executingJob, newRs);
        if (! rsMap.containsKey (oldRs))
          throw new IllegalStateException ();
        if (rsMap.get (oldRs).size () == 1)
        {
          rsMap.put (newRs, rsMap.get (oldRs));
          rsMap.remove (oldRs);
        }
        else
        {
          rsMap.get (oldRs).remove (0);
          rsMap.put (newRs, new ArrayList<> ());
          rsMap.get (newRs).add (executingJob);
        }
      }
    }
    queueState.setTime (newTime);
  }

}