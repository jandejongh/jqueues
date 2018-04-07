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
package org.javades.jqueues.r5.entity.jq.queue.composite.enc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQSimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import org.javades.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import org.javades.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue} of which job starts are hidden.
 *
 * <p>
 * This composite queue (precisely) mimics the {@link SimQueue} interface of the encapsulated queue,
 * including non-standard operations and notifications,
 * but hides the start of (delegate) job on the encapsulated queue.
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
public class EncHS
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends EncHS>
  extends AbstractEncapsulatorSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue that hides its sub-queue starts given an event list and a queue.
   *
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public EncHS
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, delegateSimJobFactory);
  }
  
  /** Returns a new {@link EncHS} object on the same {@link SimEventList}
   *  with a copy of the encapsulated queue and the same delegate-job factory.
   * 
   * @return A new {@link EncHS} object on the same {@link SimEventList}
   *         with a copy of the encapsulated queue and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public EncHS<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new EncHS (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "EncHS[encapsulated queue]".
   * 
   * @return "EncHS[encapsulated queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "EncHS[" + getQueues ().iterator ().next () + "]";
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    super.insertJobInQueueUponArrival (job, time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    super.rescheduleAfterArrival (job, time);
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

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    super.rescheduleAfterDrop (job, time);
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

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    super.rescheduleAfterRevokation (job, time, auto);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code false}, since (real) jobs cannot start.
   * 
   * <p>
   * Note: This overrides the default {@link AbstractEncapsulatorSimQueue} implementation.
   * 
   * @return {@code false}, since (real) jobs cannot start.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   * <p>
   * Note: This overrides the default {@link AbstractEncapsulatorSimQueue} implementation.
   * 
   * <p>
   * In the {@link EncHS} queue, server-access credits on the encapsulated queue are
   * left untouched.
   * The composite queue has its own little private notion of the server-access credits,
   * but since jobs do not start, these do not hurt.
   * We have to do this in order to prevent all sorts of server-access-credits-related
   * notifications to enter the composite queue and its listeners.
   * They might fail to understand why server-access credits change in the absence of
   * starting jobs.
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    super.rescheduleForNewServerAccessCredits (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws an exception.
   * 
   * <p>
   * Note: This overrides the default {@link AbstractEncapsulatorSimQueue} implementation.
   * 
   * @throws IllegalStateException Since jobs cannot start.
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws an exception.
   * 
   * <p>
   * Note: This overrides the default {@link AbstractEncapsulatorSimQueue} implementation.
   * 
   * @throws IllegalStateException Since jobs cannot start.
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    throw new IllegalStateException ();
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

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    super.removeJobFromQueueUponDeparture (departingJob, time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    super.rescheduleAfterDeparture (departedJob, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes all start notifications from the list and calls super method (and makes the implementation final).
   * 
   * @see SimJQSimpleEventType#START
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    if (notifications != null)
      for (MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification : notifications)
      {
        final Iterator<Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>>> i_sub
          = notification.getSubNotifications ().iterator ();
        while (i_sub.hasNext ())
          if (i_sub.next ().containsKey (SimJQSimpleEventType.START))
            i_sub.remove ();
      }
    super.processSubQueueNotifications (notifications);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
