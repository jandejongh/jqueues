/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.jdj.jqueues.r5.entity.jq.queue.composite.ctandem2;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue.AutoRevocationPolicy;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.AbstractEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.SJF;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Notification;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Processor;
import nl.jdj.jsimulation.r5.SimEventList;

/** Compressed tandem (serial) queue with two queues, one used for waiting and one used for serving.
 *
 * <p>
 * This special composite queue only allows two distinct queues, one for waiting and a second one for serving.
 * The composite queue bypasses the service part of the first queue, only using its wait and job-selection policies,
 * and bypasses the waiting part of the second queue.
 * 
 * <p>
 * This special model for a composite queue is named the {@code CTandem2} model,
 * and deviates significantly from, e.g., the {@code LocalStart} model
 * of {@link AbstractSimQueueComposite_LocalStart}
 * and the {@code Encapsulator} model in {@link AbstractEncapsulatorSimQueue}.
 * 
 * <p>
 * The main purpose of this rather exotic {@link SimQueue} is to replace the waiting queue of an existing {@link SimQueue}
 * implementation with another one in order to, e.g., change from FIFO behavior in the waiting area to LIFO behavior.
 * It attempts to achieve this by controlling the server-access credits on the first sub-queue, the <i>wait</i> queue,
 * allowing jobs on it to start (one at a time) <i>only</i> if the second queue, the <i>serve</i> queue,
 * has {@link SimQueue#isStartArmed} set to {@code true}.
 * Jobs that start on the wait queue are then auto-revoked ({@link AutoRevocationPolicy#UPON_START}),
 * and the composite queue (this) lets the job (well, in fact, its <i>delegate</i> job)
 * arrive on the serve queue.
 * A real job starts <i>only</i> if and when it is actually moved from the wait to the serve queue.
 * When the delegate job departs from the serve queue, its real job departs from the composite queue.
 * 
 * <p>
 * Notice that a {@link CTandem2} guarantees that the serve queue is not on queue-access vacation.
 * always has infinite server-access credits, and always has an empty waiting area.
 * This explains the three conditions mentioned in {@link SimQueue#isStartArmed}.
 * 
 * <p>
 * The interpretation in terms of replacing the wait-behavior and job-selection behavior of the serve queue
 * with that of the wait queue has several complications, and does not hold in general.
 * For instance, the wait queue may never start a job, jobs may depart from its waiting area,
 * and the serve queue may not support the service area at all.
 * 
 * <p>
 * Despite its complications, the {@link CTandem2} can be very useful to
 * construct non-standard (at least, not available in this library) {@link SimQueue} implementations,
 * and reduces the pressure on this {@code jqueues} library to implement <i>all</i> possible combinations
 * of waiting-area (buffer) size, job-selection policies and number of servers.
 * For instance, a queueing discipline we have not implemented at this time of writing in {@code jqueues}
 * is multi-server {@link SJF}.
 * There is, however, a multi-server {@link FCFS_c} implementation which features an arbitrary finite number of servers,
 * so we can replace its FIFO waiting-area behavior with SJF with a {@link CTandem2}
 * as shown below (refer to the constructor documentation for more details):
 * 
 * <pre>
 * <code>
 final SimQueue waitQueue = new SJF (eventList);
 final SimQueue serveQueue = new FCFS_c (eventList, numberOfServers);
 final SimQueue sjf_c = new CTandem2 (eventList, waitQueue, serveQueue, delegateSimJobFactory);
 </code>
 * </pre>
 * or even (ignoring generic-type arguments):
 * 
 * <pre>
 * <code>
 public class SJF_c extends CTandem2
 {
 
   public SJF_c (final SimEventList eventList, final int numberOfServers, final DelegateSimJobFactory delegateSimJobFactory)
   {
     super (eventList, new SJF (eventList), new FCFS_c (eventList, numberOfServers), delegateSimJobFactory);
   }
 </code>
 * {@code  @Override}
 * <code>  public String toStringDefault ()
 *   {
 *     return "SJF_" + ((FCFS_c) getServeQueue ()).getNumberOfServers () + "]";
 *   }
 * 
 * }
 * </code>
 * </pre>
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
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
public class CTandem2
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends CTandem2>
  extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Auxiliary method to create the required {@link Set} of {@link SimQueue}s in the constructor.
   * 
   * @param waitQueue  The wait queue.
   * @param serveQueue The serve queue.
   * 
   * @return A {@link LinkedHashSet} holding both {@link SimQueue}s in the proper order.
   * 
   */
  private static Set<SimQueue> createQueuesSet (final SimQueue waitQueue, final SimQueue serveQueue)
  {
    if (waitQueue == null || serveQueue == null || waitQueue == serveQueue)
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (waitQueue);
    set.add (serveQueue);
    return set;
  }
  
  /** Creates compressed tandem queue given an event list, a wait queue and a serve queue,
   *  and an optional factory for delegate jobs.
   *
   * <p>
   * The constructor invoking the super constructor with an appropriate anonymous {@link SimQueueSelector} object,
   * and then sets the auto-revocation policy on the wait queue to {@link AutoRevocationPolicy#UPON_START}.
   * 
   * @param eventList             The event list to use.
   * @param waitQueue             The wait queue.
   * @param serveQueue            The serve queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code> or equal.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see SimQueue#setAutoRevocationPolicy
   * @see AutoRevocationPolicy#UPON_START
   * @see #resetEntitySubClassLocal
   * 
   */
  public CTandem2
  (final SimEventList eventList,
   final SimQueue<? extends DJ, ? extends DQ> waitQueue,
   final SimQueue<? extends DJ, ? extends DQ> serveQueue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      (Set<DQ>) createQueuesSet (waitQueue, serveQueue),
      new SimQueueSelector<J, DQ> ()
      {
        @Override
        public void resetSimQueueSelector ()
        {
        }
        @Override
        public DQ selectFirstQueue (double time, J job)
        {
          return (DQ) waitQueue;
        }
        @Override
        public DQ selectNextQueue (double time, J job, DQ previousQueue)
        {
          if (previousQueue == null)
            throw new IllegalArgumentException ();
          else if (previousQueue == waitQueue)
          {
            if (job.getQueue () == null)
              throw new IllegalStateException ();
            if (! job.getQueue ().getJobs ().contains (job))
              throw new IllegalStateException ();
            if (job.getQueue ().getJobsInWaitingArea ().contains (job))
              return null;
            else
              throw new IllegalStateException ();
          }
          else if (previousQueue == serveQueue)
            return null;
          else
            throw new IllegalArgumentException ();
        }
      },
      delegateSimJobFactory);
    getWaitQueue ().setAutoRevocationPolicy (AutoRevocationPolicy.UPON_START);
  }

  /** Returns a new {@link CTandem2} object on the same {@link SimEventList} with copies of the wait and
   *  serve queues and the same delegate-job factory.
   * 
   * @return A new {@link CTandem2} object on the same {@link SimEventList} with copies of the wait and
   *         serve queues and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the wait or serve queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public CTandem2<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> waitQueueCopy = getWaitQueue ().getCopySimQueue ();
    final SimQueue<DJ, DQ> serveQueueCopy = getServeQueue ().getCopySimQueue ();
    return new CTandem2<>
                 (getEventList (), waitQueueCopy, serveQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WAIT AND SERVE QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the wait (first) queue.
   * 
   * @return The wait (first) queue.
   * 
   */
  public final DQ getWaitQueue ()
  {
    return getQueue (0);
  }
  
  /** Gets the serve (second, last) queue.
   * 
   * @return The serve (second, last) queue.
   * 
   */
  public final DQ getServeQueue ()
  {
    return getQueue (1);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "CTandem2[waitQueue,serveQueue]".
   * 
   * @return "CTandem2[waitQueue,serveQueue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "CTandem2[" + getWaitQueue () + "," + getServeQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * XXX It would be better to cleverly inherit the QoS structure from a sub-queue,
   * in case both sub-queue do not have a conflicting QoS structure.
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   * XXX It would be better to cleverly inherit the QoS structure from a sub-queue,
   * in case both sub-queue do not have a conflicting QoS structure.
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
  
  /** Resets this {@link CTandem2}.
   * 
   * <p>
   * Calls super method (not if called from constructor, for which a private variant for local resets is used),
   * and sets the server-access credits on the wait (first) sub-queue to unity
   * if the serve (second) queue has {@link SimQueue#isStartArmed} value {@code true},
   * and zero if not.
   * 
   * @see SimQueue#resetEntity
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
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
    getWaitQueue ().setServerAccessCredits (getLastUpdateTime (), getServeQueue () .isStartArmed () ? 1 : 0);
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
  protected final void queueAccessVacationDropSubClass (final double time, final J job)
  {
    super.queueAccessVacationDropSubClass (time, job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the {@code startArmed} state of the serve (i.e., second) queue.
   * 
   * @return The {@code startArmed} state of the serve (i.e., second) queue.
   * 
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return getServeQueue ().isStartArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SET SERVER-ACCESS CREDITS ON WAIT QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Sets the server-access credits on the wait queue, based upon our server-access credits
   *  and the {@link SimQueue#isStartArmed} state on the server queue.
   * 
   * <p>
   * At all times (well, if we have a consistent queue state),
   * the server-access credits on the wait queue should be unity if and only if
   * the local (this) queue {@link #hasServerAcccessCredits}
   * AND the serve queue has {@link SimQueue#isStartArmed}.
   * In all other cases, the server-access credits on the wait queue should be zero.
   * 
   * <p>
   * This method sets the server-access credits on the wait queue appropriately, but only if needed.
   * 
   * <p>
   * Caution is advised for the use of this method.
   * For one, because of its immediate side effects on the wait (and possibly serve) queue,
   * you <i>should not</i> use it from within a sub-queue notification listener
   * (at least, not without taking special measures,
   * as is done in this class through a {@link MultiSimQueueNotificationProcessor}).
   * 
   * <p>
   * This method is (left) protected for documentation (javadoc) purposes.
   * 
   * @throws IllegalStateException If The current server-access credits value on the wait queue is not zero or unity.
   * 
   * @see #getQueue(int)
   * @see #getLastUpdateTime
   * @see #hasServerAcccessCredits
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
   * 
   */
  protected final void setServerAccessCreditsOnWaitQueue ()
  {
    final DQ waitQueue = getQueue (0);
    final DQ serveQueue = getQueue (1);
    final int oldWaitQueueSac = waitQueue.getServerAccessCredits ();
    if (oldWaitQueueSac < 0 || oldWaitQueueSac > 1)
      throw new IllegalStateException ("oldWaitQueueSac: " + oldWaitQueueSac);
    final int newWaitQueueSac = (hasServerAcccessCredits () && serveQueue.isStartArmed ()) ? 1 : 0;
    if (newWaitQueueSac != oldWaitQueueSac)
      waitQueue.setServerAccessCredits (getLastUpdateTime (), newWaitQueueSac);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job and administers it.
   * 
   * @see #addRealJobLocal
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    addRealJobLocal (job);
  }

  /** Lets the delegate job arrive at the wait (i.e., first) queue.
   * 
   * @see SimQueue#arrive
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! isJob (job)) || isJobInServiceArea (job))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
    getWaitQueue ().arrive (time, delegateJob);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link CTandem2},
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
   * @see #drop
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
   * In a {@link CTandem2}, revocations on real jobs can occur either
   * through external requests, in other words, through {@link #revoke},
   * or because of auto-revocations
   * on the composite (this) queue through {@link #autoRevoke}.
   * 
   * <p>
   * The delegate job is still present on a sub-queue, and we have to forcibly revoke it.
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
   * @throws IllegalStateException If the delegate job is <i>not</i> visiting a sub-queue,
   *                               or if a pending delegate revocation has already been flagged (or been forgotten to clear).
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
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue == null)
      // This state is illegal; if a real job is present (wherever),
      // its delegate job MUST always reside somewhere at a sub-queue.
      throw new IllegalStateException ();
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
   * @throws IllegalStateException If no pending delegate revocation was found,
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
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    if (this.pendingDelegateRevocationEvent == null)
      // This state is illegal; a real job was revoked, which should always result in a forced delegate-job revocation.
      throw new IllegalStateException ();
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

  /** Adjusts the server-access credits on the wait queue if needed.
   * 
   * <p>
   * This method sets the server-access credits on the wait queue
   * <i>only</i> if we run out of local server-access credits.
   * Note that the case in which we regain them is dealt with by {@link #rescheduleForNewServerAccessCredits}.
   * 
   * @see #getServerAccessCredits
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #rescheduleForNewServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    if (getServerAccessCredits () == 0)
      setServerAccessCreditsOnWaitQueue ();
  }
  
  /** Sets the server-access credits on the wait queue.
   * 
   * <p>
   * This method sets the server-access credits on the wait queue.
   * Note that the case in which we lose them is dealt with by {@link #setServerAccessCreditsSubClass}.
   * 
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    setServerAccessCreditsOnWaitQueue ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Performs sanity checks.
   * 
   * @throws IllegalStateException If sanity checks on internal consistency fail.
   * 
   * @see #isJob
   * @see #isJobInServiceArea
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! isJob (job)) || isJobInServiceArea (job))
      throw new IllegalArgumentException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
  }

  /** Lets the delegate job arrive at the serve queue.
   * 
   * @see #isJob
   * @see #isJobInServiceArea
   * @see #getDelegateJob
   * @see SimQueue#arrive
   * @see #insertJobInQueueUponStart
   * 
   * @throws IllegalStateException If sanity checks on internal consistency fail.
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! isJob (job)) || (! isJobInServiceArea (job)))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
    getServeQueue ().arrive (time, delegateJob);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link CTandem2},
   * a (real) job can <i>only</i> depart because of one of the following two reasons:
   * <ul>
   * <li>Its delegate job departs (autonomously) on the serve queue,
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
   * @see #depart
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
   * We check that {@link SimQueueSimpleEventType#START} and {@link SimQueueSimpleEventType#AUTO_REVOCATION}
   * always come in pairs from the wait queue
   * and allow at most one such pair in a (atomic) notification.
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
   * <li>We ignore server-access credits related events
   *          {@link SimQueueSimpleEventType#SERVER_ACCESS_CREDITS},
   *          {@link SimQueueSimpleEventType#REGAINED_SAC},
   *          {@link SimQueueSimpleEventType#OUT_OF_SAC},
   *          as these are dealt with (if at all) by the outer loop.
   * <li>We ignore start-armed related events
   *          {@link SimQueueSimpleEventType#STA_FALSE},
   *          {@link SimQueueSimpleEventType#STA_TRUE},
   *          as these are dealt with (if at all) by the outer loop.
   * <li>We ignore {@link SimQueueSimpleEventType#ARRIVAL}.
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
   * <li>With {@link SimQueueSimpleEventType#AUTO_REVOCATION}, we first check the source queue (must be the wait, or first, queue),
   *                                                          and throw an exception if the check fails.
   *                                                          Subsequently, we start the real job with {@link #start}.
   * <li>With {@link SimQueueSimpleEventType#START}, we do nothing.
   * <li>With {@link SimQueueSimpleEventType#DEPARTURE}, we invoke {@link #selectNextQueue} on the real job,
   *                                                     and let the delegate job arrive at the next queue if provided,
   *                                                     or makes the real job depart if not through {@link #depart}.
   * <li>With any non-standard notification type, we do nothing.
   * </ul>
   * 
   * <p>
   * After all sub-notifications have been processed,
   * we make sure the server-access credits on the wait queue are set properly with {@link #setServerAccessCreditsOnWaitQueue},
   * unless the notification was a (single) {@link SimEntitySimpleEventType#RESET},
   * and we can rely on the composite queue reset logic.
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
   * Most checks are trivial checks on the allowed sub-notifications from the sub-queues depending
   * on the start model and on the presence or absence of real and delegate jobs
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
   * @see #setServerAccessCreditsOnWaitQueue
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
      // SANITY CHECK ON STARTED/AUTO-REVOKED PAIRS
      //
      int nrStarted = 0;
      DJ lastJobStarted = null;
      int nrAutoRevocations = 0;
      DJ lastJobAutoRevoked = null;
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
          // Queue-Access Vacations (or, in fact, any state-change reports regarding QAV's)
          // are forbidden on sub-queues except for both sub-queues.
          //
          throw new IllegalStateException ();
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
          //
          // A (delegate) job (pseudo) arrives at either sub-queue.
          // In any case, at this point, we sent the (delegate) job to that sub-queue ourselves.
          //
          ; /* NOTHING TO DO */
        else if (notificationType == SimQueueSimpleEventType.DROP)
        {
          //
          // We do not have a drop destination queue, hence we must drop the (real) job.
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
        {
          //
          // Received an AutoRevocation; can only come from wait queue.
          // Start the job on the composite queue.
          // Count the number of auto-revocations so we can match them with Starts.
          //
          if (subQueue == getWaitQueue ())
          {
            nrAutoRevocations++;
            lastJobAutoRevoked = job;
            start (notificationTime, getRealJob (job, null));
          }
          else
            throw new IllegalStateException ();
        }
        else if (notificationType == SimQueueSimpleEventType.START)
        {
          //
          // Count the number of Starts.
          //
          nrStarted++;
          lastJobStarted = job;
        }
        else if (notificationType == SimQueueSimpleEventType.DEPARTURE)
        {
          //
          // Delegate job departs; use the SimQueueSelector to find the next (serve) queue;
          // if none provided, departs the real job.
          //
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
          // We can only process and report this if we are an ecapsulator queue,
          // in which case we have to replace both queue (always) and job (if applicable).
          // 
          /* EMPTY */
        }
      }
      if (notification.getQueue () == getWaitQueue ())
      {
        //
        // START and AUTO_REVOCATION have to come in pairs on the wait queue,
        // apply to the same job and at most one pair is allowed in a sub-notification.
        //
        if (nrStarted > 1 || nrAutoRevocations > 1 || nrStarted != nrAutoRevocations || lastJobStarted != lastJobAutoRevoked)
           throw new IllegalStateException ();
      }
      if (getIndex (subQueue) == 1)
        //
        // We received a state-changing event on the second ("serve") queue in a COMPRESSED_TANDEM_2_QUEUE.
        // We must reevaluate the server-access credits on the first ("wait") queue.
        //
        setServerAccessCreditsOnWaitQueue ();
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
