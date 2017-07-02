package nl.jdj.jqueues.r5.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.jdj.jqueues.r5.entity.SimEntity.UnknownOperationPolicy;
import nl.jdj.jqueues.r5.entity.SimEntityOperation.Reset;
import nl.jdj.jqueues.r5.entity.SimEntityOperation.Update;
import nl.jdj.jqueues.r5.entity.jq.AbstractSimJQ;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.listener.StdOutSimEntityListener;
import nl.jdj.jsimulation.r5.SimEventList;
import nl.jdj.jsimulation.r5.SimEventListResetListener;

/** A partial implementation of a {@link SimEntity}.
 * 
 * <p>
 * See {@link SimEntity} and the constructor documentation for more details.
 * 
 * <p>
 * For a more complete (though still partial) implementations of jobs and queues, see {@link AbstractSimJQ}.
 * 
 * @see AbstractSimJQ
 * @see SimQueue
 * @see SimJob
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
@SuppressWarnings("LeakingThisInConstructor")
public abstract class AbstractSimEntity
implements SimEntity
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // USE ARRAY OPTIMIZATION [COMPILE-TIME SWITCH]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** When {@code true}, this class and some sub-classes maintain various array copies of collections
   *  often iterated over, like listeners and hooks, and uses array iteration instead of Collection iteration.
   * 
   */
  protected final static boolean USE_ARRAY_OPTIMIZATION = true;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOGGER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOGGER = Logger.getLogger (AbstractSimEntity.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates a new {@link SimEntity} with given event list and name.
   * 
   * <p>
   * The constructor registers the event list and the name (the latter can be set at any time, see {@link #setName}).
   * It then registers the reset and update operations and notifications through {@link #registerOperation}
   * and {@link #registerNotificationType}.
   * Finally, if available, it copies the time from the event list, and adds itself as (reset) listener to the event list.
   * (We are a {@link SimEventListResetListener}.)
   * 
   * @param eventList The event list to use, may be {@code null}.
   * @param name      The name of the entity, may be {@code null}.
   * 
   * @see #getEventList
   * @see #toStringDefault
   * @see #setName
   * @see #registerOperation
   * @see Reset
   * @see Update
   * @see SimEntitySimpleEventType#RESET
   * @see #getLastUpdateTime
   * @see SimEventList#addListener
   * @see SimEventListResetListener
   * 
   */
  public AbstractSimEntity (final SimEventList eventList, final String name)
  {
    this.eventList = eventList;
    setName (name);
    registerOperation (SimEntityOperation.Reset.getInstance ());
    registerOperation (SimEntityOperation.Update.getInstance ());
    registerNotificationType (SimEntitySimpleEventType.RESET, this::fireReset);
    if (this.eventList != null)
    {
      this.lastUpdateTime = this.eventList.getTime ();
      this.eventList.addListener (this);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT LIST
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The underlying event list (to be supplied and fixed in the constructor).
   *
   * <p>
   * Non-<code>null</code> for {@link SimQueue}s; may be <code>null</code> for other subclasses.
   * 
   */
  private final SimEventList eventList;
  
  /** Returns the underlying event list.
   * 
   * <p>
   * The event list is fixed and supplied in the constructor.
   * It is non-<code>null</code> for {@link SimQueue}s; may be <code>null</code> for other subclasses.
   * 
   * @return The underlying event list; non-<code>null</code> for {@link SimQueue}s; may be <code>null</code> for other subclasses.
   * 
   */
  @Override
  public final SimEventList getEventList ()
  {
    return this.eventList;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private String name = null;

  @Override
  public final void setName (final String name)
  {
    this.name = name;
  }
  
  /** Returns "AbstractSimEntity".
   * 
   * @return "AbstractSimEntity".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "AbstractSimEntity";
  }
  
  /** Returns the internally stored user-supplied name, if non-<code>null</code>, or the type specific default.
   * 
   * @return The internally stored user-supplied name, if non-<code>null</code>, or the type specific default.
   * 
   * @see #setName
   * @see #toStringDefault
   * 
   */
  @Override
  public String toString ()
  {
    if (this.name != null)
      return this.name;
    else
      return toStringDefault ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS: REGISTRATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimEntityOperation> registeredOperations = new LinkedHashSet<> ();
  
  @Override
  public final Set<SimEntityOperation> getRegisteredOperations ()
  {
    return Collections.unmodifiableSet (this.registeredOperations);
  }

  /** Registers a {@link SimEntityOperation} at this entity.
   * 
   * @param operation The operation, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the operation is {@code null} or already registered.
   * 
   */
  protected final void registerOperation (final SimEntityOperation operation)
  {
    if (operation == null || this.registeredOperations.contains (operation))
      throw new IllegalArgumentException ();
    this.registeredOperations.add (operation);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS: DELEGATE REGISTRATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimEntityOperation, SimEntityOperation> delegatedOperations = new LinkedHashMap<> ();
  
  /** Returns the registered delegated operations.
   * 
   * <p>
   * The set returned is always a subset of all registered operations.
   * 
   * @return An unmodifiable set holding the registered delegated operations.
   * 
   * @see #getRegisteredOperations
   * 
   */
  public final Set<SimEntityOperation> getRegisteredDelegatedOperations ()
  {
    return Collections.unmodifiableSet (this.delegatedOperations.keySet ());
  }
  
  /** Registers a {@link SimEntityOperation} at this entity, but delegates it to another operation.
   * 
   * @param operation         The operation, non-{@code null}.
   * @param delegateOperation The delegate operation, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the operation is {@code null} or already registered,
   *                                  or if the delegate operation is {@code null}.
   * 
   */
  protected final void registerDelegatedOperation (final SimEntityOperation operation, final SimEntityOperation delegateOperation)
  {
    if (operation == null || this.registeredOperations.contains (operation) || delegateOperation == null)
      throw new IllegalArgumentException ();
    if (this.delegatedOperations.containsKey (operation))
      throw new IllegalStateException ();
    this.registeredOperations.add (operation);
    this.delegatedOperations.put (operation, delegateOperation);
  }
  
  /** Delegates a registered {@link SimEntityOperation} at this entity to another operation.
   * 
   * @param operation         The operation, non-{@code null}.
   * @param delegateOperation The delegate operation, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the operation is {@code null} or not registered,
   *                                  or if the delegate operation is {@code null}.
   * 
   */
  protected final void delegateOperation (final SimEntityOperation operation, final SimEntityOperation delegateOperation)
  {
    if (operation == null || (! this.registeredOperations.contains (operation)) || delegateOperation == null)
      throw new IllegalArgumentException ();
    this.delegatedOperations.put (operation, delegateOperation);    
  }
  
  /** Removes the delegation for given {@link SimEntityOperation}, but keeps the operation registered
   *  (falling back onto its native behavior}.
   * 
   * @param operation The operation, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the operation is {@code null} or not registered or not delegated.
   * 
   */
  protected final void removeDelegationForOperation (final SimEntityOperation operation)
  {
    if (operation == null
      || (! this.registeredOperations.contains (operation))
      || (! this.delegatedOperations.containsKey (operation)))
      throw new IllegalArgumentException ();
    this.delegatedOperations.remove (operation);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS: UNKNOWN OPERATION POLICY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private UnknownOperationPolicy unknownOperationPolicy = UnknownOperationPolicy.ERROR;
  
  @Override
  public final UnknownOperationPolicy getUnknownOperationPolicy ()
  {
    return this.unknownOperationPolicy;
  }
  
  @Override
  public final void setUnknownOperationPolicy (final UnknownOperationPolicy unknownOperationPolicy)
  {
    if (unknownOperationPolicy == null)
      throw new IllegalArgumentException ();
    this.unknownOperationPolicy = unknownOperationPolicy;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS: DO OPERATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Performs the requested operation.
   * 
   * @param time    The time of the request.
   * @param request The request, non-{@code null}.
   * 
   * @return The reply.
   * 
   * @throws IllegalArgumentException If the entity or request is {@code null}, the operation is {@code null},
   *                                  time is in the past (except for the {@link Reset} operation,
   *                                  the request has target entity other than {@code this},
   *                                  the is of illegal type, or its parameter values are illegal.
   * 
   * @param <O>   The operation type.
   * @param <Req> The request type (corresponding to the operation type).
   * @param <Rep> The reply type (corresponding to the operation type).
   * 
   * @see #getRegisteredOperations
   * @see #registerDelegatedOperation
   * 
   * @see UnknownOperationPolicy
   * @see #getUnknownOperationPolicy
   * 
   */
  @Override
  public final
  <O   extends SimEntityOperation,
   Req extends SimEntityOperation.Request,
   Rep extends SimEntityOperation.Reply>
  Rep doOperation (double time, Req request)
  {
    if (request == null)
      throw new IllegalArgumentException ();
    if (request.getOperation () == null)
      throw new IllegalArgumentException ();
    if (request.getTargetEntity () != this)
      throw new IllegalArgumentException ();
    if (request.getOperation () != SimEntityOperation.Reset.getInstance ()
      && time < getLastUpdateTime ())
      throw new IllegalArgumentException ();
    if (! this.registeredOperations.contains (request.getOperation ()))
      switch (this.unknownOperationPolicy)
      {
        case ERROR:
          throw new IllegalArgumentException ();
        case REPORT:
          System.err.println ("Unknown operation on " + this + ": " + request.getOperation () + "!");
          break;
        case LOG_WARNING:
          LOGGER.log (Level.WARNING, "Unknown operation on {0}: {1}!", new Object[]{this, request.getOperation ()});
          break;
        case LOG_INFO:
          LOGGER.log (Level.INFO, "Unknown operation on {0}: {1}!", new Object[]{this, request.getOperation ()});
          break;
        case LOG_FINE:
          LOGGER.log (Level.FINE, "Unknown operation on {0}: {1}!", new Object[]{this, request.getOperation ()});
          break;
        case IGNORE:
          // Special indication that the request has been ignored.
          return null;
        default:
          throw new RuntimeException ();
      }
    if (this.delegatedOperations.containsKey (request.getOperation ()))
      return (Rep) this.delegatedOperations.get (request.getOperation ()).doOperation (time, request);
    else
      return (Rep) request.getOperation ().doOperation (time, request);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: REGISTERED NOTIFICATION TYPES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A functional interface for a notification handler.
   * 
   */
  @FunctionalInterface
  public interface Notifier
  {
    
    /** Fires the notification to all relevant listeners.
     * 
     * @param event The relevant event, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the argument is {@code null} or of illegal type.
     * 
     */
    void fire (SimEntityEvent event);
    
  }
  
  /** The mapping between a notification type (reset, arrival, departures, etc.) and a {@link Notifier} for it.
   * 
   */
  private final Map<SimEntitySimpleEventType.Member, Notifier> notificationMap = new LinkedHashMap<> ();
  
  /** Registers a mapping between a notification type (reset, arrival, departures, etc.) and a {@link Notifier} for it.
   * 
   * <p>
   * The notifier may be {@code null}, meaning the notification type is (will be) known, and will be
   * reported through {@link SimEntityListener#notifyStateChanged},
   * but no additional actions/notifications (through a {@link Notifier}) will be taken on listeners.
   * 
   * @param notificationType The notification type; non-{@code null} and not yet registered.
   * @param notifier         The {@link Notifier} for it, may be {@code null}.
   * 
   * @throws IllegalArgumentException If the notification type is {@code null} or already registered. 
   * 
   */
  protected final void registerNotificationType
  (final SimEntitySimpleEventType.Member notificationType, final Notifier notifier)
  {
    if (notificationType == null)
      throw new IllegalArgumentException ();
    if (this.notificationMap.containsKey (notificationType))
      throw new IllegalArgumentException ();
    this.notificationMap.put (notificationType, notifier);
  }
  
  @Override
  public final Set<SimEntitySimpleEventType.Member> getRegisteredNotificationTypes ()
  {
    return Collections.unmodifiableSet (new LinkedHashSet<> (this.notificationMap.keySet ()));
  }
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: UNKNOWN NOTIFICATION-TYPE POLICY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private UnknownNotificationTypePolicy unknownNotificationTypePolicy = UnknownNotificationTypePolicy.FIRE_AND_WARN;

  @Override
  public final UnknownNotificationTypePolicy getUnknownNotificationTypePolicy ()
  {
    return this.unknownNotificationTypePolicy;
  }

  @Override
  public final void setUnknownNotificationTypePolicy (final UnknownNotificationTypePolicy unknownNotificationTypePolicy)
  {
    if (unknownNotificationTypePolicy == null)
      throw new IllegalArgumentException ();
    this.unknownNotificationTypePolicy = unknownNotificationTypePolicy;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: PRE-NOTIFICATION HOOKS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A functional interface for a hook to be called before notifications are sent (i.e., before a state change is advertised).
   * 
   * <p>
   * A pre-notification hook allows sub-classes to modify the pending notifications (for whatever reason),
   * but it is typically used to automatically detect state-changes that are not explicitly dealt with otherwise.
   * 
   */
  @FunctionalInterface
  public interface PreNotificationHook
  {

    /** Invokes the hook.
     * 
     * @param pendingNotifications The pending notifications (may be changed).
     * 
     */
    void hook (List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> pendingNotifications);

  }
  
  /** The registered pre-notification hooks.
   * 
   */
  private final Set<PreNotificationHook> preNotificationHooks = new LinkedHashSet<> ();

  private PreNotificationHook[] preNotificationHooksAsArray = new PreNotificationHook[0];
  
  /** Registers a pre-notification hook.
   * 
   * @param preNotificationHook The hook, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the hook is {@code null} or already registered.
   * 
   */  
  protected final void registerPreNotificationHook (final PreNotificationHook preNotificationHook)
  {
    if (preNotificationHook == null || this.preNotificationHooks.contains (preNotificationHook))
      throw new IllegalArgumentException ();
    this.preNotificationHooks.add (preNotificationHook);
    if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
      this.preNotificationHooksAsArray
        = this.preNotificationHooks.toArray (new PreNotificationHook[this.preNotificationHooks.size ()]);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: POST-NOTIFICATION ACTIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The actions to take immediately after firing pending notifications has ended.
   * 
   */
  private final List<Action> postNotificationActions = new ArrayList<> ();
  
  /** Adds a (one-time) action to take immediately after firing pending notifications has ended.
   * 
   * <p>
   * If this entity is not firing notifications, the action is invoked immediately.
   * 
   * @param action The action to take, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the action is {@code null}.
   * 
   * @see #fireAndLockPendingNotifications
   * 
   */
  @Override
  public final void doAfterNotifications (final Action action)
  {
    if (action == null)
      throw new IllegalArgumentException ();
    if (! this.firingPendingNotifications)
      action.execute ();
    else
      this.postNotificationActions.add (action);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: PENDING NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The pending notifications for this entity, mapped onto the associated job (if applicable).
   * 
   */
  private final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> pendingNotifications = new ArrayList<> ();
  
  /** The time corresponding to the pending notifications (which all share the same time).
   * 
   */
  private double pendingNotificationsTime = Double.NEGATIVE_INFINITY;
  
  /** Whether the pending notifications are locked (no longer mutable).
   * 
   * <p>
   * Pending notifications are locked the moment they are being fired to listeners,
   * and remain locked until a top-level entity event unlocks it.
   * 
   */
  private boolean pendingNotificationsLocked = true;
  
  /** Whether this entity is firing notifications.
   * 
   * <p>
   * A flag used to prevent modifications to the pending notifications from within listeners.
   * 
   * @see SimEntity#doAfterNotifications
   * 
   */
  private boolean firingPendingNotifications = false;
  
  /** Adds a notification to the pending notifications.
   * 
   * @param notification The notification, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the notification is {@code null},
   *                                  if the map is of illegal size (other than unity), or has a {@code null} key,
   *                                  if the notification is already present
   *                                  or has time different from the pending-notifications time,
   *                                  if this entity is locked for pending notifications mutations,
   *                                  or is currently firing notifications.
   * 
   */
  protected final void addPendingNotification (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> notification)
  {
    if (notification == null
    ||  notification.size () != 1
    ||  notification.containsKey (null)
    ||  notification.containsValue (null)
    ||  this.pendingNotifications.contains (notification)
    ||  ((! this.pendingNotifications.isEmpty ()) && getLastUpdateTime () != this.pendingNotificationsTime)
    ||  this.pendingNotificationsLocked
    ||  this.firingPendingNotifications)
      throw new IllegalArgumentException ("notification: " + notification);
    if (this.pendingNotifications.isEmpty ())
      this.pendingNotificationsTime = getLastUpdateTime ();
    if (containsPendingNotification (notification))
      LOGGER.log (Level.WARNING, "Attempt to re-enter notification {0}@{1}!", new Object[]{notification, this});
    else
      this.pendingNotifications.add (notification);
  }
  
  /** Adds a notification to the pending notifications.
   * 
   * <p>
   * Convenience method to {@link #addPendingNotification(java.util.Map)} creating a singleton map from the arguments on the fly.
   * 
   * @param notificationType The notification type, non-{@code null}.
   * @param notification     The notification event, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the notification type or event is {@code null} or if
   *                                  {@link #addPendingNotification(java.util.Map)} encounters an error in the arguments.
   * 
   */
  protected final void addPendingNotification
  (final SimEntitySimpleEventType.Member notificationType, final SimEntityEvent notification)
  {
    if (notificationType == null || notification == null)
      throw new IllegalArgumentException ();
    addPendingNotification (Collections.singletonMap (notificationType, notification));
  }
  
  /** Checks if a notification is already registered in the pending notifications.
   * 
   * @param notification The notification.
   * 
   * @return True if the notification is already pending.
   * 
   * @throws IllegalArgumentException If there is an error in the arguments.
   * 
   */
  private boolean containsPendingNotification (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> notification)
  {
    if (notification == null
    ||  notification.size () != 1
    ||  notification.containsKey (null))
      throw new IllegalArgumentException ();
    final SimEntitySimpleEventType.Member eventType = notification.keySet ().iterator ().next ();
    final SimEntityEvent event = notification.values ().iterator ().next ();
    for (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> member : this.pendingNotifications)
      if (member.keySet ().iterator ().next () == eventType
      &&  member.values ().iterator ().next () == event)
        return true;
    return false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: PENDING NOTIFICATIONS LOCK/UNLOCK/CLEAR/FIRE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Clears the pending notifications and unlocks them.
   * 
   * @throws IllegalStateException If this entity is currently firing notifications.
   * 
   * @see #firingPendingNotifications
   * 
   */
  private void clearAndUnlockPendingNotifications ()
  {
    if (this.firingPendingNotifications)
      throw new IllegalStateException ();
    this.pendingNotifications.clear ();
    this.pendingNotificationsLocked = false;
  }
  
  /** Clears the pending notifications and unlocks them if needed (i.e., if currently locked).
   * 
   * <p>
   * This method does nothing if the pending notifications are already locked.
   * 
   * @return True if the entity was locked beforehand.
   * 
   * @throws IllegalStateException If this entity is currently firing notifications.
   * 
   * @see #firingPendingNotifications
   * 
   */
  protected final boolean clearAndUnlockPendingNotificationsIfLocked ()
  {
    if (this.firingPendingNotifications)
      throw new IllegalStateException ();
    final boolean isLocked = this.pendingNotificationsLocked;
    if (isLocked)
      clearAndUnlockPendingNotifications ();
    return isLocked;
  }
  
  /** Fires and locks the pending notifications to listeners.
   * 
   * <p>
   * Fires, if present, pre-notification hooks and post-notification actions, even if the set of pending notifications is empty.
   * The post-notification actions are cleared.
   * 
   * <p>
   * Note that listeners are <i>not</i> notified with empty notification sets!
   * 
   * @throws IllegalStateException If the time of pending notifications is not equal to {@link #getLastUpdateTime},
   *                               or if this entity is currently firing notifications (already).
   * 
   * @see #registerPreNotificationHook
   * @see #doAfterNotifications
   * 
   */
  protected final void fireAndLockPendingNotifications ()
  {
    if ((! this.pendingNotifications.isEmpty ()) && this.pendingNotificationsTime != getLastUpdateTime ())
      throw new IllegalStateException ();
    if (this.firingPendingNotifications)
      throw new IllegalStateException ();
    this.pendingNotificationsLocked = true;
    this.firingPendingNotifications = true;
    final boolean isResetNotification = (this.pendingNotifications.size () == 1
                                        && this.pendingNotifications.get (0).containsKey (SimEntitySimpleEventType.RESET));
    if (! isResetNotification)
      for (final PreNotificationHook preNotificationHook : this.preNotificationHooks)
        preNotificationHook.hook (this.pendingNotifications);
    if (! this.pendingNotifications.isEmpty ())
    {
      final double time = getLastUpdateTime ();
      // Respect policy for unknown notification types.
      for (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> notification : this.pendingNotifications)
      {
        final SimEntitySimpleEventType.Member notificationType = notification.keySet ().iterator ().next ();
        if (! this.notificationMap.containsKey (notificationType))
          switch (this.unknownNotificationTypePolicy)
          {
            case FIRE_AND_WARN:
              LOGGER.log (Level.WARNING, "Unknown notification type {0}.", notificationType);
              break;
            case FIRE_SILENTLY:
              break;
            case ERROR:
              throw new IllegalArgumentException ();
            default:
              throw new RuntimeException ();
          }
      }    
      for (SimEntityListener l : this.simEntityListeners)
        l.notifyStateChanged (time, this, this.pendingNotifications);
      for (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> notification : this.pendingNotifications)
      {
        final SimEntitySimpleEventType.Member notificationType = notification.keySet ().iterator ().next ();
        final SimEntityEvent notificationEvent = notification.values ().iterator ().next ();
        if (this.notificationMap.containsKey (notificationType)
        &&  this.notificationMap.get (notificationType) != null)
          this.notificationMap.get (notificationType).fire (notificationEvent);
      }
    }
    this.firingPendingNotifications = false;
    while (! this.postNotificationActions.isEmpty ())
      this.postNotificationActions.remove (0).execute ();
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS: LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The {@link SimEntityListener}s of this simulation entity.
   * 
   */
  private final Set<SimEntityListener> simEntityListeners = new LinkedHashSet<>();

  private SimEntityListener[] simEntityListenersAsArray = new SimEntityListener[0];
  
  @Override
  public final void registerSimEntityListener (final SimEntityListener listener)
  {
    if (listener != null && ! this.simEntityListeners.contains (listener))
    {
      this.simEntityListeners.add (listener);
      if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
        this.simEntityListenersAsArray = this.simEntityListeners.toArray (new SimEntityListener[this.simEntityListeners.size ()]);
    }
  }

  @Override
  public final void unregisterSimEntityListener (final SimEntityListener listener)
  {
    if (this.simEntityListeners.contains (listener))
    {
      this.simEntityListeners.remove (listener);
      if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
        this.simEntityListenersAsArray = this.simEntityListeners.toArray (new SimEntityListener[this.simEntityListeners.size ()]);
    }
  }  

  @Override
  public final Set<SimEntityListener> getSimEntityListeners ()
  {
    // The right way, actually...
    // return Collections.unmodifiableSet (this.simEntityListeners);
    // However, for performance reasons, send direct reference to the set.
    return this.simEntityListeners;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STDOUT LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A private instance of a {@link StdOutSimEntityListener}.
   * 
   * @see #registerStdOutSimEntityListener
   * @see #unregisterStdOutSimEntityListener
   * 
   */
  private final StdOutSimEntityListener stdOutSimEntityListener = new StdOutSimEntityListener ();
  
  /** Registers the (private) {@link StdOutSimEntityListener} as listener (convenience method).
   * 
   * @see #unregisterStdOutSimEntityListener
   * @see #registerSimEntityListener
   * 
   */
  public final void registerStdOutSimEntityListener ()
  {
    registerSimEntityListener (this.stdOutSimEntityListener);
  }
  
  /** Unregisters the (private) {@link StdOutSimEntityListener} as listener, if registered (convenience method).
   * 
   * @see #registerStdOutSimEntityListener
   * @see #unregisterSimEntityListener
   * 
   */
  public final void unregisterStdOutSimEntityListener ()
  {
    unregisterSimEntityListener (this.stdOutSimEntityListener);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET [OPERATION/NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   * <p>
   * Implementation of {@link SimEntity#resetEntity} in subclasses <i>without</i> notifying listeners.
   * 
   * <p>
   * As it is essential to issue a <i>single</i> {@link SimEntityListener#notifyStateChanged} and a single
   * {@link SimEntityListener#notifyResetEntity} <i>after</i> the state has
   * been set to its initial setting.
   * The implementation of {@link SimEntity#resetEntity} has been made final
   * in order to ensure this.
   * This current method is intended to be used by subclasses to perform the reset.
   * 
   * <p>
   * The last-update time ({@link #getLastUpdateTime}) has already been set to the new value,
   * but listeners have <i>not</i> been notified yet.
   * 
   * @see SimEntity#resetEntity
   * @see #update
   * @see #getLastUpdateTime
   * 
   */
  protected void resetEntitySubClass ()
  {
    /* EMPTY */
  }
  
  /** Copies the last-update time from the event list (if available),
   *  invokes {@link #resetEntitySubClass} and fires the event to listeners (as a state-change event and through a
   *  separate reset indication).
   * 
   * <p>
   * If the reset is the result of an event-list reset, this takes the new "start time" from the event list,
   * which has been set already.
   * In the (more unlikely) case of an autonomous reset of the entity, copying the last update time from the
   * event list (before notifying listeners) assures that statistics-gathering listeners restart their jobs from
   * the current time.
   * If no event list is available, the last update time is set to {@link Double#NEGATIVE_INFINITY}.
   * 
   * <p>
   * This method is <code>final</code>; use {@link #resetEntitySubClass} to override/augment behavior.
   * 
   * @see #getLastUpdateTime
   * @see #resetEntitySubClass
   * @see SimEntityListener#notifyStateChanged
   * @see SimEntityListener#notifyResetEntity
   * 
   */
  @Override
  public final void resetEntity ()
  {
    if (getEventList () != null)
      this.lastUpdateTime = getEventList ().getTime ();
    else
      this.lastUpdateTime = Double.NEGATIVE_INFINITY;
    this.pendingNotifications.clear ();
    this.pendingNotificationsLocked = true;
    this.firingPendingNotifications = false;
    this.postNotificationActions.clear ();
    resetEntitySubClass ();
    clearAndUnlockPendingNotifications ();
    addPendingNotification (SimEntitySimpleEventType.RESET, new SimEntityEvent.Reset (this, this.lastUpdateTime));
    fireAndLockPendingNotifications ();
  }
  
  private boolean ignoreEventListReset = false;
  
  @Override
  public final boolean isIgnoreEventListReset ()
  {
    return this.ignoreEventListReset;
  }
  
  @Override
  public final void setIgnoreEventListReset (final boolean ignoreEventListReset)
  {
    this.ignoreEventListReset = ignoreEventListReset;
  }
  
  /** Calls {@link #resetEntity}, unless this entity ignores event-list reset notifications.
   * 
   * <p>
   * This method is <code>final</code>; use {@link #resetEntitySubClass} to override/augment behavior.
   * 
   * @see SimEntity#isIgnoreEventListReset
   * @see SimEntity#setIgnoreEventListReset
   * 
   */
  @Override
  public final void notifyEventListReset (final SimEventList eventList)
  {
    if (! this.ignoreEventListReset)
      resetEntity ();
  }

  /** Notifies all listeners of a reset of this {@link SimEntity}.
   *
   * @param job Unused.
   *
   * @see SimEntityListener#notifyResetEntity
   * 
   */
  private void fireReset (final SimEntityEvent event)
  {
    if (event == null || ! (event instanceof SimEntityEvent.Reset))
      throw new IllegalArgumentException ();
    for (SimEntityListener l : this.simEntityListeners)
      l.notifyResetEntity (this);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE[OPERATION/NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** The last update time of this entity.
   * 
   */
  private double lastUpdateTime = Double.NEGATIVE_INFINITY;

  @Override
  public final double getLastUpdateTime ()
  {
    return this.lastUpdateTime;
  }
  
  /** Updates this entity.
   * 
   * <p>
   * This final implementation invokes the pre-event hooks (always),
   * and, if needed (i.e., we have a <i>true</i> update), invokes the pre-update hooks,
   * notifies the entity listeners, and updates its internal time (in that order!).
   * 
   * @see #getLastUpdateTime
   * @see #registerPreEventHook
   * @see #registerPreUpdateHook
   * @see SimEntityListener#notifyUpdate
   * @see #fireUpdate
   * 
   */
  @Override
  public final void update (final double time)
  {
    if (time < this.lastUpdateTime)
    {
      LOGGER.log (Level.SEVERE, "Update in the past on {0}: {1} < {2}!", new Object[]{this, time, this.lastUpdateTime});
      throw new IllegalStateException ("update in the past: " + time + " < " + this.lastUpdateTime + "!");
    }
    if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
      for (final DoubleConsumer preEventHook : this.preEventHooksAsArray)
        preEventHook.accept (time);
    else
      for (final DoubleConsumer preEventHook : this.preEventHooks)
        preEventHook.accept (time);
    if (Double.isInfinite (time) || time > this.lastUpdateTime)
    {
      if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
        for (final DoubleConsumer preUpdateHook : this.preUpdateHooksAsArray)
          preUpdateHook.accept (time);
      else
        for (final DoubleConsumer preUpdateHook : this.preUpdateHooks)
          preUpdateHook.accept (time);
      fireUpdate (time);
      this.lastUpdateTime = time;
    }
  }

  private final Set<DoubleConsumer> preUpdateHooks = new LinkedHashSet<> ();
  
  private DoubleConsumer[] preUpdateHooksAsArray = new DoubleConsumer[0];
  
  /** Registers a pre-update hook (for sub-class use only).
   * 
   * <p>
   * A pre-update hook is a {@link DoubleConsumer} (typically, a method reference)
   * that is invoked by {@link #update} <i>before</i> anything else (i.c., notifying listeners)
   * except pre-event notifications
   * and <i>if indeed there is an update</i> (in view of the elapsed time since the last update).
   * It allows sub-class implementations to update internal administration as part of the update,
   * and gives them access to the "old time", i.e.,, the time of the previous update,
   * through {@link #getLastUpdateTime} (before it is overwritten by this method {@link #update}).
   * The hook should <i>never</i> initiate state-change events or notify listeners.
   * 
   * <p>
   * The argument passed to the {@link DoubleConsumer} is the <i>new</i> time,
   * see {@link #update}, which obviously has not been set yet on the object.
   * The "old" time is available through {@link #getLastUpdateTime}.
   * 
   * @param preUpdateHook The pre-update hook, must be non-{@code null}.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}.
   * 
   * @see #update
   * @see #registerPreEventHook
   * 
   */
  protected final void registerPreUpdateHook (final DoubleConsumer preUpdateHook)
  {
    if (preUpdateHook == null)
      throw new IllegalArgumentException ();
    if (! this.preUpdateHooks.contains (preUpdateHook))
    {
      this.preUpdateHooks.add (preUpdateHook);
      if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
        this.preUpdateHooksAsArray = this.preUpdateHooks.toArray (new DoubleConsumer[this.preUpdateHooks.size ()]);
    }
  }
  
  private final Set<DoubleConsumer> preEventHooks = new LinkedHashSet<> ();
  
  private DoubleConsumer[] preEventHooksAsArray = new DoubleConsumer[0];
  
  /** Registers a pre-event hook (for sub-class use only).
   * 
   * <p>
   * A pre-event hook is a {@link DoubleConsumer} (typically, a method reference)
   * that is invoked by {@link #update} <i>before</i> anything else (i.c., notifying listeners),
   * and <i>even if indeed there is no update</i> (in view of the elapsed time since the last update).
   * It allows sub-class implementations to update internal administration as part of the update,
   * and gives them access to the "old time", i.e., the time of the previous update,
   * through {@link #getLastUpdateTime} (before it is overwritten by this method {@link #update}).
   * The hook should <i>never</i> initiate state-change events or notify listeners.
   * 
   * <p>
   * Pre-event hooks are <i>not</i> invoked upon a reset.
   * 
   * <p>
   * The argument passed to the {@link DoubleConsumer} is the <i>new</i> time,
   * see {@link #update}, which obviously has not been set yet on the object.
   * The "old" time is available through {@link #getLastUpdateTime}.
   * 
   * @param preEventHook The pre-event hook, must be non-{@code null}.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}.
   * 
   * @see #update
   * @see #registerPreUpdateHook
   * 
   */
  protected final void registerPreEventHook (final DoubleConsumer preEventHook)
  {
    if (preEventHook == null)
      throw new IllegalArgumentException ();
    if (! this.preEventHooks.contains (preEventHook))
    {
      this.preEventHooks.add (preEventHook);
      if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
        this.preEventHooksAsArray = this.preEventHooks.toArray (new DoubleConsumer[this.preEventHooks.size ()]);
    }
  }
  
  /** Notifies all listeners upon an immediate upcoming update at this entity.
   *
   * @param time The current time, which has not been set yet on this entity.
   *
   * @see SimEntityListener#notifyUpdate
   * 
   */
  private void fireUpdate (final double time)
  {
    if (AbstractSimEntity.USE_ARRAY_OPTIMIZATION)
      for (final SimEntityListener l: this.simEntityListenersAsArray)
        l.notifyUpdate (time, this);
    else
      for (SimEntityListener l : getSimEntityListeners ())
        l.notifyUpdate (time, this);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
