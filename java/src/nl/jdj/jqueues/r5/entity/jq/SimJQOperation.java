package nl.jdj.jqueues.r5.entity.jq;

import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityOperation;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** The definition of an operation on a {@link SimQueue} and/or a {SimJob}.
 * 
 * @param <J>   The type of {@link SimJob}s supported.
 * @param <Q>   The type of {@link SimQueue}s supported.
 * @param <O>   The operation type.
 * @param <Req> The request type (corresponding to the operation type).
 * @param <Rep> The reply type (corresponding to the operation type).
 * 
 * @see SimEntity#getRegisteredOperations
 * @see SimEntity#doOperation
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
public interface SimJQOperation
  <J extends SimJob,
   Q extends SimQueue,
   O extends SimEntityOperation,
   Req extends SimEntityOperation.Request,
   Rep extends SimEntityOperation.Reply>
extends SimEntityOperation<O, Req, Rep>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires a job argument.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   */
  abstract static class RequestJ<O extends SimEntityOperation, Req extends SimEntityOperation.Request>
  implements SimEntityOperation.Request<O, Req>
  {
    
    /** Creates the request.
     * 
     * @param job The job, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job is {@code null}.
     * 
     */
    public RequestJ (final SimJob job)
    {
      if (job == null)
        throw new IllegalArgumentException ();
      this.job = job;
    }
    
    private final SimJob job;
    
    /** Returns the job argument of this request.
     * 
     * @return The job argument of this request, non-{@code null}.
     * 
     */
    public final SimJob getJob ()
    {
      return this.job;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires a queue argument.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   */
  abstract static class RequestQ<O extends SimEntityOperation, Req extends SimEntityOperation.Request>
  implements SimEntityOperation.Request<O, Req>
  {
    
    /** Creates the request.
     * 
     * @param queue The queue, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the queue is {@code null}.
     * 
     */
    public RequestQ (final SimQueue queue)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      this.queue = queue;
    }
    
    private final SimQueue queue;
    
    /** Returns the queue argument of this request.
     * 
     * @return The queue argument of this request, non-{@code null}.
     * 
     */
    public final SimQueue getQueue ()
    {
      return this.queue;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH JOB AND QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires a job and a queue argument.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   */
  abstract static class RequestJQ<O extends SimEntityOperation, Req extends SimEntityOperation.Request>
  implements SimEntityOperation.Request<O, Req>
  {
    
    /** Creates the request.
     * 
     * @param job   The job, non-{@code null}.
     * @param queue The queue, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job or queue is {@code null}.
     * 
     */
    public RequestJQ (final SimJob job, final SimQueue queue)
    {
      if (job == null || queue == null)
        throw new IllegalArgumentException ();
      this.job = job;
      this.queue = queue;
    }
    
    private final SimJob job;
    
    /** Returns the job argument of this request.
     * 
     * @return The job argument of this request, non-{@code null}.
     * 
     */
    public final SimJob getJob ()
    {
      return this.job;
    }
    
    private final SimQueue queue;
    
    /** Returns the queue argument of this request.
     * 
     * @return The queue argument of this request, non-{@code null}.
     * 
     */
    public final SimQueue getQueue ()
    {
      return this.queue;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The arrival operation on a {@link SimQueue}.
   * 
   * @see SimQueue#arrive
   * 
   */
  public final static class Arrival
  implements SimEntityOperation<Arrival, ArrivalRequest, ArrivalReply>
  {

    /** Prevents instantiation.
     * 
     */
    private Arrival ()
    {
    }
    
    /** Prevents cloning.
     * 
     * @return Nothing; throws exception.
     * 
     * @throws CloneNotSupportedException Always.
     * 
     */
    @Override
    public final Object clone () throws CloneNotSupportedException
    {
      // Not necessary, but keeps IDE from complaining.
      super.clone ();
      // Actually, super call will throw this exception already...
      throw new CloneNotSupportedException ();
    }
    
    private final static Arrival INSTANCE = new Arrival ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static Arrival getInstance ()
    {
      return Arrival.INSTANCE;
    }
    
    /** Returns "Arrival".
     * 
     * @return "Arrival".
     * 
     */
    @Override
    public final String getName ()
    {
      return "Arrival";
    }

    /** Returns the class of {@link ArrivalRequest}.
     * 
     * @return The class of {@link ArrivalRequest}.
     * 
     */
    @Override
    public final Class<ArrivalRequest> getOperationRequestClass ()
    {
      return ArrivalRequest.class;
    }

    /** Returns the class of {@link ArrivalReply}.
     * 
     * @return The class of {@link ArrivalReply}.
     * 
     */
    @Override
    public final Class<ArrivalReply> getOperationReplyClass ()
    {
      return ArrivalReply.class;
    }

    /** Invokes {@link SimQueue#arrive}.
     * 
     * @see SimQueue#arrive
     * 
     */
    @Override
    public final ArrivalReply doOperation
    (final double time, final ArrivalRequest request)
    {
      if (request == null
        || request.getJob () == null
        || request.getQueue () == null
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      request.getQueue ().arrive (time, request.getJob ());
      return new ArrivalReply (request);
    }
    
  }
  
  /** A request for the arrival operation {@link Arrival}.
   * 
   */
  public final static class ArrivalRequest
  extends RequestJQ<Arrival, ArrivalRequest>
  implements SimEntityOperation.Request<Arrival, ArrivalRequest>
  {
    
    /** Creates the request.
     * 
     * @param job   The job, non-{@code null}.
     * @param queue The queue, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job or queue is {@code null}.
     * 
     */
    public ArrivalRequest (final SimJob job, final SimQueue queue)
    {
      super (job, queue);
    }
    
    /** Returns the singleton instance of {@link Arrival}.
     * 
     * @return The singleton instance of {@link Arrival}.
     * 
     */
    @Override
    public final Arrival getOperation ()
    {
      return Arrival.INSTANCE;
    }
    
  }
  
  /** A reply for the arrival operation {@link Arrival}.
   * 
   */
  public final static class ArrivalReply
  implements SimEntityOperation.Reply<Arrival, ArrivalRequest, ArrivalReply>
  {

    private final ArrivalRequest request;
    
    /** Creates the reply.
     * 
     * @param request The applicable {@link ArrivalRequest}.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public ArrivalReply (final ArrivalRequest request)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
    }
    
    /** Returns the singleton instance of {@link Arrival}.
     * 
     * @return The singleton instance of {@link Arrival}.
     * 
     */
    @Override
    public final Arrival getOperation ()
    {
      return Arrival.INSTANCE;
    }

    @Override
    public final ArrivalRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The revocation operation on a {@link SimQueue}.
   * 
   * @see SimQueue#revoke
   * 
   */
  public final static class Revocation
  implements SimEntityOperation<Revocation, RevocationRequest, RevocationReply>
  {

    /** Prevents instantiation.
     * 
     */
    private Revocation ()
    {
    }
    
    /** Prevents cloning.
     * 
     * @return Nothing; throws exception.
     * 
     * @throws CloneNotSupportedException Always.
     * 
     */
    @Override
    public final Object clone () throws CloneNotSupportedException
    {
      // Not necessary, but keeps IDE from complaining.
      super.clone ();
      // Actually, super call will throw this exception already...
      throw new CloneNotSupportedException ();
    }
    
    private final static Revocation INSTANCE = new Revocation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static Revocation getInstance ()
    {
      return Revocation.INSTANCE;
    }
    
    /** Returns "Revocation".
     * 
     * @return "Revocation".
     * 
     */
    @Override
    public final String getName ()
    {
      return "Revocation";
    }

    /** Returns the class of {@link RevocationRequest}.
     * 
     * @return The class of {@link RevocationRequest}.
     * 
     */
    @Override
    public final Class<RevocationRequest> getOperationRequestClass ()
    {
      return RevocationRequest.class;
    }

    /** Returns the class of {@link RevocationReply}.
     * 
     * @return The class of {@link RevocationReply}.
     * 
     */
    @Override
    public final Class<RevocationReply> getOperationReplyClass ()
    {
      return RevocationReply.class;
    }

    /** Invokes {@link SimQueue#revoke} on the entity.
     * 
     * @see SimQueue#revoke
     * 
     */
    @Override
    public final RevocationReply doOperation
    (final double time, final RevocationRequest request)
    {
      if (request == null
        || request.getJob () == null
        || request.getQueue () == null
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      boolean success = request.getQueue ().revoke (time, request.getJob (), request.isInterruptService ());
      return new RevocationReply (request, success);
    }
    
  }
  
  /** *  A request for the revocation operation {@link Revocation}.
   * 
   */
  public final static class RevocationRequest
  extends RequestJQ<Revocation, RevocationRequest>
  implements SimEntityOperation.Request<Revocation, RevocationRequest>
  {
    
    /** Creates the request.
     * 
     * @param job              The job, non-{@code null}.
     * @param queue            The queue, non-{@code null}.
     * @param interruptService Whether to allow revocation from the job from the service area.
     * 
     * @throws IllegalArgumentException If the job or queue is {@code null}.
     * 
     */
    public RevocationRequest (final SimJob job, final SimQueue queue, final boolean interruptService)
    {
      super (job, queue);
      this.interruptService = interruptService;
    }
    
    private final boolean interruptService;
    
    /** Returns whether to allow revocation from the job from the service area.
     * 
     * @return Whether to allow revocation from the job from the service area.
     * 
     */
    public final boolean isInterruptService ()
    {
      return this.interruptService;
    }
    
    /** *  Returns the singleton instance of {@link Revocation}.
     * 
     * @return The singleton instance of {@link Revocation}.
     * 
     */
    @Override
    public final Revocation getOperation ()
    {
      return Revocation.INSTANCE;
    }
    
  }
  
  /** *  A reply for the revocation operation {@link Revocation}.
   * 
   */
  public final static class RevocationReply
  implements SimEntityOperation.Reply<Revocation, RevocationRequest, RevocationReply>
  {

    /** Creates the reply.
     * 
     * @param request The applicable {@link RevocationRequest}.
     * @param success Whether the operation was successful.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public RevocationReply (final RevocationRequest request, final boolean success)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
      this.success = success;
    }
    
    private final boolean success;
    
    /** Returns whether the operation was successful.
     * 
     * @return Whether the operation was successful.
     * 
     */
    public final boolean isSuccess ()
    {
      return this.success;
    }
    
    /** *  Returns the singleton instance of {@link Revocation}.
     * 
     * @return The singleton instance of {@link Revocation}.
     * 
     */
    @Override
    public final Revocation getOperation ()
    {
      return Revocation.INSTANCE;
    }

    private final RevocationRequest request;
    
    @Override
    public final RevocationRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
}
