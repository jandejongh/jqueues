package nl.jdj.jqueues.r5;

import java.util.Set;
import java.util.logging.Level;
import nl.jdj.jqueues.r5.entity.AbstractSimEntity;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.extensions.qos.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;
import nl.jdj.jsimulation.r5.SimEventListResetListener;

/** The interface common to both {@link SimJob}s and {@link SimQueue}s.
 *
 * <p>
 * A {@link SimEntity} is an entity relevant to event-list scheduling in a queueing system simulation.
 * Presently, it is either a queue ({@link SimQueue}) or a job ({@link SimJob}).
 * A queue is an object capable of holding jobs <i>visiting</i>,
 * providing (generic) service to the visiting jobs, and deciding when (or if) they will leave the
 * queue, and end the visit.
 * For more details, see {@link SimQueue} and {@link SimJob}.
 * 
 * <p>
 * A {@link SimEntity} is the common part of queues and jobs.
 * What they have in common is the event list they are attached to, the fact that they have a name,
 * and their obligation to propagate state changes (including the currently visited queue of a job, and
 * the jobs currently visiting a queue).
 * 
 * <p>
 * Objects implementing this interface take care of
 * <ul>
 * <li>holding the underlying event list ({@link SimEventList}), which may be <code>null</code> on non-{@link SimQueue}s,
 * <li>registering as a {@link SimEventListResetListener} to a non-<code>null</code> {@link SimEventList},
 * <li>naming,
 * <li>maintaining listeners ({@link SimEntityListener}),
 * <li>propagating reset events on the {@link SimEventList} to {@link SimEntityListener}s through {@link #resetEntity},
 * <li>maintaining the time of the last update with {@link #getLastUpdateTime},
 * <li>supporting (deferred) actions to take immediately after notifying listeners
 *     (supported because, as by contract of {@link SimEntity} and {@link SimEntityListener},
 *     listeners are not allowed to initiate queue operations).
 * </ul>
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see AbstractSimEntity
 * @see SimEntityListener
 * @see SimJob
 * @see SimQueue
 *
 */
public interface SimEntity<J extends SimJob, Q extends SimQueue>
extends SimEventListResetListener, SimQoS<J, Q>
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
   * It may be <code>null</code>, however, for {@link SimQueue}s the underlying event list must be non-<code>null</code>.
   * 
   * <p>
   * If non-<code>null</code>, implementations must register as a {@link SimEventListResetListener},
   * and propagate any reset of the event list through {@link #resetEntity}.
   * 
   * @return The underlying event list of this {@link SimEntity}.
   * 
   */
  SimEventList getEventList ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Registers a listener to events related to this entity.
   * 
   * @param listener The listener; ignored if already registered or <code>null</code>.
   * 
   * @see #unregisterSimEntityListener
   * 
   */
  void registerSimEntityListener (SimEntityListener<J, Q> listener);
  
  /** Unregisters a listener to events related to this entity.
   * 
   * @param listener The listener; ignored if not registered or <code>null</code>.
   * 
   * @see #registerSimEntityListener
   * 
   */
  void unregisterSimEntityListener (SimEntityListener<J, Q> listener);

  /** Gets the listeners to this entity.
   * 
   * Callers must <i>not</i> attempt to change the returned set.
   * Implementations are encouraged to return read-only views.
   * 
   * @return The listeners to this entity.
   * 
   */
  Set<SimEntityListener<J, Q>> getSimEntityListeners ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a default, type-specific name for this {@link SimEntity}.
   * 
   * <p>
   * The string is used as a fallback return value for <code>Object.toString ()</code>
   * in case the user did not set an instance-specific name
   * through {@link #setName}.
   * 
   * <p>
   * Implementation classes are recommended to <i>not</i> make this method final
   * unless the class itself is final.
   * 
   * @return A default, type-specific name for this {@link SimEntity}.
   * 
   * @see #setName
   * 
   */
  String toStringDefault ();
  
  /** Sets the name of this {@link SimEntity}, to be returned by subsequent calls to <code>Object.toString ()</code>.
   * 
   * @param name The new name of this job or queue; if non-<code>null</code>, the string will be supplied by subsequent calls
   *               to <code>Object.toString ()</code>; otherwise, the type-specific default will be used for that.
   * 
   * @see #toStringDefault
   * 
   */
  void setName (String name);
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REGISTERED OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the registered operations of this entity.
   * 
   * @return The registered operations of this entity (in registration order).
   * 
   */
  Set<SimEntityOperation> getRegisteredOperations ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UNKNOWN OPERATION POLICY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Possible courses of action when an unknown operation is offered to {@link #doOperation}.
   * 
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
   * @see #doOperation
   * 
   */
  void setUnknownOperationPolicy (UnknownOperationPolicy unknownOperationPolicy);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DO OPERATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
   * @throws IllegalArgumentException If time is in the past
   *                                  and the operation is not a {@link SimEntityOperationUtils.ResetOperation},
   *                                  or if the request is {@code null}, illegally structured, or contains illegal arguments,
   *                                  or if the corresponding operation is not registered and the policy is not to accept that.
   * 
   * @see UnknownOperationPolicy
   * @see #getUnknownOperationPolicy
   * 
   */
  <O extends SimEntityOperation, Req extends SimEntityOperation.Request, Rep extends SimEntityOperation.Reply>
  Rep doOperation (double time, Req request);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UNKNOWN NOTIFICATION-TYPE POLICY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REGISTERED NOTIFICATION TYPES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the registered notification types of this entity.
   * 
   * @return The registered notifications types of this entity (in registration order).
   * 
   */
  Set<SimEntitySimpleEventType.Member> getRegisteredNotificationTypes ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ACTION (AFTER NOTIFICATIONS)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
  
}
