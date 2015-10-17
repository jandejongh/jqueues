package nl.jdj.jqueues.r4.composite;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A {@link BlackSimQueueNetwork} encapsulating a single {@link SimQueue}.
 *
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackEncapsulatorSimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackEncapsulatorSimQueue>
  extends BlackTandemSimQueue<DJ, DQ, J, Q>
{
  
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
  
  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void startForSubClass (final double t, final DJ job, final DQ queue)
  {
    super.startForSubClass (t, job, queue);
  }

  /** Returns "Enc[encapsulated queue]".
   * 
   * @return "Enc[encapsulated queue]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "Enc[" + getQueues ().iterator ().next () + "]";
  }

}
