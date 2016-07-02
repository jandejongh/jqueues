package nl.jdj.jqueues.r5.entity.queue.composite.tandem;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a tandem (serial) queue.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence.
 * 
 * <p>
 * Under the hood, a delegate job for each {@link SimJob} visits each of the
 * embedded {@link SimQueue}s in a predetermined sequence, as controlled
 * by the (deterministic) iteration order in the set offered upon construction.
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
 * @see TandemSimQueueSelector
 * @see BlackTandemSimQueue
 * @see BlackCompressedTandem2SimQueue
 * 
 */
public abstract class AbstractBlackTandemSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackTandemSimQueue>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
  implements BlackSimQueueComposite<DJ, DQ, J, Q>
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
    super (eventList, queues, new TandemSimQueueSelector<> (queues), delegateSimJobFactory);
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
   * This overrides the default implementation {@link AbstractBlackSimQueueComposite#isNoWaitArmed} which demands <i>all</i> queues
   * to be in <code>noWaitArmed</code> state.
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
