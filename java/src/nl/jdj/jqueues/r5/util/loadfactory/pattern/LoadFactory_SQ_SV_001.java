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
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r5.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r5.util.loadfactory.AbstractLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
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

  /** A load-factory hint enforcing jitter on the service-time requirement of jobs (e.g., in order to avoid ambiguities).
   * 
   */
  public static final LoadFactoryHint SERVICE_TIME_JITTER = new LoadFactoryHint ()
  {
    @Override
    public final String toString ()
    {
      return "SERVICE_TIME_JITTER";
    }
  };
  
  private final Random rngRequestedServiceTimeJitter = new Random ();
  
  /** Creates a suitable map for the requested service time for a job visit to a queue.
   * 
   * <p>
   * Upon request, a jitter from U[-0.01, +0.01] is added to the service time.
   * This is typically used to avoid ambiguities in the schedule.
   * 
   * @param queue The queue.
   * @param n     The job number.
   * @param jitter Whether to apply jitter to the requested service time.
   * 
   * @return A map holding the service time (i.e., the job number) at the queue.
   * 
   * @see SimJobFactory#newInstance For the use of the map generated.
   * @see LoadFactory_SQ_SV_001#SERVICE_TIME_JITTER
   * 
   */
  protected Map<Q, Double> generateRequestedServiceTimeMap (final Q queue, final int n, final boolean jitter)
  {
    final Map<Q, Double> requestedServiceTimeMap = new HashMap ();
    final double requestedServiceTimeJitter =
      jitter
      ? 0.01 * (2.0 * this.rngRequestedServiceTimeJitter.nextDouble () - 1.0)
      : 0.0;
    requestedServiceTimeMap.put (queue, ((double) n) + requestedServiceTimeJitter);
    return requestedServiceTimeMap;
  }
  
  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the requested number of jobs, and number them starting with one;
   * <li> set the requested service time for each job equal to its job number (adding jitter if requested through
   *      {@link LoadFactory_SQ_SV_001#SERVICE_TIME_JITTER});
   * <li> schedules a single arrival for each job at time equal to its job number.
   * </ul>
   * 
   * <p>
   * Jobs are returned in a {@link LinkedHashSet}, preserving the creation order of the jobs.
   * 
   * @see SimEntityEventScheduler#schedule
   * @see LoadFactory_SQ_SV_001#SERVICE_TIME_JITTER
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
    final Set<LoadFactoryHint> hints,
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
    final boolean jitter = (hints != null && hints.contains (LoadFactory_SQ_SV_001.SERVICE_TIME_JITTER));
    for (int i = 1; i <= numberOfJobs; i++)
    {
      final J job = jobFactory.newInstance
        (jobEventList, Integer.toString (i), generateRequestedServiceTimeMap (queue, i, jitter));
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
