package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link BlackSimQueueComposite} restricted to {@link StartModel#LOCAL}.
 *
 * <p>
 * Implementations (well, most of them) only have to route a job (actually, its delegate job) through the
 * internal network of {@link SimQueue}s,
 * see {@link #selectFirstQueue} and {@link #selectNextQueue}.
 * 
 * <p>
 * This allows for many types of queueing networks, including "feedback"-type networks.
 * 
 * <p>
 * For details about the semantics of the waiting and service areas of a black composite queue,
 * see {@link BlackSimQueueComposite.StartModel}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public abstract class AbstractBlackSimQueueComposite_LocalStartModel
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackSimQueueComposite_LocalStartModel>
extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
implements BlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract black network of queues.
   * 
   * @param eventList             The event list to be shared between this queue and the inner queues.
   * @param queues                A set holding the "inner" queues.
   * @param simQueueSelector      The object for routing jobs through the network of embedded queues;
   *                                if {@code null}, no sub-queues will be visited.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    or the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  protected AbstractBlackSimQueueComposite_LocalStartModel
  (final SimEventList eventList,
    final Set<DQ> queues,
    final SimQueueSelector simQueueSelector,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, simQueueSelector, delegateSimJobFactory);
    setStartModel (StartModel.LOCAL);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code true}.
   * 
   * @return {@code true}, the {@code noWaitArmed} state of the composite queue.
   * 
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return true;
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

  /** Starts the arrived job if server-access credits are available.
   * 
   * @see #hasServerAcccessCredits
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link AbstractBlackSimQueueComposite_LocalStartModel},
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
   * There is nothing to do here since dropping a real job and its corresponding delegate job
   * does not affect the access to the sub-queues, which is only determined by our (local) server-access credits.
   * And these are not affected by drops.
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
   * In an {@link AbstractSimQueueComposite}, revocations on real jobs can only be the result of external requests,
   * in other words, through {@link #revoke}, not because of events on delegate jobs.
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
  
  /** Starts jobs as long as there are server-access credits and jobs waiting.
   * 
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, getFirstJobInWaitingArea ());
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
    getDelegateJob (job); // Sanity on existence of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Lets the delegate job arrive at its first queue, or make it depart immediately if no such queue is provided.
   * 
   * @see #selectFirstQueue
   * @see #arrive
   * @see #depart
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
    final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
    if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
      throw new IllegalArgumentException ();
    if (firstQueue != null)
      firstQueue.arrive (time, delegateJob);
    else
      // We do not get a queue to arrive at.
      // So we depart; without having been executed!
      depart (time, job);
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link AbstractBlackSimQueueComposite_LocalStartModel},
   * a (real) job can only depart because of one of the following reasons:
   * <ul>
   * <li>
   * A delegate job departs on one of the sub-queues, see {@link #notifyDeparture},
   * and the semantics of the composite queue require the real job to depart as well (this is not a requirement).
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
   * The reset of a sub-queue can only be the result of this queue being reset itself
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
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob)
   * 
   */
  @Override
  public final void notifyArrival (final double time, final DJ job, final DQ queue)
  {
    getRealJob (job); // Sanity on existence of real job.
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

  /** Does nothing.
   * 
   * <p>
   * With {@link StartModel#LOCAL}, this composite queue has its own notion of {@code noWaitArmed},
   * which is independent of that state on any of its sub-queues;
   * nor does a {@code noWaitArmed} state change on a sub-queue require an action from the composite queue.
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE SERVER-ACCESS-CREDITS NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   * <p>
   * With {@link StartModel#LOCAL}, this composite queue has its own server-access credits,
   * and the (un)availability of server-access credits on any of the sub-queues has no effect
   * on this composite queue's state; nor does it require an action from the composite queue.
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * <p>
   * With {@link StartModel#LOCAL}, this composite queue has its own server-access credits,
   * and the (un)availability of server-access credits on any of the sub-queues has no effect
   * on this composite queue's state; nor does it require an action from the composite queue.
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE START NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Nothing to do apart from sanity check.
   * 
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob)
   * 
   */
  @Override
  public final void notifyStart (final double time, final DJ job, final DQ queue)
  {
    getRealJob (job); // Sanity on existence of real job.
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
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob, nl.jdj.jqueues.r5.SimQueue)
   * @see #getDropDestinationQueue
   * @see SimQueue#doAfterNotifications
   * @see #arrive
   * @see #drop
   * 
   */
  @Override
  public final void notifyDrop (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, null);
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
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob, nl.jdj.jqueues.r5.SimQueue)
   * 
   */
  @Override
  public final void notifyRevocation (final double time, final DJ job, final DQ queue)
  {
    getRealJob (job, null); // Sanity on existence of real job.
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE AUTO-REVOCATION NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Nothing to do apart from sanity check.
   * 
   * <p>
   * The {@link AbstractBlackSimQueueComposite_LocalStartModel} allows the use of auto-revocation
   * on its sub-queues.
   * 
   * @see #getRealJob
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final DJ job, final DQ queue)
  {
    getRealJob (job, queue); // Sanity on existence of real job.
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE DEPARTURE NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Notification of the departure of a delegate job.
   * 
   * <p>
   * Finds the next queue to visit by the delegate job.
   * If found, schedules the arrival of the delegate job at the next queue
   * through {@link #doAfterNotifications}.
   * Otherwise, invokes {@link #depart} on the real job.
   * 
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob, nl.jdj.jqueues.r5.SimQueue)
   * @see #selectNextQueue
   * @see SimQueue#arrive
   * @see #depart
   * @see SimQueue#doAfterNotifications
   * 
   */
  @Override
  public final void notifyDeparture (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, null);
    final SimQueue<DJ, DQ> nextQueue = selectNextQueue (time, realJob, queue);
    if (nextQueue == null)
      depart (time, realJob);
    else
      nextQueue.doAfterNotifications (() ->
      {
        nextQueue.arrive (time, job);
      });
  }

}
