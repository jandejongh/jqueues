package nl.jdj.jqueues.r5.extensions.lastdeptime;

import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for maintaining the last departure time.
 *
 * @see SimQueuePredictor_LeakyBucket
 * 
 */
public final class SimQueueLastDepTimeStateHandler
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueLastDepTimeHandler".
   * 
   * @return "SimQueueLastDepTimeHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueLastDepTimeHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    this.lastDepTimeSet = false;
    this.lastDepTime = Double.NEGATIVE_INFINITY;
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.lastDepTimeSet = false;
    this.lastDepTime = Double.NEGATIVE_INFINITY;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LAST DEPARTURE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean lastDepTimeSet = false;
  
  private double lastDepTime = Double.NEGATIVE_INFINITY;

  /** Returns whether the last departure time was set since construction or last reset.
   * 
   * @return Whether the last departure time was set since construction or last reset.
   * 
   * @see #resetHandler
   * @see #getLastDepTime
   * 
   */
  public final boolean isLastDepTimeSet ()
  {
    return this.lastDepTimeSet;
  }
  
  /** Gets the last departure time.
   * 
   * <p>
   * Does not check whether the time was set since construction or last reset.
   * If not, the default {@link Double#NEGATIVE_INFINITY} is returned.
   * 
   * @return The last departure time (set), or {@link Double#NEGATIVE_INFINITY}
   *         if no departure time was set since construction or last reset.
   * 
   * @see #resetHandler
   * @see #setLastDepTime
   * 
   */
  public final double getLastDepTime ()
  {
    return this.lastDepTime;
  }
  
  /** Sets the last departure time (without error checking) and flag the value has been set.
   * 
   * @param lastDepTime The (new) last departure time (unchecked).
   * 
   * @see #resetHandler
   * @see #getLastDepTime
   * @see #isLastDepTimeSet
   * 
   */
  public final void setLastDepTime (final double lastDepTime)
  {
    this.lastDepTime = lastDepTime;
    this.lastDepTimeSet = true;
  }
  
}
