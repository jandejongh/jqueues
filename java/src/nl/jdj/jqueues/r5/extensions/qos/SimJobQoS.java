package nl.jdj.jqueues.r5.extensions.qos;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimJob} with explicit QoS support.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public interface SimJobQoS<J extends SimJobQoS, Q extends SimQueue, P extends Comparable>
extends SimEntityQoS<J, Q, P>, SimJob<J, Q>
{
  
}
