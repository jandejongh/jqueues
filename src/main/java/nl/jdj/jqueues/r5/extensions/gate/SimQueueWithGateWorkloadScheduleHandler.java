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
package nl.jdj.jqueues.r5.extensions.gate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleHandler;

/** A {@link WorkloadScheduleHandler} for a {@link SimQueueWithGate}.
 *
 * <p>
 * Scans for and takes responsibility for (all) {@link SimQueueGateEvent} type(s) at registration,
 * and registers the {@link SimQueueWithGateSimpleEventType} simple event type(s).
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
public final class SimQueueWithGateWorkloadScheduleHandler
implements WorkloadScheduleHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WorkloadScheduleHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueWithGateHandler".
   * 
   * @return "SimQueueWithGateHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueWithGateHandler";
  }

  private final static Map<Class<? extends SimJQEvent>, SimEntitySimpleEventType.Member> EVENT_MAP = new HashMap<> ();
  
  static
  {
    SimQueueWithGateWorkloadScheduleHandler.EVENT_MAP.put (SimQueueGateEvent.class, SimQueueWithGateSimpleEventType.GATE);
  }
  
  /**
   * @see SimQueueGateEvent
   * @see SimQueueWithGateSimpleEventType
   * 
   */
  @Override
  public final Map<Class<? extends SimJQEvent>, SimEntitySimpleEventType.Member> getEventMap ()
  {
    return SimQueueWithGateWorkloadScheduleHandler.EVENT_MAP;
  }

  /** Returns {@code true}.
   * 
   * @return {@code true}.
   * 
   */
  @Override
  public final boolean needsScan ()
  {
    return true;
  }

  @Override
  public final Set<SimJQEvent> scan (final DefaultWorkloadSchedule workloadSchedule)
  throws WorkloadScheduleException
  {
    if (workloadSchedule == null)
      throw new IllegalArgumentException ();
    if (this.workloadSchedule != null)
      throw new IllegalStateException ();
    this.workloadSchedule = workloadSchedule;
    final Set<SimJQEvent> processedQueueEvents = new HashSet<> ();
    for (SimQueue q : workloadSchedule.getQueues ())
      this.gatePassageCreditsTimesMap.put (q, new TreeMap<> ());
    if (workloadSchedule.getQueueEvents () != null)
    {
      for (final SimJQEvent event : workloadSchedule.getQueueEvents ())
      {
        final double time = event.getTime ();
        final SimQueue queue = event.getQueue ();
        if (workloadSchedule.getQueues ().contains (queue))
        {
          if (event instanceof SimQueueGateEvent)
          {
            processedQueueEvents.add (event);
            final int credits = ((SimQueueGateEvent) event).getGatePassageCredits ();
            final NavigableMap<Double, List<Integer>> gateTimesMap_q = this.gatePassageCreditsTimesMap.get (queue);
            if (! gateTimesMap_q.containsKey (time))
              gateTimesMap_q.put (time, new ArrayList<> ());
            gateTimesMap_q.get (time).add (credits);
          }
        }
      }
    }
    return processedQueueEvents;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultWorkloadSchedule workloadSchedule = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<SimQueue, NavigableMap<Double, List<Integer>>> gatePassageCreditsTimesMap = new HashMap<> ();

  /** Returns the gate-passage-credits settings in time for a specific queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null}, unknown or or not in {@link DefaultWorkloadSchedule#getQueues}
   *                on the schedule we registered at,
   *                in which case an empty map is returned.
   * 
   * @return The gate-passage-credits settings in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws IllegalStateException If we are not registered at a {@link DefaultWorkloadSchedule}.
   * 
   */
  public final NavigableMap<Double, List<Integer>> getGatePassageCreditsMap (final SimQueue queue)
  {
    if (this.workloadSchedule == null)
      throw new IllegalStateException ();
    if (queue == null
      || (! this.workloadSchedule.getQueues ().contains (queue))
      || ! this.gatePassageCreditsTimesMap.containsKey (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.gatePassageCreditsTimesMap.get (queue));
  }
  
}
