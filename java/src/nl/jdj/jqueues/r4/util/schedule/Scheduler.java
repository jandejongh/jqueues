package nl.jdj.jqueues.r4.util.schedule;

import java.util.Set;
import nl.jdj.jsimulation.r4.SimEventList;

/** A utility class capable of scheduling {@link QueueExternalEvent}s on an event list.
 *
 */
public class Scheduler
{

  /** Schedules all {@link QueueExternalEvent}s on the given {@link SimEventList}, optionally after resetting it to a specific time.
   * 
   * @param eventList           The event list.
   * @param reset               Whether to reset the event list before scheduling.
   * @param resetTime           The new time to which to reset the event list (if requested so). 
   * @param queueExternalEvents The {@link QueueExternalEvent}s to schedule.
   * 
   * @throws IllegalArgumentException If <code>eventList == null</code> or the (non-<code>null</code>) set of events
   *                                  has at least one <code>null</code> entry.
   * 
   * @see QueueExternalEvent#schedule
   * 
   */
  public static void schedule
  (final SimEventList eventList, final boolean reset, final double resetTime, final Set<QueueExternalEvent> queueExternalEvents)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    if (queueExternalEvents != null && queueExternalEvents.contains (null))
      throw new IllegalArgumentException ();
    if (reset)
      eventList.reset (resetTime);
    if (queueExternalEvents != null)
      for (QueueExternalEvent qee : queueExternalEvents)
        qee.schedule (eventList);
  }
    
}
