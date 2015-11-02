package nl.jdj.jqueues.r4;

/** A {@link SimJobListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimJobListener<J extends SimJob, Q extends SimQueue>
extends DefaultSimEntityListener<J, Q>
implements SimJobListener<J, Q>
{

}
