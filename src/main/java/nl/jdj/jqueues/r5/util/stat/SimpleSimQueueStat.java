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
package nl.jdj.jqueues.r5.util.stat;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A concrete class for gathering the most basic queue-related statistics on a {@link SimQueue}.
 *
 * <p>
 * We call a performance measure <i>queue</i>-related if its value depends only on the state of the queue.
 * By definition of a queue's state, a queue-related performance measure is a so-called <i>simple</i> function, i.e.,
 * in this context, a function yielding a constant value during non-trivial intervals between queue updates,
 * and integration is easily achieved by maintaining the current value of the performance measure and the time of the
 * last update. Note that the actual value of the function at the "switch times" is irrelevant.
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
public class SimpleSimQueueStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
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
  public SimpleSimQueueStat (final Q queue)
  {
    super (queue);
    resetStatisticsInt ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>.
   * 
   */
  public SimpleSimQueueStat ()
  {
    super ();
    resetStatisticsInt ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN THIS CLASS.
  //
  
  // Our actual statistics, with corresponding (calculated) average.
  private int nrOfUpdates = 0;
  private double cumNrOfJobs  = 0, avgNrOfJobs  = 0,
                 minNrOfJobs = Double.NaN, maxNrOfJobs = Double.NaN; // Number of jobs residing at queue.
  private double cumNrOfJobsX = 0, avgNrOfJobsX = 0,
                 minNrOfJobsX = Double.NaN, maxNrOfJobsX = Double.NaN; // Number of jobs in service area at queue.
  
  public final int getNumberOfUpdates ()
  {
    return this.nrOfUpdates;
  }
  
  /** Resets all the statistics.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   */
  private void resetStatisticsInt ()
  {
    this.nrOfUpdates = 0;
    this.cumNrOfJobs  = 0; this.avgNrOfJobs  = 0; this.minNrOfJobs  = Double.NaN; this.maxNrOfJobs  = Double.NaN;
    this.cumNrOfJobsX = 0; this.avgNrOfJobsX = 0; this.minNrOfJobsX = Double.NaN; this.maxNrOfJobsX = Double.NaN;
    // Add others here...
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param time The actual (new) time.
   * @param dt The time since the last update.
   * 
   */
  private void updateStatisticsInt (double time, double dt)
  {
    final SimQueue queue = getQueue ();
    if (queue == null)
      return;
    final int J  = queue.getNumberOfJobs ();
    final int JX = queue.getNumberOfJobsInServiceArea ();
    this.cumNrOfJobs  += J  * dt;
    this.cumNrOfJobsX += JX * dt;
    if (this.nrOfUpdates == 0)
    {
      this.minNrOfJobs  = J;
      this.maxNrOfJobs  = J;
      this.minNrOfJobsX = JX;
      this.maxNrOfJobsX = JX;
    }
    else
    {
      this.minNrOfJobs  = Math.min (this.minNrOfJobs,  J);
      this.maxNrOfJobs  = Math.max (this.maxNrOfJobs,  J);
      this.minNrOfJobsX = Math.min (this.minNrOfJobsX, JX);
      this.maxNrOfJobsX = Math.max (this.maxNrOfJobsX, JX);
    }
    this.nrOfUpdates++;
    // Add others here...
  }
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * 
   * @see #calculate
   * 
   */
  private void calculateStatisticsInt (final double startTime, final double endTime)
  {
    if (endTime < startTime)
      throw new IllegalArgumentException ();
    else if (startTime == endTime)
    {
      this.avgNrOfJobs  = 0.0;
      this.avgNrOfJobsX = 0.0;
      // Add others here...
    }
    else
    {
      final double dT = endTime - startTime;
      this.avgNrOfJobs  = this.cumNrOfJobs  / dT;
      this.avgNrOfJobsX = this.cumNrOfJobsX / dT;
      // Add others here...
    }
  }

  // Add getters for your favorite performance measures below...
  
  /** Returns the average number of jobs residing at the queue.
   * 
   * @return The average number of jobs residing at the queue.
   * 
   */
  public final double getAvgNrOfJobs ()
  {
    calculate ();
    return this.avgNrOfJobs;
  }

  /** Returns the minimum number of jobs residing at the queue.
   * 
   * @return The minimum number of jobs residing at the queue.
   * 
   */
  public final double getMinNrOfJobs ()
  {
    return this.minNrOfJobs;
  }

  /** Returns the maximum number of jobs residing at the queue.
   * 
   * @return The maximum number of jobs residing at the queue.
   * 
   */
  public final double getMaxNrOfJobs ()
  {
    return this.maxNrOfJobs;
  }

  /** Returns the average number of jobs in the service area at the queue.
   * 
   * @return The average number of jobs in the service area at the queue.
   * 
   */
  public final double getAvgNrOfJobsInServiceArea ()
  {
    calculate ();
    return this.avgNrOfJobsX;
  }

  /** Returns the minimum number of jobs in the service area at the queue.
   * 
   * @return The minimum number of jobs in the service area at the queue.
   * 
   */
  public final double getMinNrOfJobsInServiceArea ()
  {
    return this.minNrOfJobsX;
  }

  /** Returns the maximum number of jobs in the service area at the queue.
   * 
   * @return The maximum number of jobs in the service area at the queue.
   * 
   */
  public final double getMaxNrOfJobsInServiceArea ()
  {
    return this.maxNrOfJobsX;
  }

  //
  // END: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN THIS CLASS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO OVERRIDE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN A SUBCLASS.
  //
  
  /** Resets all the statistics.
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * Unfortunately, you must reset the statistics yourself upon construction, because this class avoids the trickery of
   * calling overridable methods from its constructors.
   * 
   */
  @Override
  protected void resetStatistics ()
  {
    resetStatisticsInt ();
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void updateStatistics (double time, double dt)
  {
    updateStatisticsInt (time, dt);
  }
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void calculateStatistics (final double startTime, final double endTime)
  {
    calculateStatisticsInt (startTime, endTime);
  }
  
  //
  // END: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN A SUBCLASS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
}
