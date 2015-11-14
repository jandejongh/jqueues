package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for {@link SimQueueWithGate}.
 *
 */
public final class SimQueueWithGateStateHandler
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueWithGateHandler".
   * 
   * @return "SimQueueWithGateHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueWithGateHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    this.gatePassageCredits = Integer.MAX_VALUE;    
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.gatePassageCredits = Integer.MAX_VALUE;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private int gatePassageCredits = Integer.MAX_VALUE;
  
  /** Gets the remaining number of passage credits for the gate.
   * 
   * <p>
   * Mimics {@link SimQueueWithGate#getGatePassageCredits}.
   * 
   * @return The remaining number of passage credits for the gate; non-negative with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalStateException If we are not registered at a {@link DefaultSimQueueState}.
   * 
   */
  public int getGatePassageCredits ()
  {
    if (this.queueState == null)
      throw new IllegalStateException ();
    return this.gatePassageCredits;    
  }
  
  /** Sets the remaining number of passage credits for the gate.
   * 
   * <p>
   * Mimics {@link SimQueueWithGate#openGate(double, int)}.
   * 
   * <p>
   * The time cannot be in the past.
   * 
   * @param time               The time to set the remaining number of passages.
   * @param gatePassageCredits The new remaining number of passage credits for the gate, non-negative,
   *                           with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalStateException    If we are not registered at a {@link DefaultSimQueueState}.
   * @throws IllegalArgumentException If time is in the past, or the number of passage credits is (strictly) negative.
   * 
   * @see DefaultSimQueueState#setTime
   * 
   */
  public void setGatePassageCredits (final double time, final int gatePassageCredits)
  {
    if (this.queueState == null)
      throw new IllegalStateException ();
    if (gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    if (this.queueState == null)
      throw new IllegalStateException ();
    this.queueState.setTime (time);
    this.gatePassageCredits = gatePassageCredits;    
  }
  
}
