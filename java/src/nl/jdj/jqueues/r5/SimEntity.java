package nl.jdj.jqueues.r5;

import java.util.Set;
import nl.jdj.jqueues.r5.entity.AbstractSimEntity;
import nl.jdj.jsimulation.r4.SimEventList;
import nl.jdj.jsimulation.r4.SimEventListResetListener;

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
 * What they share in common is the event list they are attached to, the fact that they have a name,
 * and their obligation to propagate state changes (including the currently visited queue of a job, and
 * the jobs currently visiting a queue).
 * 
 * <p>
 * Objects implementing this interface take care of
 * <ul>
 * <li>holding the underlying event list ({@link SimEventList}), which may be <code>null</code> on non-{@link SimQueue}s,
 * <li>registering as a {@link SimEventListResetListener} to a non-<code>null</code> {@link SimEventList},
 * <li>maintaining listeners ({@link SimEntityListener}),
 * <li>propagating reset events on the {@link SimEventList} to {@link SimEntityListener}s through {@link #resetEntity},
 * <li>naming.
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
   * It may be <code>null</code>, however, for {@link SimQueue}s the underlying event list must be non-<code>null</code>.
   * 
   * <p>
   * If non-<code>null</code>, implementations must register as a {@link SimEventListResetListener},
   * and propagate any reset of the event list through {@link #resetEntity}.
   * 
   * @return The underlying event list of this {@link SimEntity}.
   * 
   */
  public SimEventList getEventList ();

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
  public void registerSimEntityListener (SimEntityListener<J, Q> listener);
  
  /** Unregisters a listener to events related to this entity.
   * 
   * @param listener The listener; ignored if not registered or <code>null</code>.
   * 
   * @see #registerSimEntityListener
   * 
   */
  public void unregisterSimEntityListener (SimEntityListener<J, Q> listener);

  /** Gets the listeners to this entity.
   * 
   * Callers must <i>not</i> attempt to change the returned set.
   * Implementations are encouraged to return read-only views.
   * 
   * @return The listeners to this entity.
   * 
   */
  public Set<SimEntityListener<J, Q>> getSimEntityListeners ();

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
   * @return A default, type-specific name for this {@link SimEntity}.
   * 
   * @see #setName
   * 
   */
  public String toStringDefault ();
  
  /** Sets the name of this {@link SimEntity}, to be returned by subsequent calls to <code>Object.toString ()</code>.
   * 
   * @param name The new name of this job or queue; if non-<code>null</code>, the string will be supplied by subsequent calls
   *               to <code>Object.toString ()</code>; otherwise, the type-specific default will be used for that.
   * 
   * @see #toStringDefault
   * 
   */
  public void setName (String name);
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Puts the entity into its "known" initial state.
   *
   * <p>
   * This method is used in order to restart a simulation.
   * By contract, a {@link SimEntity} must reset if its underlying {@link SimEventList} resets.
   * 
   * Implementations must ensure that only a single {@link SimEntityListener#notifyResetEntity} is invoked <i>after</i>
   * the entity is in the new (valid) state.
   * 
   * @see SimEventListResetListener#notifyEventListReset
   * 
   */
  public void resetEntity ();
  
}
