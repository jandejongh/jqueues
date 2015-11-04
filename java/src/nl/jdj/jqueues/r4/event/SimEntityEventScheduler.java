package nl.jdj.jqueues.r4.event;

import java.util.Set;
import nl.jdj.jsimulation.r4.SimEventList;

/** A utility class capable of scheduling {@link SimEntityEvent}s on an event list.
 *
 */
public abstract class SimEntityEventScheduler
{

  /** Inhibits instantiation.
   * 
   */
  private SimEntityEventScheduler ()
  {
  }
  
  /** Schedules all {@link SimEntityEvent}s on the given {@link SimEventList}, optionally after resetting it to a specific time.
   * 
   * @param eventList   The event list.
   * @param reset       Whether to reset the event list before scheduling.
   * @param resetTime   The new time to which to reset the event list (if requested so). 
   * @param queueEvents The {@link SimEntityEvent}s to schedule.
   * 
   * @throws IllegalArgumentException If <code>eventList == null</code> or the (non-<code>null</code>) set of events
   *                                  has at least one <code>null</code> entry.
   * 
   */
  public static void schedule
  (final SimEventList eventList, final boolean reset, final double resetTime, final Set<SimEntityEvent> queueEvents)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    if (queueEvents != null && queueEvents.contains (null))
      throw new IllegalArgumentException ();
    if (reset)
      eventList.reset (resetTime);
    if (queueEvents != null)
      eventList.addAll (queueEvents);
  }
    
}
