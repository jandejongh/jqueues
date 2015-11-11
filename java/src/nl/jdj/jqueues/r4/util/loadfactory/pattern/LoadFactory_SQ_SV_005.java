package nl.jdj.jqueues.r4.util.loadfactory.pattern;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimJobFactory;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.extensions.gate.SimQueueWithGate;
import nl.jdj.jqueues.r4.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r4.extensions.gate.SimQueueGateEvent;
import nl.jdj.jsimulation.r4.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 005.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class LoadFactory_SQ_SV_005<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_001<J, Q>
{

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_001#generate};
   * <li> <i>only if</i> the queue is a {@link SimQueueWithGate},
   *         it adds setting the number of gate-passage credits on the queue at 11.19, 22.19, 33.19, etc.
   * </ul>
   * 
   * <p>
   * Note: this method generates {@link SimQueueGateEvent}s <i>only</i> for {@link SimQueueWithGate}s.
   * The check is done at runtime, and not reflected in the generic-type arguments of this class.
   * If the queue is not a {@link SimQueueWithGate}, instances of this class behave as if they
   * were a {@link LoadFactory_SQ_SV_001}.
   * 
   * <p>
   * The amount of gate-passage credits is drawn from {0, 1, 5, {@link Double#MAX_VALUE}} with equal probabilities.
   * Setting the gate-passage credits is scheduled roughly until all jobs would have been served under single-server FCFS,
   * with an additional 100% to account for the gate delays.
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
    if (queue instanceof SimQueueWithGate)
    {
      final NavigableMap<Double, Set<SimEntityEvent<J, Q>>> realQueueExternalEvents =
        ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
      final int numberOfGateEventsToSchedule = Math.max (1, jobs.size () * (jobs.size () + 1) / 11);
      final Set<SimEntityEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
      final Random rngPassageCredits = new Random ();
      for (int i = 1; i <= numberOfGateEventsToSchedule; i++)
      {
        // XXX We probably need jitter on the schedule time.
        final double scheduleTime = 11.0 * i + 0.19;
        final int draw = rngPassageCredits.nextInt (4);
        final int gatePassageCredits;
        if (draw == 0)
          gatePassageCredits = 0;
        else if (draw == 1)
          gatePassageCredits = 1;
        else if (draw == 2)
          gatePassageCredits = 5;
        else if (draw == 3)
          gatePassageCredits = Integer.MAX_VALUE;
        else
          throw new RuntimeException ();
        final SimEntityEvent<J, Q> gateSchedule = new SimQueueGateEvent<> (queue, scheduleTime, gatePassageCredits);
        if (! realQueueExternalEvents.containsKey (scheduleTime))
          realQueueExternalEvents.put (scheduleTime, new LinkedHashSet<> ());
        realQueueExternalEvents.get (scheduleTime).add (gateSchedule);
        eventsToSchedule.add (gateSchedule);
      }
      // Be careful not to reset the event list (again) here!
      SimEntityEventScheduler.schedule (eventList, false, Double.NaN, eventsToSchedule);
    }
    return jobs;
  }
  
}
