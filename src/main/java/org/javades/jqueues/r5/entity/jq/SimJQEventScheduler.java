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
package org.javades.jqueues.r5.entity.jq;

import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntityEventScheduler;
import org.javades.jqueues.r5.entity.jq.SimJQEvent.Arrival;
import org.javades.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEventList;

/** A utility class capable of scheduling {@link SimJQEvent}s on an event list.
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
public abstract class SimJQEventScheduler
extends SimEntityEventScheduler
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
  protected SimJQEventScheduler ()
  {
    throw new UnsupportedOperationException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UTILITY METHODS FOR SCHEDULING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Schedules all {@link SimJQEvent}s on the given {@link SimEventList}, optionally after resetting it to a specific time.
   * 
   * @param eventList   The event list.
   * @param reset       Whether to reset the event list before scheduling.
   * @param resetTime   The new time to which to reset the event list (if requested so). 
   * @param queueEvents The {@link SimJQEvent}s to schedule.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @throws IllegalArgumentException If <code>eventList == null</code> or the (non-<code>null</code>) set of events
   *                                  has at least one <code>null</code> entry,
   *                                  or if any of the events is to be scheduled in the past after the optional event-list reset
   *                                  (compared to the time on the event list),
   * 
   * @see #scheduleJQ(SimEventList, SimJQEvent) 
   * 
   */
  public static <J extends SimJob, Q extends SimQueue> void scheduleJQ
  (final SimEventList eventList, final boolean reset, final double resetTime, final Set<SimJQEvent<J, Q>> queueEvents)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    if (queueEvents != null && queueEvents.contains (null))
      throw new IllegalArgumentException ();
    if (reset)
      eventList.reset (resetTime);
    if (queueEvents != null)
      for (final SimJQEvent<J, Q> event : queueEvents)
        SimJQEventScheduler.scheduleJQ (eventList, event);
  }

  /** Schedules a single job and/or queue event on a given event list.
   * 
   * @param eventList  The event list, non-{@code null}.
   * @param queueEvent The queue event, non-{@code null}.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @throws IllegalArgumentException If the event list or event is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or if the job is attached to a different event list than the queue.
   * 
   * @see #scheduleJQ(SimEventList, boolean, double, java.util.Set)
   * 
   */
  public static <J extends SimJob, Q extends SimQueue> void scheduleJQ
  (final SimEventList eventList, final SimJQEvent<J, Q> queueEvent)
  {
    if (eventList == null || queueEvent == null)
      throw new IllegalArgumentException ();
    final double eventListTime = eventList.getTime ();
    final double eventTime = queueEvent.getTime ();
    if (eventTime < eventListTime)
      throw new IllegalArgumentException ();
    final J job = queueEvent.getJob ();
    if (job != null && job.getEventList () != null && job.getEventList () != eventList)
      throw new IllegalArgumentException ();
    final Q queue = queueEvent.getQueue ();
    if (queue != null && queue.getEventList () != null && queue.getEventList () != eventList)
      throw new IllegalArgumentException ();
    eventList.add (queueEvent);
  }
  
  /** Creates a (default) job-arrival event and schedules it.
   * 
   * @param job         The job that arrives.
   * @param queue       The queue at which the job arrives.
   * @param arrivalTime The scheduled arrival time.
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or if the job is attached to a different event list than the queue.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see Arrival
   * @see SimQueue#arrive
   * @see #scheduleJQ(SimEventList, SimJQEvent) 
   * 
   */
  public static <J extends SimJob, Q extends SimQueue> void scheduleJobArrival
  (final J job, final Q queue, final double arrivalTime)
  {
    if (job == null || queue == null || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    SimJQEventScheduler.scheduleJQ (queue.getEventList (), new SimJQEvent.Arrival<> (job, queue, arrivalTime));
  }
    
  /** Creates a (default) job-revocation event and schedules it.
   * 
   * @param job             The job that is to be revoked.
   * @param queue           The queue at which the job is to be revoked from.
   * @param revocationTime  The scheduled revocation time.
   * @param interruptService Whether to request interruption of service (if applicable).
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or if the job is attached to a different event list than the queue.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see Revocation
   * @see SimQueue#revoke
   * @see #scheduleJQ(SimEventList, SimJQEvent) 
   * 
   */
  public static <J extends SimJob, Q extends SimQueue> void scheduleJobRevocation
  (final J job, final Q queue, final double revocationTime, final boolean interruptService)
  {
    if (job == null || queue == null || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    SimJQEventScheduler.scheduleJQ (queue.getEventList (),
      new SimJQEvent.Revocation<> (job, queue, revocationTime, interruptService));
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
