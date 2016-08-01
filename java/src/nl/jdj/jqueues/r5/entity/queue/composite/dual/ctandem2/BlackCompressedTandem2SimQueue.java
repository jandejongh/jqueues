package nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
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
   * and the auto-revocation policy on the wait queue to {@link AutoRevocationPolicy#UPON_START}.
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
   * @see SimQueue#setAutoRevocationPolicy
   * @see AutoRevocationPolicy#UPON_START
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
    getWaitQueue ().setAutoRevocationPolicy (AutoRevocationPolicy.UPON_START);
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
    // Asses the initial server-access credits on the waitQueue.
    // No need to check our local SACs; by contract of SimQueue, SACs should be infinite after reset.
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
   * @see #getStartModel
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see StartModel#COMPRESSED_TANDEM_2_QUEUE
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getWaitQueue ().isNoWaitArmed () && getServeQueue ().isNoWaitArmed ();
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
   * A delegate job is dropped on one of the sub-queues, see {@link #notifyDrop},
   * and the semantics of the composite queue require the real job to be dropped as well (this is not a requirement).
   * The notification callback relies on {@link #drop} to perform the drop.
   * The delegate job has already left the sub-queue system when we are called.
   * <li>
   * The composite queue <i>itself</i> decides to drop a (real) job, see {@link #drop}.
   * In this case we are called while the delegate job is still present on one of the sub-queues,
   * or it resides in our (local) waiting area.
   * In any way, it has to be removed.
   * (Note that we cannot forcibly drop it!)
   * </ul>
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobFromQueueUponExit}.
   * 
   * @see #drop
   * @see #notifyDrop
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponExit (job, time);
  }

  /** Empty, nothing to do.
   * 
   * <p>
   * A real job has been dropped from the composite queue, see {@link #removeJobFromQueueUponDrop} for the potential reasons.
   * We immediately realize that dropping a delegate job from the
   * wait queue (the first queue) does not require any rescheduling: the delegate job was waiting at the first queue,
   * so it was not eligible for access to the server queue, and the mere fact of dropping it can never result in another
   * job at the waiting queue getting access to the server, or result in a state change at the server queue (the second queue).
   * Dropping (or revoking) a (delegate) job from the server queue can certainly affect its {@link #isNoWaitArmed} state,
   * but this is reported to and handled by {@link #notifyNewNoWaitArmed}.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    // EMPTY
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
   * @see #notifyRevocation
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    removeJobFromQueueUponExit (job, time);
  }

  /** Empty, nothing to do.
   * 
   * @see #rescheduleAfterDrop For an explanation as to why this method can be left empty. 
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    // EMPTY
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }
  
  /** Sets the server access credits on the wait queue to unity if the serve queue is in {@code noWaitArmed} state.
   * 
   * @see #getServeQueue
   * @see SimQueue#isNoWaitArmed
   * @see #getWaitQueue
   * @see SimQueue#setServerAccessCredits
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    if (getServeQueue ().isNoWaitArmed ())
      getWaitQueue ().setServerAccessCredits (time, 1);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job in the service area (after sanity checks).
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    getDelegateJob (job); // Sanity on existance of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Nothing to do apart from sanity check.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
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
    getDelegateJob (job); // Sanity on existance of delegate job.
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
   * A delegate job departs on the serve queue, see {@link #notifyDeparture},
   * and the real job must depart as well.
   * The notification callback relies on {@link #depart} to perform the departure.
   * The delegate job has already left the sub-queue system when we are called.
   * <li>
   * The composite queue <i>itself</i> decides that the (real) job is to depart, see {@link #depart}.
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
   * @see #notifyDeparture
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponExit (departingJob, time);
  }

  /** Empty, nothing to do.
   * 
   * @see #rescheduleAfterDrop For an explanation as to why this method can be left empty. 
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    // EMPTY
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
   * This done through (unconditional) revocation.
   * 
   * <p>
   * The real and delegate jobs are removed from the internal administration through {@link #removeJobsFromQueueLocal}.
   * 
   * @param job  The job that exists, non-{@code null}.
   * @param time The current time.
   * 
   * @see #getDelegateJob
   * @see #revoke
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
  // SUB-QUEUE RESET NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing; assumes will have been reset or will soon be reset as well.
   * 
   * <p>
   * The reset of a sub-queue can only be the result of this queue being resetting itself
   * (and as a result, resetting its sub-queues),
   * or because the event-list is being reset,
   * in which case we will be reset ourselves soon, or have reset ourselves and our sub-queues already.
   * 
   * <p>
   * In both case, no response is required upon receiving this notification.
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE UPDATE / STATE CHANGED NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls {@link #update}.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * 
   */
  @Override
  public final void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    update (time);
  }
  
  /** Does nothing.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * 
   */
  @Override
  public final void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, DJ>> notifications)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE ARRIVAL NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Nothing to do apart from sanity check.
   * 
   * @see #getRealJob
   * 
   */
  @Override
  public final void notifyArrival (final double time, final DJ job, final DQ queue)
  {
    getRealJob (job, queue); // Sanity on existance of real job.
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE QUEUE-ACCESS-VACATION NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Because sub-queue-access vacations are not allowed.
   * 
   */
  @Override
  public final void notifyStartQueueAccessVacation (final double time, final DQ queue)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Because sub-queue-access vacations are not allowed.
   * 
   */
  @Override
  public final void notifyStopQueueAccessVacation (final double time, final DQ queue)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE NO-WAIT-ARMED NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Sets the server-access credits on the wait queue if the notification came from the serve queue.
   * 
   * <p>
   * If the notification came from the serve queue, we <i>always</i> set the server-access credits on the wait queue.
   * The conditions for setting it to unity are that the serve queue must report {@code noWaitArmed == true},
   * and the <i>local</i> {@link #hasServerAcccessCredits} be {@code true}.
   * In all other cases, it is set to zero.
   * 
   * <p>
   * Note that setting the server-access credits on the wait queue is done through {@link SimQueue#doAfterNotifications}.
   * 
   * @see #getServeQueue
   * @see #getWaitQueue
   * @see SimQueue#doAfterNotifications
   * @see SimQueue#setServerAccessCredits
   * @see #hasServerAcccessCredits
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    if (queue == getServeQueue ())
      getWaitQueue ().doAfterNotifications (() ->
      {
        getWaitQueue ().setServerAccessCredits (time, (hasServerAcccessCredits () && noWaitArmed) ? 1 : 0);
      });
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE SERVER-ACCESS-CREDITS NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing, apart from sanity checks.
   * 
   * <p>
   * With {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, this composite queue is under full control
   * of the server-access credits on the wait queue, and the server-access credits on the serve queue are always infinite.
   * 
   * @see #getServeQueue
   * 
   * @throws IllegalStateException If the notification came from the serve queue.
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
    if (queue == getServeQueue ())
      throw new IllegalStateException ();
  }

  /** Does nothing, apart from sanity checks.
   * 
   * <p>
   * With {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, this composite queue is under full control
   * of the server-access credits on the wait queue, and the server-access credits on the serve queue are always infinite.
   * 
   * @see #getServeQueue
   * 
   * @throws IllegalStateException If the notification came from the serve queue.
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
    if (queue == getServeQueue ())
      throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE START NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** If the notification came from the wait queue, starts the corresponding real job
   *  (which amounts only to local administration update and listener notification).
   * 
   * @see #getRealJob
   * @see #getWaitQueue
   * @see #start
   * 
   */
  @Override
  public final void notifyStart (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    if (queue == getWaitQueue ())
    {
      if (this.jobsInServiceArea.contains (realJob))
        throw new IllegalStateException ();
      start (time, realJob);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE DROP NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the corresponding real job, or sends the delegate job to the drop-destination sub-queue (if available).
   * 
   * <p>
   * Starts the dropped job on a valid non-<code>null</code> result from {@link #getDropDestinationQueue};
   * otherwise it gets the real job, sets its queue to <code>null</code> and drops it as well.
   * 
   * @see #getDropDestinationQueue
   * @see SimQueue#doAfterNotifications
   * @see #arrive
   * @see #drop
   * 
   */
  @Override
  public final void notifyDrop (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    final DQ dropDestinationQueue = getDropDestinationQueue ();
    if (dropDestinationQueue != null)
    {
      if (! getQueues ().contains (dropDestinationQueue))
        throw new RuntimeException ();
      dropDestinationQueue.doAfterNotifications (() ->
      {
        dropDestinationQueue.arrive (time, job);
      });
    }
    else
      drop (realJob, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE REVOCATION NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Nothing to do apart from sanity check.
   * 
   * @see #getRealJob
   * 
   */
  @Override
  public final void notifyRevocation (final double time, final DJ job, final DQ queue)
  {
    getRealJob (job, queue); // Sanity on existance of real job.
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE AUTO-REVOCATION NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** If the notification came from the wait queue, lets the delegate job arrive at the serve queue
   *  and updates the server-access credits on the wait queue after that;
   *  if it came from the serve queue, auto-revokes the real job.
   * 
   * <p>
   * In case the notification came from the wait queue, it schedules the arrival of the delegate job at the serve
   * queue through {@link SimQueue#doAfterNotifications}. It also schedules at the serve queue,
   * after the arrival of the delegate job, the update of the server-access credits on the wait queue:
   * If we have local server-access credits and {@link #isNoWaitArmed} {@code == true},
   * it sets the server-access credits on the wait queue to unity, again through {@link SimQueue#doAfterNotifications},
   * yet this time scheduled on the wait queue.
   * 
   * <p>
   * If the notification came from the serve queue, it invokes {@link #autoRevoke},
   * allowing auto-revocations on the serve queue.
   * 
   * @see #getRealJob
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see SimQueue#doAfterNotifications
   * @see SimQueue#arrive
   * @see #hasServerAcccessCredits
   * @see #isNoWaitArmed
   * 
   * @throws IllegalArgumentException If the notification came from wait nor serve queues.
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    if (queue == getWaitQueue ())
    {
      // Sanity check...
      if (! this.jobsInServiceArea.contains (realJob))
        throw new IllegalStateException ();
      // Schedule the arrival at the serve queue.
      getServeQueue ().doAfterNotifications (() ->
      {
        getServeQueue ().arrive (time, job);
        if (hasServerAcccessCredits ()
        &&  isNoWaitArmed ())
          getWaitQueue ().doAfterNotifications (() ->
          {
            getWaitQueue ().setServerAccessCredits (time, 1);
          });
      });
    }
    else if (queue == getServeQueue ())
      autoRevoke (time, realJob);
    else
      throw new IllegalArgumentException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE DEPARTURE NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departs the real job.
   * 
   * <p>
   * If the notification came from the wait queue, this method departs the real job if it has not yet been taken into service
   * (i.e., it is not yet in the service area).
   * This is quite legal, and corresponds to the fact that the wait queue allows job departures directly from the waiting area.
   * If, however, the job has already been taken into service, we throw an exception because the delegate job should have
   * been auto-revoked.
   * 
   * <p>
   * If the notification came from the serve queue, this method departs the real job.
   * 
   * @throws IllegalArgumentException If the notification came from wait nor serve queues.
   * 
   * @see #getRealJob
   * @see #getWaitQueue
   * @see #getServeQueue
   * @see #getJobsInServiceArea
   * @see #depart
   * 
   */
  @Override
  public final void notifyDeparture (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    if (queue == getWaitQueue ())
    {
      if (! this.jobsInServiceArea.contains (realJob))
        // Job departed from the waiting area of the wait queue.
        // This corresponds to a direct departure from the waiting area of the wait queue, which is perfectly legal.
        // Real job must depart as well (before its start).
        depart (time, realJob);
      else
        // Job departed from the service area of the wait queue.
        // This means auto-revocation upon start failed.
        throw new IllegalStateException ();
    }
    else if (queue == getServeQueue ())
      // Job departed from the serve queue.
      // Let the real job depart as well.
      depart (time, realJob);
    else
      // Job departed from unknown queue...
      throw new RuntimeException ();
  }

}
