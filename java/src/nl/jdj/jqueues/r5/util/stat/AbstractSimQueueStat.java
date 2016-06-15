package nl.jdj.jqueues.r5.util.stat;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;

/** An abstract base class for automatically gathering statistics on a {@link SimQueue}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimQueueListener
 * 
 */
public abstract class AbstractSimQueueStat<J extends SimJob, Q extends SimQueue>
implements SimQueueListener<J, Q>
{

  // The queue we are gathering statistics on.
  private Q queue = null;
  
  // The start time for gathering statistics, and the last update time.
  // Note that always startTime <= lastUpdateTime.
  // Updates before the start time are silently ignored.
  private double startTime = Double.NEGATIVE_INFINITY;
  private double lastUpdateTime = Double.NEGATIVE_INFINITY;
  
  // Whether our statistics are valid for use, or need to be calculated first.
  private boolean statisticsValid = false;
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO OVERRIDE IN A SUBCLASS.
  //
  
  /** Resets all the statistics.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * Unfortunately, you must reset the statistics yourself upon construction, because this class avoids the trickery of
   * calling override-able methods from its constructors.
   * 
   */
  protected abstract void resetStatistics ();
  
  /** Updates all the statistics from the state of the queue.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param time The actual (new) time.
   * @param dt The time since the last update.
   * 
   */
  protected abstract void updateStatistics (double time, double dt);
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param time The time of the last update.
   * @param dT The time interval between start and the last update, zero or positive.
   * 
   */
  protected abstract void calculateStatistics (double time, double dT);
  
  //
  // END: STUFF YOU NEED TO OVERRIDE IN A SUBCLASS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: MAIN ENTRY POINTS FOR STATISTICS MANAGEMENT: RESET/UPDATE/CALCULATE.
  //

  /** Resets all statistics and start a new batch of statistics gathering.
   * 
   * The start time is set to the current time (last update time), all statistics are invalidated
   * and prepared for the new batch.
   * 
   * <p>
   * Only override this method with a call to <code>super</code> if you have special actions to be performed upon a reset
   * <i>other</i> than resetting statistics. Otherwise, override {@link #resetStatistics}.
   * 
   * @see #getLastUpdateTime
   * @see #setStartTime
   * @see #resetStatistics
   * @see #getStatisticsValid
   * 
   */
  public void reset ()
  {
    resetInt ();
    resetStatistics ();
  }
  
  // Auxiliary (final) method for private use in constructor.
  private void resetInt ()
  {
    this.startTime = this.lastUpdateTime;
    this.statisticsValid = false;    
  }
  
  /** Updates all statistics at given time.
   * 
   * Note that updates timed before our latest update are silently ignored.
   * 
   * <p>
   * Only override this method with a call to <code>super</code> if you have special actions to be performed upon an update
   * <i>other</i> than updating statistics. Otherwise, override {@link #updateStatistics}.
   * 
   * @param time The (new) current time.
   * 
   */
  protected void update (double time)
  {
    if (time < this.lastUpdateTime)
      // Ignored...
      return;
    if (time == this.lastUpdateTime)
      return;
    this.statisticsValid = false;
    final double dt = time - this.lastUpdateTime;
    updateStatistics (time, dt);
    this.lastUpdateTime = time;
  }

  /** Calculates all statistics (if invalid) and marks them valid.
   * 
   * This method is automatically invoked by all statistics getters.
   * 
   * <p>
   * Only override this method with a call to <code>super</code> if you have special actions to be performed upon a calculation
   * <i>other</i> than calculating statistics. Otherwise, override {@link #calculateStatistics}.
   * 
   */
  protected void calculate ()
  {
    if (this.statisticsValid)
      return;
    final double dT = this.lastUpdateTime - this.startTime;
    if (dT < 0)
      throw new RuntimeException ();
    if (dT == 0)
    {
      this.statisticsValid = true;
      return;      
    }
    calculateStatistics (this.lastUpdateTime, dT);
    this.statisticsValid = true;
  }

  //
  // END: MAIN ENTRY POINTS FOR STATISTICS MANAGEMENT: RESET/UPDATE/CALCULATE.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CRITICAL INTERNAL BOOKKEEPING: QUEUE, START TIME, LAST UPDATE TIME, STATISTICS VALIDITY.
  //
  
  /** Gets the {@link SimQueue} we are gathering statistics on.
   * 
   * @return The {@link SimQueue} we are gathering statistics on.
   * 
   * @see #setQueue
   * 
   */
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  /** Sets the {@link SimQueue} we are gathering statistics on.
   * 
   * This methods always invokes {@link #reset}.
   * 
   * <p>
   * Override this method is discouraged, even with a call to <code>super</code>.
   * Also, an internal version of this method is used upon object construction.
   * 
   * @param queue The new queue to monitor (may be <code>null</code>).
   * 
   * @see #getQueue
   * @see SimQueue#registerSimEntityListener
   * @see SimQueue#unregisterSimEntityListener
   * @see #reset
   * 
   */
  public void setQueue (Q queue)
  {
    reset ();
    setQueueCommon (queue);
  }
  
  // Auxiliary (final) method for private use in constructor.
  private void setQueueInt (Q queue)
  {
    resetInt ();
    setQueueCommon (queue);
  }
  
  // Common code between setQueue and setQueueInt
  private void setQueueCommon (Q queue)
  {
    if (queue != this.queue)
    {
      if (this.queue != null)
      {
        this.queue.unregisterSimEntityListener (this);
      }
      this.queue = queue;
      if (this.queue != null)
      {
        this.queue.registerSimEntityListener (this);
      }
    }    
  }
  
  /** Gets the start time for gathering statistics.
   * 
   * @return The start time for gathering statistics.
   * 
   */
  public final double getStartTime ()
  {
    return this.startTime;
  }
  
  /** Sets the start time for gathering statistics.
   * 
   * <p>
   * Override this method is discouraged, even with a call to <code>super</code>.
   * Also, an internal version of this method is used upon object construction.
   * 
   * @param startTime The new start time for gathering statistics.
   */
  public void setStartTime (double startTime)
  {
    reset ();
    setStartTimeCommon (startTime);
  }
  
  // Auxiliary (final) method for private use in constructor.
  private void setStartTimeInt (double startTime)
  {
    resetInt ();
    setStartTimeCommon (startTime);
  }
  
  // Common code between setStartTime and setStartTimeInt
  private void setStartTimeCommon (double startTime)
  {
    this.startTime = startTime;
    this.lastUpdateTime = this.startTime;        
  }
  
  /** Returns the time of the last update.
   * 
   * @return The time of the last update.
   * 
   * @see #update(double)
   * @see #notifyUpdate(double, SimEntity) 
   * 
   */
  public final double getLastUpdateTime ()
  {
    return this.lastUpdateTime;
  }
  
  /** Checks if the statistics are valid.
   * 
   * @return True if the statistics are valid.
   * 
   * @see #calculate
   * 
   */
  public final boolean getStatisticsValid ()
  {
    return this.statisticsValid;
  }
  
  //
  // END: CRITICAL INTERNAL BOOKKEEPING: QUEUE, START TIME, LAST UPDATE TIME, STATISTICS VALIDITY.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CONNECTION WITH THE QUEUE / IMPLEMENTATION OF SimQueueListener.
  //
  
  /** Invokes {@link #reset}.
   *
   */
  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    reset ();
  }
  
  /** Checks the queue argument and invokes {@link #update(double)}.
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code> or not the queue we monitor.
   * 
   * @see #getQueue
   * 
   */
  @Override
  public void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || entity != getQueue ())
      throw new IllegalArgumentException ();
    update (time);
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStateChanged (final double time, final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
  }
  
  /** Does nothing.
   * 
   */
  @Override
  public void notifyNewNoWaitArmed (final double time, final Q queue, final boolean noWaitArmed)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
  }

  //
  // END: CONNECTION WITH THE QUEUE / IMPLEMENTATION OF SimQueueListener.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: CONSTRUCTORS.
  //
  
  /** Constructor.
   * 
   * @param queue The queue to gather statistics from.
   * @param startTime The start time for gathering statistics.
   * 
   */
  public AbstractSimQueueStat (Q queue, double startTime)
  {
    setQueueInt (queue);
    setStartTimeInt (startTime);
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>, the startTime property to zero.
   * 
   */
  public AbstractSimQueueStat ()
  {
    this (null, 0.0);
  }
  
  //
  // END: CONSTRUCTORS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  

}
