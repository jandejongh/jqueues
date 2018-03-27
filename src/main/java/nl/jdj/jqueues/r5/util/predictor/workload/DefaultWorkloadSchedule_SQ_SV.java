package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;

/** A default implementation of {@link WorkloadSchedule_SQ_SV}.
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
public class DefaultWorkloadSchedule_SQ_SV
extends DefaultWorkloadSchedule
implements WorkloadSchedule_SQ_SV
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  Set<SimQueue>
  getQueueSet
  (final SimQueue queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<SimQueue> allQueues = new HashSet<> ();
    allQueues.add (queue);
    return allQueues;
  }
    
  /** Creates a new {@link DefaultWorkloadSchedule_SQ_SV}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimJQEvent}s for a single specified queue, single visits to that queue for each job, and an unambiguous schedule
   *  represented as a sequence of events.
   * 
   * @param <E>         The event type.
   * @param queue       The queue to consider, non-{@code null}; events related to other queues are ignored.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws IllegalArgumentException           If the queue is {@code null}.
   * @throws WorkloadScheduleInvalidException   If the workload represented by the {@code queueEvents} argument is invalid
   *                                            (e.g., containing a job with multiple visits to the {@code queue}.
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule_SQ_SV
  (final SimQueue queue,
   final Set<E> queueEvents)
  throws WorkloadScheduleException
  {
    super (getQueueSet (queue), queueEvents);
    if (! (isSingleQueue () && isSingleVisit ()))
      throw new WorkloadScheduleInvalidException ();
  }
  
  /** Creates a new {@link DefaultWorkloadSchedule_SQ_SV}, filling out all the internal sets and maps from scanning a map of 
   *  event time onto sets of {@link SimJQEvent}s
   *  for a single specified queue, single visits to that queue for each job, and an unambiguous schedule
   *  represented as a sequence of events.
   * 
   * @param <E>         The event type.
   * @param queue       The queue to consider, non-{@code null}; events related to other queues are ignored.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws IllegalArgumentException           If the queue is {@code null}.
   * @throws WorkloadScheduleInvalidException   If the workload represented by the {@code queueEvents} argument is invalid
   *                                            (e.g., containing a job with multiple visits to the {@code queue}.
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule_SQ_SV
  (final SimQueue queue,
   final Map<Double, Set<E>> queueEvents)
  throws WorkloadScheduleException
  {
    super (getQueueSet (queue), queueEvents);
    if (! (isSingleQueue () && isSingleVisit ()))
      throw new WorkloadScheduleInvalidException ();
  }
  
}
