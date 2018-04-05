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
package nl.jdj.jqueues.r5.util.loadfactory;

import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link LoadFactory} that generates a load for a single {@link SimQueue} in which each job visits the queue at most once.
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
public interface LoadFactory_SQ_SV<J extends SimJob, Q extends SimQueue>
extends LoadFactory<J, Q>
{

  /** Generates job from a factory, schedule arrivals (and/or revocations) for them on an event list, as well as queue vacations
   *  (if applicable), and return the generated load in a user-supplied collection of {@link SimJQEvent}s.
   * 
   * @param eventList                The event list, non-{@code null}.
   * @param attachSimJobsToEventList Whether or not to attach the generated {@link SimJob}s to the {@link SimEventList};
   *                                 typically set to {@code false}
   *                                 (see {@link SimEntity} and {@link SimJob} for more details on the consequences).
   * @param queue                    The queue, non-{@code null}.
   * @param jobFactory               The job factory, non-{@code null}.
   * @param numberOfJobs             The number of jobs to generate, non-negative.
   * @param reset                    Whether or not to reset the event list.
   * @param resetTime                The time to which to reset the event list.
   * @param hints                    An optional set of {@link LoadFactoryHint}s, may be {@code null}
   *                                 and unknown hints are silently ignored.
   * @param queueEvents              An optional map for storing the generated {@link SimJQEvent}s indexed by
   *                                 event time and for events at the same time, by order of occurrence.
   *                                 The map is <i>not</i> cleared; generated events in this method are assumed to
   *                                 occur after any existing events at the same time.
   * 
   * @return The set of jobs generated.
   * 
   * @throws IllegalArgumentException If any of the arguments supplied has an illegal value.
   * 
   */
  public Set<J> generate
  (SimEventList eventList,
    boolean attachSimJobsToEventList,
    Q queue,
    SimJobFactory<J, Q> jobFactory,
    int numberOfJobs,
    boolean reset,
    double resetTime,
    Set<LoadFactoryHint> hints,
    NavigableMap<Double, Set<SimJQEvent>> queueEvents);

  /** Returns a description of this load factory.
   * 
   * <p>
   * The default implementation returns "None provided!".
   * 
   * @return A description of this load factory.
   * 
   */
  default String getDescription ()
  {
    return "None provided!";
  }
  
}
