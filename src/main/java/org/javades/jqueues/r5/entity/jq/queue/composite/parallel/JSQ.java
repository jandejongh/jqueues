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

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import org.javades.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import org.javades.jsimulation.r5.SimEventList;

/** Parallel queues with Join Shortest Queue selection policy.
 *
 * <p>
 * The selection of the "shortest queue" is either based on all jobs present in the candidate queues
 * or on all <i>waiting</i> jobs present.
 * 
 * <p>
 * Ties are broken at random with equal probabilities.
 * 
 * <p>
 * This queue uses the {@code LocalStart} model as explained with {@link AbstractSimQueueComposite_LocalStart}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 *
 * @see SimQueue#getNumberOfJobs
 * @see SimQueue#getNumberOfJobsInServiceArea
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
public class JSQ
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends JSQ>
  extends AbstractParallelSimQueues<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} that selects the queue with the shortest-queue length,
   * either in terms of jobs waiting or in terms of jobs present, as the queue to visit by a job upon arrival.
   * 
   * <p>
   * After departure from the first queue, the job is made to depart by selecting <code>null</code> as its next queue.
   * 
   * @param queues           The queues, non-<code>null</code>.
   * @param onlyWaitingJobs  Whether only waiting jobs are considered in determining the queue length
   *                           (instead of all jobs present).
   * @param rng              The random-number generator to use, non-<code>null</code>.
   * 
   * @return A new {@link SimQueueSelector} that selects the queue with the shortest-queue length as queue to visit upon arrival.
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code>, contains <code>null</code>,
   *                                    or the <code>rng</code> argument is <code>null</code>.
   * 
   */
  private static SimQueueSelector createSimQueueSelector
  (final Set<SimQueue> queues, final boolean onlyWaitingJobs, final Random rng)
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
        final Set<SimQueue> shortestQueues = selectShortestQueues (queues, onlyWaitingJobs);
        return getRandomSimQueueFromSet (shortestQueues, rng);
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
  
  /** Creates a parallel queue with Join-Shortest Queue selection policy given an event list and a list of queues.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues in no particular order.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param onlyWaitingJobs       Whether queue-length is the number of waiting jobs (<code>true</code>),
   *                                or the total number of jobs present (<code>true</code>).
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
   * @see SimQueue#getNumberOfJobs
   * @see SimQueue#getNumberOfJobsInServiceArea
   * @see Random
   * 
   */
  public JSQ
  (final SimEventList eventList,
    final Set<DQ> queues,
    final DelegateSimJobFactory delegateSimJobFactory,
    final boolean onlyWaitingJobs,
    final Random rng)
  {
    super (eventList, queues, 
      createSimQueueSelector ((Set<SimQueue>) queues, onlyWaitingJobs, ((rng != null) ? rng : new Random ())),
      delegateSimJobFactory);
    this.onlyWaitingJobs = onlyWaitingJobs;
  }

  /** Returns a new {@link JSQ} object on the same {@link SimEventList} with copies of the sub-queues,
   *  the same <code>onlyWaitingJobs</code> argument, a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link JSQ} object on the same {@link SimEventList} with copies of the sub-queues,
   *  the same <code>onlyWaitingJobs</code> argument, a new RNG, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see #getDelegateSimJobFactory
   * @see #isOnlyWaitingJobs
   * 
   */
  @Override
  public JSQ<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new JSQ<>
      (getEventList (), queuesCopy, getDelegateSimJobFactory (), isOnlyWaitingJobs (), null);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "JSQ[queue list]".
   * 
   * @return "JSQ[queue list]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String string = "JSQ[";
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
  // onlyWaitingJobs
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final boolean onlyWaitingJobs;
  
  /** Returns whether only waiting jobs are considered in queue selection.
   * 
   * @return <code>True</code> if only waiting jobs are considered in queue selection,
   *         <code>false</code> if <i>all</i> jobs present are considered.
   * 
   */
  public final boolean isOnlyWaitingJobs ()
  {
    return this.onlyWaitingJobs;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UTILITY METHODS FOR SHORTEST-QUEUE SELECTION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a subset holding the queues with shortest queue length from a set of {@link SimQueue}s.
   * 
   * @param queues           The queues, if empty, an empty set is returned.
   * @param onlyWaitingJobs  Whether only waiting jobs are considered in determining the queue length
   *                           (instead of all jobs present).
   * 
   * @return A non-<code>null</code> new subset holding the queues with shortest queue length (may be empty).
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code> or contains <code>null</code>.
   * 
   */
  private static Set<SimQueue> selectShortestQueues (final Set<SimQueue> queues, final boolean onlyWaitingJobs)
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    int currentNrOfJobs = 0;
    for (SimQueue q : queues)
    {
      final int nrOfJobs = (onlyWaitingJobs ? (q.getNumberOfJobs () - q.getNumberOfJobsInServiceArea ()) : q.getNumberOfJobs ());
      if (set.isEmpty ())
      {
        set.add (q);
        currentNrOfJobs = nrOfJobs;
      }
      else if (nrOfJobs < currentNrOfJobs)
      {
        set.clear ();
        set.add (q);
        currentNrOfJobs = nrOfJobs;
      }
      else if (nrOfJobs == currentNrOfJobs)
        set.add (q);
    }
    return set;
  }
  
  /** Selects a queue at random (with equal probabilities) from a set of queues.
   * 
   * @param queues The queues, non-<code>null</code>.
   * @param rng    The random-number generator to use, non-<code>null</code>.
   * 
   * @return A random member in the set, or <code>null</code> if the set was empty.
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code>, contains <code>null</code>,
   *                                    or the <code>rng</code> argument is <code>null</code>.
   * 
   */
  public static SimQueue getRandomSimQueueFromSet (final Set<SimQueue> queues, final Random rng)
  {
    if (queues == null || queues.contains (null) || rng == null)
      throw new IllegalArgumentException ();
    if (queues.isEmpty ())
      return null;
    if (queues.size () == 1)
      return queues.iterator ().next ();
    final int draw = rng.nextInt (queues.size ());
    int i_q = 0;
    for (final SimQueue q : queues)
    {
       if (i_q == draw)
        return q;
       i_q++;
    }
    throw new RuntimeException ();
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
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
