package nl.jdj.jqueues.r5.entity.queue.composite.single.enc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite.StartModel;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link BlackSimQueueComposite} encapsulating a single {@link SimQueue}.
 *
 * <p>
 * This composite queue mimics the {@link SimQueue} interface of the encapsulated queue.
 * 
 * <p>The main purpose of this apparently rather futile {@link SimQueue}
 * is to test the maturity of the {@link SimQueue} interface and its notifications:
 * Can we reconstruct a {@link SimQueue} interface by acting on and monitoring another {@link SimQueue}?.
 * It is, however, also useful to extract a bare {@link SimQueue} interface at the {@code Java} level
 * from a much more complicated queue implementation.
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the black composite queue.
 * For more details, refer to {@link StartModel#ENCAPSULATOR_QUEUE}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see BlackSimQueueComposite
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
 * @see #setStartModel
 * 
 */
public class BlackEncapsulatorSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackEncapsulatorSimQueue>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black encapsulator queue given an event list and a queue.
   *
   * <p>
   * The constructor sets the {@link StartModel} to {@link StartModel#ENCAPSULATOR_QUEUE}.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see StartModel
   * @see StartModel#ENCAPSULATOR_QUEUE
   * @see #setStartModel
   * 
   */
  public BlackEncapsulatorSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      Collections.singleton (queue),
      new SimQueueSelector<J, DQ> ()
      {
        @Override
        public DQ selectFirstQueue (final double time, final J job)
        {
          return queue;
        }
        @Override
        public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
        {
          if (previousQueue != queue)
            throw new IllegalArgumentException ();
          return null;
        }
      },
      delegateSimJobFactory);
    setStartModel (StartModel.ENCAPSULATOR_QUEUE);
  }
  
  /** Returns a new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *         queue and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackEncapsulatorSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new BlackEncapsulatorSimQueue (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENCAPSULATED QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the encapsulated queue.
   * 
   * @return The encapsulated queue, non-{@code null}.
   * 
   */
  public final SimQueue<DJ, DQ> getEncapsulatedQueue ()
  {
    return getQueues ().iterator ().next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Enc[encapsulated queue]".
   * 
   * @return "Enc[encapsulated queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "Enc[" + getQueues ().iterator ().next () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clears the pending revocation event for the encapsulated queue.
   * 
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.pendingDelegateRevocationEvent = null;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the {@code noWaitArmed} state of the encapsulated queue.
   * 
   * @return The {@code noWaitArmed} state of the encapsulated queue.
   * 
   * @see StartModel#ENCAPSULATOR_QUEUE
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getEncapsulatedQueue ().isNoWaitArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job, administers it and puts the (real) job into {@link #jobQueue}.
   * 
   * @see #addRealJobLocal
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    addRealJobLocal (job);
  }

  /** Lets the delegate job arrive at the encapsulated queue, after sanity checks.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getEncapsulatedQueue
   * @see SimQueue#arrive
   * @see #getDelegateJob
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
    getEncapsulatedQueue ().arrive (time, delegateJob);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link BlackEncapsulatorSimQueue},
   * a (real) job can only be dropped because
   * its delegate job is dropped on the encapsulated queue, see {@link #processSubQueueNotifications},
   * and the semantics of this composite queue require the real job to be dropped as well (this is not a general requirement).
   * The notification callback relies on {@link #drop} to perform the drop.
   * The delegate job has already left the sub-queue system when we are called.
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * 
   * @throws IllegalStateException If the delegate job is still visiting a (any) queue.
   * 
   * @see #drop
   * @see #removeJobsFromQueueLocal
   * @see #rescheduleAfterDrop
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      throw new IllegalStateException ();
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Empty, nothing to do.
   * 
   * @see #removeJobFromQueueUponDrop
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The pending delegate revocation event is a single event used to store the need to revoke a delegate job
   *  on the sub queue it resides on.
   * 
   * <p>
   * It is needed in-between {@link #removeJobFromQueueUponRevokation} and {@link #rescheduleAfterRevokation}.
   * 
   */
  private SimQueueJobRevocationEvent pendingDelegateRevocationEvent = null;

  /** Removes a job if revocation is successful.
   * 
   * <p>
   * In a {@link BlackEncapsulatorSimQueue}, revocations on real jobs can occur either
   * through external requests, in other words, through {@link #revoke}, or because of auto-revocations
   * on the composite (this) queue through {@link #autoRevoke}.
   * In both cases, the delegate job is still present on the encapsulated queue,
   * and we have to forcibly revoke it.
   * Because we cannot perform the revocation here (we are <i>not</i> allowed to reschedule!),
   * we defer until {@link #removeJobFromQueueUponRevokation} by raising an internal flag
   * (in fact a newly created, though not scheduled {@link SimQueueJobRevocationEvent}).
   * We have to use this method in order to remember the delegate job to be revoked,
   * which is wiped from the internal administration by {@link #removeJobsFromQueueLocal},
   * which is invoked last.
   * 
   * <p>
   * Note that even though a {@link SimQueueJobRevocationEvent} is used internally to flag the
   * required delegate-job revocation, it is never actually scheduled on the event list!
   * 
   * @throws IllegalStateException If the delegate job is <i>not</i> visiting a sub-queue,
   *                               or if a pending delegate revocation has already been flagged (or been forgotten to clear).
   * 
   * @see #revoke
   * @see #autoRevoke
   * @see SimQueueJobRevocationEvent
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue == null)
      throw new IllegalStateException ();
    if (this.pendingDelegateRevocationEvent != null)
      throw new IllegalStateException ();
    this.pendingDelegateRevocationEvent = new SimQueueJobRevocationEvent (delegateJob, subQueue, time, true);
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Performs the pending revocation on the sub-queue, after clearing it.
   * 
   * @throws IllegalStateException If no pending delegate revocation was found.
   * 
   * @see SimQueueJobRevocationEvent
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (this.pendingDelegateRevocationEvent == null)
      throw new IllegalStateException ();
    final SimQueueJobRevocationEvent event = this.pendingDelegateRevocationEvent;
    this.pendingDelegateRevocationEvent = null;
    // Invoking the event's action equivalent to next line:
    // event.getQueue ().revoke (event.getTime (), event.getJob (), event.isInterruptService ());
    event.getEventAction ().action (event);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Sets the server-access credits on the encapsulated queue.
   * 
   * @see #getEncapsulatedQueue
   * @see SimQueue#setServerAccessCredits
   * @see #getLastUpdateTime
   * @see #getServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    getEncapsulatedQueue ().setServerAccessCredits (getLastUpdateTime (), getServerAccessCredits ());
  }
  
  /** Empty, nothing to do.
   * 
   * We set the server-access credits directly on the encapsulated queue through {@link #setServerAccessCreditsSubClass},
   * so there is nothing to do here.
   * 
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job in the service area (after sanity checks).
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    this.jobsInServiceArea.add (job);
  }

  /** Nothing to do apart from sanity check.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getDelegateJob
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || (! this.jobsInServiceArea.contains (job)))
      throw new IllegalArgumentException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link BlackEncapsulatorSimQueue},
   * a (real) job can only depart when its delegate job departs from the encapsulated queue.
   * The notification callback from the encapsulated queue relies on {@link #depart} to perform the departure.
   * The delegate job has already left the sub-queue system when we are called.
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * 
   * @throws IllegalStateException If the delegate job is still visiting a (any) queue.
   * 
   * @see #depart
   * @see #removeJobsFromQueueLocal
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    final DJ delegateJob = getDelegateJob (departingJob);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      throw new IllegalStateException ();
    removeJobsFromQueueLocal (departingJob, delegateJob);
  }

  /** Empty, nothing to do.
   * 
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESS SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queue, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * This method takes one notification at a time, starting at the head of the list, removes it
   * and processes it with {@link #processEncapsulatedQueueNotification}.
   * While processing, new notifications may be added to the list; the list is processed until it is empty.
   * 
   * <p>
   * It finally invokes {@link #triggerPotentialNewNoWaitArmed} to make sure we did not miss
   * an "isolated" {@link SimQueue#isNoWaitArmed} notification from the encapsulated queue
   * that requires a change in our own {@link SimQueue#isNoWaitArmed} state,
   * because this is not explicitly dealt with in auxiliary processor methods.
   * (Note that after invocation of this method, no new (sub-queue) notifications are expected,
   * at the expense of a {@link IllegalStateException}.)
   * 
   * <p>
   * Note that this {@link BlackEncapsulatorSimQueue} still catches {@link SimEntityListener#notifyUpdate}
   * notifications in the main class body (all other notification types are dealt with through the
   * {@link MultiSimQueueNotificationProcessor}.
   * 
   * @see MultiSimQueueNotificationProcessor.Notification#getQueue
   * @see #processEncapsulatedQueueNotification
   * @see #update
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ();
    while (! notifications.isEmpty ())
    {
      //System.err.println ("-> Notifications: [t=" + getLastUpdateTime () + "]:");
      //  for (MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification : notifications)
      //    System.err.println ("   " + notification + ".");
      final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification = notifications.remove (0);
      final DQ queue = notification.getQueue ();
      if (queue == getEncapsulatedQueue ())
        processEncapsulatedQueueNotification (notification);
      else
        throw new IllegalArgumentException ();
    }
    triggerPotentialNewNoWaitArmed (getLastUpdateTime ());
    if (! notifications.isEmpty ())
      throw new IllegalStateException ();
  }
  
  /** Performs sanity checks on a notification from the encapsulated queue.
   * 
   * <p>
   * A full description of the sanity checks would make this {@code javadoc} become uninterestingly large.
   * Please refer to the documentation in the source code.
   * 
   * @param notification The notification.
   * 
   * @throws IllegalArgumentException If the sanity checks fail.
   * 
   * @see #sanitySubQueueNotification
   * 
   */
  protected final void sanityEncapsulatedQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    sanitySubQueueNotification (notification);
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        // The real job must exist (but may have already left).
        getRealJob (job);
      else if (notificationType == SimQueueSimpleEventType.START)
        // The real job must exist (but may have already left).
        getRealJob (job);
      else if (notificationType == SimEntitySimpleEventType.DROP)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.REVOCATION)
        // Do NOT check for the real job here; revocations (but NOT auto-revocations) are always caused by the composite queue,
        // hence the real job has already left!
        ;
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        // Auto revocations are not allowed on the encapsulated queue.
        throw new IllegalStateException ();
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
    }
  }
  
  /** Processes a notification from the encapsulated queue.
   * 
   * <p>
   * This method iterates over the sub-notifications (from the encapsulated queue) and:
   * <ul>
   * <li>Invokes {@link #drop} for the applicable real job upon a {@link SimEntitySimpleEventType#DROP};
   * <li>Invokes {@link #start} for the applicable real job upon a {@link SimEntitySimpleEventType#START};
   * <li>Invokes {@link #depart} for the applicable real job upon a {@link SimEntitySimpleEventType#DEPARTURE};
   * <li>Ignores all other sub-notification types (apart from sanity checks).
   * </ul>
   * 
   * <p>
   * Note that we can ignore {@link SimEntitySimpleEventType#RESET}, apart from sanity checks,
   * because autonomous resets on sub-queues are not allowed,
   * and we have inhibited resets from the event list on the sub-queues through {@link SimQueue#setIgnoreEventListReset}.
   * Hence, a reset can only be caused by a reset on the composite queue.
   * 
   * @param notification The notification.
   * 
   * @see #sanityEncapsulatedQueueNotification
   * @see SimEntitySimpleEventType#RESET
   * @see SimQueue#setIgnoreEventListReset
   * @see SimEntitySimpleEventType#DROP
   * @see SimEntitySimpleEventType#START
   * @see SimEntitySimpleEventType#DEPARTURE
   * @see #drop
   * @see #start
   * @see #depart
   * 
   */
  protected final void processEncapsulatedQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    sanityEncapsulatedQueueNotification (notification);
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.DROP)
        drop (getRealJob (job), getLastUpdateTime ());
      else if (notificationType == SimEntitySimpleEventType.START)
        start (getLastUpdateTime (), getRealJob (job));
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        depart (getLastUpdateTime (), getRealJob (job));
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE NOTIFICATIONS OTHER THAN UPDATE/STATE-CHANGE (ALL EMPTY; SEE SUB-QUEUE NOTIFICATION PROCESSOR)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyArrival (final double time, final DJ job, final DQ queue)
  {
  }
  
  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStartQueueAccessVacation (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStopQueueAccessVacation (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStart (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyDrop (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyRevocation (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyDeparture (final double time, final DJ job, final DQ queue)
  {
  }

}
