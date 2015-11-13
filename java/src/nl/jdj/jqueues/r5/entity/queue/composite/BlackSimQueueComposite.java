package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A network of {@link SimQueue}s embedded in a single queue hiding its internal structural details.
 *
 * <p>
 * In order to hide the internal structure of the network, visiting {@link SimJob}s
 * to the {@link BlackSimQueueComposite} are represented (1:1) by so-called
 * <i>delegate jobs</i>. The "real" and delegate jobs may be of different type.
 * 
 * <p>
 * Black composite queues are typically used to create {@link SimQueue} implementations
 * of the combined sub-queues.
 * To visiting jobs, and to listeners as well,
 * the fact that the queue is actually composed of sub-queues is completely hidden.
 * Also, "alien" visits to the embedded queues are not allowed,
 * at the expense of throwing an exception.
 * Think of a black composite queue as if it fully owns and controls its sub-queues
 * (unlike gray and white composite queues).
 * 
 * <p>
 * A base implementation of a {@link BlackSimQueueComposite} can be found in
 * {@link AbstractBlackSimQueueComposite}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public interface BlackSimQueueComposite<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
extends SimQueueComposite<DJ, DQ, J, Q>
{
  
}
