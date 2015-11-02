package nl.jdj.jqueues.r4;

/** A {@link SimJobListener} logging events on <code>System.out</code>.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class StdOutSimJobListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimEntityListener<J, Q>
implements SimJobListener<J, Q>
{

}
