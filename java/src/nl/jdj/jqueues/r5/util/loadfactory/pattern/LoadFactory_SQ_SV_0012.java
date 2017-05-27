package nl.jdj.jqueues.r5.util.loadfactory.pattern;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0012.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class LoadFactory_SQ_SV_0012<J extends SimJob, Q extends SimQueue>
extends LoadFactory_SQ_SV_0010<J, Q>
{

  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the job load according to {@link LoadFactory_SQ_SV_0010#generate};
   * <li> adds a revocation for every 5th job.
   * </ul>
   * 
   * <p>
   * The {@code interruptService} flag is chosen at random from a uniform distribution.
   * If {@code true}, the revocation is scheduled uniformly distributed in the <i>service</i> interval under
   * single-server FCFS, otherwise in the <i>wait</i> interval.
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
    final NavigableMap<Double, Set<SimJQEvent>> queueExternalEvents)
  {
    final Set<J> jobs = super.generate (eventList, attachSimJobsToEventList,
      queue, jobFactory, numberOfJobs, reset, resetTime, hints, queueExternalEvents);
    final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final Iterator<J> i_jobs = jobs.iterator ();
    final Random rngInterrupt = new Random ();
    final Random rngDelay = new Random ();
    for (int i = 1; i <= jobs.size (); i++)
    {
      final J job = i_jobs.next ();
      if (i % 5 == 0)
      {
        final double arrivalTime = (double) i;
        final double serviceTime = (double) i;
        final double expWorkArrived = 0.5 * i * (i + 1);
        final double expWorkDone = (double) (i - 1);
        final double expWait = expWorkArrived - expWorkDone - serviceTime;
        final boolean interruptService = rngInterrupt.nextBoolean ();
        final double delay = (interruptService
          ? (expWait + serviceTime * rngDelay.nextDouble ())
          : expWait * rngDelay.nextDouble ());
        final double revocationTime = arrivalTime + delay;
        final SimJQEvent<J, Q> revocationSchedule
          = new SimJQEvent.Revocation<> (job, queue, revocationTime, interruptService);
      if (! realQueueExternalEvents.containsKey (revocationTime))
        realQueueExternalEvents.put (revocationTime, new LinkedHashSet<> ());
      realQueueExternalEvents.get (revocationTime).add (revocationSchedule);
      eventsToSchedule.add (revocationSchedule);
      }
    }
    // Be careful not to reset the event list (again) here!
    SimEntityEventScheduler.schedule (eventList, false, Double.NaN, eventsToSchedule);
    return jobs;
  }
  
}
