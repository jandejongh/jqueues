package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimQueue} that embeds a fixed set of other queues;
 *  turning its job visits into visits to the embedded queues.
 * 
 * <p>
 * Also known as a <i>queueing network</i>.
 * Examples are <i>tandem queues</i>
 * and <i>parallel queues</i>, see {@link BlackTandemSimQueue} and {@link BlackParallelSimQueues},
 * respectively.
 * 
 * <p>
 * Composite queues come in different colors, determining the level of transparency of the queue:
 * <ul>
 * <li>A <i>black</i> composite queue completely hides its internal structure, and uses <i>delegate</i> jobs
 *     to represent visiting jobs; see {@link BlackSimQueueComposite}.
 * <li>A <i>gray</i> composite queue lets jobs visit the sub-queues directly, but adds zero-time visits to itself
 *     at the start and the end of a job visit; see {@link GraySimQueueComposite}.
 * <li>A <i>white</i> composite queue immediately redirects a visiting job to the first sub-queue to visit;
 *     jobs never actually visit a white composite queue; {@link WhiteSimQueueComposite}.
 * </ul>
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public interface SimQueueComposite<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
extends SimQueue<J, Q>
{
  
  /** Returns the set of queues embedded by this {@link SimQueueComposite}.
   * 
   * <p>
   * The set should not be manipulated.
   * 
   * <p>
   * Implementations must maintain deterministic ordering of the queues in the set!
   * 
   * @return The non-<code>null</code> set of queues, each non-<code>null</code>.
   * 
   */
  public Set<? extends DQ> getQueues ();

  /** Returns the sub-queue selector for this {@link SimQueueComposite}.
   * 
   * @return The sub-queue selector for this composite queue.
   * 
   */
  public SimQueueSelector<J, DQ> getSimQueueSelector ();
  
}
