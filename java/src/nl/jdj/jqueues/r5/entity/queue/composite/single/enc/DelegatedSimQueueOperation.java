package nl.jdj.jqueues.r5.entity.queue.composite.single.enc;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityOperation;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;

/** A {@link SimEntityOperation} for composite queues that delegates the execution
 *  of a specific operation on a (specific) composite queue to another operation on a (specific) sub-queue.
 * 
 * <p>
 * In theory, the use of this class is not restricted to composite queues.
 * For instance, the queue and sub-queue could be the same object.
 * This use, however, has not been tested.
 * 
 * @see SimQueue#doOperation
 * @see AbstractSimQueue#registerDelegatedOperation
 * @see BlackEncapsulatorSimQueue
 * @see BlackEncapsulatorHideStartSimQueue
 * 
 */
public class DelegatedSimQueueOperation
implements SimEntityOperation<SimJob, SimQueue, SimEntityOperation, SimEntityOperation.Request, SimEntityOperation.Reply>
{

  /** The queue at which the operation requests arrive, non-{@code null}.
   * 
   */
  private final SimQueue queue;
  
  /** The queue to which the operation requests are forwarded, non-{@code null}.
   * 
   * <p>
   * Likely to be a sub-queue of the queue field in a composite-queue (hence its name),
   * but not required.
   * 
   */
  private final SimQueue subQueue;
  
  /** The operation on the sub-queue to be performed through delegation.
   * 
   */
  private final SimEntityOperation oDQueue;
  
  /** Creates a delegated {@link SimEntityOperation}.
   * 
   * @param queue    The queue for which to delegate the given operation.
   * @param subQueue The delegate sub-queue (that will eventually execute the operation).
   * @param oDQueue  The delegate operation on the sub-queue.
   * 
   */
  public DelegatedSimQueueOperation (final SimQueue queue, final SimQueue subQueue, final SimEntityOperation oDQueue)
  {
    if (queue == null || subQueue == null || oDQueue == null)
      throw new IllegalArgumentException ();
    this.queue = queue;
    this.subQueue = subQueue;
    this.oDQueue = oDQueue;
  }
  
  /** Returns "DelegatedOperation[operation on sub-queue]".
   *
   * @return "DelegatedOperation[operation on sub-queue]".
   *
   */
  @Override
  public final String getName ()
  {
    return "DelegatedOperation[" + this.oDQueue.getName () + "]";
  }

  /** Returns the class of the request for the operation on the sub-queue.
   *
   * @return The class of the request for the operation on the sub-queue.
   *
   */
  @Override
  public final Class getOperationRequestClass ()
  {
    return this.oDQueue.getOperationRequestClass ();
  }

  /** Returns the class of the reply for the operation on the sub-queue.
   *
   * @return The class of the reply for the operation on the sub-queue.
   *
   */
  @Override
  public final Class getOperationReplyClass ()
  {
    return this.oDQueue.getOperationReplyClass ();
  }

  /** Performs the operation on the sub-queue.
   * 
   * <p>
   * XXX Currently, the request is sent unmodified to the sub-queue.
   * For operations with a {@link SimJob} parameter, this approach needs revision in future releases.
   * 
   */
  @Override
  public SimEntityOperation.Reply doOperation
  (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final SimEntityOperation.Request request)
  {
    // XXX Modify Request when a job is involved??
    return this.subQueue.doOperation (time, request);
  }

}
