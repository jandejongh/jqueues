package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r5.event.SimQueueServerAccessCreditsEvent;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0013.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class LoadFactory_SQ_SV_0013<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0010<J, Q>
{

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_0010#generate};
   * <li> adds setting server-access credits 6.75, 13.75, 20.75, etc.,
   *      with a jitter on the schedule time in U[-0.001, +0.001].
   * </ul>
   * 
   * <p>
   * The jitter on the schedule time is often required (e.g., in FCFS) to avoid ambiguities.
   * If the server-access credits (sac) set are (relatively) small,
   * the start times tend to synchronize to the
   * scheduled sac-settings.
   * Given the integral length of the interval between setting the server-access credits,
   * and the integral service time in {@link LoadFactory_SQ_SV_0010#generate},
   * this will lead to ambiguities between departures/starts and scheduled sac-settings.
   * 
   * <p>
   * The amount of credits is 0, 1, or 2 with equal probabilities.
   * Setting the credits is scheduled roughly until all jobs would have been served under single-server FCFS,
   * with an additional 100% to account for the server-access credits delays.
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
    final int numberOfSacToSchedule = Math.max (1, jobs.size () * (jobs.size () + 1) / 7);
    final Set<SimEntityEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final Random rngScheduleTimeJitter = new Random ();
    final Random rngCredits = new Random ();
    for (int i = 1; i <= numberOfSacToSchedule; i++)
    {
      // Create a jitter on the schedule time in U[-0.001, +0.001].
      final double scheduleTimeJitter = 0.001 * (2.0 * rngScheduleTimeJitter.nextDouble () - 1.0);
      final double scheduleTime = 7.0 * i - 0.25 + scheduleTimeJitter;
      final int credits = rngCredits.nextInt (3); // 0, 1, or 2.
      final SimEntityEvent<J, Q> sacSchedule = new SimQueueServerAccessCreditsEvent<> (queue, scheduleTime, credits);
      if (! realQueueExternalEvents.containsKey (scheduleTime))
        realQueueExternalEvents.put (scheduleTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (scheduleTime).add (sacSchedule);
      eventsToSchedule.add (sacSchedule);
    }
    // Be careful not to reset the event list (again) here!
    SimEntityEventScheduler.schedule (eventList, false, Double.NaN, eventsToSchedule);
    return jobs;
  }
  
}
