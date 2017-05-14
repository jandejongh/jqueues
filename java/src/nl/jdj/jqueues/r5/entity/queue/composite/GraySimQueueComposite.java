package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A composite queue that routes visiting jobs through its sub-queues.
 *
 * <p>
 * Unlike black composite queues,
 * {@link BlackSimQueueComposite}, a gray queue does not hide its
 * internal structure, nor does it use <i>delegate</i> jobs.
 * 
 * <p>
 * Arriving jobs are immediately sent to the first sub-queue to visit.
 * Subsequently, the gray queue routes the job through the network, and
 * when done, takes the job for a final visit to the gray queue and lets it
 * depart.
 * This means that a gray-composite queue visit typically results in
 * <ul>
 * <li>a zero-time visit to the gray composite queue;
 * <li>zero or more visits to the sub-queues;
 * <li>another zero-time visit to the gray composite queue;
 * <li>a special notification to registered {@link SimQueueCompositeListener}s
 *     to distinguish between the two departure events.
 * </ul>
 *
 * <p>
 * Note that a gray composite queue is "server-less", i.e.,
 * it has no service area.
 * 
 * <p>
 * Another distinction with black composite queues is that
 * gray queues allow other job to pass through its sub-queues;
 * in that sense, a gray composite queue does not "own" its sub-queues.
 * 
 * @param <DQ> The (base) type for sub-queues.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public interface GraySimQueueComposite<DQ extends SimQueue, J extends SimJob, Q extends GraySimQueueComposite>
extends SimQueueComposite<J, DQ, J, Q>
{
  
}
