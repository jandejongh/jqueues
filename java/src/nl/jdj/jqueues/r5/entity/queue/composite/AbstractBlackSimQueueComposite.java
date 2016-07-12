package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.single.encap.BlackEncapsulatorSimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link BlackSimQueueComposite}.
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
 * see {@link StartModel}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
  protected AbstractBlackSimQueueComposite
  (final SimEventList eventList,
    final Set<DQ> queues,
    final SimQueueSelector simQueueSelector,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, simQueueSelector);
    this.delegateSimJobFactory = ((delegateSimJobFactory == null) ? new DefaultDelegateSimJobFactory () : delegateSimJobFactory);
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
   * @param startModel The new start model (non-{@code null}).
   * 
   * @throws IllegalArgumentException If the argument is {@code null}, or {@link StartModel#ENCAPSULATOR_QUEUE} is chosen
   *                                  while there are fewer or more that <i>one</i> sub-queues,
   *                                  or {@link StartModel#COMPRESSED_TANDEM_2_QUEUE} is chosen
   *                                  while there are fewer or more that <i>two</i> sub-queues,
   * 
   * @see BlackEncapsulatorSimQueue
   * @see BlackCompressedTandem2SimQueue
   * 
   */
  protected final void setStartModel (final StartModel startModel)
  {
    if (startModel == null)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.ENCAPSULATOR_QUEUE && getQueues ().size () != 1)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.COMPRESSED_TANDEM_2_QUEUE && getQueues ().size () != 2)
      throw new IllegalArgumentException ();
    this.startModel = startModel;
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
   * By setting the "drop queue", the default behavior can be changed, and such jobs can be sent to one of the
   * sub-queues as an arrival.
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
   * on sub-queues. It should be set at most once (upon construction) and it should survive entity resets.
   * 
   * @param queue The destination sub-queue for dropped delegate jobs; non-{@code null}.
   * 
   * @see #getDropDestinationQueue
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or not a sub-queue of this composite queue.
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

  /** Returns the real job for given delegate job.
   * 
   * Performs various sanity checks on the arguments and the internal administration consistency.
   * 
   * @param delegateJob The delegate job.
   * @param queue       The queue at which the delegate job currently resides.
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail.
   * 
   */
  protected final J getRealJob (final DJ delegateJob, final DQ queue)
  {
    if (delegateJob == null || queue == null || ! getQueues ().contains (queue))
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

  /** Removes a real job and a delegate job from the internal data structures.
   * 
   * <p>
   * The jobs do not have to be present; if not, this method has (with respect to that job) no effect.
   * 
   * @param realJob     The real job     (may be {@code null} meaning no real job is to be removed).
   * @param delegateJob The delegate job (may be {@code null} meaning no delegate job is to be removed).
   * 
   */
  private /* final */ void removeJobsFromQueueLocal (final J realJob, final DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsInServiceArea.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AbstractSimQueue
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractBlackSimQueueComposite}.
   * 
   * <p>
   * Calls super method,
   * clears the internal mapping between real and delegate {@link SimJob}s,
   * clears the internal cache of the <code>noWaitArmed</code> state,
   * resets all sub-queues,
   * and, if needed, reassess their initial state for proper functioning of this {@link AbstractSimQueueComposite}.
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
    for (final DQ q : getQueues ())
      q.resetEntity ();
    this.previousNoWaitArmedSet = true;
    this.previousNoWaitArmed = isNoWaitArmed ();
    switch (getStartModel ())
    {
      case LOCAL:
      case ENCAPSULATOR_QUEUE:
        break;
      case COMPRESSED_TANDEM_2_QUEUE:
        // Asses the initial server-access credits on the waitQueue.
        final Set<DQ> subQueues = getQueues ();
        if (subQueues == null || subQueues.size () != 2)
          throw new IllegalStateException ();
        final Iterator<DQ> i_queues = subQueues.iterator ();
        final DQ waitQueue = i_queues.next ();
        final DQ servQueue = i_queues.next ();
        // No need to check our local SACs; by contract of SimQueue, SACs should be infinite after reset.
        waitQueue.setServerAccessCredits (getLastUpdateTime (), servQueue.isNoWaitArmed () ? 1 : 0);
        break;
    }    
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
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job, administers it and puts the (real) job into {@link #jobQueue}.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    if (this.delegateSimJobMap.containsKey (job))
      throw new IllegalStateException ();
    if (this.realSimJobMap.containsValue (job))
      throw new IllegalStateException ();
    final DJ delegateSimJob = this.delegateSimJobFactory.newInstance (time, job, (Q) this);
    if (delegateSimJob == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobMap.put (job, delegateSimJob);
    this.realSimJobMap.put (delegateSimJob, job);
    this.jobQueue.add (job);
  }

  /** Reschedules after an arrival.
   * 
   * <p>
   * In case of {@link StartModel#LOCAL}, checks the server-access credits and if passed (and after taking a credit),
   * lets the delegate job arrive at the queue returned by
   * {@link #selectFirstQueue}.
   * 
   * <p>
   * In case of {@link StartModel#ENCAPSULATOR_QUEUE} and {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * the procedure is the same, except for checking the server-access credits (and taking one);
   * the delegate job is always scheduled for arrival at the first queue.
   * 
   * <p>
   * With any {@link StartModel}, should <code>null</code> be returned by {@link #selectFirstQueue},
   * then the real job departs through {@link #depart} with listener notification.
   * 
   * @see #getStartModel
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #fireIfOutOfServerAccessCredits
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    if (! this.delegateSimJobMap.containsKey (job))
      throw new IllegalStateException ();
    if (! this.realSimJobMap.containsValue (job))
      throw new IllegalStateException ();
    final DJ delegateJob = this.delegateSimJobMap.get (job);
    if (delegateJob == null)
      throw new IllegalStateException ();
    final boolean needsSac;
    final boolean mayArrive;
    switch (getStartModel ())
    {
      case LOCAL:
        needsSac = true;
        mayArrive = hasServerAcccessCredits ();
        break;
      case ENCAPSULATOR_QUEUE:
      case COMPRESSED_TANDEM_2_QUEUE:
        needsSac = false;
        mayArrive = true;
        break;
      default:
        throw new RuntimeException ();
    }
    if (mayArrive)
    {
      if (needsSac)
        takeServerAccessCredit (false);
      final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
      if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
        throw new IllegalArgumentException ();
      if (firstQueue != null)
      {
        firstQueue.arrive (time, delegateJob);
        if (needsSac)
          fireIfOutOfServerAccessCredits (time);
      }
      else
        // We do not get a queue to arrive at.
        // So we depart; without having been executed!
        depart (time, job, true);
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
   * In the {@link AbstractBlackSimQueueComposite}, a (real) job can only be dropped because of one of the following reasons:
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
   * 
   * <p>
   * In case of {@link StartModel#LOCAL}, there is nothing to do here since dropping a real job and its corresponding delegate job
   * does not affect the access to the sub-queues, which is only determined by our (local) server-access credits.
   * And these are not affected by drops.
   * 
   * <p>
   * In case of {@link StartModel#ENCAPSULATOR_QUEUE}, we rely on (follow) the scheduling on the (single) sub-queue.
   * Since the delegate job has been dropped now (for whatever reason) on the encapsulated queue,
   * we can simply wait for notifications from the encapsulated queue for visit events
   * through the various notification listeners and act upon them. In short, there is nothing to do here.
   * 
   * <p>
   * In case of {@link StartModel#COMPRESSED_TANDEM_2_QUEUE}, we immediately realize that dropping a delegate job from the
   * wait queue (the first queue) does not require any rescheduling: the delegate job was waiting at the first queue,
   * so it was not eligible for access to the server queue, and the mere fact of dropping it can never result in another
   * job at the waiting queue getting access to the server, or result in a state change at the server queue (the second queue).
   * Dropping (or revoking) a (delegate) job from the server queue can certainly affect its {@link #isNoWaitArmed} state,
   * but this is reported to and handled by {@link #notifyNewNoWaitArmed}.
   * 
   * <p>
   * So, in short, there is really nothing to do here for good reasons.
   * 
   * @see StartModel
   * @see #getStartModel
   * @see #isNoWaitArmed
   * @see #notifyNewNoWaitArmed
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
  // isNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Determines the {@code noWaitArmed} state of the composite queue.
   * 
   * <p>
   * In case of {@link StartModel#LOCAL}, simply returns <code>true</code>.
   * 
   * <p>
   * In case of {@link StartModel#ENCAPSULATOR_QUEUE},
   *   copies the corresponding value on the sub-queue.
   * 
   * <p>
   * In case of {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   *   copies the corresponding value on the service-queue (i.e., the <i>second</i> sub-queue).
   * This result is only valid if the composite queue is in a stable state, i.e., a state in which
   *   the {@link noWaitArmed} state on the server queue implies that the waiting queue is empty and
   *   has server-access credits.
   * Care should be taken in case this method is used internally.
   * 
   * @return The {@code noWaitArmed} state of the composite queue.
   * 
   * @see #getStartModel
   * @see #getQueues
   * @see #getServerAccessCredits
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    final Set<DQ> subQueues;
    switch (getStartModel ())
    {
      case LOCAL:
        return true;
      case ENCAPSULATOR_QUEUE:
        subQueues = getQueues ();
        if (subQueues.size () != 1)
          throw new IllegalStateException ();
        return subQueues.iterator ().next ().isNoWaitArmed ();
      case COMPRESSED_TANDEM_2_QUEUE:
        subQueues = getQueues ();
        if (subQueues.size () != 2)
          throw new IllegalStateException ();
        final Iterator<DQ> i_queues = subQueues.iterator ();
        /* final DQ waitQueue = */ i_queues.next ();
        final DQ servQueue = i_queues.next ();
        return servQueue.isNoWaitArmed ();
      default:
        throw new RuntimeException ();
    }
  }

  /** Auxiliary variable to {@link #reassessNoWaitArmed}
   *  indicating whether we have a previous value for the <code>noWaitArmed</code> state.
   * 
   * @see #isNoWaitArmed
   * @see #reassessNoWaitArmed
   * @see #previousNoWaitArmed
   * 
   */
  private boolean previousNoWaitArmedSet = false;
  
  /** Auxiliary variable to {@link #reassessNoWaitArmed}
   *  being the previous value for the <code>noWaitArmed</code> state.
   * 
   * @see #isNoWaitArmed
   * @see #reassessNoWaitArmed
   * @see #previousNoWaitArmedSet
   * 
   */
  private boolean previousNoWaitArmed = false;
  
  /** Reassess the <code>noWaitArmed</code> state and fire a notification if it has changed.
   * 
   * This method internally caches the previous value of the <code>noWaitArmed</code> state.
   * 
   * @param time The current time.
   * 
   * @return The current <code>noWaitArmed</code> state.
   * 
   * @see #isNoWaitArmed
   * @see #fireNewNoWaitArmed
   * @see #notifyNewNoWaitArmed
   * 
   */
  protected final boolean reassessNoWaitArmed (final double time)
  {
    final boolean noWaitArmed = isNoWaitArmed ();
    if (this.previousNoWaitArmedSet && noWaitArmed == this.previousNoWaitArmed)
      return noWaitArmed;
    this.previousNoWaitArmedSet = true;
    this.previousNoWaitArmed = noWaitArmed;
    fireNewNoWaitArmed (time, noWaitArmed);
    return noWaitArmed;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** In case of {@link StartModel#LOCAL} or {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   *  does nothing and relies on {@link #rescheduleForNewServerAccessCredits};
   *  in case of {@link StartModel#ENCAPSULATOR_QUEUE},
   *  copies the new value for the server-access credits into the sub-queue.
   * 
   * @throws IllegalStateException If the start model is {@link StartModel#ENCAPSULATOR_QUEUE} and there is not a single sub-queue.
   * 
   * @see #getStartModel
   * @see #getServerAccessCredits
   * @see #setServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    switch (getStartModel ())
    {
      case LOCAL:
      case COMPRESSED_TANDEM_2_QUEUE:
        return;
      case ENCAPSULATOR_QUEUE:
        if (getQueues ().size () != 1)
          throw new IllegalStateException ();
        getQueues ().iterator ().next ().setServerAccessCredits (getLastUpdateTime (), getServerAccessCredits ());
        break;
      default:
        throw new RuntimeException ();
    }
  }
  
  /** Reschedules for new server-access credits.
   * 
   * <p>
   * In case of {@link StartModel#LOCAL}, schedules delegate jobs for arrival at their first queue until the
   *  (new) server-access credits are exhausted.
   * 
   * <p>
   * In case of {@link StartModel#ENCAPSULATOR_QUEUE}, this method does nothing.
   * 
   * <p>
   * In case of {@link StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * sets the server access credits on the wait queue to one if the serve queue is in {@code noWaitArmed} state.
   * 
   * @see #getStartModel
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #selectFirstQueue
   * @see SimQueue#arrive
   * @see #removeJobsFromQueueLocal 
   * @see #fireDeparture 
   * @see SimQueue#isNoWaitArmed 
   * @see SimQueue#setServerAccessCredits 
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    switch (getStartModel ())
    {
      case LOCAL:
        while (hasServerAcccessCredits ())
        {
          for (J realJob : this.jobQueue)
          {
            final DJ delegateJob = getDelegateJob (realJob);
            if (delegateJob.getQueue () == null)
            {
              // XXX (Almost) Verbatim copy from rescheduleAfterArrival...
              takeServerAccessCredit (true);
              final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, realJob);
              if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
                throw new IllegalArgumentException ();
              if (firstQueue != null)
                firstQueue.arrive (time, delegateJob);
              else
              {
                // We do not get a queue to arrive at.
                // So we depart; without having been executed!
                removeJobsFromQueueLocal (realJob, delegateJob);
                fireDeparture (time, realJob, (Q) this);
              }
            }
          }
        }
        return;
      case ENCAPSULATOR_QUEUE:
        return;
      case COMPRESSED_TANDEM_2_QUEUE:
        final Set<DQ> queues = getQueues ();
        if (queues == null || queues.size () != 2)
          throw new IllegalStateException ();
        final Iterator<DQ> i_queues = queues.iterator ();
        final DQ waitQueue = i_queues.next ();
        final DQ servQueue = i_queues.next ();
        if (servQueue.isNoWaitArmed ())
          waitQueue.setServerAccessCredits (time, 1);
        return;
      default:
        throw new RuntimeException ();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final void start (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    if (! this.delegateSimJobMap.containsKey (job))
      throw new IllegalStateException ();
    if (! this.realSimJobMap.containsValue (job))
      throw new IllegalStateException ();
    final DJ delegateJob = this.delegateSimJobMap.get (job);
    if (delegateJob == null)
      throw new IllegalStateException ();
    final boolean needsSac;
    final boolean mayArrive;
    switch (getStartModel ())
    {
      case LOCAL:
        needsSac = true;
        mayArrive = hasServerAcccessCredits ();
        break;
      case ENCAPSULATOR_QUEUE:
      case COMPRESSED_TANDEM_2_QUEUE:
        needsSac = false;
        mayArrive = true;
        break;
      default:
        throw new RuntimeException ();
    }
    if (mayArrive)
    {
      if (needsSac)
        takeServerAccessCredit (false);
      final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
      if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
        throw new IllegalArgumentException ();
      if (firstQueue != null)
      {
        firstQueue.arrive (time, delegateJob);
        if (needsSac)
          fireIfOutOfServerAccessCredits (time);
      }
      else
        // We do not get a queue to arrive at.
        // So we depart; without having been executed!
        depart (time, job, true);
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
   * In the {@link AbstractBlackSimQueueComposite}, a (real) job can only depart because of one of the following reasons:
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
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueListener IMPLEMENTATION
  //
  // LISTENS TO ALL "SUBQUEUES"
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
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
  public final void notifyUpdate (final double t, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    update (t);
  }
  
  /** Calls {@link #stateChanged}.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * 
   */
  @Override
  public final void notifyStateChanged (final double t, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    stateChanged (t);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE ARRIVAL NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Checks if the job is a known delegate job.
   * 
   * <p>
   * This implementation does not allow the arrival of foreign delegate jobs.
   * 
   * @see #getRealJob
   * 
   */
  @Override
  public final void notifyArrival (final double t, final DJ job, final DQ queue)
  {
    /* final J realJob = */ getRealJob (job, queue);
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

  /** Calls {@link #update} and {@link #reassessNoWaitArmed}.
   * 
   * @see #update
   * @see #reassessNoWaitArmed
   * @see #isNoWaitArmed
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    if (! getQueues ().contains (queue))
      throw new IllegalArgumentException ();
    switch (getStartModel ())
    {
      case LOCAL:
        break;
      case ENCAPSULATOR_QUEUE:
        break;
      case COMPRESSED_TANDEM_2_QUEUE:
        final Set<DQ> subQueues = getQueues ();
        if (subQueues == null || subQueues.size () != 2)
          throw new IllegalStateException ();
        final Iterator<DQ> i_queues = subQueues.iterator ();
        final DQ waitQueue = i_queues.next ();
        final DQ servQueue = i_queues.next ();
        if (queue == servQueue)
          waitQueue.setServerAccessCredits (time, (hasServerAcccessCredits () && noWaitArmed) ? 1 : 0);
        break;
      default:
        throw new RuntimeException ();
    }
    reassessNoWaitArmed (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE SERVER-ACCESS-CREDITS NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** In case of {@link StartModel#LOCAL} does nothing; in case of {@link StartModel#ENCAPSULATOR_QUEUE} fires a
   *  notification that we are out of server-access credits.
   * 
   * @see #getStartModel
   * @see #fireIfOutOfServerAccessCredits
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
    switch (getStartModel ())
    {
      case LOCAL:
      case COMPRESSED_TANDEM_2_QUEUE:
        return;
      case ENCAPSULATOR_QUEUE:
        fireIfOutOfServerAccessCredits (time);
        return;
      default:
        throw new RuntimeException ();
    }
  }

  /** In case of {@link StartModel#LOCAL} does nothing; in case of {@link StartModel#ENCAPSULATOR_QUEUE} fires a
   *  notification that we have regained server-access credits.
   * 
   * @see #getStartModel
   * @see #fireRegainedServerAccessCredits
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
    switch (getStartModel ())
    {
      case LOCAL:
      case COMPRESSED_TANDEM_2_QUEUE:
        return;
      case ENCAPSULATOR_QUEUE:
        fireRegainedServerAccessCredits (time);
        return;
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE START NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** In case of {@link StartModel#LOCAL}, does nothing; in case of {@link StartModel#ENCAPSULATOR_QUEUE},
   *  readjusts the server access credits, and fires the start of the real job.
   * 
   * <p>
   * Well, this method always calls {@link #update}.
   * 
   * <p>
   * This implementation does not allow the start of foreign delegate jobs.
   * 
   * @see #getRealJob
   * @see #fireStart
   * 
   */
  @Override
  public final void notifyStart (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    switch (getStartModel ())
    {
      case LOCAL:
        return;
      case ENCAPSULATOR_QUEUE:
        if (this.jobsInServiceArea.contains (realJob))
          throw new IllegalStateException ();
        takeServerAccessCredit (false);
        this.jobsInServiceArea.add (realJob);
        fireStart (t, realJob, (Q) this);
        return;
      case COMPRESSED_TANDEM_2_QUEUE:
        final Set<DQ> subQueues = getQueues ();
        if (subQueues == null || subQueues.size () != 2)
          throw new IllegalStateException ();
        final Iterator<DQ> i_queues = subQueues.iterator ();
        final DQ waitQueue = i_queues.next ();
        final DQ servQueue = i_queues.next ();
        if (queue == waitQueue)
        {
          if (this.jobsInServiceArea.contains (realJob))
            throw new IllegalStateException ();
          takeServerAccessCredit (false);
          this.jobsInServiceArea.add (realJob);
          if (! waitQueue.revoke (t, job, true))
            throw new RuntimeException ();
          servQueue.arrive (t, job);
          fireStart (t, realJob, (Q) this);
          if (hasServerAcccessCredits () && isNoWaitArmed ())
            waitQueue.setServerAccessCredits (t, 1);
          fireIfOutOfServerAccessCredits (t);
          reassessNoWaitArmed (t);
        }
        return;
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE DROP NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Notification of the dropping of a delegate job.
   * 
   * <p>
   * Calls {@link #update}.
   * Starts the dropped job on a valid non-<code>null</code> result from {@link #getDropDestinationQueue};
   * otherwise it gets the real job, sets its queue to <code>null</code> and drops it as well.
   * 
   * @see #update
   * @see #getDropDestinationQueue
   * @see #arrive
   * @see #removeJobsFromQueueLocal
   * @see SimJob#setQueue
   * @see #fireDrop
   * 
   */
  @Override
  public final void notifyDrop (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    final DQ dropDestinationQueue = getDropDestinationQueue ();
    if (dropDestinationQueue != null)
    {
      update (t);
      if (! getQueues ().contains (dropDestinationQueue))
        throw new RuntimeException ();
      dropDestinationQueue.arrive (t, job);
    }
    else
      drop (realJob, t);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE REVOCATION NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Checks if the job is a known delegate job, but nothing else.
   * 
   * <p>
   * Otherwise, this method throw an {@link IllegalStateException}.
   * 
   * <p>
   * A revocation of a delegate job is always the result of a revocation attempt from the composite (this) queue.
   * 
   * @see #drop
   * @see #revoke
   * 
   */
  @Override
  public final void notifyRevocation (final double t, final DJ job, final DQ queue)
  {
    /* final J realJob = */ getRealJob (job, queue);
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
   * If found, schedules the arrival of the delegate job at the next queue.
   * Otherwise, invokes {@link #depart} on the real job (with notification).
   * 
   * @see #selectNextQueue
   * @see #depart
   * 
   */
  @Override
  public final void notifyDeparture (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    final SimQueue<DJ, DQ> nextQueue = selectNextQueue (t, realJob, queue);
    if (nextQueue == null)
      depart (t, realJob, true);
    else
      nextQueue.arrive (t, job);
  }

}
