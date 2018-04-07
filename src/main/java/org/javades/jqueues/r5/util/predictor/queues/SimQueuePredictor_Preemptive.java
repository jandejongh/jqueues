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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.preemptive.AbstractPreemptiveSimQueue;
import org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy;
import static org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy.CUSTOM;
import static org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy.DEPART;
import static org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy.REDRAW;
import static org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy.RESTART;
import static org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy.RESUME;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;

/** An abstract {@link SimQueuePredictor} for preemptive queues.
 * 
 * @param <Q> The type of {@link SimQueue}s supported.
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
public abstract class SimQueuePredictor_Preemptive<Q extends AbstractPreemptiveSimQueue>
extends AbstractSimQueuePredictor<Q>
{

  /** Preempts a job in given {@link SimQueueState}.
   * 
   * <p>
   * Updates all relevant fields in the {@link SimQueueState} and takes the appropriate action
   * on the job that is being preempted, depending on the {@link PreemptionStrategy} on the queue.
   * 
   * @param queue        The queue, non-{@code null}.
   * @param queueState   The queue state, non-{@code null}.
   * @param executingJob The job to preempt, non-{@code null}.
   * @param visitLogsSet The visit-logs for logging dropped or departed jobs, may be {@code null}.
   * 
   * @throws SimQueuePredictionException   If the predictor cannot finish (e.g., due to ambiguity, complexity or plain errors).
   * @throws IllegalArgumentException      If one or more of the mandatory arguments is {@code null}.
   * @throws UnsupportedOperationException If the preemption strategy is {@link PreemptionStrategy#REDRAW}
   *                                         of {@link PreemptionStrategy#CUSTOM}.
   * 
   */
  protected void preemptJob
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final SimJob executingJob,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
   throws SimQueuePredictionException
  {
    if (queue == null || queueState == null || executingJob == null)
      throw new IllegalArgumentException ();
    final double time = queueState.getTime ();
    final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
    final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
    switch (queue.getPreemptionStrategy ())
    {
      case DROP:
        final Set<SimJob> drops = new HashSet<> ();
        final SimJob droppedJob = executingJob;
        drops.add (droppedJob);
        queueState.doExits (time, drops, null, null, null, visitLogsSet);
        break;
      case RESUME:
        // Nothing to do.
        break;
      case RESTART:
        final double oldRs = rsTimeMap.get (executingJob);
        // RESTART: Must always take the service time from the job!
        // final double newRs = ((DefaultSimQueueState) queueState).getServiceTime (queue, executingJob);
        final double newRs = executingJob.getServiceTime (queue);
        rsTimeMap.put (executingJob, newRs);
        rsMap.get (oldRs).remove (executingJob);
        if (rsMap.get (oldRs).isEmpty ())
          rsMap.remove (oldRs);
        if (rsMap.containsKey (newRs))
        {
          // Start time of executing job.
          final double startTime = queueState.getStartTimesMap ().get (executingJob);
          // Jobs with equal remaining service time, ordered increasing in start time.
          final List<SimJob> equalRsJobs = rsMap.get (newRs);
          int indexToInsert = 0;
          for (final SimJob equalRsJob: equalRsJobs)
          {
            final double equalRsJobStartTime = queueState.getStartTimesMap ().get (equalRsJob);
            if (equalRsJobStartTime < startTime)
              indexToInsert++;
            else if (equalRsJobStartTime == startTime)
              throw new SimQueuePredictionAmbiguityException ();
            else
              break;
          }
          rsMap.get (newRs).add (indexToInsert, executingJob);
        }
        else
        {
          rsMap.put (newRs, new ArrayList<> ());
          rsMap.get (newRs).add (executingJob);
        }
        break;
      case REDRAW:
        throw new UnsupportedOperationException ();
      case DEPART:
        final Set<SimJob> departures = new HashSet<> ();
        final SimJob departingJob = executingJob;
        departures.add (departingJob);
        queueState.doExits (time, null, null, departures, null, visitLogsSet);
        break;
      case CUSTOM:
        throw new UnsupportedOperationException ();
      default:
        throw new UnsupportedOperationException ();
    }
  }
  
}