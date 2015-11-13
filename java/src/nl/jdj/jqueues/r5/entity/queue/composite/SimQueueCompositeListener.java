package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.*;

/** A listener to state changes of one or multiple {@link SimQueueComposite}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueCompositeListener<J extends SimJob, Q extends SimQueue>
extends SimQueueListener<J, Q>
{
 
}
