package nl.jdj.jqueues.r4.util.loadfactory.pattern;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimJobFactory;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r4.event.SimQueueAccessVacationEvent;
import nl.jdj.jsimulation.r4.SimEventList;

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
   * <li> adds queue-access vacations from 9.5 until 10.5, 19.5 until 20.5, etc.
   * </ul>
   * 
   * <p>
   * This should effectively make the queue drop every 10th job generated.
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
    final NavigableMap<Double, Set<SimEntityEvent<J, Q>>> queueExternalEvents)
  {
    final Set<J> jobs = super.generate (eventList, attachSimJobsToEventList,
      queue, jobFactory, numberOfJobs, reset, resetTime, queueExternalEvents);
    final int numberOfQavToSchedule = Math.min (1, jobs.size () / 10);
    final Set<SimEntityEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    for (int i = 1; i <= numberOfQavToSchedule; i++)
    {
      final SimEntityEvent<J, Q> qavOnSchedule = new SimQueueAccessVacationEvent<> (queue, 10.0 * i - 0.5, true);
      eventsToSchedule.add (qavOnSchedule);
      final SimEntityEvent<J, Q> qavOffSchedule = new SimQueueAccessVacationEvent<> (queue, 10.0 * i + 0.5, false);
      eventsToSchedule.add (qavOffSchedule);
    }
    SimEntityEventScheduler.schedule (eventList, reset, resetTime, eventsToSchedule);
    return jobs;
  }
  
}
