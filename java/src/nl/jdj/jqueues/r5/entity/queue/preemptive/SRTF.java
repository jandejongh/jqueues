package nl.jdj.jqueues.r5.entity.queue.preemptive;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server preemptive Shortest-Remaining (Service) Time First (SRTF) queueing discipline.
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
  // CONSTRUCTOR(S) / FACTORY
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
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SRTF[preemption strategy]".
   * 
   * @return "SRTF[preemption strategy]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "SRTF[" + getPreemptionStrategy () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code true} if there are no jobs in the system.
   * 
   * @return {@code true} if there are no jobs in the system.
   * 
   * @see #getNumberOfJobs
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getNumberOfJobs () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
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

  /** Starts as many jobs as possible, given the number of waiting jobs and the available
   *  server-access credits, and preempts the currently executing job if needed.
   * 
   * <p>
   * Jobs enter the service area in order of arrival,
   * and their remaining service time is taken from {@link SimJob#getServiceTime}.
   * If a job starts with remaining service time strictly smaller than
   * the job currently being served, the latter is preempted in favor of the former.
   * 
   * @see #hasJobsWaitingInWaitingArea
   * @see #hasServerAcccessCredits
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #preemptJob
   * @see #startServiceChunk
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits ()
      && hasJobsWaitingInWaitingArea ())
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
      // Be cautious here; previous invocation(s) of fireStart could have removed the job j already!
      if (this.jobsInServiceArea.contains (j))
        fireStart (time, j, (Q) this);
    fireIfOutOfServerAccessCredits (time);
  }

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
  
  /** Removes the job from the internal data structures and takes another one into service if needed.
   * 
   * <p>
   * Removes any {@link DefaultDepartureEvent} for the job that leaves.
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
  
}
