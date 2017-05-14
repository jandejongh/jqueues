package nl.jdj.jqueues.r5.entity.queue.composite.tandem;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** Black tandem (serial) queue.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence.
 * 
 * <p>
 * In a <i>black</i> tandem queue,
 * under the hood, a so-called <i>delegate job</i> for each {@link SimJob} visits each of the
 * embedded {@link SimQueue}s in a predetermined sequence, as controlled
 * by the (deterministic) iteration order in the set offered upon construction
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
  extends AbstractBlackTandemSimQueue<DJ, DQ, J, Q>
  implements BlackSimQueueComposite<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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

  /** Returns a new {@link BlackTandemSimQueue} object on the same {@link SimEventList} with copies of the sub-queues,
   *  and the same delegate-job factory.
   * 
   * @return A new {@link BlackTandemSimQueue} object on the same {@link SimEventList} with copies of the sub-queues,
   *           and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackTandemSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new BlackTandemSimQueue<> (getEventList (), queuesCopy, getDelegateSimJobFactory ());
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

}
