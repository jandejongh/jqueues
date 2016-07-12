package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r5.event.SimQueueAccessVacationEvent;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 002.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class LoadFactory_SQ_SV_002<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_001<J, Q>
{

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_001#generate};
   * <li> adds queue-access vacations from 2.5 until 3.5, 5.5 until 6.5, etc.
   * </ul>
   * 
   * <p>
   * This should effectively make the queue drop every 3rd job generated.
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
    final Set<J> jobs = super.generate (eventList, attachSimJobsToEventList,
      queue, jobFactory, numberOfJobs, reset, resetTime, hints, queueExternalEvents);
    final NavigableMap<Double, Set<SimEntityEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final int numberOfQavToSchedule = Math.max (1, jobs.size () / 3);
    final Set<SimEntityEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    for (int i = 1; i <= numberOfQavToSchedule; i++)
    {
      final double startQavTime = 3.0 * i - 0.5;
      final double endQavTime  = 3.0 * i + 0.5;
      final SimEntityEvent<J, Q> qavOnSchedule = new SimQueueAccessVacationEvent<> (queue, startQavTime, true);
      if (! realQueueExternalEvents.containsKey (startQavTime))
        realQueueExternalEvents.put (startQavTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (startQavTime).add (qavOnSchedule);
      eventsToSchedule.add (qavOnSchedule);
      final SimEntityEvent<J, Q> qavOffSchedule = new SimQueueAccessVacationEvent<> (queue, endQavTime, false);
      if (! realQueueExternalEvents.containsKey (endQavTime))
        realQueueExternalEvents.put (endQavTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (endQavTime).add (qavOffSchedule);
      eventsToSchedule.add (qavOffSchedule);
    }
    // Be careful not to reset the event list (again) here!
    SimEntityEventScheduler.schedule (eventList, false, Double.NaN, eventsToSchedule);
    return jobs;
  }
  
}
