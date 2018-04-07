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
package org.javades.jqueues.r5.entity.jq.queue.composite.parallel;

import java.util.List;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import org.javades.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import org.javades.jsimulation.r5.SimEventList;

/** Parallel queues (abstract).
 * 
 * <p>
 * In a parallel composite queue,
 * a (delegate) job visits one and only one of the
 * embedded {@link SimQueue}s,
 * as controlled by a {@link SimQueueSelector} supplied by concrete subclasses.
 * 
 * <p>
 * This and derived queues use the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see Par
 * @see ParallelSimQueuesSelector
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
public abstract class AbstractParallelSimQueues
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractParallelSimQueues>
  extends AbstractSimQueueComposite_LocalStart<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a (abstract) parallel queue given an event list and a list of queues to put in parallel.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues in no particular order.
   * @param simQueueSelector      An optional {@link SimQueueSelector} for arriving jobs; if <code>null</code>,
   *                                jobs will leave immediately upon arrival.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   *
   * @see ParallelSimQueuesSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  protected AbstractParallelSimQueues
  (final SimEventList eventList,
   final Set<DQ> queues,
   final SimQueueSelector simQueueSelector,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, new ParallelSimQueuesSelector (simQueueSelector), delegateSimJobFactory);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (and made final).
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    super.processSubQueueNotifications (notifications);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
