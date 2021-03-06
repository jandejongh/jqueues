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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQEventScheduler;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import org.javades.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0012.
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
public class LoadFactory_SQ_SV_0012<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0010<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "Standard pattern (0010) with revocation request for every 5th job with random request time and random interrupt flag.";
  }

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_0010#generate};
   * <li> adds a revocation for every 5th job.
   * </ul>
   * 
   * <p>
   * The {@code interruptService} flag is chosen at random from a uniform distribution.
   * If {@code true}, the revocation is scheduled uniformly distributed in the <i>service</i> interval under
   * single-server FCFS, otherwise in the <i>wait</i> interval.
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
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final Iterator<J> i_jobs = jobs.iterator ();
    final Random rngInterrupt = new Random ();
    final Random rngDelay = new Random ();
    for (int i = 1; i <= jobs.size (); i++)
    {
      final J job = i_jobs.next ();
      if (i % 5 == 0)
      {
        final double arrivalTime = (double) i;
        final double serviceTime = (double) i;
        final double expWorkArrived = 0.5 * i * (i + 1);
        final double expWorkDone = (double) (i - 1);
        final double expWait = expWorkArrived - expWorkDone - serviceTime;
        final boolean interruptService = rngInterrupt.nextBoolean ();
        final double delay = (interruptService
          ? (expWait + serviceTime * rngDelay.nextDouble ())
          : expWait * rngDelay.nextDouble ());
        final double revocationTime = arrivalTime + delay;
        final SimJQEvent<J, Q> revocationSchedule
          = new SimJQEvent.Revocation<> (job, queue, revocationTime, interruptService);
      if (! realQueueExternalEvents.containsKey (revocationTime))
        realQueueExternalEvents.put (revocationTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (revocationTime).add (revocationSchedule);
      eventsToSchedule.add (revocationSchedule);
      }
    }
    // Be careful not to reset the event list (again) here!
    SimJQEventScheduler.scheduleJQ (eventList, false, Double.NaN, eventsToSchedule);
    return jobs;
  }
  
}
