package nl.jdj.jqueues.r4.util.schedule;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** A schedule of the start or end of a queue-access vacation period.
 * 
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class QueueAccessVacationSchedule<Q extends SimQueue>
extends QueueExternalEvent<SimJob, Q>
{
  
  /** Whether a queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
   * 
   */
  public final boolean vacation;

  /** Creates a queue-access vacation schedule.
   * 
   * @param queue    The queue at which to start or end a queue-access vacation.
   * @param time     The time at which to start or end a queue-access vacation.
   * @param vacation Whether a queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
   * 
   * @throws IllegalArgumentException If <code>queue == null</code>.
   * 
   */
  public QueueAccessVacationSchedule (final Q queue, final double time, final boolean vacation)
  {
    super (time, queue, null);
    this.vacation = vacation;
  }

  /** Schedules the queue-access vacation.
   * 
   * @see SimQueue#startQueueAccessVacation
   * @see SimQueue#stopQueueAccessVacation
   * 
   */
  @Override
  public final void schedule (final SimEventList eventList)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    eventList.schedule (QueueAccessVacationSchedule.this.time, (SimEventAction) (final SimEvent event) ->
    {
      if (QueueAccessVacationSchedule.this.vacation)
        QueueAccessVacationSchedule.this.queue.startQueueAccessVacation ();
      else
        QueueAccessVacationSchedule.this.queue.stopQueueAccessVacation ();        
    });
  }

}
