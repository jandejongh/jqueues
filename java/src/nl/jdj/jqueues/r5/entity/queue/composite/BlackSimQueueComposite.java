package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.single.encap.BlackEncapsulatorSimQueue;

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
 * <p>
 * For details about the semantics of the waiting and service areas of a black composite queue,
 * see {@link StartModel}.
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START MODEL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Supported models for the semantics of the start of (real) jobs, of server-access credits, and of waiting and service areas.
   * 
   */
  public enum StartModel
  {
    /** A local (FIFO) waiting area controlled by local server-access credits granting delegate jobs their initial arrival
     *  at the sub-queues system.
     * 
     * <p>
     * This model is the default,
     * and can be thought of as a (FIFO) waiting area for real jobs before access (by their delegate jobs)
     * to the sub-queues. After a (real) job has been granted access this way, it is no longer affected by
     * the server-access credits on this (super) queue.
     * The super queue never sets the server-access credits on its sub-queues, and,
     * as a result, the server-access credits on all sub-queues is always infinite.
     * The {@link #isNoWaitArmed} state is always {@code true} on this queue, because (real) jobs start immediately
     * in absence of queue-access vacations and out-of-service-access-credits conditions.
     * 
     *//** A local (FIFO) waiting area controlled by local server-access credits granting delegate jobs their initial arrival
     *  at the sub-queues system.
     * 
     * <p>
     * This model is the default,
     * and can be thought of as a (FIFO) waiting area for real jobs before access (by their delegate jobs)
     * to the sub-queues. After a (real) job has been granted access this way, it is no longer affected by
     * the server-access credits on this (super) queue.
     * The super queue never sets the server-access credits on its sub-queues, and,
     * as a result, the server-access credits on all sub-queues is always infinite.
     * The {@link #isNoWaitArmed} state is always {@code true} on this queue, because (real) jobs start immediately
     * in absence of queue-access vacations and out-of-service-access-credits conditions.
     * 
     */
    LOCAL,
    /** The waiting and service area of real jobs, and the semantics of server-access credits and starting a real job,
     *  coincide with the single sub queue.
     * 
     * <p>
     * This model can only be applied in case of a single sub-queue.
     * Whenever a real job is in the waiting area of the (super) queue, so is its delegate job in the sub-queue.
     * The same holds for the service area.
     * The number of server access credits on this queue and on the sub-queue are always equal,
     * and so are the {@link #isNoWaitArmed} states.
     * 
     * @see BlackEncapsulatorSimQueue
     * 
     */
    ENCAPSULATOR_QUEUE,
    /** The waiting area of the first queue is that of the composite queue; the service area of the second queue is that of the
     *  composite queue in a system with exactly two sub-queues.
     * 
     * <p>
     * This model can only be applied in case of exactly two sub-queues.
     * Whenever a real job is in the waiting area of the (super) queue,
     * its delegate job is in the waiting area of the <i>first</i> sub-queue.
     * Whenever a real job is in the service area of the (super) queue,
     * its delegate job is in the service area of the <i>first</i> sub-queue.
     * The number of service-access credits on the super (this) queue is used to
     * independently limit the remaining number of jobs to start (as in {@link StartModel#LOCAL}.
     * The number of server access credits on the first sub-queue is always zero or one,
     * and it is only one when a single job is to start.
     * The number of server access credits on the second sub-queue is always infinite (i.e., unused).
     * 
     * @see BlackCompressedTandem2SimQueue
     * 
     */
    COMPRESSED_TANDEM_2_QUEUE
  }
  
  /** Gets the start model of this composite queue.
   * 
   * <p>
   * The start model is set upon construction and cannot (should not) be changed afterwards.
   * 
   * @return The start model of this composite queue, non-{@code null}.
   * 
   * @see StartModel
   * 
   */
  StartModel getStartModel ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELEGATE SIMJOB FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   * @return The factory to create delegate {@link SimJob}s, non-<code>null</code>.
   */
  DelegateSimJobFactory<? extends DJ, DQ, J, Q> getDelegateSimJobFactory ();
  
  /** Sets the factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   * @param delegateSimJobFactory The new factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   * @throws IllegalArgumentException      If the argument is {@code null} or otherwise illegal.
   * @throws UnsupportedOperationException If this composite queue does not allow setting the delegate-job factory.
   * 
   */
  void setDelegateSimJobFactory (DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory);
  
}
