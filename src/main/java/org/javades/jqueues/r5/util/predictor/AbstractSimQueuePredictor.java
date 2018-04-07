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
package org.javades.jqueues.r5.util.predictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.LJF;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.NoBuffer_c;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.SJF;
import org.javades.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleAmbiguityException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV;
import org.javades.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** Partial implementation of and utility methods for {@link SimQueuePredictor}.
 * 
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
public abstract class AbstractSimQueuePredictor<Q extends SimQueue>
implements SimQueuePredictor<Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT IMPLEMENTATION OF predict_SQ_SV_ROEL_U
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A default implementation of {@link SimQueuePredictor#predict_SQ_SV_ROEL_U}.
   * 
   * <p>
   * The implementation uses a {@link SimQueueState} and a {@link WorkloadSchedule_SQ_SV_ROEL_U} as representations for
   * the queue state and the workload state, respectively, and relies on abstract (or default) methods for
   * the behavior of the specific queue type:
   * <ul>
   * <li>{@link #createWorkloadSchedule_SQ_SV_ROEL_U} for the creation of a, possibly queue-type specific,
   *            {@link WorkloadSchedule_SQ_SV_ROEL_U} from the workload events,
   * <li>{@link #createQueueState} for the creation of a, possibly queue-type specific, {@link SimQueueState},
   * <li>{@link #is_ROEL_U_UnderWorkloadQueueEventClashes},
   * <li>{@link #getNextQueueEventTimeBeyond} for determining the scheduled time and type(s) of the next queue-state event(s),
   * <li>{@link #updateToTime} for progressing time on the queue state without processing events,
   * <li>{@link #doWorkloadEvents_SQ_SV_ROEL_U} for the processing of the next workload events (like scheduled arrivals),
   * <li>{@link #doQueueEvents_SQ_SV_ROEL_U} for the processing of the next queue events (like scheduled departures).
   * </ul>
   * 
   * <p>
   * Note that the implementation actually updates the {@link SimQueueState} object,
   * but leaves the {@link WorkloadSchedule_SQ_SV_ROEL_U} object untouched.
   * 
   * @see WorkloadSchedule#getNextEventTimeBeyond
   * 
   */
  @Override
  public SimQueuePrediction_SQ_SV<Q>
  predict_SQ_SV_ROEL_U
  (final Q queue,
   final Set<SimJQEvent> workloadEvents)
  throws SimQueuePredictionException
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet = new HashSet<> ();
    final List<Map<Double, Boolean>> qavLog = new ArrayList<> ();
    final List<Map<Double, Boolean>> sacLog = new ArrayList<> ();
    final List<Map<Double, Boolean>> staLog = new ArrayList<> ();
    try
    {
      //
      // Check the set of events for SV and ROEL_U, and create a suitable workload schedule.
      //
      final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule = createWorkloadSchedule_SQ_SV_ROEL_U (queue, workloadEvents);
      //
      // Create a state object for the queue.
      //
      final SimQueueState<SimJob, Q> queueState = createQueueState (queue, true);
      if (! Double.isNaN (queueState.getTime ()))
        throw new RuntimeException ();
      //
      // Create sets to hold the (simple) event types from the workload and from the queue.
      //
      final Set<SimEntitySimpleEventType.Member> workloadEventTypes = new HashSet<> ();
      final Set<SimEntitySimpleEventType.Member> queueEventTypes = new HashSet<> ();
      //
      // A boolean to indicate when we are done.
      //
      boolean finished = false;
      //
      // Maintain the current QAV state of the queue.
      //
      boolean isQav = false;
      //
      // Maintain the current SAC-availability state of the queue.
      //
      boolean hasSac = hasServerAccessCredits (queue, queueState);
      //
      // Maintain the current STA state of the queue.
      //
      boolean isSta = isStartArmed (queue, queueState);
      //
      // Main loop; proceed as long as "the workload has more load" or "the queue still has events".
      //
      while (! finished)
      {
        final double time = queueState.getTime ();
        workloadEventTypes.clear ();
        queueEventTypes.clear ();
        final double nextWorkloadEventTime = workloadSchedule.getNextEventTimeBeyond (queue, time, workloadEventTypes);
        final double nextQueueEventTime = getNextQueueEventTimeBeyond (queue, queueState, queueEventTypes);
        final boolean hasWorkloadEvent = ! Double.isNaN (nextWorkloadEventTime);
        final boolean hasQueueEvent = ! Double.isNaN (nextQueueEventTime);
        if (hasWorkloadEvent)
        {
          if (workloadEventTypes.isEmpty ())
            throw new RuntimeException ();
          if (workloadEventTypes.size () > 1)
            throw new SimQueuePredictionAmbiguityException ();
        }
        if (hasQueueEvent)
        {
          if (queueEventTypes.isEmpty ())
            throw new RuntimeException ();
          if (queueEventTypes.size () > 1)
            throw new SimQueuePredictionAmbiguityException ();
        }
        final boolean doWorkloadEvent = hasWorkloadEvent && ((! hasQueueEvent) || nextWorkloadEventTime <= nextQueueEventTime);
        final boolean doQueueEvent = hasQueueEvent && ((! hasWorkloadEvent) || nextQueueEventTime <= nextWorkloadEventTime);
        final double nextEventTime = (doWorkloadEvent ? nextWorkloadEventTime : (doQueueEvent ? nextQueueEventTime : Double.NaN));
        if (doWorkloadEvent || doQueueEvent)
        {
          updateToTime (queue, queueState, nextEventTime);
          if (queueState.getTime () != nextEventTime)
            throw new RuntimeException ();
          if (isQueueAccessVacation (queue, queueState) != isQav)
            throw new RuntimeException ();
          if (hasServerAccessCredits (queue, queueState) != hasSac)
            throw new RuntimeException ();
          if (isStartArmed (queue, queueState) != isSta)
            throw new RuntimeException ();
        }
        if (doWorkloadEvent && doQueueEvent)
        {
          if (! is_ROEL_U_UnderWorkloadQueueEventClashes
                    (queue, queueState, workloadSchedule, workloadEventTypes, queueEventTypes))
            throw new SimQueuePredictionAmbiguityException ();
        }
        if (doQueueEvent)
          doQueueEvents_SQ_SV_ROEL_U (queue, queueState, queueEventTypes, visitLogsSet);
        if (doWorkloadEvent)
          doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule, queueState, workloadEventTypes, visitLogsSet);
        if ((doWorkloadEvent || doQueueEvent) && queueState.getTime () != nextEventTime)
          throw new RuntimeException ();
        if (isQueueAccessVacation (queue, queueState) != isQav)
        {
          isQav = ! isQav;
          qavLog.add (Collections.singletonMap (nextEventTime, isQav));
        }
        if (hasServerAccessCredits (queue, queueState) != hasSac)
        {
          hasSac = ! hasSac;
          sacLog.add (Collections.singletonMap (nextEventTime, hasSac));
        }
        if (isStartArmed (queue, queueState) != isSta)
        {
          isSta = ! isSta;
          staLog.add (Collections.singletonMap (nextEventTime, isSta));
        }
        finished = ! (hasWorkloadEvent || hasQueueEvent);
      }
    }
    catch (WorkloadScheduleAmbiguityException wsae)
    {
      throw new SimQueuePredictionAmbiguityException (wsae);
    }
    catch (WorkloadScheduleException wse)
    {
      throw new SimQueuePredictionInvalidInputException (wse);
    }
    final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs = new HashMap<> ();
    for (final JobQueueVisitLog<SimJob, Q> jqvl : visitLogsSet)
    {
      if (jqvl == null)
        throw new RuntimeException ();
      if (jqvl.queue != queue)
        throw new RuntimeException ();
      if (visitLogs.containsKey (jqvl.job))
        throw new RuntimeException ();
      visitLogs.put (jqvl.job, jqvl);
    }
    return new DefaultSimQueuePrediction_SQ_SV<> (queue, visitLogs, qavLog, sacLog, staLog);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT IMPLEMENTATION OF predict_SQ_SV_IOEL_U
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** A default implementation of {@link SimQueuePredictor#predict_SQ_SV_IOEL_U}.
   * 
   * <p>
   * The implementation uses a {@link SimQueueState} and a {@link WorkloadSchedule_SQ_SV_ROEL_U} as representations for
   * the queue state and the workload state, respectively, and relies on abstract (or default) methods for
   * the behavior of the specific queue type:
   * <ul>
   * <li>{@link #createWorkloadSchedule_SQ_SV} for the creation of a, possibly queue-type specific,
   *            {@link WorkloadSchedule_SQ_SV} from the workload events,
   * <li>{@link #createQueueState} for the creation of a, possibly queue-type specific, {@link SimQueueState},
   * <li>{@link #is_U_UnderWorkloadQueueEventClashes},
   * <li>{@link #getNextQueueEventTimeBeyond} for determining the scheduled time and type(s) of the next queue-state event(s),
   * <li>{@link #updateToTime} for progressing time on the queue state without processing events,
   * <li>{@link #doWorkloadEvents_SQ_SV} for the processing of the next workload events (like scheduled arrivals),
   * <li>{@link #doQueueEvents_SQ_SV_ROEL_U} for the processing of the next queue events (like scheduled departures).
   * </ul>
   * 
   * <p>
   * Note that the implementation actually updates the {@link SimQueueState} object,
   * but leaves the {@link WorkloadSchedule_SQ_SV} object untouched.
   * 
   * @see WorkloadSchedule#getNextEventTimeBeyond
   * 
   */
  @Override
  public SimQueuePrediction_SQ_SV<Q>
  predict_SQ_SV_IOEL_U
  (final Q queue,
   final NavigableMap<Double, Set<SimJQEvent>> workloadEventsMap,
   final NavigableMap<Double, Set<SimJQEvent>> processedEventsMap)
  throws SimQueuePredictionException
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet = new HashSet<> ();
    final List<Map<Double, Boolean>> qavLog = new ArrayList<> ();
    final List<Map<Double, Boolean>> sacLog = new ArrayList<> ();
    final List<Map<Double, Boolean>> staLog = new ArrayList<> ();
    try
    {
      //
      // Check the set of events for SV and ROEL_U, and create a suitable workload schedule.
      //
      final WorkloadSchedule_SQ_SV workloadSchedule = createWorkloadSchedule_SQ_SV (queue, workloadEventsMap);
      //
      // Create a state object for the queue.
      //
      final SimQueueState<SimJob, Q> queueState = createQueueState (queue, true);
      if (! Double.isNaN (queueState.getTime ()))
        throw new RuntimeException ();
      //
      // Create sets to hold the (simple) event types from the workload and from the queue.
      //
      final Set<SimEntitySimpleEventType.Member> workloadEventTypes = new HashSet<> ();
      final Set<SimEntitySimpleEventType.Member> queueEventTypes = new HashSet<> ();
      //
      // A boolean to indicate when we are done.
      //
      boolean finished = false;
      //
      // Maintain the current QAV state of the queue.
      //
      boolean isQav = false;
      //
      // Maintain the current SAC-availability state of the queue.
      //
      boolean hasSac = hasServerAccessCredits (queue, queueState);
      //
      // Maintain the current STA state of the queue.
      //
      boolean isSta = isStartArmed (queue, queueState);
      //
      // Main loop; proceed as long as "the workload has more load" or "the queue still has events".
      //
      while (! finished)
      {
        final double time = queueState.getTime ();
        workloadEventTypes.clear ();
        queueEventTypes.clear ();
        final double nextWorkloadEventTime = workloadSchedule.getNextEventTimeBeyond (queue, time, workloadEventTypes);
        final double nextQueueEventTime = getNextQueueEventTimeBeyond (queue, queueState, queueEventTypes);
        final boolean hasWorkloadEvent = ! Double.isNaN (nextWorkloadEventTime);
        final boolean hasQueueEvent = ! Double.isNaN (nextQueueEventTime);
        if (hasWorkloadEvent)
        {
          if (workloadEventTypes.isEmpty ())
            throw new RuntimeException ();
        }
        if (hasQueueEvent)
        {
          if (queueEventTypes.isEmpty ())
            throw new RuntimeException ();
          if (queueEventTypes.size () > 1)
            throw new SimQueuePredictionAmbiguityException ();
        }
        final boolean doWorkloadEvent = hasWorkloadEvent && ((! hasQueueEvent) || nextWorkloadEventTime <= nextQueueEventTime);
        final boolean doQueueEvent = hasQueueEvent && ((! hasWorkloadEvent) || nextQueueEventTime <= nextWorkloadEventTime);
        final double nextEventTime = (doWorkloadEvent ? nextWorkloadEventTime : (doQueueEvent ? nextQueueEventTime : Double.NaN));
        if (doWorkloadEvent || doQueueEvent)
        {
          updateToTime (queue, queueState, nextEventTime);
          if (queueState.getTime () != nextEventTime)
            throw new RuntimeException ();
          if (isQueueAccessVacation (queue, queueState) != isQav)
            throw new RuntimeException ();
          if (hasServerAccessCredits (queue, queueState) != hasSac)
            throw new RuntimeException ();
          if (isStartArmed (queue, queueState) != isSta)
            throw new RuntimeException ();
        }
        if (doWorkloadEvent && doQueueEvent)
        {
          if (! is_U_UnderWorkloadQueueEventClashes
                  (queue, queueState, workloadSchedule, queueEventTypes))
            throw new SimQueuePredictionAmbiguityException ();
        }
        if (doQueueEvent)
          doQueueEvents_SQ_SV_ROEL_U (queue, queueState, queueEventTypes, visitLogsSet);
        if (doWorkloadEvent)
          doWorkloadEvents_SQ_SV (queue, workloadSchedule, queueState, visitLogsSet);
        if ((doWorkloadEvent || doQueueEvent) && queueState.getTime () != nextEventTime)
          throw new RuntimeException ();
        if (isQueueAccessVacation (queue, queueState) != isQav)
        {
          isQav = ! isQav;
          qavLog.add (Collections.singletonMap (nextEventTime, isQav));
        }
        if (hasServerAccessCredits (queue, queueState) != hasSac)
        {
          hasSac = ! hasSac;
          sacLog.add (Collections.singletonMap (nextEventTime, hasSac));
        }
        if (isStartArmed (queue, queueState) != isSta)
        {
          isSta = ! isSta;
          staLog.add (Collections.singletonMap (nextEventTime, isSta));
        }
        finished = ! (hasWorkloadEvent || hasQueueEvent);
      }
    }
    catch (WorkloadScheduleAmbiguityException wsae)
    {
      throw new SimQueuePredictionAmbiguityException (wsae);
    }
    catch (WorkloadScheduleException wse)
    {
      throw new SimQueuePredictionInvalidInputException (wse);
    }
    final Map<SimJob, JobQueueVisitLog<SimJob, Q>> visitLogs = new HashMap<> ();
    for (final JobQueueVisitLog<SimJob, Q> jqvl : visitLogsSet)
    {
      if (jqvl == null)
        throw new RuntimeException ();
      if (jqvl.queue != queue)
        throw new RuntimeException ();
      if (visitLogs.containsKey (jqvl.job))
        throw new RuntimeException ();
      visitLogs.put (jqvl.job, jqvl);
    }
    return new DefaultSimQueuePrediction_SQ_SV<> (queue, visitLogs, qavLog, sacLog, staLog);
  }

  /** Check unambiguity under a ROEL for workload and queue-state events occurring simultaneously.
   * 
   * @param queue              The queue, non-{@code null}.
   * @param queueState         The queue-state, non-{@code null}.
   * @param workloadSchedule   The workload schedule, non-{@code null}.
   * @param workloadEventTypes The types of the workload event(s).
   * @param queueEventTypes    The types of the workload event(s).
   * 
   * @return True if this object gives unambiguous predictions under a ROEL for given simultaneous events.
   * 
   * @throws IllegalArgumentException If any of the input arguments is {@code null} or,
   *                                  for the sets, empty or containing {@code null},
   *                                  or if the time on the queue state is invalid.
   * 
   */
  protected boolean is_ROEL_U_UnderWorkloadQueueEventClashes
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if (queue == null
      || queueState == null
      || Double.isNaN (queueState.getTime ())
      || workloadSchedule == null
      || workloadEventTypes == null
      || workloadEventTypes.isEmpty ()
      || workloadEventTypes.contains (null)
      || queueEventTypes == null
      || queueEventTypes.isEmpty ()
      || queueEventTypes.contains (null))
      throw new IllegalArgumentException ();
    for (final SimEntitySimpleEventType.Member wEvent : workloadEventTypes)
      for (final SimEntitySimpleEventType.Member qEvent : queueEventTypes)
        if (wEvent == SimQueueSimpleEventType.REVOCATION
        &&  qEvent == SimQueueSimpleEventType.DEPARTURE)
          return false;
        else if (wEvent == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS
             &&  (qEvent == SimQueueSimpleEventType.DEPARTURE || qEvent == SimQueueSimpleEventType.START))
          return false;
        else if (wEvent == SimQueueSimpleEventType.ARRIVAL
             &&  (qEvent == SimQueueSimpleEventType.DEPARTURE || qEvent == SimQueueSimpleEventType.START)
             && ((queue instanceof FCFS_B) 
              || (queue instanceof LCFS)
              || (queue instanceof NoBuffer_c)
              || (queue instanceof SJF)
              || (queue instanceof LJF)
              || (queue instanceof P_LCFS)))
          return false;
    return true;
  }
  
  /** Check unambiguity under a IOEL for workload and ROEL for queue-state events occurring simultaneously.
   * 
   * @param queue              The queue, non-{@code null}.
   * @param queueState         The queue-state, non-{@code null}.
   * @param workloadSchedule   The workload schedule, non-{@code null}.
   * @param queueEventTypes    The types of the workload event(s).
   * 
   * @return True if this object gives unambiguous predictions under a IOEL for workload and ROEL for queue events.
   * 
   * @throws IllegalArgumentException If any of the input arguments is {@code null} or,
   *                                  for the sets, empty or containing {@code null},
   *                                  or if the time on the queue state is invalid.
   * 
   */
  protected boolean is_U_UnderWorkloadQueueEventClashes
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final WorkloadSchedule_SQ_SV workloadSchedule,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if (queue == null
      || queueState == null
      || Double.isNaN (queueState.getTime ())
      || workloadSchedule == null
      || queueEventTypes == null
      || queueEventTypes.isEmpty ()
      || queueEventTypes.contains (null))
      throw new IllegalArgumentException ();
    final double time = queueState.getTime ();
    final Set<SimJQEvent> events = workloadSchedule.getSimQueueTimeSimEntityEventMap ().get (queue).get (time);
    try
    {
      for (final SimJQEvent event : events)
      {
        final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule_SQ_SV_ROEL_U =
          createWorkloadSchedule_SQ_SV_ROEL_U (queue, Collections.singleton (event));
        final Set<SimEntitySimpleEventType.Member> workloadEventTypes = new HashSet<> ();
        workloadSchedule_SQ_SV_ROEL_U.getNextEventTimeBeyond (queue, Double.NaN, workloadEventTypes);
        if (! is_ROEL_U_UnderWorkloadQueueEventClashes
                (queue, queueState, workloadSchedule_SQ_SV_ROEL_U, workloadEventTypes, queueEventTypes))
          return false;
      }
      return true;
    }
    catch (WorkloadScheduleException wse)
    {
      throw new RuntimeException (wse);
    }
  }
  
}
