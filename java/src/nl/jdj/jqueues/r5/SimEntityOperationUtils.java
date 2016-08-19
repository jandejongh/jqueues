package nl.jdj.jqueues.r5;

/** Utility class defining the {@link SimEntityOperation}s for a {@link SimEntity}.
 * 
 * <p>
 * Defines reset and update operations.
 *
 */
public final class SimEntityOperationUtils
{
  
  /** Prevents instantiation.
   * 
   */
  private SimEntityOperationUtils ()
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The reset operation on a {@link SimEntity}.
   * 
   * @see SimEntity#resetEntity
   * 
   */
  public final static class ResetOperation
  implements SimEntityOperation<SimJob, SimQueue, ResetOperation, ResetRequest, ResetReply>
  {

    /** Prevents instantiation.
     * 
     */
    private ResetOperation ()
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
    
    private final static ResetOperation INSTANCE = new ResetOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static ResetOperation getInstance ()
    {
      return ResetOperation.INSTANCE;
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
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final ResetRequest request)
    {
      if (entity == null
        || request == null
        || (! (request instanceof ResetRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      entity.resetEntity ();
      return new ResetReply (request);
    }
    
  }
  
  /** A request for the reset operation {@link ResetOperation}.
   * 
   */
  public final static class ResetRequest
  implements SimEntityOperation.Request<ResetOperation, ResetRequest>
  {
    
    /** Creates the request.
     * 
     */
    public ResetRequest ()
    {
    }
    
    /** Returns the singleton instance of {@link ResetOperation}.
     * 
     * @return The singleton instance of {@link ResetOperation}.
     * 
     */
    @Override
    public final ResetOperation getOperation ()
    {
      return ResetOperation.INSTANCE;
    }
    
  }
  
  /** A reply for the reset operation {@link ResetOperation}.
   * 
   */
  public final static class ResetReply
  implements SimEntityOperation.Reply<ResetOperation, ResetRequest, ResetReply>
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
    
    /** Returns the singleton instance of {@link ResetOperation}.
     * 
     * @return The singleton instance of {@link ResetOperation}.
     * 
     */
    @Override
    public final ResetOperation getOperation ()
    {
      return ResetOperation.INSTANCE;
    }

    @Override
    public final ResetRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The update operation on a {@link SimEntity}.
   * 
   * @see SimEntity#update
   * 
   */
  public final static class UpdateOperation
  implements SimEntityOperation<SimJob, SimQueue, UpdateOperation, UpdateRequest, UpdateReply>
  {

    /** Prevents instantiation.
     * 
     */
    private UpdateOperation ()
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
    
    private final static UpdateOperation INSTANCE = new UpdateOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static UpdateOperation getInstance ()
    {
      return UpdateOperation.INSTANCE;
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
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final UpdateRequest request)
    {
      if (entity == null
        || request == null
        || (! (request instanceof UpdateRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      entity.update (time);
      return new UpdateReply (request);
    }
    
  }
  
  /** A request for the update operation {@link UpdateOperation}.
   * 
   */
  public final static class UpdateRequest
  implements SimEntityOperation.Request<UpdateOperation, UpdateRequest>
  {
    
    /** Creates the request.
     * 
     */
    public UpdateRequest ()
    {
    }
    
    /** Returns the singleton instance of {@link UpdateOperation}.
     * 
     * @return The singleton instance of {@link UpdateOperation}.
     * 
     */
    @Override
    public final UpdateOperation getOperation ()
    {
      return UpdateOperation.INSTANCE;
    }
    
  }
  
  public final static class UpdateReply
  implements SimEntityOperation.Reply<UpdateOperation, UpdateRequest, UpdateReply>
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
    
    /** Returns the singleton instance of {@link UpdateOperation}.
     * 
     * @return The singleton instance of {@link UpdateOperation}.
     * 
     */
    @Override
    public final UpdateOperation getOperation ()
    {
      return UpdateOperation.INSTANCE;
    }

    @Override
    public final UpdateRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
}
