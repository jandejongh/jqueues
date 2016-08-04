package nl.jdj.jqueues.r5.entity.queue.composite.single.enc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite.StartModel;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link BlackSimQueueComposite} encapsulating a single {@link SimQueue}.
 *
 * <p>
 * This composite queue mimics the {@link SimQueue} interface of the encapsulated queue.
 * 
 * <p>The main purpose of this apparently rather futile {@link SimQueue}
 * is to test the maturity of the {@link SimQueue} interface and its notifications:
 * Can we reconstruct a {@link SimQueue} interface by acting on and monitoring another {@link SimQueue}?.
 * It is, however, also useful to extract a bare {@link SimQueue} interface at the {@code Java} level
 * from a much more complicated queue implementation.
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the black composite queue.
 * For more details, refer to {@link StartModel#ENCAPSULATOR_QUEUE}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see BlackSimQueueComposite
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
 * @see #setStartModel
 * 
 */
public class BlackEncapsulatorSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackEncapsulatorSimQueue>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black encapsulator queue given an event list and a queue.
   *
   * <p>
   * The constructor sets the {@link StartModel} to {@link StartModel#ENCAPSULATOR_QUEUE}.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see StartModel
   * @see StartModel#ENCAPSULATOR_QUEUE
   * @see #setStartModel
   * 
   */
  public BlackEncapsulatorSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      Collections.singleton (queue),
      new SimQueueSelector<J, DQ> ()
      {
        @Override
        public DQ selectFirstQueue (final double time, final J job)
        {
          return queue;
        }
        @Override
        public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
        {
          if (previousQueue != queue)
            throw new IllegalArgumentException ();
          return null;
        }
      },
      delegateSimJobFactory);
    setStartModel (StartModel.ENCAPSULATOR_QUEUE);
  }
  
  /** Returns a new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
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
    return new BlackEncapsulatorSimQueue (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENCAPSULATED QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the encapsulated queue.
   * 
   * @return The encapsulated queue, non-{@code null}.
   * 
   */
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the {@code noWaitArmed} state of the encapsulated queue.
   * 
   * @return The {@code noWaitArmed} state of the encapsulated queue.
   * 
   * @see StartModel#ENCAPSULATOR_QUEUE
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getEncapsulatedQueue ().isNoWaitArmed ();
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

  /** Lets the delegate job arrive at the encapsulated queue, after sanity checks.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getEncapsulatedQueue
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
    final DJ delegateJob = getDelegateJob (job);
    getEncapsulatedQueue ().arrive (time, delegateJob);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link BlackEncapsulatorSimQueue}, a (real) job can only be dropped because of one of the following reasons:
   * <ul>
   * <li>
   * A delegate job is dropped on the encapsulated queue, see {@link #notifyDrop},
   * so we must drop the corresponding real job.
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
   * We realize that dropping a delegate job from the encapsulated queue does not require any rescheduling.
   * Dropping (or revoking) a (delegate) job from the encapsulated queue can certainly affect its {@link #isNoWaitArmed} state,
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
   * In a {@link BlackEncapsulatorSimQueue}, revocations on real jobs can only be the result of external requests,
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

  /** Sets the server-access credits on the encapsulated queue.
   * 
   * @see #getEncapsulatedQueue
   * @see SimQueue#setServerAccessCredits
   * @see #getLastUpdateTime
   * @see #getServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    getEncapsulatedQueue ().setServerAccessCredits (getLastUpdateTime (), getServerAccessCredits ());
  }
  
  /** Empty, nothing to do.
   * 
   * We set the server-access credits directly on the encapsulated queue through {@link #setServerAccessCreditsSubClass},
   * so there is nothing to do here.
   * 
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
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
    getDelegateJob (job); // Sanity on existence of delegate job.
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link BlackEncapsulatorSimQueue},
   * a (real) job can only depart because of one of the following reasons:
   * <ul>
   * <li>
   * A delegate job departs on the encapsulated, see {@link #notifyDeparture},
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
    getRealJob (job, queue); // Sanity on existence of real job.
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

  /** Invokes {@link #triggerPotentialNewNoWaitArmed} to make sure that the state-change is notified to listeners
   *  in case it is an autonomous event on the encapsulated queue.
   * 
   * @see #triggerPotentialNewNoWaitArmed
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    triggerPotentialNewNoWaitArmed (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE SERVER-ACCESS-CREDITS NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing, since server-access credits on the encapsulated queue are under our full control,
   *  and cannot change other than due to an (monitored) event at this {@link BlackEncapsulatorSimQueue}.
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing, since server-access credits on the encapsulated queue are under our full control,
   *  and cannot change other than due to an (monitored) event at this {@link BlackEncapsulatorSimQueue}.
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

  /** Starts the corresponding real job
   *  (which amounts only to local administration update and listener notification).
   * 
   * @see #getRealJob
   * @see #start
   * 
   */
  @Override
  public final void notifyStart (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    if (this.jobsInServiceArea.contains (realJob))
      throw new IllegalStateException ();
    start (time, realJob);
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
    getRealJob (job, queue); // Sanity on existence of real job.
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE AUTO-REVOCATION NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #autoRevoke} on the real job at this queue, allowing auto-revocations on the encapsulated queue.
   * 
   * @see #getRealJob
   * @see #autoRevoke
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final DJ job, final DQ queue)
  {
    autoRevoke (time, getRealJob (job, queue));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE DEPARTURE NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departs the real job
   * 
   * @see #getRealJob
   * @see #depart
   * 
   */
  @Override
  public final void notifyDeparture (final double time, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    depart (time, realJob);
  }

}
