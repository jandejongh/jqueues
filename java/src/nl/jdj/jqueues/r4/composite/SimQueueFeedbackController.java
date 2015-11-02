package nl.jdj.jqueues.r4.composite;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** An object capable of controlling feedback on a {@link SimQueue}.
 *
 * @param <J>  The job type.
 * @param <DJ> The delegate-job type.
 * @param <Q>  The queue-type.
 * @param <DQ> The queue-type for delegate jobs.
 * 
 * @see BlackGeneralFeedbackSimQueue
 * 
 */
public interface SimQueueFeedbackController<J extends SimJob, DJ extends SimJob, Q extends SimQueue, DQ extends SimQueue>
{
  
  /** Returns true if the delegate job is to be fed back into the queue.
   * 
   * @param time          The current time, i.e., the (latest) departure time of the job at its queue.
   * @param delegateJob   The job, non-<code>null</code>.
   * @param delegateQueue The "delegate" queue (i.e., the queue just left by the delegate job).
   * @param realJob       The real job, non-<code>null</code>.
   * @param realQueue     The "real" queue, i.e., the queue being visited by the real job.
   * @param visits        The number of visits of the delegate job to the (delegate) queue thus far.
   * 
   * @return True if the delegate job is to be fed back into the (delegate) queue.
   * 
   */
  public boolean feedback (double time, DJ delegateJob, DQ delegateQueue, J realJob, Q realQueue, int visits);
  
}
