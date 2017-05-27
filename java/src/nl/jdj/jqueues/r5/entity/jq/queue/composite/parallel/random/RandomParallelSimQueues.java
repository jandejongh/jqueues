package nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.random;

import java.util.Random;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.jackson.JacksonSimQueueNetwork;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.AbstractParallelSimQueues;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.jsq.JoinShortestSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.pattern.PatternParallelSimQueues;
import nl.jdj.jsimulation.r5.SimEventList;

/** Parallel queues with random selection policy.
 *
 * <p>
 * One of the sub-queue is selected at random (independently), with equal probabilities.
 * For non-uniform selection distributions, consider {@link JacksonSimQueueNetwork},
 * of which this composite queue is a special case.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 *
 * @see JacksonSimQueueNetwork
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
public class RandomParallelSimQueues
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends RandomParallelSimQueues>
  extends AbstractParallelSimQueues<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} that selects sub-queues at random.
   * 
   * <p>
   * After departure from the first queue, the job is made to depart by selecting <code>null</code> as its next queue.
   * 
   * @param queues  The queues, non-<code>null</code>.
   * @param rng     The random-number generator to use, non-<code>null</code>.
   * 
   * @return A new {@link SimQueueSelector} that selects sub-queues at random.
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code>, contains <code>null</code>,
   *                                    or if {@code rng == null}..
   * 
   */
  private static SimQueueSelector createSimQueueSelector
  (final Set<SimQueue> queues, final Random rng)
  {
    if (queues == null || queues.contains (null) || rng == null)
      throw new IllegalArgumentException ();
    return new SimQueueSelector ()
    {
      @Override
      public void resetSimQueueSelector ()
      {
      }
      @Override
      public final SimQueue selectFirstQueue (final double time, final SimJob job)
      {
        if (job == null)
          throw new IllegalArgumentException ();
        return JoinShortestSimQueue.getRandomSimQueueFromSet (queues, rng);
      }
      @Override
      public final SimQueue selectNextQueue (final double time, final SimJob job, final SimQueue previousQueue)
      {
        if (job == null || previousQueue == null || ! queues.contains (previousQueue))
          throw new IllegalArgumentException ();
        return null;
      }
    };
  }
  
  /** Creates a parallel queueing system with random selection policy.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues (with deterministic iteration order).
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param rng                   An optional user-supplied random-number generator
   *                                (if absent, a new one is created for local use).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimQueueSelector
   * @see Random
   * 
   */
  public RandomParallelSimQueues
  (final SimEventList eventList,
    final Set<DQ> queues,
    final DelegateSimJobFactory delegateSimJobFactory,
    final Random rng)
  {
    super (eventList,
      queues, 
      createSimQueueSelector ((Set<SimQueue>) queues, ((rng != null) ? rng : new Random ())),
      delegateSimJobFactory);
  }

  /** Returns a new {@link PatternParallelSimQueues} object on the same {@link SimEventList} with copies of the sub-queues,
   *  a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link PatternParallelSimQueues} object on the same {@link SimEventList} with copies of the sub-queues,
   *           a new RNG, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public RandomParallelSimQueues<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new RandomParallelSimQueues<>
      (getEventList (), queuesCopy, getDelegateSimJobFactory (), null);
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
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Random[queue list]".
   * 
   * @return "Random[queue list]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String string = "Random[";
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
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
