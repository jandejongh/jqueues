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

import java.util.function.ToDoubleBiFunction;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.IS;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link IS}.
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
public class SimQueuePredictor_IS
extends SimQueuePredictor_FCFS
{
  
  final boolean overrideServiceTime;
  
  final double serviceTime;

  @Override
  public SimQueueState<SimJob, SimQueue> createQueueState (SimQueue queue, boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    if (this.overrideServiceTime)
      queueState.setServiceTimeProvider (this.serviceTimeProvider);
    return queueState;
  }
  
  protected SimQueuePredictor_IS (final boolean overrideServiceTime, final double serviceTime)
  {
    super (false, 0, false, 0);
    this.overrideServiceTime = overrideServiceTime;
    this.serviceTime = serviceTime;
  }
  
  public SimQueuePredictor_IS ()
  {
    this (false, Double.NaN);
  }
  
  @Override
  public String toString ()
  {
    return "Predictor[IS]";
  }

  private final ToDoubleBiFunction<SimQueue, SimJob> serviceTimeProvider =
    (final SimQueue queue, final SimJob job) -> SimQueuePredictor_IS.this.serviceTime;
  
}