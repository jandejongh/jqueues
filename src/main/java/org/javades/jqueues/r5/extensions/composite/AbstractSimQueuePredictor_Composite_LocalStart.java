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
package org.javades.jqueues.r5.extensions.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.job.DefaultSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_Pattern;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link AbstractSimQueueComposite_LocalStart} and derived queues.
 *
 * @param <Q> The type of queue supported.
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
public abstract class AbstractSimQueuePredictor_Composite_LocalStart<Q extends AbstractSimQueueComposite_LocalStart>
extends AbstractSimQueuePredictor_Composite<Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public AbstractSimQueuePredictor_Composite_LocalStart (final List<AbstractSimQueuePredictor> subQueuePredictors)
  {
    super (subQueuePredictors);
    if (subQueuePredictors == null || subQueuePredictors.size () < 1)
      throw new IllegalArgumentException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE PREDICTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Registers a new {@link SimQueueCompositeStateHandler} at the object created by super method,
   *  creating appropriate empty {@link DefaultSimQueueState}s for the sub-queues.
   * 
   */
  @Override
  public SimQueueState<SimJob, Q> createQueueState (final Q queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    final List<SimQueue> subQueues = new ArrayList (queue.getQueues ());
    if (subQueues.size () != this.subQueuePredictors.size ())
      throw new IllegalArgumentException ();
    final Set<DefaultSimQueueState> subQueueStates = new LinkedHashSet<> ();
    for (int i = 0; i < subQueues.size (); i++)
    {
      final SimQueue subQueue = subQueues.get (i);
      final AbstractSimQueuePredictor subQueuePredictor = this.subQueuePredictors.get (i);
      final DefaultSimQueueState subQueueState = (DefaultSimQueueState) subQueuePredictor.createQueueState (subQueue, isROEL);
      subQueueStates.add (subQueueState);
    }
    queueState.registerHandler (new SimQueueCompositeStateHandler (queue.getQueues (), subQueueStates));
    return queueState;
  }

  @Override
  public void updateToTime (final Q queue, final SimQueueState queueState, final double newTime)
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final List<SimQueue> subQueues = new ArrayList<> (queue.getQueues ());
    for (int i = 0; i < this.subQueuePredictors.size (); i++)
    {
      final SimQueue subQueue = subQueues.get (i);
      final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (i);
      this.subQueuePredictors.get (i).updateToTime (subQueue, subQueueState, newTime);
    }
    queueState.setTime (newTime);
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final Q queue, final SimQueueState<SimJob, Q> queueState, final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  throws SimQueuePredictionException
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final List<SimQueue> subQueues = new ArrayList<> (queue.getQueues ());
    double minNextEventTime = Double.NaN;
    Set<SimQueue> subQueuesMinNextEventTime = new HashSet<> ();
    SimEntitySimpleEventType.Member nextEvent = null;
    for (int i = 0; i < this.subQueuePredictors.size (); i++)
    {
      final SimQueue subQueue = subQueues.get (i);
      final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (i);
      final Set<SimEntitySimpleEventType.Member> subQueueEventTypes = new LinkedHashSet<> ();
      final double subQueueNextEventTime =
        this.subQueuePredictors.get (i).getNextQueueEventTimeBeyond (subQueue, subQueueState, subQueueEventTypes);
      if ((! Double.isNaN (subQueueNextEventTime))
       && (Double.isNaN (minNextEventTime) || subQueueNextEventTime <= minNextEventTime))
      {
        if (subQueueEventTypes.size () != 1)
          throw new SimQueuePredictionAmbiguityException ();
        if ((! Double.isNaN (minNextEventTime)) && subQueueNextEventTime < minNextEventTime)
          subQueuesMinNextEventTime.clear ();
        subQueuesMinNextEventTime.add (subQueue);
        nextEvent = subQueueEventTypes.iterator ().next ();
        minNextEventTime = subQueueNextEventTime;
      }
    }
    if (subQueuesMinNextEventTime.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    else if (subQueuesMinNextEventTime.size () == 1)
      queueEventTypes.add (new SubQueueSimpleEvent (subQueuesMinNextEventTime.iterator ().next (), null, nextEvent, null, null));
    return minNextEventTime;
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
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
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
      queueState.doArrivals (time, arrivals, visitLogsSet); // Takes care of qav.
      if (queueState.getJobs ().contains (job))
        if (queueState.getServerAccessCredits () >= 1)
          startJobs (time, queue, queueState, arrivals, visitLogsSet);
    }
    else if (eventType == SimQueueSimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present and eligible for revocation.
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
          revokeJobs (time, queue, queueState, revocations, visitLogsSet);
          for (int i = 0; i < queue.getQueues ().size (); i++)
          {
            final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (i);
            if (subQueueState.getJobs ().contains (job))
            {
              final SimQueue subQueue = new ArrayList<SimQueue> (queue.getQueues ()).get (i);
              final SubQueueSimpleEvent subQueueEvent =
                new SubQueueSimpleEvent (subQueue, SimQueueSimpleEventType.REVOCATION, null, job, null);
              doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (subQueueEvent)), visitLogsSet);
              break;
            }
          }
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
        startJobs (time, queue, queueState, starters, visitLogsSet);
      }
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
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType instanceof SubQueueSimpleEvent)
    {
      final List<SimQueue> subQueues = new ArrayList<> (queue.getQueues ());
      final SimQueue subQueue = ((SubQueueSimpleEvent) eventType).subQueue;
      final int subQueueIndex = subQueues.indexOf (subQueue);
      final DefaultSimQueueState<SimJob, SimQueue> subQueueState = queueStateHandler.getSubQueueState (subQueueIndex);
      final AbstractSimQueuePredictor subQueuePredictor = this.subQueuePredictors.get (subQueueIndex);
      final SimEntitySimpleEventType.Member subQueueWorkloadEvent = ((SubQueueSimpleEvent) eventType).subQueueWorkloadEvent;
      final SimEntitySimpleEventType.Member subQueueQueueEvent = ((SubQueueSimpleEvent) eventType).subQueueQueueEvent;
      final SimJob job = ((SubQueueSimpleEvent) eventType).job;
      final Object argument = ((SubQueueSimpleEvent) eventType).argument;
      // Apply the event at the sub-queue, and capture its visit logs generated.
      final Set<JobQueueVisitLog<SimJob,Q>> subQueueVisitLogsSet = new HashSet<> ();
      try
      {
        if (subQueueWorkloadEvent != null)
        {
          final SimJQEvent subQueueEvent;
          if (subQueueWorkloadEvent == SimQueueSimpleEventType.ARRIVAL)
            subQueueEvent = new SimJQEvent.Arrival<> (job, subQueue, time);
          else if (subQueueWorkloadEvent == SimQueueSimpleEventType.REVOCATION)
            subQueueEvent = new SimJQEvent.Revocation<> (job, subQueue, time, true);
          else if (subQueueWorkloadEvent == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
            subQueueEvent = new SimQueueEvent.ServerAccessCredits<> (subQueue, time, (int) argument);
          else
            throw new RuntimeException ();
          final WorkloadSchedule_SQ_SV_ROEL_U subQueueWorkloadSchedule =
            subQueuePredictor.createWorkloadSchedule_SQ_SV_ROEL_U (subQueue, new HashSet<> (Collections.singleton (subQueueEvent)));
          subQueuePredictor.doWorkloadEvents_SQ_SV_ROEL_U
            (subQueue,
            subQueueWorkloadSchedule,
            subQueueState,
            new HashSet<> (Collections.singleton (subQueueWorkloadEvent)),
            subQueueVisitLogsSet);
        }
        else
          subQueuePredictor.doQueueEvents_SQ_SV_ROEL_U
            (subQueue, subQueueState, new HashSet<> (Collections.singleton (subQueueQueueEvent)), subQueueVisitLogsSet);
      }
      catch (WorkloadScheduleException e)
      {
        throw new RuntimeException (e);
      }
      checkSubQueueVisitLogsSet
        (time,
         queue, queueState, visitLogsSet,
         subQueue, subQueueIndex, subQueueState, subQueueVisitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE STATE SUPPORT METHODS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  protected void dropJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> drops,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  {
    queueState.doExits (time, drops, null, null, null, visitLogsSet);
  }
  
  protected void revokeJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> revokers,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  {
    queueState.doExits (time, null, revokers, null, null, visitLogsSet);
  }
  
  protected void startJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> starters,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException
  {
    queueState.doStarts (time, starters);
    final SimQueue headQueue = (SimQueue) queue.getQueues ().iterator ().next ();
    for (final SimJob job : starters)
    {
      if (! (job instanceof DefaultSimJob))
        throw new UnsupportedOperationException ();
      // Check whether job did not already leave!
      if (queueState.getJobs ().contains (job))
      {
        ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (headQueue, job.getServiceTime (queue));
        final SubQueueSimpleEvent headQueueEvent =
          new SubQueueSimpleEvent (headQueue, SimQueueSimpleEventType.ARRIVAL, null, job, null);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (headQueueEvent)), visitLogsSet);
      }
    }
  }
  
  protected void departJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> departers,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException
  {
    queueState.doExits (time, null, null, departers, null, visitLogsSet);
  }
  
  protected void checkSubQueueVisitLogsSet
  (
    final double time,
    final Q queue,
    final SimQueueState<SimJob, Q> queueState,
    final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet,
    final SimQueue subQueue,
    final int subQueueIndex,
    final DefaultSimQueueState<SimJob, SimQueue> subQueueState,
    final Set<JobQueueVisitLog<SimJob, Q>> subQueueVisitLogsSet
  )
    throws SimQueuePredictionException
  {
    final List<SimQueue> subQueues = new ArrayList<> (queue.getQueues ());
    // Check the visit logs for drops and departures.
    for (JobQueueVisitLog<SimJob,Q> jvl : subQueueVisitLogsSet)
    {
      if (jvl.dropped)
        dropJobs (time, queue, queueState, Collections.singleton (jvl.job), visitLogsSet);
      else if (jvl.departed)
      {
        // XXX
        if (subQueueIndex == subQueues.size () - 1 || (this instanceof SimQueuePredictor_Pattern))
          departJobs (time, queue, queueState, Collections.singleton (jvl.job), visitLogsSet);
        else
        {
          // Apply the arrival at the next queue through recursion.
          final int nextSubQueueIndex = subQueueIndex + 1;
          final SimQueue nextSubQueue = subQueues.get (nextSubQueueIndex);
          final SubQueueSimpleEvent nextSubQueueEvent = new SubQueueSimpleEvent
            (nextSubQueue, SimQueueSimpleEventType.ARRIVAL, null, jvl.job, null);
          if (! (jvl.job instanceof DefaultSimJob))
            throw new UnsupportedOperationException ();
          ((DefaultSimJob) jvl.job).setRequestedServiceTimeMappingForQueue (nextSubQueue, jvl.job.getServiceTime (queue));
          doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (nextSubQueueEvent)), visitLogsSet);
        }
      }
      else if (jvl.revoked)
      {
        /* EMPTY */
      }
      else
        // Job has left due to some other unknown reason? Should specify here!
        throw new UnsupportedOperationException ();
    }
  
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}