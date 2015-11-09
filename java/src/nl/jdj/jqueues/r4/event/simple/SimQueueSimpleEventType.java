package nl.jdj.jqueues.r4.event.simple;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.SimQueueAccessVacationEvent;
import nl.jdj.jqueues.r4.event.SimQueueServerAccessCreditsEvent;

/** A simple representation of a {@link SimEntityEvent} specific to {@link SimJob}s.
 * 
 */
public interface SimQueueSimpleEventType
extends SimEntitySimpleEventType
{

  /** The start or end of a queue-access vacation.
   * 
   * @see SimQueueAccessVacationEvent
   * 
   */
  public static Member QUEUE_ACCESS_VACATION = new Member ("QUEUE_ACCESS_VACATION");
  
  /** The setting of the remaining server-access credits.
   * 
   * @see SimQueueServerAccessCreditsEvent
   * 
   */
  public static Member SERVER_ACCESS_CREDITS = new Member ("SERVER_ACCESS_CREDITS");
  
}
