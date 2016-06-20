package nl.jdj.jqueues.r5.util.stat;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** An concrete class for flexible statistics gathering on a {@link SimQueue}.
 *
 * The statistics observed are under full user control by supplying a list of {@link AutoSimQueueStatEntry}s upon construction.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class AutoSimQueueStat<J extends SimJob, Q extends SimQueue>
extends AbstractSimQueueStat<J, Q>
{
  
  private List<AutoSimQueueStatEntry<Q>> entries;
  
  public final List<AutoSimQueueStatEntry<Q>> getEntries ()
  {
    return this.entries;
  }
  
  protected final void setEntries (final List<AutoSimQueueStatEntry<Q>> entries)
  {
    this.entries = entries;
    reset ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN: STUFF WE NEED TO OVERRIDE.
  //
  
  /** Resets all the statistics.
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
   * <p>
   * This method must be overridden with a call to <code>super</code> if you want to add performance measures in a subclass.
   * 
   */
  @Override
  protected void calculateStatistics (final double startTime, final double endTime)
  {
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
          e.calculate (startTime, endTime);
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
   * @param queue   The queue to gather statistics from.
   * @param entries The list of statistics to monitor on the queue.
   * 
   */
  public AutoSimQueueStat (final Q queue, final List<AutoSimQueueStatEntry<Q>> entries)
  {
    super (queue);
    this.entries = entries;
    resetStatistics ();
  }
  
  /** Constructor.
   * 
   * The queue property is set to <code>null</code>; the probe entries are empty.
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
  
  public void report (final int indent)
  {
    calculate ();
    if (this.entries != null)
      for (AutoSimQueueStatEntry<Q> e : this.entries)
        if (e != null)
        {
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Average " + e.getName () + ": " + e.getAvgValue () + ".");
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Minimum " + e.getName () + ": " + e.getMinValue () + ".");
          for (int i = 1; i <= indent; i++)
            System.out.print (" ");
          System.out.println ("Maximum " + e.getName () + ": " + e.getMaxValue () + ".");
        }    
  }
  
}
