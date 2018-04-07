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
import org.javades.jqueues.r5.entity.jq.queue.serverless.GATE;

/** A {@link SimQueue} with the notion of a single gate
 *  that can be opened (optionally for a limited number of passages) and closed.
 * 
 * <p>
 * Typically, but not necessarily, used to let {@link SimJob}s pass when the gate is open,
 * and let them wait while the gate is closed.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see GATE
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
public interface SimQueueWithGate<J extends SimJob, Q extends SimQueueWithGate>
extends SimQueue<J, Q>
{
  
  /** Returns the number of (remaining) gate-passage credits.
   * 
   * @return The number of (remaining) gate-passage credits.
   * 
   */
  public int getGatePassageCredits ();
  
  /** Sets (overwrites) the (remaining) gate-passage credits.
   * 
   * <p>
   * Note that setting the remaining number of passage credits to zero effectively closes the gate,
   * and setting it to {@link Integer#MAX_VALUE} (treated as infinity) opens it without limits on the number of passages.
   * 
   * <p>
   * If a {@link SimQueueWithGate} does not support this operation, it is to consider every strictly positive value
   * as {@link Integer#MAX_VALUE}, effectively opening the gate without limits on the number of passages.
   * 
   * @param time               The current time.
   * @param gatePassageCredits The remaining number of passages to allow (will override, not add to, any previous value),
   *                             with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If the number of passages passed is strictly negative.
   * 
   */
  public void setGatePassageCredits (double time, int gatePassageCredits);
  
}
