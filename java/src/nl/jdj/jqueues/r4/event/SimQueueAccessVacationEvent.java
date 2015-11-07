package nl.jdj.jqueues.r4.event;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** An {@link SimEvent} for the start or end of a queue-access vacation at a queue.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueAccessVacationEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueue>
  SimEventAction<J>
  createAction (final Q queue, final boolean vacation)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    return new SimEventAction<J> ()
    {
      @Override
      public void action (final SimEvent<J> event)
      {
        if (vacation)
          queue.startQueueAccessVacation ();
        else
          queue.stopQueueAccessVacation ();
      }
    };
  }
  
  /** Creates a queue-access vacation event at a specific queue.
   * 
   * @param queue    The queue at which to start or end a queue-access vacation.
   * @param time     The time at which to start or end a queue-access vacation.
   * @param vacation Whether a queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>.
   * 
   * @see SimQueue#startQueueAccessVacation
   * @see SimQueue#stopQueueAccessVacation
   * 
   */
  public SimQueueAccessVacationEvent
  (final Q queue, final double time, final boolean vacation)
  {
    super ("QAV[" + vacation + "]@" + queue, time, queue, null, createAction (queue, vacation));
    this.vacation = vacation;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  final boolean vacation;
  
  /** Returns whether the vacation starts or ends.
   * 
   * @return Whether the queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
   * 
   */
  public final boolean getVacation ()
  {
    return this.vacation;
  }
  
}