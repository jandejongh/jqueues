package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimQueueAccessVacationEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.SimQueueServerAccessCreditsEvent;

/** A default implementation of {@link WorkloadSchedule}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultWorkloadSchedule<J extends SimJob, Q extends SimQueue>
implements WorkloadSchedule<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueue>
  Set<Q>
  getAllSimQueues
  (final Set<SimEntityEvent<J, Q>> queueEvents)
  {
    final Set<Q> allQueues = new HashSet<> ();
    if (queueEvents != null)
      for (final SimEntityEvent<J, Q> qe : queueEvents)
        if (qe != null)
          allQueues.add (qe.getQueue ());
    return allQueues;
  }
  
  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimEntityEvent}s.
   * 
   * @param queues      The queues to consider; events related to other queues are ignored; if {@code null},
   *                    consider all queues found in the {@code queueEvents} set.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   */
  public DefaultWorkloadSchedule (final Set<Q> queues, final Set<SimEntityEvent<J, Q>> queueEvents)
  {
    if (queueEvents != null)
      this.queueEvents.addAll (queueEvents);
    this.queueEvents.remove (null);
    if (queues != null)
      this.queues.addAll (queues);
    else
      this.queues.addAll (DefaultWorkloadSchedule.getAllSimQueues (queueEvents));
    for (Q q : this.queues)
    {
      this.qavTimesMap.put (q, new TreeMap<> ());
      this.arrTimesMap.put (q, new HashMap<> ());
      this.timeArrsMap.put (q, new TreeMap<> ());
      this.revTimesMap.put (q, new HashMap<> ());
      this.timeRevsMap.put (q, new TreeMap<> ());
      this.sacTimesMap.put (q, new TreeMap<> ());
    }
    if (queueEvents != null)
    {
      for (final SimEntityEvent<J, Q> event : queueEvents)
      {
        final double time = event.getTime ();
        final Q queue = event.getQueue ();
        final J job = event.getJob ();
        if (this.queues.contains (queue))
        {
          if (event instanceof SimQueueAccessVacationEvent)
          {
            this.processedQueueEvents.add (event);
            final boolean vacation = ((SimQueueAccessVacationEvent) event).getVacation ();
            final NavigableMap<Double, List<Boolean>> qavTimesMap_q = this.qavTimesMap.get (queue);
            if (! qavTimesMap_q.containsKey (time))
              qavTimesMap_q.put (time, new ArrayList<> ());
            qavTimesMap_q.get (time).add (vacation);
          }
          else if (event instanceof SimQueueJobArrivalEvent)
          {
            this.processedQueueEvents.add (event);
            this.jobs.add (job);
            final Map<J, List<Double>> arrTimesMap_q = this.arrTimesMap.get (queue);
            if (! arrTimesMap_q.containsKey (job))
              arrTimesMap_q.put (job, new ArrayList<> ());
            arrTimesMap_q.get (job).add (time);
            final NavigableMap<Double, List<J>> timeArrsMap_q = this.timeArrsMap.get (queue);
            if (! timeArrsMap_q.containsKey (time))
              timeArrsMap_q.put (time, new ArrayList<> ());
            timeArrsMap_q.get (time).add (job);
          }
          else if (event instanceof SimQueueJobRevocationEvent)
          {
            this.processedQueueEvents.add (event);
            this.jobs.add (job);
            final boolean interruptService = ((SimQueueJobRevocationEvent) event).isInterruptService ();
            final  Map<J, List<Map<Double, Boolean>>> revTimesMap_q = this.revTimesMap.get (queue);
            if (! revTimesMap_q.containsKey (job))
              revTimesMap_q.put (job, new ArrayList<> ());
            final Map<Double, Boolean> timeIsMap = new HashMap<> ();
            timeIsMap.put (time, interruptService);
            revTimesMap_q.get (job).add (timeIsMap);
            final NavigableMap<Double, List<Map<J, Boolean>>> timeRevsMap_q = this.timeRevsMap.get (queue);
            if (! timeRevsMap_q.containsKey (time))
              timeRevsMap_q.put (time, new ArrayList<> ());
            final Map<J, Boolean> jobIsMap = new HashMap<> ();
            jobIsMap.put (job, interruptService);
            timeRevsMap_q.get (time).add (jobIsMap);
          }
          else if (event instanceof SimQueueServerAccessCreditsEvent)
          {
            this.processedQueueEvents.add (event);
            final int credits = ((SimQueueServerAccessCreditsEvent) event).getCredits ();
            final NavigableMap<Double, List<Integer>> sacTimesMap_q = this.sacTimesMap.get (queue);
            if (! sacTimesMap_q.containsKey (time))
              sacTimesMap_q.put (time, new ArrayList<> ());
            sacTimesMap_q.get (time).add (credits);
          }
          else
            throw new UnsupportedOperationException ();
        }
      }
    }
  }
  
  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimEntityEvent}s.
   * 
   * <p>
   * With this constructor, all queues found in the {@code queueEvent}s are considered.
   * 
   * <p>
   * Implemented as {@code this (null, queueEvents)}.
   * 
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   */
  public DefaultWorkloadSchedule (final Set<SimEntityEvent<J, Q>> queueEvents)
  {
    this (null, queueEvents);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimEntityEvent> queueEvents = new LinkedHashSet<> ();
  
  @Override
  public final Set<SimEntityEvent> getQueueEvents ()
  {
    return Collections.unmodifiableSet (this.queueEvents);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESSED QUEUE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimEntityEvent> processedQueueEvents = new LinkedHashSet<> ();
  
  @Override
  public final Set<SimEntityEvent> getProcessedQueueEvents ()
  {
    return Collections.unmodifiableSet (this.processedQueueEvents);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<Q> queues = new HashSet<> ();
  
  @Override
  public final Set<Q> getQueues ()
  {
    return Collections.unmodifiableSet (this.queues);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<Q, NavigableMap<Double, List<Boolean>>> qavTimesMap = new HashMap<> ();
  
  @Override
  public final NavigableMap<Double, List<Boolean>> getQueueAccessVacationMap (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.qavTimesMap.get (queue));
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<J> jobs = new HashSet<> ();
  
  @Override
  public final Set<J> getJobs ()
  {
    return Collections.unmodifiableSet (this.jobs);
  }

  @Override
  public final Set<J> getJobs (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.EMPTY_SET;
    final Set<J> jobs_q = new HashSet<> (this.arrTimesMap.get (queue).keySet ());
    jobs_q.addAll (this.revTimesMap.get (queue).keySet ());
    return Collections.unmodifiableSet (jobs_q);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<Q, Map<J, List<Double>>> arrTimesMap = new HashMap<> ();
  
  private final Map<Q, NavigableMap<Double, List<J>>> timeArrsMap = new HashMap<> ();
 
  @Override
  public final Map<J, List<Double>> getArrivalTimesMap (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.EMPTY_MAP;
    else
      return Collections.unmodifiableMap (this.arrTimesMap.get (queue));
  }
  
  @Override
  public final NavigableMap<Double, List<J>> getJobArrivalsMap (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.timeArrsMap.get (queue));    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<Q, Map<J, List<Map<Double, Boolean>>>> revTimesMap = new HashMap<> ();
  
  private final Map<Q, NavigableMap<Double, List<Map<J, Boolean>>>> timeRevsMap = new HashMap<> ();
  
  @Override
  public final Map<J, List<Map<Double, Boolean>>> getRevocationTimesMap (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.EMPTY_MAP;
    else
      return Collections.unmodifiableMap (this.revTimesMap.get (queue));
  }
  
  @Override
  public final NavigableMap<Double, List<Map<J, Boolean>>> getJobRevocationsMap (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.timeRevsMap.get (queue));    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<Q, NavigableMap<Double, List<Integer>>> sacTimesMap = new HashMap<> ();

  @Override
  public final NavigableMap<Double, List<Integer>> getServerAccessCreditsMap (final Q queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.sacTimesMap.get (queue));
  }
  
}
