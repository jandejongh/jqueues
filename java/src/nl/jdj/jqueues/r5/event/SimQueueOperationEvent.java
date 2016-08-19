package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.SimEntityOperation;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** A {@link SimEvent} requesting a {@link SimEntityOperation} at a specific {@link SimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueOperationEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueue>
  SimEventAction<J>
  createAction (final Q queue, SimEntityOperation.Request request)
  {
    if (queue == null || request == null)
      throw new IllegalArgumentException ();
    return (final SimEvent<J> event) ->
    {
      queue.doOperation (event.getTime (), request);
    };
  }
  
  /** Creates an operation event at a specific queue.
   * 
   * @param queue   The queue at which to perform the operation.
   * @param time    The time at which to perform the operation.
   * @param request The operation request for the queue.
   * 
   * @throws IllegalArgumentException If the queue or request is <code>null</code>.
   * 
   * @see SimQueue#doOperation
   * 
   */
  public SimQueueOperationEvent
  (final Q queue, final double time, SimEntityOperation.Request request)
  {
    super ("Op[" + request + "]@" + queue, time, queue, null, createAction (queue, request));
    this.request = request;
  }
  
  @Override
  public SimEntityEvent<J, Q> copyForQueue (final Q destQueue)
  {
    if (destQueue == null)
      throw new IllegalArgumentException ();
    return new SimQueueOperationEvent<> (destQueue, getTime (), getRequest ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REQUEST
  //  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final SimEntityOperation.Request request;
  
  /** Returns the operation request of this event.
   * 
   * @return The operation request of this event, non-{@code null}.
   * 
   */
  public final SimEntityOperation.Request getRequest ()
  {
    return this.request;
  }
  
}
