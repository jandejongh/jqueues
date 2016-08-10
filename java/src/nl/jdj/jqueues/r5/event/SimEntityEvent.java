package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

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
extends DefaultSimEvent
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new event for a queue.
   * 
   * @param name   The (optional) name of the event, may be  {@code null}.
   * @param time   The time at which the event occurs.
   * @param queue  The queue related to the event (if applicable), may be {@code null}.
   * @param job    The job related to the event (if applicable), may be {@code null}.
   * @param action The {@link SimEventAction} to take; may be {@code null}.
   * 
   */
  protected SimEntityEvent (final String name, final double time, final Q queue, final J job, final SimEventAction<J> action)
  {
    super (name, time, null, action);
    this.queue = queue;
    this.job = job;
  }
  
  /** Creates a copy of this event, but for a different queue.
   * 
   * @param destQueue The new destination queue (the {@link SimQueue} to which the newly created event applies), non-{@code null}.
   * 
   * @return A copy of this event but for given queue.
   * 
   * @throws UnsupportedOperationException If creating a copy for a different queue is not supported by the queue type,
   *                                         for instance, because it is a queue-internal event.
   * @throws IllegalArgumentException      If the argument is {@code null}.
   * 
   */
  public abstract SimEntityEvent<J, Q> copyForQueue (final Q destQueue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Q queue;
  
  /** Gets the queue (if applicable) at which the event occurs.
   * 
   * @return The queue (if applicable) to which the event applies, may be {@code null}.
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
  
  private final J job;
  
  /** Gets the job (if applicable) to which the event applies.
   * 
   * @return The job (if applicable) to which the event applies, may be {@code null}.
   * 
   */
  public final J getJob ()
  {
    return this.job;
  }
  
}
