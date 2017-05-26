package nl.jdj.jqueues.r5.entity;

import java.util.Set;
import java.util.logging.Level;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType.Member;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;
import nl.jdj.jsimulation.r5.SimEventListResetListener;

/** A simulation entity like {@link SimJob} or {@link SimQueue}.
 *
 * <p>
 * A {@link SimEntity} is an entity relevant to event-list scheduling in a queueing system simulation.
 * Usually, it is either a queue ({@link SimQueue}) or a job ({@link SimJob}).
 * However, the interface does not a priori assume this.
 * What {@link SimEntity}s have in common is the event list they are attached to,
 * the fact that they have a name, that they support operations,
 * and their obligation to propagate state changes
 * (notifications) to (registered) listeners.
 * 
 * <p>
 * Objects implementing this interface take care of (at the very least)
 * <ul>
 * <li>holding the underlying event list ({@link SimEventList}), which may be <code>null</code>,
 * <li>registering as a {@link SimEventListResetListener} to a non-<code>null</code> {@link SimEventList},
 *     obeying resets on the {@link SimEventList},
 *     and propagating reset events on the {@link SimEventList} to {@link SimEntityListener}s through {@link #resetEntity}
 *     (which, effectively, describes the {@code RESET} operation),
 * <li>naming, through {@link #setName} and {@link #toStringDefault},
 * <li>registration and maintenance of {@link SimEntityOperation}s, and supporting them through {@link #doOperation}
 *     with flexible error handling for unknown operations,
 * <li>registration, maintenance and error handling of (types of) notifications implemented as {@link Member},
 * <li>maintaining listeners ({@link SimEntityListener}) to notifications,
 * <li>notifying listeners upon <i>updates</i> and <i>state changes</i>,
 *     see {@link SimEntityListener#notifyUpdate} and {@link SimEntityListener#notifyStateChanged}, respectively,
 * <li>supporting (deferred) actions to take immediately after notifying listeners
 *     (supported because, as by contract of {@link SimEntity} and {@link SimEntityListener},
 *     listeners are not allowed to initiate state-changing operations on the entity),
 * <li>implementing updates and maintaining the time of the last update with {@link #getLastUpdateTime}
 *     (effectively, the UPDATE notification).
 * </ul>
 * 
 * <p>
 * Note that the contracts of
 * {@link SimEntityOperation},
 * {@link SimEntitySimpleEventType},
 * {@link SimEntityEvent}
 * and {@link SimEntityListener} are part of the formal interface of a {@link SimEntity}.
 * 
 * <p>
 * For an implementation starting point, see {@link AbstractSimEntity}.
 * 
 * @see SimEntityOperation
 * @see SimEntitySimpleEventType
 * @see SimEntityEvent
 * @see SimEntityListener
 * @see SimJob
 * @see SimQueue
 * @see AbstractSimEntity
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
public interface SimEntity
extends SimEventListResetListener
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT LIST
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the underlying event list of this {@link SimEntity}.
   * 
   * <p>
   * The event list is passed upon construction of the object, and cannot be changed afterwards.
   * It may be <code>null</code>.
   * 
   * <p>
   * If the event list is non-<code>null</code>, implementations must register at it as a {@link SimEventListResetListener},
   * and propagate any reset of the event list through {@link #resetEntity}.
   * 
   * @return The underlying event list of this {@link SimEntity}.
   * 
   */
  SimEventList getEventList ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a default name for this {@link SimEntity}.
   * 
   * <p>
   * The naming framework of an entity is as follows:
   * <ul>
   * <li>
   * The name of an entity never affects its functional behavior, nor that of other entities,
   * apart from generated output for diagnostics, presentation or debugging.
   * <li>
   * It is recommended to <i>not</i> make {@code Object.toString ()} implementations {@code final};
   * users should always have the option to give useful (to them) names to entities
   * by using the conventional approach of overriding {@code Object.toString ()}.
   * <li>
   * Implementations should use {@code Object.toString ()} for String representations of an entity
   * (following Java conventions).
   * <li>
   * It is <i>recommended</i> that default implementations
   * (see, for instance,{@link AbstractSimEntity#toString})
   * of {@code Object.toString} return
   * <ul>
   * <li>the String set by {@link #setName}, or, if not set,
   * <li>the String returned by {@link #toStringDefault} as set (and often made {@code final}
   *       by the implementation class(es), or, if not set,
   * <li>the String returned by {@link Object#toString}.
   * </ul>
   * <li>
   * This approach ensures that users of an entity can easily set the name of an individual entity
   * through {@link #setName} or by overriding (if still possible) {@link Object#toString}.
   * At the same time, if a user chooses to not care about naming, a type-specific (typically "short") meaningful
   * description String of the entity is used.
   * </ul>
   * 
   * <p>
   * In other words, the string is to be returned by (concrete) implementations of {@link #toStringDefault} acts
   * as a fallback return value for <code>Object.toString ()</code>
   * in case the user did not set an instance-specific name
   * through {@link #setName}.
   * 
   * <p>
   * Implementations are recommended to <i>not</i> make this method {@code final}
   * unless the class itself is final.
   * The recommendation same holds for {@code Object.toString ()}.
   * 
   * @return A default name for this {@link SimEntity}.
   * 
   * @see #setName
   * @see Object#toString
   * 
   */
  String toStringDefault ();
  
  /** Sets the name of this {@link SimEntity}, to be returned by subsequent calls to <code>Object.toString ()</code>.
   * 
   * @param name The new name of this job or queue; if non-<code>null</code>, the string will be returned by subsequent calls
   *               to <code>Object.toString ()</code> (recommended implementation!);
   *               otherwise, the type-specific default {@link #toStringDefault} will be used for that.
   * 
   * @see #toStringDefault
   * 
   */
  void setName (String name);
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the registered operations of this entity.
   * 
   * @return The registered operations at this entity (in registration-time order).
   * 
   * @see SimEntityOperation
   * 
   */
  Set<SimEntityOperation> getRegisteredOperations ();

  /** Possible courses of action when an unknown operation is offered to {@link #doOperation}.
   * 
   * @see SimEntityOperation
   * @see #doOperation
   * @see #getUnknownOperationPolicy
   * @see #setUnknownOperationPolicy
   * 
   */
  public enum UnknownOperationPolicy
  {
    /** Request for unknown operations lead to an error (exception).
     * 
     * <p>
     * This is the default on any {@link SimEntity}, unless specified otherwise.
     * 
     */
    ERROR,
    /** Requests for unknown operations are reported (to System.err).
     * 
     */
    REPORT,
    /** Requests for unknown operations are logged as warning.
     * 
     */
    LOG_WARNING,
    /** Requests for unknown operations are logged as info.
     * 
     */
    LOG_INFO,
    /** Requests for unknown operations are logged as 'FINE'.
     * 
     */
    LOG_FINE,
    /** Requests for unknown operations are silently ignored.
     * 
     */    
    IGNORE
  }
  
  /** Returns the policy for unknown operations.
   * 
   * @return The policy for unknown operations, non-{@code null}.
   * 
   * @see SimEntityOperation
   * @see #doOperation
   * 
   */
  UnknownOperationPolicy getUnknownOperationPolicy ();
  
  /** Sets the policy for unknown operations.
   * 
   * @param unknownOperationPolicy The new policy for unknown operations, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}.
   * 
   * @see SimEntityOperation
   * @see #doOperation
   * 
   */
  void setUnknownOperationPolicy (UnknownOperationPolicy unknownOperationPolicy);
  
  /** Performs the given operation, identified by an operation request, at this entity at given time.
   * 
   * <p>
   * If the operation is unknown, the course of action is as determined by {@link #getUnknownOperationPolicy}.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * @param <Rep> The reply type (corresponding to the operation type).
   * 
   * @param time    The time at which to perform the operation.
   * @param request The operation request (holds operation type as well).
   * 
   * @return The operation reply, non-{@code null} unless the request was unknown and ignored.
   * 
   * @throws IllegalArgumentException If time is in the past,
   *                                  if the request is {@code null}, illegally structured, or contains illegal arguments,
   *                                  or if the corresponding operation is not registered and the policy is not to accept that.
   * 
   * @see #getRegisteredOperations
   * @see UnknownOperationPolicy
   * @see #getUnknownOperationPolicy
   * 
   */
  <O extends SimEntityOperation, Req extends SimEntityOperation.Request, Rep extends SimEntityOperation.Reply>
  Rep doOperation (double time, Req request);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the registered notification types of this entity.
   * 
   * @return The registered notifications types of this entity (in registration order).
   * 
   */
  Set<SimEntitySimpleEventType.Member> getRegisteredNotificationTypes ();

  /** An action for use in {@link #doAfterNotifications}.
   * 
   */
  @FunctionalInterface
  interface Action
  {
    /** Performs the action.
     * 
     */
    public void execute ();
  }
  
  /** Registers an {@link Action} to be taken once this entity has finished issuing notifications,
   *  or takes the action immediately if this entity is not notifying listeners.
   * 
   * @param action The action to take, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the action is {@code null}.
   * 
   */
  void doAfterNotifications (Action action);
  
  /** Possible courses of action when an unknown notification is to be published.
   * 
   * @see #getUnknownNotificationTypePolicy
   * @see #setUnknownNotificationTypePolicy
   * @see #getRegisteredNotificationTypes
   * 
   */
  public enum UnknownNotificationTypePolicy
  {
    /** Unknown notification types are fired as {@link SimEntityListener#notifyStateChanged},
     *  but a warning is issued in the log at {@link Level#WARNING}.
     * 
     * <p>
     * This is the default.
     * 
     */
    FIRE_AND_WARN,
    /** Unknown notification types are fired (otherwise silently) as {@link SimEntityListener#notifyStateChanged}.
     * 
     */
    FIRE_SILENTLY,
    /** Unknown notification types lead to an error (exception).
     * 
     */
    ERROR
  }
  
  /** Returns the policy for unknown notifications types.
   * 
   * @return The policy for unknown operations, non-{@code null}.
   * 
   */
  UnknownNotificationTypePolicy getUnknownNotificationTypePolicy ();
  
  /** Sets the policy for unknown notifications types.
   * 
   * @param unknownNotificationTypePolicy The new policy for unknown notification types, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}.
   * 
   */
  void setUnknownNotificationTypePolicy (UnknownNotificationTypePolicy unknownNotificationTypePolicy);
  
  /** Registers a listener to event notifications related to this entity.
   * 
   * <p>
   * Implementations are encouraged to consider the use of weak/soft references to registered listeners.
   * 
   * @param listener The listener; ignored if already registered or <code>null</code>.
   * 
   * @see SimEntityListener
   * @see #unregisterSimEntityListener
   * @see #getSimEntityListeners
   * 
   */
  void registerSimEntityListener (SimEntityListener listener);
  
  /** Unregisters a listener to event notifications related to this entity.
   * 
   * @param listener The listener; ignored if not registered or <code>null</code>.
   * 
   * @see SimEntityListener
   * @see #registerSimEntityListener
   * @see #getSimEntityListeners
   * 
   */
  void unregisterSimEntityListener (SimEntityListener listener);

  /** Gets the listeners to this entity.
   * 
   * <p>
   * Callers must <i>not</i> attempt to change the returned set.
   * 
   * <p>
   * Implementations are encouraged to return read-only views.
   * 
   * @return A read-only set holding listeners to this entity.
   * 
   * @see SimEntityListener
   * @see #registerSimEntityListener
   * @see #unregisterSimEntityListener
   * @see #getSimEntityListeners
   * 
   */
  Set<SimEntityListener> getSimEntityListeners ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET [OPERATION/NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns whether this entity ignores event-list resets.
   * 
   * @return Whether this entity ignores event-list resets.
   * 
   * @see #setIgnoreEventListReset
   * 
   */
  boolean isIgnoreEventListReset ();
  
  /** Sets whether this entity ignores future event-list resets.
   * 
   * <p>
   * By contract, a {@link SimEntity} must reset with {@link #resetEntity}
   * whenever the underlying event list (if present) is reset.
   * The normal procedure to achieve this is to register as a {@link SimEventListResetListener},
   * and invoke {@link #resetEntity} upon {@link SimEventListResetListener#notifyEventListReset}.
   * 
   * <p>
   * There are cases, however, in which resetting this entity has to be delegated to another entity (or other type of
   * object listening to the event list), for instance because the order in which entities are reset is important.
   * Through this method, the automatic resetting of this entity upon an event-list reset can be disabled.
   * Note, however, that the contract remains that the entity has to follow event-list resets.
   * 
   * @param ignoreEventListReset Whether this entity ignores future event-list resets.
   * 
   * @see #isIgnoreEventListReset
   * @see SimEventListResetListener#notifyEventListReset
   * 
   */
  void setIgnoreEventListReset (boolean ignoreEventListReset);
  
  /** Puts the entity into its "known" initial state.
   *
   * <p>
   * This method is used in order to restart a simulation.
   * By contract, a {@link SimEntity} must reset if its underlying {@link SimEventList} resets.
   * However, a {@link SimEntity} can also be reset independently from the event list.
   * In the latter case, it takes its current time from the event list, if available,
   * or to {@link Double#NEGATIVE_INFINITY} otherwise.
   * 
   * <p>
   * Implementations must ensure that only a single {@link SimEntityListener#notifyResetEntity} is invoked <i>after</i>
   * the entity is in the new (valid) state.
   * 
   * @see SimEventListResetListener#notifyEventListReset
   * 
   */
  void resetEntity ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE [OPERATION/NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the time of the last update of this entity.
   * 
   * <p>
   * Upon construction, the last-update time must be set to minus infinity, mimicking the behavior of {@link SimEventList}.
   * Upon an explicit reset of this entity, the last-update time is to be copied from the event list, if available
   * (or reset to {@link Double#NEGATIVE_INFINITY} otherwise).
   * In all other cases, the time returned corresponds to the time argument of the last update of the entity,
   * see {@link SimEntityListener#notifyUpdate} for more details.
   * 
   * @return The time of the last update of this entity.
   * 
   * @see AbstractSimEntity#update
   * @see SimEntityListener#notifyUpdate
   * 
   */
  double getLastUpdateTime ();
  
  /** Updates this entity.
   * 
   * <p>
   * For a precise definition of an update of an entity, refer to {@link SimEntityListener#notifyUpdate}.
   * 
   * @param time The time of the update (the new time on the entity).
   * 
   * @throws IllegalStateException If time is in the past.
   * 
   * @see #getLastUpdateTime
   * @see SimEntityListener#notifyUpdate
   * 
   */
  void update (double time);
  
}
