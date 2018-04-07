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
package org.javades.jqueues.r5.extensions.ratelimit;

import org.javades.jqueues.r5.entity.jq.queue.serverless.ALIMIT;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DLIMIT;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;
import org.javades.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import org.javades.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for simple (arrival/departure) rate limitation.
 *
 * @see DLIMIT
 * @see ALIMIT
 * @see SimQueuePredictor_LeakyBucket
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
public final class SimQueueRateLimitStateHandler
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueRateLimitStateHandler".
   * 
   * @return "SimQueueRateLimitStateHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueRateLimitStateHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    this.lastArrTimeSet = false;
    this.lastArrTime = Double.NEGATIVE_INFINITY;
    this.lastDepTimeSet = false;
    this.lastDepTime = Double.NEGATIVE_INFINITY;
    this.isRateLimited = false;
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.lastArrTimeSet = false;
    this.lastArrTime = Double.NEGATIVE_INFINITY;
    this.lastDepTimeSet = false;
    this.lastDepTime = Double.NEGATIVE_INFINITY;
    this.isRateLimited = false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LAST ARRIVAL TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean lastArrTimeSet = false;
  
  private double lastArrTime = Double.NEGATIVE_INFINITY;

  /** Returns whether the last arrival time was set since construction or last reset.
   * 
   * @return Whether the last arrival time was set since construction or last reset.
   * 
   * @see #resetHandler
   * @see #getLastArrTime
   * 
   */
  public final boolean isLastArrTimeSet ()
  {
    return this.lastArrTimeSet;
  }
  
  /** Gets the last arrival time.
   * 
   * <p>
   * Does not check whether the time was set since construction or last reset.
   * If not, the default {@link Double#NEGATIVE_INFINITY} is returned.
   * 
   * @return The last arrival time (set), or {@link Double#NEGATIVE_INFINITY}
   *         if no arrival time was set since construction or last reset.
   * 
   * @see #resetHandler
   * @see #setLastArrTime
   * 
   */
  public final double getLastArrTime ()
  {
    return this.lastArrTime;
  }
  
  /** Sets the last arrival time (without error checking) and flags the value has been set.
   * 
   * @param lastArrTime The (new) last arrival time (unchecked).
   * 
   * @see #resetHandler
   * @see #getLastArrTime
   * @see #isLastArrTimeSet
   * 
   */
  public final void setLastArrTime (final double lastArrTime)
  {
    this.lastArrTime = lastArrTime;
    this.lastArrTimeSet = true;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LAST DEPARTURE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean lastDepTimeSet = false;
  
  private double lastDepTime = Double.NEGATIVE_INFINITY;

  /** Returns whether the last departure time was set since construction or last reset.
   * 
   * @return Whether the last departure time was set since construction or last reset.
   * 
   * @see #resetHandler
   * @see #getLastDepTime
   * 
   */
  public final boolean isLastDepTimeSet ()
  {
    return this.lastDepTimeSet;
  }
  
  /** Gets the last departure time.
   * 
   * <p>
   * Does not check whether the time was set since construction or last reset.
   * If not, the default {@link Double#NEGATIVE_INFINITY} is returned.
   * 
   * @return The last departure time (set), or {@link Double#NEGATIVE_INFINITY}
   *         if no departure time was set since construction or last reset.
   * 
   * @see #resetHandler
   * @see #setLastDepTime
   * 
   */
  public final double getLastDepTime ()
  {
    return this.lastDepTime;
  }
  
  /** Sets the last departure time (without error checking) and flags the value has been set.
   * 
   * @param lastDepTime The (new) last departure time (unchecked).
   * 
   * @see #resetHandler
   * @see #getLastDepTime
   * @see #isLastDepTimeSet
   * 
   */
  public final void setLastDepTime (final double lastDepTime)
  {
    this.lastDepTime = lastDepTime;
    this.lastDepTimeSet = true;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // IS RATE LIMITED
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean isRateLimited = false;
  
  /** Returns whether the (arrival/departure) rate is currently limited.
   * 
   * @return Whether the (arrival/departure) rate is currently limited.
   * 
   * @see #setRateLimited
   * 
   */
  public final boolean isRateLimited ()
  {
    return this.isRateLimited;
  }
  
  /** Sets whether the (arrival/departure) rate is currently limited.
   * 
   * @param isRateLimited The new value whether the (arrival/departure) rate is currently limited.
   * 
   * @see #isRateLimited
   * 
   */
  public final void setRateLimited (final boolean isRateLimited)
  {
    this.isRateLimited = isRateLimited;
  }
  
}
