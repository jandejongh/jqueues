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
public final class FCFS_FB<J extends SimJob, Q extends FCFS_FB>
extends FCFS<J, Q>
{
  
  private final int bufferSize;
  
  public /* final */ int getBufferSize ()
  {
    return this.bufferSize;
  }

  public FCFS_FB (final SimEventList eventList, final int bufferSize)
  {
    super (eventList);
    if (bufferSize < 0)
      throw new IllegalArgumentException ();
    this.bufferSize = bufferSize;
  }
  
  public /* final */ int getNumberOfJobsWaiting ()
  {
    return getNumberOfJobs () - getNumberOfJobsExecuting ();
  }
  
  /** Inserts the job at the tail of the job queue.
   * 
   * {@inheritDoc}
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected /* final */ void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (getNumberOfJobsWaiting () < getBufferSize ())
      this.jobQueue.add (job);      
  }

}
