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
package org.javades.jqueues.r5.util.stat;

import java.util.List;
import java.util.Map;
import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.SimEntityEvent;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueListener;

/** An abstract base class for automatically gathering statistics on a {@link SimQueue}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimQueueListener
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
public abstract class AbstractSimQueueStat<J extends SimJob, Q extends SimQueue>
implements SimQueueListener<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Constructor.
   * 
   * @param queue The queue to gather statistics from.
   * 
   */
  public AbstractSimQueueStat (final Q queue)
  {
    setQueueInt (queue);
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>.
   * 
   */
  public AbstractSimQueueStat ()
  {
    this (null);
  }
  
  // The queue we are gathering statistics on, may be {@code null}.
  private Q queue = null;
  
  // The start time for gathering statistics, and the last update time.
  // Note that always startTime <= lastUpdateTime.
  // Updates before the start time are silently ignored.
  private double startTime = Double.NEGATIVE_INFINITY;
  private double lastUpdateTime = Double.NEGATIVE_INFINITY;
  
  // Whether our statistics are valid for use, or need to be calculated first.
  private boolean statisticsValid = false;
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO OVERRIDE IN A SUBCLASS.
  //
  
  /** Resets all the statistics.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * Unfortunately, you must reset the statistics yourself upon construction, because this class avoids the trickery of
   * calling override-able methods from its constructors.
   * 
   */
  protected abstract void resetStatistics ();
  
  /** Updates all the statistics from the state of the queue.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param time The actual (new) time.
   * @param dt The time since the last update.
   * 
   */
  protected abstract void updateStatistics (double time, double dt);
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param startTime The start time of the statistics gathering.
   * @param endTime   The last time the statistic was updated.
   * 
   */
  protected abstract void calculateStatistics (double startTime, double endTime);
  
  //
  // END: STUFF YOU NEED TO OVERRIDE IN A SUBCLASS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: MAIN ENTRY POINTS FOR STATISTICS MANAGEMENT: RESET/UPDATE/CALCULATE.
  //

  /** Resets all statistics and start a new batch of statistics gathering.
   * 
   * <p>
   * The start time is set to the current time (the time on the event list of the queue, or minus infinity if no queue is present),
   * all statistics are invalidated and prepared for the new batch.
   * 
   * <p>
   * Only override this method with a call to <code>super</code> if you have special actions to be performed upon a reset
   * <i>other</i> than resetting statistics. Otherwise, override {@link #resetStatistics}.
   * 
   * @see #getLastUpdateTime
   * @see #resetStatistics
   * @see #getStatisticsValid
   * 
   */
  public void reset ()
  {
    resetInt ();
    resetStatistics ();
  }
  
  // Auxiliary (final) method for private use in constructor.
  private void resetInt ()
  {
    if (this.queue != null)
      this.startTime = this.queue.getEventList ().getTime ();
    else
      this.startTime = Double.NEGATIVE_INFINITY;    
    this.lastUpdateTime = this.startTime;
    this.statisticsValid = false;
  }
  
  /** Updates all statistics at given time.
   * 
   * Note that updates timed before our latest update yield an exception.
   * 
   * <p>
   * Only override this method with a call to <code>super</code> if you have special actions to be performed upon an update
   * <i>other</i> than updating statistics. Otherwise, override {@link #updateStatistics}.
   * 
   * @param time The (new) current time.
   * 
   * @throws IllegalArgumentException If the time is in the past (i.e., strictly smaller than our last update time).
   * 
   */
  protected void update (double time)
  {
    if (time < this.lastUpdateTime)
      throw new IllegalArgumentException ();
    if (time == this.lastUpdateTime)
      return;
    this.statisticsValid = false;
    final double dt = time - this.lastUpdateTime;
    updateStatistics (time, dt);
    this.lastUpdateTime = time;
  }

  /** Calculates all statistics (if invalid) and marks them valid.
   * 
   * This method is automatically invoked by all statistics getters.
   * 
   * <p>
   * Only override this method with a call to <code>super</code> if you have special actions to be performed upon a calculation
   * <i>other</i> than calculating statistics. Otherwise, override {@link #calculateStatistics}.
   * 
   * @param time The current time, may be beyond the last update time.
   * 
   */
  protected void calculate (final double time)
  {
    if (this.statisticsValid && time == this.lastUpdateTime)
      return;
    this.statisticsValid = false;
    if (time < this.lastUpdateTime)
      throw new IllegalArgumentException ();
    if (this.lastUpdateTime < this.startTime)
      throw new RuntimeException ();
    if (time > this.lastUpdateTime)
      update (time);
    calculateStatistics (this.startTime, this.lastUpdateTime);
    this.statisticsValid = true;
  }

  protected void calculate ()
  {
    final SimQueue queue = getQueue ();
    if (queue != null)
      calculate (queue.getEventList ().getTime ());
    else
      calculate (getLastUpdateTime ());    
  }
  
  //
  // END: MAIN ENTRY POINTS FOR STATISTICS MANAGEMENT: RESET/UPDATE/CALCULATE.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CRITICAL INTERNAL BOOKKEEPING: QUEUE, START TIME, LAST UPDATE TIME, STATISTICS VALIDITY.
  //
  
  /** Gets the {@link SimQueue} we are gathering statistics on.
   * 
   * @return The {@link SimQueue} we are gathering statistics on.
   * 
   * @see #setQueue
   * 
   */
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  /** Sets the {@link SimQueue} we are gathering statistics on.
   * 
   * This methods always invokes {@link #reset}.
   * 
   * <p>
   * Override this method is discouraged, even with a call to <code>super</code>.
   * Also, an internal version of this method is used upon object construction.
   * 
   * @param queue The new queue to monitor (may be <code>null</code>).
   * 
   * @see #getQueue
   * @see SimQueue#registerSimEntityListener
   * @see SimQueue#unregisterSimEntityListener
   * @see #reset
   * 
   */
  public void setQueue (Q queue)
  {
    setQueueCommon (queue);
    reset ();
  }
  
  // Auxiliary (final) method for private use in constructor.
  private void setQueueInt (Q queue)
  {
    setQueueCommon (queue);
    resetInt ();
  }
  
  // Common code between setQueue and setQueueInt
  private void setQueueCommon (Q queue)
  {
    if (queue != this.queue)
    {
      if (this.queue != null)
      {
        this.queue.unregisterSimEntityListener (this);
      }
      this.queue = queue;
      if (this.queue != null)
      {
        this.queue.registerSimEntityListener (this);
      }
    }    
  }
  
  /** Gets the start time for gathering statistics.
   * 
   * @return The start time for gathering statistics.
   * 
   */
  public final double getStartTime ()
  {
    return this.startTime;
  }
  
  /** Returns the time of the last update.
   * 
   * @return The time of the last update.
   * 
   * @see #update(double)
   * @see #notifyUpdate(double, SimEntity) 
   * 
   */
  public final double getLastUpdateTime ()
  {
    return this.lastUpdateTime;
  }
  
  /** Checks if the statistics are valid.
   * 
   * @return True if the statistics are valid.
   * 
   * @see #calculate
   * 
   */
  public final boolean getStatisticsValid ()
  {
    return this.statisticsValid;
  }
  
  //
  // END: CRITICAL INTERNAL BOOKKEEPING: QUEUE, START TIME, LAST UPDATE TIME, STATISTICS VALIDITY.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CONNECTION WITH THE QUEUE / IMPLEMENTATION OF SimQueueListener.
  //
  
  /** Invokes {@link #reset}.
   *
   */
  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    reset ();
  }
  
  /** Checks the queue argument and invokes {@link #update(double)}.
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code> or not the queue we monitor.
   * 
   * @see #getQueue
   * 
   */
  @Override
  public void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || entity != getQueue ())
      throw new IllegalArgumentException ();
    update (time);
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyAutoRevocation (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
  }
  
  /** Does nothing.
   * 
   */
  @Override
  public void notifyNewStartArmed (final double time, final Q queue, final boolean startArmed)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
  }

  //
  // END: CONNECTION WITH THE QUEUE / IMPLEMENTATION OF SimQueueListener.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CONSTRUCTORS.
  //
  
  //
  // END: CONSTRUCTORS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  

}
