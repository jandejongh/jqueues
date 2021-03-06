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
package org.javades.jqueues.r5.util.loadfactory.pattern;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQEventScheduler;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import org.javades.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0005.
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
public class LoadFactory_SQ_SV_0005<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0002<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "A few zero-service time jobs subject to zero/low server-access credits.";
  }

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> sets the server-access credits to zero at t=0;
   * <li> generates zero required service-time jobs at t=1, t=2 and t=3.
   * <li> sets the server-access credits to two at t=4.
   * <li> sets the server-access credits to infinity at t=10.
   * </ul>
   * 
   * @see SimJQEventScheduler#scheduleJQ
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
    final Set<J> jobs = new LinkedHashSet<> ();
    final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final SimEventList jobEventList = (attachSimJobsToEventList ? eventList : null);
    final SimJQEvent<J, Q> sacSchedule_0 = new SimQueueEvent.ServerAccessCredits<> (queue, 0.0, 0);
    realQueueExternalEvents.put (0.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (0.0).add (sacSchedule_0);
    eventsToSchedule.add (sacSchedule_0);
    for (int j = 1; j <= 3; j++)
    {
      final J job = jobFactory.newInstance
        (jobEventList, Integer.toString (j), generateRequestedServiceTimeMap (queue));
      final SimJQEvent<J, Q> arrivalSchedule = new SimJQEvent.Arrival<> (job, queue, (double) j);
      realQueueExternalEvents.put ((double) j, new LinkedHashSet<> ());
      realQueueExternalEvents.get ((double) j).add (arrivalSchedule);
      eventsToSchedule.add (arrivalSchedule);
      jobs.add (job);
    }
    final SimJQEvent<J, Q> sacSchedule_4 = new SimQueueEvent.ServerAccessCredits<> (queue, 4.0, 2);
    realQueueExternalEvents.put (4.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (4.0).add (sacSchedule_4);
    eventsToSchedule.add (sacSchedule_4);
    final SimJQEvent<J, Q> sacSchedule_10 = new SimQueueEvent.ServerAccessCredits<> (queue, 10.0, Integer.MAX_VALUE);
    realQueueExternalEvents.put (10.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (10.0).add (sacSchedule_10);
    eventsToSchedule.add (sacSchedule_10);
    SimJQEventScheduler.scheduleJQ (eventList, reset, resetTime, eventsToSchedule);
    return jobs;
  }
  
}
