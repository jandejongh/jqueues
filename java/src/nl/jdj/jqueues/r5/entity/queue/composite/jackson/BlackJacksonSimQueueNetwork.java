package nl.jdj.jqueues.r5.entity.queue.composite.jackson;

import java.util.Random;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link BlackSimQueueComposite} implementation of a Jackson queueing network.
 *
 * <p>
 * For more documentation see {@link JacksonSimQueueSelector}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackJacksonSimQueueNetwork
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackJacksonSimQueueNetwork>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
  implements BlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black Jackson queueing network.
   * 
   * For brevity, <code>|Q|</code> is used as a shorthand for <code>queues.size ()</code>.
   * 
   * @param eventList             The event list to use.
   * @param queues                The queues, an iteration over the set must return (deterministically)
   *                                the non-<code>null</code> queues
   *                                as indexed in the probability arguments;
   *                              see also {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param pdfArrival            See {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param pdfTransition         See {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param userRNG               See {@link JacksonSimQueueSelector#JacksonSimQueueSelector}.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry,
   *                                  or if one or both of the probability arguments is improperly dimensioned or contains
   *                                  illegal values.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackJacksonSimQueueNetwork
  (final SimEventList eventList,
    final Set<DQ> queues,
    final double[] pdfArrival,
    final double[][] pdfTransition,
    final Random userRNG,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      queues,
      new JacksonSimQueueSelector<>  (queues, pdfArrival, pdfTransition, userRNG),
      delegateSimJobFactory);
  }

  /** Returns a new {@link BlackJacksonSimQueueNetwork} object on the same {@link SimEventList} with copies of the sub-queues and
   *  probability arguments, a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link BlackJacksonSimQueueNetwork} object on the same {@link SimEventList} with copies of the sub-queues and
   *  probability arguments, a new RNG, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see JacksonSimQueueSelector#getPdfArrival
   * @see JacksonSimQueueSelector#getPdfTransition
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackJacksonSimQueueNetwork<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new BlackJacksonSimQueueNetwork<>
      (getEventList (),
        queuesCopy,
        ((JacksonSimQueueSelector) getSimQueueSelector ()).getPdfArrival (),
        ((JacksonSimQueueSelector) getSimQueueSelector ()).getPdfTransition (),
        null,
        getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Jackson[queue list]".
   * 
   * @return "Jackson[queue list]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String string = "Jackson[";
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
  // DROP DESTINATION QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    return super.getDropDestinationQueue (t, job, queue);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // allowDelegateJobRevocations
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code false}.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  protected final boolean getAllowDelegateJobRevocations ()
  {
    return false;
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
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clear the administration of visits.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return super.isNoWaitArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // startForSubClass
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void startForSubClass (final double time, final DJ job, final DQ queue)
  {
    super.startForSubClass (time, job, queue);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // notifyNewNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

}
