package nl.jdj.jqueues.r1.stat;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r1.SimJob;
import nl.jdj.jqueues.r1.SimQueue;

/** An concrete class for flexible statistics gathering on a {@link SimQueue}.
 *
 * The statistics observed are under full user control by supplying a list of {@link AutoSimQueueStatEntry}s upon construction.
 * 
 * @param <J> The type of {@link SimJobs}s supported.
 * @param <Q> The type of {@link SimQueues}s supported.
 * 
 */
public class AutoSimQueueStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
{
  
  private final List<AutoSimQueueStatEntry<Q>> entries;
  
  public final List<AutoSimQueueStatEntry<Q>> getEntries ()
  {
    return this.entries;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF WE NEED TO OVERRIDE.
  //
  
  /** Resets all the statistics.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void resetStatistics ()
  {
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.reset ();
  }
  
  /** Updates all the statistics from the state of the queue.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void updateStatistics (double time, double dt)
  {
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.update (getQueue (), dt);
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
  protected final void calculateStatistics (double time, double dT)
  {
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.calculate (dT);
  }
  
  //
  // END: STUFF WE NEED TO OVERRIDE.
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
   * @param entries The list of statistics to monitor on the queue.
   * 
   */
  public AutoSimQueueStat (Q queue, double startTime, List<AutoSimQueueStatEntry<Q>> entries)
  {
    super (queue, startTime);
    this.entries = entries;
    resetStatistics ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>, the startTime property to zero.
   * 
   */
  public AutoSimQueueStat ()
  {
    super ();
    this.entries = new ArrayList<> ();
    resetStatistics ();
  }
  
  //
  // END: CONSTRUCTORS.
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



  public void report ()
  {
    report (0);
  }
  
  public void report (int indent)
  {
    calculate ();
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
        {
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Average " + e.getName () + ": " + e.getAvgValue () + ".");
        }    
  }
  
}
