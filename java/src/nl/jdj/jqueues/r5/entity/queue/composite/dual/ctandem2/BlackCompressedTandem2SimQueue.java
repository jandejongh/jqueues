package nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
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
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
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
   * The constructor sets the {@link StartModel} to {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * and inhibits future automatic resets of the wait and serve queue from the event list,
   * since this object will take care of that (and depends on the absence of "independent" resets
   * of the sub-queues).
   * The constructor then sets the auto-revocation policy on the wait queue to {@link AutoRevocationPolicy#UPON_START}
   * and resets this object through {@link #resetEntitySubClassLocal},
   * thus (amongst others) resetting the wait and serve queues
   * and setting the proper initial server-access credits on the wait queue.
   * Finally, it constructs a new {@link MultiSimQueueNotificationProcessor} for both sub-queues,
   * and registers {@link #processSubQueueNotifications} as its processor.
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
   * @see SimEntity#setIgnoreEventListReset
   * @see SimQueue#setAutoRevocationPolicy
   * @see AutoRevocationPolicy#UPON_START
   * @see #resetEntitySubClassLocal
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processSubQueueNotifications
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
      // XXX: The SimQueueSelector is not used??
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
    getWaitQueue ().setIgnoreEventListReset (true);
    getServeQueue ().setIgnoreEventListReset (true);
    getWaitQueue ().setAutoRevocationPolicy (AutoRevocationPolicy.UPON_START);
    resetEntitySubClassLocal ();
    final MultiSimQueueNotificationProcessor<DJ, DQ>  subQueueEventProcessor =
      new MultiSimQueueNotificationProcessor<> (getQueues ());
    subQueueEventProcessor.setProcessor (this::processSubQueueNotifications);
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
  
  /** Calls super method and sets the initial server-access credits on the wait queue.
   * 
   * <p>
   * The super method will reset the wait and serve queues.
   * 
   * @see #resetEntitySubClassLocal
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    resetEntitySubClassLocal ();
  }
  
  /** Resets the local part of this entity.
   * 
   * <p>
   * Used both in the constructor and in {@link #resetEntitySubClass}.
   * 
   * <p>
   * First clears the pending revocation event (administrative; it is never not actually scheduled) for a sub-queue, if applicable,
   * see {@link #removeJobFromQueueUponRevokation} and {@link #rescheduleAfterRevokation}.
   * 
   * <p>
   * Then sets the server-access credits on the wait queue.
   * Sets it to unity if the serve queue has {@code NoWaitArmed == true}, and to zero otherwise.
   * This method silently (and correctly) assumes the availability of server-access credits on the local object.
   * Although very similar to {@link #setServerAccessCreditsOnWaitQueue},
   * we cannot use the latter method because of its explicit sanity check on the current server-access credits value
   * on the wait queue.
   * 
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see SimQueue#setServerAccessCredits
   * @see #getLastUpdateTime
   * @see SimQueue#isNoWaitArmed
   * 
   */
  protected final void resetEntitySubClassLocal ()
  {
    this.pendingDelegateRevocationEvent = null;
    getWaitQueue ().setServerAccessCredits (getLastUpdateTime (), getServeQueue ().isNoWaitArmed () ? 1 : 0);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Determines the {@code noWaitArmed} state of the composite queue.
   * 
   * <p>
   * A {@link BlackCompressedTandem2SimQueue} has {@code noWaitArmed == true}
   * if and only if that is the case on both the wait and the serve queues.
   * Refer to {@link StartModel#COMPRESSED_TANDEM_2_QUEUE} for a detailed explanation for this.
   * 
   * @return The local AND operation of the {@code noWaitArmed} states on both sub-queues.
   * 
   * @see StartModel#COMPRESSED_TANDEM_2_QUEUE
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see SimQueue#isNoWaitArmed
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getWaitQueue ().isNoWaitArmed () && getServeQueue ().isNoWaitArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SET SERVER-ACCESS CREDITS ON WAIT QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Sets the server-access credits on the wait queue, based upon our server-access credits
   *  and the {@link SimQueue#isNoWaitArmed} state on the server queue.
   * 
   * <p>
   * At all times (well, if we have a consistent queue state),
   * the server-access credits on the wait queue should be unity if and only if
   * the local (this) queue {@link #hasServerAcccessCredits}
   * AND the serve queue has {@link SimQueue#isNoWaitArmed}.
   * In all other cases, the server-access credits on the wait queue should be zero.
   * 
   * <p>
   * This method sets the server-access credits on the wait queue appropriately, but only if needed.
   * 
   * <p>
   * Caution is advised for the use of this method.
   * For one, because of its immediate side effects on the wait (and possibly serve) queue,
   * you <i>should not</i> use it from within a sub-queue notification listener
   * (at least, not without taking special measures,
   * as is done in this class through a {@link MultiSimQueueNotificationProcessor}).
   * 
   * <p>
   * This method is (left) protected for documentation (javadoc) purposes.
   * 
   * @throws IllegalStateException If the current server-access credits value on the wait queue is not zero or unity.
   * 
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see #getLastUpdateTime
   * @see #hasServerAcccessCredits
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isNoWaitArmed
   * 
   */
  protected final void setServerAccessCreditsOnWaitQueue ()
  {
    final int oldWaitQueueSac = getWaitQueue ().getServerAccessCredits ();
    if (oldWaitQueueSac < 0 || oldWaitQueueSac > 1)
      throw new IllegalStateException ();
    final int newWaitQueueSac = (hasServerAcccessCredits () && getServeQueue ().isNoWaitArmed ()) ? 1 : 0;
    if (newWaitQueueSac != oldWaitQueueSac)
      getWaitQueue ().setServerAccessCredits (getLastUpdateTime (), newWaitQueueSac);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job, administers it and puts the (real) job into {@link #jobQueue}.
   * 
   * @see #addRealJobLocal
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    addRealJobLocal (job);
  }

  /** Lets the delegate job arrive at the wait queue, after sanity checks.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getWaitQueue
   * @see SimQueue#arrive
   * @see #getDelegateJob
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    getWaitQueue ().arrive (time, getDelegateJob (job));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link BlackCompressedTandem2SimQueue},
   * a (real) job can only be dropped because
   * its delegate job is dropped on one of the sub-queues, see {@link #processSubQueueNotifications},
   * and the semantics of this composite queue require the real job to be dropped as well (this is not a general requirement).
   * The notification callback relies on {@link #drop} to perform the drop.
   * The delegate job has already left the sub-queue system when we are called.
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * 
   * @throws IllegalStateException If the delegate job is still visiting a (any) queue.
   * 
   * @see #drop
   * @see #removeJobsFromQueueLocal
   * @see #rescheduleAfterDrop
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      throw new IllegalStateException ();
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Empty, nothing to do.
   * 
   * @see #removeJobFromQueueUponDrop
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The pending delegate revocation event is a single event used to store the need to revoke a delegate job
   *  on the sub queue it resides on.
   * 
   * <p>
   * It is needed in-between {@link #removeJobFromQueueUponRevokation} and {@link #rescheduleAfterRevokation}.
   * 
   */
  private SimQueueJobRevocationEvent pendingDelegateRevocationEvent = null;

  /** Removes a job if revocation is successful.
   * 
   * <p>
   * In a {@link BlackCompressedTandem2SimQueue}, revocations on real jobs can occur either
   * through external requests, in other words, through {@link #revoke}, or because of auto-revocations
   * on the composite (this) queue through {@link #autoRevoke}.
   * In both cases, the delegate job is still present on one of the sub-queues,
   * and we have to forcibly revoke it.
   * Because we cannot perform the revocation here (we are <i>not</i> allowed to reschedule!),
   * we defer until {@link #removeJobFromQueueUponRevokation} by raising an internal flag
   * (in fact a newly created, though not scheduled {@link SimQueueJobRevocationEvent}).
   * We have to use this method in order to remember the delegate job to be revoked, and on which sub-queue to revoke it,
   * both of which are wiped from the internal administration by {@link #removeJobsFromQueueLocal},
   * which is invoked last.
   * 
   * <p>
   * Note that even though a {@link SimQueueJobRevocationEvent} is used internally to flag the
   * required delegate-job revocation, it is never actually scheduled on the event list!
   * 
   * @throws IllegalStateException If the delegate job is <i>not</i> visiting a sub-queue,
   *                               or if a pending delegate revocation has already been flagged (or been forgotten to clear).
   * 
   * @see #revoke
   * @see #autoRevoke
   * @see SimQueueJobRevocationEvent
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue == null)
      throw new IllegalStateException ();
    if (this.pendingDelegateRevocationEvent != null)
      throw new IllegalStateException ();
    this.pendingDelegateRevocationEvent = new SimQueueJobRevocationEvent (delegateJob, subQueue, time, true);
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Performs the pending revocation on the sub-queue, after clearing it.
   * 
   * @throws IllegalStateException If no pending delegate revocation was found.
   * 
   * @see SimQueueJobRevocationEvent
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (this.pendingDelegateRevocationEvent == null)
      throw new IllegalStateException ();
    final SimQueueJobRevocationEvent event = this.pendingDelegateRevocationEvent;
    this.pendingDelegateRevocationEvent = null;
    // Invoking the event's action equivalent to next line:
    // event.getQueue ().revoke (event.getTime (), event.getJob (), event.isInterruptService ());
    event.getEventAction ().action (event);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** If our (local) server-access credits drop to zero,
   *  sets the server access credits on the wait queue appropriately through invoking {@link #setServerAccessCreditsOnWaitQueue}.
   * 
   * <p>
   * Otherwise, this method does nothing; the case in which we regain server-access credits is dealt with by
   * {@link #rescheduleForNewServerAccessCredits}.
   * 
   * @see #getServerAccessCredits
   * @see #setServerAccessCreditsOnWaitQueue
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    if (getServerAccessCredits () == 0)
      setServerAccessCreditsOnWaitQueue ();
  }
  
  /** Sets the server access credits on the wait queue (if needed) through {@link #setServerAccessCreditsOnWaitQueue}.
   * 
   * @see #setServerAccessCreditsSubClass
   * @see #setServerAccessCreditsOnWaitQueue
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    setServerAccessCreditsOnWaitQueue ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job in the service area (after sanity checks).
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getDelegateJob
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Lets the delegate job arrive at the serve queue, after sanity checks.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getServeQueue
   * @see SimQueue#arrive
   * @see #getDelegateJob
   * @see #insertJobInQueueUponStart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || (! this.jobsInServiceArea.contains (job)))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
    getServeQueue ().arrive (time, delegateJob);
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link BlackCompressedTandem2SimQueue},
   * a (real) job can only depart when its delegate job departs from the serve queue.
   * The notification callback from the serve queue relies on {@link #depart} to perform the departure.
   * The delegate job has already left the sub-queue system when we are called.
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * 
   * @throws IllegalStateException If the delegate job is still visiting a (any) queue.
   * 
   * @see #depart
   * @see #removeJobsFromQueueLocal
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    final DJ delegateJob = getDelegateJob (departingJob);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      throw new IllegalStateException ();
    removeJobsFromQueueLocal (departingJob, delegateJob);
  }

  /** Empty, nothing to do.
   * 
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESS SUB-QUEUE NOTIFICATIONS (THROUGH NOTIFICATION PROCESSOR); ALL EXCEPT UPDATE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queues, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} notifications from both sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for both sub-queues) created upon construction,
   * see {@link MultiSimQueueNotificationProcessor.Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * This method takes one notification at a time, starting at the head of the list, removes it
   * and processes it either with {@link #processWaitQueueNotification}
   * or {@link #processServeQueueNotification}.
   * While processing, new notifications may be added to the list; the list is processed until it is empty.
   * 
   * <p>
   * It finally invokes {@link #triggerPotentialNewNoWaitArmed} to make sure we did not miss
   * an "isolated" {@link SimQueue#isNoWaitArmed} notification from one of the sub-queues
   * that requires a change in our own {@link SimQueue#isNoWaitArmed} state,
   * because this is not explicitly dealt with in auxiliary processor methods.
   * (Note that after invocation of this method, no new (sub-queue) notifications are expected,
   * at the expense of a {@link IllegalStateException}.)
   * 
   * <p>
   * Note that this {@link BlackCompressedTandem2SimQueue} still catches {@link SimEntityListener#notifyUpdate}
   * notifications in the main class body (all other notification types are dealt with through the
   * {@link MultiSimQueueNotificationProcessor}.
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty, or contains a notification from another queue
   *                                  than the two sub-queues (i.e., the wait- and serve-queues),
   *                                  or if other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor.Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processWaitQueueNotification
   * @see #processServeQueueNotification
   * @see #update
   * 
   */
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ();
    while (! notifications.isEmpty ())
    {
      //System.err.println ("-> Notifications: [t=" + getLastUpdateTime () + "]:");
      //  for (MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification : notifications)
      //    System.err.println ("   " + notification + ".");
      final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification = notifications.remove (0);
      final DQ queue = notification.getQueue ();
      if (queue == getWaitQueue ())
        processWaitQueueNotification (notification);
      else if (queue == getServeQueue ())
        processServeQueueNotification (notification);        
      else
        throw new IllegalArgumentException ();
    }
    triggerPotentialNewNoWaitArmed (getLastUpdateTime ());
    if (! notifications.isEmpty ())
      throw new IllegalStateException ();
  }
  
  /** Performs sanity checks on a notification from a sub-queue (irrespective of which one).
   * 
   * @param notification The notification.
   * 
   * @throws IllegalArgumentException If the time of the notification differs from our last update time,
   *                                  or if a {@link SimEntitySimpleEventType#RESET} notification was found in combination
   *                                  with other sub-notifications (i.e., as part of an atomic notification that includes
   *                                  other sub-notifications next to the reset).
   * 
   */
  protected final void sanitySubQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    if (notification.getTime () != getLastUpdateTime ())
      throw new IllegalArgumentException ("on " + this + ": notification time [" + notification.getTime ()
      + "] != last update time [" + getLastUpdateTime () + "], subnotifications: "
      + notification.getSubNotifications () + ".");
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
      {
        if (notification.getSubNotifications ().size () > 1)
          throw new IllegalStateException ();
      }
    }
  }
  
  /** Performs sanity checks on a notification from the wait queue.
   * 
   * <p>
   * A full description of the sanity checks would make this {@code javadoc} become uninterestingly large.
   * Please refer to the documentation in the source code.
   * 
   * @param notification The notification.
   * 
   * @throws IllegalArgumentException If the sanity checks fail.
   * 
   * @see #sanitySubQueueNotification
   * 
   */
  protected final void sanityWaitQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    sanitySubQueueNotification (notification);
    boolean foundStart = false;
    DJ jobStarted = null;
    boolean foundAutoRevocation = false;
    DJ jobAutoRevoked = null;
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
        ;
      else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        // The real job must exist (but may have already left).
        getRealJob (job);
      else if (notificationType == SimQueueSimpleEventType.QAV_START
            || notificationType == SimQueueSimpleEventType.QAV_END)
        // Queue-Access Vacations are forbidden on both wait and serve queues.
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.NWA_FALSE
            || notificationType == SimQueueSimpleEventType.NWA_TRUE)
        ;
      else if (notificationType == SimQueueSimpleEventType.OUT_OF_SAC
           || notificationType == SimQueueSimpleEventType.REGAINED_SAC)
        ;
      else if (notificationType == SimQueueSimpleEventType.START)
      {
        // Only a single START sub-notification is allowed in the atomic-event notification.
        if (foundStart)
          throw new IllegalStateException ();
        // The real job must exist (and have left already due to auto-revocation, but this is checked with AUTO_REVOCATION).
        getRealJob (job);
        foundStart = true;
        jobStarted = job;
      }
      else if (notificationType == SimEntitySimpleEventType.DROP)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.REVOCATION)
        // Do NOT check for the real job here; revocations (but NOT auto-revocations) are always caused by the composite queue,
        // hence the real job has already left!
        ;
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
      {
        // Only a single AUTO_REVOCATION sub-notification is allowed in the atomic-event notification.
        if (foundAutoRevocation)
          throw new IllegalStateException ();
        // An AUTO_REVOCATION sub-notification must always follow a START sub-notification in a single  atomic-event notification.
        if (! foundStart)
          throw new IllegalStateException ();
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
        foundAutoRevocation = true;
        jobAutoRevoked = job;
      }
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
    }
    if (foundStart != foundAutoRevocation || jobStarted != jobAutoRevoked)
      // START and AUTO_REVOCATION have to come in pairs, and apply to the same job.
      throw new IllegalStateException ();
  }
  
  /** Processes a notification from the wait queue.
   * 
   * <p>
   * This method iterates over the sub-notifications (from the wait queue) and:
   * <ul>
   * <li>Invokes {@link #drop} for the applicable real job upon a {@link SimEntitySimpleEventType#DROP};
   * <li>Invokes {@link #start} for the applicable real job upon a {@link SimEntitySimpleEventType#AUTO_REVOCATION};
   * <li>Invokes {@link #depart} for the applicable real job upon a {@link SimEntitySimpleEventType#DEPARTURE};
   *     <i>after</i> verifying that the real job has not already started (throwing an {@link IllegalStateException} if it did);
   * <li>Ignores all other sub-notification types (apart from sanity checks).
   * </ul>
   * 
   * <p>
   * Note that we can ignore {@link SimEntitySimpleEventType#RESET}, apart from sanity checks,
   * because autonomous resets on sub-queues are not allowed,
   * and we have inhibited resets from the event list on the sub-queues through {@link SimQueue#setIgnoreEventListReset}.
   * Hence, a reset can only be caused by a reset on the composite queue,
   * and it will soon set the server-access credits on the wait queue,
   * as well as its own NoWaitArmed state.
   * 
   * @param notification The notification.
   * 
   * @see #sanityWaitQueueNotification
   * @see SimEntitySimpleEventType#RESET
   * @see SimQueue#setIgnoreEventListReset
   * @see SimEntitySimpleEventType#DROP
   * @see SimEntitySimpleEventType#AUTO_REVOCATION
   * @see SimEntitySimpleEventType#DEPARTURE
   * @see #drop
   * @see #start
   * @see #depart
   * 
   */
  protected final void processWaitQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    sanityWaitQueueNotification (notification);
    final DQ waitQueue = getWaitQueue ();
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
        // There is nothing to do; autonomous RESETs on sub-queues are not allowed,
        // and we have inhibited RESETs from the event list on the sub-queues.
        // Hence, a RESET can only be caused by a RESET on the composite queue,
        // and it will soon set the server-access credits on the wait queue,
        // as well as its own NoWaitArmed state.
        ;
      else if (notificationType == SimEntitySimpleEventType.DROP)
        drop (getRealJob (job, null), getLastUpdateTime ());
      else if (notificationType == SimEntitySimpleEventType.REVOCATION)
        ;
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        start (getLastUpdateTime (), getRealJob (job, null));
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
      {
        final J realJob = getRealJob (job, null);
        if (! this.jobsInServiceArea.contains (realJob))
          // Job departed from the waiting area of the wait queue.
          // This corresponds to a direct departure from the waiting area of the wait queue, which is perfectly legal.
          // Real job must depart as well (before its start).
          depart (getLastUpdateTime (), realJob);
        else
          // Job departed from the service area of the wait queue.
          // This means auto-revocation upon start failed.
          throw new IllegalStateException ();
      }
    }
  }
  
  /** Performs sanity checks on a notification from the serve queue.
   * 
   * <p>
   * A full description of the sanity checks would make this {@code javadoc} become uninterestingly large.
   * Please refer to the documentation in the source code.
   * 
   * @param notification The notification.
   * 
   * @throws IllegalArgumentException If the sanity checks fail.
   * 
   * @see #sanitySubQueueNotification
   * 
   */
  protected final void sanityServeQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    sanitySubQueueNotification (notification);
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
        ;
      else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        // The real job must exist (but may have already left).
        getRealJob (job);
      else if (notificationType == SimQueueSimpleEventType.QAV_START
            || notificationType == SimQueueSimpleEventType.QAV_END)
        // Queue-Access Vacations are forbidden on both wait and serve queues.
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.NWA_FALSE
            || notificationType == SimQueueSimpleEventType.NWA_TRUE)
        ;
      else if (notificationType == SimQueueSimpleEventType.OUT_OF_SAC
            || notificationType == SimQueueSimpleEventType.REGAINED_SAC)
        // Server-Access Credits events should never occur on the serve queue;
        // it should always have infinite server-access credits.
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.START)
        // The real job must exist (but may have already left).
        getRealJob (job);
      else if (notificationType == SimEntitySimpleEventType.DROP)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.REVOCATION)
        // Do NOT check for the real job here; revocations (but NOT auto-revocations) are always caused by the composite queue,
        // hence the real job has already left!
        ;
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
    }
  }
  
  /** Processes a notification from the serve queue.
   * 
   * <p>
   * This method iterates over the sub-notifications (from the serve queue) and:
   * <ul>
   * <li>Invokes {@link #drop} for the applicable real job upon a {@link SimEntitySimpleEventType#DROP};
   * <li>Invokes {@link #autoRevoke} for the applicable real job upon a {@link SimEntitySimpleEventType#AUTO_REVOCATION};
   * <li>Invokes {@link #depart} for the applicable real job upon a {@link SimEntitySimpleEventType#DEPARTURE};
   * <li>Ignores all other sub-notification types (apart from sanity checks).
   * </ul>
   * 
   * <p>
   * Unless having processed a (single) {@link SimEntitySimpleEventType#RESET},
   * it then invokes {@link #setServerAccessCreditsOnWaitQueue},
   * as it might require changing (due to a flip in {@link SimQueue#isNoWaitArmed} on the serve queue).
   * 
   * <p>
   * Note that we can ignore {@link SimEntitySimpleEventType#RESET}, apart from sanity checks,
   * because autonomous resets on sub-queues are not allowed,
   * and we have inhibited resets from the event list on the sub-queues through {@link SimQueue#setIgnoreEventListReset}.
   * Hence, a reset can only be caused by a reset on the composite queue,
   * and it will soon set the server-access credits on the wait queue,
   * as well as its own NoWaitArmed state.
   * 
   * @param notification The notification.
   * 
   * @see #sanityServeQueueNotification
   * @see SimEntitySimpleEventType#RESET
   * @see SimQueue#setIgnoreEventListReset
   * @see SimEntitySimpleEventType#DROP
   * @see SimEntitySimpleEventType#AUTO_REVOCATION
   * @see SimEntitySimpleEventType#DEPARTURE
   * @see #drop
   * @see #autoRevoke
   * @see #depart
   * @see #setServerAccessCreditsOnWaitQueue
   * 
   */
  protected final void processServeQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    sanityServeQueueNotification (notification);
    final DQ serveQueue = getServeQueue ();
    boolean mustSetSacOnWaitQueue = true;
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
        // There is nothing to do; autonomous RESETs on sub-queues are not allowed,
        // and we have inhibited RESETs from the event list on the sub-queues.
        // Hence, a RESET can only be caused by a RESET on the composite queue,
        // and it will soon set the server-access credits on the wait queue,
        // as well as its own NoWaitArmed state.
        mustSetSacOnWaitQueue = false;
      else if (notificationType == SimEntitySimpleEventType.DROP)
        drop (getRealJob (job, null), getLastUpdateTime ());
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        autoRevoke (getLastUpdateTime (), getRealJob (job, serveQueue));
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        depart (getLastUpdateTime (), getRealJob (job, null));
    }
    // Do not attempt to set SACs on the wait queue if we are a RESET event (and SACs have been set already above).
    if (mustSetSacOnWaitQueue)
      setServerAccessCreditsOnWaitQueue ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE UPDATE NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls {@link #update} in order to update our own time in response to an increase in time on one of the sub-queues.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * @throws IllegalStateException    If time is in the past.
   * 
   * @see #getQueues
   * @see #update
   * @see #getLastUpdateTime
   * 
   */
  @Override
  public final void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    update (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE NOTIFICATIONS OTHER THAN UPDATE (ALL EMPTY; SEE SUB-QUEUE NOTIFICATION PROCESSOR)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, DJ>> notifications)
  {
  }
  
  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyArrival (final double time, final DJ job, final DQ queue)
  {
  }
  
  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStartQueueAccessVacation (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStopQueueAccessVacation (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStart (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyDrop (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyRevocation (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final DJ job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyDeparture (final double time, final DJ job, final DQ queue)
  {
  }

}
