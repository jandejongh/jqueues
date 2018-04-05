/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.jdj.jqueues.r5.entity.jq;

import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityOperation;
import nl.jdj.jqueues.r5.entity.SimEntityOperation.Request;
import nl.jdj.jqueues.r5.entity.SimEntityOperation.RequestE;
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
  // REQUEST
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request of an operation on a {@link SimQueue} and/or a {SimJob}.
   * 
   * <p>
   * Either the job or the queue (or both) must be non-{@code null},
   * and one of them (non-{@code null}) is always the target entity.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   * @see Request#forTargetEntity
   * 
   */
  abstract static class RequestJAndOrQ<O extends SimEntityOperation, Req extends RequestJAndOrQ>
  extends SimEntityOperation.RequestE<O, Req>
  {

    /** Creates a new request for a job or queue or both, with the option to set the target entity.
     * 
     * <p>
     * Either the job or the queue or both must be non-{@code null}.
     * 
     * @param job         The job.
     * @param queue       The queue.
     * @param jobIsTarget Whether to set the job {@code true}) or the queue ({@code false}) as the target entity.
     * 
     * @throws IllegalArgumentException If both job and queue are {@code null}, or the target entity selected is {@code null}.
     * 
     * @see Request
     * @see RequestE
     * @see Request#getTargetEntity
     * 
     */
    public RequestJAndOrQ (final SimJob job, final  SimQueue queue, final boolean jobIsTarget)
    {
      super (jobIsTarget ? job : queue);
      this.job = job;
      this.queue = queue;
    }
    
    /** Creates a new request for a job or queue or both, preferring a non-{@code null} queue as target entity.
     * 
     * <p>
     * Either the job or the queue or both must be non-{@code null}.
     * 
     * <p>
     * If the queue is non-{@code null}, it is always the target entity.
     * If the job is non-{@code null}, it becaomes target entity if and only if the queue is {@code null}.
     * 
     * @param job   The job.
     * @param queue The queue.
     * 
     * @throws IllegalArgumentException If both job and queue are {@code null}.
     * 
     * @see Request
     * @see RequestE
     * @see Request#getTargetEntity
     * 
     */
    public RequestJAndOrQ (final SimJob job, final  SimQueue queue)
    {
      this (job, queue, queue == null);
    }
    
    private final SimJob job;
    
    /** Returns the job associated with this request.
     * 
     * <p>
     * Note: job and queue cannot both be {@code null}.
     * 
     * @return The job associated with this request, may be {@code null}.
     *
     * @see #getQueue
     * @see #getTargetEntity
     * 
     */
    public final SimJob getJob ()
    {
      return this.job;
    }
    
    private final SimQueue queue;
    
    /** Returns the queue associated with this request.
     * 
     * <p>
     * Note: job and queue cannot both be {@code null}.
     * 
     * @return The queue associated with this request, may be {@code null}.
     *
     * @see #getJob
     * @see #getTargetEntity
     * 
     */
    public final SimQueue getQueue ()
    {
      return this.queue;
    }
    
    /** Returns a copy of this request for another job.
     * 
     * @param job The new job, non-{@code null}.
     * 
     * @return A copy of this request for given job.
     * 
     * @throws IllegalArgumentException      If the job is {@code null}.
     * @throws UnsupportedOperationException If this request does not support jobs.
     * 
     */
    public abstract Req forJob (SimJob job);
    
    /** Returns a copy of this request for another queue.
     * 
     * @param queue The new queue, non-{@code null}.
     * 
     * @return A copy of this request for given queue.
     * 
     * @throws IllegalArgumentException      If the queue is {@code null}.
     * @throws UnsupportedOperationException If this request does not support queues.
     * 
     */
    public abstract Req forQueue (SimQueue queue);
    
    /** Returns a copy of this request for another job and another queue.
     * 
     * @param job   The new job, non-{@code null}.
     * @param queue The new queue, non-{@code null}.
     * 
     * @return A copy of this request for given job and queue.
     * 
     * @throws IllegalArgumentException      If the job and/or queue  is {@code null}.
     * @throws UnsupportedOperationException If this request does not support jobs or does not support queues.
     * 
     */
    public abstract Req forJobAndQueue (SimJob job, SimQueue queue);

    /** Creates a new request in which given new entity replaces the current job or queue.
     * 
     * <p>
     * In the current object, either the job or the queue is the target entity; the argument given must respect this.
     * In other words, this implementation does not allow a change from job to queue as target entity, or vice versa.
     * 
     * @param newTargetEntity The target entity, non-{@code null}.
     * 
     * @return The new request in which given new entity replaces the current job or queue.
     * 
     * @throws IllegalArgumentException If the target entity is null or does not match the type of the current target entity.
     * 
     * @see #forJob
     * @see #forQueue
     * @see #forJobAndQueue
     * 
     */
    @Override
    public final Req forTargetEntity (final SimEntity newTargetEntity)
    {
      if (newTargetEntity == null)
        throw new IllegalArgumentException ();
      if (getTargetEntity () == null)
        throw new IllegalStateException ();
      if (getTargetEntity () instanceof SimJob)
      {
        if (! (newTargetEntity instanceof SimJob))
          throw new IllegalArgumentException ();
        if (getQueue () != null)
          return forJobAndQueue ((SimJob) newTargetEntity, getQueue ());
        else
          return forJob ((SimJob) newTargetEntity);
      }
      else if (getTargetEntity () instanceof SimQueue)
      {
        if (! (newTargetEntity instanceof SimQueue))
          throw new IllegalArgumentException ();
        if (getJob () != null)
          return forJobAndQueue (getJob (), (SimQueue) newTargetEntity);
        else
          return forQueue ((SimQueue) newTargetEntity);        
      }
      else
        throw new IllegalArgumentException ();
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires (only) a job argument.
   * 
   * The queue is always {@code null}, and the job is the target entity.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   * @see #getTargetEntity
   * 
   */
  abstract static class RequestJ<O extends SimEntityOperation, Req extends RequestJ>
  extends RequestJAndOrQ<O, Req>
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
      super (job, null);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires (only) a queue argument.
   * 
   * The job is always {@code null}, and the queue is the target entity.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   * @see #getTargetEntity
   * 
   */
  abstract static class RequestQ<O extends SimEntityOperation, Req extends RequestQ>
  extends RequestJAndOrQ<O, Req>
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
      super (null, queue);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH JOB AND QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires a job and a queue argument.
   * 
   * Both job and queue are non-{@code null}, and either one of them is the target entity.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   * @see #getTargetEntity
   * 
   */
  abstract static class RequestJQ<O extends SimEntityOperation, Req extends RequestJQ>
  extends RequestJAndOrQ<O, Req>
  {
    
    /** Creates the request, with choice of target-entity selection.
     * 
     * @param job         The job, non-{@code null}.
     * @param queue       The queue, non-{@code null}.
     * @param jobIsTarget Whether the job ({@code true}) or the queue ({@code false}) is the target entity.
     * 
     * @throws IllegalArgumentException If the job or queue is {@code null}.
     * 
     * @see #getTargetEntity
     * 
     */
    public RequestJQ (final SimJob job, final SimQueue queue, final boolean jobIsTarget)
    {
      super (job, queue, jobIsTarget);
      // Must explicitly check this as our super-class will in certain cases happily accept one of the arguments being null.
      if (job == null || queue == null)
        throw new IllegalArgumentException ();
    }
    
    /** Creates the request with the queue becoming the target entity.
     * 
     * @param job   The job, non-{@code null}.
     * @param queue The queue, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job or queue is {@code null}.
     * 
     * @see #getTargetEntity
     * 
     */
    public RequestJQ (final SimJob job, final SimQueue queue)
    {
      this (job, queue, false);
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

    @Override
    public ArrivalRequest forJob (final SimJob job)
    {
      if (job == null)
        throw new IllegalArgumentException ();
      return new ArrivalRequest (job, getQueue ());
    }

    @Override
    public ArrivalRequest forQueue (final SimQueue queue)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      return new ArrivalRequest (getJob (), queue);
    }

    @Override
    public ArrivalRequest forJobAndQueue (final SimJob job, final SimQueue queue)
    {
      if (job == null || queue == null)
        throw new IllegalArgumentException ();
      return new ArrivalRequest (job, queue);
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
  
  /** A request for the revocation operation {@link Revocation}.
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
    
    /** Returns the singleton instance of {@link Revocation}.
     * 
     * @return The singleton instance of {@link Revocation}.
     * 
     */
    @Override
    public final Revocation getOperation ()
    {
      return Revocation.INSTANCE;
    }
    
    @Override
    public RevocationRequest forJob (final SimJob job)
    {
      if (job == null)
        throw new IllegalArgumentException ();
      return new RevocationRequest (job, getQueue (), isInterruptService ());
    }

    @Override
    public RevocationRequest forQueue (final SimQueue queue)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      return new RevocationRequest (getJob (), queue, isInterruptService ());
    }

    @Override
    public RevocationRequest forJobAndQueue (final SimJob job, final SimQueue queue)
    {
      if (job == null || queue == null)
        throw new IllegalArgumentException ();
      return new RevocationRequest (job, queue, isInterruptService ());
    }
    
  }
  
  /** A reply for the revocation operation {@link Revocation}.
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
    
    /** Returns the singleton instance of {@link Revocation}.
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
