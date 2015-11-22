package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** An {@link SimEvent} for controlling the gate of a {@link SimQueueWithGate}.
 * 
 * <p>
 * The event can be created for any {@link SimQueue}, but it has no effect unless that queue
 * is a {@link SimQueueWithGate}.
 * This is actually checked at runtime, and not reflected in the generic-type arguments.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueGateEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueue>
  String createName (final Q queue, final int gatePassageCredits)
  {
    if (gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    if (! (queue instanceof SimQueueWithGate))
      return "GATE_NOP@" + queue;
    else if (gatePassageCredits == 0)
      return "GATE_CLOSE@" + queue;
    else if (gatePassageCredits < Integer.MAX_VALUE)
      return "GATE_OPEN[" + gatePassageCredits + "]@" + queue;
    else
      return "GATE_OPEN@" + queue;
  }
  
  private static
  <J extends SimJob, Q extends SimQueue>
  SimEventAction<J>
  createAction (final Q queue, final int gatePassageCredits)
  {
    if (queue == null || gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    return (final SimEvent<J> event) ->
    {
      if (queue instanceof SimQueueWithGate)
        ((SimQueueWithGate) queue).setGatePassageCredits (event.getTime (), gatePassageCredits);
    };
  }
  
  /** Creates a gate-setting event at a specific queue.
   * 
   * <p>
   * The value zero for the gate-passage credits effectively closes the gate;
   * {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @param queue              The queue at which to control the gate.
   * @param time               The time at which to control the gate.
   * @param gatePassageCredits The gate-passage credits embedded in this event.
   * 
   * @throws IllegalArgumentException If <code>queue == null</code>, or the number of gate-passage credits is strictly negative.
   * 
   * @see SimQueueWithGate#setGatePassageCredits
   * 
   */
  public SimQueueGateEvent
  (final Q queue, final double time, final int gatePassageCredits)
  {
    super (createName (queue, gatePassageCredits), time, queue, null, createAction (queue, gatePassageCredits));
    this.gatePassageCredits = gatePassageCredits;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int gatePassageCredits;
  
  /** Returns the number of gate-passage credits of the event.
   * 
   * <p>
   * The value zero effectively closes the gate;
   * {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @return The number of gate-passage credits of the event; zero or positive.
   * 
   */
  public final int getGatePassageCredits ()
  {
    return this.gatePassageCredits;
  }
  
}
