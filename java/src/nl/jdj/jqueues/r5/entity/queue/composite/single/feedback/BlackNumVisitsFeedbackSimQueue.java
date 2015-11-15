package nl.jdj.jqueues.r5.entity.queue.composite.single.feedback;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** Feedback queue with fixed number of visits to the embedded {@link SimQueue}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackNumVisitsFeedbackSimQueue
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackNumVisitsFeedbackSimQueue>
  extends AbstractBlackFeedbackSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a suitable {@link SimQueueFeedbackController}.
   * 
   * Auxiliary function to constructor.
   * 
   */
  private static SimQueueFeedbackController createFeedbackController (final int numberOfVisits)
  {
    if (numberOfVisits < 1)
      throw new IllegalArgumentException ();
    return (SimQueueFeedbackController)
      (final double time, final SimQueue delegateQueue, final SimJob realJob, final int visits)
        -> visits < numberOfVisits;
  }
  
  /** Creates a black feedback queue given an event list a queue and the number of visits required.
   *
   * @param eventList             The event list to use.
   * @param queue                 The queue, non-<code>null</code>.
   * @param numberOfVisits        The required number of visits to the queue, must be at least unity.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or queue is <code>null</code>,
   *                                  or the required number of visits is zero or negative.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackNumVisitsFeedbackSimQueue
  (final SimEventList eventList,
    final DQ queue,
    final int numberOfVisits,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, createFeedbackController (numberOfVisits), delegateSimJobFactory);
    this.numberOfVisits = numberOfVisits;
  }
  
  /**  Returns a new {@link BlackNumVisitsFeedbackSimQueue} object on the same {@link SimEventList} with a copy of the sub-queue,
   *  the same number of visits required, and the same delegate-job factory.
   * 
   * @return A new {@link BlackNumVisitsFeedbackSimQueue} object on the same {@link SimEventList} with a copy of the sub-queue,
   *  the same number of visits required, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getNumberOfVisits
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackNumVisitsFeedbackSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> queueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new BlackNumVisitsFeedbackSimQueue<>
      (getEventList (), (DQ) queueCopy, getNumberOfVisits (), getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "FB_numVisits[embedded queue]".
   * 
   * @return "FB_numVisits[embedded queue]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "FB_" + this.numberOfVisits + "[" + getQueues ().iterator ().next () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF VISITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The required number of visits to the embedded queue,
   * 
   */
  private final int numberOfVisits;
  
  /** Returns the required number of visits to the embedded queue.
   * 
   * @return The required number of visits to the embedded queue.
   * 
   */
  public final int getNumberOfVisits ()
  {
    return this.numberOfVisits;
  }
  
}
