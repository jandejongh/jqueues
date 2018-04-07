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

import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jsimulation.r5.SimEventList;

/** A composite queue with two queues, a main one and one collecting all departed jobs from the main queue.
 *
 * <p>
 * The main and depart arguments may be equal.
 * 
 * <p>
 * This queue uses the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
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
public class DepCol
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends DepCol>
  extends AbstractCollectorSimQueue<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a departure-collector queue given an event list, a main queue and a departure (collector) queue.
   *
   * <p>
   * Note that the mainQueue and the depQueue arguments may be equal!
   * 
   * @param eventList             The event list to use.
   * @param mainQueue             The main queue.
   * @param depQueue              The departure queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public DepCol
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> mainQueue,
   final SimQueue<DJ, DQ> depQueue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, mainQueue, depQueue, false, false, true, delegateSimJobFactory);
  }

  /** Returns a new {@link DepCol} object on the same {@link SimEventList} with copies of the main and
   *  departure queues and the same delegate-job factory.
   * 
   * @return A new {@link DepCol} object on the same {@link SimEventList} with copies of the main and
   *         departure queues and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the main or departure queues
   *                                       could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getMainQueue
   * @see #getDepartureQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public DepCol<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> mainQueueCopy = getMainQueue ().getCopySimQueue ();
    final SimQueue<DJ, DQ> depQueueCopy = getDepartureQueue ().getCopySimQueue ();
    return new DepCol<> (getEventList (), mainQueueCopy, depQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the departure (second, last) queue.
   * 
   * @return The departure (second, last) queue.
   * 
   */
  protected final DQ getDepartureQueue ()
  {
    return getCollectorQueue ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "DepCol[mainQueue-&gt;depQueue]".
   * 
   * @return "DepCol[mainQueue-&gt;depQueue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DepCol[" + getMainQueue () + "->" + getDepartureQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
