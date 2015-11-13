package nl.jdj.jqueues.r5.entity.queue.composite.parallel;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for parallel queues.
 * 
 * <p>
 * With parallel queues, a (delegate) job visits no more than one sub-queue and then leaves.
 * 
 * <p>
 * This selector embed the user-supplied selector for parallel queues, and makes sure that
 * a (delegate) job leaves the queueing system after one (zero) visit.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 *
 */
public class ParallelSimQueuesSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  /** Creates a {@link SimQueueSelector} for parallel queues.
   * 
   * @param userSimQueueSelector The user-supplied {@link SimQueueSelector, may be {@code null},
   *                             in which case a visiting jobs leaves upon arrival.
   * 
   */
  public ParallelSimQueuesSelector (final SimQueueSelector<J, DQ> userSimQueueSelector)
  {
    this.userSimQueueSelector = userSimQueueSelector;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // USER-SUPPLIED SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final SimQueueSelector<J, DQ> userSimQueueSelector;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the result from the user selector if present, {@code null} otherwise.
   * 
   * @return The result from the user sub-queue selector if present, {@code null} otherwise.
   * 
   */
  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    return (this.userSimQueueSelector != null ? this.userSimQueueSelector.selectFirstQueue (time, job) : null);
  }
      
  /** Returns {@code null}.
   * 
   * @return {@code null}.
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code>.
   * 
   */
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (previousQueue == null)
      throw new IllegalStateException ();
    return null;
  }
  
}
