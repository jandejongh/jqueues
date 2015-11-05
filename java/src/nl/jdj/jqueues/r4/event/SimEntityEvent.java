package nl.jdj.jqueues.r4.event;

import nl.jdj.jqueues.r4.SimEntity;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** A {@link SimEvent} for a {@link SimEntity} (queue, job, or other) operation.
 * 
 * <p>
 * This class only administers the key parameters for the event; it does not actually schedule it.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class SimEntityEvent<J extends SimJob, Q extends SimQueue>
extends SimEvent<J>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new event for a queue.
   * 
   * @param name   The (optional) name of the event, may be  <code>null</code>.
   * @param time   The time at which the event occurs.
   * @param queue  The queue at which the event occurs, non-{@code null}.
   * @param job    The job related to the event (if applicable, may be <code>null</code>).
   * @param action The {@link SimEventAction} to take; may be {@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  protected SimEntityEvent (final String name, final double time, final Q queue, final J job, final SimEventAction<J> action)
  {
    super (name, time, job, action);
    if (queue == null)
      throw new IllegalArgumentException ();
    this.queue = queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queue at which the event occurs, non-{@code null}.
   * 
   */
  private final Q queue;
  
  /** Gets the queue at which the event occurs.
   * 
   * @return The queue at which the event occurs, non-{@code null}.
   * 
   */
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the job (if applicable) to which the event applies.
   * 
   * <p>
   * The job is stored as user object on the {@link SimEvent}.
   * 
   * @return The job (if applicable) to which the event applies, may be {@code null}.
   * 
   * @see SimEvent#getObject
   * 
   */
  public final J getJob ()
  {
    return getObject ();
  }
  
}
