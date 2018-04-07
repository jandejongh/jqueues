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
package org.javades.jqueues.r5.entity.jq.job;

import org.javades.jqueues.r5.entity.jq.AbstractSimJQ;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEventList;

/** Convenience class with a basic (yet still abstract) implementation of {@link SimJob}.
 *
 * <p>
 * This class maintains an internal reference to the {@link SimQueue} being visited.
 * The method {@link #getServiceTime} is kept abstract on purpose, as forgetting to override a default implementation is
 * considered too risky.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see DefaultSimJob
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
public abstract class AbstractSimJob<J extends SimJob, Q extends SimQueue>
extends AbstractSimJQ<J, Q>
implements SimJob<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link SimJob} with given event list and name.
   * 
   * @param eventList The event list to use, may be {@code null}.
   * @param name The name of the job, may be <code>null</code>.
   * 
   * @see #getEventList
   * @see #setName
   * 
   */
  public AbstractSimJob (final SimEventList eventList, final String name)
  {
    super (eventList, name);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private Q queue = null;
  
  /** Returns the internally stored {@link SimQueue} visiting.
   * 
   * @return The internally stored {@link SimQueue} visiting.
   * 
   */
  @Override
  public final Q getQueue ()
  {
    return this.queue;
  }

  /** Sets the internally stored {@link SimQueue} visiting.
   * 
   * @param queue The new {@link SimQueue} visiting, may be <code>null</code>.
   * 
   */
  @Override
  public final void setQueue (Q queue) throws IllegalStateException
  {
    this.queue = queue;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method and sets the queue to {@code null}.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.queue = null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public abstract double getServiceTime (Q queue) throws IllegalArgumentException;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
