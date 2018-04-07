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
package org.javades.jqueues.r5.entity.jq.job;

import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;

/** A {@link SimEvent} for a {@link SimJob} operation on {@link SimJob}s.
 * 
 * <p>
 * This class only administers the key parameters for the event; it does not actually schedule it.
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
public abstract class SimJobEvent<J extends SimJob, Q extends SimQueue>
extends SimJQEvent<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new event for a job.
   * 
   * @param name   The (optional) name of the event, may be  {@code null}.
   * @param time   The time at which the event occurs.
   * @param queue  The queue related to the event (if applicable), may be {@code null}.
   * @param job    The job related to the event, non-{@code null}.
   * @param action The {@link SimEventAction} to take; may be {@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null}.
   * 
   */
  protected SimJobEvent
  (final String name,
   final double time,
   final Q queue,
   final J job,
   final SimEventAction<? extends SimEntity> action)
  {
    super (name, time, queue, job, action);
    if (job == null)
      throw new IllegalArgumentException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}