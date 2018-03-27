package nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel;

import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite_LocalStart;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jsimulation.r5.SimEventList;

/** Parallel queues (with user-supplied queue selector).
 * 
 * <p>
 * In a parallel composite queue,
 * a (delegate) job visits one and only one of the
 * embedded {@link SimQueue}s,
 * as controlled by a {@link SimQueueSelector} supplied by concrete subclasses.
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
public class Par
<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends Par>
  extends AbstractParallelSimQueues<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a (abstract) parallel queue given an event list and a list of queues to put in parallel.
   *
   * @param eventList             The event list to use.
   * @param queues                The queues in no particular order.
   * @param simQueueSelector      An optional {@link SimQueueSelector} for arriving jobs; if <code>null</code>,
   *                                jobs will leave immediately upon arrival.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   *
   * @see ParallelSimQueuesSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public Par
  (final SimEventList eventList,
   final Set<DQ> queues,
   final SimQueueSelector simQueueSelector,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, new ParallelSimQueuesSelector (simQueueSelector), delegateSimJobFactory);
  }

  @Override
  public Par<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new Par<>
      (getEventList (), queuesCopy, getSimQueueSelector (), getDelegateSimJobFactory ());
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
