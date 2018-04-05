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
package nl.jdj.jqueues.r5.util.predictor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule_SQ_SV_ROEL_U;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** An object capable of predicting the behavior of one or more {@link SimQueue}s under external and internal events.
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimQueuePredictor
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
public interface SimQueueEventPredictor<Q extends SimQueue>
{
  
  /** Creates and prepares a suitable {@link WorkloadSchedule_SQ_SV_ROEL_U} object for this predictor and given queue,
   *  for a given set of workload events.
   * 
   * <p>
   * The initial time must be set to {@link Double#NaN}.
   * 
   * <p>
   * Implementations must prepare the required maps from the {@link WorkloadSchedule_SQ_SV_ROEL_U} at construction.
   * 
   * <p>
   * The default implementation returns a new {@link DefaultWorkloadSchedule_SQ_SV_ROEL_U}.
   * 
   * @param queue          The queue, non-{@code null}.
   * @param workloadEvents The workload events, may be {@code null} or empty.
   * 
   * @return A new suitable {@link WorkloadSchedule_SQ_SV_ROEL_U} object for this predictor and given queue.
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous (for instance).
   * 
   */
  public default
  WorkloadSchedule_SQ_SV_ROEL_U
  createWorkloadSchedule_SQ_SV_ROEL_U
  (final Q queue, final Set<SimJQEvent> workloadEvents)
  throws WorkloadScheduleException
  {
    return new DefaultWorkloadSchedule_SQ_SV_ROEL_U (queue, workloadEvents);
  }
  
  /** Creates and prepares a suitable {@link WorkloadSchedule_SQ_SV} object for this predictor and given queue,
   *  for a given map of event times onto workload events.
   * 
   * <p>
   * The initial time must be set to {@link Double#NaN}.
   * 
   * <p>
   * Implementations must prepare the required maps from the {@link WorkloadSchedule_SQ_SV} at construction.
   * 
   * <p>
   * The default implementation returns a new {@link DefaultWorkloadSchedule_SQ_SV}.
   * 
   * @param queue             The queue, non-{@code null}.
   * @param workloadEventsMap The workload events, may be {@code null} or empty.
   * 
   * @return A new suitable {@link WorkloadSchedule_SQ_SV} object for this predictor and given queue.
   * 
   * @throws WorkloadScheduleException If the workload is invalid (for instance).
   * 
   */
  public default
  WorkloadSchedule_SQ_SV
  createWorkloadSchedule_SQ_SV
  (final Q queue,
   final Map<Double, Set<SimJQEvent>> workloadEventsMap)
  throws WorkloadScheduleException
  {
    return new DefaultWorkloadSchedule_SQ_SV (queue, workloadEventsMap);
  }
  
  /** Creates a suitable {@link SimQueueState} object for this predictor and given queue.
   * 
   * <p>
   * The initial time must be set to {@link Double#NaN}.
   * 
   * @param queue  The queue, non-{@code null}.
   * @param isROEL Whether or not the event list used is a Random-Order Event List.
   * 
   * @return A new suitable {@link SimQueueState} object for this predictor and given queue.
   * 
   * @throws IllegalArgumentException      If {@code queue == null}.
   * @throws UnsupportedOperationException If {@code isROEL == true}, because the default implementation does not
   *                                       support non-ROEL event lists.
   * 
   */
  public default
  SimQueueState<SimJob, Q>
  createQueueState
  (final Q queue, final boolean isROEL)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (! isROEL)
      throw new UnsupportedOperationException ();
    return new DefaultSimQueueState<> (queue);
  }
  
  /** Returns the time and types of the next event(s)
   *  scheduled strictly beyond the time at (the state object of) a specific queue.
   * 
   * <p>
   * The time from which to search must be taken from {@link SimQueueState#getTime},
   * and equals {@link Double#NaN} after initialization.
   * 
   * @param queue           The queue, non-{@code null}.
   * @param queueState      The queue-state, non-{@code null}.
   * @param queueEventTypes A non-{@code null} set to store the (possible multiple) event types; it must be cleared upon entry.
   * 
   * @return The time of the next event, or {@link Double#NaN} if no such event exists.
   * 
   * @see SimQueueSimpleEventType#START
   * @see SimQueueSimpleEventType#DEPARTURE
   * 
   * @throws IllegalArgumentException    If any of the input arguments is {@code null}.
   * @throws SimQueuePredictionException If the result cannot be computed, e.g., due to invalid input or schedule ambiguities.
   * 
   */
  double getNextQueueEventTimeBeyond
  (Q queue, SimQueueState<SimJob, Q> queueState, Set<SimEntitySimpleEventType.Member> queueEventTypes)
   throws SimQueuePredictionException;
  
  /** Updates the queue state to a new time, without processing any events.
   * 
   * <p>
   * Implementations must at least set the time on the queue state.
   * Beware that the old time on the queue state may equal its initial value {@link Double#NaN}.
   * 
   * @param queue      The queue, non-{@code null}.
   * @param queueState The queue-state, non-{@code null}.
   * @param newTime    The new time.
   * 
   * @throws IllegalArgumentException If the queue or queue state is {@code null},
   *                                  the new time is in the past,
   *                                  or the new time equals {@link Double#NaN}.
   * 
   * @see SimQueueState#setTime
   * 
   */
  void updateToTime (Q queue, SimQueueState queueState, double newTime);
  
  /** Process the next event(s) from given {@link WorkloadSchedule} at a queue with given state.
   * 
   * <p>
   * The scheduled time and the types of the next events must be known beforehand,
   * e.g., through {@link WorkloadSchedule#getNextEventTimeBeyond}.
   * The scheduled time has already been set on the {@link SimQueueState} object,
   * and the object has been updated upto that time.
   * The time on the queue state must not be changed.
   * 
   * <p>
   * Implementations must update the queue state and (if applicable) add suitable entries to the visit logs.
   * 
   * <p>
   * Implementations must <i>not</i> modify the workload schedule.
   * 
   * @param queue              The queue, non-{@code null}.
   * @param workloadSchedule   The workload schedule, non-{@code null}.
   * @param queueState         The queue-state, non-{@code null}.
   * @param workloadEventTypes The (pre-calculated) types of the next workload event(s).
   * @param visitLogsSet       The visit logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException    If any of the mandatory input arguments is {@code null}.
   * @throws SimQueuePredictionException If the result cannot be computed, e.g., due to invalid input or schedule ambiguities.
   * @throws WorkloadScheduleException   If the workload is invalid (e.g., containing ambiguities).
   * 
   * @see WorkloadSchedule#getNextEventTimeBeyond
   * @see #updateToTime
   * @see SimQueueState#setTime
   * 
   */
  void doWorkloadEvents_SQ_SV_ROEL_U
  (Q queue,
   WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   SimQueueState<SimJob, Q> queueState,
   Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException, WorkloadScheduleException;
  
  /** Process the next event(s) from given {@link WorkloadSchedule} at a queue with given state under IOEL.
   * 
   * <p>
   * The scheduled time and the types of the next events must be known beforehand,
   * e.g., through {@link WorkloadSchedule#getNextEventTimeBeyond}.
   * The scheduled time has already been set on the {@link SimQueueState} object,
   * and the object has been updated upto that time.
   * The time on the queue state must not be changed.
   * 
   * <p>
   * Implementations must update the queue state and (if applicable) add suitable entries to the visit logs.
   * 
   * <p>
   * The default implementation creates a workload schedule for each individual event
   * through {@link #createWorkloadSchedule_SQ_SV_ROEL_U}
   * and has it processed
   * through {@link #doWorkloadEvents_SQ_SV_ROEL_U}.
   * 
   * <p>
   * Implementations must <i>not</i> modify the workload schedule.
   * 
   * @param queue              The queue, non-{@code null}.
   * @param workloadSchedule   The workload schedule, non-{@code null}.
   * @param queueState         The queue-state, non-{@code null}.
   * @param visitLogsSet       The visit logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException    If any of the mandatory input arguments is {@code null}.
   * @throws SimQueuePredictionException If the result cannot be computed, e.g., due to invalid input or schedule ambiguities.
   * @throws WorkloadScheduleException   If the workload is invalid (e.g., containing ambiguities).
   * 
   * @see WorkloadSchedule#getNextEventTimeBeyond
   * @see #updateToTime
   * @see SimQueueState#setTime
   * 
   */
  public default void
  doWorkloadEvents_SQ_SV
  (final Q queue,
   final WorkloadSchedule_SQ_SV workloadSchedule,
   final SimQueueState queueState,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException, WorkloadScheduleException
  {
    if ( queue == null
      || workloadSchedule == null
      || queueState == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final Set<SimJQEvent> events = workloadSchedule.getSimQueueTimeSimEntityEventMap ().get (queue).get (time);
    for (final SimJQEvent event : events)
    {
      final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule_SQ_SV_ROEL_U =
        createWorkloadSchedule_SQ_SV_ROEL_U (queue, Collections.singleton (event));
      final Set<SimEntitySimpleEventType.Member> workloadEventTypes = new HashSet<> ();
      workloadSchedule_SQ_SV_ROEL_U.getNextEventTimeBeyond (queue, Double.NaN, workloadEventTypes);
      doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule_SQ_SV_ROEL_U, queueState, workloadEventTypes, visitLogsSet);
    }
  }
  
  /** Process the next event(s) at a queue with given state.
   * 
   * <p>
   * The scheduled time and the types of the next events must be known beforehand,
   * e.g., through {@link #getNextQueueEventTimeBeyond}.
   * The scheduled time has already been set on the {@link SimQueueState} object,
   * and the object has been updated upto that time.
   * The time on the queue state must not be changed.
   * 
   * <p>
   * Implementations must update the queue state and (if applicable) add suitable entries to the visit logs.
   * 
   * @param queue           The queue, non-{@code null}.
   * @param queueState      The queue-state, non-{@code null}
   * @param queueEventTypes The (pre-calculated) types of the next workload event(s).
   * @param visitLogsSet    The visit logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException    If any of the mandatory input arguments is {@code null}.
   * @throws SimQueuePredictionException If the result cannot be computed, e.g., due to invalid input or schedule ambiguities.
   * 
   * @see #getNextQueueEventTimeBeyond
   * @see #updateToTime
   * @see SimQueueState#setTime
   * 
   */
  void doQueueEvents_SQ_SV_ROEL_U
  (Q queue,
   SimQueueState<SimJob, Q> queueState,
   Set<SimEntitySimpleEventType.Member> queueEventTypes,
   Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException;
  
}
