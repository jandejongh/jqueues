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
package org.javades.jqueues.r5.entity.jq.queue.serverless;

import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEventList;

/** In the {@link LeakyBucket} queue jobs depart without service in arrival order,
 *  but not at a higher rate than a given limit,
 *  at the expense of waiting in a waiting area with limited capacity,
 *  or being dropped if the waiting area is fully occupied.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * <p>
 * Apart from having a potentially finite-sized (fixed) waiting area defined by the property {@code bufferSize},
 * {@link LeakyBucket} is equal to {@link DLIMIT}.
 * If the buffer size is infinite ({@link Integer#MAX_VALUE}), this queueing system is equivalent to {@link DLIMIT}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see DLIMIT
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
public class LeakyBucket<J extends SimJob, Q extends LeakyBucket>
extends DLIMIT<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link LeakyBucket} queue with infinite buffer size given an event list and (departure) rate limit.
   *
   * @param eventList  The event list to use.
   * @param bufferSize The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param rateLimit  The (departure) rate limit, non-negative.
   *
   * @throws IllegalArgumentException If the buffer size or the rate limit is strictly negative.
   * 
   */
  public LeakyBucket (final SimEventList eventList, final int bufferSize, final double rateLimit)
  {
    super (eventList, bufferSize, rateLimit);
  }
  
  /** Returns a new {@link LeakyBucket} object on the same {@link SimEventList}
   *  with the same buffer size and (departure) rate limit.
   * 
   * @return A new {@link LeakyBucket} object on the same {@link SimEventList}
   *         with the same buffer size and (departure) rate limit.
   * 
   * @see #getEventList
   * @see #getBufferSize
   * @see #getRateLimit
   * 
   */
  @Override
  public LeakyBucket<J, Q> getCopySimQueue ()
  {
    return new LeakyBucket<> (getEventList (), getBufferSize (), getRateLimit ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "LeakyBucket[bufferSize,rateLimit]".
   * 
   * @return "LeakyBucket[bufferSize,rateLimit]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    if (getBufferSize () == Integer.MAX_VALUE)
      return "LeakyBucket[Infinity," + getRateLimit () + "]";
    else
      return "LeakyBucket[" + getBufferSize () + "," + getRateLimit () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
