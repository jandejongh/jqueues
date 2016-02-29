package nl.jdj.jqueues.r5.entity.queue.composite.parallel;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jsimulation.r5.SimEventList;

/** Parallel queues.
 * 
 * <p>
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
  implements BlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black parallel queue given an event list and a list of queues to put in parallel.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues in no particular order.
   * @param simQueueSelector      An optional {@link SimQueueSelector} for arriving jobs; if <code>null</code>,
   *                                jobs will leave immediately upon arrival.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry,
   *                                  or if no suitable <code>simQueueSelector</code> is found.
   * 
   * @see ParallelSimQueuesSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackParallelSimQueues
  (final SimEventList eventList,
    final Set<DQ> queues,
    final SimQueueSelector simQueueSelector,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, simQueueSelector, delegateSimJobFactory);
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
    // XXX I NEED A NEW SimQueueSelector HERE!!!
    final SimQueueSelector simQueueSelectorCopy = getSimQueueSelector ();
    return new BlackParallelSimQueues<>
      (getEventList (), queuesCopy, simQueueSelectorCopy, getDelegateSimJobFactory ());
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
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
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
  public String toStringDefault ()
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
