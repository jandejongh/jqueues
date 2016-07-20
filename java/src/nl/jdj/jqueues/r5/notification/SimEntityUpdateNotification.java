package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimEntity;

/** An update notification.
 *
 */
public class SimEntityUpdateNotification
extends AbstractSimEntityNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** An update notification.
   * 
   * <p>
   * Note that we cannot take the time from the underlying event-list.
   * 
   * @param entity The entity, non-{@code null}.
   * @param time   The time of the notification.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or the entity has {@code null} event list.
   * 
   */
  public SimEntityUpdateNotification (final SimEntity entity, final double time)
  {
    super (entity, time);
    setName ("@" + getTime () + ": UPDATE" + "@" + entity);
  }

}
