package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

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
  
  /**  Returns a new {@link FCFS_B} object on the same {@link SimEventList} with the same buffer size.
   * 
   * @return A new {@link FCFS_B} object on the same {@link SimEventList} with the same buffer size.
   * 
   * @see #getEventList
   * @see #getBufferSize
   * 
   */
  @Override
  public FCFS_B<J, Q> getCopySimQueue ()
  {
    return new FCFS_B<> (getEventList (), getBufferSize ());
  }
  
  /** Inserts the job at the tail of the job queue if it will be taken into service immediately,
   * or else if there is still waiting room available.
   * 
   * <p>
   * Note that we must temporarily accept the fact that in case there is no waiting room left, but we know that the job will
   * be taken into service immediately, we leave the queue in an inconsistent state by adding the job to {@link #jobQueue},
   * having more jobs waiting than allowed.
   * Here we rely on the fact that by contract of {@link AbstractSimQueue#arrive}, between corresponding calls to
   * {@link #insertJobInQueueUponArrival} and {@link #rescheduleAfterArrival} there can be no event handling from the event list
   * or from notifications from elsewhere.
   * 
   * @see #hasServerAcccessCredits
   * @see #isNoWaitArmed
   * @see #getNumberOfJobsWaiting
   * @see #getBufferSize
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if ((hasServerAcccessCredits () && isNoWaitArmed ())
      || getNumberOfJobsWaiting () < getBufferSize ())
      this.jobQueue.add (job);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  /** Returns "FCFS_B[buffer size]".
   * 
   * @return "FCFS_B[buffer size]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "FCFS_B[" + getBufferSize () + "]";
  }

}
