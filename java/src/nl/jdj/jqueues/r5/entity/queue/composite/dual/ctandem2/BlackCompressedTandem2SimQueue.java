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
   * <p>
   * The constructor sets the {@link StartModel} to {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * and inhibits future automatic resets of the wait and serve queue from the event list,
   * since this object will take care of that (and depends on the absence of "independent" resets
   * of the sub-queues).
   * The constructor then sets the auto-revocation policy on the wait queue to {@link AutoRevocationPolicy#UPON_START}
   * and resets this object through {@link #resetEntitySubClassLocal},
   * thus resetting the wait and serve queues
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
   * Note that we actually rely on the wait queue being reset before the serve queue!
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
   * Sets the server-access credits on the wait queue.
   * Sets it to unity if the serve queue has {@code NoWaitArmed == true}, and to zero otherwise.
   * This method silently assumes the availability of server-access credits on the local object.
   * Although very similar to {@link #setServerAccessCreditsOnWaitQueue},
   * we cannot use the latter method because of its explicit sanity check on the current server-access credits value
   * on the wait queue.
   * 
   * <p>
   * Note that this method (and the notification processor)
   * assumes the following reset order: this object, then the wait queue and then the serve queue.
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
   * you <i>should not</i> use it from within a sub-queue notification listener.
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
   * a (real) job can only be dropped because of one of the following reasons:
   * <ul>
   * <li>
   * A delegate job is dropped on one of the sub-queues, see {@link #processSubQueueNotifications},
   * and the semantics of the composite queue require the real job to be dropped as well (this is not a requirement).
   * The notification callback relies on {@link #drop} to perform the drop.
   * The delegate job has already left the sub-queue system when we are called.
   * <li>
   * The composite queue <i>itself</i> decides to drop a (real) job, see {@link #drop}
   * and {@link AutoRevocationPolicy#UPON_START}.
   * In this case we are called while the delegate job is still present on one of the sub-queues,
   * or it resides in our (local) waiting area.
   * In any way, it has to be removed.
   * (Note that we cannot forcibly drop it from the sub-queue!)
   * </ul>
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobFromQueueUponExit}.
   * 
   * @see #drop
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    // XXX This actually reschedules...
    removeJobFromQueueUponExit (job, time);
  }

  /** Empty, nothing to do.
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

  /** Removes a job if revocation is successful.
   * 
   * <p>
   * In a {@link BlackCompressedTandem2SimQueue}, revocations on real jobs can only be the result of external requests,
   * in other words, through {@link #revoke}, not because of events on delegate jobs
   * (unlike <i>auto</i>-revocations).
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobFromQueueUponExit}.
   * 
   * @see #revoke
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    // XXX This actually reschedules...
    removeJobFromQueueUponExit (job, time);
  }

  /** Empty, nothing to do.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** If our (local) server-access credits drop to zero,
   *  sets the server access credits on the wait queue appropriately through invoking {@link #setServerAccessCreditsOnWaitQueue}.
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
   * a (real) job can only depart because of one of the following reasons:
   * <ul>
   * <li>
   * A delegate job departs on the serve queue, see {@link #processSubQueueNotifications},
   * and the real job must depart as well.
   * The notification callback relies on {@link #depart} to perform the departure.
   * The delegate job has already left the sub-queue system when we are called.
   * <li>
   * The composite queue <i>itself</i> decides that the (real) job is to depart, see {@link #depart}.
   * (This is actually hypothetical at the current time for {@link BlackCompressedTandem2SimQueue}!)
   * In this case we are called while the delegate job is still present on one of the sub-queues,
   * or it resides in our (local) waiting area.
   * In any way, it has to be removed.
   * (Note that we cannot forcibly depart it!)
   * </ul>
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobFromQueueUponExit}.
   * 
   * @see #depart
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    // XXX This actually reschedules...
    removeJobFromQueueUponExit (departingJob, time);
  }

  /** Empty, nothing to do.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXIT (DROP/REVOCATION/DEPARTURE)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Performs the exit of a real job: Revokes the delegate job if it is still present on a sub-queue,
   *  and removes the real and delegate jobs from the internal administration.
   * 
   * <p>
   * In case where the delegate job is still present on a sub-queue, we have to remove it from there.
   * This done through (unconditional) revocation of the delegate job on the sub-queue.
   * 
   * <p>
   * The real and delegate jobs are removed from the internal administration through {@link #removeJobsFromQueueLocal}.
   * 
   * @param job  The job that exists, non-{@code null}.
   * @param time The current time.
   * 
   * @see #getDelegateJob
   * @see SimQueue#revoke
   * @see #removeJobsFromQueueLocal
   * 
   * @throws IllegalStateException If the delegate jobs reports its visiting a queue, but that queue does not agree.
   * @throws RuntimeException      If sanity checks after revocation of the delegate job fail (somehow still present on sub-queue,
   *                               or job still reporting a visit to any queue).
   * 
   */
  protected final void removeJobFromQueueUponExit (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
    {
      // Santity check...
      if (! subQueue.getJobs ().contains (delegateJob))
        throw new IllegalStateException ();
      subQueue.revoke (time, delegateJob);
      // More santity checks...
      if (subQueue.getJobs ().contains (delegateJob)
        || subQueue.getJobsInWaitingArea ().contains (delegateJob)
        || subQueue.getJobsInServiceArea ().contains (delegateJob)
        || delegateJob.getQueue () != null)
        throw new RuntimeException ();
    }
    removeJobsFromQueueLocal (job, delegateJob);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESS SUB-QUEUE NOTIFICATIONS (THROUGH NOTIFICATION PROCESSOR); ALL EXCEPT UPDATE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queues, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} events from both sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for both sub-queues) created upon construction,
   * see {@link MultiSimQueueNotificationProcessor.Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * This method takes one notification at a time, starting at the head of the list, remove it
   * and process it either with {@link #processWaitQueueNotification}
   * or {@link #processServeQueueNotification}.
   * While processing, new events may be added to the list; the list is processed until it is empty.
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
    final DQ waitQueue = getWaitQueue ();
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
        getRealJob (job);
      else if (notificationType == SimQueueSimpleEventType.QAV_START
            || notificationType == SimQueueSimpleEventType.QAV_END)
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.NWA_FALSE
            || notificationType == SimQueueSimpleEventType.NWA_TRUE)
        ;
      else if (notificationType == SimQueueSimpleEventType.OUT_OF_SAC
           || notificationType == SimQueueSimpleEventType.REGAINED_SAC)
        ;
      else if (notificationType == SimQueueSimpleEventType.START)
      {
        if (foundStart)
          throw new IllegalStateException ();
        getRealJob (job);
        foundStart = true;
        jobStarted = job;
      }
      else if (notificationType == SimEntitySimpleEventType.DROP)
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.REVOCATION)
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
      {
        if (foundAutoRevocation)
          throw new IllegalStateException ();
        getRealJob (job, null);
        foundAutoRevocation = true;
        jobAutoRevoked = job;
      }
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        getRealJob (job, null);
    }
    if (foundStart != foundAutoRevocation || jobStarted != jobAutoRevoked)
      throw new IllegalStateException ();
  }
  
  /** Processes a notification from the wait queue.
   * 
   * @param notification The notification.
   * 
   * @see #sanityWaitQueueNotification
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
      {
        // Special treatment of reset notification, because of tricky update-time semantics.
        // For instance, we cannot operate on a SimEntity 'in the past'.
        // Note that we rely on this object being reset before its sub-queues
        // AND on the wait queue being reset before the serve queue!
        final DQ serveQueue = getServeQueue ();
        waitQueue.setServerAccessCredits (waitQueue.getLastUpdateTime (), serveQueue.isNoWaitArmed () ? 1 : 0);
      }
      else if (notificationType == SimEntitySimpleEventType.DROP)
        // XXX DropCollectorQueue??
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
    final DQ serveQueue = getServeQueue ();
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
        ;
      else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        getRealJob (job);
      else if (notificationType == SimQueueSimpleEventType.QAV_START
            || notificationType == SimQueueSimpleEventType.QAV_END)
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.NWA_FALSE
            || notificationType == SimQueueSimpleEventType.NWA_TRUE)
        ;
      else if (notificationType == SimQueueSimpleEventType.OUT_OF_SAC
            || notificationType == SimQueueSimpleEventType.REGAINED_SAC)
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.START)
        ;
      else if (notificationType == SimEntitySimpleEventType.DROP)
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.REVOCATION)
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        getRealJob (job, null);
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        getRealJob (job, null);
    }
  }
  
  /** Processes a notification from the wait queue.
   * 
   * @param notification The notification.
   * 
   * @see #sanityServeQueueNotification
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
      {
        mustSetSacOnWaitQueue = false;
        // Special treatment of reset notification, because of tricky update-time semantics.
        // For instance, we cannot operate on a SimEntity 'in the past'.
        // Note that we rely on this object being reset before its sub-queues
        // AND on the wait queue being reset before the serve queue!
        final DQ waitQueue = getWaitQueue ();
        waitQueue.setServerAccessCredits (waitQueue.getLastUpdateTime (), serveQueue.isNoWaitArmed () ? 1 : 0);
      }
      else if (notificationType == SimEntitySimpleEventType.DROP)
        // XXX DropCollectorQueue??
        drop (getRealJob (job, null), getLastUpdateTime ());
      else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        autoRevoke (getLastUpdateTime (), getRealJob (job, serveQueue));
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        depart (getLastUpdateTime (), getRealJob (job, null));
      // XXX I think we still need special treatment of a stand-alone NoWaitArmed change in the server queue,
      // because we have to report changes to our own NoWaitArmed state (even if nothing else happens...).
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
   * @throws IllegalStateException    If time is in the past (and updates are not inhibited).
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
