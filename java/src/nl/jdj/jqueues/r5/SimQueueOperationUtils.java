package nl.jdj.jqueues.r5;

/** Utility class defining the {@link SimEntityOperation}s for a {@link SimQueue}.
 * 
 */
public class SimQueueOperationUtils
{

  /** Prevents instantiation.
   * 
   */
  private SimQueueOperationUtils ()
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATION REQUEST WITH JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A request for a queue operation that requires a job argument.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * 
   */
  protected abstract static class OperationRequestWithJob<O extends SimEntityOperation, Req extends SimEntityOperation.Request>
  implements SimEntityOperation.Request<O, Req>
  {
    
    /** Creates the request.
     * 
     * @param job The job, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job is {@code null}.
     * 
     */
    public OperationRequestWithJob (final SimJob job)
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
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queue-access vacation operation on a {@link SimQueue}.
   * 
   * @see SimQueue#setQueueAccessVacation
   * 
   */
  public final static class QueueAccessVacationOperation
  implements
    SimEntityOperation<SimJob, SimQueue, QueueAccessVacationOperation, QueueAccessVacationRequest, QueueAccessVacationReply>
  {

    /** Prevents instantiation.
     * 
     */
    private QueueAccessVacationOperation ()
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
    
    private final static QueueAccessVacationOperation INSTANCE = new QueueAccessVacationOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static QueueAccessVacationOperation getInstance ()
    {
      return QueueAccessVacationOperation.INSTANCE;
    }
    
    /** Returns "QueueAccessVacation".
     * 
     * @return "QueueAccessVacation".
     * 
     */
    @Override
    public final String getName ()
    {
      return "QueueAccessVacation";
    }

    /** Returns the class of {@link QueueAccessVacationRequest}.
     * 
     * @return The class of {@link QueueAccessVacationRequest}.
     * 
     */
    @Override
    public final Class<QueueAccessVacationRequest> getOperationRequestClass ()
    {
      return QueueAccessVacationRequest.class;
    }

    /** Returns the class of {@link QueueAccessVacationReply}.
     * 
     * @return The class of {@link QueueAccessVacationReply}.
     * 
     */
    @Override
    public final Class<QueueAccessVacationReply> getOperationReplyClass ()
    {
      return QueueAccessVacationReply.class;
    }

    /** Invokes {@link SimQueue#setQueueAccessVacation} on the entity.
     * 
     * @see SimQueue#setQueueAccessVacation
     * 
     */
    @Override
    public final QueueAccessVacationReply doOperation
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final QueueAccessVacationRequest request)
    {
      if (entity == null
        || (! (entity instanceof SimQueue))
        || request == null
        || (! (request instanceof QueueAccessVacationRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      ((SimQueue) entity).setQueueAccessVacation (time, request.isStart ());
      return new QueueAccessVacationReply (request);
    }
    
  }
  
  /** A request for the queue-access vacation operation {@link QueueAccessVacationOperation}.
   * 
   */
  public final static class QueueAccessVacationRequest
  implements SimEntityOperation.Request<QueueAccessVacationOperation, QueueAccessVacationRequest>
  {
    
    /** Creates the request.
     * 
     * @param start Whether to start ({@code true}) or end ({@code false}) the vacation.
     * 
     */
    public QueueAccessVacationRequest (final boolean start)
    {
      this.start = start;
    }
    
    /** Returns the singleton instance of {@link QueueAccessVacationOperation}.
     * 
     * @return The singleton instance of {@link QueueAccessVacationOperation}.
     * 
     */
    @Override
    public final QueueAccessVacationOperation getOperation ()
    {
      return QueueAccessVacationOperation.INSTANCE;
    }
    
    private final boolean start;
    
    /** Returns whether to start or end the vacation.
     * 
     * 
     * @return Whether to start ({@code true}) or end ({@code false}) the vacation.
     * 
     * 
     */
    public final boolean isStart ()
    {
      return this.start;
    }
    
  }
  
  /** A reply for the reset operation {@link QueueAccessVacationOperation}.
   * 
   */
  public final static class QueueAccessVacationReply
  implements SimEntityOperation.Reply<QueueAccessVacationOperation, QueueAccessVacationRequest, QueueAccessVacationReply>
  {

    private final QueueAccessVacationRequest request;
    
    /** Creates the reply.
     * 
     * @param request The applicable {@link QueueAccessVacationRequest}.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public QueueAccessVacationReply (final QueueAccessVacationRequest request)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
    }
    
    /** Returns the singleton instance of {@link QueueAccessVacationOperation}.
     * 
     * @return The singleton instance of {@link QueueAccessVacationOperation}.
     * 
     */
    @Override
    public final QueueAccessVacationOperation getOperation ()
    {
      return QueueAccessVacationOperation.INSTANCE;
    }

    @Override
    public final QueueAccessVacationRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The arrival operation on a {@link SimQueue}.
   * 
   * @see SimQueue#arrive
   * 
   */
  public final static class ArrivalOperation
  implements SimEntityOperation<SimJob, SimQueue, ArrivalOperation, ArrivalRequest, ArrivalReply>
  {

    /** Prevents instantiation.
     * 
     */
    private ArrivalOperation ()
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
    
    private final static ArrivalOperation INSTANCE = new ArrivalOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static ArrivalOperation getInstance ()
    {
      return ArrivalOperation.INSTANCE;
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

    /** Invokes {@link SimQueue#arrive} on the entity.
     * 
     * @see SimQueue#arrive
     * 
     */
    @Override
    public final ArrivalReply doOperation
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final ArrivalRequest request)
    {
      if (entity == null
        || (! (entity instanceof SimQueue))
        || request == null
        || (! (request instanceof ArrivalRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      ((SimQueue) entity).arrive (time, request.getJob ());
      return new ArrivalReply (request);
    }
    
  }
  
  /** A request for the arrival operation {@link ArrivalOperation}.
   * 
   */
  public final static class ArrivalRequest
  extends OperationRequestWithJob<ArrivalOperation, ArrivalRequest>
  implements SimEntityOperation.Request<ArrivalOperation, ArrivalRequest>
  {
    
    /** Creates the request.
     * 
     * @param job The job, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job is {@code null}.
     * 
     */
    public ArrivalRequest (final SimJob job)
    {
      super (job);
    }
    
    /** Returns the singleton instance of {@link ArrivalOperation}.
     * 
     * @return The singleton instance of {@link ArrivalOperation}.
     * 
     */
    @Override
    public final ArrivalOperation getOperation ()
    {
      return ArrivalOperation.INSTANCE;
    }
    
  }
  
  /** A reply for the arrival operation {@link ArrivalOperation}.
   * 
   */
  public final static class ArrivalReply
  implements SimEntityOperation.Reply<ArrivalOperation, ArrivalRequest, ArrivalReply>
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
    
    /** Returns the singleton instance of {@link ArrivalOperation}.
     * 
     * @return The singleton instance of {@link ArrivalOperation}.
     * 
     */
    @Override
    public final ArrivalOperation getOperation ()
    {
      return ArrivalOperation.INSTANCE;
    }

    @Override
    public final ArrivalRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The revocation operation on a {@link SimQueue}.
   * 
   * @see SimQueue#revoke
   * 
   */
  public final static class RevocationOperation
  implements SimEntityOperation<SimJob, SimQueue, RevocationOperation, RevocationRequest, RevocationReply>
  {

    /** Prevents instantiation.
     * 
     */
    private RevocationOperation ()
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
    
    private final static RevocationOperation INSTANCE = new RevocationOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static RevocationOperation getInstance ()
    {
      return RevocationOperation.INSTANCE;
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
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final RevocationRequest request)
    {
      if (entity == null
        || (! (entity instanceof SimQueue))
        || request == null
        || (! (request instanceof RevocationRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      final boolean success = ((SimQueue) entity).revoke (time, request.getJob (), request.isInterruptService ());
      return new RevocationReply (request, success);
    }
    
  }
  
  /** A request for the revocation operation {@link RevocationOperation}.
   * 
   */
  public final static class RevocationRequest
  extends OperationRequestWithJob<RevocationOperation, RevocationRequest>
  implements SimEntityOperation.Request<RevocationOperation, RevocationRequest>
  {
    
    /** Creates the request.
     * 
     * @param job              The job, non-{@code null}.
     * @param interruptService Whether to allow revocation from the job from the service area.
     * 
     * @throws IllegalArgumentException If the job is {@code null}.
     * 
     */
    public RevocationRequest (final SimJob job, final boolean interruptService)
    {
      super (job);
      this.interruptService = interruptService;
    }
    
    private final boolean interruptService;
    
    /** Returns whether to allow revocation from the job from the service area.
     * 
     * @return Whether to allow revocation from the job from the service area.
     * 
     * 
     */
    public final boolean isInterruptService ()
    {
      return this.interruptService;
    }
    
    /** Returns the singleton instance of {@link RevocationOperation}.
     * 
     * @return The singleton instance of {@link RevocationOperation}.
     * 
     */
    @Override
    public final RevocationOperation getOperation ()
    {
      return RevocationOperation.INSTANCE;
    }
    
  }
  
  /** A reply for the revocation operation {@link RevocationOperation}.
   * 
   */
  public final static class RevocationReply
  implements SimEntityOperation.Reply<RevocationOperation, RevocationRequest, RevocationReply>
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
    
    /** Returns the singleton instance of {@link RevocationOperation}.
     * 
     * @return The singleton instance of {@link RevocationOperation}.
     * 
     */
    @Override
    public final RevocationOperation getOperation ()
    {
      return RevocationOperation.INSTANCE;
    }

    private final RevocationRequest request;
    
    @Override
    public final RevocationRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The server-access credits operation on a {@link SimQueue}.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  public final static class ServerAccessCreditsOperation
  implements
    SimEntityOperation<SimJob, SimQueue, ServerAccessCreditsOperation, ServerAccessCreditsRequest, ServerAccessCreditsReply>
  {

    /** Prevents instantiation.
     * 
     */
    private ServerAccessCreditsOperation ()
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
    
    private final static ServerAccessCreditsOperation INSTANCE = new ServerAccessCreditsOperation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static ServerAccessCreditsOperation getInstance ()
    {
      return ServerAccessCreditsOperation.INSTANCE;
    }
    
    /** Returns "ServerAccessCredits".
     * 
     * @return "ServerAccessCredits".
     * 
     */
    @Override
    public final String getName ()
    {
      return "ServerAccessCredits";
    }

    /** Returns the class of {@link ServerAccessCreditsRequest}.
     * 
     * @return The class of {@link ServerAccessCreditsRequest}.
     * 
     */
    @Override
    public final Class<ServerAccessCreditsRequest> getOperationRequestClass ()
    {
      return ServerAccessCreditsRequest.class;
    }

    /** Returns the class of {@link ServerAccessCreditsReply}.
     * 
     * @return The class of {@link ServerAccessCreditsReply}.
     * 
     */
    @Override
    public final Class<ServerAccessCreditsReply> getOperationReplyClass ()
    {
      return ServerAccessCreditsReply.class;
    }

    /** Invokes {@link SimQueue#setServerAccessCredits} on the entity.
     * 
     * @see SimQueue#setServerAccessCredits
     * 
     */
    @Override
    public final ServerAccessCreditsReply doOperation
    (final double time, final SimEntity<? extends SimJob, ? extends SimQueue> entity, final ServerAccessCreditsRequest request)
    {
      if (entity == null
        || (! (entity instanceof SimQueue))
        || request == null
        || (! (request instanceof ServerAccessCreditsRequest))
        || request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      ((SimQueue) entity).setServerAccessCredits (time, request.getCredits ());
      return new ServerAccessCreditsReply (request);
    }
    
  }
  
  /** A request for the server-access credits operation {@link ServerAccessCreditsOperation}.
   * 
   */
  public final static class ServerAccessCreditsRequest
  implements SimEntityOperation.Request<ServerAccessCreditsOperation, ServerAccessCreditsRequest>
  {
    
    /** Creates the request.
     * 
     * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
     * 
     * @throws IllegalArgumentException If credits is (strictly) negative.
     * 
     */
    public ServerAccessCreditsRequest (final int credits)
    {
      if (credits < 0)
        throw new IllegalArgumentException ();
      this.credits = credits;
    }
    
    /** Returns the singleton instance of {@link ServerAccessCreditsOperation}.
     * 
     * @return The singleton instance of {@link ServerAccessCreditsOperation}.
     * 
     */
    @Override
    public final ServerAccessCreditsOperation getOperation ()
    {
      return ServerAccessCreditsOperation.INSTANCE;
    }
    
    private final int credits;
    
    /** Returns the number of credits argument of this request.
     * 
     * @return The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
     * 
     */
    public final int getCredits ()
    {
      return this.credits;
    }
    
  }
  
  /** A reply for the reset operation {@link ServerAccessCreditsOperation}.
   * 
   */
  public final static class ServerAccessCreditsReply
  implements SimEntityOperation.Reply<ServerAccessCreditsOperation, ServerAccessCreditsRequest, ServerAccessCreditsReply>
  {

    private final ServerAccessCreditsRequest request;
    
    /** Creates the reply.
     * 
     * @param request The applicable {@link ServerAccessCreditsRequest}.
     * 
     * @throws IllegalArgumentException If the request is {@code null}.
     * 
     */
    public ServerAccessCreditsReply (final ServerAccessCreditsRequest request)
    {
      if (request == null)
        throw new IllegalArgumentException ();
      this.request = request;
    }
    
    /** Returns the singleton instance of {@link ServerAccessCreditsOperation}.
     * 
     * @return The singleton instance of {@link ServerAccessCreditsOperation}.
     * 
     */
    @Override
    public final ServerAccessCreditsOperation getOperation ()
    {
      return ServerAccessCreditsOperation.INSTANCE;
    }

    @Override
    public final ServerAccessCreditsRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
}
