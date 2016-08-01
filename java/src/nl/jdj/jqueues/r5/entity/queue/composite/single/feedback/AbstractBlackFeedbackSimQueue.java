package nl.jdj.jqueues.r5.entity.queue.composite.single.feedback;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite_LocalStartModel;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** Abstract (general) Feedback queue.
 * 
 * <p>
 * Under the hood, a delegate job for each {@link SimJob} visits the (single) embedded {@link SimQueue},
 * and upon departing from that queue, the delegate job is optionally fed back to the embedded queue's input.
 * Feedback is controlled through a {@link SimQueueFeedbackController}, allowing maximum flexibility.
 * 
 * <p>
 * After the delegate job departs the embedded queue and is not fed back, the "real" job departs
 * from the {@link AbstractBlackFeedbackSimQueue}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public abstract class AbstractBlackFeedbackSimQueue
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackFeedbackSimQueue>
  extends AbstractBlackSimQueueComposite_LocalStartModel<DJ, DQ, J, Q>
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
  
  /** Creates an (abstract) black feedback queue given an event list, a queue and a feedback controller.
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
  protected AbstractBlackFeedbackSimQueue
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
  
  /** Calls super method and clear the administration of visits.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    // This is good, BUT:
    // XXX Resetting the selector should be done at higher level.
    // this.visits.clear ();
  }

}
