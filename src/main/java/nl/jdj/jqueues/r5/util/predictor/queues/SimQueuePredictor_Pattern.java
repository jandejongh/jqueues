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
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.Pattern;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite_LocalStart;
import nl.jdj.jqueues.r5.extensions.visitscounter.SimQueueVisitsCounterStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link Pattern}.
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
public class SimQueuePredictor_Pattern
extends AbstractSimQueuePredictor_Composite_LocalStart<Pattern>
implements SimQueuePredictor<Pattern>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public SimQueuePredictor_Pattern (final List<AbstractSimQueuePredictor> subQueuePredictors)
  {
    super (subQueuePredictors);
  }

  /** Registers a new {@link SimQueueVisitsCounterStateHandler} at the object created by super method.
   * 
   */
  @Override
  public SimQueueState<SimJob, Pattern> createQueueState
  (final Pattern queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueVisitsCounterStateHandler ());
    return queueState;
  }

  @Override
  public String toString ()
  {
    return "Predictor[Pattern[?]]";
  }

  @Override
  public boolean isStartArmed
  (final Pattern queue, final SimQueueState<SimJob, Pattern> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return true;
  }

  @Override
  protected void startJobs
  (final double time,
   final Pattern queue,
   final SimQueueState<SimJob, Pattern> queueState,
   final Set<SimJob> starters,
   final Set<JobQueueVisitLog<SimJob, Pattern>> visitLogsSet)
  throws SimQueuePredictionException
  {
    queueState.doStarts (time, starters);
    final SimQueueVisitsCounterStateHandler queueStateHandler =
      (SimQueueVisitsCounterStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueVisitsCounterStateHandler");
    final int[] pattern = queue.getPattern ();
    if (pattern == null || pattern.length == 0)
      departJobs (time, queue, queueState, starters, visitLogsSet);
    else
      for (final SimJob job : starters)
      {
        if (! (job instanceof DefaultSimJob))
          throw new UnsupportedOperationException ();
        final int modCounter = queueStateHandler.getTotalNumberOfVisits ();
        if (modCounter >= pattern.length)
          throw new IllegalStateException ();
        final int index = pattern[modCounter];
        if (modCounter == pattern.length - 1)
          queueStateHandler.resetTotalNumberOfVisits ();
        else
          queueStateHandler.incTotalNumberOfVisits ();
        final SimQueue subQueue;
        if (index < 0 || index >= queue.getQueues ().size ())
          subQueue = null;
        else
          subQueue = AbstractSimQueueComposite.getQueue (queue.getQueues (), index);
        final SubQueueSimpleEvent subQueueEvent;
        if (subQueue != null)
        {
          // Check whether job did not already leave!
          if (queueState.getJobs ().contains (job))
          {
            ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (subQueue, job.getServiceTime (queue));
            subQueueEvent = new SubQueueSimpleEvent (subQueue, SimQueueSimpleEventType.ARRIVAL, null, job, null);
            doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (subQueueEvent)), visitLogsSet);
          }
        }
        else
          departJobs (time, queue, queueState, Collections.singleton (job), visitLogsSet);
      }
  }
    
}