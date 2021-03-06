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
import java.util.HashSet;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.serverless.LeakyBucket;
import org.javades.jqueues.r5.extensions.ratelimit.RateLimitSimpleEventType;
import org.javades.jqueues.r5.extensions.ratelimit.SimQueueRateLimitStateHandler;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link LeakyBucket}.
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
public class SimQueuePredictor_LeakyBucket
extends AbstractSimQueuePredictor<LeakyBucket>
{

  @Override
  public SimQueueState<SimJob, LeakyBucket> createQueueState (final LeakyBucket queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    final SimQueueRateLimitStateHandler queueStateHandler = new SimQueueRateLimitStateHandler ();
    queueState.registerHandler (queueStateHandler);
    queueStateHandler.setRateLimited (queue.getRateLimit () == 0.0);
    return queueState;
  }

  @Override
  public String toString ()
  {
    return "Predictor[LeakyBucket[?]]";
  }

  @Override
  public boolean isStartArmed (final LeakyBucket queue, final SimQueueState<SimJob, LeakyBucket> queueState)
  {
    return false;
  }
  
  @Override
  public double getNextQueueEventTimeBeyond
  (final LeakyBucket queue,
   final SimQueueState<SimJob, LeakyBucket> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final SimQueueRateLimitStateHandler queueStateHandler =
      (SimQueueRateLimitStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueRateLimitStateHandler");
    if (! queueStateHandler.isRateLimited ())
      return Double.NaN;
    queueEventTypes.add (RateLimitSimpleEventType.RATE_LIMIT_EXPIRATION);
    return queueStateHandler.getLastDepTime () + (1.0 / queue.getRateLimit ());
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final LeakyBucket queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, LeakyBucket> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, LeakyBucket>> visitLogsSet)
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
    final SimQueueRateLimitStateHandler queueStateHandler =
      (SimQueueRateLimitStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueRateLimitStateHandler");
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
        // Drops.
        queueState.doExits (time, arrivals, null, null, null, visitLogsSet);
      else if (queueState.getJobs ().isEmpty () && ! queueStateHandler.isRateLimited ())
      {
        // Departures.
        queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
        queueStateHandler.setLastDepTime (time);
        if (Double.isFinite (queue.getRateLimit ()))
          queueStateHandler.setRateLimited (true);
      }
      else if (queue.getBufferSize () == Integer.MAX_VALUE
            || queueState.getJobsInWaitingArea ().size () < queue.getBufferSize ())
        // Arrivals.
        queueState.doArrivals (time, arrivals, visitLogsSet);
      else
        // Drops.
        queueState.doExits (time, arrivals, null, null, null, visitLogsSet);
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
        queueState.doExits (time, null, revocations, null, null, visitLogsSet);
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final LeakyBucket queue,
   final SimQueueState<SimJob, LeakyBucket> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, LeakyBucket>> visitLogsSet)
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
    final SimQueueRateLimitStateHandler queueStateHandler =
      (SimQueueRateLimitStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueRateLimitStateHandler");
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType == RateLimitSimpleEventType.RATE_LIMIT_EXPIRATION)
    {
      if (! queueState.getJobs ().isEmpty ())
      {
        // Make sure we only let depart a single job here, even if multiple jobs have identical arrival times!
        final Set<SimJob> departures = Collections.singleton (queueState.getJobArrivalsMap ().firstEntry ().getValue ().get (0));
        queueState.doExits (time, null, null, departures, null, visitLogsSet);
        queueStateHandler.setLastDepTime (time);
        queueStateHandler.setRateLimited (true);
      }
      else
        queueStateHandler.setRateLimited (false);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }
  
  @Override
  public void updateToTime (final LeakyBucket queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    queueState.setTime (newTime);
  }

}