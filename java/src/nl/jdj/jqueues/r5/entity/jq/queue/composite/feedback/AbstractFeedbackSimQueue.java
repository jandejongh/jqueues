package nl.jdj.jqueues.r5.entity.jq.queue.composite.feedback;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jsimulation.r5.SimEventList;

/** Abstract (general) Feedback queue.
 * 
 * <p>
 * Under the hood, a delegate job for each {@link SimJob} visits the (single) embedded {@link SimQueue},
 * and upon departing from that queue, the delegate job is optionally fed back to the embedded queue's input.
 * Feedback is controlled through a {@link SimQueueFeedbackController}, allowing maximum flexibility.
 * The feedback controller is passed upon construction and cannot be changed afterwards.
 * An internal {@link FeedbackSimQueueSelector} is used as {@link SimQueueSelector} for the super class.
 * 
 * <p>
 * After the delegate job departs the embedded queue and is not fed back, the "real" job departs
 * from the {@link AbstractFeedbackSimQueue}.
 *
 * <p>
 * This and derived queues use the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
public abstract class AbstractFeedbackSimQueue
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractFeedbackSimQueue>
  extends AbstractSimQueueComposite_LocalStart<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Auxiliary method to create the required {@link Set} of {@link SimQueue}s in the constructor.
   * 
   * @param queue  The queue.
   * 
   * @return A {@link LinkedHashSet} holding the {@link SimQueue}.
   * 
   */
  private static Set<SimQueue> createQueuesSet (final SimQueue queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (queue);
    return set;
  }
  
  /** Creates an (abstract) feedback queue given an event list, a queue and a feedback controller.
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
  protected AbstractFeedbackSimQueue
  (final SimEventList eventList,
    final DQ queue,
    final SimQueueFeedbackController<J, DQ> feedbackController,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      (Set<DQ>) createQueuesSet (queue),
      new FeedbackSimQueueSelector<> (queue, feedbackController),
      delegateSimJobFactory);
    this.feedbackController = feedbackController;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENCAPSULATED QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the encapsulated queue.
   * 
   * @return The encapsulated queue, non-<code>null</code>.
   * 
   */
  public final SimQueue<DJ, DQ> getEncapsulatedQueue ()
  {
    return getQueues ().iterator ().next ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FEEDBACK CONTROLLER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The feedback controller.
   * 
   */
  private final SimQueueFeedbackController<J, DQ> feedbackController;
  
  /** Returns the feedback controller.
   * 
   * @return The feedback controller, non-<code>null</code>.
   * 
   */
  public final SimQueueFeedbackController<J, DQ> getFeedbackController ()
  {
    return this.feedbackController;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method.
   * 
   * <p>
   * Note that the {@link SimQueueSelector} of this composite queue is reset by our super class.
   * In turn, the selector will reset the embedded {@link SimQueueFeedbackController}.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
