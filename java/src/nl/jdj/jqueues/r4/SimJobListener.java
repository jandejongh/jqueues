package nl.jdj.jqueues.r4;

/** A listener to state changes of one or multiple {@link SimJob}s.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimJobListener<J extends SimJob, Q extends SimQueue>
extends SimEntityListener<J, Q>
{
 
}
