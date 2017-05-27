package nl.jdj.jqueues.r5.entity.jq.queue.composite.single.feedback;

import java.util.Random;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.jackson.JacksonSimQueueNetwork;
import nl.jdj.jsimulation.r5.SimEventList;

/** Feedback queue with fixed probability of feedback to the embedded {@link SimQueue}.
 * 
 * <p>
 * This composite queue is a special case of a {@link JacksonSimQueueNetwork}.
 * 
 * <p>
 * Internally, a suitable {@link SimQueueFeedbackController} is created from the feedback probability and a
 * (optionally, user-supplied) random-number generator.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see JacksonSimQueueNetwork
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
public class ProbabilisticFeedbackSimQueue
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends ProbabilisticFeedbackSimQueue>
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
  public ProbabilisticFeedbackSimQueue
  (final SimEventList eventList,
    final DQ queue,
    final double p_feedback,
    final Random userRNG,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, createFeedbackController (p_feedback, userRNG), delegateSimJobFactory);
    this.p_feedback = p_feedback;
  }
  
  /** Returns a new {@link ProbabilisticFeedbackSimQueue} object on the same {@link SimEventList} with a copy of the sub-queue,
   *  the same feedback probability, a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link ProbabilisticFeedbackSimQueue} object on the same {@link SimEventList} with a copy of the sub-queue,
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
  public ProbabilisticFeedbackSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> queueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new ProbabilisticFeedbackSimQueue<>
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
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
