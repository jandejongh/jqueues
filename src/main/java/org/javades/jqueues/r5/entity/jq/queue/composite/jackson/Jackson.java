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
package org.javades.jqueues.r5.entity.jq.queue.composite.jackson;

import java.util.List;
import java.util.Random;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import org.javades.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import org.javades.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} implementation of a Jackson queueing network.
 *
 * <p>
 * This queue uses the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * <p>
 * For more documentation see {@link JacksonSimQueueSelector}.
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
public class Jackson
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends Jackson>
  extends AbstractSimQueueComposite_LocalStart<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a Jackson queueing network.
   * 
   * <p>
   * For brevity, <code>|Q|</code> is used as a shorthand for <code>queues.size ()</code>.
   * 
   * @param eventList             The event list to use.
   * @param queues                The queues, an iteration over the set must return (deterministically)
   *                                the non-<code>null</code> queues
   *                                as indexed in the probability arguments;
   *                                see also {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param pdfArrival            See {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param pdfTransition         See {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param userRNG               See {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry,
   *                                  or if one or both of the probability arguments is improperly dimensioned or contains
   *                                  illegal values.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public Jackson
  (final SimEventList eventList,
   final Set<DQ> queues,
   final double[] pdfArrival,
   final double[][] pdfTransition,
   final Random userRNG,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      queues,
      new JacksonSimQueueSelector<>  (queues, pdfArrival, pdfTransition, userRNG),
      delegateSimJobFactory);
  }

  /** Returns a new {@link Jackson} object on the same {@link SimEventList} with copies of the sub-queues and
   *  probability arguments, a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link Jackson} object on the same {@link SimEventList} with copies of the sub-queues and
   *         probability arguments, a new RNG, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see JacksonSimQueueSelector#getPdfArrival
   * @see JacksonSimQueueSelector#getPdfTransition
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public Jackson<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new Jackson<>
      (getEventList (),
        queuesCopy,
        ((JacksonSimQueueSelector) getSimQueueSelector ()).getPdfArrival (),
        ((JacksonSimQueueSelector) getSimQueueSelector ()).getPdfTransition (),
        null,
        getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Jackson[queue list]".
   * 
   * @return "Jackson[queue list]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String string = "Jackson[";
    boolean first = true;
    for (DQ dq : getQueues ())
    {
      if (! first)
        string += ",";
      else
        first = false;
      string += dq;
    }
    string += "]";
    return string;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clear the administration of visits.
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
