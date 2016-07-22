package nl.jdj.jqueues.r5.entity.queue.preemptive;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server preemptive Shortest-Remaining (Service) Time First (SRTF) queueing discipline.
 *
 * <p>
 * In SRTF, the job present (and admitted to the server in view of server-access credits) with
 * the minimum remaining service time is in service until completion.
 * In case of a tie between a job entering the service area and the job currently in service,
 * the job in service is <i>not</i> preempted.
 * In case of a tie between multiple jobs entering the service area simultaneously,
 * the jobs are served in arrival order.
 * 
 * <p>
 * This implementation admits waiting jobs to the service area (server) as soon as server-access credits are available,
 * irrespective of their remaining (i.c., required) service time.
 * Once admitted to the server, they wait until being served in the service area.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SRTF<J extends SimJob, Q extends SRTF>
extends AbstractPreemptiveSingleServerSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive SRTF queue given an event list and preemption strategy.
   *
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public SRTF (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public SRTF<J, Q> getCopySimQueue ()
  {
    return new SRTF<> (getEventList (), getPreemptionStrategy ());
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
  
  /** Returns <code>true</code>.
   * 
   * @return True.
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
  
  /** Inserts the job in the job queue maintaining non-decreasing (required) service-time ordering.
   * 
   * <p>
   * In case of ties, jobs are inserted in order of arrival.
   * 
   * @see #jobQueue
   * @see SimJob#getServiceTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    int newPosition = 0;
    while (newPosition < this.jobQueue.size ()
      && this.jobQueue.get (newPosition).getServiceTime (this) <= job.getServiceTime (this))
      newPosition++;
    this.jobQueue.add (newPosition, job);    
  }
  
  /** Does sanity checks and invokes {@link #rescheduleForNewServerAccessCredits} if there are server-access credits.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (hasServerAcccessCredits () && (getNumberOfJobsInWaitingArea () != 1))
        throw new IllegalStateException ();
    if (hasServerAcccessCredits ())
      rescheduleForNewServerAccessCredits (time);    
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
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    super.removeJobFromQueueUponRevokation (job, time);
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
  
  /** Starts as many eligible waiting jobs as possible, given the number of waiting jobs and the available
   *  server-access credits, and preempts the currently executing job if needed.
   * 
   * <p>
   * Jobs enter the service area in order of arrival,
   * and their remaining service time is taken from {@link SimJob#getServiceTime}.
   * If a job starts with remaining service time strictly smaller than
   * the job currently being served, the latter is preempted in favor of the former.
   * Jobs taken into execution with zero requested service time will depart immediately.
   * 
   * @see #hasJobsInWaitingArea
   * @see #hasServerAcccessCredits
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #preemptJob
   * @see #startServiceChunk
   * @see #fireStart
   * @see #fireDeparture
   * @see #fireIfNewServerAccessCreditsAvailability
   * @see #fireIfNewNoWaitArmed
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    final Set<J> departedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits ()
      && hasJobsInWaitingArea ())
    {
      takeServerAccessCredit (false);
      final J job = getFirstJobInWaitingArea ();
      if (job == null)
        throw new IllegalStateException ();
      startedJobs.add (job);
      this.jobsInServiceArea.add (job);
      final double jobServiceTime = job.getServiceTime (this);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      this.remainingServiceTime.put (job, jobServiceTime);
      if (jobServiceTime == 0.0)
      {
        removeJobFromQueueUponDeparture (job, time);
        job.setQueue (null);
        departedJobs.add (job);
      }
    }
    if (! getJobsInServiceArea ().isEmpty ())
    {
      J jobBeingServed = null;
      if (! this.jobsBeingServed.isEmpty ())
      {
        if (this.jobsBeingServed.size () > 1)
          throw new IllegalStateException ();
        jobBeingServed = this.jobsBeingServed.keySet ().iterator ().next ();
        final double newRemainingServiceTime
          = this.remainingServiceTime.get (jobBeingServed) - (time - this.jobsBeingServed.get (jobBeingServed));
        this.jobsBeingServed.put (jobBeingServed, time);
        this.remainingServiceTime.put (jobBeingServed, newRemainingServiceTime);
      }
      final double sRST = this.remainingServiceTime.firstValue ();
      final Set<J> jobsWithSRST = this.remainingServiceTime.getPreImageForValue (sRST);
      if (jobsWithSRST.isEmpty ())
        throw new IllegalStateException ();
      if (jobBeingServed == null || ! jobsWithSRST.contains (jobBeingServed))
      {
        if (jobBeingServed != null)
        {
          preemptJob (time, jobBeingServed);
          jobBeingServed = null;
        }
        startServiceChunk (time, jobsWithSRST.iterator ().next ());
      }
    }
    // Notification section.
    for (final J j : startedJobs)
      fireStart (time, j, (Q) this);
    for (final J j : departedJobs)
      fireDeparture (time, j, (Q) this);
    fireIfNewServerAccessCreditsAvailability (time);
    fireIfNewNoWaitArmed (time, isNoWaitArmed ());
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
  
  /** Removes the job from the internal data structures and takes another one into service if needed.
   * 
   * <p>
   * Removes any departure event for the job that leaves.
   * 
   * <p>
   * Central entry for jobs leaving the system,
   * either from the waiting area (drop/revocation) or from the service area
   * (revocation/departure).
   * 
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #getJobsBeingServed
   * @see #cancelDepartureEvent
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
    if (mustServeOther && ! this.jobsInServiceArea.isEmpty ())
    {
      final double sRST = this.remainingServiceTime.firstValue ();
      final Set<J> jobsWithSRST = this.remainingServiceTime.getPreImageForValue (sRST);
      if (jobsWithSRST.isEmpty ())
        throw new IllegalStateException ();
      startServiceChunk (time, jobsWithSRST.iterator ().next ());
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SRTF[preemption strategy]".
   * 
   * @return "SRTF[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SRTF[" + getPreemptionStrategy () + "]";
  }

}
