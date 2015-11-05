package nl.jdj.jqueues.r4.util.loadfactory.pattern;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimJobFactory;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.loadfactory.AbstractLoadFactorySingleQueueSingleVisit;
import nl.jdj.jqueues.r4.util.loadfactory.LoadFactorySingleQueueSingleVisit;
import nl.jdj.jqueues.r4.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.SimEntityEventScheduler;
import nl.jdj.jsimulation.r4.SimEventList;

/** A concrete {@link LoadFactorySingleQueueSingleVisit}, pattern 01.
 *
 * @see #generate
 * 
 */
public class LoadFactorySingleQueueSingleVisit_01<J extends SimJob, Q extends SimQueue>
extends AbstractLoadFactorySingleQueueSingleVisit<J, Q>
{

  /** Creates a suitable map for the requested service time for a job visit to a queue.
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
    requestedServiceTimeMap.put (queue, (double) n);
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
    final TreeMap<Double, LinkedHashSet<SimEntityEvent<J, Q>>> queueExternalEvents)
  {
    if (eventList == null || queue == null || jobFactory == null)
      throw new IllegalArgumentException ();
    if (numberOfJobs < 0)
      throw new IllegalArgumentException ();
    final Set<J> jobs = new LinkedHashSet<> ();
    final TreeMap<Double, LinkedHashSet<SimEntityEvent<J, Q>>> realQueueExternalEvents =
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
