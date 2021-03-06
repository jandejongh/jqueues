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
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQEventScheduler;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent;
import org.javades.jqueues.r5.extensions.gate.SimQueueGateEvent;
import org.javades.jqueues.r5.extensions.gate.SimQueueWithGate;
import org.javades.jqueues.r5.extensions.gate.SimQueueWithGateOperationUtils;
import org.javades.jqueues.r5.extensions.gate.SimQueueWithGateOperationUtils.GatePassageCreditsOperation;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import org.javades.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0014.
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
public class LoadFactory_SQ_SV_0014<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0010<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "Standard pattern (0010) with random (0, 1, 5, infty) gate-passage-credits settings @t=11.19, @t=22.19, @t=33.19, ....";
  }

  /** A load-factory hint that forces gate-passage credits events
   *  (irrespective of the auto-detection of the queue's capabilities).
   * 
   */
  public static final LoadFactoryHint FORCE_GPC = new LoadFactoryHint ()
  {
    @Override
    public final String toString ()
    {
      return "FORCE_GPC";
    }
  };
  
  /** A load-factory hint that disables gate-passage credits events
   *  (irrespective of the auto-detection of the queue's capabilities).
   * 
   */
  public static final LoadFactoryHint DISABLE_GPC = new LoadFactoryHint ()
  {
    @Override
    public final String toString ()
    {
      return "DISABLE_GPC";
    }
  };
  
  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_0010#generate};
   * <li> <i>only if</i> the queue is a {@link SimQueueWithGate}
   *         or has {@link GatePassageCreditsOperation} registered,
   *         it adds setting the number of gate-passage credits on the queue at 11.19, 22.19, 33.19, etc.
   * </ul>
   * 
   * <p>
   * Note: this method generates {@link SimQueueGateEvent}s <i>only</i> for {@link SimQueueWithGate}s
   * or queues that have has {@link GatePassageCreditsOperation} registered as operation.
   * The check is done at runtime, and not reflected in the generic-type arguments of this class.
   * Otherwise, instances of this class behave as if they
   * were a {@link LoadFactory_SQ_SV_0010}.
   * 
   * <p>
   * The amount of gate-passage credits is drawn from {0, 1, 5, {@link Double#MAX_VALUE}} with equal probabilities.
   * Setting the gate-passage credits is scheduled roughly until all jobs would have been served under single-server FCFS,
   * with an additional 100% to account for the gate delays.
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
    final boolean forceGpc = (hints != null && hints.contains (LoadFactory_SQ_SV_0014.FORCE_GPC));
    final boolean disableGpc = (hints != null && hints.contains (LoadFactory_SQ_SV_0014.DISABLE_GPC));
    if (forceGpc && disableGpc)
      // Conflicting hints...
      throw new IllegalArgumentException ();
    if ((! disableGpc) &&
      (forceGpc
       || (queue instanceof SimQueueWithGate)
       || queue.getRegisteredOperations ().contains (SimQueueWithGateOperationUtils.GatePassageCreditsOperation.getInstance ())))
    {
      final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
        ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
      final int numberOfGateEventsToSchedule = Math.max (1, jobs.size () * (jobs.size () + 1) / 11);
      final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
      final Random rngPassageCredits = new Random ();
      for (int i = 1; i <= numberOfGateEventsToSchedule; i++)
      {
        // XXX We probably need jitter on the schedule time.
        final double scheduleTime = 11.0 * i + 0.19;
        final int draw = rngPassageCredits.nextInt (4);
        final int gatePassageCredits;
        switch (draw)
        {
          case 0:
            gatePassageCredits = 0;
            break;
          case 1:
            gatePassageCredits = 1;
            break;
          case 2:
            gatePassageCredits = 5;
            break;
          case 3:
            gatePassageCredits = Integer.MAX_VALUE;
            break;
          default:
            throw new RuntimeException ();
        }
        final SimJQEvent<J, Q> gateSchedule;
        if (queue instanceof SimQueueWithGate)
          gateSchedule = new SimQueueGateEvent<> (queue, scheduleTime, gatePassageCredits);
        else
        {
          final SimQueueWithGateOperationUtils.GatePassageCreditsRequest request =
            new SimQueueWithGateOperationUtils.GatePassageCreditsRequest (queue, gatePassageCredits);
          gateSchedule = new SimQueueEvent.Operation<> (queue, scheduleTime, request);
        }
        if (! realQueueExternalEvents.containsKey (scheduleTime))
          realQueueExternalEvents.put (scheduleTime, new LinkedHashSet<> ());
        realQueueExternalEvents.get (scheduleTime).add (gateSchedule);
        eventsToSchedule.add (gateSchedule);
      }
      // Be careful not to reset the event list (again) here!
      SimJQEventScheduler.scheduleJQ (eventList, false, Double.NaN, eventsToSchedule);
    }
    return jobs;
  }
  
}
