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
package nl.jdj.jqueues.r5.entity;

/** The definition of an operation on a {@link SimEntity} (or on multiple ones).
 * 
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
public interface SimEntityOperation
  <O extends SimEntityOperation,
   Req extends SimEntityOperation.Request,
   Rep extends SimEntityOperation.Reply>
{
  
  /** Gets the name of this operation.
   * 
   * @return The name of this operation, non-{@code null} and non-empty.
   *
   */
  String getName ();
  
  /** Gets the type of requests for this operation.
   * 
   * @return The type of requests for this operation, non-{@code null}.
   * 
   */
  Class<Req> getOperationRequestClass ();
    
  /** Gets the type of replies for this operation.
   * 
   * @return The type of replies for this operation, non-{@code null}.
   * 
   */
  Class<Rep> getOperationReplyClass ();
    
  /** Performs the operation at given time with given request.
   * 
   * @param time    The time at which to perform the request.
   * @param request The request, non-{@code null}.
   * 
   * @return The result (reply) of the operation, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the entity or request is {@code null}, time is in the past,
   *                                  the request is of illegal type, or its parameter values are illegal.
   * 
   */
  Rep doOperation (double time, Req request);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for a {@link SimEntityOperation}.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   */
  interface Request<O extends SimEntityOperation, Req extends SimEntityOperation.Request>
  {

    /** Gets the operation (type).
     * 
     * @return The operation type (fixed).
     * 
     */
    O getOperation ();
      
    /** Gets the target entity; the entity to perform the request.
     * 
     * <p>
     * Normally, the target entity is also the entity or one of the entities to which the request applies.
     * 
     * @return The target entity; the entity to perform the request (may be {@code null}).
     * 
     */
    SimEntity getTargetEntity ();
    
    /** Creates a copy of this request for another target entity.
     * 
     * @param newTargetEntity The new target entity, non-{@code null}.
     * 
     * @return A copy of this request for given target entity.
     * 
     * @throws IllegalArgumentException      If the entity argument is {@code null}.
     * @throws UnsupportedOperationException If this request does not support the (kind of) new target entity.
     * 
     */
    Req forTargetEntity (SimEntity newTargetEntity);
    
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REPLY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A reply from a {@link SimEntityOperation}.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * @param <Rep> The reply type (corresponding to the operation type).
   * 
   */
  interface Reply<O extends SimEntityOperation, Req extends SimEntityOperation.Request, Rep extends SimEntityOperation.Reply>
  {
      
    /** Gets the operation (type).
     * 
     * @return The operation type (fixed).
     * 
     */
    O getOperation ();
      
    /** Returns the {@link Request} to which this reply applies.
     * 
     * @return The {@link Request} to which this reply applies.
     * 
     */
    Req getRequest ();

  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for an operation that requires an entity argument.
   * 
   * <p>
   * The entity provided must be non-{@code null} and is always the target entity.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   * @see Request#getTargetEntity
   * 
   */
  abstract static class RequestE<O extends SimEntityOperation, Req extends SimEntityOperation.Request>
  implements SimEntityOperation.Request<O, Req>
  {
    
    /** Creates the request.
     * 
     * @param entity The entity, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the entity is {@code null}.
     * 
     */
    public RequestE (final SimEntity entity)
    {
      if (entity == null)
        throw new IllegalArgumentException ();
      this.entity = entity;
    }
    
    private final SimEntity entity;
    
    /** Returns the entity argument of this request.
     * 
     * @return The entity argument of this request, non-{@code null}.
     * 
     */
    @Override
    public final SimEntity getTargetEntity ()
    {
      return this.entity;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The (default) reset operation on a {@link SimEntity}.
   * 
   * @see SimEntity#resetEntity
   * 
   */
  public final static class Reset
  implements SimEntityOperation<Reset, ResetRequest, ResetReply>
  {

    /** Prevents instantiation.
     * 
     */
    private Reset ()
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
    
    private final static Reset INSTANCE = new Reset ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static Reset getInstance ()
    {
      return Reset.INSTANCE;
    }
    
    /** Returns "Reset".
     * 
     * @return "Reset".
     * 
     */
    @Override
    public final String getName ()
    {
      return "Reset";
    }

    /** Returns the class of {@link ResetRequest}.
     * 
     * @return The class of {@link ResetRequest}.
     * 
     */
    @Override
    public final Class<ResetRequest> getOperationRequestClass ()
    {
      return ResetRequest.class;
    }

    /** Returns the class of {@link ResetReply}.
     * 
     * @return The class of {@link ResetReply}.
     * 
     */
    @Override
    public final Class<ResetReply> getOperationReplyClass ()
    {
      return ResetReply.class;
    }

    /** Invokes {@link SimEntity#resetEntity} on the entity.
     * 
     * @see SimEntity#resetEntity
     * 
     */
    @Override
    public final ResetReply doOperation
    (final double time, final ResetRequest request)
    {
      if (request == null
        || request.getTargetEntity () == null
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      request.getTargetEntity ().resetEntity ();
      return new ResetReply (request);
    }
    
  }
  
  /** A request for the reset operation {@link Reset}.
   * 
   */
  public final static class ResetRequest
  extends SimEntityOperation.RequestE<Reset, ResetRequest>
  {
    
    /** Creates the request.
     * 
     * @param entity The entity, non-{@code null}.
     * 
     */
    public ResetRequest (final SimEntity entity)
    {
      super (entity);
    }
    
    /** Returns the singleton instance of {@link Reset}.
     * 
     * @return The singleton instance of {@link Reset}.
     * 
     */
    @Override
    public final Reset getOperation ()
    {
      return Reset.INSTANCE;
    }
    
    @Override
    public final ResetRequest forTargetEntity (final SimEntity newTargetEntity)
    {
      if (newTargetEntity == null)
        throw new IllegalArgumentException ();
      return new ResetRequest (newTargetEntity);
    }
    
  }
  
  /** A reply for the reset operation {@link Reset}.
   * 
   */
  public final static class ResetReply
  implements SimEntityOperation.Reply<Reset, ResetRequest, ResetReply>
  {

    private final ResetRequest request;
    
    /** Creates the reply.
     * 
     * @param request The applicable {@link ResetRequest}.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public ResetReply (final ResetRequest request)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
    }
    
    /** Returns the singleton instance of {@link Reset}.
     * 
     * @return The singleton instance of {@link Reset}.
     * 
     */
    @Override
    public final Reset getOperation ()
    {
      return Reset.INSTANCE;
    }

    @Override
    public final ResetRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The (default) update operation on a {@link SimEntity}.
   * 
   * @see SimEntity#update
   * 
   */
  public final static class Update
  implements SimEntityOperation<Update, UpdateRequest, UpdateReply>
  {

    /** Prevents instantiation.
     * 
     */
    private Update ()
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
    
    private final static Update INSTANCE = new Update ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static Update getInstance ()
    {
      return Update.INSTANCE;
    }
    
    /** Returns "Update".
     * 
     * @return "Update".
     * 
     */
    @Override
    public final String getName ()
    {
      return "Update";
    }

    /** Returns the class of {@link UpdateRequest}.
     * 
     * @return The class of {@link UpdateRequest}.
     * 
     */
    @Override
    public final Class<UpdateRequest> getOperationRequestClass ()
    {
      return UpdateRequest.class;
    }

    /** Returns the class of {@link UpdateReply}.
     * 
     * @return The class of {@link UpdateReply}.
     * 
     */
    @Override
    public final Class<UpdateReply> getOperationReplyClass ()
    {
      return UpdateReply.class;
    }

    /** Invokes {@link SimEntity#update} on the entity.
     * 
     * @see SimEntity#update
     * 
     */
    @Override
    public final UpdateReply doOperation
    (final double time, final UpdateRequest request)
    {
      if (request == null
        || request.getTargetEntity () == null
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      request.getTargetEntity ().update (time);
      return new UpdateReply (request);
    }
    
  }
  
  /** A request for the update operation {@link Update}.
   * 
   */
  public final static class UpdateRequest
  extends SimEntityOperation.RequestE<Update, UpdateRequest>
  {
    
    /** Creates the request.
     * 
     * @param entity The entity, non-{@code null}.
     * 
     */
    public UpdateRequest (final SimEntity entity)
    {
      super (entity);
    }
    
    /** Returns the singleton instance of {@link Update}.
     * 
     * @return The singleton instance of {@link Update}.
     * 
     */
    @Override
    public final Update getOperation ()
    {
      return Update.INSTANCE;
    }

    @Override
    public final UpdateRequest forTargetEntity (final SimEntity newTargetEntity)
    {
      if (newTargetEntity == null)
        throw new IllegalArgumentException ();
      return new UpdateRequest (newTargetEntity);
    }
    
  }
  
  /** A reply for the update operation {@link Update}.
   * 
   */
  public final static class UpdateReply
  implements SimEntityOperation.Reply<Update, UpdateRequest, UpdateReply>
  {

    private final UpdateRequest request;
    
    /** Creates the reply.
     * 
     * @param request The applicable {@link UpdateRequest}.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public UpdateReply (final UpdateRequest request)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
    }
    
    /** Returns the singleton instance of {@link Update}.
     * 
     * @return The singleton instance of {@link Update}.
     * 
     */
    @Override
    public final Update getOperation ()
    {
      return Update.INSTANCE;
    }

    @Override
    public final UpdateRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
}
