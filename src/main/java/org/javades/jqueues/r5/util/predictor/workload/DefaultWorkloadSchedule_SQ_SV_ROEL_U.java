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
package org.javades.jqueues.r5.util.predictor.workload;

import java.util.Map;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** A default implementation of {@link WorkloadSchedule_SQ_SV_ROEL_U}.
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
public class DefaultWorkloadSchedule_SQ_SV_ROEL_U
extends DefaultWorkloadSchedule_SQ_SV
implements WorkloadSchedule_SQ_SV_ROEL_U
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link DefaultWorkloadSchedule_SQ_SV_ROEL_U}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimJQEvent}s for a single specified queue, single visits to that queue for each job, and an unambiguous schedule
   *  on a Random-Order Event List.
   * 
   * @param <E>         The event type.
   * @param queue       The queue to consider, non-{@code null}; events related to other queues are ignored.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws IllegalArgumentException           If the queue is {@code null}.
   * @throws WorkloadScheduleAmbiguityException If the workload represented by the {@code queueEvents} argument is
   *                                            ambiguous on a Random-Order Event List.
   * @throws WorkloadScheduleInvalidException   If the workload represented by the {@code queueEvents} argument is invalid
   *                                            (e.g., containing a job with multiple visits to the {@code queue}.
   * 
   * @see #isUnambiguous_ROEL
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule_SQ_SV_ROEL_U
  (final SimQueue queue,
   final Set<E> queueEvents)
  throws WorkloadScheduleException
  {
    super (queue, queueEvents);
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleAmbiguityException ();
  }
  
  /** Creates a new {@link DefaultWorkloadSchedule_SQ_SV}, filling out all the internal sets and maps from scanning a map of 
   *  event time onto sets of {@link SimJQEvent}s
   *  for a single specified queue, single visits to that queue for each job, and an unambiguous schedule
   *  on a Random-Order Event List.
   * 
   * @param <E>         The event type.
   * @param queue       The queue to consider, non-{@code null}; events related to other queues are ignored.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws IllegalArgumentException           If the queue is {@code null}.
   * @throws WorkloadScheduleAmbiguityException If the workload represented by the {@code queueEvents} argument is
   *                                            ambiguous on a Random-Order Event List.
   * @throws WorkloadScheduleInvalidException   If the workload represented by the {@code queueEvents} argument is invalid
   *                                            (e.g., containing a job with multiple visits to the {@code queue}.
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule_SQ_SV_ROEL_U
  (final SimQueue queue,
   final Map<Double, Set<E>> queueEvents)
  throws WorkloadScheduleException
  {
    super (queue, queueEvents);
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleAmbiguityException ();    
  }
  
}
