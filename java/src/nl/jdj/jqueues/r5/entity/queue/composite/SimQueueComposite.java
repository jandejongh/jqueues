package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.parallel.BlackParallelSimQueues;
import nl.jdj.jqueues.r5.entity.queue.composite.tandem.BlackTandemSimQueue;

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
 * Composite queues come in different colors ({@link #getColor}), determining the level of transparency of the queue:
 * <ul>
 * <li>A <i>black</i> composite queue completely hides its internal structure, and uses <i>delegate</i> jobs
 *     to represent visiting jobs; see {@link BlackSimQueueComposite}.
 * <li>A <i>gray</i> composite queue lets jobs visit the sub-queues directly, but adds zero-time visits to itself
 *     at the start and the end of a job visit; see {@link GraySimQueueComposite}.
 * <li>A <i>white</i> composite queue immediately drops and redirects a visiting job to the first sub-queue to visit;
 *     jobs never actually visit a white composite queue; {@link WhiteSimQueueComposite}.
 * </ul>
 * 
 * <p>
 * The so-called <i>managed jobs</i> are those {@link SimJob}s for which a {@link SimQueueComposite} is currently performing
 * (at least) routing through the sub-queues.
 * For <i>black</i> composite queues, the set of managed jobs is always equal to the set of jobs present on the composite queue,
 * whereas for <i>gray</i> and <i>white</i> composite it is a super-set of the jobs present on the composite queue.
 * On <i>white</i> composite queues, the set of jobs present is always empty.
 * Jobs that arrive through {@link SimQueue#arrive} are dropped on the white composite queue, and forwarded to the
 * ("first") sub-queue.
 * 
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see Color
 * @see BlackSimQueueComposite
 * @see GraySimQueueComposite
 * @see WhiteSimQueueComposite
 * 
 */
public interface SimQueueComposite<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
extends SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // COLOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The color of a composite queue.
   * 
   * @see SimQueueComposite
   * 
   */
  public enum Color
  {
    /** A composite queue hiding its internal structure and owning its sub-queues.
     * 
     * <p>
     * A {@link Color#BLACK} queue uses <i>delegate</i> jobs.
     * 
     * @see BlackSimQueueComposite
     * 
     */
    BLACK,
    /** A composite queue routing visiting jobs through its sub-queues with mandatory visits to the composite queue upon
     *  arrival and departure.
     * 
     * <p>
     * A {@link Color#GRAY} queue does <i>not</i> use <i>delegate</i> jobs, and does <i>not</i> own its sub-queues.
     * 
     * @see GraySimQueueComposite
     * 
     */
    GRAY,
    /** A composite queue routing visiting jobs through its sub-queues without visits to the composite queue.
     * 
     * <p>
     * A {@link Color#WHITE} queue does <i>not</i> use <i>delegate</i> jobs, and does <i>not</i> own its sub-queues.
     * 
     * @see WhiteSimQueueComposite
     * 
     */
    WHITE
  }
  
  /** Gets the {@link Color} of this queue.
   * 
   * @return The {@link Color} of this queue.
   * 
   * @see SimQueueComposite
   * @see Color
   * 
   */
  Color getColor ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (SUB-)QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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
  Set<? extends DQ> getQueues ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MANAGED JOBS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the set of jobs currently managed by this queue.
   *
   * @return The set of jobs currently managed by the queue, non-{@code null}.
   * 
   * @see #getNumberOfManagedJobs
   * 
   */
  Set<J> getManagedJobs ();

  /** Gets the number of jobs currently managed by this queue.
   *
   * <p>
   * Typically, this method is more efficient than {@code getManagedJobs ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs currently managed by the queue, zero or positive.
   * 
   * @see #getManagedJobs
   * 
   */
  int getNumberOfManagedJobs ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the sub-queue selector for this {@link SimQueueComposite}.
   * 
   * @return The sub-queue selector for this composite queue.
   * 
   */
  SimQueueSelector<J, DQ> getSimQueueSelector ();
  
}
