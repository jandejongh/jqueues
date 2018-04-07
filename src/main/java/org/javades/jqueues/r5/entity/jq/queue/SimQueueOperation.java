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
package org.javades.jqueues.r5.entity.jq.queue;

import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.SimEntityOperation;
import org.javades.jqueues.r5.entity.jq.SimJQOperation;
import org.javades.jqueues.r5.entity.jq.job.SimJob;

/** The definition of an operation on a {@link SimQueue}.
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
public interface SimQueueOperation
  <J extends SimJob,
   Q extends SimQueue,
   O extends SimEntityOperation,
   Req extends SimEntityOperation.Request,
   Rep extends SimEntityOperation.Reply>
  extends SimJQOperation<J, Q, O, Req, Rep>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queue-access vacation operation on a {@link SimQueue}.
   * 
   * @see SimQueue#setQueueAccessVacation
   * 
   */
  public final static class QueueAccessVacation
  implements
    SimEntityOperation<QueueAccessVacation, QueueAccessVacationRequest, QueueAccessVacationReply>
  {

    /** Prevents instantiation.
     * 
     */
    private QueueAccessVacation ()
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
    
    private final static QueueAccessVacation INSTANCE = new QueueAccessVacation ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static QueueAccessVacation getInstance ()
    {
      return QueueAccessVacation.INSTANCE;
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
    (final double time, final QueueAccessVacationRequest request)
    {
      if (request == null
      ||  request.getQueue () == null
      ||  request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      request.getQueue ().setQueueAccessVacation (time, request.isStart ());
      return new QueueAccessVacationReply (request);
    }
    
  }
  
  /** A request for the queue-access vacation operation {@link QueueAccessVacation}.
   * 
   */
  public final static class QueueAccessVacationRequest
  extends RequestQ<QueueAccessVacation, QueueAccessVacationRequest>
  {
    
    /** Creates the request.
     * 
     * @param queue The queue, non-{@code null}.
     * @param start Whether to start ({@code true}) or end ({@code false}) the vacation.
     * 
     */
    public QueueAccessVacationRequest (final SimQueue queue, final boolean start)
    {
      super (queue);
      this.start = start;
    }
    
    /** Returns the singleton instance of {@link QueueAccessVacation}.
     * 
     * @return The singleton instance of {@link QueueAccessVacation}.
     * 
     */
    @Override
    public final QueueAccessVacation getOperation ()
    {
      return QueueAccessVacation.INSTANCE;
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
    
    @Override
    public QueueAccessVacationRequest forJob (final SimJob job)
    {
      throw new UnsupportedOperationException ();
    }

    @Override
    public QueueAccessVacationRequest forQueue (final SimQueue queue)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      return new QueueAccessVacationRequest (queue, isStart ());
    }

    @Override
    public QueueAccessVacationRequest forJobAndQueue (final SimJob job, final SimQueue queue)
    {
      throw new UnsupportedOperationException ();
    }
    
  }
  
  /** A reply for the queue-access vacation operation {@link QueueAccessVacation}.
   * 
   */
  public final static class QueueAccessVacationReply
  implements SimEntityOperation.Reply<QueueAccessVacation, QueueAccessVacationRequest, QueueAccessVacationReply>
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
    
    /** Returns the singleton instance of {@link QueueAccessVacation}.
     * 
     * @return The singleton instance of {@link QueueAccessVacation}.
     * 
     */
    @Override
    public final QueueAccessVacation getOperation ()
    {
      return QueueAccessVacation.INSTANCE;
    }

    @Override
    public final QueueAccessVacationRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** The server-access credits operation on a {@link SimQueue}.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  public final static class ServerAccessCredits
  implements
    SimEntityOperation<ServerAccessCredits, ServerAccessCreditsRequest, ServerAccessCreditsReply>
  {

    /** Prevents instantiation.
     * 
     */
    private ServerAccessCredits ()
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
    
    private final static ServerAccessCredits INSTANCE = new ServerAccessCredits ();
    
    /** Returns the single instance of this class.
     * 
     * <p>
     * Singleton pattern.
     * 
     * @return The single instance of this class.
     * 
     */
    public static ServerAccessCredits getInstance ()
    {
      return ServerAccessCredits.INSTANCE;
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
    (final double time, final ServerAccessCreditsRequest request)
    {
      if (request == null
      ||  request.getQueue () == null
      ||  request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      request.getQueue ().setServerAccessCredits (time, request.getCredits ());
      return new ServerAccessCreditsReply (request);
    }
    
  }
  
  /** A request for the server-access credits operation {@link ServerAccessCredits}.
   * 
   */
  public final static class ServerAccessCreditsRequest
  extends RequestQ<ServerAccessCredits, ServerAccessCreditsRequest>
  {
    
    /** Creates the request.
     * 
     * @param queue   The queue, non-{@code null}.
     * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
     * 
     * @throws IllegalArgumentException If the queue is {@code null} or credits is (strictly) negative.
     * 
     */
    public ServerAccessCreditsRequest (final SimQueue queue, final int credits)
    {
      super (queue);
      if (credits < 0)
        throw new IllegalArgumentException ();
      this.credits = credits;
    }
    
    /** Returns the singleton instance of {@link ServerAccessCredits}.
     * 
     * @return The singleton instance of {@link ServerAccessCredits}.
     * 
     */
    @Override
    public final ServerAccessCredits getOperation ()
    {
      return ServerAccessCredits.INSTANCE;
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
    
    @Override
    public ServerAccessCreditsRequest forJob (final SimJob job)
    {
      throw new UnsupportedOperationException ();
    }

    @Override
    public ServerAccessCreditsRequest forQueue (final SimQueue queue)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      return new ServerAccessCreditsRequest (queue, getCredits ());
    }

    @Override
    public ServerAccessCreditsRequest forJobAndQueue (final SimJob job, final SimQueue queue)
    {
      throw new UnsupportedOperationException ();
    }
    
  }
  
  /** A reply for the server-access credits operation {@link ServerAccessCredits}.
   * 
   */
  public final static class ServerAccessCreditsReply
  implements SimEntityOperation.Reply<ServerAccessCredits, ServerAccessCreditsRequest, ServerAccessCreditsReply>
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
    
    /** Returns the singleton instance of {@link ServerAccessCredits}.
     * 
     * @return The singleton instance of {@link ServerAccessCredits}.
     * 
     */
    @Override
    public final ServerAccessCredits getOperation ()
    {
      return ServerAccessCredits.INSTANCE;
    }

    @Override
    public final ServerAccessCreditsRequest getRequest ()
    {
      return this.request;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
