package nl.jdj.jqueues.r5.util.predictor.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToDoubleBiFunction;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;

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
    this.time = Double.NaN;
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
  // HANDLERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<String, SimQueueStateHandler> handlerNameMap = new HashMap<> ();
  
  /** Registers a handler.
   * 
   * <p>
   * A (extension) handler allow for extending the queue-state representation with
   * additional state variables without creating a subclass of {@link DefaultSimQueueState} for that
   * (which would lead to problems in case we have multiple such extensions).
   * 
   * @param handler The handler.
   * 
   * @throws IllegalArgumentException If the handler or its name are {@code null},
   *                                  or if a handler with the same name has been registered already.
   * 
   * @see SimQueueStateHandler
   * 
   */
  public final void registerHandler (final SimQueueStateHandler handler)
  {
    if (handler == null
      || handler.getHandlerName () == null
      || this.handlerNameMap.containsKey (handler.getHandlerName ()))
      throw new IllegalArgumentException ();
    this.handlerNameMap.put (handler.getHandlerName (), handler);
    handler.initHandler (this);
  }
  
  /** Gets a handler by name.
   * 
   * @param name The name to look for.
   * 
   * @return The handler, or {@code null} if not found.
   * 
   */
  public final SimQueueStateHandler getHandler (final String name)
  {
    return this.handlerNameMap.get (name);
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
    this.time = Double.NaN;
    this.queueAccessVacation = false;
    this.jobArrivalsMap.clear ();
    this.arrivalTimesMap.clear ();
    this.serverAccessCredits = Integer.MAX_VALUE;
    this.startTimesMap.clear ();
    this.jobsExecutingMap.clear ();
    this.remainingServiceMap.clear ();
    this.jobRemainingServiceTimeMap.clear ();
    for (SimQueueStateHandler handler : this.handlerNameMap.values ())
      handler.resetHandler (this);
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
    if (Double.isNaN (time))
      throw new IllegalArgumentException ();
    if ((! Double.isNaN (this.time)) && time < this.time)
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
  
  @Override
  public final Set<J> getJobsWaitingOrdered ()
  {
    return SimQueueState.super.getJobsWaitingOrdered ();
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
  
  private ToDoubleBiFunction<Q, J> serviceTimeProvider = null;
  
  /** Gets the service time of a job at a queue (central entry point).
   * 
   * @param queue The queue.
   * @param job   The job.
   * @return      The jobs service time, either taken from {@link SimJob#getServiceTime} or
   *              from the service-time provider (if installed).
   * 
   */
  public final double getServiceTime (final Q queue, final J job)
  {
    if (this.serviceTimeProvider == null)
      return job.getServiceTime (this.queue);
    else
      return this.serviceTimeProvider.applyAsDouble (queue, job);
  }
  
  /** Sets a service-time provider, overruling job-settings for obtaining the required service time for a job visit.
   * 
   * @param serviceTimeProvider The service-time provider; its two arguments are the queue and job, respectively.
   * 
   */
  public void setServiceTimeProvider (final ToDoubleBiFunction<Q, J> serviceTimeProvider)
  {
    this.serviceTimeProvider = serviceTimeProvider;
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
      final double rsJob = getServiceTime (this.queue, job);
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
      // Drops may occur for jobs not present, e.g., in the DROP queue.
      if (drops != null && drops.contains (job) && ! this.arrivalTimesMap.containsKey (job))
      {
        JobQueueVisitLog.addDroppedJob (visitLogs, this.queue, job, time, false, Double.NaN, time);
        continue;
      }
      // Likewise for departures, e.g., in the ZERO queue.
      if (departures != null && departures.contains (job) && ! this.arrivalTimesMap.containsKey (job))
      {
        JobQueueVisitLog.addDepartedJob (visitLogs, this.queue, job, time, false, Double.NaN, time);
        continue;
      }
      // Take care that revocation requests may target jobs not present (anymore).
      if (revocations != null && revocations.contains (job) && ! this.arrivalTimesMap.containsKey (job))
        continue;
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
