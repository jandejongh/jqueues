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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.processorsharing.CUPS;
import org.javades.jqueues.r5.entity.jq.queue.processorsharing.SimQueueCatchUpSimpleEventType;
import org.javades.jqueues.r5.extensions.ost.SimQueueOSTStateHandler;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link CUPS}.
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
public class SimQueuePredictor_CUPS
extends AbstractSimQueuePredictor<CUPS>
{

  @Override
  public String toString ()
  {
    return "Predictor[CUPS]";
  }

  /** Registers a new {@link SimQueueOSTStateHandler} at the object created by super method.
   *
   * @return The object created by the super method with a new registered {@link SimQueueOSTStateHandler}.
   * 
   */
  @Override
  public SimQueueState<SimJob, CUPS> createQueueState (final CUPS queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueOSTStateHandler ());
    return queueState;
  }

  @Override
  public boolean isStartArmed (final CUPS queue, final SimQueueState<SimJob, CUPS> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return true;
  }

  private Map<SimJob, Double> getFirstDeparter
  (final SimQueueState<SimJob, CUPS> queueState,
   final SimQueueOSTStateHandler queueStateHandler)
  {
    for (final Entry<Double, List<SimJob>> entry : queueState.getRemainingServiceMap ().entrySet ())
      for (final SimJob job : queueStateHandler.getJobsWithMinimumObtainedServiceTime ())
        if (entry.getValue ().contains (job))
        {
          final Map<SimJob, Double> map = new HashMap<> ();
          map.put (job, entry.getKey ());
          return map;
        }
    throw new IllegalStateException ();
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final CUPS queue, final SimQueueState<SimJob, CUPS> queueState, final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    final SimQueueOSTStateHandler queueStateHandler =
      (SimQueueOSTStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueOSTHandler");
    if (queueStateHandler.isEmpty ())
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final double minOst = queueStateHandler.getMinimumObtainedServiceTime ();
    final Set<SimJob> jobsMinOst = queueStateHandler.getJobsWithMinimumObtainedServiceTime ();
    final Map<SimJob, Double> firstDeparter = getFirstDeparter (queueState, queueStateHandler);
    double dT = firstDeparter.entrySet ().iterator ().next ().getValue () * jobsMinOst.size ();
    SimEntitySimpleEventType.Member eventType = SimQueueSimpleEventType.DEPARTURE;
    if (queueStateHandler.getNumberOfOstGroups () > 1)
    {
      final double minOstNext = queueStateHandler.getNextHigherThanMinimumObtainedServiceTime ();
      final double dT_cup =  (minOstNext - minOst) * jobsMinOst.size ();
      if (dT_cup < dT)
      {
        dT = dT_cup;
        eventType = SimQueueCatchUpSimpleEventType.CATCH_UP;
      }
    }
    if (Double.isFinite (dT))
    {
      queueEventTypes.add (eventType);
      return time + dT;
    }
    else
      return Double.NaN;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final CUPS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, CUPS> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, CUPS>> visitLogsSet)
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
    final SimQueueOSTStateHandler queueStateHandler =
      (SimQueueOSTStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueOSTHandler");
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
      {
        queueState.doStarts (time, arrivals);
        // Check whether job did not already leave!
        if (queueState.getJobs ().contains (job))
          queueStateHandler.addStartingJob (job);
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
        // Make sure we do not revoke an executing job without the interruptService flag.
        final boolean hasStarted = queueState.getJobsInServiceArea ().contains (job);
        if (interruptService || ! hasStarted)
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
          // Note: since jobs can be revoked from the waiting area (because they are awaiting server-access credits),
          // and jobs are only inserted into the queue-state handler when they START,
          // the job is only present in the handler if it is in the service area of the queue!
          // So prepare 'removeJob' properly for the potential absence of the job.
          queueStateHandler.removeJob (job, hasStarted);
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
      if (oldSac == 0 && newSac > 0)
      {
        final Set<SimJob> starters = new LinkedHashSet<> ();
        final Iterator<SimJob> i_waiters = queueState.getJobsInWaitingAreaOrdered ().iterator ();
        int remainingSac = newSac;
        while ((remainingSac == Integer.MAX_VALUE || remainingSac > 0) && i_waiters.hasNext ())
        {
          starters.add (i_waiters.next ());
          if (remainingSac != Integer.MAX_VALUE)
            remainingSac--;
        }
        queueState.doStarts (time, starters);
        // Check whether job did not already leave!
        final Iterator<SimJob> i_starters = starters.iterator ();
        while (i_starters.hasNext ())
          if (! queueState.getJobs ().contains (i_starters.next ()))
            i_starters.remove ();
        if (! starters.isEmpty ())
          queueStateHandler.addStartingJobs (starters);
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final CUPS queue,
   final SimQueueState<SimJob, CUPS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, CUPS>> visitLogsSet)
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
    final SimQueueOSTStateHandler queueStateHandler =
      (SimQueueOSTStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueOSTHandler");
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType == SimQueueCatchUpSimpleEventType.CATCH_UP)
    {
      /* NOTHING TO DO */
    }
    else if (eventType == SimQueueSimpleEventType.DEPARTURE)
    {
      final Set<SimJob> departures =
        Collections.singleton (getFirstDeparter (queueState, queueStateHandler).entrySet ().iterator ().next ().getKey ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      queueStateHandler.removeJob (departures.iterator ().next (), true);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }
  
  @Override
  public void updateToTime (final CUPS queue, final SimQueueState queueState, final double newTime)
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
      final SimQueueOSTStateHandler queueStateHandler =
        (SimQueueOSTStateHandler)
          ((DefaultSimQueueState) queueState).getHandler ("SimQueueOSTHandler");
      if (! queueStateHandler.isEmpty ())
      {
        // Update obtained service time.
        final Set<SimJob> jobsMinOst = queueStateHandler.getJobsWithMinimumObtainedServiceTime ();
        final double dS = dT / jobsMinOst.size ();
        queueStateHandler.increaseMinimumObtainedServiceTime (newTime, dS, false, CUPS.TOLERANCE_OST);
        // Update remaining service time.
        final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
        final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
        for (final SimJob job : jobsMinOst)
        {
          // Calculate new remaining service time; should not (with respect to tolerance) be (strictly) negative.
          final double oldRst = rsTimeMap.get (job);
          final double newRstCalculated =  oldRst - dS;
          if (newRstCalculated < - CUPS.TOLERANCE_OST)
            throw new IllegalStateException ("calculated remaining service time is (too) negative on job "
            + job + ": " + newRstCalculated + ".");
          final double newRst = Math.max (newRstCalculated, 0.0);
          // Update rst on rsTimeMap.
          rsTimeMap.put (job, newRst);
          // Update rst on rsMap.
          rsMap.get (oldRst).remove (job);
          if (rsMap.get (oldRst).isEmpty ())
            rsMap.remove (oldRst);
          if (! rsMap.containsKey (newRst))
            rsMap.put (newRst, new ArrayList<> ());
          rsMap.get (newRst).add (job);
        }
      }
    }
    queueState.setTime (newTime);
  }

}