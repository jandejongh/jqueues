package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LJF;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.SJF;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r5.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r5.util.loadfactory.AbstractLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 001.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class LoadFactory_SQ_SV_001<J extends SimJob, Q extends SimQueue>
extends AbstractLoadFactory_SQ_SV<J, Q>
{

  private final Random rngRequestedServiceTimeJitter = new Random ();
  
  /** Creates a suitable map for the requested service time for a job visit to a queue.
   * 
   * <p>
   * For specific queue types, i.c.,
   * {@link FCFS_B}, {@link NoBuffer_c}, {@link LCFS}, {@link SJF}, {@link LJF},
   * a jitter from U[-0.01, +0.01] is added to the service time.
   * 
   * @param queue The queue.
   * @param n     The job number.
   * 
   * @return A map holding the service time (i.e., the job number) at the queue.
   * 
   * @see SimJobFactory#newInstance For the use of the map generated.
   * 
   */
  protected Map<Q, Double> generateRequestedServiceTimeMap (final Q queue, final int n)
  {
    final Map<Q, Double> requestedServiceTimeMap = new HashMap ();
    final double requestedServiceTimeJitter =
      (((queue instanceof FCFS_B)
        || (queue instanceof NoBuffer_c)
        || (queue instanceof LCFS)
        || (queue instanceof SJF)
        || (queue instanceof LJF))
      ? 0.01 * (2.0 * this.rngRequestedServiceTimeJitter.nextDouble () - 1.0)
      : 0.0);
    requestedServiceTimeMap.put (queue, ((double) n) + requestedServiceTimeJitter);
    return requestedServiceTimeMap;
  }
  
  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the requested number of jobs, and number them starting with one;
   * <li> set the requested service time for each job equal to its job number;
   * <li> schedules a single arrival for each job at time equal to its job number.
   * </ul>
   * 
   * <p>
   * Jobs are returned in a {@link LinkedHashSet}, preserving the creation order of the jobs.
   * 
   * @see SimEntityEventScheduler#schedule
   * 
   */
  @Override
  public Set<J> generate
  (final SimEventList eventList,
    boolean attachSimJobsToEventList,
    final Q queue,
    final SimJobFactory<J, Q> jobFactory,
    final int numberOfJobs,
    final boolean reset,
    final double resetTime,
    final NavigableMap<Double, Set<SimEntityEvent>> queueExternalEvents)
  {
    if (eventList == null || queue == null || jobFactory == null)
      throw new IllegalArgumentException ();
    if (numberOfJobs < 0)
      throw new IllegalArgumentException ();
    final Set<J> jobs = new LinkedHashSet<> ();
    final NavigableMap<Double, Set<SimEntityEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimEntityEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final SimEventList jobEventList = (attachSimJobsToEventList ? eventList : null);
    for (int i = 1; i <= numberOfJobs; i++)
    {
      final J job = jobFactory.newInstance (jobEventList, Integer.toString (i), generateRequestedServiceTimeMap (queue, i));
      final SimEntityEvent<J, Q> arrivalSchedule = new SimQueueJobArrivalEvent (job, queue, (double) i);
      if (! realQueueExternalEvents.containsKey ((double) i))
        realQueueExternalEvents.put ((double) i, new LinkedHashSet<> ());
      realQueueExternalEvents.get ((double) i).add (arrivalSchedule);
      eventsToSchedule.add (arrivalSchedule);
      jobs.add (job);
    }
    SimEntityEventScheduler.schedule (eventList, reset, resetTime, eventsToSchedule);
    return jobs;
  }
  
}
