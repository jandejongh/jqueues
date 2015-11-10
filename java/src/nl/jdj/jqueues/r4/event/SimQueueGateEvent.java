package nl.jdj.jqueues.r4.event;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.SimQueueWithGate;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** An {@link SimEvent} for controlling the gate of a {@link SimQueueWithGate}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueGateEvent<J extends SimJob, Q extends SimQueueWithGate>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueueWithGate>
  String createName (final Q queue, final int numberOfPassages)
  {
    if (numberOfPassages < 0)
      throw new IllegalArgumentException ();
    if (numberOfPassages == 0)
      return "GATE_CLOSE@" + queue;    

    else if (numberOfPassages < Integer.MAX_VALUE)
      return "GATE[" + numberOfPassages + "]@" + queue;
    else
      return "GATE_OPEN@" + queue;    
  }
  
  private static
  <J extends SimJob, Q extends SimQueueWithGate>
  SimEventAction<J>
  createAction (final Q queue, final int passages)
  {
    if (queue == null || passages < 0)
      throw new IllegalArgumentException ();
    return new SimEventAction<J> ()
    {
      @Override
      public void action (final SimEvent<J> event)
      {
        queue.openGate (event.getTime (), passages);
      }
    };
  }
  
  /** Creates a gate-setting event at a specific queue.
   * 
   * <p>
   * The value zero for the remaining number of passages effectively closes the gate;
   * {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @param queue            The queue at which to control the gate.
   * @param time             The time at which to control the gate.
   * @param numberOfPassages The (remaining) number of passages embedded in this event.
   * 
   * @throws IllegalArgumentException If <code>queue == null</code>, or the number of passages is strictly negative.
   * 
   * @see SimQueueWithGate#openGate
   * @see SimQueueWithGate#closeGate
   * 
   */
  public SimQueueGateEvent
  (final Q queue, final double time, final int numberOfPassages)
  {
    super (createName (queue, numberOfPassages), time, queue, null, createAction (queue, numberOfPassages));
    this.numberOfPassages = numberOfPassages;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PASSAGES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int numberOfPassages;
  
  /** Returns the number of passages of the event.
   * 
   * <p>
   * The value zero effectively closes the gate;
   * {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @return The number of passages of the event; zero or positive.
   * 
   */
  public final int getNumberOfPassages ()
  {
    return this.numberOfPassages;
  }
  
}
