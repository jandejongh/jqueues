package nl.jdj.jqueues.r5.entity.jq.queue.composite.enc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.SimEntityOperation;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Notification;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Processor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue} (abstract implementation).
 *
 * <p>
 * This composite queue takes as single {@link SimQueue} as argument (the <i>encapsulated</i> queue, or <i>the</i> sub-queue),
 * and mimics that queue's interface in all its aspects (by default).
 * Specific concrete implementations seek to (1) subtly modify the behavior of the embedded queue,
 * and/or (2) subtly transform the view on the embedded queue.
 * 
 * <p>
 * This base class implements the case in which the behavior of the composite queue precisely matches that of the
 * encapsulated queue (including non-standard operations and notifications).
 * Encapsulator queues by default inherit the QoS structure from the encapsulated queue.
 * 
 * <p>
 * Encapsulator queues are said to have {@code Encapsulator} model,
 * as opposed to e.g. {@code LocalStart} model as explained in {@link AbstractSimQueueComposite_LocalStart}.
 * In comparison with the latter,
 * encapsulator queues have a lot more freedom in mapping between the respective {@link SimQueueComposite}
 * interfaces of the encapsulator and the encapsulated queue.
 * This explains why many methods in this abstract base class are not {@code final}.
 * Pretty much the only invariant sub-classes must stick to is a 1:1 mapping between
 * jobs on the composite and encapsulated queues.
 * In other words, any job present in the composite queue must also be present on the encapsulated queue
 * and (obviously) vice versa.
 * 
 * <p>
 * The default behavior is implemented in {@link Enc},
 * and is as follows (sloppily erasing the distinction between real and delegate jobs):
 * <ul>
 * <li>
 * If a job arrives at the encapsulator, it arrives at the encapsulated queue.
 * <li>
 * If a job is dropped on the encapsulated queue, it is dropped at the encapsulator.
 * <li>
 * If a job is revoked on the encapsulator (after checking the {@code interruptService} flag),
 * it is forcibly revoked from the encapsulated queue.
 * <li>
 * If a job is auto-revoked on the encapsulator (e.g., due to {@link #getAutoRevocationPolicy} conditions on the composite queue),
 * it is forcibly revoked from the encapsulated queue.
 * <li>
 * If a job is auto-revoked on the encapsulated queue, it is auto-revoked on the encapsulator.
 * <li>
 * If a job starts on the encapsulated queue, it starts on the encapsulator.
 * <li>
 * If a job departs from the encapsulated queue, it departs from the encapsulator.
 * </ul>
 * 
 * <p>
 * However, again we stress the fact that concrete sub-classes may in various ways
 * change and/or augment this behavior.
 * For instance, the {@link EncHS} system hides start-events from the encapsulated queue;
 * all real jobs are thus always in the waiting area of the encapsulator.
 * The {@link EncXM} system can map exit methods of delegate jobs on the encapsulated queue
 * onto other exit methods on the encapsulator, e.g., jobs being dropped on the encapsulated
 * queue appear as departures on the encapsulator.
 * 
 * <p>
 * Users of concrete sub-classes of {@link AbstractEncapsulatorSimQueue}
 * can rely on the fact that <i>no</i> auto-revocation
 * triggers are set on the encapsulated queue.
 * As a result, such conditions may have been set already upon the supplied encapsulated queue upon construction.
 * The encapsulator will simply "inherit" the auto-revocation conditions.
 * However, this comes at a price: Sub-classes have complete freedom in adding additional
 * auto-revocation conditions <i>at the encapsulator level</i>.
 * Hopefully, such additional conditions are properly documented in the sub-class.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
 * @see Enc
 * @see EncHS
 * @see EncTL
 * @see EncJL
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
public abstract class AbstractEncapsulatorSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractEncapsulatorSimQueue>
  extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue given an event list and a sub-queue, mimicking the sub-queue's behavior.
   *
   * <p>
   * This composite queue mimics the QoS behavior of the encapsulated queue,
   * and all its operation and notification types.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see #getRegisteredOperations
   * @see #registerDelegatedOperation
   * @see #getRegisteredNotificationTypes
   * @see #registerNotificationType
   * 
   */
  public AbstractEncapsulatorSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      Collections.singleton (queue),
      new SimQueueSelector<J, DQ> ()
      {
        @Override
        public void resetSimQueueSelector ()
        {
        }
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
    // Find the operations on the encapsulated queue that we do not know, and install a delegate for it.
    for (final SimEntityOperation oDQueue : (Set<SimEntityOperation>) queue.getRegisteredOperations ())
      if (! getRegisteredOperations ().contains (oDQueue))
        registerDelegatedOperation (oDQueue, new DelegatedSimQueueOperation (this, queue, oDQueue, this.realDelegateJobMapper));
    // Register unknown notifications from the encapsulated queue.
    for (final SimEntitySimpleEventType.Member nDQueue :
      (Set<SimEntitySimpleEventType.Member>) queue.getRegisteredNotificationTypes ())
      if (! getRegisteredNotificationTypes ().contains (nDQueue))
        registerNotificationType (nDQueue, null);
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
    return getQueue (0);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the QoS value of the encapsulated queue.
   * 
   * @return The QoS value of the encapsulated queue.
   * 
   */
  @Override
  public Object getQoS ()
  {
    return getEncapsulatedQueue ().getQoS ();
  }

  /** Returns the QoS class of the encapsulated queue.
   * 
   * @return The QoS class of the encapsulated queue.
   * 
   */
  @Override
  public Class getQoSClass ()
  {
    return getEncapsulatedQueue ().getQoSClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates the delegate job, administers it and puts the (real) job into {@link #jobQueue}.
   * 
   * @see #addRealJobLocal
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected void insertJobInQueueUponArrival (final J job, final double time)
  {
    addRealJobLocal (job);
  }

  /** Lets the delegate job arrive at the encapsulated queue.
   * 
   * @see SimQueue#arrive
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected void rescheduleAfterArrival (final J job, final double time)
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
   * A (real) job can <i>only</i> be dropped because of one of the following two reasons:
   * <ul>
   * <li>Its delegate job is dropped (autonomously) on the sub-queue,
   *     see {@link #processSubQueueNotifications}.
   *     In this case,
   *     the delegate job has already left the sub-queue when we are called,
   *     hence no action is required to remove it from there.
   *     All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * <li>The composite queue (us) enforces dropping the job.
   *     In this case, the delegate job <i>may</i> still be present on the sub-queue,
   *     and we have to forcibly revoke it on the sub-queue if so.
   *     We abuse {@link #removeJobFromQueueUponRevokation} to initiate the revocation
   *     (note that we cannot directly invoke {@link #revoke} or {@link #autoRevoke} on the composite queue
   *     as that would trigger an incorrect revocation notification).
   * </ul>
   * 
   * @throws IllegalStateException If the real or delegate job does not exist.
   * 
   * @see #drop
   * @see #rescheduleAfterDrop
   * @see #removeJobFromQueueUponRevokation
   * @see #removeJobsFromQueueLocal
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected void removeJobFromQueueUponDrop (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      removeJobFromQueueUponRevokation (job, time, true);
    else
      removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Enforces the scheduled revocation on the sub-queue, if applicable.
   * 
   * @see #removeJobFromQueueUponDrop
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected void rescheduleAfterDrop (final J job, final double time)
  {
    if (this.pendingDelegateRevocationEvent != null)
      rescheduleAfterRevokation (job, time, true);
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
  private SimJQEvent.Revocation<DJ, DQ> pendingDelegateRevocationEvent = null;

  /** Removes a job upon successful revocation, as determined by our super-class,
   *  or upon auto-revocation on the encapsulator (i.e., sub-class) or on the encapsulated queue.
   * 
   * <p>
   * In case of an auto-revocation,
   * the presence or absence of the job on the encapsulated queue determines
   * whether auto-revocation is caused by the composite queue or by the encapsulated queue,
   * respectively.
   * In the latter case, all we have to do is remove the real job from our local admin.
   * 
   * <p>
   * In the former case,
   * and in case of (plain) revocation,
   * we have to forcibly remove the delegate job from the encapsulated queue.
   * To that effect, this method interacts delicately with {@link #rescheduleAfterRevokation}
   * and the {@link MultiSimQueueNotificationProcessor} on the sub-queue,
   * through the use of a pending revocation event (a local private field).
   * 
   * <p>
   * We have to forcibly revoke the delegate job still present on the encapsulated sub-queue.
   * Because we cannot perform the revocation here (we are <i>not</i> allowed to reschedule!),
   * we defer until {@link #removeJobFromQueueUponRevokation} by raising an internal flag
   * (in fact a newly created, though not scheduled {@link Revocation}).
   * We have to use this method in order to remember the delegate job to be revoked,
   * and the queue from which to revoke it,
   * both of which are wiped from the internal administration by {@link #removeJobsFromQueueLocal},
   * which is invoked last.
   * 
   * <p>
   * Note that even though a {@link Revocation} is used internally to flag the
   * required delegate-job revocation, it is never actually scheduled on the event list!
   * 
   * @throws IllegalStateException If the delegate job is <i>not</i> visiting the sub-queue while it should,
   *                               if a pending delegate revocation has already been flagged (or been forgotten to clear),
   *                               or if any other sanity check fails.
   * 
   * @see #revoke
   * @see #autoRevoke
   * @see Revocation
   * @see #rescheduleAfterRevokation
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * @see #removeJobsFromQueueLocal
   * 
   */
  @Override
  protected void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    // This checks the existance of the delegate job in our local admin (throws an exception if not).
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (auto)
    {
      // An auto-revocation; either from the encapsulated queue due to its (initial) auto-revocation conditions,
      // or due to the encapsulator's auto-revocation settings (perhaps implemented in sub-classes).
      // The two cases are distinguished by the presence or absence of the delegate job on the encapsulated queue.
      if (subQueue == null)
      {
        // The job has been auto-revoked from on encapsulated queue; so...., we didn't do it :-).
        // All there is left to do now is remove the real job from our local admin, and return.
        // If, however, there is a pending revocation event,
        // we got caught up in the middle of a forced revocation on the encapsulated queue.
        // We're in trouble now; this really should not happen...
        if (this.pendingDelegateRevocationEvent != null)
          throw new IllegalStateException ();
        // One more check: our caller must have updated the time, and have started an atomic notification,
        // since the auto-revocation must be a top-level (internal) event.
        if (time != getLastUpdateTime () || clearAndUnlockPendingNotificationsIfLocked ())
          throw new IllegalStateException ();
        removeJobsFromQueueLocal (job, delegateJob);
        return;
      }
      else
        // We (as a composite queue or encapsulator) are causing the auto-revocation ourselves.
        // We must forcibly revoke the delegate job from the encapsulated queue,
        // but that is exactly identical to what needs to be done in case of a job revocation.
        // Hence, we simply fall through.
        ; /* EMPTY; FALL THROUGH INTO REMAINDER OF BODY */
    }
    // We are about to forcibly revoke the delegate job from the encapsulated queue.
    if (subQueue == null)
      // This state is illegal; the delegate job MUST be present at the sub-queue.
      throw new IllegalStateException ();
    // Revoke the delegate job on the sub-queue.
    // Throw illegal state if such a forced delegate-job revocation is still pending.
    if (this.pendingDelegateRevocationEvent != null)
      throw new IllegalStateException ();
    // Prepare the revocation event for rescheduleAfterRevokation.
    this.pendingDelegateRevocationEvent = new SimJQEvent.Revocation<> (delegateJob, subQueue, time, true);
    // Remove the job and delegate job from our admin anyhow.
    // rescheduleAfterRevokation and the sub-queue event processor take special care of this condition.
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** If present, performs the pending revocation on the applicable sub-queue, and check whether that succeeded.
   * 
   * <p>
   * This method interacts delicately with {@link #removeJobFromQueueUponRevokation}
   * and the {@link MultiSimQueueNotificationProcessor} on the sub-queues,
   * through the use of a pending revocation event (a local private field).
   * 
   * <p>
   * Upon return, the pending revocation event has been reset to {@code null}.
   * 
   * @throws IllegalStateException If this is not an auto-revocation and no pending delegate revocation was found,
   *                               or revoking the delegate job failed
   *                               (as indicated by the failure to reset the pending revocation event by the
   *                                sub-queue notification processor, see {@link #processSubQueueNotifications}).
   * 
   * <p>
   * Note that even though a {@link Revocation} is used internally to flag the
   * required delegate-job revocation, it is never actually scheduled on the event list!
   * 
   * @see Revocation
   * @see #removeJobFromQueueUponRevokation
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  protected void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    // Entry note: We already removed real and delegate jobs from our local admin.
    if (auto && this.pendingDelegateRevocationEvent == null)
      // A job was auto-revoked on the encapsulated queue; we removed real and delegate jobs from our local admin.
      // There's really nothing more to do now.
      return;
    // At this point, revocation or auto-revocation must result in the forced revocation of
    // the delegate job on the encapsulated queue.
    if (this.pendingDelegateRevocationEvent == null)
      // This state is illegal; a real job was revoked, which should always result in a forced delegate-job revocation.
      throw new IllegalStateException ();
    // Effectuate the pending revocation event by directly invoking the event's action.
    // We reset the pendingDelegateRevocationEvent to null in the sub-queue event processor
    // upon receiving the revocation acknowledgement!
    this.pendingDelegateRevocationEvent.getEventAction ().action (this.pendingDelegateRevocationEvent);
    // Check that sub-queue actually confirmed the revocation.
    if (this.pendingDelegateRevocationEvent != null)
      throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the {@code startArmed} state of the encapsulated queue.
   * 
   * @return The {@code startArmed} state of the of the encapsulated queue.
   * 
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  public boolean isStartArmed ()
  {
    return getEncapsulatedQueue ().isStartArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Copies the new server-access credits into the encapsulated queue.
   * 
   * @see #getServerAccessCredits
   * @see #getLastUpdateTime
   * @see SimQueue#setServerAccessCredits
   * @see #rescheduleForNewServerAccessCredits
   * 
   */
  @Override
  protected void setServerAccessCreditsSubClass ()
  {
    getEncapsulatedQueue ().setServerAccessCredits (getLastUpdateTime (), getServerAccessCredits ());
  }
  
  /** Does nothing.
   * 
   * <p>
   * This method does nothing (we follow the server-access credits on the
   * encapsulated queue, and only set them upon external request).
   * 
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected void rescheduleForNewServerAccessCredits (final double time)
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job in the service area (after sanity checks).
   * 
   * @throws IllegalStateException If sanity checks on internal consistency fail.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getDelegateJob
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Does nothing.
   * 
   * <p>
   * This method does nothing;
   * we are merely being notified of the start of a delegate
   * job on the encapsulated queue,
   * and our own notification will be dealt with by our caller, {@link #start}.
   * 
   * @throws IllegalStateException If sanity checks on internal consistency fail.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getDelegateJob
   * @see #insertJobInQueueUponStart
   * 
   */
  @Override
  protected void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || (! this.jobsInServiceArea.contains (job)))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method.
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected double getServiceTimeForJob (final J job)
  {
    return super.getServiceTimeForJob (job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link AbstractEncapsulatorSimQueue},
   * a (real) job can <i>only</i> depart because of one of the following two reasons:
   * <ul>
   * <li>Its delegate job departs (autonomously) on the encapsulated queue,
   *     see {@link #processSubQueueNotifications}.
   *     In this case,
   *     the delegate job has already left the sub-queue when we are called,
   *     hence no action is required to remove it from there.
   *     All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * <li>The composite queue (us) enforces departure the job.
   *     In this case, the delegate job <i>may</i> still be present on a sub-queue,
   *     and we have to forcibly revoke it on the sub-queue if so.
   *     We abuse {@link #removeJobFromQueueUponRevokation} to initiate the revocation
   *     (note that we cannot directly invoke {@link #revoke} or {@link #autoRevoke} on the composite queue
   *     as that would trigger an incorrect revocation notification).
   * </ul>
   * 
   * @throws IllegalStateException If the real or delegate job does not exits.
   * 
   * @see #depart
   * @see #rescheduleAfterDeparture
   * @see #removeJobFromQueueUponRevokation
   * @see #removeJobsFromQueueLocal
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    final DJ delegateJob = getDelegateJob (departingJob);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      removeJobFromQueueUponRevokation (departingJob, time, true);
    else
      removeJobsFromQueueLocal (departingJob, delegateJob);
  }

  /** Enforces the scheduled revocation on the sub-queue, if applicable.
   * 
   * @see #removeJobFromQueueUponDeparture
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (this.pendingDelegateRevocationEvent != null)
      rescheduleAfterRevokation (departedJob, time, true);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queues, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} notifications from all sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for all sub-queues) created upon construction,
   * see {@link Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * This method takes one notification at a time, starting at the head of the list, removes it,
   * and processes the notification as described below.
   * While processing, new notifications may be added to the list; the list is processed until it is empty.
   * 
   * <p>
   * However, before processing any event, it checks for {@link SimEntitySimpleEventType#RESET}
   * (sub-)notifications. If it finds <i>any</i>, the notifications list is cleared and immediate return from this method follows.
   * A reset event, however, is subjected to rigorous sanity checks; notably, it has to be an isolated atomic event.
   * Failure of the sanity checks will lead to an {@link IllegalStateException}.
   * 
   * <p>
   * Otherwise, this method processes the notifications as described below;
   * the remainder of the method is encapsulated in a
   * {@link #clearAndUnlockPendingNotificationsIfLocked} and {@link #fireAndLockPendingNotifications} pair,
   * to make sure we create atomic notifications in case of a top-level event.
   * 
   * <p>
   * A notification consists of a (fixed) sequence of sub-notifications,
   * see {@link Notification#getSubNotifications},
   * each of which is processed in turn as follows:
   * <ul>
   * <li>With {@link SimEntitySimpleEventType#RESET}, impossible, see above; throws an {@link IllegalStateException}.
   * <li>With queue-access vacation related events,
   *          {@link SimQueueSimpleEventType#QUEUE_ACCESS_VACATION},
   *          {@link SimQueueSimpleEventType#QAV_START},
   *          {@link SimQueueSimpleEventType#QAV_END},
   *          we do nothing (allowed for sub-class use).
   * <li>With server-access credits related events
   *          {@link SimQueueSimpleEventType#SERVER_ACCESS_CREDITS},
   *          {@link SimQueueSimpleEventType#REGAINED_SAC},
   *          {@link SimQueueSimpleEventType#OUT_OF_SAC},
   *          we so nothing (allowed for sub-class use).
   * <li>We ignore start-armed related events
   *          {@link SimQueueSimpleEventType#STA_FALSE},
   *          {@link SimQueueSimpleEventType#STA_TRUE},
   *          as these are dealt with (if at all) by the outer loop.
   * <li>With {@link SimQueueSimpleEventType#ARRIVAL}, we do nothing.
   * <li>With {@link SimQueueSimpleEventType#DROP}, we drop the real job through {@link #drop}.
   * <li>With {@link SimQueueSimpleEventType#REVOCATION}, we check for the presence of a corresponding real job through
   *                                                      {@link #getRealJob}, and throw an {@link IllegalStateException}
   *                                                      if we found one. Revocation notifications must always be the result
   *                                                      of the composite queue's {@link #revoke} operation, and at this stage,
   *                                                      the real job has already been removed from the composite queue.
   *                                                      Subsequently, we perform sanity checks on the pending revocation event,
   *                                                      again throwing an {@link IllegalStateException} in case of an error.
   *                                                      If all is well, we simply clear the pending revocation event
   *                                                      on the composite queue.
   * <li>With {@link SimQueueSimpleEventType#AUTO_REVOCATION},
   *          we invoke {@link #autoRevoke} (allowed for encapsulated-queue or sub-class use).
   * <li>With {@link SimQueueSimpleEventType#START}, we start the real job.
   * <li>With {@link SimQueueSimpleEventType#DEPARTURE}, we make the real job depart.
   * <li>With any non-standard notification type, we pass the notification using {@link SimJQEvent#copyForQueueAndJob}
   *     to map between real and delegate job and between composite and encapsulated queue.
   * </ul>
   * 
   * <p>
   * After all notifications have been processed, and the notification list is empty,
   * we invoke {@link #triggerPotentialNewStartArmed} on the composite queue,
   * in order to make sure we are not missing an autonomous change in {@link SimQueue#isStartArmed}
   * on a sub-queue.
   * Since we do not expect any back-fire notifications from sub-queues from that method,
   * we check again the notification list, and throw an exception if it is non-empty.
   * 
   * <p>
   * A full description of the sanity checks would make this entry uninterestingly large(r), hence we refer to the source code.
   * Most checks are trivial checks on the allowed sub-notifications from the sub-queues
   * and on the presence or absence of real and delegate jobs
   * (and their expected presence or absence on a sub-queue).
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty, or contains a notification from another queue
   *                                  than the a sub-queue,
   *                                  or if other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see SimEntitySimpleEventType#RESET
   * @see SimQueueSimpleEventType#ARRIVAL
   * @see SimQueueSimpleEventType#QUEUE_ACCESS_VACATION
   * @see SimQueueSimpleEventType#QAV_START
   * @see SimQueueSimpleEventType#QAV_END
   * @see SimQueueSimpleEventType#SERVER_ACCESS_CREDITS
   * @see SimQueueSimpleEventType#OUT_OF_SAC
   * @see SimQueueSimpleEventType#REGAINED_SAC
   * @see SimQueueSimpleEventType#STA_FALSE
   * @see SimQueueSimpleEventType#STA_TRUE
   * @see SimQueueSimpleEventType#DROP
   * @see SimQueueSimpleEventType#REVOCATION
   * @see SimQueueSimpleEventType#AUTO_REVOCATION
   * @see SimQueueSimpleEventType#START
   * @see SimQueueSimpleEventType#DEPARTURE
   * @see #addPendingNotification
   * @see SimQueue#arrive
   * @see #start
   * @see #selectNextQueue
   * @see #depart
   * @see #triggerPotentialNewStartArmed
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * 
   */
  @Override
  protected void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    //
    // SANITY: Should receive at least one notification.
    //
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ();
    //
    // Special treatment of RESET notifications: clear the list of notifications, ignore them, and return immediately.
    //
    // Either the sub-queue was reset from the event list, and we will follow shortly,
    // or we were reset ourselves, and forced this upon our sub-queues.
    // Either way, the reset notification has to be a fully isolated one; it cannot be issued in conjunction with
    // other sub-queue events, so we make a rigorous check.
    //
    // However, in the end, we are safe to ignore the event here.
    //
    if (MultiSimQueueNotificationProcessor.contains (notifications, SimEntitySimpleEventType.RESET))
    {
      if (notifications.size () != 1
      ||  notifications.get (0).getSubNotifications ().size () != 1
      || ! (((SimEntityEvent) notifications.get (0).getSubNotifications ().get (0).get (SimEntitySimpleEventType.RESET))
            instanceof SimEntityEvent.Reset))
        throw new IllegalStateException ();
      notifications.clear ();
      return;
    }
    //
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    //
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    //
    // Iterate over all notifications, noting that additional notifications may be added as a result of our processing.
    //
    while (! notifications.isEmpty ())
    {
      //
      // Remove the notification at the head of the list.
      //
      final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification = notifications.remove (0);
      //
      // Sanity check on the notification time (should be our last update time, i.e.,, out current time).
      //
      final double notificationTime = notification.getTime ();
      if (notification.getTime () != getLastUpdateTime ())
        throw new IllegalArgumentException ("on " + this + ": notification time [" + notification.getTime ()
        + "] != last update time [" + getLastUpdateTime () + "], subnotifications: "
        + notification.getSubNotifications () + ".");
      //
      // Sanity check on the queue that issued the notification; should be one of our sub-queues.
      //
      final DQ subQueue = notification.getQueue ();
      if (subQueue == null || ! getQueues ().contains (subQueue))
        throw new IllegalStateException ();
      //
      // Iterate over all sub-notifications from this queue.
      //
      for (final Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>> subNotification
        : notification.getSubNotifications ())
      {
        if (subNotification == null || subNotification.size () != 1)
          throw new RuntimeException ();
        final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
        final SimJQEvent<DJ, DQ> notificationEvent = subNotification.values ().iterator ().next ();
        if (notificationEvent == null)
          throw new RuntimeException ();
        final DJ job = subNotification.values ().iterator ().next ().getJob ();
        //
        // Sanity check on the (delegate) job (if any) to which the notification applies.
        //
        if (job != null)
        {
          //
          // We must have a real job corresponding to the delegate job,
          // except in case of a revocation (the composite queue has already disposed the real job from its administration).
          //
          if (notificationType != SimQueueSimpleEventType.REVOCATION)
            getRealJob (job);
        }
        if (notificationType == SimEntitySimpleEventType.RESET)
          //
          // If we receive a RESET notification at this point, we throw an exception, since we already took care of RESET
          // notifications earlier on the initial set of notifications.
          // Their appearance here indicates the RESET was due to our own actions on the sub-queue(s),
          // and added later, which is unexpected.
          //
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.QUEUE_ACCESS_VACATION
             ||  notificationType == SimQueueSimpleEventType.QAV_START
             ||  notificationType == SimQueueSimpleEventType.QAV_END)
        {
          //
          // Queue-Access Vacations are allowed on the encapsulated queue.
          //
          ; /* NOTHING TO DO */
        }
        else if (notificationType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS
             ||  notificationType == SimQueueSimpleEventType.OUT_OF_SAC
             ||  notificationType == SimQueueSimpleEventType.REGAINED_SAC)
          //
          // Server-Acess Credits events are, if even relevant, taken care of by the outer loop
          // and the AbstractSimQueue implementation automatically,
          // so we must ignore them here.
          //
          ; /* NOTHING TO DO */
        else if (notificationType == SimQueueSimpleEventType.STA_FALSE
             ||  notificationType == SimQueueSimpleEventType.STA_TRUE)
          //
          // StartArmed events are taken care of by the outer loop
          // and the AbstractSimQueue implementation automatically,
          // so we must ignore them here.
          //
          ; /* NOTHING TO DO */
        else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        {
          //
          // A (delegate) job (pseudo) arrives at the sub-queue.
          // In any case, at this point, we sent the (delegate) job to that sub-queue ourselves.
          //
          ; /* NOTHING TO DO */
        }
        else if (notificationType == SimQueueSimpleEventType.DROP)
        {
          //
          // A job has been dropped from the encapsulated queue, hence we must drop the (real) job.
          //
          final J realJob = getRealJob (job);
          drop (realJob, notificationTime);
        }
        else if (notificationType == SimQueueSimpleEventType.REVOCATION)
        {
          //
          // A (delegate) job is revoked on the encapsulated queue.
          // This should always be the result from a revocation request on
          // the composite queue, and the real job should already have left the latter queue.
          // We perform sanity checks and clear the pending revocation event.
          //
          if (isDelegateJob (job))
            throw new IllegalStateException ();
          if (this.pendingDelegateRevocationEvent == null
          || this.pendingDelegateRevocationEvent.getQueue () != subQueue
          || this.pendingDelegateRevocationEvent.getJob () != job)
            throw new IllegalStateException ();
          this.pendingDelegateRevocationEvent = null;
        }
        else if (notificationType == SimQueueSimpleEventType.AUTO_REVOCATION)
          //
          // An auto-revocation on the sub-queue.
          // Not used in the base class, but we allow it for use in sub-classes.
          //
          autoRevoke (notificationTime, getRealJob (job));
        else if (notificationType == SimQueueSimpleEventType.START)
          start (notificationTime, getRealJob (job));          
        else if (notificationType == SimQueueSimpleEventType.DEPARTURE)
          depart (notificationTime, getRealJob (job));
        else
        {
          //
          // We received a "non-standard" SimQueue event.
          // We pass then on at the composite-queue level,
          // but we have to replace both queue (always) and job (if applicable).
          // 
          final J realJob = (job != null ? getRealJob (job) : null);
          // XXX Shouldn't we check if this notification type was actually registered at the composite queue?
          // Or does out super-class take care of that?
          addPendingNotification (notificationType,
            (SimJQEvent<J, Q>) notificationEvent.copyForQueueAndJob ((DQ) this, (DJ) realJob));
        }
      }
    }
    triggerPotentialNewStartArmed (getLastUpdateTime ());
    if (! notifications.isEmpty ())
      throw new IllegalStateException ();
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
