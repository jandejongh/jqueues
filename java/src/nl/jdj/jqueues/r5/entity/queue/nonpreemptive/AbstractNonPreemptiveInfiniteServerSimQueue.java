package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a non-preemptive queueing system with an infinite number of servers.
 *
 * <p>Concrete implementations only have to provide the service-time for a job at the moment it is taken into service,
 * by implementing {@link #getServiceTime}. It is not possible to bypass the service phase of a job visit in implementations.
 * 
 * <p>
 * In the presence of vacations, i.e., jobs are not immediately admitted to the servers,
 * this implementation respects the arrival order of jobs.
 * 
 * <p>In the absence of queue-access and server-access vacations,
 * implementations must start serving an arriving job immediately,
 * in other words, the implementation must assure that the queue is always in <code>noWaitArmed</code> state;
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractNonPreemptiveInfiniteServerSimQueue
<J extends SimJob, Q extends AbstractNonPreemptiveInfiniteServerSimQueue>
extends AbstractNonPreemptiveSimQueue<J, Q>
{

  /** Creates a new {@link AbstractNonPreemptiveInfiniteServerSimQueue} with given {@link SimEventList}.
   * 
   * @param eventList The event list to use.
   * 
   */
  protected AbstractNonPreemptiveInfiniteServerSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns the service time for a job at this {@link AbstractNonPreemptiveInfiniteServerSimQueue}.
   * 
   * Implementations must always return a non-negative value.
   * Otherwise, a {@link RuntimeException} is thrown in {@link #rescheduleAfterArrival}.
   * 
   * <p>
   * Implementations are free to <i>not</i> use {@link SimJob#getServiceTime}.
   * 
   * @param job The job, non-<code>null</code>.
   * @return The required service time, non-negative.
   * 
   */
  protected abstract double getServiceTime (J job);

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
  
  /** Inserts the job at the tail of {@link #jobQueue}.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Takes the arrived job into service immediately, provided there are server-access credits.
   * 
   * <p>
   * If server-access credits are present, one credit is taken and the job's service time is requested through
   * {@link #getServiceTime}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent},
   * and listeners are notified through {@link #fireStart}.
   * 
   * <p>
   * If server-access credits are absent, this method does nothing, relying on {@link #rescheduleForNewServerAccessCredits}
   * to eventually take the job into service.
   * 
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #jobsExecuting
   * @see #getServiceTime
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (hasServerAcccessCredits ())
    {
      // XXX
      takeServerAccessCredit (true);
      this.jobsExecuting.add (job);
      final double jobServiceTime = getServiceTime (job);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      scheduleDepartureEvent (time + jobServiceTime, job);
      fireStart (time, job, (Q) this);
    }
  }

  /** Repeatedly starts jobs waiting as long as there are server-access credits.
   * 
   * <p>
   * As long as there are service-access credits,
   * jobs are started one at a time, as if they are new arrivals, through {@link #rescheduleAfterArrival}.
   * Jobs are started in order of appearance in an {@link Iterator} over {@link #jobQueue}.
   * 
   * @see #jobQueue
   * @see #jobsExecuting
   * @see #hasServerAcccessCredits
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits ()
      && hasJobsWaiting ())
    {
      takeServerAccessCredit (false);
      final J job = getFirstJobWaiting ();
      if (job == null)
        throw new IllegalStateException ();
      this.jobsExecuting.add (job);
      final double jobServiceTime = getServiceTime (job);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      scheduleDepartureEvent (time + jobServiceTime, job);
      // Defer notifications until we are in a valid state again.
      startedJobs.add (job);
    }
    // Notification section.
    for (J j : startedJobs)
      // Be cautious here; previous invocation(s) of fireStart could have removed the job j already!
      if (this.jobsExecuting.contains (j))
        fireStart (time, j, (Q) this);
    fireIfOutOfServerAccessCredits (time);
    // XXX May want to check this...
    fireNewNoWaitArmed (time, isNoWaitArmed ());
  }

  /** Does nothing.
   * 
   * No rescheduling is required after a successful revocation.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   * No rescheduling is required after a departure.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

}