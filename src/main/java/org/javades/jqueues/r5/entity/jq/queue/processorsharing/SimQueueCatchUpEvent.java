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
package org.javades.jqueues.r5.entity.jq.queue.processorsharing;

import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;

/** A catch-up {@link SimEvent} at a queue.
 * 
 * <p>
 * Do not <i>ever</i> schedule this yourself unless for your own implementation;
 * it is for private use by {@link SimQueue} implementations.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see CUPS
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
public class SimQueueCatchUpEvent<J extends SimJob, Q extends SimQueue>
extends SimJQEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a catch-up event at a specific queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
   * 
   * @param queue       The queue at which catch-up occurs.
   * @param catchUpTime The scheduled catch-up time.
   * @param action      The {@link SimEventAction} to take; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>.
   * 
   */
  public SimQueueCatchUpEvent
  (final Q queue, final double catchUpTime, final SimEventAction<J> action)
  {
    super ("CatchUp@" + queue, catchUpTime, queue, null, action);
    if (action == null)
      throw new IllegalArgumentException ();
  }

  /** Throws an {@link UnsupportedOperationException}.
   * 
   * <p>
   * A {@link SimQueueCatchUpEvent} is a queue-internal event.
   * 
   * @throws UnsupportedOperationException Always.
   * 
   */
  @Override
  public final SimJQEvent<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
  {
    throw new UnsupportedOperationException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
