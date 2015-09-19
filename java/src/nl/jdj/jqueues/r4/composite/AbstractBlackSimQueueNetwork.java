package nl.jdj.jqueues.r4.composite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.SimQueueListener;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a {@link BlackSimQueueNetwork}.
 *
 * Implementations only have to route a job (actually, its delegate job) through the
 * internal network of {@link SimQueue}s,
 * see {@link #getFirstQueue} and {@link #getNextQueue}.
 * 
 * <p>
 * This allows for many types of queueing networks, even including "feedback"-type networks.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public abstract class AbstractBlackSimQueueNetwork
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractSimQueue>
extends AbstractSimQueue<J, Q>
implements BlackSimQueueNetwork<DJ, DQ, J, Q>,
  SimQueueListener<DJ, DQ>
{
  
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
  (SimEventList eventList, Set<DQ> queues, DelegateSimJobFactory delegateSimJobFactory)
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
  // AbstractSimQueue.reset
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public void reset ()
  {
    super.reset ();
    this.delegateSimJobMap.clear ();
    this.realSimJobMap.clear ();
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
  protected final void insertJobInQueueUponArrival (J job, double time)
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
    final DJ delegateSimJob = this.delegateSimJobFactory.newInstance (time, job);
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
   * Should <code>null</code> be returned, then the real job departs.
   * 
   */
  @Override
  protected void rescheduleAfterArrival (J job, double time)
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
      takeServerAccessCredit ();
      final SimQueue<DJ, DQ> firstQueue = getFirstQueue (time, job);
      if (firstQueue != null && ! getQueues ().contains (firstQueue))
        throw new IllegalArgumentException ();
      if (firstQueue != null)
        firstQueue.arrive (delegateJob, time);
      else
      {
        // We do not get a queue to arrive at.
        // So we depart; without having been executed!
        exitJobFromQueues (job, delegateJob);
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
  protected void removeJobFromQueueUponDrop (J job, double time)
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
  protected void rescheduleAfterDrop (J job, double time)
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
  protected boolean removeJobFromQueueUponRevokation (J job, double time, boolean interruptService)
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
  protected void rescheduleAfterRevokation (J job, double time)
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
  protected void rescheduleForNewServerAccessCredits (double time)
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
          takeServerAccessCredit ();
          final SimQueue<DJ, DQ> firstQueue = getFirstQueue (time, realJob);
          if (firstQueue != null && ! getQueues ().contains (firstQueue))
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
  protected void removeJobFromQueueUponDeparture (J departingJob, double time)
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
  protected void rescheduleAfterDeparture (J departedJob, double time)
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
   * Calls super method.
   * 
   */
  @Override
  public final void update (double t, DQ queue)
  {
    if (queue == null || ! getQueues ().contains (queue))
      throw new IllegalStateException ();
    super.update (t);
  }
  
  /**
   * {@inheritDoc}
   * 
   * Checks if the job is a known delegate job, and calls {@link #update}.
   * 
   */
  @Override
  public final void arrival (double t, DJ job, DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    // NOTHING MORE TO DO.
  }
  
  /**
   * {@inheritDoc}
   * 
   * Calls {@link #update}.
   * If needed, fires a start event for the real job, and
   * puts that job in {@link #jobsExecuting}.
   * 
   * @see #fireStart
   * 
   */
  @Override
  public final void start (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    if (! this.jobsExecuting.contains (realJob))
    {
      this.jobsExecuting.add (realJob);
      // Use a separate event for start notifications,
      // because the start at the delegate queue hasn't completed yet (e.g., in terms of notifications).
      // fireStart (t, realJob);
      getEventList ().add (new SimEvent (t, null, new SimEventAction ()
      {
        @Override
        public void action (SimEvent event)
        {
          fireStart (t, realJob);
        }
      }
      ));
      
    }
  }

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
  protected final DJ getDelegateJob (J realJob)
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
  private final J getRealJob (DJ delegateJob, DQ queue)
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
  private final void exitJobFromQueues (J realJob, DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsExecuting.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  /**
   * {@inheritDoc}
   * 
   * Calls {@link #update}.
   * Gets the real job and drops it as well.
   * 
   * @see #fireDrop
   * 
   */
  @Override
  public final void drop (double t, DJ job, DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    exitJobFromQueues (realJob, job);
    fireDrop (t, realJob);
  }

  /**
   * {@inheritDoc}
   * 
   * Calls {@link #update}.
   * Gets the real job and revokes it as well.
   * 
   * @see #fireRevocation
   * 
   */
  @Override
  public final void revocation (double t, DJ job, DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    exitJobFromQueues (realJob, job);
    fireRevocation (t, realJob);
  }

  /**
   * {@inheritDoc}
   * 
   * Calls {@link #update}.
   * 
   * Finds the next queue to visit by the delegate job.
   * If found, schedules the arrival of the delegate job at the next queue.
   * Otherwise, removes both real and delegate jobs and fires a departure event.
   * 
   * @see #getNextQueue
   * @see #fireDeparture
   * 
   */
  @Override
  public final void departure (final double t, final DJ job, final DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    final SimQueue<DJ, DQ> nextQueue = getNextQueue (t, realJob, queue);
    if (nextQueue == null)
    {
      exitJobFromQueues (realJob, job);
      fireDeparture (t, realJob);  
    }
    else
      // Use a separate event for arrival at nextQueue,
      // because the departure at the current queue hasn't completed yet (e.g., in terms of notifications).
      // nextQueue.arrive (job, t);
      getEventList ().add (new SimEvent (t, null, new SimEventAction ()
      {
        @Override
        public void action (SimEvent event)
        {
          nextQueue.arrive (job, t);
        }
      }
      ));
  }

}
