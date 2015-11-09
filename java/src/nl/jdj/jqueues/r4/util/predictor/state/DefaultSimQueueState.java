package nl.jdj.jqueues.r4.util.predictor.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;

/** A default implementation of {@link SimQueueState}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimQueueState<J extends SimJob, Q extends SimQueue>
implements SimQueueState<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new state object for give queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  public DefaultSimQueueState
  (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    this.queue = queue;
    this.time = Double.NEGATIVE_INFINITY;
    this.queueAccessVacation = false;
    this.jobArrivalsMap = new TreeMap<> ();
    this.arrivalTimesMap = new HashMap<> ();
    this.serverAccessCredits = Integer.MAX_VALUE;
    this.startTimesMap = new HashMap<> ();
    this.jobsExecutingMap = new TreeMap<> ();
    this.remainingServiceMap = new TreeMap<> ();
    this.jobRemainingServiceTimeMap = new HashMap<> ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Q queue;

  @Override
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public void reset ()
  {
    this.time = Double.NEGATIVE_INFINITY;
    this.queueAccessVacation = false;
    this.jobArrivalsMap.clear ();
    this.arrivalTimesMap.clear ();
    this.serverAccessCredits = Integer.MAX_VALUE;
    this.startTimesMap.clear ();
    this.jobsExecutingMap.clear ();
    this.remainingServiceMap.clear ();
    this.jobRemainingServiceTimeMap.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private double time;

  @Override
  public final double getTime ()
  {
    return this.time;
  }

  @Override
  public final void setTime (final double time)
  {
    if (time < this.time)
      throw new IllegalArgumentException ();
    this.time = time;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean queueAccessVacation = false;

  @Override
  public final boolean isQueueAccessVacation ()
  {
    return this.queueAccessVacation;
  }

  @Override
  public final void startQueueAccessVacation (final double time)
  {
    setTime (time);
    this.queueAccessVacation = true;
  }

  @Override
  public final void stopQueueAccessVacation (final double time)
  {
    setTime (time);
    this.queueAccessVacation = false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final NavigableMap<Double, List<J>> jobArrivalsMap;
  
  @Override
  public final NavigableMap<Double, List<J>> getJobArrivalsMap ()
  {
    return this.jobArrivalsMap;
  }
  
  private final Map<J, Double> arrivalTimesMap;
    
  @Override
  public final Map<J, Double> getArrivalTimesMap ()
  {
    return this.arrivalTimesMap;
  }

  @Override
  public final Set<J> getJobs ()
  {
    return SimQueueState.super.getJobs ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS WAITING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public final Set<J> getJobsWaiting ()
  {
    return SimQueueState.super.getJobsWaiting ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private int serverAccessCredits = Integer.MAX_VALUE;
  
  @Override
  public int getServerAccessCredits ()
  {
    return this.serverAccessCredits;
  }
  
  @Override
  public void setServerAccessCredits (final double time, final int credits)
  {
    if (credits < 0)
      throw new IllegalArgumentException ();
    setTime (time);
    this.serverAccessCredits = credits;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS EXECUTING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<J, Double> startTimesMap;  
  
  @Override
  public final Map<J, Double> getStartTimesMap ()
  {
    return this.startTimesMap;
  }
  
  private final NavigableMap<Double, Set<J>> jobsExecutingMap;  
  
  @Override
  public final NavigableMap<Double, Set<J>> getJobsExecutingMap ()
  {
    return this.jobsExecutingMap;
  }
  
  @Override
  public final Set<J> getJobsExecuting ()
  {
    return SimQueueState.super.getJobsExecuting ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS EXECUTING - REMAINING SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final NavigableMap<Double, List<J>> remainingServiceMap;
  
  @Override
  public final NavigableMap<Double, List<J>> getRemainingServiceMap ()
  {
    return this.remainingServiceMap;
  }  
  
  private final Map<J, Double> jobRemainingServiceTimeMap;
  
  @Override
  public final Map<J, Double> getJobRemainingServiceTimeMap ()
  {
    return this.jobRemainingServiceTimeMap;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void doArrivals (final double time, final Set<J> arrivals, final Set<JobQueueVisitLog<J, Q>> visitLogs)
  {
    if (arrivals == null || arrivals.contains (null))
      throw new IllegalArgumentException ();
    setTime (time);
    for (final J job : arrivals)
      if (getJobs ().contains (job))
        throw new IllegalArgumentException ();
    if (this.queueAccessVacation)
    {
      if (visitLogs != null)
        for (final J job : arrivals)
          JobQueueVisitLog.addDroppedJob (visitLogs, this.queue, job, time, false, Double.NaN, time);
    }
    else
    {
      for (final J job : arrivals)
      {
        if (! this.jobArrivalsMap.containsKey (time))
          this.jobArrivalsMap.put (time, new ArrayList<> ());
        this.jobArrivalsMap.get (time).add (job);
        this.arrivalTimesMap.put (job, time);
      }
    }
  }

  @Override
  public void doStarts (final double time, final Set<J> starters)
  {
    if (starters == null || starters.contains (null))
      throw new IllegalArgumentException ();
    setTime (time);
    for (final J job : starters)
      if (! getJobsWaiting ().contains (job))
        throw new IllegalArgumentException ();
    if (this.serverAccessCredits < starters.size ())
      throw new IllegalArgumentException ();
    if (this.serverAccessCredits < Integer.MAX_VALUE)
      this.serverAccessCredits -= starters.size ();
    for (final J job : starters)
    {
      this.startTimesMap.put (job, time);
      if (! this.jobsExecutingMap.containsKey (time))
        this.jobsExecutingMap.put (time, new LinkedHashSet<> ());
      this.jobsExecutingMap.get (time).add (job);
      final double rsJob = job.getServiceTime (this.queue);
      if (! this.remainingServiceMap.containsKey (rsJob))
        this.remainingServiceMap.put (rsJob, new ArrayList<> ());
      this.remainingServiceMap.get (rsJob).add (job);
      this.jobRemainingServiceTimeMap.put (job, rsJob);
    }
  }

  @Override
  public void doExits (final double time,
    final Set<J> drops, final Set<J> revocations, final Set<J> departures, final Set<J> stickers,
    final Set<JobQueueVisitLog<J, Q>> visitLogs)
  {
    if ((drops != null && drops.contains (null))
      || (revocations != null && revocations.contains (null))
      || (departures != null && departures.contains (null))
      || (stickers != null && stickers.contains (null)))
      throw new IllegalArgumentException ();
    final Set<J> allExits = new LinkedHashSet<> ();
    int allExitsOffered = 0;
    if (drops != null)
    {
      allExits.addAll (drops);
      allExitsOffered += drops.size ();
    }
    if (revocations != null)
    {
      allExits.addAll (revocations);
      allExitsOffered += revocations.size ();
    }
    if (departures != null)
    {
      allExits.addAll (departures);
      allExitsOffered += departures.size ();
    }
    if (stickers != null)
    {
      allExits.addAll (stickers);
      allExitsOffered += stickers.size ();
    }
    if (allExitsOffered == 0 || allExitsOffered > allExits.size ())
      throw new IllegalArgumentException ();
    setTime (time);
    for (final J job : allExits)
    {
      final double arrivalTime = this.arrivalTimesMap.get (job);
      final boolean started = this.startTimesMap.containsKey (job);
      final double startTime = (started ? this.startTimesMap.get (job) : Double.NaN);
      if (visitLogs != null)
      {
        if (drops != null && drops.contains (job))
          JobQueueVisitLog.addDroppedJob (visitLogs, this.queue, job, arrivalTime, started, startTime, time);
        else if (revocations != null && revocations.contains (job))
          JobQueueVisitLog.addRevokedJob (visitLogs, this.queue, job, arrivalTime, started, startTime, time);
        else if (departures != null && departures.contains (job))
          JobQueueVisitLog.addDepartedJob (visitLogs, this.queue, job, arrivalTime, started, startTime, time);
        else if (stickers != null && stickers.contains (job))
          JobQueueVisitLog.addStickyJob (visitLogs, this.queue, job, arrivalTime, started, startTime);
        else
          throw new RuntimeException ();
      }
      this.jobArrivalsMap.get (arrivalTime).remove (job);
      if (this.jobArrivalsMap.get (arrivalTime).isEmpty ())
        this.jobArrivalsMap.remove (arrivalTime);
      this.arrivalTimesMap.remove (job);
      if (started)
      {
        this.startTimesMap.remove (job);
        this.jobsExecutingMap.get (startTime).remove (job);
        if (this.jobsExecutingMap.get (startTime).isEmpty ())
          this.jobsExecutingMap.remove (startTime);
        final double rsJob = this.jobRemainingServiceTimeMap.get (job);
        this.jobRemainingServiceTimeMap.remove (job);
        this.remainingServiceMap.get (rsJob).remove (job);
        if (this.remainingServiceMap.get (rsJob).isEmpty ())
          this.remainingServiceMap.remove (rsJob);
      }
    }
  }
  
}
