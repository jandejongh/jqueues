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
package org.javades.jqueues.r5.entity.jq.queue.composite.feedback;

import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import org.javades.jsimulation.r5.SimEventList;

/** Feedback queue with fixed number of visits to the embedded {@link SimQueue}.
 * 
 * <p>
 * This queue uses the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueFeedbackController
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
public class FB_v
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends FB_v>
  extends AbstractFeedbackSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a suitable {@link SimQueueFeedbackController}.
   * 
   * <p>
   * Note that the controller is stateless, hence we do not have to worry about resets.
   * 
   * <p>
   * Auxiliary function to constructor.
   * 
   */
  private static SimQueueFeedbackController createFeedbackController (final int numberOfVisits)
  {
    if (numberOfVisits < 1)
      throw new IllegalArgumentException ();
    return (SimQueueFeedbackController)
      (final double time, final SimQueue delegateQueue, final SimJob realJob, final int visits)
        -> visits < numberOfVisits;
  }
  
  /** Creates a feedback queue given an event list a queue and the number of visits required.
   *
   * @param eventList             The event list to use.
   * @param queue                 The queue, non-<code>null</code>.
   * @param numberOfVisits        The required number of visits to the queue, must be at least unity.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or queue is <code>null</code>,
   *                                  or the required number of visits is zero or negative.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public FB_v
  (final SimEventList eventList,
    final DQ queue,
    final int numberOfVisits,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, createFeedbackController (numberOfVisits), delegateSimJobFactory);
    this.numberOfVisits = numberOfVisits;
  }
  
  /** Returns a new {@link FB_v} object on the same {@link SimEventList} with a copy of the sub-queue,
   *  the same number of visits required, and the same delegate-job factory.
   * 
   * @return A new {@link FB_v} object on the same {@link SimEventList} with a copy of the sub-queue,
   *         the same number of visits required, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getNumberOfVisits
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public FB_v<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> queueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new FB_v<>
      (getEventList (), (DQ) queueCopy, getNumberOfVisits (), getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "FB_numVisits[embedded queue]".
   * 
   * @return "FB_numVisits[embedded queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "FB_" + this.numberOfVisits + "[" + getQueues ().iterator ().next () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF VISITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The required number of visits to the embedded queue,
   * 
   */
  private final int numberOfVisits;
  
  /** Returns the required number of visits to the embedded queue.
   * 
   * @return The required number of visits to the embedded queue.
   * 
   */
  public final int getNumberOfVisits ()
  {
    return this.numberOfVisits;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (and made final).
   * 
   * <p>
   * Note that the {@link SimQueueSelector} of this composite queue is reset by our super class.
   * In turn, the selector will reset the embedded {@link SimQueueFeedbackController}.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
