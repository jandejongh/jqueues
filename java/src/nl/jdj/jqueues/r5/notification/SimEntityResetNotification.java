package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimEntity;

/** A reset notification.
 *
 */
public class SimEntityResetNotification
extends AbstractSimEntityNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A reset notification with time taken from the event list of the queue.
   * 
   * @param entity The entity, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or the entity has {@code null} event list.
   * 
   */
  public SimEntityResetNotification (final SimEntity entity)
  {
    super (entity);
    setName ("@" + getTime () + ": RESET" + "@" + entity);
  }

}
