package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A composite queue routing jobs through its sub-queues, but is never actually visited itself.
 * 
 * <p>
 * A {@link WhiteSimQueueComposite} is very similar to a {@link GraySimQueueComposite} with the following difference:
 * Arriving jobs are redirected immediately to the first sub-queue to visit,
 * <i>without</i> actually visiting the white composite queue, as in gray (and black) composite queues.
 *
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public interface WhiteSimQueueComposite<J extends SimJob, Q extends SimQueue>
extends SimQueueComposite<J, Q, J, Q>
{
  
}
