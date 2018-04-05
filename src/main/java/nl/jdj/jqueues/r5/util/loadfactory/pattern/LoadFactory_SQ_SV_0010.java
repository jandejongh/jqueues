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
package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.util.loadfactory.AbstractLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0010.
 *
 * @see #generate
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
public class LoadFactory_SQ_SV_0010<J extends SimJob, Q extends SimQueue>
extends AbstractLoadFactory_SQ_SV<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "Standard test load pattern A1=1/S1=1, A2=2/S2=2, ..., with optional service-time jitter.";
  }

  /** A load-factory hint enforcing jitter on the service-time requirement of jobs (e.g., in order to avoid ambiguities).
   * 
   */
  public static final LoadFactoryHint SERVICE_TIME_JITTER = new LoadFactoryHint ()
  {
    @Override
    public final String toString ()
    {
      return "SERVICE_TIME_JITTER";
    }
  };
  
  private final Random rngRequestedServiceTimeJitter = new Random ();
  
  /** Creates a suitable map for the requested service time for a job visit to a queue.
   * 
   * <p>
   * Upon request, a jitter from U[-0.01, +0.01] is added to the service time.
   * This is typically used to avoid ambiguities in the schedule.
   * 
   * @param queue The queue.
   * @param n     The job number.
   * @param jitter Whether to apply jitter to the requested service time.
   * 
   * @return A map holding the service time (i.e., the job number) at the queue.
   * 
   * @see SimJobFactory#newInstance For the use of the map generated.
   * @see LoadFactory_SQ_SV_0010#SERVICE_TIME_JITTER
   * 
   */
  protected Map<Q, Double> generateRequestedServiceTimeMap (final Q queue, final int n, final boolean jitter)
  {
    final Map<Q, Double> requestedServiceTimeMap = new HashMap ();
    final double requestedServiceTimeJitter =
      jitter
      ? 0.01 * (2.0 * this.rngRequestedServiceTimeJitter.nextDouble () - 1.0)
      : 0.0;
    requestedServiceTimeMap.put (queue, ((double) n) + requestedServiceTimeJitter);
    return requestedServiceTimeMap;
  }
  
  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the requested number of jobs, and number them starting with one;
   * <li> set the requested service time for each job equal to its job number (adding jitter if requested through
   *      {@link LoadFactory_SQ_SV_0010#SERVICE_TIME_JITTER});
   * <li> schedules a single arrival for each job at time equal to its job number.
   * </ul>
   * 
   * <p>
   * Jobs are returned in a {@link LinkedHashSet}, preserving the creation order of the jobs.
   * 
   * @see SimJQEventScheduler#scheduleJQ
   * @see LoadFactory_SQ_SV_0010#SERVICE_TIME_JITTER
   * 
   */
  @Override
  public Set<J> generate
  (final SimEventList eventList,
    boolean attachSimJobsToEventList,
    final Q queue,
    final SimJobFactory<J, Q> jobFactory,
    final int numberOfJobs,
    final boolean reset,
    final double resetTime,
    final Set<LoadFactoryHint> hints,
    final NavigableMap<Double, Set<SimJQEvent>> queueExternalEvents)
  {
    if (eventList == null || queue == null || jobFactory == null)
      throw new IllegalArgumentException ();
    if (numberOfJobs < 0)
      throw new IllegalArgumentException ();
    final Set<J> jobs = new LinkedHashSet<> ();
    final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final SimEventList jobEventList = (attachSimJobsToEventList ? eventList : null);
    final boolean jitter = (hints != null && hints.contains (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER));
    for (int i = 1; i <= numberOfJobs; i++)
    {
      final J job = jobFactory.newInstance
        (jobEventList, Integer.toString (i), generateRequestedServiceTimeMap (queue, i, jitter));
      final SimJQEvent<J, Q> arrivalSchedule = new SimJQEvent.Arrival<> (job, queue, (double) i);
      if (! realQueueExternalEvents.containsKey ((double) i))
        realQueueExternalEvents.put ((double) i, new LinkedHashSet<> ());
      realQueueExternalEvents.get ((double) i).add (arrivalSchedule);
      eventsToSchedule.add (arrivalSchedule);
      jobs.add (job);
    }
    SimJQEventScheduler.scheduleJQ (eventList, reset, resetTime, eventsToSchedule);
    return jobs;
  }
  
}
