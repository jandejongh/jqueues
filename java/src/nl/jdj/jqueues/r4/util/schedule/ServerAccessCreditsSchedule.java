package nl.jdj.jqueues.r4.util.schedule;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** A schedule of setting server-access credits at a specific queue.
 * 
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public final class ServerAccessCreditsSchedule<Q extends SimQueue>
extends QueueExternalEvent<SimJob, Q>
{
  
  /** The number of credits to grant.
   * 
   */
  public final int credits;

  /** Creates a server-access credits schedule.
   * 
   * @param queue   The queue at which to set server-access credits.
   * @param time    The time at which to set server-access credits.
   * @param credits The number of credits to grant.
   * 
   * @throws IllegalArgumentException If <code>queue == null</code> or the number of credits is strictly negative.
   * 
   */
  public ServerAccessCreditsSchedule (final Q queue, final double time, final int credits)
  {
    super (time, queue, null);
    if (credits < 0)
      throw new IllegalArgumentException ();
    this.credits = credits;
  }

  /** Schedules setting the server-access credits.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  @Override
  public final void schedule (final SimEventList eventList)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    eventList.schedule (ServerAccessCreditsSchedule.this.time, (SimEventAction) (final SimEvent event) ->
    {
      ServerAccessCreditsSchedule.this.queue.setServerAccessCredits (ServerAccessCreditsSchedule.this.credits);
    });
  }

}
