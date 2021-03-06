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

import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jsimulation.r5.SimEventList;

/** An abstract base class sub-classing {@link AbstractSimQueue}
 *  for "classic" queueing-system models with a buffer size (possibly zero or infinite)
 *  and a fixed number of servers (possibly zero or infinite).
 *
 * <p>
 * This abstract base class in itself merely adds place-holders for the buffer size and the number of servers as properties
 * to {@link AbstractSimQueue}.
 * It does not schedule.
 * It <i>does</i> however insist that the buffer size and the number of servers are non-negative,
 * and it documents the interpretation of {@link Integer#MAX_VALUE} as infinity for both property values.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public abstract class AbstractClassicSimQueue
  <J extends SimJob, Q extends AbstractClassicSimQueue>
  extends AbstractSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a classic queueing system given an event list, buffer size and number of servers.
   *
   * @param eventList       The event list to use.
   * @param bufferSize      The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param numberOfServers The number of servers (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   *
   */
  protected AbstractClassicSimQueue
  (final SimEventList eventList, final int bufferSize, final int numberOfServers)
  {
    super (eventList);
    if (bufferSize < 0 || numberOfServers < 0)
      throw new IllegalArgumentException ();
    this.bufferSize = bufferSize;
    this.numberOfServers= numberOfServers;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BUFFER SIZE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int bufferSize;
  
  /** Returns the buffer size.
   * 
   * <p>
   * The buffer size is fixed upon construction and cannot be changed.
   * 
   * @return The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * 
   */
  public final int getBufferSize ()
  {
    return this.bufferSize;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF SERVERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The number of servers, non-negative.
   * 
   */
  private final int numberOfServers;
  
  /** Returns the number of servers (non-negative).
   * 
   * @return The number of servers (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * 
   */
  public final int getNumberOfServers ()
  {
    return this.numberOfServers;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
