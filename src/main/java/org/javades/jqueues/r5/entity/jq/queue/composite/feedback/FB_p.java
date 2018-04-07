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

import java.util.Random;
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.jackson.Jackson;
import org.javades.jsimulation.r5.SimEventList;

/** Feedback queue with fixed probability of feedback to the embedded {@link SimQueue}.
 * 
 * <p>
 * This composite queue is a special case of a {@link Jackson}.
 * 
 * <p>
 * Internally, a suitable {@link SimQueueFeedbackController} is created from the feedback probability and a
 * (optionally, user-supplied) random-number generator.
 * 
 * <p>
 * This queue uses the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see Jackson
 * @see Random
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
public class FB_p
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends FB_p>
  extends AbstractFeedbackSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a suitable {@link SimQueueFeedbackController}.
   * 
   * Auxiliary function to constructor.
   * 
   */
  private static SimQueueFeedbackController createFeedbackController (final double p_feedback, final Random userRNG)
  {
    final Random RNG = ((userRNG != null) ? userRNG : new Random ());
    if (p_feedback < 0 || p_feedback > 1)
      throw new IllegalArgumentException ();
    return (SimQueueFeedbackController)
      (final double time, final SimQueue delegateQueue, final SimJob realJob, final int visits)
        -> RNG.nextDouble () < p_feedback;
  }
  
  /** Creates a feedback queue given an event list a queue and the number of visits required.
   *
   * @param eventList             The event list to use.
   * @param queue                 The queue, non-<code>null</code>.
   * @param p_feedback            The feedback probability, must be between zero and unity inclusive.
   * @param userRNG               An optional user-supplied random-number generator
   *                                (if absent, a new one is created for local use).
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or queue is <code>null</code>,
   *                                  or the feedback probability is strictly negative or larger than unity.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public FB_p
  (final SimEventList eventList,
    final DQ queue,
    final double p_feedback,
    final Random userRNG,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, createFeedbackController (p_feedback, userRNG), delegateSimJobFactory);
    this.p_feedback = p_feedback;
  }
  
  /** Returns a new {@link FB_p} object on the same {@link SimEventList} with a copy of the sub-queue,
   *  the same feedback probability, a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link FB_p} object on the same {@link SimEventList} with a copy of the sub-queue,
   *  the same feedback probability, a new RNG, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getFeedbackProbability
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public FB_p<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> queueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new FB_p<>
      (getEventList (), (DQ) queueCopy, getFeedbackProbability (), null, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "FB_p_feedback*100%[embedded queue]".
   * 
   * @return "FB_p_feedback*100%[embedded queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "FB_" + this.p_feedback * 100.0 + "%[" + getQueues ().iterator ().next () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FEEDBACK PROBABILITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The feedback probability (in [0, 1]).
   * 
   */
  private final double p_feedback;
  
  /** Returns the feedback probability.
   * 
   * @return The feedback probability, between zero and unity inclusive.
   * 
   */
  public final double getFeedbackProbability ()
  {
    return this.p_feedback;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (and made final).
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
