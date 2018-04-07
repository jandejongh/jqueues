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

import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQEventScheduler;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.QueueAccessVacation;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.ServerAccessCredits;
import org.javades.jsimulation.r5.SimEventList;

/** A utility class capable of scheduling {@link SimQueueEvent}s on an event list.
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
public abstract class SimQueueEventScheduler
extends SimJQEventScheduler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inhibits instantiation (somewhat) yet allows extensions.
   * 
   * @throws UnsupportedOperationException Always.
   * 
   */
  protected SimQueueEventScheduler ()
  {
    throw new UnsupportedOperationException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UTILITY METHODS FOR SCHEDULING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a (default) queue-access vacation event and schedules it.
   * 
   * @param queue    The queue at which to start or end a queue-access vacation.
   * @param time     The time at which to start or end a queue-access vacation.
   * @param vacation Whether a queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see QueueAccessVacation
   * @see SimQueue#setQueueAccessVacation
   * @see SimJQEventScheduler#scheduleJQ(SimEventList, SimJQEvent) 
   * 
   */
  public static <J extends SimJob, Q extends SimQueue> void scheduleQueueAccessVacation
  (final Q queue, final double time, final boolean vacation)
  {
    if (queue == null || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    SimJQEventScheduler.scheduleJQ (queue.getEventList (),
      new SimQueueEvent.QueueAccessVacation<> (queue, time, vacation));
  }
    
  /** Creates a (default) a server-access-credits event and schedules it.
   * 
   * @param queue   The queue at which to set server-access credits.
   * @param time    The time at which to set server-access credits.
   * @param credits The number of credits to grant.
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or the number of credits is strictly negative.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see ServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimJQEventScheduler#scheduleJQ(SimEventList, SimJQEvent) 
   * 
   */
  public static <J extends SimJob, Q extends SimQueue> void scheduleServerAccessCredits
  (final Q queue, final double time, final int credits)
  {
    if (queue == null || queue.getEventList () == null || credits < 0)
      throw new IllegalArgumentException ();
    SimJQEventScheduler.scheduleJQ (queue.getEventList (),
      new SimQueueEvent.ServerAccessCredits<> (queue, time, credits));
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
