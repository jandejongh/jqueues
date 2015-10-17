package nl.jdj.jqueues.r4.composite;

import java.util.Iterator;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** Tandem (serial) queue.
 * 
 * Under the hood, a delegate job for each {@link SimJob} visits each of the
 * embedded {@link SimQueue}s in a predetermined sequence, as controlled
 * by a the queues' order in the set in which they are offered upon construction
 * of a {@link BlackTandemSimQueue}.
 * 
 * <p>
 * After the delegate job departs from the last queue, the "real" job departs
 * from the {@link BlackTandemSimQueue}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
  
//  /** Calls super method (in order to make implementation final).
//   * 
//   * {@inheritDoc}
//   * 
//   */
//  @Override
//  public final void newNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
//  {
//    super.newNoWaitArmed (time, queue, noWaitArmed);
//  }
//
//  /** Calls super method (in order to make implementation final).
//   * 
//   * {@inheritDoc}
//   * 
//   */
//  @Override
//  protected final void startForSubClass (final double t, final DJ job, final DQ queue)
//  {
//    super.startForSubClass (t, job, queue);
//  }
//
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
  public final void update (final double time)
  {
    super.update (time);
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

}
