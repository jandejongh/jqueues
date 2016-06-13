package nl.jdj.jqueues.r5.entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.DoubleConsumer;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueueBase;
import nl.jdj.jsimulation.r5.SimEventList;

/** An implementation of the common part of a {@link SimJob} and a {@link SimQueue}.
 * 
 * <p>
 * In addition to the {@link SimEntity} requirements, this class implements event notifications,
 * with special treatment for {@link SimQueue} and {@link SimJob} main operations,
 * in the sense that a {@link SimQueue} will automatically notify listeners on {@link SimJob}s about the main operations
 * (arrival, start, drop, revocation, departure).
 * 
 * <p>
 * For a more complete (though still partial) implementations of jobs, see {@link AbstractSimJob}.
 * <p>
 * For more complete (though still partial) implementations of queues,
 * see {@link AbstractSimQueueBase} and {@link AbstractSimQueue}.
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
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates a new {@link SimEntity} with given event list and name.
   * 
   * @param eventList The event list to use, may be {@code null}.
   * @param name The name of the entity, may be <code>null</code>.
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
    if (this.eventList != null)
      this.eventList.addListener (this);
    if ((this instanceof SimQueue) && (this instanceof SimJob))
      throw new IllegalArgumentException ();
  }
    
  /** Creates a new {@link SimEntity} with given event list and <code>null</code> (initial) name.
   * 
   * @param eventList The event list, may be {@code null}.
   * 
   * @see #getEventList
   * @see #setName
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
   * As it is essential to issue a <i>single</i> {@link SimEntityListener#notifyResetEntity} <i>after</i> the state has
   * been set to its initial setting, the implementation of {@link SimEntity#resetEntity} has been made final
   * in order to ensure this.
   * 
   * @see SimEntity#resetEntity
   * 
   */
  protected void resetEntitySubClass ()
  {
    /* EMPTY */
  }
  
  /** Calls {@link #resetEntitySubClass} and {@link #fireResetEntity}.
   * 
   * <p>
   * This method is <code>final</code>; use {@link #resetEntitySubClass} to override/augment behavior.
   * 
   * @see #resetEntitySubClass
   * @see #notifyEventListReset
   * 
   */
  @Override
  public final void resetEntity ()
  {
    this.lastUpdateTime = Double.NEGATIVE_INFINITY;
    resetEntitySubClass ();
    fireResetEntity (this);
  }
  
  /** Calls {@link #resetEntity}.
   * 
   * <p>
   * This method is <code>final</code>; use {@link #resetEntitySubClass} to override/augment behavior.
   * 
   */
  @Override
  public final void notifyEventListReset (final SimEventList eventList)
  {
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

  /** Gets the time of the last update of this entity.
   * 
   * @return The time of the last update of this entity.
   * 
   * @see #update
   * @see SimEntityListener#notifyUpdate
   * 
   */
  public final double getLastUpdateTime ()
  {
    return this.lastUpdateTime;
  }
  
  /** Updates this entity (for internal use).
   * 
   * <p>
   * For a precise definition of an update of an entity, refer to {@link SimEntityListener#notifyUpdate}.
   * 
   * <p>
   * This method should <i>not</i> be called from user code, as it <i>must</i> be immediately followed by an imminent state change
   * of this entity.
   * 
   * <p>
   * This final implementation invokes the update hooks,
   * notifies the entity listeners, and updates its internal time (in that order!).
   * 
   * @param time The time of the update (i.c., the current time).
   * 
   * @see #registerUpdateHook
   * @see SimEntityListener#notifyUpdate
   * @see #fireUpdate
   * 
   */
  protected final void update (final double time)
  {
    if (time < this.lastUpdateTime)
      throw new IllegalStateException ();
    if (Double.isInfinite (time) || time > this.lastUpdateTime)
    {
      for (final DoubleConsumer updateHook : this.updateHooks)
        updateHook.accept (time);
      fireUpdate (time);
      this.lastUpdateTime = time;
    }
  }

  private final Set<DoubleConsumer> updateHooks = new LinkedHashSet<> ();
  
  /** Registers an update hook (for sub-class use only).
   * 
   * <p>
   * An update hook is a {@link DoubleConsumer} (typically, a method reference)
   * that is invoked by {@link #update} before anything else (i.c., notifying listeners).
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
   * @param updateHook The update hook, must be non-{@code null}.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}.
   * 
   * @see #update
   * 
   */
  protected final void registerUpdateHook (final DoubleConsumer updateHook)
  {
    if (updateHook == null)
      throw new IllegalArgumentException ();
    this.updateHooks.add (updateHook);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE CHANGED
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Internal notification that the entity's state has changed, requiring notifications.
   * 
   * @param time The time of the state change (i.c., the current time).
   * 
   * @see SimEntityListener#notifyStateChanged
   * @see #fireStateChanged
   * 
   */
  protected final void stateChanged (final double time)
  {
    if (time < this.lastUpdateTime)
      throw new IllegalStateException ();
    fireStateChanged (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a reset.
   *
   * @param entity The {@link SimEntity} that received the reset.
   *
   * @see SimEntityListener#notifyResetEntity
   * 
   */
  protected final void fireResetEntity (final SimEntity<J, Q> entity)
  {
    for (SimEntityListener<J, Q> l : this.simEntityListeners)
      l.notifyResetEntity (entity);
  }
  
  /** Notifies all listeners upon an immediate upcoming update at this entity.
   *
   * @param time The current time, which has not been set yet on this entity.
   *
   * @see SimEntityListener#notifyUpdate
   * 
   */
  protected final void fireUpdate (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyUpdate (time, this);
  }
  
  /** Notifies all listeners upon an immediate upcoming update at this entity.
   *
   * @param time The current time, which has not been set yet on this entity.
   *
   * @see SimEntityListener#notifyStateChanged
   * 
   */
  protected final void fireStateChanged (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyStateChanged (time, this);
  }
  
  /** Notifies all listeners of a job arrival at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param time  The current time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimEntityListener#notifyArrival
   * 
   */
  protected final void fireArrival (final double time, final J job, final Q queue)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      l.notifyArrival (time, job, queue);
    if ((this instanceof SimQueue) && (queue == this) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyArrival (time, job, queue);
  }
  
  /** Notifies all listeners of a job starting at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param time  The current time.
   * @param job   The job.
   * @param queue The queue.
   * 
   * @see SimEntityListener#notifyStart
   * 
   */
  protected final void fireStart (final double time, final J job, final Q queue)
  {
    for (SimEntityListener<J, Q> l : this.simEntityListeners)
      l.notifyStart (time, job, queue);
    if ((this instanceof SimQueue) && (queue == this) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyStart (time, job, queue);
  }
  
  /** Notifies all listeners of a job drop at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param time  The current time.
   * @param job   The job.
   * @param queue The queue.
   *
   * @see SimEntityListener#notifyDrop
   * 
   */
  protected final void fireDrop (final double time, final J job, final Q queue)
  {
    for (SimEntityListener<J, Q> l : this.simEntityListeners)
      l.notifyDrop (time, job, queue);
    if ((this instanceof SimQueue) && (queue == this) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyDrop (time, job, queue);
  }
  
  /** Notifies all listeners of a successful job revocation at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param time  The current time.
   * @param job   The job.
   * @param queue The queue.
   *
   * @see SimEntityListener#notifyRevocation
   * 
   */
  protected final void fireRevocation (final double time, final J job, final Q queue)
  {
    for (SimEntityListener<J, Q> l : this.simEntityListeners)
      l.notifyRevocation (time, job, queue);
    if ((this instanceof SimQueue) && (queue == this) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyRevocation (time, job, queue);
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
  protected final void fireDeparture (final double time, final J job, final Q queue)
  {
    for (SimEntityListener<J, Q> l : this.simEntityListeners)
      l.notifyDeparture (time, job, queue);
    if ((this instanceof SimQueue) && (queue == this) && (job != null))
      for (SimEntityListener<J, Q> l : ((SimJob<J, Q>) job).getSimEntityListeners ())
        l.notifyDeparture (time, job, queue);
  }
  
}
