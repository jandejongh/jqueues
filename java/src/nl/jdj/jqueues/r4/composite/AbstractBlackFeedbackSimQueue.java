package nl.jdj.jqueues.r4.composite;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

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
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
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
    final SimQueueFeedbackController<J, DJ, Q, DQ> feedbackController,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, (Set<DQ>) createQueuesSet (queue), delegateSimJobFactory);
    if (feedbackController == null)
      throw new IllegalArgumentException ();
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
  // AbstractBlackSimQueueNetwork
  // ABSTRACT METHODS FOR (SUB-)QUEUE SELECTION IN SUBCLASSES
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FEEDBACK CONTROLLER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The feedback controller.
   * 
   */
  private final SimQueueFeedbackController<J, DJ, Q, DQ> feedbackController;
  
  /** Returns the feedback controller.
   * 
   * @return The feedback controller, non-<code>null</code>.
   * 
   */
  public final SimQueueFeedbackController<J, DJ, Q, DQ> getFeedbackController ()
  {
    return this.feedbackController;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP DESTINATION QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    return super.getDropDestinationQueue (t, job, queue);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // allowDelegateJobRevocations
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code false}.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  protected final boolean getAllowDelegateJobRevocations ()
  {
    return false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // allowSubQueueAccessVacationChanges
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code false}.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  protected final boolean getAllowSubQueueAccessVacationChanges ()
  {
    return false;
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
  
  /** Calls super method and clear the administration of visits.
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.visits.clear ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return super.isNoWaitArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // startForSubClass
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void startForSubClass (final double time, final DJ job, final DQ queue)
  {
    super.startForSubClass (time, job, queue);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // notifyNewNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

}
