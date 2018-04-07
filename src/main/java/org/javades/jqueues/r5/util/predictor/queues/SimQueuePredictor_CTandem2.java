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
import org.javades.jqueues.r5.entity.jq.queue.composite.ctandem2.CTandem2;
import org.javades.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite;
import org.javades.jqueues.r5.extensions.composite.SimQueueCompositeStateHandler;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link CTandem2}.
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
public class SimQueuePredictor_CTandem2<Q extends CTandem2>
extends AbstractSimQueuePredictor_Composite<Q>
{

  private static List<AbstractSimQueuePredictor> asList (final AbstractSimQueuePredictor p1, final AbstractSimQueuePredictor p2)
  {
    if (p1 == null || p2 == null)
      throw new IllegalArgumentException ();
    final List<AbstractSimQueuePredictor> list = new ArrayList<> ();
    list.add (p1);
    list.add (p2);
    return list;
  }
  
  public SimQueuePredictor_CTandem2
  (final AbstractSimQueuePredictor waitQueuePredictor, final AbstractSimQueuePredictor serveQueuePredictor)
  {
    super (asList (waitQueuePredictor, serveQueuePredictor));
  }

  @Override
  public String toString ()
  {
    return "Predictor[CTandem2[?]]";
  }

  private SimQueue getWaitQueue (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Iterator<? extends SimQueue> i_queues = queue.getQueues ().iterator ();
    return i_queues.next ();
  }
  
  private SimQueue getServeQueue (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Iterator<? extends SimQueue> i_queues = queue.getQueues ().iterator ();
    i_queues.next ();
    return i_queues.next ();
  }

  private SimQueuePredictor getWaitQueuePredictor ()
  {
    return this.subQueuePredictors.get (0);
  }
  
  private SimQueuePredictor getServeQueuePredictor ()
  {
    return this.subQueuePredictors.get (1);
  }
  
  private DefaultSimQueueState getWaitQueueState (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    return queueStateHandler.getSubQueueState (0);
  }
  
  private DefaultSimQueueState getServeQueueState (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    return queueStateHandler.getSubQueueState (1);
  }
  
  @Override
  public boolean isStartArmed (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return getServeQueuePredictor ().isStartArmed (getServeQueue (queue), getServeQueueState (queue, queueState));
  }

  private class SimQueueAndSimQueueState
  {
    public final SimQueue queue;
    public final DefaultSimQueueState queueState;
    public SimQueueAndSimQueueState (final SimQueue queue, final DefaultSimQueueState simQueueState)
    {
      this.queue = queue;
      this.queueState = simQueueState;
    }
  }
  
  @Override
  public SimQueueState<SimJob, Q> createQueueState (Q queue, boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    final List<SimQueue> subQueues = new ArrayList (queue.getQueues ());
    if (subQueues.size () != 2)
      throw new RuntimeException ();
    final DefaultSimQueueState waitQueueState =
      (DefaultSimQueueState) getWaitQueuePredictor ().createQueueState (getWaitQueue (queue), isROEL);
    final DefaultSimQueueState serveQueueState =
      (DefaultSimQueueState) getServeQueuePredictor ().createQueueState (getServeQueue (queue), isROEL);
    final Set<DefaultSimQueueState> subQueueStates = new LinkedHashSet<> ();
    subQueueStates.add (waitQueueState);
    subQueueStates.add (serveQueueState);
    queueState.registerHandler (new SimQueueCompositeStateHandler (queue.getQueues (), subQueueStates));
    ((DefaultSimQueueState) getWaitQueueState (queue, queueState)).registerPostStartHook
      (this::waitQueuePostStartHook, new SimQueueAndSimQueueState (queue, queueState));
    return queueState;
  }

  private Set<JobQueueVisitLog<SimJob, Q>> cachedVisitLogsSet;

  @Override
  public double getNextQueueEventTimeBeyond
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
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
  public void updateToTime
  (final Q queue,
   final SimQueueState queueState,
   final double newTime)
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
    this.cachedVisitLogsSet = visitLogsSet;
    if (workloadEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    //
    // Set the initial SAC on the waitQueue (only if we have no jobs, which is a prerequisite for a RESET), and only if needed.
    //
    if (queueState.getJobs ().isEmpty ())
    {
      final boolean needSacOnWaitQueue =
        (getServeQueuePredictor ().isStartArmed (getServeQueue (queue), getServeQueueState (queue, queueState)) 
         && queueState.getServerAccessCredits () > 0);
      final boolean sacOnWaitQueue =
        getWaitQueuePredictor ().hasServerAccessCredits (getServeQueue (queue), getWaitQueueState (queue, queueState));
      if (needSacOnWaitQueue != sacOnWaitQueue)
      {
        // System.err.println ("SimQueuePredictor_CTandem2; t=" + time
        //  + ": Setting (initial) sac on " + getWaitQueue (queue) + " to " + (needSacOnWaitQueue ? 1 : 0) + ".");
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent
            (getWaitQueue (queue), SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, needSacOnWaitQueue ? 1 : 0);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (waitQueueSacEvent)), visitLogsSet);
      }
    }
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
      {
        // Let the job arrive immediately and unconditionally arrive at the waitQueue.
        if (! (job instanceof DefaultSimJob))
          throw new UnsupportedOperationException ();
        final SimQueue waitQueue = getWaitQueue (queue);
        ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (waitQueue, job.getServiceTime (queue));
        final SubQueueSimpleEvent waitQueueEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.ARRIVAL, null, job, null);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (waitQueueEvent)), visitLogsSet);
      }
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
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
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
      int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      // System.err.println ("SimQueuePredictor_CTandem2; t=" + time + ": Setting sac on " + queue + " to " + newSac + ".");
      queueState.setServerAccessCredits (time, newSac);
      final SimQueue waitQueue = getWaitQueue (queue);
      if (oldSac == 0
          && newSac > 0
          && getServeQueuePredictor ().isStartArmed (getServeQueue (queue), getServeQueueState (queue, queueState)))
      {
        // System.err.println ("SimQueuePredictor_CTandem2; t=" + time + ": Setting sac on " + waitQueue + " to " + 1 + ".");
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 1);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (waitQueueSacEvent)), visitLogsSet);
        newSac = queueState.getServerAccessCredits ();
      }
      else if (oldSac > 0 && newSac == 0)
      {
        // System.err.println ("SimQueuePredictor_CTandem2; t=" + time + ": Setting sac on " + waitQueue + " to " + 0 + ".");
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 0);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (waitQueueSacEvent)), visitLogsSet);      
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
    this.cachedVisitLogsSet = visitLogsSet;
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
      final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (subQueueIndex);
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
            subQueueEvent = new SimQueueEvent.ServerAccessCredits<> (subQueue, time, (Integer) argument);
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
      // Check the visit logs for drops and departures.
      for (JobQueueVisitLog<SimJob,Q> jvl : subQueueVisitLogsSet)
        if (jvl.dropped)
          queueState.doExits (time, Collections.singleton (jvl.job), null, null, null, visitLogsSet);
        else if (jvl.departed)
          queueState.doExits (time, null, null, Collections.singleton (jvl.job), null, visitLogsSet);
      // Reassess the SAC value on the wait queue.
      final SimQueue waitQueue = getWaitQueue (queue);
      final SimQueue serveQueue = getServeQueue (queue);
      final int sac = queueState.getServerAccessCredits ();
      final int waitQueueSac = getWaitQueueState (queue, queueState).getServerAccessCredits ();
      final DefaultSimQueueState serveQueueState = getServeQueueState (queue, queueState);
      if (sac > 0 && getServeQueuePredictor ().isStartArmed (serveQueue, serveQueueState) && waitQueueSac == 0)
      {
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 1);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (waitQueueSacEvent)), visitLogsSet);
      }
      else if ((sac == 0 || ! getServeQueuePredictor ().isStartArmed (serveQueue, serveQueueState)) && waitQueueSac > 0)
      {
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 0);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (waitQueueSacEvent)), visitLogsSet);        
      }
    }
    else
      throw new RuntimeException ();
//    if (eventType != null)
//      queueEventTypes.remove (eventType);
  }

  protected void waitQueuePostStartHook (final double time, final Set<SimJob> starters, final Object userData)
    throws SimQueuePredictionException
  {
    if (starters == null || starters.size () != 1 || userData == null)
      throw new RuntimeException ();
    final SimJob job = starters.iterator ().next ();
    final Q queue = (Q) ((SimQueueAndSimQueueState) userData).queue;
    final DefaultSimQueueState queueState = ((SimQueueAndSimQueueState) userData).queueState;
    if (queue == null || queueState == null)
      throw new RuntimeException ();
    final SimQueue waitQueue = getWaitQueue (queue);
    final SimQueue serveQueue = getServeQueue (queue);
    final DefaultSimQueueState waitQueueState = getWaitQueueState (queue, queueState);
    final DefaultSimQueueState serveQueueState = getServeQueueState (queue, queueState);
    // Remove the job from the wait queue (as revocations, but we will not record it anyway).
    waitQueueState.doExits (time, null, Collections.singleton (job), null, null, null);
    // Let the job start on the main queue.
    queueState.doStarts (time, starters);
    //System.err.println ("SimQueuePredictor_CTandem2; t=" + time + ": Starting job " + job + " on " + queue
    //  + ", remaining SAC=" + queueState.getServerAccessCredits () + ".");
    // Check whether job did not already leave!
    if (queueState.getJobs ().contains (job))
    {
      // Let the job arrive at the serve queue, but his time, use the predictor.
      if (! (job instanceof DefaultSimJob))
        throw new UnsupportedOperationException ();
      ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (serveQueue, job.getServiceTime (queue));
      final SubQueueSimpleEvent serveQueueEvent =
        new SubQueueSimpleEvent (serveQueue, SimQueueSimpleEventType.ARRIVAL, null, job, null);
      doQueueEvents_SQ_SV_ROEL_U
        (queue, queueState, new HashSet<> (Collections.singleton (serveQueueEvent)), this.cachedVisitLogsSet);
    }
  }
  
}