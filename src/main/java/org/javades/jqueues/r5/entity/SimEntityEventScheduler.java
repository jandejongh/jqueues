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
package org.javades.jqueues.r5.entity;

import java.util.Set;
import org.javades.jsimulation.r5.SimEventList;

/** A utility class capable of scheduling {@link SimEntityEvent}s on an event list.
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
public abstract class SimEntityEventScheduler
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
  protected SimEntityEventScheduler ()
  {
    throw new UnsupportedOperationException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UTILITY METHODS FOR SCHEDULING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Schedules all {@link SimEntityEvent}s on the given {@link SimEventList}, optionally after resetting it to a specific time.
   * 
   * @param eventList    The event list.
   * @param reset        Whether to reset the event list before scheduling.
   * @param resetTime    The new time to which to reset the event list (if requested so). 
   * @param entityEvents The {@link SimEntityEvent}s to schedule.
   * 
   * @throws IllegalArgumentException If <code>eventList == null</code> or the (non-<code>null</code>) set of events
   *                                  has at least one <code>null</code> entry,
   *                                  or if any of the events is to be scheduled in the past after the optional event-list reset
   *                                  (compared to the time on the event list),
   * 
   * @see #scheduleE(SimEventList, SimEntityEvent) 
   * 
   */
  public static void scheduleE
  (final SimEventList eventList, final boolean reset, final double resetTime, final Set<SimEntityEvent> entityEvents)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    if (entityEvents != null && entityEvents.contains (null))
      throw new IllegalArgumentException ();
    if (reset)
      eventList.reset (resetTime);
    if (entityEvents != null)
      for (final SimEntityEvent event : entityEvents)
        SimEntityEventScheduler.scheduleE (eventList, event);
  }

  /** Schedules a single entity event on a given event list.
   * 
   * @param eventList   The event list, non-{@code null}.
   * @param entityEvent The entity event, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the event list or event is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list).
   * 
   * @see #scheduleE(SimEventList, boolean, double, java.util.Set) 
   * 
   */
  public static void scheduleE
  (final SimEventList eventList, final SimEntityEvent entityEvent)
  {
    if (eventList == null || entityEvent == null)
      throw new IllegalArgumentException ();
    final double eventListTime = eventList.getTime ();
    final double eventTime = entityEvent.getTime ();
    if (eventTime < eventListTime)
      throw new IllegalArgumentException ();
    eventList.add (entityEvent);
  }
  
  /** Creates a default reset (entity) event and schedules it.
   * 
   * <p>
   * The event resets the <i>entity</i>, not the event list!
   * 
   * @param eventList The event list, non-{@code null}.
   * @param entity    The entity to reset, non-{@code null}.
   * @param resetTime The scheduled reset (entity) time.
   * 
   * @throws IllegalArgumentException If the event list or entity is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if the entity has a non-{@code null} event list different from the argument.
   * 
   * @see SimEntityEvent.Reset
   * @see SimEntity#resetEntity
   * @see #scheduleE(SimEventList, SimEntityEvent) 
   * 
   */
  public static void scheduleResetEntity
  (final SimEventList eventList, final SimEntity entity, final double resetTime)
  {
    if (eventList == null || entity == null || eventList.getTime () > resetTime
    || (entity.getEventList () != null && entity.getEventList () != eventList))
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.scheduleE (eventList, new SimEntityEvent.Reset (entity, resetTime));
  }
    
  /** Creates a default update event and schedules it.
   * 
   * @param eventList  The event list, non-{@code null}.
   * @param entity     The entity to update, non-{@code null}.
   * @param updateTime The scheduled update time.
   * 
   * @throws IllegalArgumentException If the event list or entity is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if the entity has a non-{@code null} event list different from the argument.
   * 
   * @see SimEntityEvent.Update
   * @see SimEntity#update
   * @see #scheduleE(SimEventList, SimEntityEvent) 
   * 
   */
  public static void scheduleUpdate
  (final SimEventList eventList, final SimEntity entity, final double updateTime)
  {
    if (eventList == null || entity == null || eventList.getTime () > updateTime
    || (entity.getEventList () != null && entity.getEventList () != eventList))
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.scheduleE (eventList, new SimEntityEvent.Update (entity, updateTime));
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
