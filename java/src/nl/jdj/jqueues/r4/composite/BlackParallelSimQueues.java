package nl.jdj.jqueues.r4.composite;

import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** Parallel queues.
 * 
 * Under the hood, a delegate job for each {@link SimJob} visits one and only one of the
 * embedded {@link SimQueue}s, as controlled by a {@link SimQueueSelector}.
 * 
 * <p>
 * After the delegate job departs from its internal queue, the "real" job departs
 * from the {@link BlackParallelSimQueues}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackParallelSimQueues
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackParallelSimQueues>
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
  implements BlackSimQueueNetwork<DJ, DQ, J, Q>
{
  
  /** The {@link SimQueueSelector} for selecting the internal queue to visit.
   * 
   */
  private final SimQueueSelector<J, DJ, DQ> simQueueSelector;
  
  /** Creates a black parallel queue given an event list and a list of queues to put in parallel.
   *
   * @param eventList The event list to use.
   * @param queues    The queues in no particular order.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param simQueueSelector An optional {@link SimQueueSelector} for arriving jobs; if <code>null</code>, this
   *                         {@link BlackParallelSimQueues} must itself be a (and is selected as the)
   *                         {@link SimQueueSelector}.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry,
   *                                  or if no suitable <code>simQueueSelector</code> is found.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimQueueSelector
   * 
   */
  public BlackParallelSimQueues
  (final SimEventList eventList,
    final Set<DQ> queues,
    final DelegateSimJobFactory delegateSimJobFactory,
    final SimQueueSelector simQueueSelector)
  {
    super (eventList, queues, delegateSimJobFactory);
    if (simQueueSelector == null)
    {
      if (! (this instanceof SimQueueSelector))
        throw new IllegalArgumentException ();
      this.simQueueSelector = (SimQueueSelector) this;
    }
    else
      this.simQueueSelector = simQueueSelector;
  }

  /**
   * {@inheritDoc}
   * 
   * @return The first {@link SimQueue} returned by an iterator over the set obtained from {@link #getQueues},
   *         or <code>null</code> if that set is empty.
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getFirstQueue (final double time, final J job)
  {
    return this.simQueueSelector.selectFirstQueue (time, job);
  }

  /**
   * {@inheritDoc}
   * 
   * @return The next {@link SimQueue} after the <code>previousQueue</code> in an iterator
   *         over the set obtained from {@link #getQueues},
   *         or <code>null</code> if no such exists
   *         (i.e., <code>previousQueue</code> is the last element returned from the iterator).
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code> or not a member of {@link #getQueues}.
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (getQueues ().isEmpty ())
      throw new IllegalStateException ();
    if (previousQueue == null)
      throw new IllegalStateException ();
    return null;
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void startForSubClass (final double t, final DJ job, final DQ queue)
  {
    super.startForSubClass (t, job, queue);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    return super.getDropDestinationQueue (t, job, queue);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Returns "Par[queue list]".
   * 
   * @return "Par[queue list]".
   * 
   */
  @Override
  public String toString ()
  {
    String string = "Par[";
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

}
