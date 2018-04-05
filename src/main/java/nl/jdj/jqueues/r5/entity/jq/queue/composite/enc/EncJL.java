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
package nl.jdj.jqueues.r5.entity.jq.queue.composite.enc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue}
 *  equipped with fixed expiration limits on waiting time, service time and sojourn time.
 *
 * <p>
 * This composite queue mimics (precisely) the {@link SimQueue} interface of the encapsulated queue,
 * yet imposes (independent) limits on the numbers of jobs in waiting area,
 * service area and queueing system as a whole.
 * Jobs whose arrival exceeds any limit will be dropped,
 * and jobs are detained in the waiting area if
 * their potential start would exceed the limit on the number of jobs in the service area.
 * 
 * <p>
 * Apart from local drops, this queue uses the server-access credits on the encapsulated queue
 * to restrict the number of jobs in the service area.
 * 
 * <p>
 * For any limit, {@link Integer#MAX_VALUE} is treated as infinity,
 * and that value effectively disables monitoring that particular limit.
 * 
 * <p>
 * Refer to {@link AbstractEncapsulatorSimQueue},
 * {@link AbstractSimQueueComposite}
 * and {@link SimQueueComposite}
 * for more details on encapsulated queues.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
 * @see Enc
 * @see EncTL
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
public class EncJL
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends Enc>
  extends AbstractEncapsulatorSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue given an event list and a queue and limits on respective number of jobs.
   *
   * <p>
   * The server-access credits on the encapsulated queue are set accordingly.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param maxJw                 The maximum number of jobs allowed in the waiting area, non-negative,
   *                                {@link Integer#MAX_VALUE} is treated as infinity.
   * @param maxJs                 The maximum number of jobs allowed in the service area, non-negative,
   *                                {@link Integer#MAX_VALUE} is treated as infinity.
   * @param maxJ                  The maximum number of jobs allowed in the queueing system, non-negative,
   *                                {@link Integer#MAX_VALUE} is treated as infinity.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>,
   *                                    or any of the limit arguments is strictly negative.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public EncJL
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory,
   final int maxJw,
   final int maxJs,
   final int maxJ)
  {
    super (eventList, queue, delegateSimJobFactory);
    if (maxJw < 0 || maxJs < 0 || maxJ < 0)
      throw new IllegalArgumentException ();
    this.maxJw = maxJw;
    this.maxJs = maxJs;
    this.maxJ = maxJ;
    setServerAccessCreditsOnEncapsulatedQueue (getLastUpdateTime ());
  }
  
  /** Returns a new {@link EncTL} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue, the same delegate-job factory, and equal limits on respective number of jobs.
   * 
   * @return A new {@link EncTL} object on the same {@link SimEventList} with a copy of the encapsulated
   *           queue, the same delegate-job factory, and equal limits on respective number of jobs.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * @see #getMaxJobsInWaitingArea
   * @see #getMaxJobsInServiceArea
   * @see #getMaxJobs
   * 
   */
  @Override
  public EncJL<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    final EncJL<DJ, DQ, J, Q> copy =
      new EncJL (getEventList (),
                 encapsulatedQueueCopy,
                 getDelegateSimJobFactory (),
                 getMaxJobsInWaitingArea (),
                 getMaxJobsInServiceArea (),
                 getMaxJobs ());
    return copy;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "EncJL(maxJw,maxJs,maxJ)[encapsulated queue]".
   * 
   * <p>
   * In the output, the value {@link Integer#MAX_VALUE} is replaced with "inf".
   * 
   * @return "EncJL(maxJw,maxJs,maxJ)[encapsulated queue]".
   * 
   * @see #getEncapsulatedQueue
   * @see #getMaxJobsInWaitingArea
   * @see #getMaxJobsInServiceArea
   * @see #getMaxJobs
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "EncJL("
      + (getMaxJobsInWaitingArea () == Integer.MAX_VALUE ? "inf" : getMaxJobsInWaitingArea ())
      + ","
      + (getMaxJobsInServiceArea () == Integer.MAX_VALUE ? "inf" : getMaxJobsInServiceArea ())
      + ","
      + (getMaxJobs () == Integer.MAX_VALUE ? "inf" : getMaxJobs ())
      + ")"
      + "[" + getEncapsulatedQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX JOBS IN WAITING AREA
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int maxJw;
  
  /** Returns the maximum number of jobs allowed in the waiting area.
   * 
   * @return The maximum number of jobs allowed in the waiting area, {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   */
  public final int getMaxJobsInWaitingArea ()
  {
    return this.maxJw;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX JOBS IN SERVICE AREA
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int maxJs;
  
  /** Returns the maximum number of jobs allowed in the service area.
   * 
   * @return The maximum number of jobs allowed in the service area, {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   */
  public final int getMaxJobsInServiceArea ()
  {
    return this.maxJs;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX JOBS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int maxJ;
  
  /** Returns the maximum number of jobs allowed in the queueing system.
   * 
   * @return The maximum number of jobs allowed in the queueing system, {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   */
  public final int getMaxJobs ()
  {
    return this.maxJ;
  }  

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
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
  
  /** Calls super method and sets the server-access credits on the encapsulated queue.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    setServerAccessCreditsOnEncapsulatedQueue (getLastUpdateTime ());
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
  
  /** Calls super method.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    super.insertJobInQueueUponArrival (job, time);
  }

  /** Calls super method if allowed in view of the limits of this queue; drops the job otherwise.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    setServerAccessCreditsOnEncapsulatedQueue (time);
    if (getMaxJobs () > 0
    &&  (getMaxJobsInWaitingArea () == Integer.MAX_VALUE
         || getEncapsulatedQueue ().getNumberOfJobsInWaitingArea () < getMaxJobsInWaitingArea ()
         || (getEncapsulatedQueue ().getNumberOfJobsInWaitingArea () == getMaxJobsInWaitingArea ()
             && getEncapsulatedQueue ().getNumberOfJobsInWaitingArea () == 0
             && getEncapsulatedQueue ().getServerAccessCredits () > 0
             && getEncapsulatedQueue ().isStartArmed ())))
      super.rescheduleAfterArrival (job, time);
    else
      drop (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    super.removeJobFromQueueUponDrop (job, time);
  }

  /** Calls super method and sets the server-access credits on the encapsulated queue.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    super.rescheduleAfterDrop (job, time);
    setServerAccessCreditsOnEncapsulatedQueue (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    super.removeJobFromQueueUponRevokation (job, time, auto);
  }

  /** Calls super method and sets the server-access credits on the encapsulated queue.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    super.rescheduleAfterRevokation (job, time, auto);
    setServerAccessCreditsOnEncapsulatedQueue (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** XXXXXCalls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return
    (getMaxJobs () > 0
     && (getMaxJobs () == Integer.MAX_VALUE
         || getEncapsulatedQueue ().getNumberOfJobs () < getMaxJobs ())
     && (getMaxJobsInServiceArea () == Integer.MAX_VALUE
         || getEncapsulatedQueue ().getNumberOfJobsInServiceArea () < getMaxJobsInServiceArea ())
     && getEncapsulatedQueue ().isStartArmed ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    // super.setServerAccessCreditsSubClass ();
//    System.err.println ("@" + getLastUpdateTime () + " @ " + this + " -> NEW SAC -> " + getServerAccessCredits () + ".");
//    System.err.println ("                        FROM SetSAC:");
    setServerAccessCreditsOnEncapsulatedQueue (getLastUpdateTime ());
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
//    super.rescheduleForNewServerAccessCredits (time);
//    setServerAccessCreditsOnEncapsulatedQueue (getLastUpdateTime ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method.
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    super.insertJobInQueueUponStart (job, time);
  }

  /** Calls super method.
   * 
   * <p>
   * Note that starting a job can never require re-assessment of the server-access credits on the encapsulated queue.
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    super.rescheduleAfterStart (job, time);
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

  /** Calls super method.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    super.removeJobFromQueueUponDeparture (departingJob, time);
  }

  /** Calls super method and sets the server-access credits on the encapsulated queue.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    super.rescheduleAfterDeparture (departedJob, time);
    setServerAccessCreditsOnEncapsulatedQueue (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SET SERVER-ACCESS CREDITS ON SUB-QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void setServerAccessCreditsOnEncapsulatedQueue (final double time)
  {
    int sacRequired = Integer.MAX_VALUE;
    if (getMaxJobsInServiceArea () < Integer.MAX_VALUE)
      sacRequired = getMaxJobsInServiceArea () - getEncapsulatedQueue ().getNumberOfJobsInServiceArea ();
    if (getServerAccessCredits () < Integer.MAX_VALUE
      && getServerAccessCredits () < sacRequired)
      sacRequired = getServerAccessCredits ();
    if (getEncapsulatedQueue ().getServerAccessCredits () != sacRequired)
      getEncapsulatedQueue ().setServerAccessCredits (time, sacRequired);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method after removing and local processing all start events from the sub-queues.
   * 
   * <p>
   * Removing the start events first is needed in order to properly
   * update the local server-access credits with (atomic) sub-queue notifications with multiple
   * start events in combination with other events.
   * 
   * <p>
   * This method opens a top-level event since we are processing the events ahead of our super implementation.
   * 
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * @see SimJQSimpleEventType#START
   * @see #start
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    //
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    //
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (notifications != null)
      for (MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification : notifications)
      {
        final Iterator<Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>>> i_sub
          = notification.getSubNotifications ().iterator ();
        while (i_sub.hasNext ())
        {
          Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>> subNotification = i_sub.next ();
          if (subNotification.containsKey (SimJQSimpleEventType.START))
          {
            final DJ job = subNotification.get (SimJQSimpleEventType.START).getJob ();
            start (notification.getTime (), getRealJob (job));
            i_sub.remove ();
          }
        }
      }
    super.processSubQueueNotifications (notifications);
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
