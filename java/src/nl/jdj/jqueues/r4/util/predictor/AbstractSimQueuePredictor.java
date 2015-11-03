package nl.jdj.jqueues.r4.util.predictor;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.schedule.JobQueueVisitArrivalSchedule;
import nl.jdj.jqueues.r4.util.schedule.JobQueueVisitRevocationSchedule;
import nl.jdj.jqueues.r4.util.schedule.QueueAccessVacationSchedule;
import nl.jdj.jqueues.r4.util.schedule.QueueExternalEvent;
import nl.jdj.jqueues.r4.util.schedule.ServerAccessCreditsSchedule;

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
  // COLLECTION UTILITY METHODS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a copy of a {@code Map<Double, ? extends Set<C>>} into a {@code TreeMap<Double, LinkedHashSet<C>>}.
   * 
   * <p>
   * Copies the map and the sets, not the objects.
   * The method allows {@code null} values for the sets as well as in the sets.
   * 
   * @param map The original, if {@code null}, {@code null} is returned.
   * @return    A copy.
   * 
   * @param <C> The object type in the sets.
   * 
   */
  protected static <C> TreeMap<Double, LinkedHashSet<C>> copy (final Map<Double, ? extends Set<C>> map)
  {
    if (map == null)
      return null;
    final TreeMap<Double, LinkedHashSet<C>> copyMap = new TreeMap<> ();
    for (final double d : map.keySet ())
      if (map.get (d) == null)
        copyMap.put (d, null);
      else
        copyMap.put (d, new LinkedHashSet<> (map.get (d)));
    return copyMap;
  }
  
  /** Adds an entry to a {@code TreeMap<Double, LinkedHashSet<C>>} mapping from time to ordered objects.
   * 
   * <p>
   * The entry must not be present already!
   * 
   * @param map   The map.
   * @param time  The time (key) at which to insert the object.
   * @param entry The object.
   * 
   * @param <C> The object type in the sets.
   * 
   * @throws IllegalArgumentException If the map or entry is {@code null}, or if it contains {@code null} values,
   *                                  or if the entry is already preset (at any time!).
   * 
   */
  protected static <C> void add (final TreeMap<Double, LinkedHashSet<C>> map, final double time, final C entry)
  {
    if (map == null || entry == null)
      throw new IllegalArgumentException ();
    for (final double d : map.keySet ())
      if (map.get (d) == null || map.get (d).contains (entry))
        throw new IllegalArgumentException ();
    if (! map.containsKey (time))
      map.put (time, new LinkedHashSet<> ());
    map.get (time).add (entry);
  }
  
  /** Replaces an object in a {@code TreeMap<Double, LinkedHashSet<C>>} mapping from time to ordered objects.
   * 
   * @param map      The map.
   * @param time     The time (key) at which to replace the object.
   * @param oldEntry The old object.
   * @param newEntry The new object.
   * 
   * @param <C> The object type in the sets.
   * 
   * @throws IllegalArgumentException If the map or one of the entries is {@code null},
   *                                  or if the old entry is not present,
   *                                  or if the new entry is already preset.
   * 
   */
  protected static <C> void replace
  (final TreeMap<Double, LinkedHashSet<C>> map, final double time, final C oldEntry, final C newEntry)
  {
    if (map == null
      || oldEntry == null
      || newEntry == null
      || (! map.containsKey (time))
      || (! map.get (time).contains (oldEntry))
      || map.get (time).contains (newEntry))
      throw new IllegalArgumentException ();
    final LinkedHashSet<C> newSet = new LinkedHashSet<> ();
    for (final C entry : map.get (time))
      newSet.add ((entry == oldEntry) ? newEntry : entry);
    map.put (time, newSet);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FILTERING SimQueueEvents
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes (in-situ) irrelevant {@link QueueExternalEvent}s from a map.
   * 
   * @param queue               The queue, non-{@code null}.
   * @param queueExternalEvents The external events (to arbitrary queues), non-{@code null}.
   * 
   * @return The {@code queueExternalEvents} argument in which all events not applying to the given queue are removed.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  protected TreeMap<Double, ? extends Set<QueueExternalEvent<J, Q>>> filterSimQueueEventsForRelevance
  (final Q queue, final TreeMap<Double, ? extends Set<QueueExternalEvent<J, Q>>> queueExternalEvents)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (queueExternalEvents == null)
      return null;
    final Set<Double> timesToRemove = new HashSet<> ();
    for (final double d : queueExternalEvents.keySet ())
      if (queueExternalEvents.get (d) == null)
        timesToRemove.add (d);
      else
      {
        final Set<QueueExternalEvent> qeeToRemove = new HashSet<> ();
        for (final QueueExternalEvent qee : queueExternalEvents.get (d))
          if (qee.queue != queue)
              qeeToRemove.add (qee);
        queueExternalEvents.get (d).removeAll (qeeToRemove);
        if (queueExternalEvents.get (d).isEmpty ())
          timesToRemove.add (d);
      }
    for (final double time : timesToRemove)
      queueExternalEvents.remove (time);
    return queueExternalEvents;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JobQueueVisitLog factories and insertion (SINGLE VISIT)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Adds a dropped job at a queue to a map of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs    The map.
   * @param queue        The queue.
   * @param job          The job.
   * @param arrivalTime  The arrival time.
   * @param dropTime     The drop time.
   * 
   * @throws IllegalArgumentException If the map, queue, or job is {@code null}, the map already has an entry for the job,
   *                                  or if the drop time is strictly smaller than the arrival time.
   * 
   */
  protected void addDroppedJob_SingleVisit
  (final Map<J, JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final double dropTime)
  {
    if (visitLogs == null || queue == null || job == null || dropTime < arrivalTime)
      throw new IllegalArgumentException ();
    if (visitLogs.containsKey (job))
      throw new IllegalArgumentException ();
    visitLogs.put (job, new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        true, dropTime,
        false, Double.NaN,
        false, Double.NaN,
        false, Double.NaN));
  }
  
  /** Adds a revoked job at a queue to a map of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs      The map.
   * @param queue          The queue.
   * @param job            The job.
   * @param arrivalTime    The arrival time.
   * @param started        Whether the job has already started.
   * @param startTime      The start time of the job, if started.
   * @param revocationTime The revocation time.
   * 
   * @throws IllegalArgumentException If the map, queue, or job is {@code null}, the map already has an entry for the job,
   *                                  or if sanity checks on the time arguments fail.
   * 
   */
  protected void addRevokedJob_SingleVisit
  (final Map<J, JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime,
   final double revocationTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || revocationTime < arrivalTime
      || (started && startTime < arrivalTime)
      || (started && revocationTime < startTime))
      throw new IllegalArgumentException ();
    if (visitLogs.containsKey (job))
      throw new IllegalArgumentException ();
    visitLogs.put (job, new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        false, Double.NaN,
        true, revocationTime,
        false, Double.NaN));
  }
  
  /** Adds a sticky job (never leaves) at a queue to a map of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs      The map.
   * @param queue          The queue.
   * @param job            The job.
   * @param arrivalTime    The arrival time.
   * @param started        Whether the job has already started.
   * @param startTime      The start time of the job, if started.
   * 
   * @throws IllegalArgumentException If the map, queue, or job is {@code null}, the map already has an entry for the job,
   *                                  or if sanity checks on the time arguments fail.
   * 
   */
  protected void addStickyJob_SingleVisit
  (final Map<J, JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || (started && startTime < arrivalTime))
      throw new IllegalArgumentException ();
    if (visitLogs.containsKey (job))
      throw new IllegalArgumentException ();
    visitLogs.put (job, new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        false, Double.NaN,
        false, Double.NaN,
        false, Double.NaN));
  }
  
  /** Adds a departed job at a queue to a map of {@link JobQueueVisitLog}s.
   * 
   * @param visitLogs     The map.
   * @param queue         The queue.
   * @param job           The job.
   * @param arrivalTime   The arrival time.
   * @param started       Whether the job has already started.
   * @param startTime     The start time of the job, if started.
   * @param departureTime The departure time.
   * 
   * @throws IllegalArgumentException If the map, queue, or job is {@code null}, the map already has an entry for the job,
   *                                  or if sanity checks on the time arguments fail.
   * 
   */
  protected void addDeparture_SingleVisit
  (final Map<J, JobQueueVisitLog<J, Q>> visitLogs,
   final Q queue,
   final J job,
   final double arrivalTime,
   final boolean started,
   final double startTime,
   final double departureTime)
  {
    if (visitLogs == null
      || queue == null
      || job == null
      || (departureTime < arrivalTime)
      || (started && startTime < arrivalTime)
      || (started && startTime > departureTime))
      throw new IllegalArgumentException ();
    if (visitLogs.containsKey (job))
      throw new IllegalArgumentException ();
    visitLogs.put (job, new JobQueueVisitLog<>
      (job, queue,
        true, arrivalTime,
        0,
        started, startTime,
        false, Double.NaN,
        false, Double.NaN,
        true, departureTime));
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QueueExternalEvents information extraction (SINGLE VISIT)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets a map from jobs onto their arrival time at a given queue, assuming at most one visit to that queue.
   * 
   * <p>
   * Events related to other queues are ignored.
   * 
   * @param queue               The queue.
   * @param queueExternalEvents The events, if {@code null}, an empty map is returned.
   * 
   * @return The map from jobs onto their arrival time at the queue, the jobs are ordered by the appearance of their arrival
   *         in the events (meaning they are ordered based upon arrival time, and for identical arrival times, ordered based
   *         upon their arrival event in the event set for that time).
   * 
   * @throws IllegalArgumentException If {@code queue == null} or the events reveal multiple visits to the given queue for any job.
   * 
   */
  protected LinkedHashMap<J, Double> getArrivalTimes_SingleVisit
  (final Q queue, final TreeMap<Double, ? extends Set<QueueExternalEvent<J, Q>>> queueExternalEvents)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final LinkedHashMap<J, Double> arrivalTimes = new LinkedHashMap<> ();
    if (queueExternalEvents != null)
      for (final Set<QueueExternalEvent<J, Q>> queueExternalEventsNow : queueExternalEvents.values ())
        for (final QueueExternalEvent<J, Q> qee : queueExternalEventsNow)
          if (qee.queue == queue && (qee instanceof JobQueueVisitArrivalSchedule))
          {
            if (qee.job == null)
              throw new IllegalArgumentException ();
            else if (arrivalTimes.containsKey (qee.job))
              throw new UnsupportedOperationException ();
            else
              arrivalTimes.put (qee.job, qee.time);
          }
    return arrivalTimes;    
  }
  
  /** XXX To be documented.
   * 
   * @param queue
   * @param queueExternalEvents
   * @param visitLogs
   * @param revocationTime
   * 
   * @return
   * 
   */
  protected LinkedHashMap<J, Double> getNonDroppedArrivals_SingleVisit
  (final Q queue,
   final TreeMap<Double, ? extends Set<QueueExternalEvent<J, Q>>> queueExternalEvents,
   final Map<J, JobQueueVisitLog<J, Q>> visitLogs,
   final Map<J, Double> revocationTime)
  {
    if (queue == null || visitLogs == null || revocationTime == null)
      throw new IllegalArgumentException ();
    final Set<J> qavArrivals = new HashSet<> ();
    final LinkedHashMap<J, Double> nonQavArrivals = new LinkedHashMap<> ();
    revocationTime.clear ();
    boolean vacation = false;
    if (queueExternalEvents != null)
      for (final Set<QueueExternalEvent<J, Q>> queueExternalEventsNow : queueExternalEvents.values ())
      {
        final Set<QueueExternalEvent> eventsToRemove = new HashSet<> ();
        for (final QueueExternalEvent<J, Q> qee : queueExternalEventsNow)
          if (qee.queue == queue)
          {
            if (qee instanceof QueueAccessVacationSchedule)
            {
              vacation = ((QueueAccessVacationSchedule) qee).vacation;
              eventsToRemove.add (qee);
            }
            else if (qee instanceof JobQueueVisitArrivalSchedule)
            {
              if (qee.job == null
                  || nonQavArrivals.containsKey (qee.job)
                  || qavArrivals.contains (qee.job))
              {
                throw new IllegalArgumentException ();
              }
              else if (vacation)
              {
                final double arrivalTime = qee.time;
                final double dropTime = qee.time;
                addDroppedJob_SingleVisit (visitLogs, queue, qee.job, arrivalTime, dropTime);
                qavArrivals.add (qee.job);
              }
              else
              {
                nonQavArrivals.put (qee.job, qee.time);
              }
              eventsToRemove.add (qee);
            }
            else if (qee instanceof JobQueueVisitRevocationSchedule)
            {
              if (qee.job == null
                  || (! nonQavArrivals.containsKey (qee.job))
                  || qavArrivals.contains (qee.job)
                  || revocationTime.containsKey (qee.job))
              {
                throw new IllegalArgumentException ();
              }
              else
              {
                revocationTime.put (qee.job, qee.time);
              }
              eventsToRemove.add (qee);
            }
          }
        queueExternalEventsNow.removeAll (eventsToRemove);
      }
    return nonQavArrivals;    
  }
  
  /** XXX To be documented.
   * 
   * @param queue
   * @param queueExternalEvents
   * @param visitLogs
   * 
   * @return 
   * 
   */
  protected Set<J> predictQueueAccessVacationDrops_singleVisit
  (final Q queue,
   final TreeMap<Double, ? extends Set<QueueExternalEvent<J, Q>>> queueExternalEvents,
   final Map<J, JobQueueVisitLog<J, Q>> visitLogs)
  {
    if (queue == null || visitLogs == null)
      throw new IllegalArgumentException ();
    final Set<J> jobsDropped = new HashSet<> ();
    boolean vacation = false;
    if (queueExternalEvents != null)
      for (final Set<QueueExternalEvent<J, Q>> queueExternalEventsNow : queueExternalEvents.values ())
      {
        final Set<QueueExternalEvent> eventsToRemove = new HashSet<> ();
        for (final QueueExternalEvent<J, Q> qee : queueExternalEventsNow)
          if (qee.queue == queue)
          {
            if (qee instanceof QueueAccessVacationSchedule)
            {
              vacation = ((QueueAccessVacationSchedule) qee).vacation;
              eventsToRemove.add (qee);
            }
            else if ((qee instanceof JobQueueVisitArrivalSchedule) && vacation)
            {
              final double arrivalTime = qee.time;
              final double dropTime = qee.time;
              addDroppedJob_SingleVisit (visitLogs, queue, qee.job, arrivalTime, dropTime);
              jobsDropped.add (qee.job);
              eventsToRemove.add (qee);
            }
          }
        queueExternalEventsNow.removeAll (eventsToRemove);
      }
    return jobsDropped;
  }
  
  /** XXX To be documented.
   * 
   * @param queue
   * @param queueExternalEvents
   * 
   * @return 
   * 
   */
  protected TreeMap<Double, Integer> predictInitialServerAccessCredits
  (final Q queue, final TreeMap<Double, ? extends Set<QueueExternalEvent<J, Q>>> queueExternalEvents)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final TreeMap<Double, Integer> sacMap = new TreeMap<> ();
    if (queueExternalEvents != null)
      for (final Set<QueueExternalEvent<J, Q>> queueExternalEventsNow : queueExternalEvents.values ())
      {
        final Set<QueueExternalEvent> eventsToRemove = new HashSet<> ();
        for (final QueueExternalEvent<J, Q> qee : queueExternalEventsNow)
          if (qee.queue == queue && (qee instanceof ServerAccessCreditsSchedule))
          {
            if (sacMap.containsKey (qee.time))
              throw new IllegalArgumentException ();
            sacMap.put (qee.time, ((ServerAccessCreditsSchedule) qee).credits);
            eventsToRemove.add (qee);
          }
        queueExternalEventsNow.removeAll (eventsToRemove);
      }    
    return sacMap;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RUNNING-JOBS SETS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Predicts the sets of jobs running, indexed by time (of change).
   * 
   * @param startTimes A map of jobs onto their start times (at some hypothetical queue).
   * 
   * @return The sets of jobs running, indexed by time (of change); the jobs in the returned set are ordered by start time.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}, or if it contains {@code null} keys or values.
   * 
   */
  protected TreeMap<Double, LinkedHashSet<J>> predictRunningJobs (final Map<J, Double> startTimes)
  {
    if (startTimes == null || startTimes.containsKey (null) || startTimes.containsValue (null))
      throw new IllegalArgumentException ();
    final TreeMap<Double, LinkedHashSet<J>> startingJobs = new TreeMap<> ();
    for (final Entry<J, Double> jobStart : startTimes.entrySet ())
    {
      final J job = jobStart.getKey ();
      final double startTime = jobStart.getValue ();
      if (! startingJobs.containsKey (startTime))
        startingJobs.put (startTime, new LinkedHashSet<> ());
      startingJobs.get (startTime).add (job);
    }
    final LinkedHashSet<J> jobsRunningNow = new LinkedHashSet<> ();
    final TreeMap<Double, LinkedHashSet<J>> runningJobsMap = new TreeMap<> ();
    for (final Entry<Double, LinkedHashSet<J>> epoch : startingJobs.entrySet ())
    {
      jobsRunningNow.addAll (epoch.getValue ());
      runningJobsMap.put (epoch.getKey (), new LinkedHashSet<> (jobsRunningNow));
    }
    return runningJobsMap;
  }
  
}
