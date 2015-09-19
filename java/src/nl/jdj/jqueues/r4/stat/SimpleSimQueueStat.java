package nl.jdj.jqueues.r4.stat;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** An concrete class for gathering the most basic statistics on a {@link SimQueue}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimpleSimQueueStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN THIS CLASS.
  //
  
  // Our actual statistics, with corresponding (calculated) average.
  private double cumNrOfJobs  = 0, avgNrOfJobs  = 0; // Number of jobs residing at queue.
  private double cumNrOfJobsX = 0, avgNrOfJobsX = 0; // Number of jobs executing at queue.
  
  /** Resets all the statistics.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   */
  private void resetStatisticsInt ()
  {
    this.cumNrOfJobs  = 0; this.avgNrOfJobs  = 0;
    this.cumNrOfJobsX = 0; this.avgNrOfJobsX = 0;
    // Add others here...
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param time The actual (new) time.
   * @param dt The time since the last update.
   * 
   */
  private void updateStatisticsInt (double time, double dt)
  {
    final SimQueue queue = getQueue ();
    if (queue == null)
      return;
    this.cumNrOfJobs  += queue.getNumberOfJobs () * dt;
    this.cumNrOfJobsX += queue.getNumberOfJobsExecuting ()* dt;
    // Add others here...
  }
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   * @param time The time of the last update.
   * @param dT The time interval between start and the last update, zero or positive.
   * 
   */
  private void calculateStatisticsInt (double time, double dT)
  {
    if (dT < 0)
      throw new RuntimeException ();
    else if (dT == 0)
    {
      this.avgNrOfJobs  = 0.0;
      this.avgNrOfJobsX = 0.0;
      // Add others here...
    }
    else
    {
      this.avgNrOfJobs  = this.cumNrOfJobs  / dT;
      this.avgNrOfJobsX = this.cumNrOfJobsX / dT;
      // Add others here...
    }
  }
  
  // Add getters for your favorite performance measures below...
  
  /** Returns the average number of jobs residing at the queue.
   * 
   * @return The average number of jobs residing at the queue.
   * 
   */
  public final double getAvgNrOfJobs ()
  {
    calculate ();
    return this.avgNrOfJobs;
  }

  /** Returns the average number of jobs executing at the queue.
   * 
   * @return The average number of jobs executing at the queue.
   * 
   */
  public final double getAvgNrOfJobsExecuting ()
  {
    calculate ();
    return this.avgNrOfJobsX;
  }

  //
  // END: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN THIS CLASS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO OVERRIDE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN A SUBCLASS.
  //
  
  /** Resets all the statistics.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * Unfortunately, you must reset the statistics yourself upon construction, because this class avoids the trickery of
   * calling overridable methods from its constructors.
   * 
   */
  @Override
  protected void resetStatistics ()
  {
    resetStatisticsInt ();
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void updateStatistics (double time, double dt)
  {
    updateStatisticsInt (time, dt);
  }
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void calculateStatistics (double time, double dT)
  {
    calculateStatisticsInt (time, dT);
  }
  
  //
  // END: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN A SUBCLASS.
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
  public SimpleSimQueueStat (Q queue, double startTime)
  {
    super (queue, startTime);
    resetStatisticsInt ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>, the startTime property to zero.
   * 
   */
  public SimpleSimQueueStat ()
  {
    super ();
    resetStatisticsInt ();
  }
  
  //
  // END: CONSTRUCTORS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



}
