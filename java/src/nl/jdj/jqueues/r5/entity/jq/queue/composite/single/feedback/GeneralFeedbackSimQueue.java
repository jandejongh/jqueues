package nl.jdj.jqueues.r5.entity.jq.queue.composite.single.feedback;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jsimulation.r5.SimEventList;

/** (General) Feedback queue.
 * 
 * <p>
 * Under the hood, a delegate job for each {@link SimJob} visits the (single) embedded {@link SimQueue},
 * and upon departing from that queue, the delegate job is optionally fed back to the embedded queue's input.
 * Feedback is controlled through a {@link SimQueueFeedbackController}, allowing maximum flexibility.
 * 
 * <p>
 * After the delegate job departs the embedded queue and is not fed back, the "real" job departs
 * from the {@link GeneralFeedbackSimQueue}.
 *
 * <p>
 * The start model is set to (fixed) {@link StartModel#LOCAL}.
 * 
 * <p>
 * See {@link AbstractFeedbackSimQueue} for more details on the interplay between
 * {@link SimQueueFeedbackController} and the embedded {@link FeedbackSimQueueSelector},
 * and how, for instance, reset events are propagated.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see AbstractFeedbackSimQueue
 * @see FeedbackSimQueueSelector
 * @see SimQueueFeedbackController
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class GeneralFeedbackSimQueue
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends GeneralFeedbackSimQueue>
  extends AbstractFeedbackSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a feedback queue given an event list, a queue and a feedback controller.
   *
   * <p>
   * The start model is set to (fixed) {@link StartModel#LOCAL}.
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
   * @see StartModel
   * 
   */
  public GeneralFeedbackSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final SimQueueFeedbackController<J, DQ> feedbackController,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, feedbackController, delegateSimJobFactory);
  }
  
  /** Returns a new {@link GeneralFeedbackSimQueue} object on the same {@link SimEventList} with a copy of of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link GeneralFeedbackSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
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
  public GeneralFeedbackSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new GeneralFeedbackSimQueue<>
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
  public String toStringDefault ()
  {
    return "GenFB[" + getQueues ().iterator ().next () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (and made final).
   * 
   * <p>
   * Note that the {@link SimQueueSelector} of this composite queue is reset by our super class.
   * In turn, the selector will reset the embedded {@link SimQueueFeedbackController}.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
