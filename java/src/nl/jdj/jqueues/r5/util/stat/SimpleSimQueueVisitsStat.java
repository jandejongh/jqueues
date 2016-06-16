package nl.jdj.jqueues.r5.util.stat;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** An concrete class for gathering the most basic visits-related statistics on a {@link SimQueue}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimpleSimQueueVisitsStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
{
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF YOU NEED TO CHANGE WHEN ADDING STATISTICS / PERFORMANCE MEASURES IN THIS CLASS.
  //
  
  // Our actual statistics, with corresponding (calculated) average.
  
  private int Narrivals;   // Number of arrivals.
  private int Nstarted;    // Number of jobs started.
  private int Ndepartures; // Number of departures.
  
  private double cumWaitingTime = 0; // Cumulative waiting time (over jobs started).
  private double cumSojournTime = 0; // Cumulative sojourn time (over departures).
  
  private double avgWaitingTime = Double.NaN; // Average waiting time, when calculated (over jobs started).
  private double avgSojournTime = Double.NaN; // Average sojourn time, when calculated (over jobs departed).
  
  private final Map<J, Double> arrivals = new HashMap<> ();
  private final Map<J, Double> started  = new HashMap<> ();
  
  /** Resets all the statistics.
   * 
   * All other required internal bookkeeping has already been taken care of.
   * 
   */
  private void resetStatisticsInt ()
  {
    this.Narrivals = 0;
    this.Nstarted = 0;
    this.Ndepartures = 0;
    this.cumWaitingTime = 0;
    this.cumSojournTime = 0;
    this.avgWaitingTime = Double.NaN;
    this.avgSojournTime = Double.NaN;
    this.arrivals.clear ();
    this.started.clear ();
    // Add others here...
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * Nothing to do here...
   * 
   * @param time The actual (new) time.
   * @param dt The time since the last update.
   * 
   */
  private void updateStatisticsInt (final double time, final double dt)
  {
    // How to obtain the queue; may be null.
    // final SimQueue queue = getQueue ();
  }
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * 
   * @see #calculate
   * 
   */
  private void calculateStatisticsInt (final double startTime, final double endTime)
  {
    if (endTime < startTime)
      throw new IllegalArgumentException ();
    if (this.Nstarted > 0)
      this.avgWaitingTime = this.cumWaitingTime / Nstarted;
    if (this.Ndepartures > 0)
      this.avgSojournTime = this.cumSojournTime / Ndepartures;    
  }
  
  // Add getters for your favorite performance measures below...
  
  /** Returns the number of arrivals.
   * 
   * @return The number of arrivals.
   * 
   */
  public final int getNumberOfArrivals ()
  {
    calculate ();
    return this.Narrivals;
  }
  
  /** Returns the number of started jobs.
   * 
   * @return The number of started jobs.
   * 
   */
  public final int getNumberOfStartedJobs ()
  {
    calculate ();
    return this.Nstarted;
  }
  
  /** Returns the number departures.
   * 
   * @return The number of departures.
   * 
   */
  public final int getNumberOfDepartures ()
  {
    calculate ();
    return this.Nstarted;
  }
  
  /** Returns the average waiting time at the queue.
   * 
   * @return The average waiting time at the queue.
   * 
   */
  public final double getAvgWaitingTime ()
  {
    calculate ();
    return this.avgWaitingTime;
  }

  /** Returns the average sojourn time at the queue.
   * 
   * @return The average sojourn time at the queue.
   * 
   */
  public final double getAvgSojournTime ()
  {
    calculate ();
    return this.avgSojournTime;
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
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void updateStatistics (final double time, final double dt)
  {
    updateStatisticsInt (time, dt);
  }
  
  /** Calculates all the statistics from the accumulated updates.
   * 
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void calculateStatistics (final double startTime, final double endTime)
  {
    calculateStatisticsInt (startTime,endTime);
  }
  
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
    super.notifyArrival (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || this.arrivals.containsKey (job) || this.started.containsKey (job))
      throw new IllegalArgumentException ();
    this.arrivals.put (job, time);
    this.Narrivals++;
  }
    
  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
    super.notifyStart (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || (! this.arrivals.containsKey (job)) || this.started.containsKey (job))
      throw new IllegalArgumentException ();
    this.started.put (job, time);
    this.cumWaitingTime += (this.started.get (job) - this.arrivals.get (job));
    this.Nstarted++;
  }

  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
    super.notifyDrop (time, job, queue);
    // XXX To be implemented later; also think about semantics of the performance measures!
    throw new UnsupportedOperationException ();
  }

  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
    super.notifyRevocation (time, job, queue);
    // XXX To be implemented later; also think about semantics of the performance measures!
    throw new UnsupportedOperationException ();
  }

  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
    super.notifyDeparture (time, job, queue);
    if (time < getLastUpdateTime () || job == null || queue == null || queue != getQueue ()
      || ! this.arrivals.containsKey (job))
      throw new IllegalArgumentException ();
    this.cumSojournTime += (time - this.arrivals.get (job));
    this.Ndepartures++;
    this.arrivals.remove (job);
    this.started.remove (job);
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
   * 
   */
  public SimpleSimQueueVisitsStat (final Q queue)
  {
    super (queue);
    resetStatisticsInt ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>.
   * 
   */
  public SimpleSimQueueVisitsStat ()
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
