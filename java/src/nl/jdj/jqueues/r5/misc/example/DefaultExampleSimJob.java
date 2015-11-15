package nl.jdj.jqueues.r5.misc.example;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.listener.StdOutSimEntityListener;
import nl.jdj.jsimulation.r5.SimEventList;

/** Implementation of {@link SimJob} used (as starting point) in (most of) the examples.
 * 
 * <p>
 * Each job has a public index 'n', set upon construction ({@code n > 0}).
 * The requested service time for the job equals its index.
 * This is merely to create interesting examples.
 * 
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class DefaultExampleSimJob<J extends SimJob, Q extends SimQueue>
extends AbstractSimJob<J, Q>
{
 
  /** Whether or not this job reports main queue operations to {@link System#out}
   *  through a {@link StdOutSimEntityListener}.
   * 
   */
  private final boolean reported;
    
  /** The index of the job, strictly positive.
   * 
   */
  public final int n;

  /** Creates a new {@link DefaultExampleSimJob}.
   * 
   * <p>
   * The {@link SimJob} created is <i>not</i> attached to a {@link SimEventList} (i.e., it does not receive reset events
   * from the event list, nor does it have to; subclasses may override this).
   * 
   * @param reported Whether or not this job reports main queue operations to {@link System#out}.
   * @param n        The index of the job, strictly positive.
   * 
   */
  public DefaultExampleSimJob (final boolean reported, final int n)
  {
    super (null, Integer.toString (n));
    if (n <= 0)
      throw new IllegalArgumentException ();
    this.reported = reported;
    this.n = n;
    if (this.reported)
      registerSimEntityListener (new StdOutSimEntityListener ());
  }

  /** Returns the index number as service time at given (any non-{@code null}) queue,
   *  unless the {@link SimJob#getServiceTime} contract orders otherwise.
   * 
   * @param queue The queue to visit; any non-{@code null} value of the argument
   *              returns the index number as requested service time.
   * 
   * @return The index number of this job, or zero if mandated by the {@link SimJob#getServiceTime} contract.
   * 
   */
  @Override
  public double getServiceTime (final SimQueue queue)
  {
    if (queue == null && getQueue () == null)
      return 0.0;
    else
      return (double) n;
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  /** Returns {@code "DefaultExampleSimJob"}.
   * 
   * @return {@code "DefaultExampleSimJob"}.
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "DefaultExampleSimJob";
  }
  
}
