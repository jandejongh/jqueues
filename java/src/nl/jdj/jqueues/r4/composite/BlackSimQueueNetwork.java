package nl.jdj.jqueues.r4.composite;

import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** A network of {@link SimQueue}s embedded in a single queue hiding its internal structural details.
 *
 * Also known as a <i>queueing network</i>. Examples are <i>tandem queues</i>
 * and <i>parallel queues</i>, see {@link BlackTandemSimQueue} and {@link BlackParallelSimQueues},
 * respectively. The internal structure of the {@link BlackSimQueueNetwork} is hidden to
 * visiting jobs, hence the name "black".
 * 
 * <p>
 * In order to hide the internal structure of the network, visiting {@link SimJob}s
 * to the {@link BlackSimQueueNetwork} are represented (1:1) by so-called
 * <i>delegate jobs</i>. The "real" and delegate jobs may be of different type.
 * 
 * <p>
 * A base implementation of a {@link BlackSimQueueNetwork} can be found in
 * {@link AbstractBlackSimQueueNetwork}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public interface BlackSimQueueNetwork<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
extends SimQueue<J, Q>
{
  
  /** Returns the set of queues embedded by this {@link BlackSimQueueNetwork}.
   * 
   * The set should not be manipulated.
   * 
   * @return The non-<code>null</code> set of queues, each non-<code>null</code>.
   * 
   */
  public Set<? extends DQ> getQueues ();

}
