package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
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

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0002.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class LoadFactory_SQ_SV_0002<J extends SimJob, Q extends SimQueue>
extends AbstractLoadFactory_SQ_SV<J, Q>
{

  /** Creates a suitable map for the requested service time for a job visit to a queue.
   * 
   * <p>
   * A job with zero index and zero requested service time is returned.
   * 
   * @param queue The queue.
   * 
   * @return A map holding the service time (i.e., the job number) at the queue.
   * 
   * @see SimJobFactory#newInstance For the use of the map generated.
   * 
   */
  protected Map<Q, Double> generateRequestedServiceTimeMap (final Q queue)
  {
    final Map<Q, Double> requestedServiceTimeMap = new HashMap ();
    requestedServiceTimeMap.put (queue, 0.0);
    return requestedServiceTimeMap;
  }
  
  /** Generates the load.
   * 
   * <p>
   * This method generates a single job arriving at t=0 with zero requested service time (ignoring the {@code numberOfJobs}
   * argument).
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
    final Set<LoadFactoryHint> hints,
    final NavigableMap<Double, Set<SimEntityEvent>> queueExternalEvents)
  {
    if (eventList == null || queue == null || jobFactory == null)
      throw new IllegalArgumentException ();
    final Set<J> jobs = new LinkedHashSet<> ();
    final NavigableMap<Double, Set<SimEntityEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimEntityEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final SimEventList jobEventList = (attachSimJobsToEventList ? eventList : null);
    final J job = jobFactory.newInstance
      (jobEventList, Integer.toString (1), generateRequestedServiceTimeMap (queue));
    final SimEntityEvent<J, Q> arrivalSchedule = new SimQueueJobArrivalEvent (job, queue, 0.0);
    realQueueExternalEvents.put (0.0, new LinkedHashSet<> ());
    realQueueExternalEvents.get (0.0).add (arrivalSchedule);
    eventsToSchedule.add (arrivalSchedule);
    jobs.add (job);
    SimEntityEventScheduler.schedule (eventList, reset, resetTime, eventsToSchedule);
    return jobs;
  }
  
}