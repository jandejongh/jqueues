package nl.jdj.jqueues.r5;

import nl.jdj.jqueues.r5.notification.AbstractSimEntityNotification;

/** A (named) notification from a {@link SimEntity}.
 * 
 * @see SimEntity
 * @see AbstractSimEntityNotification
 *
 */
public interface SimEntityNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the name of the notification.
   * 
   * @return The name of the notification (may be {@code null}).
   * 
   */
  String getName ();
  
  /** Sets the name of the notification.
   * 
   * @param name The new name of the notification (may be {@code null}).
   * 
   */
  void setName (String name);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the {@link SimEntity} that issued the notification.
   * 
   * @return The {@link SimEntity} that issued the notification.
   *
   */
  SimEntity getEntity ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the time the notification was issued.
   * 
   * @return The time the notification was issued.
   * 
   */
  double getTime ();
  
}
