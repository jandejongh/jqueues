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
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimEntityOperation;
import nl.jdj.jqueues.r5.SimEntityOperationUtils;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** An implementation of the common part of a {@link SimJob} and a {@link SimQueue}.
 * 
 * <p>
 * In addition to the {@link SimEntity} requirements, this class implements event notifications,
 * with special treatment for {@link SimQueue} and {@link SimJob} main operations,
 * in the sense that a {@link SimQueue} will automatically notify listeners on {@link SimJob}s about the main operations
 * (arrival, start, drop, revocation, departure).
 * To that extent, this class implements a registration mechanism for event notifications
 * (as subclasses may introduce new event types, and listener extensions).
 * 
 * <p>
 * For a more complete (though still partial) implementations of jobs, see {@link AbstractSimJob}.
 * 
 * <p>
 * For more complete (though still partial) implementations of queues, see {@link AbstractSimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see SimJob
 * @see SimQueue
 * 
 */
public abstract class AbstractSimEntity<J extends SimJob, Q extends SimQueue>
implements SimEntity<J, Q>
{
  
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
   * @param eventList The event list to use, may be {@code null}.
   * @param name      The name of the entity, may be <code>null</code>.
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If this object is both a {@link SimQueue} <i>and</i> a {@link SimJob}.
   * 
   */
  public AbstractSimEntity (final SimEventList eventList, final String name)
  {
    this.eventList = eventList;
    setName (name);
    registerOperation (SimEntityOperationUtils.ResetOperation.getInstance ());
    registerOperation (SimEntityOperationUtils.UpdateOperation.getInstance ());
    registerNotificationType (SimEntitySimpleEventType.RESET, this::fireReset);
    registerNotificationType (SimEntitySimpleEventType.ARRIVAL, this::fireArrival);
    registerNotificationType (SimEntitySimpleEventType.DROP, this::fireDrop);
    registerNotificationType (SimEntitySimpleEventType.REVOCATION, this::fireRevocation);
    registerNotificationType (SimEntitySimpleEventType.AUTO_REVOCATION, this::fireAutoRevocation);
    registerNotificationType (SimEntitySimpleEventType.START, this::fireStart);
    registerNotificationType (SimEntitySimpleEventType.DEPARTURE, this::fireDeparture);
    if (this.eventList != null)
      this.eventList.addListener (this);
    if ((this instanceof SimQueue) && (this instanceof SimJob))
      throw new IllegalArgumentException ("Trying to instantiate a SimEntity that is both a SimJob and a SimQueue!");
  }
    
  /** Creates a new {@link SimEntity} with given event list and <code>null</code> (initial) name.
   * 
   * @param eventList The event list, may be {@code null}.
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If this object is both a {@link SimQueue} <i>and</i> a {@link SimJob}.
   * 
   */
  public AbstractSimEntity (final SimEventList eventList)
  {
    this (eventList, null);
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
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The {@link SimEntityListener}s of this simulation entity.
   * 
   */
  private final Set<SimEntityListener<J, Q>> simEntityListeners = new LinkedHashSet<>();

  @Override
  public final void registerSimEntityListener (final SimEntityListener<J, Q> listener)
  {
    if (listener != null)
      this.simEntityListeners.add (listener);
  }

  @Override
  public final void unregisterSimEntityListener (final SimEntityListener<J, Q> listener)
  {
    this.simEntityListeners.remove (listener);
  }  

  @Override
  public final Set<SimEntityListener<J, Q>> getSimEntityListeners ()
  {
    return Collections.unmodifiableSet (this.simEntityListeners);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME, toString
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
  public final String toString ()
  {
    if (this.name != null)
      return this.name;
    else
      return toStringDefault ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OPERATIONS REGISTRATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimEntityOperation> registeredOperations = new LinkedHashSet<> ();
  
  private final Map<SimEntityOperation, SimEntityOperation> delegatedOperations = new LinkedHashMap<> ();
  
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
  
  /** Registers a {@link SimEntityOperation} at this entity, but delegate it to another operation.
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
  // UNKNOWN OPERATION POLICY
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
  // DO OPERATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public final
  <O   extends SimEntityOperation,
   Req extends SimEntityOperation.Request,
   Rep extends SimEntityOperation.Reply>
  Rep doOperation (double time, Req request)
  {
    if (request == null)
      throw new IllegalArgumentException ();
    if (request.getOperation () != SimEntityOperationUtils.ResetOperation.getInstance ()
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
      return (Rep) this.delegatedOperations.get (request.getOperation ()).doOperation (time, this, request);
    else
      return (Rep) request.getOperation ().doOperation (time, this, request);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET ENTITY
  // SimEventListResetListener
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
   * @see SimEntity#resetEntity
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
    addPendingNotification (SimEntitySimpleEventType.RESET, null);
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
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
    for (final DoubleConsumer preEventHook : this.preEventHooks)
      preEventHook.accept (time);
    if (Double.isInfinite (time) || time > this.lastUpdateTime)
    {
      for (final DoubleConsumer preUpdateHook : this.preUpdateHooks)
        preUpdateHook.accept (time);
      fireUpdate (time);
      this.lastUpdateTime = time;
    }
  }

  private final Set<DoubleConsumer> preUpdateHooks = new LinkedHashSet<> ();
  
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
    this.preUpdateHooks.add (preUpdateHook);
  }
  
  private final Set<DoubleConsumer> preEventHooks = new LinkedHashSet<> ();
  
  /** Registers a pre-event hook (for sub-class use only).
   * 
   * <p>
   * A pre-event hook is a {@link DoubleConsumer} (typically, a method reference)
   * that is invoked by {@link #update} <i>before</i> anything else (i.c., notifying listeners),
   * and <i>even if indeed there is no update</i> (in view of the elapsed time since the last update).
   * It allows sub-class implementations to update internal administration as part of the update,
   * and gives them access to the "old time", i.e.,, the time of the previous update,
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
    this.preEventHooks.add (preEventHook);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a reset of this {@link SimEntity}.
   *
   * @param job Unused.
   *
   * @see SimEntityListener#notifyResetEntity
   * 
   */
  private void fireReset (final SimJob job)
  {
    for (SimEntityListener<J, Q> l : this.simEntityListeners)
      l.notifyResetEntity (this);
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
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyUpdate (time, this);
  }
  
  /** Notifies all listeners of a job arrival at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   * 
   * @see SimEntityListener#notifyArrival
   * 
   */
  private void fireArrival (final J job)
  {
    final double time = getLastUpdateTime ();
    final Q queue;
    if (this instanceof SimQueue)
      queue = (Q) this;
    else
      queue = (Q) job.getQueue ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyArrival (time, job, queue);
    if ((this instanceof SimQueue) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyArrival (time, job, queue);
  }
  
  /** Notifies all listeners of a job drop at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   *
   * @see SimEntityListener#notifyDrop
   * 
   */
  private void fireDrop (final J job)
  {
    final double time = getLastUpdateTime ();
    final Q queue;
    if (this instanceof SimQueue)
      queue = (Q) this;
    else
      queue = (Q) job.getQueue ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyDrop (time, job, queue);
    if ((this instanceof SimQueue) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyDrop (time, job, queue);
  }
  
  /** Notifies all listeners of a successful job revocation at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   *
   * @see SimEntityListener#notifyRevocation
   * 
   */
  private void fireRevocation (final J job)
  {
    final double time = getLastUpdateTime ();
    final Q queue;
    if (this instanceof SimQueue)
      queue = (Q) this;
    else
      queue = (Q) job.getQueue ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyRevocation (time, job, queue);
    if ((this instanceof SimQueue) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyRevocation (time, job, queue);
  }
  
  /** Notifies all listeners of a job auto-revocation at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   *
   * @see SimEntityListener#notifyAutoRevocation
   * 
   */
  private void fireAutoRevocation (final J job)
  {
    final double time = getLastUpdateTime ();
    final Q queue;
    if (this instanceof SimQueue)
      queue = (Q) this;
    else
      queue = (Q) job.getQueue ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyAutoRevocation (time, job, queue);
    if ((this instanceof SimQueue) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyAutoRevocation (time, job, queue);
  }
  
  /** Notifies all listeners of a job starting at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   * 
   * @see SimEntityListener#notifyStart
   * 
   */
  private void fireStart (final J job)
  {
    final double time = getLastUpdateTime ();
    final Q queue;
    if (this instanceof SimQueue)
      queue = (Q) this;
    else
      queue = (Q) job.getQueue ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyStart (time, job, queue);
    if ((this instanceof SimQueue) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyStart (time, job, queue);
  }
  
  /** Notifies all listeners of a job departure at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param time  The current time.
   * @param job   The job.
   * @param queue The queue.
   *
   * @see SimEntityListener#notifyDeparture
   * 
   */
  private void fireDeparture (final J job)
  {
    final double time = getLastUpdateTime ();
    final Q queue;
    if (this instanceof SimQueue)
      queue = (Q) this;
    else
      queue = (Q) job.getQueue ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyDeparture (time, job, queue);
    if ((this instanceof SimQueue) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyDeparture (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UNKNOWN NOTIFICATION-TYPE POLICY
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
  // NOTIFICATION MAP
  // REGISTERED NOTIFICATION TYPES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A function interface for a notification handler.
   * 
   * @param <J> The type of job supported.
   * 
   */
  @FunctionalInterface
  public interface Notifier<J extends SimJob>
  {
    
    /** Fires the notification to all relevant listeners.
     * 
     * @param job The job, may be {@code null} for non-visit-related notifications (e.g., queue-access vacations).
     * 
     */
    void fire (J job);
    
  }
  
  /** The mapping between a notification type (reset, arrival, departures, etc.) and a {@link Notifier} for it.
   * 
   */
  private final Map<SimEntitySimpleEventType.Member, Notifier<J>> notificationMap = new LinkedHashMap<> ();
  
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
  (final SimEntitySimpleEventType.Member notificationType, final Notifier<J> notifier)
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
  // PRE-NOTIFICATION HOOKS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A functional interface for a hook to be called before notifications are sent (i.e., before a state change is advertised).
   * 
   * <p>
   * A pre-notification hook allows sub-classes to modify the pending notifications (for whatever reason),
   * but it is typically used to automatically detect state-changes that are not explicitly dealt with otherwise.
   * 
   * @param <J> The type of job supported.
   * 
   */
  @FunctionalInterface
  public interface PreNotificationHook<J extends SimJob>
  {

    /** Invokes the hook.
     * 
     * @param pendingNotifications The pending notifications (may be changed).
     * 
     */
    void hook (List<Map<SimEntitySimpleEventType.Member, J>> pendingNotifications);

  }
  
  /** The registered pre-notification hooks.
   * 
   */
  private final Set<PreNotificationHook<J>> preNotificationHooks = new LinkedHashSet<> ();

  /** Registers a pre-notification hook.
   * 
   * @param preNotificationHook The hook, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the hook is {@code null} or already registered.
   * 
   */  
  protected final void registerPreNotificationHook (final PreNotificationHook<J> preNotificationHook)
  {
    if (preNotificationHook == null || this.preNotificationHooks.contains (preNotificationHook))
      throw new IllegalArgumentException ();
    this.preNotificationHooks.add (preNotificationHook);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // POST-NOTIFICATION ACTIONS
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
  // PENDING NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The pending notifications for this entity, mapped onto the associated job (if applicable).
   * 
   */
  private final List<Map<SimEntitySimpleEventType.Member, J>> pendingNotifications = new ArrayList<> ();
  
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
   * @throws IllegalArgumentException If the notification is {@code null}, if the map is of illegal size (other than unity),
   *                                  or has a {@code null} key, if the notification is already present or has time different
   *                                  from the pending-notifications time,
   *                                  if this entity is locked for pending notifications mutations,
   *                                  or is currently firing notifications.
   * 
   */
  protected final void addPendingNotification (final Map<SimEntitySimpleEventType.Member, J> notification)
  {
    if (notification == null
    ||  notification.size () != 1
    ||  notification.containsKey (null)
    || this.pendingNotifications.contains (notification)
    || ((! this.pendingNotifications.isEmpty ()) && getLastUpdateTime () != this.pendingNotificationsTime)
    || this.pendingNotificationsLocked
    || this.firingPendingNotifications)
      throw new IllegalArgumentException ();
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
   * @param job              The associated job, if applicable; may be {@code null} for non-visit related notifications.
   * 
   * @throws IllegalArgumentException If the notification type is {@code null} or if
   *                                  {@link #addPendingNotification(java.util.Map)} encounters an error in the arguments.
   * 
   */
  protected final void addPendingNotification (final SimEntitySimpleEventType.Member notificationType, final J job)
  {
    if (notificationType == null)
      throw new IllegalArgumentException ();
    addPendingNotification (Collections.singletonMap (notificationType, job));
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
  private boolean containsPendingNotification (final Map<SimEntitySimpleEventType.Member, J> notification)
  {
    if (notification == null
    ||  notification.size () != 1
    ||  notification.containsKey (null))
      throw new IllegalArgumentException ();
    final SimEntitySimpleEventType.Member eventType = notification.keySet ().iterator ().next ();
    final J job = notification.values ().iterator ().next ();
    for (final Map<SimEntitySimpleEventType.Member, J> member : this.pendingNotifications)
      if (member.keySet ().iterator ().next () == eventType
      &&  member.values ().iterator ().next () == job)
        return true;
    return false;
  }
  
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
   * Fires, if present, pre-notification hooks and post-notification actions.
   * The post-notification actions are cleared.
   * 
   * @throws IllegalStateException If the time of pending notifications is not equal to {@link #getLastUpdateTime},
   *                               if this entity is currently firing notifications (already).
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
    final double time = getLastUpdateTime ();
    // Respect policy for unknown notification types.
    for (final Map<SimEntitySimpleEventType.Member, J> notification : this.pendingNotifications)
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
    for (final Map<SimEntitySimpleEventType.Member, J> notification : this.pendingNotifications)
    {
      final SimEntitySimpleEventType.Member notificationType = notification.keySet ().iterator ().next ();
      final J job = notification.values ().iterator ().next ();
      if (this.notificationMap.containsKey (notificationType)
      &&  this.notificationMap.get (notificationType) != null)
        this.notificationMap.get (notificationType).fire (job);
    }
    if (this instanceof SimQueue)
    {
      final Map<J, List<Map<SimEntitySimpleEventType.Member, J>>> jobNotifications = new LinkedHashMap<> ();
      for (final Map<SimEntitySimpleEventType.Member, J> notification : this.pendingNotifications)
      {
        final J job = notification.values ().iterator ().next ();
        if (job != null && (job instanceof AbstractSimEntity))
        {
          if (! jobNotifications.containsKey (job))
            jobNotifications.put (job, new ArrayList<> ());
          jobNotifications.get (job).add (notification);
        }
      }
      for (final Map.Entry<J, List<Map<SimEntitySimpleEventType.Member, J>>> entry : jobNotifications.entrySet ())
      {
        final J job = entry.getKey ();
        final List<Map<SimEntitySimpleEventType.Member, J>> notifications = entry.getValue ();
        final Set<SimEntityListener> listeners = job.getSimEntityListeners ();
        for (final SimEntityListener l : listeners)
          l.notifyStateChanged (time, job, notifications);
      }
    }
    this.firingPendingNotifications = false;
    while (! this.postNotificationActions.isEmpty ())
      this.postNotificationActions.remove (0).execute ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
