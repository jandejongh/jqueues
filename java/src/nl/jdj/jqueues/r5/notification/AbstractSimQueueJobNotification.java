package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueJobNotification;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueueJobNotification}.
 *
 */
public abstract class AbstractSimQueueJobNotification
extends AbstractSimQueueNotification
implements SimQueueJobNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Constructs a new notification.
   * 
   * @param name  The (initial) name of the notification, may be {@code null}.
   * @param queue The {@link SimQueue}, non-{@code null}.
   * @param job   The {@link SimJob}, non-{@code null}.
   * @param time  The time of the notification.
   * 
   * @throws IllegalArgumentException If the queue or job is {@code null}.
   * 
   */
  protected AbstractSimQueueJobNotification (final String name, final SimQueue queue, final SimJob job, final double time)
  {
    super (name, queue, time);
    if (job == null)
      throw new IllegalArgumentException ();
    this.job = job;
  }
  
  /** Constructs a new notification with {@code null} name.
   * 
   * @param queue The {@link SimQueue}, non-{@code null}.
   * @param job   The {@link SimJob}, non-{@code null}.
   * @param time  The time of the notification.
   * 
   * @throws IllegalArgumentException If the queue or job is {@code null}.
   * 
   */
  protected AbstractSimQueueJobNotification (final SimQueue queue, final SimJob job, final double time)
  {
    super (queue, time);
    if (job == null)
      throw new IllegalArgumentException ();
    this.job = job;
  }
  
  /** Constructs a new notification with time taken from the event list of the queue.
   * 
   * @param name  The (initial) name of the notification, may be {@code null}.
   * @param queue The {@link SimQueue}, non-{@code null}.
   * @param job   The {@link SimJob}, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue or job is {@code null} or the queue has {@code null} event list.
   * 
   * @see SimEntity#getEventList
   * @see SimEventList#getTime
   * 
   */
  protected AbstractSimQueueJobNotification (final String name, final SimQueue queue, final SimJob job)
  {
    super (name, queue);
    if (job == null)
      throw new IllegalArgumentException ();
    this.job = job;
  }
  
  /** Constructs a new notification with {@code null} name and time taken from the event list of the queue.
   * 
   * @param queue The {@link SimEntity}, non-{@code null}.
   * @param job   The {@link SimJob}, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue or job is {@code null} or the queue has {@code null} event list.
   * 
   */
  protected AbstractSimQueueJobNotification (final SimQueue queue, final SimJob job)
  {
    super (queue);
    if (job == null)
      throw new IllegalArgumentException ();
    this.job = job;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final SimJob job;
  
  @Override
  public final SimJob getJob ()
  {
    return this.job;
  }
  
}
