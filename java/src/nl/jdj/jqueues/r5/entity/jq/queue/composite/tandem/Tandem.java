package nl.jdj.jqueues.r5.entity.jq.queue.composite.tandem;

import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.dual.ctandem2.CompressedTandem2SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A tandem (serial) queue.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence
 * (as determined by the deterministic iteration order over the sub-queues).
 * 
 * <p>
 * Internally, a {@link TandemSimQueueSelector} is generated from the sub-queues supplied.
 * 
 * <p>
 * The start model is set to (fixed) {@link StartModel#LOCAL}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see TandemSimQueueSelector
 * @see CompressedTandem2SimQueue
 * @see StartModel
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
public class Tandem
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends Tandem>
  extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a tandem queue given an event list and a list of queues to put in sequence.
   *
   * <p>
   * The start model is set to (fixed) {@link StartModel#LOCAL}.
   * 
   * @param eventList             The event list to use.
   * @param queues                The queues, an iteration over the set must return (deterministically)
   *                              the non-<code>null</code> queues in intended order of visit.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see StartModel
   * 
   */
  public Tandem
  (final SimEventList eventList,
   final Set<DQ> queues,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, new TandemSimQueueSelector<> (queues), delegateSimJobFactory, StartModel.LOCAL);
  }

  @Override
  public Tandem<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new Tandem<>
      (getEventList (), queuesCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Tandem[queue list]".
   * 
   * @return "Tandem[queue list]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String string = "Tandem[";
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