package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.map.SimEntityEventMap;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;

/** A representation of a schedule of workload and state-setting events for a set of {@link SimQueue}s.
 *
 * <p>
 * The set of {@link SimQueue}s to which the workload applies must be fixed upon construction.
 * 
 * 
 */
public interface WorkloadSchedule
extends SimEntityEventMap
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the underlying queue events (all) of this workload representation.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @return The underlying queue events (all) of this workload representation,
   *         ordered as found in the source upon construction.
   * 
   */
  public Set<SimEntityEvent> getQueueEvents ();

  /** Returns the time of the next event(s), and optionally their types,
   *  scheduled strictly beyond a given time at a specific queue.
   * 
   * @param queue      The queue, may be {@code null} or not in {@link #getQueues}, in which case {@link Double#NaN} is returned.
   * @param time       The time from which to search, use {@link Double#NaN} to retrieve the first-event time (which
   *                     may be {@link Double#NEGATIVE_INFINITY}).
   * @param eventTypes An optional set to store the (possible multiple) event types; a non-{@code null} set is cleared upon entry.
   * 
   * @return The time of the next event, or {@link Double#NaN} if no such event exists.
   * 
   * @throws WorkloadScheduleException If the workload is invalid, or this schedule is incapable of parsing it (completely).
   * 
   * @see #hasEventsBeyond
   * 
   */
  public abstract double getNextEventTimeBeyond
  (SimQueue queue,
   double time,
   Set<SimEntitySimpleEventType.Member> eventTypes)
   throws WorkloadScheduleException;
  
  /** Returns whether there exist event(s) scheduled strictly beyond a given time at a specific queue.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case {@code false} is returned.
   * @param time  The time from which to search, use {@link Double#NaN} to retrieve the first-event time (which
   *              may be {@link Double#NEGATIVE_INFINITY}).
   * 
   * @return True if there exist event(s) strictly beyond a given time.
   * 
   * @throws WorkloadScheduleException If the workload is invalid, or this schedule is incapable of parsing it (completely).
   * 
   * @see #getNextEventTimeBeyond
   * 
   */
  public default boolean hasEventsBeyond (final SimQueue queue, final double time)
  throws WorkloadScheduleException
  {
    return ! Double.isNaN (getNextEventTimeBeyond (queue, time, null));
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESSED QUEUE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the underlying queue events that were processed for this workload representation.
   * 
   * <p>
   * The set returned must always be a subset of {@link #getQueueEvents}.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @return The underlying queue events that were processed for this workload representation,
   *         in no particular specified order.
   * 
   */
  public Set<SimEntityEvent> getProcessedQueueEvents ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the queues (in no particular order) to which this workload representation applies.
   * 
   * <p>
   * The set must be fixed upon construction.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @return The queue (in no particular order) to which this workload representation applies.
   * 
   */
  public Set<SimQueue> getQueues ();
  
  /** Returns whether of not this workload describes a single queue only.
   * 
   * @return Whether this workload describes a single queue only.
   * 
   */
  public default boolean isSingleQueue ()
  {
    return getQueues ().size () == 1;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the queue-access vacation settings in time for a specific queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty map is returned.
   * 
   * @return The queue-access vacation settings in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   */
  public NavigableMap<Double, List<Boolean>> getQueueAccessVacationMap (SimQueue queue);

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the jobs (in no particular order) operating (arriving, revoking, ...) at least once at <i>any</i> of the queues.
   * 
   * <p>
   * Note that this set may include jobs that did not <i>arrive</i> at any queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @return The jobs (in no particular order) in a non-{@code null} set
   *         operating (arriving, revoking, ...) at least once at <i>any</i> of the queues.
   * 
   */
  public Set<SimJob> getJobs ();

  /** Gets the jobs (in no particular order) operating (arriving, revoking, ...) at least once at given queue.
   * 
   * <p>
   * The set returned must always be a subset of {@link #getJobs(SimQueue)}.
   * 
   * <p>
   * Note that this set may include jobs that did not <i>arrive</i> at the queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty set is returned.
   * 
   * @return The jobs (in no particular order) in a non-{@code null} set
   *         operating (arriving, revoking, ...) at least once at given queue.
   * 
   */
  public Set<SimJob> getJobs (SimQueue queue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the arrival times indexed by job at given queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty map is returned.
   * 
   * @return The job arrival times indexed by job and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   */
  public Map<SimJob, List<Double>> getArrivalTimesMap (SimQueue queue);
  
  /** Gets the job arrivals indexed by time at given queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty map is returned.
   * 
   * @return The job arrivals in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   */
  public NavigableMap<Double, List<SimJob>> getJobArrivalsMap (SimQueue queue);
  
  /** Return whether jobs arrive at most once at given queue.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case {@code false} is returned.
   * 
   * @return Whether jobs arrive at most once at given queue.
   * 
   */
  public default boolean isSingleVisit (final SimQueue queue)
  {
    if (queue == null || ! getQueues ().contains (queue))
      return false;
    for (final List<Double> arrTimes_j : getArrivalTimesMap (queue).values ())
      if (arrTimes_j.size () != 1)
        return false;
    return true;
  }
  
  /** Return whether jobs arrive at most once at each queue.
   * 
   * <p>
   * Note that if there are no queue, {@code true} is returned.
   * Also note that if {@code true} is returned, jobs may still visit multiple, albeit different, queues.
   * 
   * @return Whether jobs arrive at most once at each queue.
   * 
   */
  public default boolean isSingleVisit ()
  {
    for (final SimQueue q : getQueues ())
      if (! isSingleVisit (q))
        return false;
    return true;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the revocation times indexed by job at given queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * <p>
   * The inner map yields the 'interruptService' argument for the event time.
   * It has only one entry.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty map is returned.
   * 
   * @return The job revocation times indexed by job and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   */
  public Map<SimJob, List<Map<Double, Boolean>>> getRevocationTimesMap (SimQueue queue);
  
  /** Gets the job revocations indexed by time at given queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * <p>
   * The inner map yields the 'interruptService' argument for the job.
   * It has only one entry.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty map is returned.
   * 
   * @return The job revocations in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   */
  public NavigableMap<Double, List<Map<SimJob, Boolean>>> getJobRevocationsMap (SimQueue queue);

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the server-access-credits settings in time for a specific queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case an empty map is returned.
   * 
   * @return The server-access-credits settings in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   */
  public NavigableMap<Double, List<Integer>> getServerAccessCreditsMap (SimQueue queue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UNAMBIGUITY (ROEL)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Determines whether the workload is unambiguous under a ROEL (Random-Order Event List).
   * 
   * <p>
   * This implementation checks that for each queue in {@link #getQueues}, there are no simultaneous queue events.
   * It ignores job-only events, events related to queues <i>not</i> in {@link #getQueues},
   * and allows simultaneous events to distinct queues.
   * 
   * <p>
   * The criterion for unambiguity is rather strong, but is believed to be applicable to <i>any</i> queue type.
   * 
   * @return Whether the workload is unambiguous under a ROEL.
   * 
   * @throws WorkloadScheduleInvalidException If the workload is invalid.
   * 
   */
  public default boolean isUnambiguous_ROEL ()
  throws WorkloadScheduleInvalidException
  {
    for (final Map<SimQueue, Set<SimEntityEvent>> timeEvents : getTimeSimQueueSimEntityEventMap ().values ())
      for (final Entry<SimQueue, Set<SimEntityEvent>> timeEventsQueueEntry : timeEvents.entrySet ())
        if (getQueues ().contains (timeEventsQueueEntry.getKey ()) && timeEventsQueueEntry.getValue ().size () > 1)
          return false;
    return true;
  }
  
  //
  // OLDER IMPLEMENTATION OF isUnambiguous_ROEL.
  //
  // This implementation is a lot more subtle, removing "duplicate" events.
  //
  // Unfortunately, the implementation fails in presence of WorkloadScheduleHandlers
  // on DefaultWorkloadSchedule.
  //
  
//  /** Determines whether the workload is unambiguous under a ROEL (Random-Order Event List).
//   * 
//   * <p>
//   * Our basic strategy is to count the number of distinct event times from the collections.
//   * Then count the number of "duplicate" events.
//   * Finally, compare the size of the event-times set to the number of distinct (i.e., not being duplicates) events processed.
//   * If we find a smaller number, we know there are ambiguities.
//   * 
//   * <p>
//   * The criterion for unambiguity is rather strong, but is believed to be applicable to <i>any</i> queue type.
//   * 
//   * @return Whether the workload is unambiguous under a ROEL.
//   * 
//   * @throws WorkloadScheduleInvalidException If the workload is invalid.
//   * 
//   */
//  public default boolean isUnambiguous_ROEL ()
//  throws WorkloadScheduleInvalidException
//  {
//    final Set<Double> eventTimes = new HashSet<> ();
//    int eventDuplicates = 0;
//    for (final SimQueue q : getQueues ())
//    {
//      for (final List<Boolean> l : getQueueAccessVacationMap (q).values ())
//        if (new HashSet<> (l).size () < l.size ())
//          return false;
//        else
//          eventDuplicates += (l.size () - 1);
//      eventTimes.addAll (getQueueAccessVacationMap (q).keySet ());
//      for (final List<SimJob> l : getJobArrivalsMap (q).values ())
//        if (l.size () > 1)
//          return false;
//      eventTimes.addAll (getJobArrivalsMap (q).keySet ());
//      for (final List<Map<SimJob, Boolean>> l : getJobRevocationsMap (q).values ())
//      {
//        if (l.size () != 1)
//          throw new WorkloadScheduleInvalidException ();
//        final Set<SimJob> jobs = new HashSet<> ();
//        final Set<Boolean> iSs = new HashSet<> ();
//        for (final Map<SimJob, Boolean> member : l)
//        {
//          jobs.addAll (member.keySet ());
//          iSs.addAll (member.values ());
//        }
//        if (jobs.size () > 1 || iSs.size () > 1)
//          return false;
//        else
//          eventDuplicates += (l.size () - 1);
//      }
//      eventTimes.addAll (getJobRevocationsMap (q).keySet ());
//      for (final List<Integer> l : getServerAccessCreditsMap (q).values ())
//        if (new HashSet<> (l).size () < l.size ())
//          return false;
//        else
//          eventDuplicates += (l.size () - 1);
//      eventTimes.addAll (getServerAccessCreditsMap (q).keySet ());
//    }
//    return eventTimes.size () == (getProcessedQueueEvents ().size () - eventDuplicates);
//  }

}
