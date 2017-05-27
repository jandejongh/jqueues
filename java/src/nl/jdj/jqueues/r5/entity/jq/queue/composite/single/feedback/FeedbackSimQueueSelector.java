package nl.jdj.jqueues.r5.entity.jq.queue.composite.single.feedback;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for feedback queues.
 * 
 * <p>
 * This is the mandatory {@link SimQueueSelector} for feedback queues, as enforced in the constructor
 * of {@link AbstractFeedbackSimQueue}.
 * It uses a more convenient {@link SimQueueFeedbackController} provided upon construction.
 * 
 * <p>
 * In addition, in support for {@link NumVisitsFeedbackSimQueue},
 * it maintains the number of visits since the last reset for each "real" job in the composite queue.
 * 
 * <p>
 * If attached to a {@link AbstractFeedbackSimQueue},
 * the selector will be reset automatically by the queue when required.
 * In return, the {@link FeedbackSimQueueSelector} then automatically resets the embedded {@link SimQueueFeedbackController}.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 * 
 * @see SimQueueFeedbackController
 * @see NumVisitsFeedbackSimQueue
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
  
  /** The per-real job number of visits since the last reset.
   * 
   * <p>
   * Note that jobs are entered immediately upon arrival for a first visit;
   * the value is then initialized to zero.
   * 
   */
  private final Map<J, Integer> visits = new HashMap<> ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets the feedback controller and clears the registration of visits.
   * 
   * @see SimQueueFeedbackController#resetFeedbackController
   * 
   */
  @Override
  public void resetSimQueueSelector ()
  {
    this.feedbackController.resetFeedbackController ();
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
   * Updates the visits administration, putting the new job in with zero visits.
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
   * Updates the visits administration, removing the job when it is leaving or increasing its number of visits when it is fed back.
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
