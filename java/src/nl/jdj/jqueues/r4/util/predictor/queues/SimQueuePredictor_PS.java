package nl.jdj.jqueues.r4.util.predictor.queues;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.processorsharing.PS;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.event.SimEntityEvent;

/** A {@link SimQueuePredictor} for {@link PS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_PS<J extends SimJob>
extends AbstractSimQueuePredictor<J, PS>
{

  /** XXX To be documented.
   * 
   * @param queue
   * @param queueEvents
   * 
   * @return
   * 
   * @throws SimQueuePredictionAmbiguityException 
   * 
   */
  @Override
  public Map<J, JobQueueVisitLog<J, PS>> predictUniqueJobQueueVisitLogs_SingleVisit
  (final PS queue, final TreeMap<Double, Set<SimEntityEvent<J, PS>>> queueEvents)
    throws SimQueuePredictionAmbiguityException
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final TreeMap<Double, ? extends Set<SimEntityEvent<J, PS>>> queueEventsCopy
      = filterSimQueueEventsForRelevance (queue, AbstractSimQueuePredictor.copy (queueEvents));
    final Map<J, JobQueueVisitLog<J, PS>> visitLogs = new HashMap<> ();
    final LinkedHashMap<J, Double> revocationTime = new LinkedHashMap<> ();
    final LinkedHashMap<J, Double> jobArrivalTimes
      = getNonDroppedArrivals_SingleVisit (queue, queueEventsCopy, visitLogs, revocationTime);
    final TreeMap<Double, Integer> sacMap = predictInitialServerAccessCredits (queue, queueEventsCopy);
    final LinkedHashMap<J, Double> startTimes =
      predictStartTimes_SingleVisit (queue, jobArrivalTimes, revocationTime, sacMap, visitLogs);
    final TreeMap<Double, LinkedHashSet<J>> runningJobs = predictRunningJobs (startTimes);
    predictJobsAtNegativeInfinity (queue, runningJobs, revocationTime, visitLogs);
    final Map<J, Double> remainingServiceTime = new HashMap<> ();
    for (final LinkedHashSet<J> jobSet : runningJobs.values ())
      for (final J j : jobSet)
        if (j == null)
          throw new RuntimeException ();
        else if (! remainingServiceTime.containsKey (j))
          remainingServiceTime.put (j, j.getServiceTime (queue));
    // XXX Remove revocations that do not interrupt; check them later...
    //final LinkedHashMap<J, Double> nonInterruptingRevocations
    //  = removeNonInterruptingRevocations (revocationTime);
    while (! runningJobs.isEmpty ())
      nextRunningEpoch (queue, runningJobs, jobArrivalTimes, startTimes, remainingServiceTime, revocationTime, visitLogs);
    return visitLogs;
  }
  
  /** XXX To be documented.
   * 
   * @param queue
   * @param jobArrivalTimes
   * @param revocationTime
   * @param sacMap
   * @param visitLogs
   * 
   * @return
   * 
   * @throws SimQueuePredictionAmbiguityException 
   * 
   */
  protected LinkedHashMap<J, Double> predictStartTimes_SingleVisit
  (final PS queue,
   final LinkedHashMap<J, Double> jobArrivalTimes,
   final LinkedHashMap<J, Double> revocationTime,
   final TreeMap<Double, Integer> sacMap,
   final Map<J, JobQueueVisitLog<J, PS>> visitLogs)
    throws SimQueuePredictionAmbiguityException
  {
    if (queue == null || jobArrivalTimes == null || sacMap == null || visitLogs == null)
      throw new IllegalArgumentException ();
    final Map<Double, LinkedHashSet<J>> reverseArrivalMap = new LinkedHashMap<> ();
    for (final Entry<J, Double> jobArrival : jobArrivalTimes.entrySet ())
    {
      if (! reverseArrivalMap.containsKey (jobArrival.getValue ()))
        reverseArrivalMap.put (jobArrival.getValue (), new LinkedHashSet<> ());
      reverseArrivalMap.get (jobArrival.getValue ()).add (jobArrival.getKey ());
    }
    final LinkedHashMap<J, Double> startTimes = new LinkedHashMap<> ();
    for (final Entry<Double, LinkedHashSet<J>> jobArrivalMoment : reverseArrivalMap.entrySet ())
    {
      final double arrivalTime = jobArrivalMoment.getKey ();
      final LinkedHashSet<J> arrivingJobs = jobArrivalMoment.getValue ();
      final Map.Entry<Double, Integer> sacEntry = sacMap.floorEntry (arrivalTime);
      int serverAccessCredits = ((sacEntry != null) ? sacEntry.getValue () : Integer.MAX_VALUE);
      if (sacMap.containsKey (arrivalTime) && arrivingJobs.size () > serverAccessCredits)
        throw new SimQueuePredictionAmbiguityException ();
      for (final J job : arrivingJobs)
      {
        serverAccessCredits = ((sacEntry != null) ? sacEntry.getValue () : Integer.MAX_VALUE);
        if (serverAccessCredits > 0)
        {
          if (revocationTime.containsKey (job))
          {
            if (revocationTime.get (job) != arrivalTime)
              throw new IllegalArgumentException ();
            else if (sacMap.containsKey (arrivalTime))
              throw new SimQueuePredictionAmbiguityException ();
            else
            {
              // Revoked while waiting.
              final boolean started = false;
              final double startTime = Double.NaN;
              final double revocationTime_job = arrivalTime;
              addRevokedJob_SingleVisit (visitLogs, queue, job, arrivalTime, started, startTime, revocationTime_job);
              revocationTime.remove (job);
            }
          }
          else
          {
            // Started upon arrival.
            final double startTime = arrivalTime;
            startTimes.put (job, startTime);
            if (serverAccessCredits < Integer.MAX_VALUE)
              sacMap.put (arrivalTime, serverAccessCredits - 1);
          }
        }
        else
        {
          Map.Entry<Double, Integer> nextSacEntry = sacMap.higherEntry (arrivalTime);
          while (nextSacEntry != null && nextSacEntry.getValue () == 0)
            nextSacEntry = sacMap.higherEntry (nextSacEntry.getKey ());
          if (nextSacEntry != null)
          {
            final double nextSacAvailableTime = nextSacEntry.getKey ();
            boolean startJob = true;
            if (revocationTime.containsKey (job))
            {
              final double revocationTime_job = revocationTime.get (job);
              if (revocationTime_job < arrivalTime)
                throw new IllegalArgumentException ();
              else if (revocationTime_job < nextSacAvailableTime)
              {
                // Revoked while waiting.
                final boolean started = false;
                final double startTime = Double.NaN;
                addRevokedJob_SingleVisit (visitLogs, queue, job, arrivalTime, started, startTime, revocationTime_job);
                startJob = false;
                revocationTime.remove (job);
              }
              else if (revocationTime_job == nextSacAvailableTime)
                throw new SimQueuePredictionAmbiguityException ();
            }
            if (startJob)
            {
              final int serverAccessCreditsNow = nextSacEntry.getValue ();
              // Started after waiting for server-access credits.
              final double startTime = nextSacEntry.getKey ();
              startTimes.put (job, startTime);
              if (serverAccessCreditsNow < Integer.MAX_VALUE)
                sacMap.put (startTime, serverAccessCreditsNow - 1);
            }
          }
          else
          {
            // Job never starts; must wait indefinitely unless revoked!
            if (revocationTime.containsKey (job))
            {
              final double revocationTime_job = revocationTime.get (job);
              if (revocationTime_job < arrivalTime)
                throw new IllegalArgumentException ();
              else
              {
                // Revoked while waiting (indefinitely).
                final boolean started = false;
                final double startTime = Double.NaN;
                addRevokedJob_SingleVisit (visitLogs, queue, job, arrivalTime, started, startTime, revocationTime_job);
                revocationTime.remove (job);
              }
            }
            else
            {
              // Job arrives but never start nor exits (sticy job).
              final boolean started = false;
              final double startTime = Double.NaN;
              addStickyJob_SingleVisit (visitLogs, queue, job, arrivalTime, started, startTime);
            }
          }
        }
      }      
    }
    return startTimes;
  }
  
  /** XXX To be documented.
   * 
   * @param queue
   * @param runningJobs
   * @param revocationTime
   * @param visitLogs 
   * 
   */
  protected void predictJobsAtNegativeInfinity
  (final PS queue,
   final TreeMap<Double, LinkedHashSet<J>> runningJobs,
   final LinkedHashMap<J, Double> revocationTime,
   final Map<J, JobQueueVisitLog<J, PS>> visitLogs)    
  {
    if (queue == null || runningJobs == null || revocationTime == null || visitLogs == null)
      throw new IllegalArgumentException ();
    if (runningJobs.containsKey (Double.NEGATIVE_INFINITY))
    {
      final Set<J> jobsAtNegativeInfinity = runningJobs.get (Double.NEGATIVE_INFINITY);
      for (final J j : jobsAtNegativeInfinity)
      {
        final double arrivalTime = Double.NEGATIVE_INFINITY;
        final double startTime = Double.NEGATIVE_INFINITY;
        if (j.getServiceTime (queue) < 0)
          throw new IllegalArgumentException ();
        if (j.getServiceTime (queue) == Double.POSITIVE_INFINITY)
          throw new IllegalArgumentException ();
        if (revocationTime.containsKey (j))
        {
          final double revocationTime_j = revocationTime.get (j);
          if (revocationTime_j != Double.NEGATIVE_INFINITY)
            throw new IllegalArgumentException ();
          addRevokedJob_SingleVisit (visitLogs, queue, j, arrivalTime, true, startTime, revocationTime_j);
          revocationTime.remove (j);
        }
        final double departureTime = Double.NEGATIVE_INFINITY;
        addDeparture_SingleVisit (visitLogs, queue, j, arrivalTime, true, startTime, departureTime);
      }
      runningJobs.remove (Double.NEGATIVE_INFINITY);
      for (LinkedHashSet<J> s : runningJobs.values ())
        s.removeAll (jobsAtNegativeInfinity);
    }
  }
 
  /** XXX To be documented!
   * 
   * @param queue
   * @param runningJobs
   * @param arrivalTimes
   * @param startTimes
   * @param remainingServiceTime
   * @param revocationTime
   * @param visitLogs 
   * 
   */
  protected void sanityNextRunningEpoch
  (final PS queue,
   final TreeMap<Double, LinkedHashSet<J>> runningJobs,
   final LinkedHashMap<J, Double> arrivalTimes,
   final LinkedHashMap<J, Double> startTimes,
   final Map<J, Double> remainingServiceTime,
   final LinkedHashMap<J, Double> revocationTime,
   final Map<J, JobQueueVisitLog<J, PS>> visitLogs)
  {
    
    // Regular non-null parameter check.
    if (queue == null
      || runningJobs == null
      || startTimes == null
      || remainingServiceTime == null
      || revocationTime == null
      || visitLogs == null)
      throw new IllegalArgumentException ();
    
    // The runningJobs map must not be empty - we are supposed to handle the next "event" in runningJobs!
    if (runningJobs.isEmpty ())
      throw new IllegalArgumentException ();
    // The runningJobs map must not hold a null value (Set); it cannot by contract of TreeMap<Double, ...> hold null keys.
    if (runningJobs.containsValue (null))
      throw new IllegalArgumentException ();
    // The runningJobs map must not contain negative infinity..
    if (runningJobs.firstKey () == Double.NEGATIVE_INFINITY)
      throw new IllegalArgumentException ();
    // None of the Set values in runningJobs can be empty or contain a null value.
    for (final Set<J> s : runningJobs.values ())
      if (s.isEmpty () || s.contains (null))
        throw new IllegalArgumentException ();
    // The ordered values of the runningJobs map must be strictly increasing; each next step a single job must be added.
    final Iterator<LinkedHashSet<J>> iterator = runningJobs.values ().iterator ();
    LinkedHashSet<J> runningJobsNow = iterator.next ();
    while (iterator.hasNext ())
    {
      final LinkedHashSet<J> runningJobsNext = iterator.next ();
      if (runningJobsNext.size () != runningJobsNow.size () + 1 || ! runningJobsNext.containsAll (runningJobsNow))
        throw new IllegalArgumentException ();
      else
        runningJobsNow = runningJobsNext;
    }
    
    // Our fixed set of "all jobs".
    final Set<J> allJobs = runningJobsNow;
    
    // The currently running jobs.
    runningJobsNow = runningJobs.get (runningJobs.firstKey ());
    
    // Our current time.
    final double now = runningJobs.firstKey ();
    
    // The arrivalTimes map must not contain null keys or values.
    if (arrivalTimes.containsKey (null ) || arrivalTimes.containsValue (null))
      throw new IllegalArgumentException ();
    // The arrivalTimes map key set must be identical to "all jobs".
    if (! (allJobs.containsAll (arrivalTimes.keySet ()) && arrivalTimes.keySet ().containsAll (allJobs)))
      throw new IllegalArgumentException ();
    // The arrivalTimes map must not contain values in the future for our currently running jobs.
    for (final J j : runningJobsNow)
      if (arrivalTimes.get (j) > now)
        throw new IllegalArgumentException ();
        
    // The startTimes map must not contain null keys or values.
    if (startTimes.containsKey (null ) || startTimes.containsValue (null))
      throw new IllegalArgumentException ();
    // The startTimes map key set must be identical to "all jobs".
    if (! (allJobs.containsAll (startTimes.keySet ()) && startTimes.keySet ().containsAll (allJobs)))
      throw new IllegalArgumentException ();
    // The startTimes map must not contain values in the future for our currently running jobs.
    for (final J j : runningJobsNow)
      if (startTimes.get (j) > now)
        throw new IllegalArgumentException ();
    // Start times must not preceed arrival times for our currently running jobs.
    for (final J j : runningJobsNow)
      if (startTimes.get (j) > arrivalTimes.get (j))
        throw new IllegalArgumentException ();
        
    // The remainingServiceTime map must not contain null keys or values.
    if (remainingServiceTime.containsKey (null ) || remainingServiceTime.containsValue (null))
      throw new IllegalArgumentException ();
    // The remainingServiceTime map must not contain "alien" jobs.
    if (! allJobs.containsAll (remainingServiceTime.keySet ()))
      throw new IllegalArgumentException ();
    // The remainingServiceTime map must not contain strictly negative values.
    for (final double d : remainingServiceTime.values ())
      if (d < 0)
        throw new IllegalArgumentException ();
    
    // The revocationTime map must not contain null keys or values.
    if (revocationTime.containsKey (null ) || revocationTime.containsValue (null))
      throw new IllegalArgumentException ();
    // The revocationTime map must not contain "alien" jobs.
    if (! allJobs.containsAll (revocationTime.keySet ()))
      throw new IllegalArgumentException ();
    // The revocationTime map must not contain entries from the past.
    for (final double d : revocationTime.values ())
      if (d < runningJobs.firstKey ())
        throw new IllegalArgumentException ();
    
    // The visitLogs map must not contain null keys or values.
    if (visitLogs.containsKey (null ) || visitLogs.containsValue (null))
      throw new IllegalArgumentException ();
    // The visitLogs map must not contain entries for "our jobs".
    for (final J j : visitLogs.keySet ())
      if (allJobs.contains (j))
        throw new IllegalArgumentException ();
    
    // Well, looks like we're good to go at this point :-).
    
  }
  
  /** XXX To be documented.
   * 
   * @param queue
   * @param runningJobs
   * @param arrivalTimes
   * @param startTimes
   * @param remainingServiceTime
   * @param revocationTime
   * @param visitLogs
   * 
   * @throws SimQueuePredictionAmbiguityException 
   * 
   */
  protected void nextRunningEpoch
  (final PS queue,
   final TreeMap<Double, LinkedHashSet<J>> runningJobs,
   final LinkedHashMap<J, Double> arrivalTimes,
   final LinkedHashMap<J, Double> startTimes,
   final Map<J, Double> remainingServiceTime,
   final LinkedHashMap<J, Double> revocationTime,
   final Map<J, JobQueueVisitLog<J, PS>> visitLogs)
    throws SimQueuePredictionAmbiguityException
  {
    // Perform rigorous sanity checks on our arguments.
    sanityNextRunningEpoch (queue, runningJobs, arrivalTimes, startTimes, remainingServiceTime, revocationTime, visitLogs);
    // Remove the first entry from runningJobs - we will deal with it completely.
    // Record the current time and the (number of) jobs executing and verify that there are actually jobs present.
    final Entry<Double, LinkedHashSet<J>> runningJobsNow = runningJobs.pollFirstEntry ();
    final double now = runningJobsNow.getKey ();
    final LinkedHashSet<J> jobSet = runningJobsNow.getValue ();
    if (jobSet.isEmpty ())
      throw new IllegalArgumentException ();
    final int jobSetSize = jobSet.size ();
    // Find the next arrival time from runningJobs peeking at the next "even" in runningJobs.
    final double nextArrivalTime = runningJobs.isEmpty () ? Double.POSITIVE_INFINITY : runningJobs.firstKey ();
    // Find the virtual departure times (ignoring other future "events")
    // of all jobs present (in jobSet); put them in the departureTimesMap.
    // Mind the induced delay due to multiple jobs being served at once.
    final TreeMap<Double, LinkedHashSet<J>> departureTimesMap = new TreeMap<> ();
    for (final J j : jobSet)
    {
      final double departureTime = now + (remainingServiceTime.get (j) * jobSetSize);
      if (! departureTimesMap.containsKey (departureTime))
        departureTimesMap.put (departureTime, new LinkedHashSet<> ());
      departureTimesMap.get (departureTime).add (j);
    }
    // Find the first virtual departure time and the set of jobs departing at that instant.
    final Entry<Double, LinkedHashSet<J>> firstDepartureEntry = departureTimesMap.firstEntry ();
    final double firstDepartureTime = firstDepartureEntry.getKey ();
    final LinkedHashSet<J> firstDepartures = firstDepartureEntry.getValue ();
    // Find the virtual revocation times (ignoring other future "events")
    // of all jobs present (in jobSet) from revocationTime; put them in the revocationTimesMap.
    final TreeMap<Double, LinkedHashSet<J>> revocationTimesMap = new TreeMap<> ();
    for (final Entry<J, Double> revocationEntry : revocationTime.entrySet ())
    {
      final J jobToRevoke = revocationEntry.getKey ();
      final double jobRevocationTime = revocationEntry.getValue ();
      if (! revocationTimesMap.containsKey (jobRevocationTime))
        revocationTimesMap.put (jobRevocationTime, new LinkedHashSet<> ());
      revocationTimesMap.get (jobRevocationTime).add (jobToRevoke);
    }
    // Find the first virtual revocation time and the set of jobs being revoked at that instant.
    final double firstRevocationTime = (revocationTimesMap.isEmpty () ? Double.POSITIVE_INFINITY : revocationTimesMap.firstKey ());
    final LinkedHashSet<J> firstRevocations = (revocationTimesMap.isEmpty () ? null : revocationTimesMap.firstEntry ().getValue ());
    // Check whether we have to do departures or revocations before the next arrival, or both.
    final boolean doRevocation =
      (firstRevocations != null
      && firstRevocationTime <= nextArrivalTime
      && firstRevocationTime <= firstDepartureTime);
    final boolean doDeparture = (firstDepartureTime <= nextArrivalTime && firstDepartureTime <= firstRevocationTime);
    // We now know the next "event time".
    final double nextEventTime = (doRevocation ? firstRevocationTime : (doDeparture ? firstDepartureTime : nextArrivalTime));
    // Check for the simultaneous revocation and departure of a job - we cannot deal with that (ambiguity).
    if (doRevocation && doDeparture)
    {
      for (final J j : firstRevocations)
        if (firstDepartures.contains (j))
          throw new SimQueuePredictionAmbiguityException ();
    }
    if (doRevocation)
      for (final J j : firstRevocations)
      {
        final double arrivalTime_j = arrivalTimes.get (j);
        final boolean started_j = true;
        final double startTime_j = startTimes.get (j);
        final double revocationTime_j = nextEventTime;
        addRevokedJob_SingleVisit (visitLogs, queue, j, arrivalTime_j, started_j, startTime_j, revocationTime_j);
        jobSet.remove (j);
        for (final Set<J> runningJobSet : runningJobs.values ())
          runningJobSet.remove (j);
        arrivalTimes.remove (j);
        startTimes.remove (j);
        remainingServiceTime.remove (j);
        revocationTime.remove (j);
      }
    if (doDeparture)
      for (final J j : firstDepartures)
      {
        if (revocationTime.containsKey (j))
          // Revocation after departure.
          throw new IllegalArgumentException ();
        final double arrivalTime_j = arrivalTimes.get (j);
        final boolean started_j = true;
        final double startTime_j = startTimes.get (j);
        final double departureTime_j = nextEventTime;
        addDeparture_SingleVisit (visitLogs, queue, j, arrivalTime_j, started_j, startTime_j, departureTime_j);
        jobSet.remove (j);
        for (final Set<J> runningJobSet : runningJobs.values ())
          runningJobSet.remove (j);
        arrivalTimes.remove (j);
        startTimes.remove (j);
        remainingServiceTime.remove (j);
        revocationTime.remove (j);
      }
    for (final J j : jobSet)
      remainingServiceTime.put (j, remainingServiceTime.get (j) - ((nextEventTime - now) / jobSetSize));
    if ((doRevocation || doDeparture) && nextEventTime < nextArrivalTime && ! runningJobs.isEmpty ())
      runningJobs.put (nextEventTime, jobSet);
  }
  
}
