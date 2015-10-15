package nl.jdj.jqueues.r4.composite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.SimQueueListener;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a {@link BlackSimQueueNetwork}.
 *
 * Implementations only have to route a job (actually, its delegate job) through the
 * internal network of {@link SimQueue}s,
 * see {@link #getFirstQueue} and {@link #getNextQueue}.
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
public abstract class AbstractBlackSimQueueNetwork
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackSimQueueNetwork>
extends AbstractSimQueue<J, Q>
implements BlackSimQueueNetwork<DJ, DQ, J, Q>,
  SimQueueListener<DJ, DQ>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (SUB)QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queues, all non-null.
   * 
   * Set is also non-null and final.
   * 
   */
  private final Set<DQ> queues;
  
  @Override
  public final Set<DQ> getQueues ()
  {
    return this.queues;
  }

  /** Returns the index of given sub-queue.
   * 
   * @param queue The sub-queue; must be present in {@link #getQueues}.
   * 
   * @return The index of the sub-queue in {@link #getQueues}.
   * 
   * @throws IllegalArgumentException If the <code>queue</code> is <code>null</code> or not present in {@link #getQueues}.
   * 
   */
  protected final int getIndex (final DQ queue)
  {
    if (queue == null || getQueues () == null || ! getQueues ().contains (queue))
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = getQueues ().iterator ();
    for (int q = 0; q < getQueues ().size (); q++)
      if (iterator.next () == queue)
        return q;
    throw new RuntimeException ();
  }
  
  /** Returns a sub-queue by its index.
   * 
   * @param q The index.
   * 
   * @return The (sub-)queue in {@link #getQueues} with given index.
   * 
   * @throws IllegalArgumentException If the index is (strictly) negative or larger or equal than the size of {@link #getQueues}.
   * 
   */
  protected final DQ getQueue (final int q)
  {
    if (q < 0 || q >= getQueues ().size ())
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = getQueues ().iterator ();
    int i = 0;
    DQ dq = iterator.next ();
    while (i < q)
    {
      i++;
      dq = iterator.next ();
    }
    return dq;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELEGATE SIMJOBS AND REAL/DELEGATE SIMJOB MAPPINGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   */
  private final DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory;
  
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
    this.jobsExecuting.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ABSTRACT METHODS FOR (SUB-)QUEUE SELECTION IN SUBCLASSES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the first queue to visit for an arriving job.
   * 
   * @param time The time of arrival of the job.
   * @param job The job, non-<code>null</code>.
   * 
   * @return The first queue to visit, if <code>null</code>, the job is to depart from this {@link AbstractBlackSimQueueNetwork}.
   * 
   */
  protected abstract SimQueue<DJ, DQ> getFirstQueue (double time, J job);
  
  /** Returns the next queue to visit for a job.
   * 
   * @param time The current time, i.e., the departure time of the job at its previous queue.
   * @param job The job, non-<code>null</code>.
   * @param previousQueue The previous queue the job visited, and just departed from.
   * 
   * @return The next queue to visit, if <code>null</code>, the job is to depart from this {@link AbstractBlackSimQueueNetwork}.
   * 
   */
  protected abstract SimQueue<DJ, DQ> getNextQueue (double time, J job, DQ previousQueue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract black network of queues.
   * 
   * @param eventList The event list to be shared between this queue and the inner queues.
   * @param queues A set holding the "inner" queues.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * @throws IllegalArgumentException If the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  protected AbstractBlackSimQueueNetwork
  (final SimEventList eventList, final Set<DQ> queues, final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList);
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    this.queues = queues;
    for (DQ queue : this.queues)
      queue.registerQueueListener (this);
    this.delegateSimJobFactory = ((delegateSimJobFactory == null) ? new DefaultDelegateSimJobFactory () : delegateSimJobFactory);
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
   * {@inheritDoc}
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
  // AbstractSimQueue.reset
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method,
   * clears the internal mapping between real and delegate {@link SimJob}s,
   * and clears the internal cache of the <code>noWaitArmed</code> state.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public void reset ()
  {
    super.reset ();
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

  /**
   * {@inheritDoc}
   * 
   * Creates the delegate job, administers it and puts the job into {@link #jobQueue}.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (job) || this.jobsExecuting.contains (job))
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

  /**
   * {@inheritDoc}
   * 
   * Checks the server-access credits and if passed,
   * lets the delegate job arrive at the queue returned by
   * {@link #getFirstQueue}.
   * Should <code>null</code> be returned, then the real job departs, removing it from all internal queues,
   * setting the job's queue to <code>null</code>, and notifying listeners of the departure.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsExecuting.contains (job))
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
      final SimQueue<DJ, DQ> firstQueue = getFirstQueue (time, job);
      if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
        throw new IllegalArgumentException ();
      if (firstQueue != null)
        firstQueue.arrive (delegateJob, time);
      else
      {
        // We do not get a queue to arrive at.
        // So we depart; without having been executed!
        exitJobFromQueues (job, delegateJob);
        job.setQueue (null);
        fireDeparture (time, job);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * Removes the real and delegate jobs from the internal administration.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    exitJobFromQueues (job, delegateJob);
    // XXX Should check for getQueue on delegateJob??
  }

  /**
   * {@inheritDoc}
   * 
   * Empty, nothing to do.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    // EMPTY
  }

  /**
   * {@inheritDoc}
   * 
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
    if (queue != null && ! queue.revoke (delegateJob, time, interruptService))
      return false;
    if (queue == null)
      exitJobFromQueues (job, delegateJob);
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * Empty, nothing to do.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    // EMPTY
  }

  /**
   * {@inheritDoc}
   * 
   * Schedules delegate jobs for arrival at their first queue until the
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
          final SimQueue<DJ, DQ> firstQueue = getFirstQueue (time, realJob);
          if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
            throw new IllegalArgumentException ();
          if (firstQueue != null)
            firstQueue.arrive (delegateJob, time);
          else
          {
            // We do not get a queue to arrive at.
            // So we depart; without having been executed!
            exitJobFromQueues (realJob, delegateJob);
            fireDeparture (time, realJob);
          }
        }
      }
      
    }
  }

  /**
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
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
  // THIS AbstractBlackSimQueueNetwork LISTENS TO ALL "SUBQUEUES" IN getQueues ().
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * {@inheritDoc}
   * 
   * Does nothing; assumes will have been reset or will soon be reset as well.
   * 
   */
  @Override
  public final void notifyReset (final double oldTime, final DQ queue)
  {
  }
  
  /**
   * {@inheritDoc}
   * 
   * Calls super method.
   * 
   */
  @Override
  public final void notifyUpdate (final double t, final DQ queue)
  {
    if (queue == null || ! getQueues ().contains (queue))
      throw new IllegalStateException ();
    super.update (t);
  }
  
  /**
   * {@inheritDoc}
   * 
   * Checks if the job is a known delegate job, and calls {@link #notifyUpdate}.
   * 
   */
  @Override
  public final void notifyArrival (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    // NOTHING MORE TO DO.
  }
  
  /** *  Does nothing, called from {@link #notifyStart} for special treatment by subclasses.
   * 
   */
  protected void startForSubClass (final double t, final DJ job, final DQ queue)
  {
  }
  
  /**
   * {@inheritDoc}
   * 
   * Calls {@link #notifyUpdate}.
   * If needed, fires a start event for the real job, and
   * puts that job in {@link #jobsExecuting}.
   * 
   * @see #fireStart
   * 
   */
  @Override
  public final void notifyStart (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    if (! this.jobsExecuting.contains (realJob))
    {
      this.jobsExecuting.add (realJob);
      // Use a separate event for start notifications,
      // because the start at the delegate queue hasn't completed yet (e.g., in terms of notifications).
      fireStart (t, realJob);
      startForSubClass (t, job, queue);
      //getEventList ().add (new SimEvent (t, null, new SimEventAction ()
      //{
      //  @Override
      //  public void action (SimEvent event)
      //  {
      //    fireStart (t, realJob);
      //    AbstractBlackSimQueueNetwork.this.startForSubClass (t, job, queue);
      //  }
      //}
      //));
      
    }
  }

  /**
   * {@inheritDoc}
   * 
   * Calls {@link #notifyUpdate}.
   * Gets the real job and drops it as well.
   * 
   * @see #fireDrop
   * 
   */
  @Override
  public final void notifyDrop (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    exitJobFromQueues (realJob, job);
    fireDrop (t, realJob);
  }

  /** *  Checks if the job is a known delegate job, and calls {@link #notifyUpdate}.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void notifyRevocation (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    // NOTHING MORE TO DO.
  }

  /**
   * {@inheritDoc}
   * 
   * Calls {@link #notifyUpdate}.
   * 
   * Finds the next queue to visit by the delegate job.
   * If found, schedules the arrival of the delegate job at the next queue.
   * Otherwise, removes both real and delegate job,
   * reset the queue on the real job and fires a departure event.
   * 
   * @see #getNextQueue
   * @see #fireDeparture
   * 
   */
  @Override
  public final void notifyDeparture (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    final SimQueue<DJ, DQ> nextQueue = getNextQueue (t, realJob, queue);
    if (nextQueue == null)
    {
      exitJobFromQueues (realJob, job);
      realJob.setQueue (null);
      fireDeparture (t, realJob);  
    }
    else
      // OLD CODE FRAGMENT:
      // Use a separate event for arrival at nextQueue,
      // because the departure at the current queue hasn't completed yet (e.g., in terms of notifications).
      // getEventList ().add (new SimEvent (t, null, new SimEventAction ()
      // {
      //   @Override
      //   public void action (SimEvent event)
      //   {
      //     nextQueue.arrive (job, t);
      //   }
      // }
      // ));
      // END OLD CODE FRAGMENT.
      // Directly invoke arrive on the destination queue instead of event-list scheduling,
      // otherwise we may expose an inconsistent queue state in which the job has departed from DQ,
      // but is not yet at nextQueue.
      // We must accept possible out-of-sequence notifications from the sub-queues here;
      // there seems to be no ideal solution.
      nextQueue.arrive (job, t);
  }

  /**
   * {@inheritDoc}
   * 
   * Calls {@link #notifyUpdate} and {@link #reassessNoWaitArmed}.
   * May be overridden.
   *
   * @see #notifyUpdate
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

}
