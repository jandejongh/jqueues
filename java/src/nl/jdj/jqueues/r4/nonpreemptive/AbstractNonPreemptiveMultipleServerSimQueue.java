package nl.jdj.jqueues.r4.nonpreemptive;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** An abstract base class for non-preemptive multiple-server queueing disciplines
 *  for {@link SimJob}s.
 *
 * The class supports job revocations.
 * 
 * <p>This abstract class relies heavily on the partial {@link SimQueue} implementation of {@link AbstractNonPreemptiveSimQueue}.
 * This class {@link AbstractNonPreemptiveMultipleServerSimQueue} implements most remaining abstract methods of
 * {@link AbstractNonPreemptiveSimQueue},
 * and takes care of maintenance of the internal data structures {@link AbstractSimQueue#jobQueue} and
 * {@link AbstractSimQueue#jobsExecuting}, by automatically taking into service the first job(s) in {@link AbstractSimQueue#jobQueue}
 * and serve them until completion repeatedly.
 * Concrete implementations only have to insert an arriving job into {@link AbstractSimQueue#jobQueue} by
 * implementing {@link #insertJobInQueueUponArrival}.
 * Note that this restricts the use of this class to those non-preemptive multiple-server queueing disciplines
 * in which the order of service of jobs can only change due to job arrivals.
 * It is legal to permute <i>other</i> jobs than the arriving job in {@link AbstractSimQueue#jobQueue}.
 * If this is required, implementers should be aware that in most cases, some of the jobs in
 * the list are currently being served. Moving such jobs in the list is allowed, but it will have no effect.
 * 
 * <p>All concrete subclasses of {@link AbstractNonPreemptiveMultipleServerSimQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 *
 * <p>
 * The <code>noWaitArmed</code> state to any implementation is / must be equivalent
 * to strictly fewer jobs than servers being present in the system,
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * @see SimEventList#run
 * 
 */
public abstract class AbstractNonPreemptiveMultipleServerSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveMultipleServerSimQueue>
  extends AbstractNonPreemptiveSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  /** The number of servers, non-negative.
   * 
   */
  private final int numberOfServers;
  
  /** Returns the number of servers (non-negative).
   * 
   * @return The number of servers, non-negative.
   * 
   */
  public final int getNumberOfServers ()
  {
    return this.numberOfServers;
  }
  
  /** Creates a non-preemptive multi-server queue given an event list.
   *
   * @param eventList The event list to use.
   * @param numberOfServers The number of servers (non-negative).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code> or the number of servers is negative.
   *
   */
  protected AbstractNonPreemptiveMultipleServerSimQueue (final SimEventList eventList, final int numberOfServers)
  {
    super (eventList);
    if (numberOfServers < 0)
      throw new IllegalArgumentException ();
    this.numberOfServers= numberOfServers;
  }

  /** Returns true if there are strictly fewer jobs than servers present in the system.
   * 
   * {@inheritDoc}
   * 
   * @return True if there are strictly fewer jobs than servers present in the system.
   * 
   * @see #getNumberOfJobs
   * @see #getNumberOfServers
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getNumberOfJobs () < this.getNumberOfServers ();
  }

  /** Invokes {@link #rescheduleAfterDeparture} passing <code>null</code> as job argument.
   * 
   * <p>
   * {@inheritDoc}
   * 
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsExecuting.contains (job))
      throw new IllegalStateException ();
    rescheduleAfterDeparture (null, time);
  }
    
  /** Invokes {@link #rescheduleAfterDeparture} passing <code>null</code> as job argument.
   * 
   * <p>
   * {@inheritDoc}
   * 
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    rescheduleAfterDeparture (null, time);
  }

  /** Invokes {@link #rescheduleAfterDeparture} passing revoked job as argument.
   * 
   * <p>
   * {@inheritDoc}
   * 
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (this.jobQueue.contains (job) || this.jobsExecuting.contains (job))
      throw new IllegalStateException ();
    rescheduleAfterDeparture (job, time);
  }

  /** Takes as many jobs into service as there are servers idle and server-access credits available.
   * 
   * <p>
   * {@inheritDoc}
   * 
   * <p>
   * In the current implementation, this method serves as the central point for rescheduling,
   * including after arrivals, revocations and new server-access credits.
   * 
   * <p>
   * If there are server-access credits, at least one jobs waiting, and at least one server available,
   * one credit is taken, the first waiting job in {@link #jobQueue} is selected for service.
   * ,The job's service time is requested through
   * {@link SimJob#getServiceTime}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent},
   * but no start notifications are sent at this point.
   * These actions are repeated until there are
   * no more jobs to start,
   * no more servers available,
   * or no more server-access credits.
   * 
   * <p>Subsequently, listeners are notified through {@link #fireStart} for all jobs that just started
   * (and still present as jobs may have left due to previous notifications).
   * 
   * <p>
   * Finally, if a job really left the queueing system (i.e., <code>departedJob != null</code>),
   * or if we previously started any job,
   * the (new) <code>noWaitArmed</code> is reassessed and fired.
   * 
   * @see #jobQueue
   * @see #getNumberOfJobs
   * @see #jobsExecuting
   * @see #getNumberOfJobsExecuting
   * @see #getFirstJobWaiting
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see SimJob#getServiceTime
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * @see #fireNewNoWaitArmed
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture
    (final J departedJob, final double time)
  {
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits ()
      && hasJobsWaiting ()
      && getNumberOfJobsExecuting () < getNumberOfServers ())
    {
      takeServerAccessCredit (false);
      final J job = getFirstJobWaiting ();
      if (job == null)
        throw new IllegalStateException ();
      this.jobsExecuting.add (job);
      final double jobServiceTime = job.getServiceTime (this);
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
        fireStart (time, j);
    fireIfOutOfServerAccessCredits (time);
    if (departedJob != null || ! startedJobs.isEmpty ())
      fireNewNoWaitArmed (time, isNoWaitArmed ());
  }

}
