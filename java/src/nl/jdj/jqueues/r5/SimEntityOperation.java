package nl.jdj.jqueues.r5;

/** The definition of an operation on a {@link SimEntity}.
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
 */
public interface SimEntityOperation
  <J extends SimJob,
   Q extends SimQueue,
   O extends SimEntityOperation,
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
    
  /** Performs the operation on given entity at given time with given request.
   * 
   * @param time    The time at which to perform the request.
   * @param entity  The entity, non-{@code null}.
   * @param request The request, non-{@code null}.
   * 
   * @return The result (reply) of the operation, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the entity or request is {@code null}, time is in the past,
   *                                  the request is of illegal type, or its parameter values are illegal.
   * 
   */
  Rep doOperation (double time, SimEntity<? extends J, ? extends Q> entity, Req request);
  
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
      
  }
    
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
    
}
