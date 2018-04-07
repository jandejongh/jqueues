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
package org.javades.jqueues.r5.util.predictor.state;

import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleHandler;

/** A handler for an extension to a {@link DefaultSimQueueState}.
 * 
 * <p>
 * For a more detailed description of the rationale and architecture of extensions to a {@link DefaultSimQueueState},
 * see the description of {@link WorkloadScheduleHandler} for {@link DefaultWorkloadSchedule}.
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
public interface SimQueueStateHandler
{

  /** Returns the name of the handler.
   * 
   * <p>
   * The handler name must be unique within the realm of the {@link DefaultSimQueueState} at which this
   * handler registers. For {@link SimQueue} state extensions, the convention is to use
   * the interface name appended with "Handler", like, "SimQueueHandler" and "SimQueueWithGateHandler".
   * 
   * @return The name of the handler (must remain fixed during the handler's lifetime).
   * 
   */
  public String getHandlerName ();

  /** Initializes the handler, and passes the {@link DefaultSimQueueState} object.
   * 
   * <p>
   * This method is called only once during registration at the {@link DefaultSimQueueState} object.
   * 
   * @param queueState The {@link DefaultSimQueueState} at which we register, non-{@code null}.
   * 
   * @see DefaultSimQueueState#registerHandler
   * 
   */
  public void initHandler (DefaultSimQueueState queueState);
  
  /** Resets the (state represented by) this handler.
   * 
   * @param queueState The {@link DefaultSimQueueState} at which we are registered, non-{@code null}.
   * 
   * @see SimQueueState#reset
   * @see DefaultSimQueueState#reset
   * 
   */
  public void resetHandler (DefaultSimQueueState queueState);
  
}
