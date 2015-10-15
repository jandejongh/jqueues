package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A {@link FCFS} queue with finite buffer size.
 *
 * Job arriving when the buffer is full are dropped.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class FCFS_B<J extends SimJob, Q extends FCFS_B>
extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{
  
  private final int bufferSize;
  
  public final int getBufferSize ()
  {
    return this.bufferSize;
  }

  public FCFS_B (final SimEventList eventList, final int bufferSize)
  {
    super (eventList);
    if (bufferSize < 0)
      throw new IllegalArgumentException ();
    this.bufferSize = bufferSize;
  }
  
  /** Inserts the job at the tail of the job queue, if there is still room available.
   * 
   * {@inheritDoc}
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (getNumberOfJobsWaiting () < getBufferSize ())
      this.jobQueue.add (job);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }  
  
  /** Returns "FCFS_B[buffer size]".
   * 
   * @return "FCFS_B[buffer size]".
   * 
   */
  @Override
  public String toString ()
  {
    return "FCFS_B[" + getBufferSize () + "]";
  }

}