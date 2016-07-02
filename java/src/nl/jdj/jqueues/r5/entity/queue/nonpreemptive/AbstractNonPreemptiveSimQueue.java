package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for non-preemptive queueing disciplines
 *  for {@link SimJob}s.
 *
 * <p>
 * The class fully supports job revocations.
 * 
 * <p>
 * This abstract class relies heavily on the partial {@link SimQueue} implementation of {@link AbstractSimQueue}.
 * It implements (and often finalizes) those abstract methods of {@link AbstractSimQueue} that
 * do not depend on the service structure, apart from it being non-preemptive.
 * In particular, these methods do not depend on the number of servers in the queueing system.
 * 
 * <p>
 * Concrete implementations <i>must</i> implement {@link #insertJobInQueueUponArrival}, {@link #hasServerAvailable}
 * and {@link #getCopySimQueue}.
 * Optionally, they may override {@link #getServiceTimeForJob} and {@link #toStringDefault}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see AbstractNonPreemptiveSingleServerSimQueue
 * @see AbstractNonPreemptiveFiniteServerSimQueue
 * @see AbstractNonPreemptiveInfiniteServerSimQueue
 * 
 */
public abstract class AbstractNonPreemptiveSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveSimQueue>
  extends AbstractSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  protected AbstractNonPreemptiveSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the service time for a job at this queue.
   * 
   * <p>
   * Implementations must always return a non-negative value.
   * Otherwise, a {@link RuntimeException} is thrown in {@link #reschedule}.
   * 
   * <p>
   * The default implementation uses {@link SimJob#getServiceTime}, but this may be overridden.
   * Note that implementations must return the same value for the service time during a <i>single</i> visit.
   * 
   * @param job The job, non-<code>null</code>.
   * 
   * @return The required service time, non-negative.
   * 
   */
  protected double getServiceTimeForJob (final J job)
  {
    return job.getServiceTime (this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER AVAILABLE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Checks whether at least one server is available (idle),
   *  in other words, whether a waiting job can be admitted to the service area.
   * 
   * <p>
   * Implementations must only check the state of the service area; the result returned should <i>not</i>
   * depend on the availability of server-access credits, start-able jobs, waiting jobs, queue-access vacations, etc.
   * 
   * @return True if at least one server is available (idle).
   * 
   */
  protected abstract boolean hasServerAvailable ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@link #hasServerAvailable}.
   * 
   * @return {@link #hasServerAvailable}.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return hasServerAvailable ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Invokes {@link #reschedule} passing the time argument.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #removeJobFromQueueUponRevokation}, requesting <code>interruptService</code>.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time, true);
  }
  
  /** Invokes {@link #reschedule} passing the time argument.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    reschedule (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** If possible, removes the job from the internal data structures, and cancels a pending departure event.
   * 
   * If the job is already in service, and the <code>interruptService</code> argument is set to <code>false</code>,
   * this method returns <code>false</code>, by contract of {@link SimQueue}.
   * Otherwise, if the job is in service, its departure event is canceled through {@link #cancelDepartureEvent},
   * and the job is removed from {@link #jobsInServiceArea} and {@link #jobQueue}.
   * Subsequently, whether the job was in service or not, it is removed from {@link #jobQueue},
   * and <code>true</code> is returned.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
    {
      if (interruptService)
      {
        this.jobsInServiceArea.remove (job);
        cancelDepartureEvent (job);
      }
      else
        return false;
    }
    this.jobQueue.remove (job);
    return true;
  }

  /** Invokes {@link #reschedule} passing the time argument.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #reschedule} passing the time argument.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Checks the presence of the departing job in {@link #jobQueue} and {@link #jobsInServiceArea},
   *  and removes the job from those lists.
   * 
   * @throws IllegalStateException If the <code>departingJob</code> is not in one of the lists.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (! this.jobQueue.contains (departingJob))
      throw new IllegalStateException ();
    if (! this.jobsInServiceArea.contains (departingJob))
      throw new IllegalStateException ();
    this.jobQueue.remove (departingJob);
    this.jobsInServiceArea.remove (departingJob);
  }

  /** Invokes {@link #reschedule} passing the time argument.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Central rescheduling method.
   * 
   * <p>
   * As long as there are service-access credits ({@link #hasServerAcccessCredits}),
   * start-able jobs (all waiting jobs)
   * and at least one server ({@link #hasServerAvailable}) available,
   * a single job is selected and started (i.e., added to {@link #jobsInServiceArea}),
   * taking a single server-access credit ({@link #takeServerAccessCredit} (without notifications).
   * 
   * <p>
   * The service time of the job to start is requested through
   * {@link #getServiceTimeForJob}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent} for the job.
   * 
   * <p>
   * After starting the jobs and scheduling departure events for each job,
   * a separate notification part of the method takes care of notifying listeners
   * through {@link #fireStart}, {@link #fireIfOutOfServerAccessCredits} and {@link #fireNewNoWaitArmed}.
   * 
   * <p>
   * If server-access credits are absent, this method does nothing, relying on {@link #rescheduleForNewServerAccessCredits}
   * to eventually take jobs into service.
   * 
   * <p>
   * If no servers are available, this method does nothing, relying on future departures (or jobs exiting otherwise)
   * to eventually take jobs into service.
   * 
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #jobsInServiceArea
   * @see #getServiceTimeForJob
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * @see #fireNewNoWaitArmed
   * 
   * @param time The time of rescheduling.
   * 
   */
  protected final void reschedule (final double time)
  {
    // Scheduling section; make sure we do not issue notifications.
    if (! (hasServerAcccessCredits () && hasServerAvailable ()))
      return;
    final Set<J> startableJobs = getJobsInWaitingArea ();
    final Set<J> startedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits ()
      && (! startableJobs.isEmpty ())
      && hasServerAvailable ())
    {
      takeServerAccessCredit (false);
      final J job = startableJobs.iterator ().next ();
      startableJobs.remove (job);
      if (job == null)
        throw new IllegalStateException ();
      this.jobsInServiceArea.add (job);
      final double jobServiceTime = getServiceTimeForJob (job);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      scheduleDepartureEvent (time + jobServiceTime, job);
      // Defer notifications until we are in a valid state again.
      startedJobs.add (job);
    }
    // Notification section.
    for (J j : startedJobs)
      // Be cautious here; previous invocation(s) of fireStart could have removed the job j already!
      if (this.jobsInServiceArea.contains (j))
        fireStart (time, j, (Q) this);
    fireIfOutOfServerAccessCredits (time);
    fireNewNoWaitArmed (time, isNoWaitArmed ());
  }
  
}
