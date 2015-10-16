package nl.jdj.jqueues.r4.composite;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A composite queue with two queues, a main one and one collecting all dropped jobs from the main queue.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackDropCollectorSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackDropCollectorSimQueue>
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
{

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
  
  private /* final */ DQ getMainQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    return iterator.next ();
  }
  
  private /* final */ DQ getDropQueue ()
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
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black drop-coolector queue given an event list, a main queue and a drop (collector) queue.
   *
   * @param eventList The event list to use.
   * @param mainQueue  The wait queue.
   * @param dropQueue The serve queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackDropCollectorSimQueue
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> mainQueue,
   final SimQueue<DJ, DQ> dropQueue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, (Set<DQ>) createQueuesSet (mainQueue, dropQueue), delegateSimJobFactory);
  }

  @Override
  protected final SimQueue<DJ, DQ> getFirstQueue (final double time, final J job)
  {
    return getMainQueue ();
  }

  @Override
  protected SimQueue<DJ, DQ> getNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (previousQueue == null || ! getQueues ().contains (previousQueue))
      throw new IllegalStateException ();
    return null;
  }

  @Override
  protected final DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    if (queue != getMainQueue ())
      throw new IllegalStateException ();
    return getDropQueue ();
  }

  @Override
  public final void reset ()
  {
    super.reset ();
  }

  @Override
  public final boolean isNoWaitArmed ()
  {
    return getMainQueue ().isNoWaitArmed ();
  }
  
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

  @Override
  public final void update (double time)
  {
    super.update (time);
  }

  @Override
  protected final void startForSubClass (final double time, final DJ job, final DQ queue)
  {
    super.startForSubClass (time, job, queue);
  }

  /** Returns "DropCol[mainQueue,dropQueue]".
   * 
   * @return "DropCol[mainQueue,dropQueue]".
   * 
   */
  @Override
  public String toString ()
  {
    return "DropCol[" + getMainQueue () + "," + getDropQueue () + "]";
  }

}
