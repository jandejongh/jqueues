package nl.jdj.jqueues.r5.entity.queue.composite.single.feedback;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** An object capable of controlling feedback on a {@link SimQueue}.
 *
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 * 
 * @see BlackGeneralFeedbackSimQueue
 * 
 */
@FunctionalInterface
public interface SimQueueFeedbackController<J extends SimJob, DQ extends SimQueue>
{
  
  /** Returns true if the delegate job is to be fed back into the queue.
   * 
   * @param time          The current time, i.e., the (latest) departure time of the job at its queue.
   * @param delegateQueue The "delegate" queue (i.e., the queue just left by the delegate job).
   * @param realJob       The real job, non-<code>null</code>.
   * @param visits        The number of visits of the delegate job to the (delegate) queue thus far.
   * 
   * @return True if the delegate job is to be fed back into the (delegate) queue.
   * 
   */
  public boolean feedback (double time, DQ delegateQueue, J realJob, int visits);
  
}
