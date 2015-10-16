package nl.jdj.jqueues.r4.composite;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** (General) Feedback queue.
 * 
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
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
{

  /** The feedback controller.
   * 
   */
  private final SimQueueFeedbackController<J, DJ, Q, DQ> feedbackController;
  
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
    super (eventList, (Set<DQ>) createQueuesSet (queue), delegateSimJobFactory);
    if (feedbackController == null)
      throw new IllegalArgumentException ();
    this.feedbackController = feedbackController;
  }
  
  private final Map<J, Integer> visits = new HashMap<> ();

  @Override
  public final void reset ()
  {
    super.reset ();
    this.visits.clear ();
  }

  /**
   * {@inheritDoc}
   * 
   * @return The embedded {@link SimQueue}.
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getFirstQueue (final double time, final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (this.visits.containsKey (job))
      throw new IllegalStateException ();
    if (getQueues ().isEmpty ())
      throw new IllegalStateException ();
    this.visits.put (job, 0);
    return getQueues ().iterator ().next ();
  }

  /**
   * {@inheritDoc}
   * 
   * @return If consultation of the feedback controller indicates a new feedback is needed,
   *         returns the embedded {@link SimQueue},
   *         <code>null</code> otherwise.
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (getQueues ().isEmpty ())
      throw new IllegalStateException ();
    if (previousQueue != getQueues ().iterator ().next ())
      throw new IllegalStateException ();
    if (! this.visits.containsKey (job) || this.visits.get (job) == null)
      throw new IllegalStateException ();
    this.visits.put (job, this.visits.get (job) + 1);
    if (this.feedbackController.feedback (time, getDelegateJob (job), previousQueue, job, (Q) this, this.visits.get (job)))
      return previousQueue;
    else
    {
      this.visits.remove (job);
      return null;
    }
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return super.isNoWaitArmed ();
  }

  @Override
  public final void notifyNewNoWaitArmed (double time, DQ queue, boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

  @Override
  protected final void startForSubClass (double t, DJ job, DQ queue)
  {
    super.startForSubClass (t, job, queue);
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    return super.getDropDestinationQueue (t, job, queue);
  }

  /** Returns "GenFB[embedded queue]".
   * 
   * @return "GenFB[embedded queue]".
   * 
   */
  @Override
  public String toString ()
  {
    return "GenFB[" + getQueues ().iterator ().next () + "]";
  }

}
