package nl.jdj.jqueues.r5.entity.jq.queue.composite.enc;

import nl.jdj.jqueues.r5.entity.SimEntityOperation;
import nl.jdj.jqueues.r5.entity.jq.SimJQOperation;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite.RealDelegateJobMapper;

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
 * @see Enc
 * @see EncHS
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class DelegatedSimQueueOperation
implements SimEntityOperation<SimEntityOperation, SimEntityOperation.Request, SimEntityOperation.Reply>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a delegated {@link SimEntityOperation}.
   * 
   * @param queue     The queue for which to delegate the given operation.
   * @param subQueue  The delegate sub-queue (that will eventually execute the operation).
   * @param oDQueue   The delegate operation on the sub-queue.
   * @param jobMapper An optional mapper between real and delegate jobs.
   * 
   */
  public DelegatedSimQueueOperation
  (final SimQueue queue,
   final SimQueue subQueue,
   final SimEntityOperation oDQueue,
   final AbstractSimQueueComposite.RealDelegateJobMapper jobMapper)
  {
    if (queue == null || subQueue == null || oDQueue == null)
      throw new IllegalArgumentException ();
    this.queue = queue;
    this.subQueue = subQueue;
    this.oDQueue = oDQueue;
    this.jobMapper = jobMapper;
  }

  private final AbstractSimQueueComposite.RealDelegateJobMapper jobMapper;
  
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
   * A copy of the operation request is requested with sub-queue and delegate job as request parameters.
   * This, however, is not fully supported for all request types.
   * 
   * @throws IllegalArgumentException      If the request is {@code null} or requires a job mapper that is absent.
   * @throws UnsupportedOperationException If the request's parameters could not be changed into those required for the sub-queue.
   * 
   * @see RealDelegateJobMapper
   * 
   */
  @Override
  public SimEntityOperation.Reply doOperation
  (final double time, final SimEntityOperation.Request request)
  {
    if (request == null)
      throw new IllegalArgumentException ();
    // XXX Could use additional error checking on the request!
    if (this.oDQueue == null)
      throw new IllegalStateException ();
    if (! SimJQOperation.RequestJAndOrQ.class.isAssignableFrom (this.oDQueue.getOperationRequestClass ()))
      throw new UnsupportedOperationException ();
    if (! (request instanceof SimJQOperation.RequestJAndOrQ))
      throw new UnsupportedOperationException ();
    final SimJQOperation.RequestJAndOrQ rRequest = (SimJQOperation.RequestJAndOrQ) request;
    final SimJQOperation.RequestJAndOrQ dRequest;
    if (rRequest.getJob () != null && this.jobMapper == null)
      throw new IllegalArgumentException ();
    final SimJob dJob;
    if (rRequest.getJob () != null)
      dJob = this.jobMapper.getDelegateJob (rRequest.getJob ());
    else
      dJob = null;
    if (rRequest instanceof SimJQOperation.RequestJ)
      dRequest = rRequest.forJob (dJob);
    else if (rRequest instanceof SimJQOperation.RequestQ)
      dRequest = rRequest.forQueue (this.subQueue);
    else if (rRequest instanceof SimJQOperation.RequestJQ)
      dRequest = rRequest.forJobAndQueue (dJob, this.subQueue);
    else
      throw new UnsupportedOperationException ();
    final Reply dReply = this.subQueue.doOperation (time, dRequest);
    // XXX Should also modify dReply!!
    final Reply rReply = dReply;
    return rReply;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
