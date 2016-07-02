package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server Preemptive Last-Come First-Served (P_LCFS) queueing discipline.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class P_LCFS<J extends SimJob, Q extends P_LCFS>
extends AbstractPreemptiveSingleServerSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive LCFS queue given an event list and preemption strategy.
   *
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public P_LCFS (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link P_LCFS} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link P_LCFS} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public P_LCFS<J, Q> getCopySimQueue ()
  {
    return new P_LCFS<> (getEventList (), getPreemptionStrategy ());
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
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code true}.
   * 
   * @return {@code true}.
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
  
  /** Inserts the job at the head of the job queue.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (0, job);
  }

  /** Starts the job if there are server-access credits, preempting the currently executing job if needed.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see SimJob#getServiceTime
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #preemptJob
   * @see #startServiceChunk
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (hasServerAcccessCredits ())
    {
      // Scheduling section; make sure we do not issue notifications.
      takeServerAccessCredit (false);
      this.jobsInServiceArea.add (job);
      final double jobServiceTime = job.getServiceTime (this);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      this.remainingServiceTime.put (job, jobServiceTime);
      if (! this.jobsBeingServed.isEmpty ())
      {
        if (this.jobsBeingServed.size () > 1)
          throw new IllegalStateException ();
        preemptJob (time, this.jobsBeingServed.keySet ().iterator ().next ());
      }
      startServiceChunk (time, job);
      // Notification section.
      fireStart (time, job, (Q) this);
      fireIfOutOfServerAccessCredits (time);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    super.removeJobFromQueueUponDrop (job, time);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    return super.removeJobFromQueueUponRevokation (job, time, interruptService);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Schedules an eligible waiting job if possible, preempting the currently executing job if needed.
   * 
   * @see #hasServerAcccessCredits
   * @see #hasJobsWaitingInWaitingArea
   * @see #jobsBeingServed
   * @see #jobQueue
   * @see #takeServerAccessCredit
   * @see #jobsInServiceArea
   * @see SimJob#getServiceTime
   * @see #remainingServiceTime
   * @see #preemptJob
   * @see #startServiceChunk
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    if (hasServerAcccessCredits ()
      && hasJobsWaitingInWaitingArea ())
    {
      // Scheduling section; make sure we do not issue notifications.
      // Get the latest arrival in the waiting area.
      final J youngestWaiter = getFirstJobInWaitingArea ();
      if (youngestWaiter == null)
        throw new IllegalStateException ();
      // Get the job currently being served; if any.
      final J jobBeingServed;
      if (! this.jobsBeingServed.isEmpty ())
      {
        if (this.jobsBeingServed.size () > 1)
          throw new IllegalStateException ();
        jobBeingServed = this.jobsBeingServed.keySet ().iterator ().next ();
      }
      else
        jobBeingServed = null;
      // Check whether to start the youngest waiter, noting that this.jobQueue is ordered decreasing in arrival time.
      if (jobBeingServed == null || this.jobQueue.indexOf (youngestWaiter) < this.jobQueue.indexOf (jobBeingServed))
      {
        takeServerAccessCredit (false);      
        this.jobsInServiceArea.add (youngestWaiter);
        final double jobServiceTime = youngestWaiter.getServiceTime (this);
        if (jobServiceTime < 0)
          throw new RuntimeException ();
        this.remainingServiceTime.put (youngestWaiter, jobServiceTime);
        if (jobBeingServed != null)
          preemptJob (time, jobBeingServed);
        startServiceChunk (time, youngestWaiter);
        // Notification section.
        fireStart (time, youngestWaiter, (Q) this);
        fireIfOutOfServerAccessCredits (time);        
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    super.removeJobFromQueueUponDeparture (departingJob, time);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXIT (DEPARTURE / DROP / REVOKATION)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from internal administration, and starts a service chunk for another job (if any, and if possible).
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #hasServerAcccessCredits
   * @see #getFirstJobInWaitingArea
   * @see #getFirstJobInServiceArea
   * @see #rescheduleForNewServerAccessCredits
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit (final J exitingJob, final double time)
  {
    if (exitingJob == null || ! this.jobQueue.contains (exitingJob))
      throw new IllegalArgumentException ();
    boolean mustServeOther = false;
    if (this.jobsInServiceArea.contains (exitingJob))
    {
      if (! this.remainingServiceTime.containsKey (exitingJob))
        throw new IllegalStateException ();
      this.remainingServiceTime.remove (exitingJob);
      if (this.jobsBeingServed.containsKey (exitingJob))
      {
        mustServeOther = true;
        // Note: getDepartureEvents requires its argument to be present in this.jobQueue!
        if (! getDepartureEvents (exitingJob).isEmpty ())
        {
          if (getDepartureEvents (exitingJob).size () > 1)
            throw new IllegalStateException ();
          cancelDepartureEvent (exitingJob);
        }
        this.jobsBeingServed.remove (exitingJob);
      }
      this.jobsInServiceArea.remove (exitingJob);
    }
    this.jobQueue.remove (exitingJob);
    if (mustServeOther)
    {
      if (! this.jobsBeingServed.isEmpty ())
        throw new IllegalStateException ();
      // Find the youngest eligible waiter in the waiting area (may be null).
      final J youngestWaiter = (hasServerAcccessCredits () ? getFirstJobInWaitingArea () : null);
      // Find the youngest job in the service area (may be null).
      final J youngestServed = getFirstJobInServiceArea ();
      // Check whether to start the youngest waiter.
      if (youngestWaiter != null
        && (youngestServed == null || this.jobQueue.indexOf (youngestWaiter) < this.jobQueue.indexOf (youngestServed)))
      {
        // Rely on new server-access credits for getting the 'start stuff' right.
        // It SHOULD start youngestWaiter.
        rescheduleForNewServerAccessCredits (time);
        // But check our a-priori assumption.
        if (this.jobsBeingServed.size () != 1 || ! this.jobsBeingServed.containsKey (youngestWaiter))
          throw new IllegalStateException ();
      }
      // If not, check whether to resume the youngestServed.
      else if (youngestServed != null)
        startServiceChunk (time, youngestServed);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "P_LCFS[preemption strategy]".
   * 
   * @return "P_LCFS[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "P_LCFS[" + getPreemptionStrategy () + "]";
  }

}
