package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.*;

/** Utility class defining {@link SimEntityOperation}s for a {@link SimQueueWithGate}.
 * 
 */
public class SimQueueWithGateOperationUtils
{

  /** Prevents instantiation.
   * 
   */
  private SimQueueWithGateOperationUtils ()
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The gate-passage credits operation on a {@link SimQueueWithGate}.
   * 
   * @see SimQueueWithGate#setGatePassageCredits
   * 
   */
  public final static class GatePassageCreditsOperation
  implements
    SimEntityOperation<SimJob, SimQueue, GatePassageCreditsOperation, GatePassageCreditsRequest, GatePassageCreditsReply>
  {

    /** Prevents instantiation.
     * 
     */
    private GatePassageCreditsOperation ()
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
    
    private final static GatePassageCreditsOperation INSTANCE = new GatePassageCreditsOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static GatePassageCreditsOperation getInstance ()
    {
      return GatePassageCreditsOperation.INSTANCE;
    }
    
    /** Returns "GatePassageCredits".
     * 
     * @return "GatePassageCredits".
     * 
     */
    @Override
    public final String getName ()
    {
      return "GatePassageCredits";
    }

    /** Returns the class of {@link GatePassageCreditsRequest}.
     * 
     * @return The class of {@link GatePassageCreditsRequest}.
     * 
     */
    @Override
    public final Class<GatePassageCreditsRequest> getOperationRequestClass ()
    {
      return GatePassageCreditsRequest.class;
    }

    /** Returns the class of {@link GatePassageCreditsReply}.
     * 
     * @return The class of {@link GatePassageCreditsReply}.
     * 
     */
    @Override
    public final Class<GatePassageCreditsReply> getOperationReplyClass ()
    {
      return GatePassageCreditsReply.class;
    }

    /** Invokes {@link SimQueueWithGate#setGatePassageCredits} on the entity.
     * 
     * @see SimQueueWithGate#setGatePassageCredits
     * 
     */
    @Override
    public final GatePassageCreditsReply doOperation
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final GatePassageCreditsRequest request)
    {
      if (entity == null
        || request == null
        || (! (request instanceof GatePassageCreditsRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      if (entity instanceof SimQueueWithGate)
      {
        // Our target entity has native support for gate-passage credits, hence we directly invoke the appropriate method.
        ((SimQueueWithGate) entity).setGatePassageCredits (time, request.getCredits ());
        return new GatePassageCreditsReply (request);
      }
      else if (entity.getRegisteredOperations ().contains
        (SimQueueWithGateOperationUtils.GatePassageCreditsOperation.getInstance ()))
        // Our target entity does NOT have native support for gate-passage credits.
        // We take the risk of directly issuing the request at the entity (XXX at the huge risk of infinite recursion!).
        return entity.doOperation (time, request);
      else
        // Our target entity has no clue of gate-passage credits.
        // We can either throw an exception here, or put our hopes on the entity accepting unknown operations.
        // We put our stakes on the last option...
        return entity.doOperation (time, request);
    }
    
  }
  
  /** A request for the gate-passage credits operation {@link GatePassageCreditsOperation}.
   * 
   */
  public final static class GatePassageCreditsRequest
  implements SimEntityOperation.Request<GatePassageCreditsOperation, GatePassageCreditsRequest>
  {
    
    /** Creates the request.
     * 
     * @param credits The new remaining gate-passage credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
     * 
     * @throws IllegalArgumentException If credits is (strictly) negative.
     * 
     */
    public GatePassageCreditsRequest (final int credits)
    {
      if (credits < 0)
        throw new IllegalArgumentException ();
      this.credits = credits;
    }
    
    /** Returns the singleton instance of {@link GatePassageCreditsOperation}.
     * 
     * @return The singleton instance of {@link GatePassageCreditsOperation}.
     * 
     */
    @Override
    public final GatePassageCreditsOperation getOperation ()
    {
      return GatePassageCreditsOperation.INSTANCE;
    }
    
    private final int credits;
    
    /** Returns the number of credits argument of this request.
     * 
     * @return The new remaining gate-passage credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
     * 
     */
    public final int getCredits ()
    {
      return this.credits;
    }
    
  }
  
  /** A reply for the reset operation {@link GatePassageCreditsOperation}.
   * 
   */
  public final static class GatePassageCreditsReply
  implements SimEntityOperation.Reply<GatePassageCreditsOperation, GatePassageCreditsRequest, GatePassageCreditsReply>
  {

    private final GatePassageCreditsRequest request;
    
    /** Creates the reply.
     * 
     * @param request The applicable {@link GatePassageCreditsRequest}.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public GatePassageCreditsReply (final GatePassageCreditsRequest request)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
    }
    
    /** Returns the singleton instance of {@link GatePassageCreditsOperation}.
     * 
     * @return The singleton instance of {@link GatePassageCreditsOperation}.
     * 
     */
    @Override
    public final GatePassageCreditsOperation getOperation ()
    {
      return GatePassageCreditsOperation.INSTANCE;
    }

    @Override
    public final GatePassageCreditsRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
}
