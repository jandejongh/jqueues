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
  extends AbstractBlackParallelSimQueues<DJ, DQ, J, Q>
  implements BlackSimQueueNetwork<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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
    super (eventList, queues, delegateSimJobFactory, simQueueSelector);
  }

  /** Returns a new {@link BlackParallelSimQueues} object on the same {@link SimEventList} with copies of the sub-queues,
   *  the same {@link SimQueueSelector} (bearing in mind <code>this</code> could be its own selector),
   *  and the same delegate-job factory.
   * 
   * @return A new {@link BlackParallelSimQueues} object on the same {@link SimEventList} with copies of the sub-queues,
   *  the same {@link SimQueueSelector} (bearing in mind <code>this</code> could be its own selector),
   *  and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see #getSimQueueSelector
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackParallelSimQueues<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    final SimQueueSelector simQueueSelectorCopy = (getSimQueueSelector () == this ? null : getSimQueueSelector ());
    return new BlackParallelSimQueues<>
      (getEventList (), queuesCopy, getDelegateSimJobFactory (), simQueueSelectorCopy);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Par[queue list]".
   * 
   * @return "Par[queue list]".
   * 
   */
  @Override
  public final String toStringDefault ()
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return super.isNoWaitArmed ();
  }

}
