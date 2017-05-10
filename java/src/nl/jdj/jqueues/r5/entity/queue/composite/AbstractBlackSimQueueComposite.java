package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.collector.BlackDropCollectorSimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.single.enc.BlackEncapsulatorHideStartSimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.single.enc.BlackEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link BlackSimQueueComposite}.
 *
 * <p>
 * This abstract base class registers the start model
 * and the the drop destination queue,
 * takes care of all administration related to creating delegate jobs and of mapping between real and delegate jobs,
 * and deals with all {@link SimQueue} operations and all sub-queue notifications
 * (through the use of a {@link MultiSimQueueNotificationProcessor}).
 * 
 * <p>
 * The major functional "degrees of freedom" of an {@link AbstractBlackSimQueueComposite} are
 * (apart from other super-class aspects like naming and delegate-job creation)
 * the {@link SimQueueSelector} passed upon construction,
 * its {@link StartModel} (the default is {@link StartModel#LOCAL}),
 * and its drop-destination queue, as given by {@link #getDropDestinationQueue}.
 * 
 * <p>
 * Among these, the start model has the most dominant impact on the implementation
 * and allowed structure of {@link AbstractBlackSimQueueComposite}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see MultiSimQueueNotificationProcessor
 * @see StartModel
 * 
 */
public abstract class AbstractBlackSimQueueComposite
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackSimQueueComposite>
extends AbstractSimQueueComposite<DJ, DQ, J, Q>
implements BlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract black network of queues.
   * 
   * <p>
   * After calling the super constructor,
   * this methods sets the delegate job factory,
   * and inhibits future automatic resets from the event list on all sub-queues through {@link SimQueue#setIgnoreEventListReset},
   * since this object will take care of that (and depends on the absence of "independent" resets
   * of the sub-queues).
   * It then creates a new {@link MultiSimQueueNotificationProcessor} for all sub-queues,
   * and registers {@link #processSubQueueNotifications} as its processor.
   * Finally, it resets the local part of the object through a (private) variant of {@link #resetEntitySubClass}
   * that does not invoke its super method.
   * 
   * @param eventList             The event list to be shared between this queue and the inner queues.
   * @param queues                A set holding the "inner" queues.
   * @param simQueueSelector      The object for routing jobs through the network of embedded queues;
   *                                if {@code null}, no sub-queues will be visited.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    or the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimEntity#setIgnoreEventListReset
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processSubQueueNotifications
   * @see #resetEntitySubClass
   * 
   */
  protected AbstractBlackSimQueueComposite
  (final SimEventList eventList,
    final Set<DQ> queues,
    final SimQueueSelector simQueueSelector,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, simQueueSelector);
    this.delegateSimJobFactory = ((delegateSimJobFactory == null) ? new DefaultDelegateSimJobFactory () : delegateSimJobFactory);
    for (final DQ q : getQueues ())
      q.setIgnoreEventListReset (true);
    final MultiSimQueueNotificationProcessor<DJ, DQ>  subQueueEventProcessor =
      new MultiSimQueueNotificationProcessor<> (getQueues ());
    subQueueEventProcessor.setProcessor (this::processSubQueueNotifications);
    resetEntitySubClassLocal ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START MODEL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private StartModel startModel = StartModel.LOCAL;
  
  @Override
  public final StartModel getStartModel ()
  {
    return this.startModel;
  }
  
  /** Sets the start model of this composite queue (for use by sub-classes only).
   * 
   * <p>
   * This method should only be used by sub-classes upon construction, if needed, and should not
   * be called afterwards. The start-model setting should survive queue resets.
   * 
   * <p>
   * Upon exit, this method invokes {@link #resetEntitySubClass} to ensure the initial
   * state of the sub-queues.
   * 
   * @param startModel The new start model (non-{@code null}).
   * 
   * @throws IllegalArgumentException If the argument is {@code null},
   *                                  or {@link BlackSimQueueComposite.StartModel#ENCAPSULATOR_QUEUE}
   *                                  or {@link BlackSimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE} is chosen
   *                                  while there are fewer or more than <i>one</i> sub-queues,
   *                                  or {@link BlackSimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE} is chosen
   *                                  while there are fewer or more than <i>two</i> sub-queues,
   * 
   * @see BlackEncapsulatorSimQueue
   * @see BlackEncapsulatorHideStartSimQueue
   * @see BlackCompressedTandem2SimQueue
   * 
   */
  protected final void setStartModel (final StartModel startModel)
  {
    if (startModel == null)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.ENCAPSULATOR_QUEUE && getQueues ().size () != 1)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.ENCAPSULATOR_HIDE_START_QUEUE && getQueues ().size () != 1)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.COMPRESSED_TANDEM_2_QUEUE && getQueues ().size () != 2)
      throw new IllegalArgumentException ();
    this.startModel = startModel;
    resetEntitySubClassLocal ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP DESTINATION QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DQ dropDestinationQueue = null;
  
  /** Returns an optional destination (delegate) {@link SimQueue} for dropped jobs.
   * 
   * <p>
   * Normally, dropping a delegate job as noted by {@link #notifyDrop} results in dropping the corresponding real job.
   * By setting the "drop queue", the default behavior can be changed (but only from sub-classes),
   * and such jobs can be sent to one of the sub-queues as an arrival.
   * 
   * <p>
   * The default value is <code>null</code>, implying that the real job is to be dropped if its delegate job is dropped.
   * 
   * @return Any {@link SimQueue} in {@link #getQueues} to which the dropped delegate job is to be sent as an arrival,
   *           or <code>null</code> if the corresponding real job is to be dropped as well.
   * 
   * @see #notifyDrop
   * 
   */
  protected final DQ getDropDestinationQueue ()
  {
    return this.dropDestinationQueue;
  }
  
  /** Sets the destination sub-queue for dropped delegate jobs.
   * 
   * <p>
   * The drop destination queue is only to be used by sub-classes for specific behavior related to dropping of jobs
   * on sub-queues.
   * It should be set at most once (upon construction) and it should survive entity resets.
   * 
   * @param queue The destination sub-queue for dropped delegate jobs; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or not a sub-queue of this composite queue.
   * 
   * @see #getDropDestinationQueue
   * @see BlackDropCollectorSimQueue
   * 
   */
  protected final void setDropDestinationQueue (final DQ queue)
  {
    if (queue == null || ! getQueues ().contains (queue))
      throw new IllegalArgumentException ();
    this.dropDestinationQueue = queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELEGATE SIMJOBS AND REAL/DELEGATE SIMJOB MAPPINGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   */
  private DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory;
  
  @Override
  public final DelegateSimJobFactory<? extends DJ, DQ, J, Q> getDelegateSimJobFactory ()
  {
    return this.delegateSimJobFactory;
  }
  
  @Override
  public final void setDelegateSimJobFactory (final DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory)
  {
    if (delegateSimJobFactory == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobFactory = delegateSimJobFactory;
  }
  
  /** Maps "real" jobs onto delegate jobs.
   * 
   * Kept in sync with {@link realSimJobMap}.
   * 
   */
  private final Map<J, DJ> delegateSimJobMap = new HashMap<> ();
  
  /** Maps delegate jobs onto "real" jobs.
   * 
   * Kept in sync with {@link realSimJobMap}.
   * 
   */
  private final Map<DJ, J> realSimJobMap = new HashMap<> ();
  
  /** Returns the delegate job for given real job.
   * 
   * Performs various sanity checks on the argument and the internal administration consistency.
   * 
   * @param realJob The real job.
   * 
   * @return The delegate job.
   * 
   * @throws IllegalStateException If sanity checks fail.
   * 
   */
  protected final DJ getDelegateJob (final J realJob)
  {
    if (realJob == null)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    final DJ delegateJob = this.delegateSimJobMap.get (realJob);
    if (delegateJob == null)
      throw new IllegalStateException ();
    if (this.realSimJobMap.get (delegateJob) != realJob)
      throw new IllegalStateException ();
    return delegateJob;
  }

  /** Returns the real job for given delegate job, and asserts its presence on the given (sub-)queue, or on no (sub-)queue at all.
   * 
   * <p>
   * By using this method, you assume that the delegate job is present on the given sub-{@code queue},
   * or, if passing a {@code null} argument for the {@code queue},
   * on no sub-queue at all.
   * This method will rigorously check your assumption and happily throw an {@link IllegalStateException}
   * if your assumption proves wrong.
   * Clearly, this method is primarily intended for internal consistency checking.
   * 
   * <p>
   * Performs various additional sanity checks on the arguments and the internal administration consistency.
   * 
   * @param delegateJob The delegate job.
   * @param queue       The queue at which the delegate job currently resides,
   *                    {@code null} if it is supposed to reside on <i>none</i> of the (sub-)queues..
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found,
   *                               or where assumption on the delegate-job whereabout proves to be wrong.
   * 
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob)
   * 
   */
  protected final J getRealJob (final DJ delegateJob, final DQ queue)
  {
    if (delegateJob == null || (queue != null && ! getQueues ().contains (queue)))
      throw new IllegalStateException ();
    final J realJob = this.realSimJobMap.get (delegateJob);
    if (realJob == null)
      throw new IllegalStateException ();
    if (this.delegateSimJobMap.get (realJob) != delegateJob)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    if (queue == null)
    {
      for (final DQ subQueue : getQueues ())
        if (subQueue.getJobs ().contains (delegateJob))
          throw new IllegalStateException ();
    }
    else if (! queue.getJobs ().contains (delegateJob))
      throw new IllegalStateException ();
    return realJob;
  }
  
  /** Returns the real job for given delegate job.
   * 
   * <p>
   * Performs various sanity checks on the arguments and the internal administration consistency.
   * 
   * @param delegateJob The delegate job.
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found.
   * 
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob, nl.jdj.jqueues.r5.SimQueue)
   * 
   */
  protected final J getRealJob (final DJ delegateJob)
  {
    if (delegateJob == null)
      throw new IllegalStateException ();
    final J realJob = this.realSimJobMap.get (delegateJob);
    if (realJob == null)
      throw new IllegalStateException ();
    if (this.delegateSimJobMap.get (realJob) != delegateJob)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    return realJob;
  }
  
  /** Adds a real job, creating its delegate job.
   * 
   * @param realJob The real job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the real job is already present,
   *                                  or if no delegate job for it could be created.
   * @throws IllegalStateException    If the internal administration is found inconsistent.
   * 
   */
  protected final void addRealJobLocal (final J realJob)
  {
    if (realJob == null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (realJob) || this.jobsInServiceArea.contains (realJob))
      throw new IllegalArgumentException ();
    if (this.delegateSimJobMap.containsKey (realJob))
      throw new IllegalStateException ();
    if (this.realSimJobMap.containsValue (realJob))
      throw new IllegalStateException ();
    final DJ delegateSimJob = this.delegateSimJobFactory.newInstance (getLastUpdateTime (), realJob, (Q) this);
    if (delegateSimJob == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobMap.put (realJob, delegateSimJob);
    this.realSimJobMap.put (delegateSimJob, realJob);
    this.jobQueue.add (realJob);
    
  }

  /** Removes a real job and a delegate job from the internal data structures.
   * 
   * <p>
   * The jobs do not have to be present; if not, this method has (with respect to that job) no effect.
   * 
   * @param realJob     The real job     (may be {@code null} meaning no real job is to be removed).
   * @param delegateJob The delegate job (may be {@code null} meaning no delegate job is to be removed).
   * 
   */
  protected final void removeJobsFromQueueLocal (final J realJob, final DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsInServiceArea.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractBlackSimQueueComposite}.
   * 
   * <p>
   * Calls super method (not if called from constructor, for which a private variant for local resets is used),
   * clears the pending revocation event for a sub-queue,
   * and clears the internal mapping between real and delegate {@link SimJob}s (removing all real and delegate jobs)
   * and resets all sub-queues in the order in which they appear in {@link #getQueues}.
   * (Note: some sub-classes rely on this order!)
   * 
   * <p>
   * Finally, if the start model is {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * it sets the server-access credits on the wait (first) sub-queue to unity
   * if the serve (second) queue has {@link SimQueue#isStartArmed} value {@code true},
   * and zero if not.
   * 
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * @see SimQueue#resetEntity
   * @see #getStartModel
   * @see StartModel#COMPRESSED_TANDEM_2_QUEUE
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    // Implemented in private method to make it accessible from the constructor(s).
    resetEntitySubClassLocal ();
  }
  
  private void resetEntitySubClassLocal ()
  {
    this.delegateSimJobMap.clear ();
    this.realSimJobMap.clear ();
    this.pendingDelegateRevocationEvent = null;
    for (final DQ q : getQueues ())
      q.resetEntity ();
    if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE)
      getQueue (0).setServerAccessCredits (getLastUpdateTime (), getQueue (1).isStartArmed () ? 1 : 0);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isStartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the {@code startArmed} state of the composite queue, possibly depending on the state of its sub-queues.
   * 
   * <p>
   * For {@link StartModel#LOCAL}, this method returns {@code true}.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_QUEUE}, this method returns the {@link SimQueue#isStartArmed}
   * state of the encapsulated queue.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE}, this method returns {@code false}.
   * 
   * <p>
   * For {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, this method returns the {@link SimQueue#isStartArmed}
   * state of the serve (i.e., second) queue.
   * 
   * @return The {@code startArmed} state of the composite queue, possibly depending on the state of its sub-queues.
   * 
   * @see #getStartModel
   * @see StartModel
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    switch (getStartModel ())
    {
      case LOCAL:
        return true;
      case ENCAPSULATOR_QUEUE:
        return getQueue (0).isStartArmed ();
      case ENCAPSULATOR_HIDE_START_QUEUE:
        return false;
      case COMPRESSED_TANDEM_2_QUEUE:
        return getQueue (1).isStartArmed ();
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SET SERVER-ACCESS CREDITS ON WAIT QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Sets the server-access credits on the wait queue, based upon our server-access credits
   *  and the {@link SimQueue#isStartArmed} state on the server queue.
   * 
   * <p>
   * This method can only be used with {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * at the expense of an exception.
   * 
   * <p>
   * With {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * at all times (well, if we have a consistent queue state),
   * the server-access credits on the wait queue should be unity if and only if
   * the local (this) queue {@link #hasServerAcccessCredits}
   * AND the serve queue has {@link SimQueue#isStartArmed}.
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
   * @throws IllegalStateException If the start-model of this queue is other than {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   *                               or if the current server-access credits value on the wait queue is not zero or unity.
   * 
   * @see #getStartModel
   * @see StartModel
   * @see #getQueue(int)
   * @see #getLastUpdateTime
   * @see #hasServerAcccessCredits
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
   * 
   */
  protected final void setServerAccessCreditsOnWaitQueue ()
  {
    if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE)
    {
      final DQ waitQueue = getQueue (0);
      final DQ serveQueue = getQueue (1);
      final int oldWaitQueueSac = waitQueue.getServerAccessCredits ();
      if (oldWaitQueueSac < 0 || oldWaitQueueSac > 1)
        throw new IllegalStateException ();
      final int newWaitQueueSac = (hasServerAcccessCredits () && serveQueue.isStartArmed ()) ? 1 : 0;
      if (newWaitQueueSac != oldWaitQueueSac)
        waitQueue.setServerAccessCredits (getLastUpdateTime (), newWaitQueueSac);
    }
    else
      throw new IllegalStateException ();
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

  /** Depending on the start model, starts the arrived job if possible or sends its delegate job to the first queue.
   * 
   * <p>
   * For {@link StartModel#LOCAL}, this method invokes {@link #start} if there are (local) server-access credits.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_QUEUE} and {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   * this method lets the delegate job arrive at the encapsulated queue.
   * 
   * <p>
   * For {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, this method lets the delegate job arrive at the wait queue.
   * 
   * @see #getStartModel
   * @see StartModel
   * @see #hasServerAcccessCredits
   * @see #start
   * @see SimQueue#arrive
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
    final DJ delegateJob = getDelegateJob (job);
    switch (getStartModel ())
    {
      case LOCAL:
        if (hasServerAcccessCredits ())
          start (time, job);
        break;
      case ENCAPSULATOR_QUEUE:
      case ENCAPSULATOR_HIDE_START_QUEUE:
      case COMPRESSED_TANDEM_2_QUEUE:
        getQueue (0).arrive (time, delegateJob);
        break;
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link AbstractBlackSimQueueComposite},
   * a (real) job can <i>only</i> be dropped because
   * its delegate job is dropped on one of the sub-queues, or perhaps due to some other sub-queue-related event,
   * see {@link #processSubQueueNotifications},
   * and the semantics of this composite queue require the real job to be dropped as well
   * (this is not a general requirement, but we impose it on {@link AbstractBlackSimQueueComposite}).
   * The notification callback relies on {@link #drop} to perform the drop.
   * 
   * <p>
   * The essential notion is that the delegate job has already left the sub-queue system when we are called,
   * hence no action is required to remove it from there.
   * Another way of saying this is that an {@link AbstractBlackSimQueueComposite}
   * <i>never</i> decides <i>by itself</i> to drop a job,
   * but only in response to event notifications (drops) from its sub-queues.
   * If the feature of "locally initiated drops" is desired,
   * the drop call-backs need to be retrofitted conform the approach with the
   * {@link #removeJobFromQueueUponRevokation} and {@link #rescheduleAfterRevokation} combo.
   * 
   * <p>
   * This method ignores the drop-destination queue, see {@link #getDropDestinationQueue}.
   * Following the contract of {@link AbstractSimQueue#drop}, the drop of the (real) job is inevitable (by) now.
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
   * In a {@link AbstractBlackSimQueueComposite}, revocations on real jobs can occur either
   * through external requests, in other words, through {@link #revoke}, or because of auto-revocations
   * on the composite (this) queue through {@link #autoRevoke}.
   * In both cases, the delegate job is still present on a sub-queue,
   * and we have to forcibly revoke it.
   * Because we cannot perform the revocation here (we are <i>not</i> allowed to reschedule!),
   * we defer until {@link #removeJobFromQueueUponRevokation} by raising an internal flag
   * (in fact a newly created, though not scheduled {@link SimQueueJobRevocationEvent}).
   * We have to use this method in order to remember the delegate job to be revoked,
   * and the queue from which to revoke it,
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

  /** Performs the pending revocation on the applicable sub-queue, after clearing it (as a flag to revoke a delegate job).
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

  /** Takes appropriate action if needed on the server-access credits of sub-queues.
   * 
   * <p>
   * For {@link StartModel#LOCAL}, this method does nothing, since server-access credits with this model are managed locally.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_QUEUE}, this method copies the new server-access credits into the encapsulated queue.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE}, this method does nothing,
   * since (real) jobs cannot start on the composite queue, and the number of server-access credits on the
   * encapsulated queue in always infinite.
   * 
   * <p>
   * For {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, this method sets the server-access credits on the wait queue
   *                                                   <i>only</i> if we run out of local server-access credits.
   * Note that the case in which we regain them is dealt with by {@link #rescheduleForNewServerAccessCredits}.
   * 
   * @see #getStartModel
   * @see StartModel
   * @see #getLastUpdateTime
   * @see #getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #rescheduleForNewServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    switch (getStartModel ())
    {
      case LOCAL:
        break;
      case ENCAPSULATOR_QUEUE:
        getQueue (0).setServerAccessCredits (getLastUpdateTime (), getServerAccessCredits ());
        break;
      case ENCAPSULATOR_HIDE_START_QUEUE:
        break;
      case COMPRESSED_TANDEM_2_QUEUE:
        if (getServerAccessCredits () == 0)
          setServerAccessCreditsOnWaitQueue ();
        break;
      default:
        throw new RuntimeException ();
    }
  }
  
  /** Depending on the start model,
   *  takes appropriate action if needed on waiting jobs or setting the server-access credits of sub-queues.
   * 
   * <p>
   * For {@link StartModel#LOCAL}, this method starts waiting jobs (in the local waiting area)
   *                               as long as there are such jobs and there are (local) server-access credits available.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_QUEUE}, this method does nothing (we follow the server-access credits on the
   *                                            encapsulated queue, and only set them upon external request).
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE}, this method does nothing (the server-access credits on the
   *                                            encapsulated queue is always infinite,
   *                                            and on the composite queue there are no job starts).
   * 
   * <p>
   * For {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, this method sets the server-access credits on the wait queue.
   * Note that the case in which we lose them is dealt with by {@link #setServerAccessCreditsSubClass}.
   * 
   * @see #getStartModel
   * @see StartModel
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    switch (getStartModel ())
    {
      case LOCAL:
        while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
          start (time, getFirstJobInWaitingArea ());
        break;
      case ENCAPSULATOR_QUEUE:
      case ENCAPSULATOR_HIDE_START_QUEUE:
        break;
      case COMPRESSED_TANDEM_2_QUEUE:
        setServerAccessCreditsOnWaitQueue ();
        break;
      default:
        throw new RuntimeException ();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job in the service area (after sanity checks).
   * 
   * @throws IllegalStateException If the start model is {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   *                               or if other sanity checks on internal consistency fail.
   * 
   * @see #getStartModel
   * @see StartModel#ENCAPSULATOR_HIDE_START_QUEUE
   * @see #jobsInServiceArea
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
    if (getStartModel () == StartModel.ENCAPSULATOR_HIDE_START_QUEUE)
      // Real jobs cannot start; so a call of this method should not happen!
      throw new IllegalStateException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Depending on the start model,
   *  lets the delegate job arrive at its first queue, or make it depart immediately if no such queue is provided.
   * 
   * <p>
   * For {@link StartModel#LOCAL}, this method selects the first sub-queue for the delegate job to arrive on
   *                               through {@link #selectFirstQueue}. If a sub-queue is provided,
   *                               it makes the delegate job arrive on that sub-queue;
   *                               otherwise it invokes {@link #depart} on the real job.
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_QUEUE}, this method does nothing (we are merely being notified of the start of a delegate
   *                                            job on the encapsulated queue, and our own notification will be dealt with by
   *                                            our caller, {@link #start}).
   * 
   * <p>
   * For {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE}, this method throws an {@link IllegalStateException}
   *                                                       because (real) jobs cannot start and an invocation of this method
   *                                                       is therefore unexpected (illegal).
   * 
   * <p>
   * For {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, lets the delegate job arrive on the serve queue (the second queue).
   * 
   * @see #getStartModel
   * @see StartModel
   * @see #getDelegateJob
   * @see #selectFirstQueue
   * @see #arrive
   * @see #depart
   * @see #getQueue
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
    switch (getStartModel ())
    {
      case LOCAL:
        // Arrive at first queue, if provided.
        final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
        if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
          throw new IllegalArgumentException ();
        if (firstQueue != null)
          firstQueue.arrive (time, delegateJob);
        else
          // We do not get a queue to arrive at.
          // So we depart; without having been executed!
          depart (time, job);
        break;
      case ENCAPSULATOR_QUEUE:
        break;
      case ENCAPSULATOR_HIDE_START_QUEUE:
        // Real jobs cannot start; so a call of this method should not happen!
        throw new IllegalStateException ();
      case COMPRESSED_TANDEM_2_QUEUE:
        // Arrive at serve queue.
        getQueue (1).arrive (time, delegateJob);
        break;
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link AbstractBlackSimQueueComposite},
   * a (real) job can only depart as a result from an event (notification) of one of its sub-queues.
   * Refer to {@link #removeJobFromQueueUponDrop} for a more detailed explanation
   * on the rationale behind this and behind the action taken here...
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
  // PROCESS (AND SANITY ON) SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queues, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} notifications from all sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for all sub-queues) created upon construction,
   * see {@link MultiSimQueueNotificationProcessor.Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * This method takes one notification at a time, starting at the head of the list, removes it,
   * and invokes {@link #sanitySubQueueNotification} on it.
   * Subsequently, it processes the notification as described below.
   * While processing, new notifications may be added to the list; the list is processed until it is empty.
   * 
   * <p>
   * However, before processing any event it checks for {@link SimEntitySimpleEventType#RESET}
   * (sub-)notifications. If it finds <i>any</i>, the notifications list is cleared and immediate return from this method follows.
   * 
   * Otherwise, this method processes the notifications are described;
   * the remainder of the method is encapsulated in a
   * {@link #clearAndUnlockPendingNotificationsIfLocked} and {@link #fireAndLockPendingNotifications} pair,
   * to make sure we create atomic notifications in case of a top-level event.
   * 
   * <p>
   * A notification consists of a (fixed) sequence of sub-notifications,
   * see {@link MultiSimQueueNotificationProcessor.Notification#getSubNotifications},
   * each of which is processed in turn as follows:
   * <ul>
   * <li>With {@link SimEntitySimpleEventType#RESET}, impossible, see above; throws an {@link IllegalStateException}.
   * <li>With {@link SimEntitySimpleEventType#DROP}, we let the dropped delegate job arrive on a drop-destination queue,
   *                                                 if provided through {@link #getDropDestinationQueue},
   *                                                 otherwise, drops the real job through {@link #drop}.
   * <li>With {@link SimQueueSimpleEventType#AUTO_REVOCATION}, we first check the start model
   *                                                           (must be {@link StartModel#COMPRESSED_TANDEM_2_QUEUE})
   *                                                           and the source queue (must be the wait, or first, queue),
   *                                                           and throw an exception if the check fails.
   *                                                           Subsequently, we start the real job with {@link #start}.
   * <li>With {@link SimQueueSimpleEventType#START}, we start the real job if the start model
   *                                                 is {@link StartModel#ENCAPSULATOR_QUEUE},
   *                                                 but we do nothing otherwise.
   * <li>With {@link SimQueueSimpleEventType#DEPARTURE}, we invoke {@link #selectNextQueue} on the real job,
   *                                                     and let the delegate job arrive at the next queue if provided,
   *                                                     or makes the real job depart if not through {@link #depart}.
   * <li>With any non-standard notification type, see {@link #isStandardNotification},
   *                                              and start model {@link StartModel#ENCAPSULATOR_QUEUE}
   *                                              or {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   *                                              we add the notification from the sub-queue to our own notification list
   *                                              (through {@link #addPendingNotification}),
   *                                              replacing a job in the sub-queue notification with its corresponding real job
   *                                              in our own notification.
   * </ul>
   * After all sub-notifications have been processed, and if the start model is {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * we make sure the server-access credits on the wait queue are set properly with {@link #setServerAccessCreditsOnWaitQueue},
   * unless the notification was a (single) {@link SimEntitySimpleEventType#RESET},
   * and we can rely on the composite queue reset logic.
   * 
   * <p>
   * After all notification have been processed, and the notification list is empty,
   * we invoke {@link #triggerPotentialNewStartArmed} on the composite queue,
   * in order to make sure we are not missing an autonomous change in {@link SimQueue#isStartArmed}
   * on a sub-queue.
   * Since we do not expect any back-fire notifications from sub-queues from that method,
   * we check again the notification list, and throw an exception if it is non-empty.
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty, or contains a notification from another queue
   *                                  than the a sub-queue,
   *                                  or if other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor.Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #sanitySubQueueNotification
   * @see #getStartModel
   * @see StartModel
   * @see SimEntitySimpleEventType#RESET
   * @see SimEntitySimpleEventType#DROP
   * @see SimEntitySimpleEventType#AUTO_REVOCATION
   * @see SimEntitySimpleEventType#START
   * @see SimEntitySimpleEventType#DEPARTURE
   * @see #isStandardNotification
   * @see #addPendingNotification
   * @see #getDropDestinationQueue
   * @see SimQueue#arrive
   * @see #start
   * @see #selectNextQueue
   * @see #depart
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #triggerPotentialNewStartArmed
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ();
    // Special treatment of RESET notifications: clear everthing and return immediately.
    boolean containsResetNotification = false;
    for (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification : notifications)
    {
      for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
        if (subNotification.keySet ().iterator ().next () == SimEntitySimpleEventType.RESET)
        {
          containsResetNotification = true;
          break;
        }
      if (containsResetNotification)
        break;
    }
    if (containsResetNotification)
    {
      notifications.clear ();
      return;
    }
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    while (! notifications.isEmpty ())
    {
      final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification = notifications.remove (0);
      sanitySubQueueNotification (notification);
      final double notificationTime = notification.getTime ();
      final DQ subQueue = notification.getQueue ();
      for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
      {
        final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
        final DJ job = subNotification.values ().iterator ().next ();
        if (notificationType == SimEntitySimpleEventType.RESET)
          throw new IllegalStateException ();
        else if (notificationType == SimEntitySimpleEventType.DROP)
        {
          final J realJob = getRealJob (job);
          final DQ dropDestinationQueue = getDropDestinationQueue ();
          if (dropDestinationQueue != null)
          {
            if (! getQueues ().contains (dropDestinationQueue))
              throw new RuntimeException ();
            dropDestinationQueue.arrive (notificationTime, job);
          }
          else
            drop (realJob, notificationTime);
        }
        else if (notificationType == SimEntitySimpleEventType.AUTO_REVOCATION)
        {
          if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE && subQueue == getQueue (0))
            start (notificationTime, getRealJob (job, null));
          else
            throw new IllegalStateException ();
        }
        else if (notificationType == SimEntitySimpleEventType.START && getStartModel () == StartModel.ENCAPSULATOR_QUEUE)
          start (notificationTime, getRealJob (job));
        else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        {
          final J realJob = getRealJob (job);
          final SimQueue<DJ, DQ> nextQueue = selectNextQueue (notificationTime, realJob, subQueue);
          if (nextQueue == null)
            depart (notificationTime, realJob);
          else
            nextQueue.arrive (notificationTime, job);
        }
        else if ((! isStandardNotification (notificationType))
          && (getStartModel () == StartModel.ENCAPSULATOR_QUEUE || getStartModel () == StartModel.ENCAPSULATOR_HIDE_START_QUEUE))
        {
          // XXX At the present time, only supports non-job related notifications!
          // In future, must check for presence of job parameter, check presence of delegate job in admin,
          // and replace with real job.
          addPendingNotification (notificationType, null);          
        }
      }
      if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE && getIndex (subQueue) == 1)
        setServerAccessCreditsOnWaitQueue ();
    }
    triggerPotentialNewStartArmed (getLastUpdateTime ());
    if (! notifications.isEmpty ())
      throw new IllegalStateException ();
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  /** Performs sanity checks on a notification from a sub-queue (irrespective of which one).
   * 
   * <p>
   * A full description of the sanity checks would make this entry uninterestingly large, hence we refer to the source code.
   * Most checks are trivial checks on the allowed sub-notifications from the sub-queues depending
   * on the start model and on the presence or absence of real and delegate jobs
   * (and their expected presence or absence on a sub-queue).
   * 
   * <p>
   * Noteworthy is that for {@link SimEntitySimpleEventType#RESET},
   * we expect it to be the only sub-notification in a notification, in other words,
   * it cannot be put in an atomic notification with others.
   * 
   * <p>
   * With {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * we check that {@link SimEntitySimpleEventType#START} and {@link SimEntitySimpleEventType#AUTO_REVOCATION}
   * always come in pairs from the wait queue
   * and allow at most one such pair in a (atomic) notification.
   * 
   * @param notification The notification.
   * 
   * @throws IllegalArgumentException If the time of the notification differs from our last update time.
   * @throws IllegalStateException    If a {@link SimEntitySimpleEventType#RESET} notification was found in combination
   *                                  with other sub-notifications (i.e., as part of an atomic notification that includes
   *                                  other sub-notifications next to the reset),
   *                                  or if a Queue-Access Vacation notification was found.
   * 
   * @see MultiSimQueueNotificationProcessor.Notification#getTime
   * @see #getLastUpdateTime
   * @see SimQueueSimpleEventType#RESET
   * @see MultiSimQueueNotificationProcessor.Notification#getSubNotifications
   * 
   */
  protected final void sanitySubQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    if (notification.getTime () != getLastUpdateTime ())
      throw new IllegalArgumentException ("on " + this + ": notification time [" + notification.getTime ()
      + "] != last update time [" + getLastUpdateTime () + "], subnotifications: "
      + notification.getSubNotifications () + ".");
    int nrStarted = 0;
    DJ lastJobStarted = null;
    int nrAutoRevocations = 0;
    DJ lastJobAutoRevoked = null;
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      final DJ job = subNotification.values ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
      {
        if (notification.getSubNotifications ().size () > 1)
          throw new IllegalStateException ();
      }
      else if (notificationType == SimQueueSimpleEventType.QAV_START
            || notificationType == SimQueueSimpleEventType.QAV_END)
        // Queue-Access Vacations are forbidden on sub-queues.
        throw new IllegalStateException ();
      else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        // The real job must exist (but may have already left).
        getRealJob (job);
      else if (notificationType == SimQueueSimpleEventType.START)
      {
        // The real job must exist (but may have already left).
        getRealJob (job);
        nrStarted++;
        lastJobStarted = job;
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
        if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE)
        {
          // The real job must exist, but its delegate job must not be present on any sub-queue.
          getRealJob (job, null);
          nrAutoRevocations++;
          lastJobAutoRevoked = job;
        }
        else
          throw new IllegalArgumentException ();
      }
      else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        // The real job must exist, but its delegate job must not be present on any sub-queue.
        getRealJob (job, null);
    }
    if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE && notification.getQueue () == getQueue (0))
    {
      // START and AUTO_REVOCATION have to come in pairs on the wait queue,
      // apply to the same job and at most one pair is allowed in a sub-notification.
      if (nrStarted > 1 || nrAutoRevocations > 1 || nrStarted != nrAutoRevocations || lastJobStarted != lastJobAutoRevoked)
        throw new IllegalStateException ();
    }
  }
  
  /** Checks whether a notification type is standard (i.e., belonging to a {@link SimQueue} without notification extensions).
   * 
   * @param notificationType The notification type, non-{@code null}.
   * 
   * @return True if the notification type is standard.
   * 
   * @throws IllegalArgumentException If the notification type is {@code null}.
   * 
   */
  protected final boolean isStandardNotification (final SimEntitySimpleEventType.Member notificationType)
  {
    if (notificationType == null)
      throw new IllegalArgumentException ();
    return notificationType == SimEntitySimpleEventType.RESET
        || notificationType == SimQueueSimpleEventType.QAV_START
        || notificationType == SimQueueSimpleEventType.QAV_END
        || notificationType == SimQueueSimpleEventType.ARRIVAL
        || notificationType == SimQueueSimpleEventType.DROP
        || notificationType == SimQueueSimpleEventType.REVOCATION
        || notificationType == SimQueueSimpleEventType.AUTO_REVOCATION
        || notificationType == SimQueueSimpleEventType.STA_FALSE
        || notificationType == SimQueueSimpleEventType.STA_TRUE
        || notificationType == SimQueueSimpleEventType.OUT_OF_SAC
        || notificationType == SimQueueSimpleEventType.REGAINED_SAC
        || notificationType == SimQueueSimpleEventType.START
        || notificationType == SimQueueSimpleEventType.DEPARTURE;
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
  public final void notifyNewStartArmed (final double time, final DQ queue, final boolean startArmed)
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
