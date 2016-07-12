package nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite.StartModel;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jsimulation.r5.SimEventList;

/** Compressed tandem (serial) queue with two queues, one used for waiting and one used for serving.
 *
 * <p>
 * This special black composite queue only allows two distinct queues, one for waiting and a second one for serving.
 * The composite queue bypasses the service part of the first queue, only using its wait and job-selection policies,
 * and bypasses the waiting part of the second queue.
 * 
 * <p>The main purpose of this rather exotic {@link SimQueue} is to replace the waiting queue of an existing {@link SimQueue}
 * implementation with another one in order to, e.g., change from FIFO behavior in the waiting area to LIFO behavior.
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the black composite queue.
 * For more details, refer to {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see BlackSimQueueComposite
 * @see StartModel
 * @see StartModel#COMPRESSED_TANDEM_2_QUEUE
 * @see #setStartModel
 * 
 */
public class BlackCompressedTandem2SimQueue
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackCompressedTandem2SimQueue>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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
  
  /** Creates a black compressed tandem queue given an event list, a wait queue and a serve queue,
   *  and an optional factory for delegate jobs.
   *
   * @param eventList             The event list to use.
   * @param waitQueue             The wait queue.
   * @param serveQueue            The serve queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  one of or both queues are <code>null</code> or equal.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see StartModel
   * @see StartModel#COMPRESSED_TANDEM_2_QUEUE
   * @see #setStartModel
   * 
   */
  public BlackCompressedTandem2SimQueue
  (final SimEventList eventList,
   final SimQueue<DJ, DQ> waitQueue,
   final SimQueue<DJ, DQ> serveQueue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      (Set<DQ>) createQueuesSet (waitQueue, serveQueue),
      new SimQueueSelector<J, DQ> ()
      {
        @Override
        public DQ selectFirstQueue (double time, J job)
        {
          return (DQ) waitQueue;
        }
        @Override
        public DQ selectNextQueue (double time, J job, DQ previousQueue)
        {
          if (previousQueue == null)
            throw new IllegalArgumentException ();
          if (previousQueue == waitQueue)
            return (DQ) serveQueue;
          if (previousQueue == serveQueue)
            return null;
          throw new IllegalArgumentException ();
        }
      },
      delegateSimJobFactory);
    setStartModel (StartModel.COMPRESSED_TANDEM_2_QUEUE);
  }

  /** Returns a new {@link BlackCompressedTandem2SimQueue} object on the same {@link SimEventList} with copies of the wait and
   * serve queues and the same delegate-job factory.
   * 
   * @return A new {@link BlackCompressedTandem2SimQueue} object on the same {@link SimEventList} with copies of the wait and
   * serve queues and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the wait or serve queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackCompressedTandem2SimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> waitQueueCopy = getWaitQueue ().getCopySimQueue ();
    final SimQueue<DJ, DQ> serveQueueCopy = getServeQueue ().getCopySimQueue ();
    return new BlackCompressedTandem2SimQueue<> (getEventList (), waitQueueCopy, serveQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WAIT AND SERVE QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the wait (first) queue.
   * 
   * @return The wait (first) queue.
   * 
   */
  protected final DQ getWaitQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    return iterator.next ();
  }
  
  /** Gets the serve (second, last) queue.
   * 
   * @return The serve (second, last) queue.
   * 
   */
  protected final DQ getServeQueue ()
  {
    final Iterator<DQ> iterator = getQueues ().iterator ();
    iterator.next ();
    return iterator.next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "ComprTandem2[waitQueue,serveQueue]".
   * 
   * @return "ComprTandem2[waitQueue,serveQueue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "ComprTandem2[" + getWaitQueue () + "," + getServeQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
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
