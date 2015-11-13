package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
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
extends SimEvent
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
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
