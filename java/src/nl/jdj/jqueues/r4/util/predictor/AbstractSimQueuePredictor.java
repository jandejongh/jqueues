package nl.jdj.jqueues.r4.util.predictor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r4.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r4.util.predictor.workload.DefaultWorkloadSchedule_SQ_SV_ROEL_U;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadSchedule;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadScheduleAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** Partial implementation of and utility methods for {@link SimQueuePredictor}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractSimQueuePredictor<J extends SimJob, Q extends SimQueue>
implements SimQueuePredictor<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT IMPLEMENTATION OF predictVisitLogs_SQ_SV_ROEL_U
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A default implementation of {@link SimQueuePredictor#predictVisitLogs_SQ_SV_ROEL_U}.
   * 
   * <p>
   * The implementation uses a {@link SimQueueState} and a {@link WorkloadSchedule_SQ_SV_ROEL_U} as representations for
   * the queue state and the workload state, respectively, and relies on abstract (or default) methods for
   * the behavior of the specific queue type:
   * <ul>
   * <li>{@link #createQueueState} for the creation of a, possibly queue-type specific, {@link SimQueueState},
   * <li>{@link #is_ROEL_U_UnderWorkloadQueueEventClashes},
   * <li>{@link #getNextQueueEventTimeBeyond} for determining the scheduled time and type(s) of the next queue-state event(s),
   * <li>{@link #doWorkloadEvents_SQ_SV_ROEL_U} for the processing of the next workload events (like scheduled arrivals),
   * <li>{@link #doQueueEvents_SQ_SV_ROEL_U} for the processing of the next queue events (like scheduled departures).
   * </ul>
   * 
   * <p>Note that the implementation actually updates the {@link SimQueueState} object,
   * but leaves the {@link WorkloadSchedule_SQ_SV_ROEL_U} object untouched.
   * 
   * @see WorkloadSchedule#getNextEventTimeBeyond
   * 
   */
  @Override
  public Map<J, JobQueueVisitLog<J, Q>>
  predictVisitLogs_SQ_SV_ROEL_U
  (final Q queue,
   final Set<SimEntityEvent<J, Q>> queueEvents)
   throws SimQueuePredictionException
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<JobQueueVisitLog<J, Q>> visitLogsSet = new HashSet<> ();
    try
    {
      //
      // Check the set of events for SV and ROEL_U, and create a suitable workload schedule.
      //
      final WorkloadSchedule_SQ_SV_ROEL_U<J, Q> workloadSchedule
        = new DefaultWorkloadSchedule_SQ_SV_ROEL_U<> (queue, queueEvents);
      //
      // Create a state object for the queue.
      //
      final SimQueueState<J, Q> queueState = createQueueState (queue, true);
      //
      // Reset the time to Double.NaN, meaning "left of Double.MINUS_INFINITY".
      //
      double time = Double.NaN;
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
      // Main loop; proceed as long as "the workload has more load" or "the queue still has events".
      //
      while (! finished)
      {
        workloadEventTypes.clear ();
        queueEventTypes.clear ();
        final double nextWorkloadEventTime = workloadSchedule.getNextEventTimeBeyond (queue, time, workloadEventTypes);
        final double nextQueueEventTime = getNextQueueEventTimeBeyond (queue, queueState, time, queueEventTypes);
        final boolean hasWorkloadEvent = ! Double.isNaN (nextWorkloadEventTime);
        final boolean hasQueueEvent = ! Double.isNaN (nextQueueEventTime);
        final boolean doWorkloadEvent = hasWorkloadEvent && ((! hasQueueEvent) || nextWorkloadEventTime <= nextQueueEventTime);
        final boolean doQueueEvent = hasQueueEvent && ((! hasWorkloadEvent) || nextQueueEventTime <= nextWorkloadEventTime);
        if (! is_ROEL_U_UnderWorkloadQueueEventClashes (queue))
        {
          if (doWorkloadEvent && doQueueEvent)
            throw new SimQueuePredictionAmbiguityException ();
        }
        if (doQueueEvent)
        {
          doQueueEvents_SQ_SV_ROEL_U
            (queue, queueState, nextQueueEventTime, queueEventTypes, visitLogsSet);
          time = nextQueueEventTime;
        }
        if (doWorkloadEvent)
        {
          doWorkloadEvents_SQ_SV_ROEL_U
            (queue, workloadSchedule, queueState, nextWorkloadEventTime, workloadEventTypes, visitLogsSet);
          time = nextWorkloadEventTime;
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
    final Map<J, JobQueueVisitLog<J, Q>> visitLogs = new HashMap<> ();
    for (final JobQueueVisitLog<J, Q> jqvl : visitLogsSet)
    {
      if (jqvl == null)
        throw new RuntimeException ();
      if (jqvl.queue != queue)
        throw new RuntimeException ();
      if (visitLogs.containsKey (jqvl.job))
        throw new RuntimeException ();
      visitLogs.put (jqvl.job, jqvl);
    }
    return visitLogs;
  }

  /** Creates a suitable {@link SimQueueState} object for this predictor and given queue.
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
  protected SimQueueState<J, Q> createQueueState (final Q queue, final boolean isROEL)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (! isROEL)
      throw new UnsupportedOperationException ();
    return new DefaultSimQueueState<> (queue);
  }
  
  /** Returns whether the given queue, which must be a valid input to this predictor,
   *  gives unambiguous predictions under a ROEL even if workload and queue-state events
   *  occur simultaneously.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @return True if the queue gives unambiguous predictions under a ROEL even if workload and queue-state events
   *         occur simultaneously. This default implementation returns {@code false}.
   * 
   * @throws IllegalArgumentException If {@code queue == null}.
   * 
   */
  protected boolean is_ROEL_U_UnderWorkloadQueueEventClashes (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    return false;
  }
  
  /** Returns the time and types of the next event(s)
   *  scheduled strictly beyond a given time at a specific queue.
   * 
   * @param queue           The queue, non-{@code null}.
   * @param queueState      The queue-state, non-{@code null}
   * @param time            The time from which to search, use {@link Double#NaN} to retrieve the first-event time (which
   *                          may be {@link Double#NEGATIVE_INFINITY}).
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
  protected abstract double getNextQueueEventTimeBeyond
  (Q queue,
   SimQueueState<J, Q> queueState,
   double time,
   Set<SimEntitySimpleEventType.Member> queueEventTypes)
   throws SimQueuePredictionException;
  
  /** Process the next event(s) from given {@link WorkloadSchedule} at a queue with given state.
   * 
   * <p>
   * The scheduled time and the types of the next events must be known beforehand,
   * e.g., through {@link WorkloadSchedule#getNextEventTimeBeyond}.
   * 
   * <p>
   * Implementations must update the queue state and (if applicable) add suitable entries to the visit logs.
   * 
   * <p>
   * Implementations must <i>not</i> modify the workload schedule.
   * 
   * @param queue                 The queue, non-{@code null}.
   * @param workloadSchedule      The workload schedule, non-{@code null}.
   * @param queueState            The queue-state, non-{@code null}
   * @param nextWorkloadEventTime The (pre-calculated) time of the next workload event(s).
   * @param workloadEventTypes    The (pre-calculated) types of the next workload event(s).
   * @param visitLogsSet          The visit logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException    If any of the mandatory input arguments is {@code null}.
   * @throws SimQueuePredictionException If the result cannot be computed, e.g., due to invalid input or schedule ambiguities.
   * @throws WorkloadScheduleException   If the workload is invalid (e.g., containing ambiguities).
   * 
   * @see WorkloadSchedule#getNextEventTimeBeyond
   * 
   */
  protected abstract void doWorkloadEvents_SQ_SV_ROEL_U
  (Q queue,
   WorkloadSchedule_SQ_SV_ROEL_U<J, Q> workloadSchedule,
   SimQueueState<J, Q> queueState,
   double nextWorkloadEventTime,
   Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   Set<JobQueueVisitLog<J, Q>> visitLogsSet)
   throws SimQueuePredictionException, WorkloadScheduleException;
  
  /** Process the next event(s) at a queue with given state.
   * 
   * <p>
   * The scheduled time and the types of the next events must be known beforehand,
   * e.g., through {@link #getNextQueueEventTimeBeyond}.
   * 
   * <p>
   * Implementations must update the queue state and (if applicable) add suitable entries to the visit logs.
   * 
   * @param queue              The queue, non-{@code null}.
   * @param queueState         The queue-state, non-{@code null}
   * @param nextQueueEventTime The (pre-calculated) time of the next queue event(s).
   * @param queueEventTypes    The (pre-calculated) types of the next workload event(s).
   * @param visitLogsSet       The visit logs, non-{@code null}.
   * 
   * @throws IllegalArgumentException    If any of the mandatory input arguments is {@code null}.
   * @throws SimQueuePredictionException If the result cannot be computed, e.g., due to invalid input or schedule ambiguities.
   * 
   */
  protected abstract void doQueueEvents_SQ_SV_ROEL_U
  (Q queue,
   SimQueueState<J, Q> queueState,
   double nextQueueEventTime,
   Set<SimEntitySimpleEventType.Member> queueEventTypes,
   Set<JobQueueVisitLog<J, Q>> visitLogsSet)
   throws SimQueuePredictionException;
  
}
