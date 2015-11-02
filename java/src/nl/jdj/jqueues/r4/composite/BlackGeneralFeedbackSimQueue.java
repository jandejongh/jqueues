package nl.jdj.jqueues.r4.composite;

import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** (General) Feedback queue.
 * 
 * <p>
 * Under the hood, a delegate job for each {@link SimJob} visits the (single) embedded {@link SimQueue},
 * and upon departing from that queue, the delegate job is optionally fed back to the embedded queue's input.
 * Feedback is controlled through a {@link SimQueueFeedbackController}, allowing maximum flexibility.
 * 
 * <p>
 * After the delegate job departs the embedded queue and is not fed back, the "real" job departs
 * from the {@link BlackGeneralFeedbackSimQueue}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackGeneralFeedbackSimQueue
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackGeneralFeedbackSimQueue>
  extends AbstractBlackFeedbackSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black feedback queue given an event list, a queue and a feedback controller.
   *
   * @param eventList             The event list to use.
   * @param queue                 The queue, non-<code>null</code>.
   * @param feedbackController    The feedback controller, non-<code>null</code>.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list, queue or feedback controller is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackGeneralFeedbackSimQueue
  (final SimEventList eventList,
    final DQ queue,
    final SimQueueFeedbackController<J, DJ, Q, DQ> feedbackController,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, feedbackController, delegateSimJobFactory);
  }
  
  /** Returns a new {@link BlackGeneralFeedbackSimQueue} object on the same {@link SimEventList} with a copy of of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link BlackGeneralFeedbackSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *         queue and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getFeedbackController
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackGeneralFeedbackSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new BlackGeneralFeedbackSimQueue<>
      (getEventList (), (DQ) encapsulatedQueueCopy, getFeedbackController (), getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "GenFB[embedded queue]".
   * 
   * @return "GenFB[embedded queue]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "GenFB[" + getQueues ().iterator ().next () + "]";
  }

}
