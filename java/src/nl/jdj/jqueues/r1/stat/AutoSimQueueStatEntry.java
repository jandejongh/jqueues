package nl.jdj.jqueues.r1.stat;

import nl.jdj.jqueues.r2.SimQueue;

/** A description of an entry for automatic statistics management.
 * 
 * @param <Q> The type of {@link SimQueues}s supported.
 * 
 */
public class AutoSimQueueStatEntry<Q extends SimQueue>
{

  private final String name;
  
  public final String getName ()
  {
    return this.name;
  }
  
  private final SimQueueProbe<Q> probe;
  
  public final SimQueueProbe<Q> getSimQueueProbe ()
  {
    return this.probe;
  }

  public AutoSimQueueStatEntry (String name, SimQueueProbe<Q> probe)
  {
    if (name == null || probe == null)
      throw new IllegalArgumentException ();
    this.name = name;
    this.probe = probe;
    resetInt ();
  }
  
  private double cumValue = 0.0;

  public final double getCumValue ()
  {
    return this.cumValue;
  }
  
  private double avgValue = 0.0;
  
  public final double getAvgValue ()
  {
    return this.avgValue;
  }
  
  private void resetInt ()
  {
    this.cumValue = 0.0;
    this.avgValue = 0.0;
  }
  
  public void reset ()
  {
    resetInt ();
  }
  
  public void update (Q queue, double dt)
  {
    if (queue != null)
      this.cumValue += this.probe.get (queue) * dt;
  }
  
  public void calculate (double dT)
  {
    if (dT < 0)
      this.avgValue = 0.0;
    else
      this.avgValue = this.cumValue / dT;
  }

}
