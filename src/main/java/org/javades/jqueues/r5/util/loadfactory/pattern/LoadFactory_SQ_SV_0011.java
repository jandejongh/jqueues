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

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0011.
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
public class LoadFactory_SQ_SV_0011<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0010<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "Standard pattern (0010) with qav from 2.5-3.5, 5.5-6.5, etc., dropping every third job.";
  }

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_0010#generate};
   * <li> adds queue-access vacations from 2.5 until 3.5, 5.5 until 6.5, etc.
   * </ul>
   * 
   * <p>
   * This should effectively make the queue drop every 3rd job generated.
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
    final Set<J> jobs = super.generate (eventList, attachSimJobsToEventList,
      queue, jobFactory, numberOfJobs, reset, resetTime, hints, queueExternalEvents);
    final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final int numberOfQavToSchedule = Math.max (1, jobs.size () / 3);
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    for (int i = 1; i <= numberOfQavToSchedule; i++)
    {
      final double startQavTime = 3.0 * i - 0.5;
      final double endQavTime  = 3.0 * i + 0.5;
      final SimJQEvent<J, Q> qavOnSchedule = new SimQueueEvent.QueueAccessVacation<> (queue, startQavTime, true);
      if (! realQueueExternalEvents.containsKey (startQavTime))
        realQueueExternalEvents.put (startQavTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (startQavTime).add (qavOnSchedule);
      eventsToSchedule.add (qavOnSchedule);
      final SimJQEvent<J, Q> qavOffSchedule = new SimQueueEvent.QueueAccessVacation<> (queue, endQavTime, false);
      if (! realQueueExternalEvents.containsKey (endQavTime))
        realQueueExternalEvents.put (endQavTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (endQavTime).add (qavOffSchedule);
      eventsToSchedule.add (qavOffSchedule);
    }
    // Be careful not to reset the event list (again) here!
    SimJQEventScheduler.scheduleJQ (eventList, false, Double.NaN, eventsToSchedule);
    return jobs;
  }
  
}
