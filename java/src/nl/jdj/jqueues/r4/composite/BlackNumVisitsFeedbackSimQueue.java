package nl.jdj.jqueues.r4.composite;

import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

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
  extends BlackGeneralFeedbackSimQueue<DJ, DQ, J, Q>
{
  
  /** The required number of visits to the embedded queue,
   * 
   */
  private final int numberOfVisits;
  
  /** Creates a suitable {@link SimQueueFeedbackController}.
   * 
   * Auxiliary function to constructor.
   * 
   */
  private static SimQueueFeedbackController createFeedbackController (final int numberOfVisits)
  {
    if (numberOfVisits < 1)
      throw new IllegalArgumentException ();
    return new SimQueueFeedbackController ()
    {
      @Override
      public final boolean feedback (final double time,
        final SimJob delegateJob, final SimQueue delegateQueue,
        final SimJob realJob, final SimQueue realQueue,
        final int visits)
      {
        return visits < numberOfVisits;
      } 
    };
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
  
  /** Returns "FB_numVisits[embedded queue]".
   * 
   * @return "FB_numVisits[embedded queue]".
   * 
   */
  @Override
  public String toString ()
  {
    return "FB_" + this.numberOfVisits + "[" + getQueues ().iterator ().next () + "]";
  }

}
