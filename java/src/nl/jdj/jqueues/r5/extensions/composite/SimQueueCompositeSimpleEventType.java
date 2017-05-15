package nl.jdj.jqueues.r5.extensions.composite;

import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;

/** A simple representation of a {@link SimEntityEvent} specific to {@link SimQueueComposite}s.
 * 
 */
public interface SimQueueCompositeSimpleEventType
extends SimQueueSimpleEventType
{

  /** The pseudo-arrival of a job at a composite queue.
   * 
   */
  public static Member PSEUDO_ARRIVAL = new Member ("PSEUDO_ARRIVAL");
  
  /** The pseudo-drop of a job at a composite queue.
   * 
   */
  public static Member PSEUDO_DROP = new Member ("PSEUDO_DROP");
  
  /** The pseudo-revocation of a job at a composite queue.
   * 
   */
  public static Member PSEUDO_REVOCATION = new Member ("PSEUDO_REVOCATION");
  
  /** The pseudo-auto-revocation of a job at a composite queue.
   * 
   */
  public static Member PSEUDO_AUTO_REVOCATION = new Member ("PSEUDO_AUTO_REVOCATION");
  
  /** The pseudo-start of a job at a composite queue.
   * 
   */
  public static Member PSEUDO_START = new Member ("PSEUDO_START");
  
  /** The pseudo-departure of a job at a composite queue.
   * 
   */
  public static Member PSEUDO_DEPARTURE = new Member ("PSEUDO_DEPARTURE");
  
}
