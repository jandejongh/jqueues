package nl.jdj.jqueues.r5.event.simple;

import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimQueueAccessVacationEvent;
import nl.jdj.jqueues.r5.event.SimQueueServerAccessCreditsEvent;

/** A simple representation of a {@link SimEntityEvent} specific to {@link SimQueue}s.
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
  
  /** The start of a queue-access vacation.
   * 
   * @see SimQueueAccessVacationEvent
   * 
   */
  public static Member QAV_START = new Member ("QAV_START");
  
  /** The end of a queue-access vacation.
   * 
   * @see SimQueueAccessVacationEvent
   * 
   */
  public static Member QAV_END = new Member ("QAV_END");
  
  /** The setting of the remaining server-access credits.
   * 
   * @see SimQueueServerAccessCreditsEvent
   * 
   */
  public static Member SERVER_ACCESS_CREDITS = new Member ("SERVER_ACCESS_CREDITS");
  
  /** The loss of remaining server-access credits.
   * 
   * @see SimQueueServerAccessCreditsEvent
   * 
   */
  public static Member OUT_OF_SAC = new Member ("OUT_OF_SAC");
  
  /** The regain of remaining server-access credits.
   * 
   * @see SimQueueServerAccessCreditsEvent
   * 
   */
  public static Member REGAINED_SAC = new Member ("REGAIN_SAC");
  
  /** The loss of {@code startArmed}.
   * 
   */
  public static Member STA_FALSE = new Member ("STA_FALSE");
  
  /** The gain of {@code startArmed}.
   * 
   */
  public static Member STA_TRUE = new Member ("STA_TRUE");
  
}
