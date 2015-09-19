package nl.jdj.jqueues.r3.composite;

import java.util.Iterator;
import java.util.Set;
import nl.jdj.jqueues.r3.AbstractSimJob;
import nl.jdj.jqueues.r3.SimJob;
import nl.jdj.jqueues.r3.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class BlackTandemSimQueue<DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackTandemSimQueue>
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
  implements BlackSimQueueNetwork<DJ, DQ, J, Q>
{

  /** Creates a black tandem queue given an event list and a list of queues to put in sequence.
   *
   * @param eventList The event list to use.
   * @param queues    The queues, an iteration over the set must return (deterministically) the non-<code>null</code> queues
   *                  in intended order of visit.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackTandemSimQueue
  (final SimEventList eventList, final Set<DQ> queues, final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, delegateSimJobFactory);
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
    return (getQueues ().isEmpty () ? null : getQueues ().iterator ().next ());
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
    final Iterator<DQ> iterator = getQueues ().iterator ();
    boolean found = false;
    while (iterator.hasNext () && ! found)
      found = (iterator.next () == previousQueue);
    if (! found)
      throw new IllegalStateException ();
    return (iterator.hasNext () ? iterator.next () : null);
  }
  
}
