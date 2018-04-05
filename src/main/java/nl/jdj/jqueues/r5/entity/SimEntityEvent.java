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
package nl.jdj.jqueues.r5.entity;

import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** A {@link SimEvent} for a {@link SimEntity} (queue, job, or other) operation.
 * 
 * <p>
 * This class only administers the key parameters for the event; it does not actually schedule it.
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
public abstract class SimEntityEvent
extends DefaultSimEvent
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new event for an entity.
   * 
   * @param name   The (optional) name of the event, may be  {@code null}.
   * @param time   The time at which the event occurs.
   * @param entity The entity related to the event, non-{@code null}.
   * @param action The {@link SimEventAction} to take; may be {@code null}.
   * 
   * @throws IllegalArgumentException If {@code entity == null}.
   */
  protected SimEntityEvent
  (final String name,
   final double time,
   final SimEntity entity,
   final SimEventAction<? extends SimEntity> action)
  {
    super (name, time, null, action);
    if (entity == null)
      throw new IllegalArgumentException ();
    this.entity = entity;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final SimEntity entity;
  
  /** Gets the entity at which the event occurs.
   * 
   * @return The entity to which the event applies, may be {@code null}.
   * 
   */
  public final SimEntity getEntity ()
  {
    return this.entity;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The (default) {@link SimEvent} for resetting a {@link SimEntity}.
   * 
   */
  public final static class Reset
  extends SimEntityEvent
  {
 
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
    private static SimEventAction createAction (final SimEntity entity)
    {
      if (entity == null)
        throw new IllegalArgumentException ();
      return (final SimEvent event) -> entity.resetEntity ();
    }
  
    /** Creates a reset event at a specific entity.
     *   
     * @param entity The entity, non-{@code null}.
     * @param time   The time at which to reset the entity.
     * 
     * @throws IllegalArgumentException If <code>entity == null</code>.
     * 
     * @see SimEntity#resetEntity
     * 
     */
    public Reset
    (final SimEntity entity, final double time)
    {
      super ("Reset@" + entity, time, entity, createAction (entity));
    }
  
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The (default) {@link SimEvent} for updating a {@link SimEntity}.
   * 
   */
  public final static class Update
  extends SimEntityEvent
  {
 
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
    private static SimEventAction createAction (final SimEntity entity, final double time)
    {
      if (entity == null)
        throw new IllegalArgumentException ();
      return (final SimEvent event) -> entity.update (time);
    }
  
    /** Creates an update event at a specific entity.
     *   
     * @param entity The entity, non-{@code null}.
     * @param time   The time at which to update the entity.
     * 
     * @throws IllegalArgumentException If <code>entity == null</code>.
     * 
     * @see SimEntity#update
     * 
     */
    public Update
    (final SimEntity entity, final double time)
    {
      super ("Update@" + entity, time, entity, createAction (entity, time));
    }
  
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
