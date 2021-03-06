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
package org.javades.jqueues.r5.extensions.gate;

import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueListener;

/** A listener to state changes of a {@link SimQueueWithGate}.
 *
 * @param <J> The type of {@link SimJob}s supported.
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
public interface SimQueueWithGateListener<J extends SimJob, Q extends SimQueue>
extends SimQueueListener<J, Q>
{
 
  /** Notification of a change of in status (in terms of open/close) of the gate of a {@link SimQueueWithGate}.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * @param open  Whether the gate opened ({@code true}) or closed ({@code false}).
   * 
   * @see SimQueueWithGate#setGatePassageCredits
   * 
   */
  public void notifyNewGateStatus (double time, Q queue, boolean open);
  
}
