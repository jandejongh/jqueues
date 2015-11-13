package nl.jdj.jqueues.r5.entity.queue.composite.single.feedback;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for feedback queues.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 * 
 */
public class FeedbackSimQueueSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} for a feedback queue.
   * 
   * @param queue              The queue, non-<code>null</code>.
   * @param feedbackController The feedback controller, non-<code>null</code>.
   * 
   * @throws IllegalArgumentException If the queue or feedback controller is <code>null</code>.
   * 
   */
  public FeedbackSimQueueSelector (final DQ queue, final SimQueueFeedbackController<J, DQ> feedbackController)
  {
    if (queue == null || feedbackController == null)
      throw new IllegalArgumentException ();
    this.queue = queue;
    this.feedbackController = feedbackController;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final DQ queue;
  
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
  // VISITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<J, Integer> visits = new HashMap<> ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  // XXX
  // @Override
  public void resetSimQueueSelector ()
  {
    this.visits.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the embedded queue.
   * 
   * <p>
   * Updates the visits administration.
   * 
   * @return The embedded {@link SimQueue}.
   * 
   */
  @Override
  public DQ selectFirstQueue (final double time, final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (this.visits.containsKey (job))
      throw new IllegalStateException ();
    this.visits.put (job, 0);
    return this.queue;
  }

  /** Returns the embedded queue if the delegate job is to be fed back, {@code null} otherwise.
   * 
   * <p>
   * Updates the visits administration.
   * 
   * @return If consultation of the feedback controller indicates a new feedback is needed,
   *         returns the embedded {@link SimQueue},
   *         <code>null</code> otherwise.
   * 
   * @see SimQueueFeedbackController
   * @see #getFeedbackController
   * 
   */
  @Override
  public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (previousQueue != this.queue)
      throw new IllegalStateException ();
    if ((! this.visits.containsKey (job)) || this.visits.get (job) == null)
      throw new IllegalStateException ();
    this.visits.put (job, this.visits.get (job) + 1);
    if (this.feedbackController.feedback (time, previousQueue, job, this.visits.get (job)))
      return previousQueue;
    else
    {
      this.visits.remove (job);
      return null;
    }
  }
  
}
