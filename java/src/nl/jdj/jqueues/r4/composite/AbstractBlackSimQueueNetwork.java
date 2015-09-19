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

/**
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

  private final DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory;
  
  private final Map<J, DJ> delegateSimJobMap = new HashMap<> ();
  
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
  //
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
  //
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

  @Override
  protected void removeJobFromQueueUponDrop (J job, double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    exitJobFromQueues (job, delegateJob);
    // XXX Should check for getQueue on delegateJob??
  }

  @Override
  protected void rescheduleAfterDrop (J job, double time)
  {
    // EMPTY
  }

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

  @Override
  protected void rescheduleAfterRevokation (J job, double time)
  {
    // EMPTY
  }

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

  @Override
  protected void removeJobFromQueueUponDeparture (J departingJob, double time)
  {
    throw new IllegalStateException ();
  }

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

  @Override
  public final void update (double t, DQ queue)
  {
    if (queue == null || ! getQueues ().contains (queue))
      throw new IllegalStateException ();
    super.update (t);
  }
  
  @Override
  public final void arrival (double t, DJ job, DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    // NOTHING MORE TO DO.
  }
  
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
  
  private final DJ getDelegateJob (J realJob)
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

  private final void exitJobFromQueues (J realJob, DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsExecuting.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  @Override
  public final void drop (double t, DJ job, DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    exitJobFromQueues (realJob, job);
    fireDrop (t, realJob);
  }

  @Override
  public final void revocation (double t, DJ job, DQ queue)
  {
    final J realJob = getRealJob (job, queue);
    update (t);
    exitJobFromQueues (realJob, job);
    fireRevocation (t, realJob);
  }

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
