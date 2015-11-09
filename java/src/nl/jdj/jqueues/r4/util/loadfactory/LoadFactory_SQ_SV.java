package nl.jdj.jqueues.r4.util.loadfactory;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r4.SimEntity;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimJobFactory;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jsimulation.r4.SimEventList;

/** A {@link LoadFactory} that generates a load for a single {@link SimQueue} in which each job visits the queue at most once.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public interface LoadFactory_SQ_SV<J extends SimJob, Q extends SimQueue>
extends LoadFactory<J, Q>
{

  /** Generates job from a factory, schedule arrivals (and/or revocations) for them on an event list, as well as queue vacations
   *  (if applicable), and return the generated load in a user-supplied collection of {@link SimEntityEvent}s.
   * 
   * @param eventList                The event list, non-{@code null}.
   * @param attachSimJobsToEventList Whether or not to attach the generated {@link SimJob}s to the {@link SimEventList};
   *                                 typically set to {@code false}
   *                                 (see {@link SimEntity} and {@link SimJob} for more details on the consequences).
   * @param queue                    The queue, non-{@code null}.
   * @param jobFactory               The job factory, non-{@code null}.
   * @param numberOfJobs             The number of jobs to generate, non-negative.
   * @param reset                    Whether or not to reset the event list.
   * @param resetTime                The time to which to reset the event list.
   * @param queueEvents              An optional map for storing the generated {@link SimEntityEvent}s indexed by
   *                                 event time and for events at the same time, by order of occurrence.
   *                                 The map is <i>not</i> cleared; generated events in this method are assumed to
   *                                 occur after any existing events at the same time.
   * 
   * @return The set of jobs generated.
   * 
   * @throws IllegalArgumentException If any of the arguments supplied has an illegal value.
   * 
   */
  public Set<J> generate
  (SimEventList eventList,
    boolean attachSimJobsToEventList,
    Q queue,
    SimJobFactory<J, Q> jobFactory,
    int numberOfJobs,
    boolean reset,
    double resetTime,
    NavigableMap<Double, Set<SimEntityEvent<J, Q>>> queueEvents);

}
