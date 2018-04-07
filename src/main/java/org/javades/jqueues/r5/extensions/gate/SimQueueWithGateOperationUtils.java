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
package org.javades.jqueues.r5.extensions.gate;

import org.javades.jqueues.r5.entity.SimEntityOperation;
import org.javades.jqueues.r5.entity.jq.SimJQOperation;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** Utility class defining {@link SimEntityOperation}s for a {@link SimQueueWithGate}.
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
    SimEntityOperation<GatePassageCreditsOperation, GatePassageCreditsRequest, GatePassageCreditsReply>
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
    (final double time, final GatePassageCreditsRequest request)
    {
      if (request == null
      ||  request.getQueue () == null
      ||  request.getOperation () != getInstance ())
        throw new IllegalArgumentException ();
      final SimQueue queue = request.getQueue ();
      if (queue instanceof SimQueueWithGate)
      {
        // Our target entity has native support for gate-passage credits, hence we directly invoke the appropriate method.
        ((SimQueueWithGate) queue).setGatePassageCredits (time, request.getCredits ());
        return new GatePassageCreditsReply (request);
      }
      else if (queue.getRegisteredOperations ().contains
        (SimQueueWithGateOperationUtils.GatePassageCreditsOperation.getInstance ()))
        // Our target entity does NOT have native support for gate-passage credits.
        // We take the risk of directly issuing the request at the entity (XXX at the huge risk of infinite recursion!).
        return queue.doOperation (time, request);
      else
        // Our target entity has no clue of gate-passage credits.
        // We can either throw an exception here, or put our hopes on the entity accepting unknown operations.
        // We put our stakes on the last option...
        return queue.doOperation (time, request);
    }
    
  }
  
  /** A request for the gate-passage credits operation {@link GatePassageCreditsOperation}.
   * 
   */
  public final static class GatePassageCreditsRequest
  extends SimJQOperation.RequestQ<GatePassageCreditsOperation, GatePassageCreditsRequest>
  {
    
    /** Creates the request.
     * 
     * @param queue   The queue, non-{@code null}.
     * @param credits The new remaining gate-passage credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
     * 
     * @throws IllegalArgumentException If credits is (strictly) negative.
     * 
     */
    public GatePassageCreditsRequest (final SimQueue queue, final int credits)
    {
      super (queue);
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
    
    @Override
    public GatePassageCreditsRequest forJob (final SimJob job)
    {
      throw new UnsupportedOperationException ();
    }

    @Override
    public GatePassageCreditsRequest forQueue (final SimQueue queue)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      return new GatePassageCreditsRequest (queue, getCredits ());
    }

    @Override
    public GatePassageCreditsRequest forJobAndQueue (final SimJob job, final SimQueue queue)
    {
      throw new UnsupportedOperationException ();
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
