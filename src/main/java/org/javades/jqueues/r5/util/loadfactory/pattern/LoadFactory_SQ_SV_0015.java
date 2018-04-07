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

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0015.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see LoadFactory_SQ_SV_0005
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
public class LoadFactory_SQ_SV_0015<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0002<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "Three jobs arrive (t=1, 2, 3) during zero sac, as well as revocation requests (t=4, 5) (from the waiting area).";
  }

  /** Generates the load.
   * 
   * <p>
   * Simple revocations from the waiting area.
   * 
   * <p>
   * This method
   * <ul>
   * <li> sets the server-access credits to zero at t=0;
   * <li> generates zero required service-time jobs at t=1, t=2 and t=3;
   * <li> revokes job 2 at t=4 (no interrupt);
   * <li> revokes job 1 at t=5 (interrupt);
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
    //
    final Set<J> jobs = new LinkedHashSet<> ();
    final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final SimEventList jobEventList = (attachSimJobsToEventList ? eventList : null);
    // t=0
    final SimJQEvent<J, Q> sacSchedule_0 = new SimQueueEvent.ServerAccessCredits<> (queue, 0.0, 0);
    realQueueExternalEvents.put (0.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (0.0).add (sacSchedule_0);
    eventsToSchedule.add (sacSchedule_0);
    // t=1,2,3
    J job1 = null, job2 = null;
    for (int j = 1; j <= 3; j++)
    {
      final J job = jobFactory.newInstance
        (jobEventList, Integer.toString (j), generateRequestedServiceTimeMap (queue));
      if (j == 1)
        job1 = job;
      if (j == 2)
        job2 = job;
      final SimJQEvent<J, Q> arrivalSchedule = new SimJQEvent.Arrival<> (job, queue, (double) j);
      realQueueExternalEvents.put ((double) j, new LinkedHashSet<> ());
      realQueueExternalEvents.get ((double) j).add (arrivalSchedule);
      eventsToSchedule.add (arrivalSchedule);
      jobs.add (job);
    }
    // t=4
    final SimJQEvent<J, Q> rev2Schedule = new SimJQEvent.Revocation<> (job2, queue, 4.0, false);
    realQueueExternalEvents.put (4.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (4.0).add (rev2Schedule);
    eventsToSchedule.add (rev2Schedule);
    // t=5
    final SimJQEvent<J, Q> rev1Schedule = new SimJQEvent.Revocation<> (job1, queue, 5.0, true);
    realQueueExternalEvents.put (5.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (5.0).add (rev1Schedule);
    eventsToSchedule.add (rev1Schedule);
    // t=10
    final SimJQEvent<J, Q> sacSchedule_10 = new SimQueueEvent.ServerAccessCredits<> (queue, 10.0, Integer.MAX_VALUE);
    realQueueExternalEvents.put (10.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (10.0).add (sacSchedule_10);
    eventsToSchedule.add (sacSchedule_10);
    // schedule
    SimJQEventScheduler.scheduleJQ (eventList, reset, resetTime, eventsToSchedule);
    //
    return jobs;
  }
  
}
