package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueNotification;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueueNotification}.
 *
 */
public abstract class AbstractSimQueueNotification
extends AbstractSimEntityNotification
implements SimQueueNotification
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
   * @param time  The time of the notification.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  protected AbstractSimQueueNotification (final String name, final SimQueue queue, final double time)
  {
    super (name, queue, time);
  }
  
  /** Constructs a new notification with {@code null} name.
   * 
   * @param queue The {@link SimQueue}, non-{@code null}.
   * @param time  The time of the notification.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  protected AbstractSimQueueNotification (final SimQueue queue, final double time)
  {
    super (queue, time);
  }
  
  /** Constructs a new notification with time taken from the event list of the queue.
   * 
   * @param name  The (initial) name of the notification, may be {@code null}.
   * @param queue The {@link SimQueue}, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or has {@code null} event list.
   * 
   * @see SimEntity#getEventList
   * @see SimEventList#getTime
   * 
   */
  protected AbstractSimQueueNotification (final String name, final SimQueue queue)
  {
    super (name, queue);
  }
  
  /** Constructs a new notification with {@code null} name and time taken from the event list of the queue.
   * 
   * @param queue The {@link SimEntity}, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or has {@code null} event list.
   * 
   */
  protected AbstractSimQueueNotification (final SimQueue queue)
  {
    super (queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Overridden to make the implementation {@link SimQueueNotification#getQueue} final.
   * 
   * @return The result from {@link #getEntity}, cast to a {@link SimQueue}.
   * 
   */
  @Override
  public final SimQueue getQueue ()
  {
    return SimQueueNotification.super.getQueue ();
  }
  
}
