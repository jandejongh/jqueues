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

import java.util.NavigableMap;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.SJF;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link SJF}.
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
public class SimQueuePredictor_SJF
extends SimQueuePredictor_FCFS
{

  @Override
  public String toString ()
  {
    return "Predictor[SJF]";
  }

  @Override
  protected SimJob getJobToStart (final SimQueue queue, final SimQueueState<SimJob, SimQueue> queueState)
  throws SimQueuePredictionException
  {
    final NavigableMap<Double, SimJob> serviceTimeMap = new TreeMap<> ();
    for (final SimJob job : queueState.getJobsInWaitingAreaOrdered ())
    {
      final double serviceTime = ((DefaultSimQueueState) queueState).getServiceTime (queue, job);
      if (serviceTimeMap.containsKey (serviceTime))
        // We already found a job with equal requested service time.
        // Since SJF breaks ties through arrival-time ordering, we can skip this job.
        continue;
      serviceTimeMap.put (serviceTime, job);
    }
    return serviceTimeMap.firstEntry ().getValue ();
  }

  
  public SimQueuePredictor_SJF ()
  {
    super ();
  }

}