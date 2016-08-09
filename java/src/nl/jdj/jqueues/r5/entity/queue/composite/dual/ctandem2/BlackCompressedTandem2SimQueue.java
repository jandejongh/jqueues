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
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.SJF;
import nl.jdj.jsimulation.r5.SimEventList;

/** Compressed tandem (serial) queue with two queues, one used for waiting and one used for serving.
 *
 * <p>
 * This special black composite queue only allows two distinct queues, one for waiting and a second one for serving.
 * The composite queue bypasses the service part of the first queue, only using its wait and job-selection policies,
 * and bypasses the waiting part of the second queue.
 * 
 * <p>
 * The main purpose of this rather exotic {@link SimQueue} is to replace the waiting queue of an existing {@link SimQueue}
 * implementation with another one in order to, e.g., change from FIFO behavior in the waiting area to LIFO behavior.
 * It attempts to achieve this by controlling the server-access credits on the first sub-queue, the <i>wait</i> queue,
 * allowing jobs on it to start (one at a time) <i>only</i> if the second queue, the <i>serve</i> queue,
 * has {@link SimQueue#isNoWaitArmed} set to {@code true}.
 * Jobs that start on the wait queue are then auto-revoked ({@link AutoRevocationPolicy#UPON_START}),
 * and the composite queue (this) lets the job (well, in fact, its <i>delegate</i> job)
 * arrive on the serve queue.
 * A real job starts <i>only</i> if and when it is actually moved from the wait to the serve queue.
 * When the delegate job departs from the serve queue, its real job departs from the composite queue.
 * 
 * <p>
 * The interpretation in terms of replacing the wait-behavior and job-selection behavior of the serve queue
 * with that of the wait queue has several complications, and does not hold in general.
 * For instance, the wait queue may never start a job, jobs may depart from its waiting area,
 * and the {@code NoWaitArmed == true} condition on the serve queue does not guarantee "start upon arrival"
 * (it may be dropped, or it may depart without starting at all).
 * 
 * <p>
 * Despite its complications, the {@link BlackCompressedTandem2SimQueue} can be very useful to
 * construct non-standard (at least, not available in this library) {@link SimQueue} implementations,
 * and reduces the pressure on this {@code jqueues} library to implement <i>all</i> possible combinations
 * of waiting-area (buffer) size, job-selection policies and number of servers.
 * For instance, a queueing discipline we have not implemented at this time of writing in {@code jqueues}
 * is multi-server {@link SJF}.
 * There is, however, a multi-server {@link FCFS_c} implementation which features an arbitrary finite number of servers,
 * so we can replace its FIFO waiting-area behavior with SJF with a {@link BlackCompressedTandem2SimQueue}
 * as shown below (refer to the constructor documentation for more details):
 * 
 * <pre>
 * <code>
 * final SimQueue waitQueue = new SJF (eventList);
 * final SimQueue serveQueue = new FCFS_c (eventList, numberOfServers);
 * final SimQueue sjf_c = new BlackCompressedTandem2SimQueue (eventList, waitQueue, serveQueue, delegateSimJobFactory);
 * </code>
 * </pre>
 * or even (ignoring generic-type arguments):
 * 
 * <pre>
 * <code>
 * public class SJF_c extends BlackCompressedTandem2SimQueue
 * {
 * 
 *   public SJF_c (final SimEventList eventList, final int numberOfServers, final DelegateSimJobFactory delegateSimJobFactory)
 *   {
 *     super (eventList, new SJF (eventList), new FCFS_c (eventList, numberOfServers), delegateSimJobFactory);
 *   }
 * </code>
 * {@code  @Override}
 * <code>  public String toStringDefault ()
 *   {
 *     return "SJF_" + ((FCFS_c) getServeQueue ()).getNumberOfServers () + "]";
 *   }
 * 
 * }
 * </code>
 * </pre>
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
   * <p>
   * The constructor,
   * after invoking the super constructor with an appropriate anonymous {@link SimQueueSelector} object,
   * sets the {@link StartModel} to {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}.
   * The constructor then sets the auto-revocation policy on the wait queue to {@link AutoRevocationPolicy#UPON_START}.
   * 
   * <p>
   * Note that setting the initial server-access credits on the wait queue is a side effect of {@link #setStartModel}.
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
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see SimQueue#setAutoRevocationPolicy
   * @see AutoRevocationPolicy#UPON_START
   * @see #resetEntitySubClassLocal
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
          else if (previousQueue == waitQueue)
          {
            if (job.getQueue () == null)
              throw new IllegalStateException ();
            if (! job.getQueue ().getJobs ().contains (job))
              throw new IllegalStateException ();
            if (job.getQueue ().getJobsInWaitingArea ().contains (job))
              return null;
            else
              throw new IllegalStateException ();
          }
          else if (previousQueue == serveQueue)
            return null;
          else
            throw new IllegalArgumentException ();
        }
      },
      delegateSimJobFactory);
    setStartModel (StartModel.COMPRESSED_TANDEM_2_QUEUE);
    getWaitQueue ().setAutoRevocationPolicy (AutoRevocationPolicy.UPON_START);
  }

  /** Returns a new {@link BlackCompressedTandem2SimQueue} object on the same {@link SimEventList} with copies of the wait and
   *  serve queues and the same delegate-job factory.
   * 
   * @return A new {@link BlackCompressedTandem2SimQueue} object on the same {@link SimEventList} with copies of the wait and
   *         serve queues and the same delegate-job factory.
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
