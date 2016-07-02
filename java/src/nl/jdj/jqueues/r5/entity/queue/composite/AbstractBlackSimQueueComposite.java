package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link BlackSimQueueComposite}.
 *
 * <p>
 * Implementations only have to route a job (actually, its delegate job) through the
 * internal network of {@link SimQueue}s,
 * see {@link #selectFirstQueue} and {@link #selectNextQueue}.
 * 
 * <p>
 * This allows for many types of queueing networks, including "feedback"-type networks.
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
   * @throws IllegalArgumentException If the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
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
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP DESTINATION QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns an optional destination (delegate) {@link SimQueue} for dropped jobs.
   * 
   * <p>
   * Normally, dropping a delegate job as noted by {@link #notifyDrop} results in dropping the corresponding real job.
   * By overriding this method the default behavior can be changed, and such jobs can be sent to one of the
   * sub-queues as an arrival.
   * 
   * <p>
   * The default implementation returns <code>null</code>, implying that the real job is to be dropped as well.
   * 
   * @param t The time the delegate job was dropped, i.e., the current time.
   * @param job The (delegate) job that was dropped.
   * @param queue The queue at which it was dropped.
   * 
   * @return Any {@link SimQueue} in {@link #getQueues} to which the dropped job is to be sent as an arrival, or <code>null</code>
   *           if the corresponding real job is to be dropped as well.
   * 
   * @see #notifyDrop
   * 
   */
  protected DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    return null;
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
   * @param queue The queue at which the delegate job currently resides.
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
   * @param realJob The real job.
   * @param delegateJob The delegate job.
   * 
   */
  private /* final */ void exitJobFromQueues (final J realJob, final DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsInServiceArea.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueue.isNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns <code>true</code> if and only if all queues in {@link #getQueues} are in <code>noWaitArmed</code> state.
   * 
   * This is a default implementation, and may be overridden by subclasses.
   * 
   * @return True if and only if all queues in {@link #getQueues} are in <code>noWaitArmed</code> state.
   * 
   */
  @Override
  public boolean isNoWaitArmed ()
  {
    for (DQ q : getQueues ())
      if (! q.isNoWaitArmed ())
        return false;
    return true;
  }
  
  /** Auxiliary variable to {@link #reassessNoWaitArmed}
   * indicating whether we have a previous value for the <code>noWaitArmed</code> state.
   * 
   * @see #isNoWaitArmed
   * @see #reassessNoWaitArmed
   * @see #previousNoWaitArmed
   * 
   */
  private boolean previousNoWaitArmedSet = false;
  
  /** Auxiliary variable to {@link #reassessNoWaitArmed}
   * being the previous value for the <code>noWaitArmed</code> state.
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
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractBlackSimQueueComposite}.
   * 
   * <p>
   * Calls super method,
   * clears the internal mapping between real and delegate {@link SimJob}s,
   * and clears the internal cache of the <code>noWaitArmed</code> state.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.delegateSimJobMap.clear ();
    this.realSimJobMap.clear ();
    this.previousNoWaitArmedSet = false;
    this.previousNoWaitArmed = false;
    // No need to reset the queues; they will be reset directly from the event list.
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AbstractSimQueue
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job, administers it and puts the job into {@link #jobQueue}.
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
    update (time);
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
   * Checks the server-access credits and if passed,
   * lets the delegate job arrive at the queue returned by
   * {@link #selectFirstQueue}.
   * Should <code>null</code> be returned, then the real job departs, removing it from all internal queues,
   * setting the job's queue to <code>null</code>, and notifying listeners of the departure.
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
    update (time);
    if (hasServerAcccessCredits ())
    {
      // XXX
      takeServerAccessCredit (true);
      final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
      if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
        throw new IllegalArgumentException ();
      if (firstQueue != null)
        firstQueue.arrive (time, delegateJob);
      else
      {
        // We do not get a queue to arrive at.
        // So we depart; without having been executed!
        exitJobFromQueues (job, delegateJob);
        job.setQueue (null);
        fireDeparture (time, job, (Q) this);
      }
    }
  }

  /** Removes the real and delegate jobs from the internal administration.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    exitJobFromQueues (job, delegateJob);
    // XXX Should check for getQueue on delegateJob??
  }

  /** Empty, nothing to do.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    // EMPTY
  }

  /** Removes a job after successful revocation.
   * 
   * <p>
   * Checks if the delegate job can be revoked (if present at a queue);
   * returns <code>false</code> if not.
   * Otherwise, if the delegate job is not currently visiting a {@link SimQueue},
   * removes the real and delegate jobs from the internal administration.
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    final DJ delegateJob = getDelegateJob (job);
    final SimQueue queue = delegateJob.getQueue ();
    if (queue != null && ! queue.revoke (time, delegateJob, interruptService))
      return false;
    if (queue == null)
      exitJobFromQueues (job, delegateJob);
    return true;
  }

  /** Empty, nothing to do.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    // EMPTY
  }

  /** Schedules delegate jobs for arrival at their first queue until the
   * (new) server-access credits are exhausted.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    update (time);
    while (hasServerAcccessCredits ())
    {
      for (J realJob : this.jobQueue)
      {
        final DJ delegateJob = getDelegateJob (realJob);
        if (delegateJob.getQueue () == null)
        {
          // XXX (Almost) Verbatim copy from rescheduleAfterArrival...
          // XXX
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
            exitJobFromQueues (realJob, delegateJob);
            fireDeparture (time, realJob, (Q) this);
          }
        }
      }
      
    }
  }

  /** Throws {@link IllegalStateException}.
   * 
   * Should never be called.
   * 
   * @throws IllegalStateException
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * Should never be called.
   * 
   * @throws IllegalStateException
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueListener IMPLEMENTATION
  //
  // LISTENS TO ALL "SUBQUEUES"
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing; assumes will have been reset or will soon be reset as well.
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }
  
  /** Calls super method.
   * 
   */
  @Override
  public final void notifyUpdate (final double t, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalStateException ();
    super.update (t);
  }
  
  /** Calls super method.
   * 
   */
  @Override
  public final void notifyStateChanged (final double t, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalStateException ();
    super.stateChanged (t);
  }
  
  /** Checks if the job is a known delegate job, and calls {@link #update}.
   * 
   * <p>
   * This implementation does not allow the arrival of foreign delegate jobs.
   * 
   */
  @Override
  public final void notifyArrival (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    // NOTHING MORE TO DO.
  }
  
  /** Does nothing, called from {@link #notifyStart} for special treatment by subclasses.
   * 
   */
  protected void startForSubClass (final double t, final DJ job, final DQ queue)
  {
  }
  
  /** Notification of the start of a delegate job.
   * 
   * <p>
   * Calls {@link #update}.
   * If needed, fires a start event for the real job, and
   * puts that job in {@link #jobsInServiceArea}.
   * 
   * <p>
   * This implementation does not allow the start of foreign delegate jobs.
   * 
   * @see #fireStart
   * 
   */
  @Override
  public final void notifyStart (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    if (! this.jobsInServiceArea.contains (realJob))
    {
      this.jobsInServiceArea.add (realJob);
      fireStart (t, realJob, (Q) this);
      startForSubClass (t, job, queue);
    }
  }

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
   * @see #exitJobFromQueues
   * @see SimJob#setQueue
   * @see #fireDrop
   * 
   */
  @Override
  public final void notifyDrop (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    final DQ dropDestinationQueue = getDropDestinationQueue (t, job, queue);
    if (dropDestinationQueue != null)
    {
      if (! getQueues ().contains (dropDestinationQueue))
        throw new RuntimeException ();
      dropDestinationQueue.arrive (t, job);
    }
    else
    {
      exitJobFromQueues (realJob, job);
      realJob.setQueue (null);
      fireDrop (t, realJob, (Q) this);
    }
  }

  /** Returns whether revocations on delegate jobs are allowed.
   * 
   * <p>
   * The default implementation returns {@code false}.
   * 
   * <p>
   * By default, a {@link AbstractBlackSimQueueComposite} does not allow revocations of delegate jobs,
   * and it will throw an exception if it detects this.
   * By overriding this method and returning {@code true}, a subclass indicates that it uses
   * revocations on delegate jobs in order to meet its requirements,
   * and that relevant notifications are to be ignored.
   * 
   * <p>
   * We want to stress that this method concerns revocations of <i>delegate jobs</i> on <i>sub-queues</i>,
   * not on "real" jobs on this {@link AbstractBlackSimQueueComposite} itself.
   * 
   * @return Whether revocations on delegate jobs are allowed.
   * 
   * @see #notifyRevocation
   * 
   */
  protected boolean getAllowDelegateJobRevocations ()
  {
    return false;
  }
  
  /** Checks if the job is a known delegate job and revocations are allowed, and if so, calls {@link #update}.
   * 
   * <p>
   * Otherwise, this method throw an {@link IllegalStateException}.
   * 
   * <p>
   * This implementation does not allow the revocation of foreign delegate jobs.
   * 
   * @throws IllegalStateException If delegate-job revocations are not allowed.
   * 
   * @see #getAllowDelegateJobRevocations
   * 
   */
  @Override
  public final void notifyRevocation (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    if (getAllowDelegateJobRevocations ())
      update (t);
    else
      throw new IllegalStateException ();
  }

  /** Notification of the departure of a delegate job.
   * 
   * <p>
   * Calls {@link #update}.
   * Finds the next queue to visit by the delegate job.
   * If found, schedules the arrival of the delegate job at the next queue.
   * Otherwise, removes both real and delegate job,
   * reset the queue on the real job and fires a departure event.
   * 
   * @see #selectNextQueue
   * @see #fireDeparture
   * 
   */
  @Override
  public final void notifyDeparture (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    final SimQueue<DJ, DQ> nextQueue = selectNextQueue (t, realJob, queue);
    if (nextQueue == null)
    {
      exitJobFromQueues (realJob, job);
      realJob.setQueue (null);
      fireDeparture (t, realJob, (Q) this);  
    }
    else
      nextQueue.arrive (t, job);
  }

  /** Calls {@link #update} and {@link #reassessNoWaitArmed}.
   * 
   * <p>
   * May be overridden.
   *
   * @see #update
   * @see #reassessNoWaitArmed
   * @see #isNoWaitArmed
   * 
   */
  @Override
  public void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    update (time);
    reassessNoWaitArmed (time);
  }

  /** Returns whether (changes to) queue-access vacations are allowed on sub-queues.
   * 
   * <p>
   * The default implementation returns {@code false}.
   * 
   * <p>
   * By default, a {@link AbstractBlackSimQueueComposite} does not allow changes to the state of
   * queue-access vacations from a foreign entity, and it will throw an exception if it detects this.
   * By overriding this method and returning {@code true}, a subclass indicates that it uses
   * queue-access vacation on sub-queues in order to meet its requirements,
   * and that relevant notifications are to be ignored.
   * 
   * <p>
   * We want to stress that this method concerns queue-access vacations on <i>sub-queues</i>,
   * not on this {@link AbstractBlackSimQueueComposite} itself.
   * 
   * @return Whether (changes to) queue-access vacations are allowed on sub-queues.
   * 
   * @see #notifyStartQueueAccessVacation
   * @see #notifyStopQueueAccessVacation
   * 
   */
  protected boolean getAllowSubQueueAccessVacationChanges ()
  {
    return false;
  }
  
  /** Throws {@link IllegalStateException} unless (changes to) sub-queue-access vacations are allowed by the subclasses.
   * 
   * <p>
   * Otherwise, this implementation does nothing.
   * 
   * @throws IllegalStateException Unless (changes to) sub-queue-access vacations are allowed by the subclasses.
   * 
   * @see #getAllowSubQueueAccessVacationChanges
   * 
   */
  @Override
  public final void notifyStartQueueAccessVacation (final double time, final DQ queue)
  {
    if (! getAllowSubQueueAccessVacationChanges ())
      throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException} unless (changes to) sub-queue-access vacations are allowed by the subclasses.
   * 
   * <p>
   * Otherwise, this implementation does nothing.
   * 
   * @throws IllegalStateException Unless (changes to) sub-queue-access vacations are allowed by the subclasses.
   * 
   * @see #getAllowSubQueueAccessVacationChanges
   * 
   */
  @Override
  public final void notifyStopQueueAccessVacation (final double time, final DQ queue)
  {
    if (! getAllowSubQueueAccessVacationChanges ())
      throw new IllegalStateException ();
  }

  /** Does nothing.
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
    /* EMPTY */
  }

}
