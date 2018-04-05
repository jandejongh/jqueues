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

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A concrete class for flexible statistics gathering on a {@link SimQueue}.
 *
 * The statistics observed are under full user control by supplying a list of {@link AutoSimQueueStatEntry}s upon construction.
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
public class AutoSimQueueStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
{
  
  private List<AutoSimQueueStatEntry<Q>> entries;
  
  public final List<AutoSimQueueStatEntry<Q>> getEntries ()
  {
    return this.entries;
  }
  
  protected final void setEntries (final List<AutoSimQueueStatEntry<Q>> entries)
  {
    this.entries = entries;
    reset ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF WE NEED TO OVERRIDE.
  //
  
  /** Resets all the statistics.
   * 
   */
  @Override
  protected final void resetStatistics ()
  {
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.reset ();
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   */
  @Override
  protected final void updateStatistics (double time, double dt)
  {
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.update (getQueue (), dt);
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
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.calculate (startTime, endTime);
  }
  
  //
  // END: STUFF WE NEED TO OVERRIDE.
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
   * @param queue   The queue to gather statistics from.
   * @param entries The list of statistics to monitor on the queue.
   * 
   */
  public AutoSimQueueStat (final Q queue, final List<AutoSimQueueStatEntry<Q>> entries)
  {
    super (queue);
    this.entries = entries;
    resetStatistics ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>; the probe entries are empty.
   * 
   */
  public AutoSimQueueStat ()
  {
    super ();
    this.entries = new ArrayList<> ();
    resetStatistics ();
  }
  
  //
  // END: CONSTRUCTORS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



  public void report ()
  {
    report (0);
  }
  
  public void report (final int indent)
  {
    calculate ();
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
        {
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Average " + e.getName () + ": " + e.getAvgValue () + ".");
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Minimum " + e.getName () + ": " + e.getMinValue () + ".");
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Maximum " + e.getName () + ": " + e.getMaxValue () + ".");
        }    
  }
  
}
