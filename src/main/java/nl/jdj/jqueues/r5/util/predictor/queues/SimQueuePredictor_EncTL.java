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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.EncTL;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite_Enc;
import nl.jdj.jqueues.r5.extensions.composite.SimQueueCompositeStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link EncTL}.
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
public class SimQueuePredictor_EncTL
extends AbstractSimQueuePredictor_Composite_Enc<EncTL>
implements SimQueuePredictor<EncTL>
{
  
  final AbstractSimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_EncTL (final AbstractSimQueuePredictor encQueuePredictor)
  {
    super (encQueuePredictor);
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public String toString ()
  {
    return "Predictor[EncTL[?]]";
  }

  @Override
  public boolean hasServerAccessCredits
  (final EncTL queue,
   final SimQueueState<SimJob, EncTL> queueState)
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
  (final EncTL queue,
   final SimQueueState<SimJob, EncTL> queueState)
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

  private static class ForcedDeparture extends SimEntitySimpleEventType.Member
  {

    private final Set<SimJob> jobs;
    
    public ForcedDeparture (final String name, final Set<SimJob> jobs)
    {
      super (name);
      if (jobs == null || jobs.isEmpty () || jobs.contains (null))
        throw new IllegalArgumentException ();
      this.jobs = jobs;
    }
    
  }
  
  @Override
  public double getNextQueueEventTimeBeyond
  (final EncTL queue,
    final SimQueueState<SimJob, EncTL> queueState,
    final Set<SimEntitySimpleEventType.Member> queueEventTypes)
    throws SimQueuePredictionException
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double encTime = super.getNextQueueEventTimeBeyond (queue, queueState, queueEventTypes);
    if (queueState.getJobs ().isEmpty ())
      return encTime;
    double schedTime = encTime;
    Set<SimJob> expJobs = null;
    if ((! Double.isInfinite (queue.getMaxWaitingTime ()))
    &&  (! queueState.getJobsInWaitingArea ().isEmpty ())
    &&  (Double.isNaN (schedTime)
         || queueState.getArrivalTimesMap ().get (queueState.getJobsInWaitingAreaOrdered ().iterator ().next ())
            + queue.getMaxWaitingTime () < schedTime))
    {
      schedTime = queueState.getArrivalTimesMap ().get (queueState.getJobsInWaitingAreaOrdered ().iterator ().next ())
                  + queue.getMaxWaitingTime ();
      expJobs = new LinkedHashSet<> ();
      for (final SimJob job : queueState.getJobsInWaitingAreaOrdered ())
        if (queueState.getArrivalTimesMap ().get (job) + queue.getMaxWaitingTime () < schedTime)
          throw new RuntimeException ();
        else if (queueState.getArrivalTimesMap ().get (job) + queue.getMaxWaitingTime () == schedTime)
          expJobs.add (job);
        else if (expJobs.isEmpty ())
          throw new RuntimeException ();
        else
          break;
    }
    if ((! Double.isInfinite (queue.getMaxServiceTime ()))
    &&  (! queueState.getJobsInServiceArea ().isEmpty ())
    &&   (Double.isNaN (schedTime)
          || queueState.getJobsInServiceAreaMap ().firstKey () + queue.getMaxServiceTime () < schedTime))
    {
      schedTime = queueState.getJobsInServiceAreaMap ().firstKey () + queue.getMaxServiceTime ();
      expJobs = new LinkedHashSet<> (queueState.getJobsInServiceAreaMap ().firstEntry ().getValue ());
    }
    if ((! Double.isInfinite (queue.getMaxSojournTime ()))
    &&  (! queueState.getJobs ().isEmpty ())
    &&   (Double.isNaN (schedTime)
          || queueState.getJobArrivalsMap ().firstKey () + queue.getMaxSojournTime () < schedTime))
    {
      schedTime = queueState.getJobArrivalsMap ().firstKey () + queue.getMaxSojournTime ();
      expJobs = new LinkedHashSet<> (queueState.getJobArrivalsMap ().firstEntry ().getValue ());
    }
    if (expJobs != null)
    {
      queueEventTypes.clear ();
      queueEventTypes.add (new ForcedDeparture ("FORCED_DEPARTURE", expJobs));
    }
    return schedTime;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final EncTL queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, EncTL> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncTL>> visitLogsSet)
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
    final SimQueue subQueue = queue.getEncapsulatedQueue ();
    final SimQueueState subQueueState = queueStateHandler.getSubQueueState (0);
    final SimEntitySimpleEventType.Member eventType = (workloadEventTypes.isEmpty ()
      ? null
      : workloadEventTypes.iterator ().next ());
    if (eventType == SimQueueSimpleEventType.ARRIVAL
      && (queue.getMaxSojournTime () == 0
          || (queue.getMaxWaitingTime () == 0
              && ! (this.subQueuePredictor.isStartArmed (subQueue, subQueueState))
                    && subQueueState.getJobsInWaitingArea ().isEmpty ()
                    && subQueueState.getServerAccessCredits () > 0)))
    {
      final SimJob job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      final Set<SimJob> arrivals = new HashSet<> ();
      arrivals.add (job);
      queueState.doArrivals (time, arrivals, visitLogsSet); // Takes care of qav.
      if (queueState.getJobs ().contains (job))
        queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
      workloadEventTypes.remove (eventType);
    }
    else
      super.doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule, queueState, workloadEventTypes, visitLogsSet);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final EncTL queue,
   final SimQueueState<SimJob, EncTL> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncTL>> visitLogsSet)
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
    if (eventType != null && (eventType instanceof ForcedDeparture))
    {
      final Set<SimJob> jobs = ((ForcedDeparture) eventType).jobs;
      for (final SimJob job : jobs)
      {
          final SimQueue encQueue = (SimQueue) queue.getQueues ().iterator ().next ();
          if (! (job instanceof DefaultSimJob))
            throw new UnsupportedOperationException ();
          final SubQueueSimpleEvent encQueueEvent =
            new SubQueueSimpleEvent (encQueue, SimQueueSimpleEventType.REVOCATION, null, job, true);
          doQueueEvents_SQ_SV_ROEL_U
            (queue, queueState, new HashSet<> (Collections.singleton (encQueueEvent)), /* visitLogsSet */ new HashSet<> ());
      }
      departJobs (time, queue, queueState, jobs, visitLogsSet);
    }
    else
      super.doQueueEvents_SQ_SV_ROEL_U (queue, queueState, queueEventTypes, visitLogsSet);
  }

}