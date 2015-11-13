package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a {@link SimQueueComposite}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public abstract class AbstractSimQueueComposite
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractSimQueueComposite>
extends AbstractSimQueue<J, Q>
implements SimQueueComposite<DJ, DQ, J, Q>,
  SimQueueListener<DJ, DQ>, SimQueueSelector<J, DQ>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract network of queues.
   * 
   * @param eventList        The event list to be shared between this queue and the embedded queues.
   * @param queues           A set holding the "embedded" queues.
   * @param simQueueSelector The object for routing jobs through the network of embedded queues;
   *                         if {@code null}, no sub-queues will be visited.
   * 
   * @throws IllegalArgumentException If the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
   * 
   */
  protected AbstractSimQueueComposite
  (final SimEventList eventList, final Set<DQ> queues, final SimQueueSelector<J, DQ> simQueueSelector)
  {
    super (eventList);
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    this.queues = queues;
    this.simQueueSelector = simQueueSelector;
    for (DQ queue : this.queues)
      queue.registerSimEntityListener (this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (SUB)QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queues, all non-null.
   * 
   * Set is also non-null and final.
   * 
   */
  private final Set<DQ> queues;
  
  @Override
  public final Set<DQ> getQueues ()
  {
    return this.queues;
  }

  /** Returns the index of a given queue in a set.
   * 
   * <p>
   * Note that elements in a {@link Set} are unordered,
   * unless the specific implementation (like {@link LinkedHashSet}) takes care of that.
   * Be careful with this method!
   * 
   * @param queues The set, must be non-{@code null}.
   * @param queue  The queue; must be present in the set and non-{@code null}.
   * 
   * @return The index of the queue in the set (by iteration order).
   * 
   * @throws IllegalArgumentException If the set or the queue is {@code null},
   *                                  or if the queue is not present in the set.
   * 
   * @param <DQ> The queue-type.
   * 
   */
  public static <DQ extends SimQueue> int getIndex (final Set<DQ> queues, final DQ queue)
  {
    if (queues == null || queue == null || ! queues.contains (queue))
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = queues.iterator ();
    for (int q = 0; q < queues.size (); q++)
      if (iterator.next () == queue)
        return q;
    throw new IllegalArgumentException ();
  }
  
  /** Returns the index of given sub-queue.
   * 
   * @param queue The sub-queue; must be present in {@link #getQueues}.
   * 
   * @return The index of the sub-queue in {@link #getQueues}.
   * 
   * @throws IllegalArgumentException If the <code>queue</code> is <code>null</code> or not present in {@link #getQueues}.
   * 
   */
  protected final int getIndex (final DQ queue)
  {
    return AbstractBlackSimQueueComposite.getIndex (getQueues (), queue);
  }
  
  /** Returns a queue in a set by its index.
   * 
   * <p>
   * Note that elements in a {@link Set} are unordered,
   * unless the specific implementation (like {@link LinkedHashSet}) takes care of that.
   * Be careful with this method!
   * 
   * @param queues The queues.
   * @param q      The index.
   * 
   * @return The queue in the set with given index (by iteration order).
   * 
   * @throws IllegalArgumentException If the set is {@code null}
   *                                  or the index is (strictly) negative or larger or equal than the size of the set.
   *
   * @param <DQ> The queue-type.
   * 
   */
  public static <DQ extends SimQueue> DQ getQueue (final Set<DQ> queues, final int q)
  {
    if (queues == null || q < 0 || q >= queues.size ())
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = queues.iterator ();
    int i = 0;
    DQ dq = iterator.next ();
    while (i < q)
    {
      i++;
      dq = iterator.next ();
    }
    return dq;    
  }
  
  /** Returns a sub-queue by its index.
   * 
   * @param q The index.
   * 
   * @return The (sub-)queue in {@link #getQueues} with given index.
   * 
   * @throws IllegalArgumentException If the index is (strictly) negative or larger or equal than the size of {@link #getQueues}.
   * 
   */
  protected final DQ getQueue (final int q)
  {
    return AbstractBlackSimQueueComposite.getQueue (getQueues (), q);
  }
  
  /** Returns a copy of a given set of {@link SimQueue}s, each of which is copied in itself.
   * 
   * @param queues The set of {@link SimQueue}s.
   * 
   * @return A copy of the given set of {@link SimQueue}s, each of which is copied in itself.
   * 
   * @see SimQueue#getCopySimQueue
   * 
   * @throws IllegalArgumentException If <code>queues == null </code> or contains a <code>null</code> element.
   * @throws UnsupportedOperationException If copying any of the sub-queues is unsupported; this should be considered as a
   *         software error.
   * 
   */
  public static Set<SimQueue> getCopySimQueues (final Set<SimQueue> queues) throws UnsupportedOperationException
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    for (final SimQueue q : queues)
      set.add (q.getCopySimQueue ());
    return set;    
  }
  
  /** Returns a copy of the set of sub-queues, each of which is copied in itself.
   * 
   * @return A copy of the set of sub-queues, each of which is copied in itself.
   * 
   * @see #getQueues
   * @see SimQueue#getCopySimQueue
   * 
   * @throws UnsupportedOperationException If copying any of the sub-queues is unsupported; this should be considered as a
   *         software error.
   * 
   */
  protected final Set<DQ> getCopySubSimQueues () throws UnsupportedOperationException
  {
    final Set<DQ> set = new LinkedHashSet<> ();
    for (final DQ dq : getQueues ())
      set.add ((DQ) dq.getCopySimQueue ());
    return set;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The {@link SimQueueSelector} for selecting the internal queue to visit.
   * 
   */
  private final SimQueueSelector<J, DQ> simQueueSelector;
  
  /** Returns the {@link SimQueueSelector} for selecting the internal queue to visit.
   * 
   * @return The {@link SimQueueSelector} for selecting the internal queue to visit.
   * 
   */
  @Override
  public final SimQueueSelector<J, DQ> getSimQueueSelector ()
  {
    return this.simQueueSelector;
  }

  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    if (this.simQueueSelector != null && this.simQueueSelector != this)
      return this.simQueueSelector.selectFirstQueue (time, job);
    else
      return null;
  }
  
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (this.simQueueSelector != null && this.simQueueSelector != this)
      return this.simQueueSelector.selectNextQueue (time, job, previousQueue);
    else
      return null;
  }

}
