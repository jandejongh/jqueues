package nl.jdj.jqueues.r4.event;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** An {@link SimEvent} for  setting server-access credits at a queue.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueServerAccessCreditsEvent<J extends SimJob, Q extends SimQueue>
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
  createAction (final Q queue, final int credits)
  {
    if (queue == null || credits < 0)
      throw new IllegalArgumentException ();
    return new SimEventAction<J> ()
    {
      @Override
      public void action (final SimEvent<J> event)
      {
        queue.setServerAccessCredits (credits);
      }
    };
  }
  
  /** Creates a server-access-credits event at a specific queue.
   * 
   * @param queue   The queue at which to set server-access credits.
   * @param time    The time at which to set server-access credits.
   * @param credits The number of credits to grant.
   * 
   * @throws IllegalArgumentException If <code>queue == null</code> or the number of credits is strictly negative.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  public SimQueueServerAccessCreditsEvent
  (final Q queue, final double time, final int credits)
  {
    super ("SAC[" + credits + "]@" + queue, time, queue, null, createAction (queue, credits));
    this.credits = credits;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int credits;
  
  /** Returns the number of credits to grant.
   * 
   * @return The number of credits to grant.
   * 
   */
  public final int getCredits ()
  {
    return this.credits;
  }
  
}