package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityNotification;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimEntityNotification}.
 *
 */
public abstract class AbstractSimEntityNotification
implements SimEntityNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Constructs a new notification (main constructor).
   * 
   * @param name   The (initial) name of the notification, may be {@code null}.
   * @param entity The {@link SimEntity}, non-{@code null}.
   * @param time   The time of the notification.
   * 
   * @throws IllegalArgumentException If the entity is {@code null}.
   * 
   */
  protected AbstractSimEntityNotification (final String name, final SimEntity entity, final double time)
  {
    if (entity == null)
      throw new IllegalArgumentException ();
    this.entity = entity;
    this.time = time;
    this.name = name;
  }
  
  /** Constructs a new notification with {@code null} name.
   * 
   * @param entity The {@link SimEntity}, non-{@code null}.
   * @param time   The time of the notification.
   * 
   * @throws IllegalArgumentException If the entity is {@code null}.
   * 
   */
  protected AbstractSimEntityNotification (final SimEntity entity, final double time)
  {
    this (null, entity, time);
  }
  
  /** Gets the time on the event list of a {@link SimEntity}.
   * 
   * <p>
   * Static method used in some of the constructors to convert a {@link NullPointerException}
   * into an {@link IllegalArgumentException} in case we cannot obtain the time on the event list from a {@link SimEntity}.
   * 
   * @param entity The entity, non-{@code null} with non-{@code null} event list.
   * 
   * @return The time on the event-list underlying the supplied {@link SimEntity}.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or has {@code null} event list.
   * 
   * @see SimEntity#getEventList
   * @see SimEventList#getTime
   * 
   */
  protected static double getTimeFromEventList (final SimEntity entity)
  {
    if (entity == null || entity.getEventList () == null)
      throw new IllegalArgumentException ();
    return entity.getEventList ().getTime ();
  }
  
  /** Constructs a new notification with time taken from the event list of the entity.
   * 
   * @param name   The (initial) name of the notification, may be {@code null}.
   * @param entity The {@link SimEntity}, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or has {@code null} event list.
   * 
   * @see SimEntity#getEventList
   * @see SimEventList#getTime
   * 
   */
  protected AbstractSimEntityNotification (final String name, final SimEntity entity)
  {
    this (name, entity, getTimeFromEventList (entity));
  }
  
  /** Constructs a new notification with {@code null} name and time taken from the event list of the entity.
   * 
   * @param entity The {@link SimEntity}, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or has {@code null} event list.
   * 
   * @see SimEntity#getEventList
   * @see SimEventList#getTime
   * 
   */
  protected AbstractSimEntityNotification (final SimEntity entity)
  {
    this (null, entity);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private String name;
  
  @Override
  public final String getName ()
  {
    return this.name;
  }
  
  @Override
  public final void setName (final String name)
  {
    this.name = name;
  }
  
  /** Returns the name of the notification, if non-{@code null}, else invokes super-class method.
   * 
   * @return The name of the notification, if non-{@code null}, else invokes super-class method.
   * 
   */
  @Override
  public String toString ()
  {
    if (this.name != null)
      return this.name;
    else
      return super.toString ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final SimEntity entity;
  
  @Override
  public final SimEntity getEntity ()
  {
    return this.entity;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double time;
  
  @Override
  public final double getTime ()
  {
    return this.time;
  }
  
}
