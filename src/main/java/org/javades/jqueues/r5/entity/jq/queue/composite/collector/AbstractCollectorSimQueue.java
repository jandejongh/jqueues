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
package org.javades.jqueues.r5.entity.jq.queue.composite.collector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQSimpleEventType;
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import org.javades.jsimulation.r5.SimEventList;

/** A composite queue with two queues, a main one and one collecting, upon request,
 *  all dropped, auto-revoked and/or departed jobs from the main queue.
 *
 * <p>
 * The main and collector arguments may be equal.
 * 
 * <p>
 * This and derived queues use the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
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
public abstract class AbstractCollectorSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractCollectorSimQueue>
  extends AbstractSimQueueComposite_LocalStart<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Auxiliary method to create the required {@link Set} of {@link SimQueue}s in the constructor.
   * 
   * <p>
   * Note that the mainQueue and the collectorQueue arguments may be equal!
   * 
   * @param mainQueue      The wait queue.
   * @param collectorQueue The collector queue.
   * 
   * @return A {@link LinkedHashSet} holding both {@link SimQueue}s in the proper order.
   * 
   */
  private static Set<SimQueue> createQueuesSet (final SimQueue mainQueue, final SimQueue collectorQueue)
  {
    if (mainQueue == null || collectorQueue == null)
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (mainQueue);
    set.add (collectorQueue);
    return set;
  }
  
  /** Creates a collector queue given an event list, a main queue and a collector queue.
   *
   * <p>
   * Note that the mainQueue and the dropQueue arguments may be equal!
   * 
   * @param eventList              The event list to use.
   * @param mainQueue              The main queue.
   * @param collectorQueue         The collector queue.
   * @param collectDrops           Whether to collect drops from the main queue.
   * @param collectAutoRevocations Whether to collect auto-revocations from the main queue.
   * @param collectDepartures      Whether to collect departures from the main queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code>.
   * 
   * @see CollectorSimQueueSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public AbstractCollectorSimQueue
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> mainQueue,
   final SimQueue<DJ, DQ> collectorQueue,
   final boolean collectDrops,
   final boolean collectAutoRevocations,
   final boolean collectDepartures,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      (Set<DQ>) createQueuesSet (mainQueue, collectorQueue),
      new CollectorSimQueueSelector (mainQueue, collectorQueue),
      delegateSimJobFactory);
    this.collectDrops = collectDrops;
    this.collectAutoRevocations = collectAutoRevocations;
    this.collectDepartures = collectDepartures;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN AND COLLECTOR QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the main (first) queue.
   * 
   * @return The main (first) queue.
   * 
   */
  protected final DQ getMainQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    return iterator.next ();
  }
  
  /** Returns the collector (second, last) queue.
   * 
   * @return The collector (second, last) queue.
   * 
   */
  protected final DQ getCollectorQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    final DQ firstQueue = iterator.next ();
    if (! iterator.hasNext ())
      return firstQueue;
    else
      return iterator.next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // COLLECT DROPS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final boolean collectDrops;

  /** Return whether to collect drops.
   * 
   * @return Whether to collect drops.
   * 
   */
  public final boolean isCollectDrops ()
  {
    return this.collectDrops;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // COLLECT AUTO-REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final boolean collectAutoRevocations;

  /** Return whether to collect auto-revocations.
   * 
   * @return Whether to collect auto-revocations.
   * 
   */
  public final boolean isCollectAutoRevocations ()
  {
    return this.collectAutoRevocations;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // COLLECT DEPARTURES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final boolean collectDepartures;

  /** Return whether to collect departures.
   * 
   * @return Whether to collect departures.
   * 
   */
  public final boolean isCollectDepartures ()
  {
    return this.collectDepartures;
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
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes applicable exit notifications from the main queue, and let the jobs arrive at the collector queue;
   *  invoke super method for all other notifications.
   * 
   * @see #collectDrops
   * @see #collectAutoRevocations
   * @see #collectDepartures
   * @see SimQueue#arrive
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    //
    // Empty or null notifications => let super-class throw the exception.
    //
    if (notifications == null || notifications.isEmpty ())
    {
      super.processSubQueueNotifications (notifications);
      return;
    }
    //
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    //
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    //
    // Collect (and remove from the original data) relevant exit events from the mainQueue into a new List exitJobs.
    // Use nested pair of Iterator's to allow for element removal while iterating.
    //
    final List<DJ> exitJobs = new ArrayList<> ();
    final Iterator<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> i_not = notifications.iterator ();
    while (i_not.hasNext ())
    {
      final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification = i_not.next ();
      if (notification.getTime () != getLastUpdateTime ())
        throw new IllegalStateException ();
      if (notification.getQueue () == getMainQueue ())
      {
        final Iterator<Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>>> i_sub
          = notification.getSubNotifications ().iterator ();
        while (i_sub.hasNext ())
        {
          Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>> subNotification = i_sub.next ();
          if (subNotification.size () != 1)
            throw new IllegalArgumentException ();
          if (isCollectDrops () && subNotification.containsKey (SimJQSimpleEventType.DROP))
          {
            if (subNotification.get (SimJQSimpleEventType.DROP).getTime () != getLastUpdateTime ())
              throw new IllegalArgumentException ();
            final DJ job = subNotification.get (SimJQSimpleEventType.DROP).getJob ();
            exitJobs.add (job);
            i_sub.remove ();
          }
          else if (isCollectAutoRevocations () && subNotification.containsKey (SimJQSimpleEventType.AUTO_REVOCATION))
          {
            if (subNotification.get (SimJQSimpleEventType.AUTO_REVOCATION).getTime () != getLastUpdateTime ())
              throw new IllegalArgumentException ();
            final DJ job = subNotification.get (SimJQSimpleEventType.AUTO_REVOCATION).getJob ();
            exitJobs.add (job);
            i_sub.remove ();
          }
          else if (isCollectDepartures () && subNotification.containsKey (SimJQSimpleEventType.DEPARTURE))
          {
            if (subNotification.get (SimJQSimpleEventType.DEPARTURE).getTime () != getLastUpdateTime ())
              throw new IllegalArgumentException ();
            final DJ job = subNotification.get (SimJQSimpleEventType.DEPARTURE).getJob ();
            exitJobs.add (job);
            i_sub.remove ();
          }
        }
        if (notification.getSubNotifications ().isEmpty ())
          i_not.remove ();
      }
    }
    //
    // Collectively process the exit jobs by letting them arrive at the collector queue.
    //
    for (final DJ job : exitJobs)
      getCollectorQueue ().arrive (getLastUpdateTime (), job);
    //
    // Invoke super method in case there are other (sub-)notifications to process.
    //
    if (! notifications.isEmpty ())
      super.processSubQueueNotifications (notifications);
    //
    // Fire notification as we are most likely a top-level notification.
    //
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
