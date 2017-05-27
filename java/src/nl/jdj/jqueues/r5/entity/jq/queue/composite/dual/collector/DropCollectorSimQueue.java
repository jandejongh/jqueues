package nl.jdj.jqueues.r5.entity.jq.queue.composite.dual.collector;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A composite queue with two queues, a main one and one collecting all dropped jobs from the main queue.
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
public class DropCollectorSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends DropCollectorSimQueue>
  extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Auxiliary method to create the required {@link Set} of {@link SimQueue}s in the constructor.
   * 
   * Note that the mainQueue and the dropQueue arguments may be equal!
   * 
   * @param mainQueue The wait queue.
   * @param dropQueue The serve queue.
   * 
   * @return A {@link LinkedHashSet} holding both {@link SimQueue}s in the proper order.
   * 
   */
  private static Set<SimQueue> createQueuesSet (final SimQueue mainQueue, final SimQueue dropQueue)
  {
    if (mainQueue == null || dropQueue == null)
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (mainQueue);
    set.add (dropQueue);
    return set;
  }
  
  /** Creates a drop-collector queue given an event list, a main queue and a drop (collector) queue.
   *
   * @param eventList             The event list to use.
   * @param mainQueue             The main queue.
   * @param dropQueue             The drop queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code>.
   * 
   * @see DropCollectorSimQueueSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see #setDropDestinationQueue
   * 
   */
  public DropCollectorSimQueue
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> mainQueue,
   final SimQueue<DJ, DQ> dropQueue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      (Set<DQ>) createQueuesSet (mainQueue, dropQueue),
      new DropCollectorSimQueueSelector (mainQueue, dropQueue),
      delegateSimJobFactory);
    setDropDestinationQueue ((DQ) dropQueue);
  }

  /** Returns a new {@link DropCollectorSimQueue} object on the same {@link SimEventList} with copies of the main and
   *  drop queues and the same delegate-job factory.
   * 
   * @return A new {@link DropCollectorSimQueue} object on the same {@link SimEventList} with copies of the main and
   * drop queues and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the main or drop queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getMainQueue
   * @see #getDropQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public DropCollectorSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    // XXX Not correct for white composite queues?
    final SimQueue<DJ, DQ> mainQueueCopy = getMainQueue ().getCopySimQueue ();
    final SimQueue<DJ, DQ> dropQueueCopy = getDropQueue ().getCopySimQueue ();
    return new DropCollectorSimQueue<> (getEventList (), mainQueueCopy, dropQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN AND DROP QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the main (first) queue.
   * 
   * @return The main (first) queue.
   * 
   */
  protected final DQ getMainQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    return iterator.next ();
  }
  
  /** Returns the drop (second, last) queue.
   * 
   * @return The drop (second, last) queue.
   * 
   */
  protected final DQ getDropQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    final DQ firstQueue = iterator.next ();
    if (! iterator.hasNext ())
      return firstQueue;
    else
      return iterator.next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "DropCol[mainQueue,dropQueue]".
   * 
   * @return "DropCol[mainQueue,dropQueue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DropCol[" + getMainQueue () + "," + getDropQueue () + "]";
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
