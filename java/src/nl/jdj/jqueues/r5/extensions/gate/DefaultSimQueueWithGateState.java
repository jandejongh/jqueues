package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;

/** A default implementation of {@link SimQueueWithGateState}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimQueueWithGateState<J extends SimJob, Q extends SimQueueWithGate>
extends DefaultSimQueueState<J, Q>
implements SimQueueWithGateState<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new state object for give queue.
   * 
   * @param queue The queue, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  public DefaultSimQueueWithGateState
  (final Q queue)
  {
    super (queue);
    this.gatePassageCredits = Integer.MAX_VALUE;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public void reset ()
  {
    super.reset ();
    this.gatePassageCredits = Integer.MAX_VALUE;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private int gatePassageCredits = Integer.MAX_VALUE;
  
  @Override
  public int getGatePassageCredits ()
  {
    return this.gatePassageCredits;
  }
  
  @Override
  public void setGatePassageCredits (final double time, final int gatePassageCredits)
  {
    if (gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    setTime (time);
    this.gatePassageCredits = gatePassageCredits;
  }
  
}
