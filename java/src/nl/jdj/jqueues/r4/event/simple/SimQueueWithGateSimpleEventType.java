package nl.jdj.jqueues.r4.event.simple;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.SimQueueGateEvent;

/** A simple representation of a {@link SimEntityEvent} specific to {@link SimJob}s.
 * 
 */
public interface SimQueueWithGateSimpleEventType
extends SimQueueSimpleEventType
{

  /** The control of the gate of a queue.
   * 
   * @see SimQueueGateEvent
   * 
   */
  public static Member GATE = new Member ("GATE");
  
}
