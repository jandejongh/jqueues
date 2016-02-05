package nl.jdj.jqueues.r5.entity.queue.composite.tandem;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link BlackSimQueueComposite} encapsulating a single {@link SimQueue}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackEncapsulatorSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackEncapsulatorSimQueue>
  extends AbstractBlackTandemSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Auxiliary method to create the required {@link Set} of a single {@link SimQueue} in the constructor.
   * 
   * @param queue  The queue.
   * 
   * @return A {@link LinkedHashSet} holding the {@link SimQueue}.
   * 
   */
  private static Set<SimQueue> createQueuesSet (final SimQueue queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (queue);
    return set;
  }
  
  /** Creates a black encapsulated queue given an event list and a queue.
   *
   * @param eventList The event list to use.
   * @param queue     The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackEncapsulatorSimQueue
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, (Set<DQ>) createQueuesSet (queue), delegateSimJobFactory);
  }
  
  /** Returns a new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *         queue and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackEncapsulatorSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new BlackEncapsulatorSimQueue<> (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENCAPSULATED QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public final SimQueue<DJ, DQ> getEncapsulatedQueue ()
  {
    return getQueues ().iterator ().next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Enc[encapsulated queue]".
   * 
   * @return "Enc[encapsulated queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "Enc[" + getQueues ().iterator ().next () + "]";
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
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
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
  protected final DQ getDropDestinationQueue (final double time, final DJ job, final DQ queue)
  {
    return super.getDropDestinationQueue (time, job, queue);
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
