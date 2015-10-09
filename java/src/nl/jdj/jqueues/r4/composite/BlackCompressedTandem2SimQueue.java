package nl.jdj.jqueues.r4.composite;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** Tandem (serial) queue with two queues, one for waiting and one for serving.
 *
 * This special {@link BlackTandemSimQueue} only allows two distinct queues, one for waiting and a second one for serving.
 * The tandem queue bypasses the service part of the first queue, only using its wait and job-selection policies,
 * and bypasses the waiting part of the second queue.
 * 
 * <p>The main purpose of this rather exotic {@link SimQueue} is to replace the waiting queue of an existing {@link SimQueue}
 * implementation with another one in order to, e.g., change from FCFS behavior in the waiting area to LCFS behavior.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackCompressedTandem2SimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackTandemSimQueue>
  extends BlackTandemSimQueue<DJ, DQ, J, Q>
{

  /** Auxiliary method to create the required {@link Set} of {@link SimQueue}s in the constructor.
   * 
   * @param waitQueue  The wait queue.
   * @param serveQueue The serve queue.
   * 
   * @return A {@link LinkedHashSet} holding both {@link SimQueue}s in the proper order.
   * 
   */
  private static Set<SimQueue> createQueuesSet (final SimQueue waitQueue, final SimQueue serveQueue)
  {
    if (waitQueue == null || serveQueue == null || waitQueue == serveQueue)
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (waitQueue);
    set.add (serveQueue);
    return set;
  }
  
  private /* final */ DQ getWaitQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    return iterator.next ();
  }
  
  private /* final */ DQ getServeQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    iterator.next ();
    return iterator.next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black compressed tandem queue given an event list, a wait queue and a serve queue.
   *
   * @param eventList The event list to use.
   * @param waitQueue  The wait queue.
   * @param serveQueue The serve queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code> or equal.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackCompressedTandem2SimQueue
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> waitQueue,
   final SimQueue<DJ, DQ> serveQueue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, (Set<DQ>) createQueuesSet (waitQueue, serveQueue), delegateSimJobFactory);
  }

  @Override
  public final void newNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.newNoWaitArmed (time, queue, noWaitArmed);
    if (! getQueues ().contains (queue))
      throw new IllegalArgumentException ();
    if (queue == getServeQueue ())
      getWaitQueue ().setServerAccessCredits (noWaitArmed ? 1 : 0);
  }

  @Override
  protected final void startForSubClass (final double time, final DJ job, final DQ queue)
  {
    super.startForSubClass (time, job, queue);
    if (queue == getWaitQueue ())
    {
      if (! getWaitQueue ().revoke (job, time, true))
        throw new RuntimeException ();
      getServeQueue ().arrive (job, time);
    }
    
  }


}
