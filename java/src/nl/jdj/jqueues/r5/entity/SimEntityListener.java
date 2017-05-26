package nl.jdj.jqueues.r5.entity;

import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType.Member;
import nl.jdj.jqueues.r5.util.stat.AbstractSimQueueStat;

/** A listener to one or multiple {@link SimEntity}s.
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
public interface SimEntityListener
{
  
  /** Notification of a reset at a {@link SimEntity}.
   * 
   * <p>
   * Convenience method as resets are also notified through {@link #notifyStateChanged}.
   * 
   * @param entity The entity that has been reset.
   * 
   */
  public void notifyResetEntity (SimEntity entity);
  
  /** Notification of an immediate upcoming update at an entity.
   * 
   * <p>
   * An update at an entity is defined as a moment in time at which the entity is about to change its state.
   * The main advantage of an update notification is that you can inspect the entity right before it is about to change.
   * This leads the way to (efficiently) maintaining time-based statistics on the entity.
   * 
   * <p>
   * The general contract is that in between entity notifications, the entity does not change its state.
   * However, what exactly comprises the state of the entity has to be documented by concrete implementations.
   * 
   * <p>
   * Update notifications through {@link #notifyUpdate}
   * and state-change notifications through {@link #notifyStateChanged} are dual in the sense that
   * the former are issued <i>before</i> and the latter <i>after</i>
   * a state change.
   * 
   * <p>
   * Preferably, update and state-change notifications for a single entity must always alternate.
   * However, the only requirement in that sense is that a state-change notification
   * <i>must</i> be immediately preceded with an update notification with equal time.
   * It is therefore allowed to issue multiple update-notifications in between
   * two consecutive state-change notifications,
   * as long as the last update notification has the same time-stamp as the the second
   * state-change notification.
   * 
   * <p>
   * Both types of notifications should <i>not</i> be sent upon construction of the entity;
   * there are no {@link SimEntityListener}s at this stage anyway.
   * 
   * <p>
   * However, upon an explicit reset of a {@link SimEntity},
   * it is recommended to issue a {@link #notifyUpdate} <i>before</i> applying the reset.
   * 
   * <p>
   * Listeners must never directly or indirectly cause state changes on the reporting entity.
   * See {@link SimEntity#doAfterNotifications} for a workaround.
   * 
   * <p>
   * Beware that updates are <i>never</i> reported as state-change event.
   * 
   * @param time   The time of the update.
   * @param entity The entity that is about to be updated.
   * 
   * @see #notifyStateChanged
   * @see AbstractSimQueueStat
   * 
   */
  public void notifyUpdate (double time, SimEntity entity);
  
  /** Notification of a (any) state change of a {@link SimEntity}.
   * 
   * <p>
   * All state changes of a {@link SimEntity} <i>must</i> be reported through this method.
   * 
   * <p>
   * The notification is passed as a list of ({@link Member}, {@link SimEntityEvent}) <i>tuples</i>,
   * the <i>sub-notifications</i>.
   * Note that the maps must have exactly one non-{@code null} key with non-{@code null} value.
   * All sub-notifications must have time set identical to the time passed as argument.
   * These sub-notifications form the sequence of (sub-)events describing the atomic state-change as a whole.
   * 
   * <p>
   * Beware that updates are <i>never</i> reported as state-change event.
   * 
   * @param time          The current time (the time of the state change).
   * @param entity        The entity at which the state changed.
   * @param notifications The sequence of notifications of state-changes the combination of which led to the new state.
   * 
   * @see #notifyUpdate
   * 
   */
  public void notifyStateChanged
  (double time, SimEntity entity, List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications);
    
}
