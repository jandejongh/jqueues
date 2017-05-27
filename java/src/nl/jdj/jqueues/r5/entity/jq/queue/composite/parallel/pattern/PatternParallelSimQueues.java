package nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.pattern;

import java.util.Arrays;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.AbstractParallelSimQueues;
import nl.jdj.jsimulation.r5.SimEventList;

/** Parallel queues with pattern selection policy.
 *
 * <p>
 * Jobs are assigned a sub-queue according to a fixed (repeated) selection pattern.
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
public class PatternParallelSimQueues
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends PatternParallelSimQueues>
  extends AbstractParallelSimQueues<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} that selects sub-queues according to a pattern.
   * 
   * <p>
   * After departure from the first queue, the job is made to depart by selecting <code>null</code> as its next queue.
   * 
   * @param queues  The queues, non-<code>null</code>.
   * @param pattern The queue-selection pattern; if {@code null} or empty,
   *                  or if the current value in the pattern for a job is out-of-range,
   *                  the job departs upon arrival (in fact, upon start).
   * 
   * @return A new {@link SimQueueSelector} that selects sub-queues according to a pattern.
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code> or contains <code>null</code>.
   * 
   */
  private static SimQueueSelector createSimQueueSelector
  (final Set<SimQueue> queues, final int[] pattern)
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    return new SimQueueSelector ()
    {
      private int lastIndex = -1;
      @Override
      public void resetSimQueueSelector ()
      {
        this.lastIndex = -1;
      }
      @Override
      public final SimQueue selectFirstQueue (final double time, final SimJob job)
      {
        if (job == null)
          throw new IllegalArgumentException ();
        if (pattern == null || pattern.length == 0)
          return null;
        this.lastIndex = (this.lastIndex + 1) % pattern.length;
        final int queueIndex = pattern[this.lastIndex];
        if (queueIndex < 0 || queueIndex >= queues.size ())
          return null;
        else
          return AbstractSimQueueComposite.getQueue (queues, queueIndex);
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
  
  /** Creates a parallel queueing system with pattern selection policy.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues (with deterministic iteration order).
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param pattern               The queue-selection pattern; if {@code null} or empty,
   *                                or if the current value in the pattern for a job is out-of-range,
   *                                the job departs upon arrival (in fact, upon start).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimQueueSelector
   * 
   */
  public PatternParallelSimQueues
  (final SimEventList eventList,
    final Set<DQ> queues,
    final DelegateSimJobFactory delegateSimJobFactory,
    final int[] pattern)
  {
    super (eventList,
      queues, 
      createSimQueueSelector ((Set<SimQueue>) queues, pattern),
      delegateSimJobFactory);
    if (pattern == null)
      this.pattern = null;
    else
      this.pattern = Arrays.copyOf (pattern, pattern.length);
  }

  /** Returns a new {@link PatternParallelSimQueues} object on the same {@link SimEventList} with copies of the sub-queues,
   *  the same pattern, and the same delegate-job factory.
   * 
   * @return A new {@link PatternParallelSimQueues} object on the same {@link SimEventList} with copies of the sub-queues,
   *           the same pattern, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see #getDelegateSimJobFactory
   * @see #getPattern
   * 
   */
  @Override
  public PatternParallelSimQueues<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new PatternParallelSimQueues<>
      (getEventList (), queuesCopy, getDelegateSimJobFactory (), getPattern ());
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
  
  /** Returns "Pattern[queue list]".
   * 
   * @return "Pattern[queue list]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String string = "Pattern{";
    if (this.pattern == null)
      string += "null";
    else
    {
      boolean first = true;
      for (int i : this.pattern)
      {
        if (! first)
          string += ",";
        else
          first = false;
        string += i;
      }
    }
    string += "}[";
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
  // PATTERN
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final int[] pattern;
  
  /** Returns the pattern associated with this queue.
   * 
   * <p>
   * For non-{@code null} patterns, an array copy of the internally held pattern is returned.
   * 
   * @return The pattern (read-only), may be {@code null} or empty.
   * 
   */
  public final int[] getPattern ()
  {
    if (this.pattern == null)
      return null;
    return Arrays.copyOf (this.pattern, this.pattern.length);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}