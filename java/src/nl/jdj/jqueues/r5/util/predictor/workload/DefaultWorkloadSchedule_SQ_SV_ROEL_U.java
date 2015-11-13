package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.HashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;

/** A default implementation of {@link WorkloadSchedule_SQ_SV_ROEL_U}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultWorkloadSchedule_SQ_SV_ROEL_U<J extends SimJob, Q extends SimQueue>
extends DefaultWorkloadSchedule<J, Q>
implements WorkloadSchedule_SQ_SV_ROEL_U<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueue>
  Set<Q>
  getQueueSet
  (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<Q> allQueues = new HashSet<> ();
    allQueues.add (queue);
    return allQueues;
  }
    
  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimEntityEvent}s for a single specified queue, single visits to that queue for each job, and an unambiguous schedule
   *  on a Random-Order Event List.
   * 
   * @param queue       The queue to consider, non-{@code null}; events related to other queues are ignored.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws IllegalArgumentException           If the queue is {@code null}.
   * @throws WorkloadScheduleAmbiguityException If the workload represented by the {@code queueEvents} argument is
   *                                            ambiguous on a Random-Order Event List.
   * @throws WorkloadScheduleInvalidException   If the workload represented by the {@code queueEvents} argument is invalid
   *                                            (e.g., containing a job with multiple visits to the {@code queue}.
   * 
   */
  public DefaultWorkloadSchedule_SQ_SV_ROEL_U (final Q queue, final Set<SimEntityEvent<J, Q>> queueEvents)
  throws WorkloadScheduleException
  {
    super (getQueueSet (queue), queueEvents);
    if (! (isSingleQueue () && isSingleVisit ()))
      throw new WorkloadScheduleInvalidException ();
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleAmbiguityException ();
  }
  
}
