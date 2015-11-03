package nl.jdj.jqueues.r4.util.schedule;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A scheduled external event to a queue.
 * 
 * <p>
 * This class only administers the key parameters for the event; it does not actually schedule it.
 * Use the abstract method {@link #schedule} for that.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class QueueExternalEvent<J extends SimJob, Q extends SimQueue>
{
  
  /** The time at which the event occurs.
   * 
   */
  public final double time;
  
  /** The queue at which the event occurs.
   * 
   */
  public final Q queue;
  
  /** The job related to the event (if applicable, may be <code>null</code>).
   * 
   */
  public final J job;

  /** Creates a new scheduled external event to a queue.
   * 
   * @param time  The time at which the event occurs.
   * @param queue The queue at which the event occurs.
   * @param job   The job related to the event (if applicable, may be <code>null</code>).
   * 
   * @throws IllegalArgumentException If <code>queue == null</code>.
   * 
   */
  protected QueueExternalEvent (final double time, final Q queue, final J job)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    this.time = time;
    this.queue = queue;
    this.job = job;
  }
  
  /** Schedules the event onto given event list.
   * 
   * <p>
   * Note that implementation do no protect against repeated invocations of this method with resetting the event list.
   * 
   * @param eventList The event list, must be non-<code>null</code>.
   * 
   * @throws IllegalArgumentException If <code>eventList == null</code>.
   * 
   */
  public abstract void schedule (final SimEventList eventList);
  
}
