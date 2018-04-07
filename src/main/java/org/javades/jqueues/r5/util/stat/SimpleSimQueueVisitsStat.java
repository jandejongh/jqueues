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

import java.util.HashMap;
import java.util.Map;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** An concrete class for gathering the most basic visits-related statistics on a {@link SimQueue}.
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
public class SimpleSimQueueVisitsStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
{
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN THIS CLASS.
  //
  
  // Our actual statistics, with corresponding (calculated) average.
  
  private int Narrivals;        // Number of arrivals.
  private int Ndrops;           // Number of jobs dropped.
  private int Nrevocations;     // Number of jobs revoked (not auto-revoked).
  private int NautoRevocations; // Number of jobs auto-revoked.
  private int Nstarted;         // Number of jobs started.
  private int Ndepartures;      // Number of departures.
  private int Nexits;           // Number of jobs that left the queue.
  
  private double cumWaitingTime = 0; // Cumulative waiting time (over jobs started).
  private double cumSojournTime = 0; // Cumulative sojourn time (over departures).
  
  private double avgWaitingTime = Double.NaN; // Average waiting time, when calculated (over jobs started).
  private double avgSojournTime = Double.NaN; // Average sojourn time, when calculated (over jobs departed).
  
  private double minWaitingTime = Double.NaN; // Minimum waiting time (over jobs started).
  private double minSojournTime = Double.NaN; // Minimum sojourn time (over jobs departed).
  
  private double maxWaitingTime = Double.NaN; // Maximum waiting time (over jobs started).
  private double maxSojournTime = Double.NaN; // Maximum sojourn time (over jobs departed).
  
  private final Map<J, Double> arrivals = new HashMap<> ();
  private final Map<J, Double> started  = new HashMap<> ();
  
  /** Resets all the statistics.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   */
  private void resetStatisticsInt ()
  {
    this.Narrivals = 0;
    this.Ndrops = 0;
    this.Nrevocations = 0;
    this.NautoRevocations = 0;
    this.Nstarted = 0;
    this.Ndepartures = 0;
    this.Nexits = 0;
    this.cumWaitingTime = 0;
    this.cumSojournTime = 0;
    this.avgWaitingTime = Double.NaN;
    this.avgSojournTime = Double.NaN;
    this.minWaitingTime = Double.NaN;
    this.minSojournTime = Double.NaN;
    this.maxWaitingTime = Double.NaN;
    this.maxSojournTime = Double.NaN;
    this.arrivals.clear ();
    this.started.clear ();
    // Add others here...
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * Nothing to do here...
   * 
   * @param time The actual (new) time.
   * @param dt The time since the last update.
   * 
   */
  private void updateStatisticsInt (final double time, final double dt)
  {
    // How to obtain the queue; may be null.
    // final SimQueue queue = getQueue ();
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
    if (this.Nstarted > 0)
      this.avgWaitingTime = this.cumWaitingTime / this.Nstarted;
    else
      this.avgWaitingTime = Double.NaN;
    if (this.Ndepartures > 0)
      this.avgSojournTime = this.cumSojournTime / this.Ndepartures;
    else
      this.avgSojournTime = Double.NaN;
  }
  
  // Add getters for your favorite performance measures below...
  
  /** Returns the number of arrivals.
   * 
   * @return The number of arrivals.
   * 
   */
  public final int getNumberOfArrivals ()
  {
    calculate ();
    return this.Narrivals;
  }
  
  /** Returns the number of dropped jobs.
   * 
   * @return The number of dropped jobs.
   * 
   */
  public final int getNumberOfJobsDropped ()
  {
    calculate ();
    return this.Ndrops;
  }
  
  /** Returns the number of revoked jobs (not auto-revoked).
   * 
   * @return The number of revoked jobs (not auto-revoked).
   * 
   */
  public final int getNumberOfRevocations ()
  {
    calculate ();
    return this.Nrevocations;
  }
  
  /** Returns the number of auto-revoked jobs.
   * 
   * @return The number of auto-revoked jobs.
   * 
   */
  public final int getNumberOfAutoRevocations ()
  {
    calculate ();
    return this.NautoRevocations;
  }
  
  /** Returns the number of started jobs.
   * 
   * @return The number of started jobs.
   * 
   */
  public final int getNumberOfStartedJobs ()
  {
    calculate ();
    return this.Nstarted;
  }
  
  /** Returns the number of departures.
   * 
   * @return The number of departures.
   * 
   */
  public final int getNumberOfDepartures ()
  {
    calculate ();
    return this.Nstarted;
  }
  
  /** Returns the number of job exits.
   * 
   * @return The number of job exits.
   * 
   */
  public final int getNumberOfExits ()
  {
    calculate ();
    return this.Nexits;
  }
  
  /** Returns the average waiting time at the queue.
   * 
   * <p>
   * The average is taken over all jobs that <i>started</i>.
   * 
   * @return The average waiting time at the queue; {@link Double#NaN} in case no job has started (yet).
   * 
   */
  public final double getAvgWaitingTime ()
  {
    calculate ();
    return this.avgWaitingTime;
  }

  /** Returns the average sojourn time at the queue.
   * 
   * <p>
   * The average is taken over all jobs that <i>departed</i>;
   * ignoring jobs that left the queue through other means.
   * 
   * @return The average sojourn time at the queue; {@link Double#NaN} in case no job has departed (yet).
   * 
   */
  public final double getAvgSojournTime ()
  {
    calculate ();
    return this.avgSojournTime;
  }

  /** Returns the minimum waiting time at the queue.
   * 
   * <p>
   * The minimum is taken over all jobs that <i>started</i>.
   * 
   * @return The minimum waiting time at the queue; {@link Double#NaN} in case no job has started (yet).
   * 
   */
  public final double getMinWaitingTime ()
  {
    return this.minWaitingTime;
  }

  /** Returns the minimum sojourn time at the queue.
   * 
   * <p>
   * The minimum is taken over all jobs that <i>departed</i>;
   * ignoring jobs that left the queue through other means.
   * 
   * @return The minimum sojourn time at the queue; {@link Double#NaN} in case no job has departed (yet).
   * 
   */
  public final double getMinSojournTime ()
  {
    return this.minSojournTime;
  }

  /** Returns the maximum waiting time at the queue.
   * 
   * <p>
   * The maximum is taken over all jobs that <i>started</i>.
   * 
   * @return The maximum waiting time at the queue; {@link Double#NaN} in case no job has started (yet).
   * 
   */
  public final double getMaxWaitingTime ()
  {
    return this.maxWaitingTime;
  }

  /** Returns the maximum sojourn time at the queue.
   * 
   * <p>
   * The maximum is taken over all jobs that <i>departed</i>;
   * ignoring jobs that left the queue through other means.
   * 
   * @return The maximum sojourn time at the queue; {@link Double#NaN} in case no job has departed (yet).
   * 
   */
  public final double getMaxSojournTime ()
  {
    return this.maxSojournTime;
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
  protected void updateStatistics (final double time, final double dt)
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
    calculateStatisticsInt (startTime,endTime);
  }
  
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
    super.notifyArrival (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || this.arrivals.containsKey (job) || this.started.containsKey (job))
      throw new IllegalArgumentException ();
    this.arrivals.put (job, time);
    this.Narrivals++;
  }
    
  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
    super.notifyStart (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || (! this.arrivals.containsKey (job)) || this.started.containsKey (job))
      throw new IllegalArgumentException ();
    this.started.put (job, time);
    final double waitingTime = (this.started.get (job) - this.arrivals.get (job));
    this.cumWaitingTime += waitingTime;
    if (this.Nstarted == 0)
    {
      this.minWaitingTime = waitingTime;
      this.maxWaitingTime = waitingTime;
    }
    else
    {
      this.minWaitingTime = Math.min (this.minWaitingTime, waitingTime);
      this.maxWaitingTime = Math.max (this.maxWaitingTime, waitingTime);      
    }
    this.Nstarted++;
  }

  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
    super.notifyDrop (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || ! this.arrivals.containsKey (job))
      throw new IllegalArgumentException ();
    this.Ndrops++;
    this.Nexits++;
    this.arrivals.remove (job);
    this.started.remove (job);
  }

  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
    super.notifyRevocation (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || ! this.arrivals.containsKey (job))
      throw new IllegalArgumentException ();
    this.Nrevocations++;
    this.Nexits++;
    this.arrivals.remove (job);
    this.started.remove (job);
  }

  @Override
  public void notifyAutoRevocation (final double time, final J job, final Q queue)
  {
    super.notifyAutoRevocation (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || ! this.arrivals.containsKey (job))
      throw new IllegalArgumentException ();
    this.NautoRevocations++;
    this.Nexits++;
    this.arrivals.remove (job);
    this.started.remove (job);
  }

  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
    super.notifyDeparture (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || ! this.arrivals.containsKey (job))
      throw new IllegalArgumentException ();
    final double sojournTime = (time - this.arrivals.get (job));
    this.cumSojournTime += sojournTime;
    if (this.Ndepartures == 0)
    {
      this.minSojournTime = sojournTime;
      this.maxSojournTime = sojournTime;
    }
    else
    {
      this.minSojournTime = Math.min (this.minSojournTime, sojournTime);
      this.maxSojournTime = Math.max (this.maxSojournTime, sojournTime);      
    }
    this.Ndepartures++;
    this.Nexits++;
    this.arrivals.remove (job);
    this.started.remove (job);
  }

  //
  // END: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN A SUBCLASS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CONSTRUCTORS.
  //
  
  /** Constructor.
   * 
   * @param queue The queue to gather statistics from.
   * 
   */
  public SimpleSimQueueVisitsStat (final Q queue)
  {
    super (queue);
    resetStatisticsInt ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>.
   * 
   */
  public SimpleSimQueueVisitsStat ()
  {
    super ();
    resetStatisticsInt ();
  }
  
  //
  // END: CONSTRUCTORS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



}
