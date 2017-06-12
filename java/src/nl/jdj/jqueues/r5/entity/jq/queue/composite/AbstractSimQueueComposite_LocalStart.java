package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.feedback.FB_p;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.feedback.FB_v;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Notification;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Processor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation with {@code LocalStart} model of a {@link SimQueueComposite}.
 * 
 * <p>
 * This abstract base class implements for a large part composite queues in which
 * the waiting area of the composite queue and its local server-access credits
 * are used to control access to the sub-queues.
 * In other words, arriving jobs may have to wait in the composite waiting area,
 * and upon entering the service area of the local,
 * arrive at the first sub-queue (if applicable).
 * 
 * <p>
 * We call this the {@code LocalStart} model for a  {@link SimQueueComposite}.
 * Many concrete implementations such as {@link Tandem},
 * {@link FB_v} and {@link FB_p} use this model.
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
public abstract class AbstractSimQueueComposite_LocalStart
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractSimQueueComposite_LocalStart>
extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract network of queues with {@code LocalStart} model.
   * 
   * 
   * @param eventList             The event list to be shared between this queue and the embedded queues.
   * @param queues                A set holding the "embedded" queues.
   * @param simQueueSelector      The object for routing jobs through the network of embedded queues;
   *                                if {@code null}, no sub-queues will be visited.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    the <code>queue</code> argument is <code>null</code>, has <code>null</code> members,
   *                                    or contains this composite queue.
   * 
   * @see SimQueueSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see #resetEntitySubClass
   * 
   */
  protected AbstractSimQueueComposite_LocalStart
  (final SimEventList eventList,
   final Set<DQ> queues,
   final SimQueueSelector<J, DQ> simQueueSelector,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, simQueueSelector, delegateSimJobFactory);
    resetEntitySubClassLocal ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code null} since QoS is not supported with the {@code LocalStart} model for composite queues.
   * 
   * @return {@code null}.
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return null;
  }

  /** Returns {@code null} since QoS is not supported with the {@code LocalStart} model for composite queues.
   * 
   * @return {@code null}.
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return null;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractSimQueueComposite_LocalStart}.
   * 
   * <p>
   * Calls super method (not if called from constructor, for which a private variant for local resets is used)
   * and clears the pending revocation event for a sub-queue.
   * 
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * @see SimQueue#resetEntity
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    // Implemented in private method to make it accessible from the constructor(s).
    resetEntitySubClassLocal ();
  }
  
  private void resetEntitySubClassLocal ()
  {
    //
    // NOTE: This method is invoked from the constructor instead of resetEntitySubClass ().
    //
    this.pendingDelegateRevocationEvent = null;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code true}.
   * 
   * @return {@code true}.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void queueAccessVacationDropSubClass (double time, J job)
  {
    super.queueAccessVacationDropSubClass (time, job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job, administers it and puts the (real) job into {@link #jobQueue}.
   * 
   * @see AbstractSimQueue#arrive
   * @see #addRealJobLocal
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    addRealJobLocal (job);
  }

  /** Starts the job if server-access credits are available.
   * 
   * @see AbstractSimQueue#arrive
   * @see #hasServerAcccessCredits
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link AbstractSimQueueComposite_LocalStart},
   * a (real) job can <i>only</i> be dropped because of one of the following two reasons:
   * <ul>
   * <li>Its delegate job is dropped (autonomously) on one of the sub-queues,
   *     see {@link #processSubQueueNotifications}.
   *     In this case,
   *     the delegate job has already left the sub-queue system when we are called,
   *     hence no action is required to remove it from there.
   *     All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * <li>The composite queue (us) enforces dropping the job.
   *     In this case, the delegate job is <i>may</i> still be present on a sub-queue,
   *     and we have to forcibly revoke it on the sub-queue if so.
   *     We abuse {@link #removeJobFromQueueUponRevokation} to initiate the revocation
   *     (note that we cannot directly invoke {@link #revoke} or {@link #autoRevoke} on the composite queue
   *     as that would trigger an incorrect revocation notification).
   * </ul>
   * 
   * @throws IllegalStateException If the real or delegate job does not exits.
   * 
   * @see AbstractSimQueue#drop
   * @see #rescheduleAfterDrop
   * @see #removeJobFromQueueUponRevokation
   * @see #removeJobsFromQueueLocal
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
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
   * @see AbstractSimQueue#drop
   * @see #removeJobFromQueueUponDrop
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
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

  /** Removes a job upon successful revocation (as determined by our super-class).
   * 
   * <p>
   * This method interacts delicately with {@link #rescheduleAfterRevokation}
   * and the {@link MultiSimQueueNotificationProcessor} on the sub-queues,
   * through the use of a pending revocation event (a local private field).
   * 
   * <p>
   * In a {@link AbstractSimQueueComposite_LocalStart}, revocations on real jobs can occur either
   * through external requests, in other words, through {@link #revoke},
   * or because of auto-revocations
   * on the composite (this) queue through {@link #autoRevoke}.
   * 
   * <p>
   * If the real job is still in the waiting area of the composite queue
   * (without presence of the delegate job in any sub-queue),
   * we suffice with cleaning up both real and delegate job through {@link #removeJobsFromQueueLocal}
   * and we are finished.
   * 
   * <p>
   * Otherwise, the delegate job is still present on a sub-queue, and we have to forcibly revoke it.
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
   * @throws IllegalStateException If a pending delegate revocation has already been flagged (or been forgotten to clear).
   * 
   * @see AbstractSimQueue#revoke
   * @see AbstractSimQueue#autoRevoke
   * @see SimJQEvent.Revocation
   * @see SimJQEvent.AutoRevocation
   * @see #rescheduleAfterRevokation
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * @see #removeJobsFromQueueLocal
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue == null)
      // All OK, the job is still in our waiting area,
      // and has not been released to a sub-queue yet (e.g., due to sac restraints).
      // No need to revoke the delegate job, just fall through towards cleaning up the local admin.
      ;
    else
    {
      // Revoke the delegate job on the sub-queue.
      // Throw illegal state if such a forced delegate-job revocation is still pending.
      if (this.pendingDelegateRevocationEvent != null)
        throw new IllegalStateException ();
      // Prepare the revocation event for rescheduleAfterRevokation.
      this.pendingDelegateRevocationEvent = new SimJQEvent.Revocation<> (delegateJob, subQueue, time, true);
    }
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
   * @throws IllegalStateException If revoking the delegate job failed
   *                               (as indicated by the failure to reset the pending revocation event by the
   *                                sub-queue notification processor, see {@link #processSubQueueNotifications}).
   * 
   * <p>
   * Note that even though a {@link Revocation} is used internally to flag the
   * required delegate-job revocation, it is never actually scheduled on the event list!
   * 
   * @see AbstractSimQueue#revoke
   * @see AbstractSimQueue#autoRevoke
   * @see SimJQEvent.Revocation
   * @see SimJQEvent.AutoRevocation
   * @see #removeJobFromQueueUponRevokation
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    if (this.pendingDelegateRevocationEvent == null)
      // All OK, the job is revoked from our own waiting area,
      // and the delegate job was not on any sub-queue.
      // Nothing to do here!
      ;
    else
    {
      // Effectuate the pending revocation event by directly invoking the event's action.
      // We reset the pendingDelegateRevocationEvent to null in the sub-queue event processor
      // upon receiving the revocation acknowledgement!
      this.pendingDelegateRevocationEvent.getEventAction ().action (this.pendingDelegateRevocationEvent);
      // Check that sub-queue actually confirmed the revocation.
      if (this.pendingDelegateRevocationEvent != null)
        throw new IllegalStateException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Takes appropriate action if needed on the server-access credits of sub-queues.
   * 
   * <p>
   * This method does nothing, since server-access credits with this model are managed locally.
   * 
   * @see AbstractSimQueue#setServerAccessCredits
   * @see #rescheduleForNewServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
  }
  
  /** Starts waiting jobs (in the local waiting area) as long as there are such jobs
   *  and there are (local) server-access credits available.
   * 
   * @see AbstractSimQueue#setServerAccessCredits
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, getFirstJobInWaitingArea ());
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
   * @see AbstractSimQueue#start
   * @see #jobsInServiceArea
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Lets the delegate job arrive at its first queue, or make it depart immediately if no such queue is provided.
   * 
   * <p>
   * This method selects the first sub-queue for the delegate job to arrive on through {@link #selectFirstQueue}.
   * If a sub-queue is provided, it makes the delegate job arrive on that sub-queue;
   * otherwise it invokes {@link #depart} on the real job.
   * 
   * @see AbstractSimQueue#start
   * @see #getDelegateJob
   * @see #selectFirstQueue
   * @see #arrive
   * @see #depart
   * @see #getQueue
   * @see #insertJobInQueueUponStart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || (! this.jobsInServiceArea.contains (job)))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
    final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
    if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
      throw new IllegalArgumentException ();
    if (firstQueue != null)
      firstQueue.arrive (time, delegateJob);          
    else
      // We do not get a queue to arrive at.
      // So we depart; without having been executed!
      depart (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
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
   * In the {@link AbstractSimQueueComposite_LocalStart},
   * a (real) job can <i>only</i> depart because of one of the following two reasons:
   * <ul>
   * <li>Its delegate job departs (autonomously) on one of the sub-queues and there is no successor queue,
   *     see {@link #processSubQueueNotifications}.
   *     In this case,
   *     the delegate job has already left the sub-queue system when we are called,
   *     hence no action is required to remove it from there.
   *     All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * <li>The composite queue (us) enforces departure the job.
   *     In this case, the delegate job is <i>may</i> still be present on a sub-queue,
   *     and we have to forcibly revoke it on the sub-queue if so.
   *     We abuse {@link #removeJobFromQueueUponRevokation} to initiate the revocation
   *     (note that we cannot directly invoke {@link #revoke} or {@link #autoRevoke} on the composite queue
   *     as that would trigger an incorrect revocation notification).
   * </ul>
   * 
   * @throws IllegalStateException If the real or delegate job does not exits.
   * 
   * @see AbstractSimQueue#depart
   * @see #rescheduleAfterDeparture
   * @see #removeJobFromQueueUponRevokation
   * @see #removeJobsFromQueueLocal
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
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
   * @see AbstractSimQueue#depart
   * @see #removeJobFromQueueUponDeparture
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (this.pendingDelegateRevocationEvent != null)
      rescheduleAfterRevokation (departedJob, time, true);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESS (AND SANITY ON) SUB-QUEUE STATE-CHANGE NOTIFICATIONS
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
   *          we throw an {@link IllegalStateException}.
   * <li>With server-access credits related events
   *          {@link SimQueueSimpleEventType#SERVER_ACCESS_CREDITS},
   *          {@link SimQueueSimpleEventType#REGAINED_SAC},
   *          {@link SimQueueSimpleEventType#OUT_OF_SAC},
   *          we throw an {@link IllegalStateException}.
   * <li>We ignore start-armed related events
   *          {@link SimQueueSimpleEventType#STA_FALSE},
   *          {@link SimQueueSimpleEventType#STA_TRUE},
   *          as these are dealt with (if at all) by the outer loop.
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
   * <li>With {@link SimQueueSimpleEventType#AUTO_REVOCATION}, we throw an {@link IllegalStateException}.
   * <li>With {@link SimQueueSimpleEventType#START}, we start the real job.
   * <li>With {@link SimQueueSimpleEventType#DEPARTURE}, we invoke {@link #selectNextQueue} on the real job,
   *                                                     and let the delegate job arrive at the next queue if provided,
   *                                                     or makes the real job depart if not through {@link #depart}.
   * <li>With any non-standard notification type, we ignore the notification.
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
  protected final void processSubQueueNotifications
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
          //
          // Queue-Access Vacations (or, in fact, any state-change reports regarding QAV's) are forbidden on sub-queues.
          //
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS
             ||  notificationType == SimQueueSimpleEventType.OUT_OF_SAC
             ||  notificationType == SimQueueSimpleEventType.REGAINED_SAC)
          //
          // Server-Acess Credits events are (or, in fact, any state-change reports regarding QAV's) are forbidden on sub-queues.
          //
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.STA_FALSE
             ||  notificationType == SimQueueSimpleEventType.STA_TRUE)
          //
          // StartArmed events are ignored.
          //
          ; /* NOTHING TO DO */
        else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        {
          //
          // A (delegate) job (pseudo) arrives at a sub-queue.
          // In any case, at this point, we sent the (delegate) job to that sub-queue ourselves.
          //
          ; /* NOTHING TO DO */
        }
        else if (notificationType == SimQueueSimpleEventType.DROP)
        {
          //
          // Drop the (real) job.
          //
          final J realJob = getRealJob (job);
          drop (realJob, notificationTime);
        }
        else if (notificationType == SimQueueSimpleEventType.REVOCATION)
        {
          //
          // A (delegate) job is revoked on a sub-queue. This should always be the result from a revocation request on
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
          // AutoRevocations are forbidden on sub-queues.
          //
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.START)
          //
          // Start notifications from sub-queues are ignored.
          //
          ; /* EMPTY */
        else if (notificationType == SimQueueSimpleEventType.DEPARTURE)
        {
          //
          // Job departure on a sub-queue.
          // Invoke selector and route the job accordingly,
          // or depart the real job.
          final J realJob = getRealJob (job);
          final SimQueue<DJ, DQ> nextQueue = selectNextQueue (notificationTime, realJob, subQueue);
          if (nextQueue != null)
            nextQueue.arrive (notificationTime, job);
          else
            depart (notificationTime, realJob);
        }
        else
        {
          //
          // We received a "non-standard" SimQueue event.
          // Ignore it.
          // 
          ; /* EMPTY */
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
