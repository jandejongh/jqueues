package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;

/** A simple representation of a {@link SimEntityEvent} specific to {@link SimQueueWithGate}s.
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
