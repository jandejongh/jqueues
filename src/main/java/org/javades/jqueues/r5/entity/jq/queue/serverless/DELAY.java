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

/** The {@link DELAY} queue induces a fixed waiting delay, after which jobs depart without service.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see ZERO
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
public class DELAY<J extends SimJob, Q extends DELAY>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link DELAY} queue with infinite buffer size given a (fixed) waiting time and an event list,.
   *
   * @param eventList The event list to use.
   * @param waitTime  The (fixed) wait time for all jobs.
   *
   * @throws IllegalArgumentException If <code>waitTime</code> is strictly negative.
   * 
   */
  public DELAY (final SimEventList eventList, final double waitTime)
  {
    super (eventList, Integer.MAX_VALUE);
    if (waitTime < 0)
      throw new IllegalArgumentException ();
    this.waitTime = waitTime;
  }
  
  /** Returns a new {@link DELAY} object on the same {@link SimEventList} with the same wait time.
   * 
   * @return A new {@link DELAY} object on the same {@link SimEventList} with the same wait time.
   * 
   * @see #getEventList
   * @see #getWaitTime
   * 
   */
  @Override
  public DELAY<J, Q> getCopySimQueue ()
  {
    return new DELAY<> (getEventList (), getWaitTime ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "DELAY[wait time]".
   * 
   * @return "DELAY[wait time]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DELAY[" + getWaitTime () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WAIT TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double waitTime;
  
  /** Returns the wait time for all jobs.
   * 
   * @return The wait time for all jobs, non-negative.
   * 
   */
  public final double getWaitTime ()
  {
    return this.waitTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
  }

  /** If needed, schedules a departure event for the arrived job respecting the fixed wait time of this queue;
   *  otherwise (zero wait time), makes the job depart immediately.
   * 
   * <p>
   * If the wait time is {@link Double#POSITIVE_INFINITY}, no departure events are scheduled, in other words,
   * this queue does not schedule departures at infinity.
   * 
   * @see #getWaitTime
   * @see #scheduleDepartureEvent
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    final double waitTime = getWaitTime ();
    if (waitTime < 0)
      throw new IllegalStateException ();
    else if (waitTime == 0)
      depart (time, job);
    else if (! Double.isInfinite (waitTime))
      scheduleDepartureEvent (time + waitTime, job);
    else
      // waitTime is positive infinity; do not schedule a departure event!
      ;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Cancels the departure of the job (if present).
   * 
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    if (! getDepartureEvents (job).isEmpty ())
      cancelDepartureEvent (job);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
