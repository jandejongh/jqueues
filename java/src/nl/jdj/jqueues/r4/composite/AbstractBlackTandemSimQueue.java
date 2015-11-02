package nl.jdj.jqueues.r4.composite;

import java.util.Iterator;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a tandem (serial) queue.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence.
 * 
 * <p>
 * Under the hood, a delegate job for each {@link SimJob} visits each of the
 * embedded {@link SimQueue}s in a predetermined sequence, as controlled
 * by a the queues' order in the set in which they are offered upon construction
 * of a {@link AbstractBlackTandemSimQueue}.
 * 
 * <p>
 * After the delegate job departs from the last queue, the "real" job departs
 * from the {@link AbstractBlackTandemSimQueue}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see BlackTandemSimQueue
 * @see BlackCompressedTandem2SimQueue
 * 
 */
public abstract class AbstractBlackTandemSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackTandemSimQueue>
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
  implements BlackSimQueueNetwork<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract black tandem queue given an event list and a list of queues to put in sequence.
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
   * 
   */
  protected AbstractBlackTandemSimQueue
  (final SimEventList eventList, final Set<DQ> queues, final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, delegateSimJobFactory);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AbstractBlackSimQueueNetwork
  // ABSTRACT METHODS FOR (SUB-)QUEUE SELECTION IN SUBCLASSES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the first {@link SimQueue} to visit by a delegate {@link SimJob}.
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

  /** Returns the next {@link SimQueue} to visit by a delegate {@link SimJob}.
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // allowSubQueueAccessVacationChanges
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code false}.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  protected final boolean getAllowSubQueueAccessVacationChanges ()
  {
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code> if there are no queues in {@link #getQueues}
   * or if the first queue in that list is in <code>noWaitArmed</code> state.
   * 
   * This overrides the default implementation {@link AbstractBlackSimQueueNetwork#isNoWaitArmed} which demands <i>all</i> queues
   * to be in <code>noWaitArmed</code> state.
   * 
   * {@inheritDoc}
   * 
   * @return True if there are no queues in {@link #getQueues}
   *           or if the first queue in that list is in <code>noWaitArmed</code> state.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getQueues ().isEmpty () ? true : getQueues ().iterator ().next ().isNoWaitArmed ();
  }
  
}
