package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.map.SimEntityEventMap;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;

/** A representation of a schedule of workload and state-setting events for a set of {@link SimQueue}s.
 *
 * <p>
 * The set of {@link SimQueue}s to which the workload applies must be fixed upon construction.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface WorkloadSchedule<J extends SimJob, Q extends SimQueue>
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
   * @param queue       The queue, may be {@code null} or not in {@link #getQueues}, in which case {@link Double#NaN} is returned.
   * @param time        The time from which to search, use {@link Double#NaN} to retrieve the first-event time (which
   *                      may be {@link Double#NEGATIVE_INFINITY}).
   * @param eventTypes An optional set to store the (possible multiple) event types; a non-{@code null} set is cleared upon entry.
   * 
   * @return The time of the next event, or {@link Double#NaN} if no such event exists.
   * 
   * @see #hasEventsBeyond
   * @see SimQueueSimpleEventType#QUEUE_ACCESS_VACATION
   * @see SimEntitySimpleEventType#ARRIVAL
   * @see SimEntitySimpleEventType#REVOCATION
   * @see SimQueueSimpleEventType#SERVER_ACCESS_CREDITS
   * 
   */
  public default double getNextEventTimeBeyond
  (final Q queue,
   final double time,
   final Set<SimEntitySimpleEventType.Member> eventTypes)
  {
    if (eventTypes != null)
      eventTypes.clear ();
    if (queue == null || ! getQueues ().contains (queue))
      return Double.NaN;
    double nextEventTime = Double.NaN;
    if (! getQueueAccessVacationMap (queue).isEmpty ())
    {
      final Double nextQavTime = (Double.isNaN (time)
        ? getQueueAccessVacationMap (queue).firstKey ()
        : getQueueAccessVacationMap (queue).higherKey (time));
      if (nextQavTime != null && (Double.isNaN (nextEventTime) || nextQavTime <= nextEventTime))
      {
        if (eventTypes != null)
        {
          if ((! Double.isNaN (nextEventTime)) && nextQavTime < nextEventTime)
            eventTypes.clear ();
          eventTypes.add (SimQueueSimpleEventType.QUEUE_ACCESS_VACATION);
        }
        nextEventTime = nextQavTime;
      }
    }
    if (! getJobArrivalsMap (queue).isEmpty ())
    {
      final Double nextArrTime = (Double.isNaN (time)
        ? getJobArrivalsMap (queue).firstKey ()
        : getJobArrivalsMap (queue).higherKey (time));
      if (nextArrTime != null && (Double.isNaN (nextEventTime) || nextArrTime <= nextEventTime))
      {
        if (eventTypes != null)
        {
          if ((! Double.isNaN (nextEventTime)) && nextArrTime < nextEventTime)
            eventTypes.clear ();
          eventTypes.add (SimEntitySimpleEventType.ARRIVAL);
        }
        nextEventTime = nextArrTime;
      }
    }
    if (! getJobRevocationsMap (queue).isEmpty ())
    {
      final Double nextRevTime = (Double.isNaN (time)
        ? getJobRevocationsMap (queue).firstKey ()
        : getJobRevocationsMap (queue).higherKey (time));
      if (nextRevTime != null && (Double.isNaN (nextEventTime) || nextRevTime <= nextEventTime))
      {
        if (eventTypes != null)
        {
          if ((! Double.isNaN (nextEventTime)) && nextRevTime < nextEventTime)
            eventTypes.clear ();
          eventTypes.add (SimEntitySimpleEventType.REVOCATION);
        }
        nextEventTime = nextRevTime;
      }
    }
    if (! getServerAccessCreditsMap (queue).isEmpty ())
    {
      final Double nextSacTime = (Double.isNaN (time)
        ? getServerAccessCreditsMap (queue).firstKey ()
        : getServerAccessCreditsMap (queue).higherKey (time));
      if (nextSacTime != null && (Double.isNaN (nextEventTime) || nextSacTime <= nextEventTime))
      {
        if (eventTypes != null)
        {
          if ((! Double.isNaN (nextEventTime)) && nextSacTime < nextEventTime)
            eventTypes.clear ();
          eventTypes.add (SimQueueSimpleEventType.SERVER_ACCESS_CREDITS);
        }
        nextEventTime = nextSacTime;
      }
    }
    return nextEventTime;
  }
  
  /** Returns whether there exist event(s) scheduled strictly beyond a given time at a specific queue.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case {@code false} is returned.
   * @param time  The time from which to search, use {@link Double#NaN} to retrieve the first-event time (which
   *              may be {@link Double#NEGATIVE_INFINITY}).
   * 
   * @return True if there exist event(s) strictly beyond a given time.
   * 
   * @see #getNextEventTimeBeyond
   * 
   */
  public default boolean hasEventsBeyond (final Q queue, final double time)
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
   *         ordered as found in the source upon construction.
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
  public Set<Q> getQueues ();
  
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
  public NavigableMap<Double, List<Boolean>> getQueueAccessVacationMap (Q queue);

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
  public Set<J> getJobs ();

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
  public Set<J> getJobs (Q queue);
  
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
  public Map<J, List<Double>> getArrivalTimesMap (Q queue);
  
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
  public NavigableMap<Double, List<J>> getJobArrivalsMap (Q queue);
  
  /** Return whether jobs arrive at most once at given queue.
   * 
   * @param queue The queue, may be {@code null} or not in {@link #getQueues}, in which case {@code false} is returned.
   * 
   * @return Whether jobs arrive at most once at given queue.
   * 
   */
  public default boolean isSingleVisit (final Q queue)
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
    for (final Q q : getQueues ())
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
  public Map<J, List<Map<Double, Boolean>>> getRevocationTimesMap (Q queue);
  
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
  public NavigableMap<Double, List<Map<J, Boolean>>> getJobRevocationsMap (Q queue);

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
  public NavigableMap<Double, List<Integer>> getServerAccessCreditsMap (Q queue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UNAMBIGUITY (ROEL)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Determines whether the workload is unambiguous under a ROEL (Random-Order Event List).
   * 
   * <p>
   * Our basic strategy is to count the number of distinct event times from the collections.
   * Then count the number of "duplicate" events.
   * Finally, compare the size of the event-times set to the number of distinct (i.e., not being duplicates) events processed.
   * If we find a smaller number, we know there are ambiguities.
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
    final Set<Double> eventTimes = new HashSet<> ();
    int eventDuplicates = 0;
    for (final Q q : getQueues ())
    {
      for (final List<Boolean> l : getQueueAccessVacationMap (q).values ())
        if (new HashSet<> (l).size () < l.size ())
          return false;
        else
          eventDuplicates += (l.size () - 1);
      eventTimes.addAll (getQueueAccessVacationMap (q).keySet ());
      for (final List<J> l : getJobArrivalsMap (q).values ())
        if (l.size () > 1)
          return false;
      eventTimes.addAll (getJobArrivalsMap (q).keySet ());
      for (final List<Map<J, Boolean>> l : getJobRevocationsMap (q).values ())
      {
        if (l.size () != 1)
          throw new WorkloadScheduleInvalidException ();
        final Set<J> jobs = new HashSet<> ();
        final Set<Boolean> iSs = new HashSet<> ();
        for (final Map<J, Boolean> member : l)
        {
          jobs.addAll (member.keySet ());
          iSs.addAll (member.values ());
        }
        if (jobs.size () > 1 || iSs.size () > 1)
          return false;
        else
          eventDuplicates += (l.size () - 1);
      }
      eventTimes.addAll (getJobRevocationsMap (q).keySet ());
      for (final List<Integer> l : getServerAccessCreditsMap (q).values ())
        if (new HashSet<> (l).size () < l.size ())
          return false;
        else
          eventDuplicates += (l.size () - 1);
      eventTimes.addAll (getServerAccessCreditsMap (q).keySet ());
    }
    return eventTimes.size () == (getProcessedQueueEvents ().size () - eventDuplicates);
  }
  
}