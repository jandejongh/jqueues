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
package nl.jdj.jqueues.r5.extensions.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for composite queues.
 *
 * @see SimQueueComposite
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
public final class SimQueueCompositeStateHandler
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the state handler for composite queues.
   * 
   * @param subQueues      The sub-queues.
   * @param subQueueStates The individual states (initial state objects) of the sub-queues (in proper order!)
   * 
   */
  public SimQueueCompositeStateHandler (final Set<SimQueue> subQueues, final Set<DefaultSimQueueState> subQueueStates)
  {
    if (subQueues == null || subQueueStates == null || subQueues.size () != subQueueStates.size ())
      throw new IllegalArgumentException ();
    this.subQueues = new ArrayList<> (subQueues);
    this.subQueueStates = new ArrayList<> (subQueueStates);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUBQUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final List<SimQueue> subQueues;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUBQUEUE STATES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final List<DefaultSimQueueState> subQueueStates;

  /** Gets the state of sub-queue with given index.
   * 
   * @param  i The index.
   * 
   * @return The state of sub-queue with given index.
   * 
   */
  public final DefaultSimQueueState getSubQueueState (final int i)
  {
    return this.subQueueStates.get (i);
  }
  
  /** Resets the state of each sub-queue.
   * 
   */
  protected void resetSubQueueStates ()
  {
    for (final DefaultSimQueueState subQueueState : subQueueStates)
      subQueueState.reset ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SimQueueCompositeHandler".
   * 
   * @return "SimQueueCompositeHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueCompositeHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    resetSubQueueStates ();
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    resetSubQueueStates ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
