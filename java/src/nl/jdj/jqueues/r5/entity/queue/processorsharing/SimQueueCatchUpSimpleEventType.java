package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;

/** A simple representation of a {@link SimEntityEvent} specific to a "catch up" in (e.g.) {@link CUPS}s.
 * 
 * @see CUPS
 * 
 */
public interface SimQueueCatchUpSimpleEventType
extends SimQueueSimpleEventType
{

  /** A "catch up" in (e.g.) {@link CUPS}.
   * 
   * @see SimQueueCatchUpEvent
   * 
   */
  public static Member CATCH_UP = new Member ("CATCH UP");
  
}
