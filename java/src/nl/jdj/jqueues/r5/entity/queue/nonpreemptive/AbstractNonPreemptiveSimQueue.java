package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for non-preemptive queueing disciplines.
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
   * XXX
   * This method should be moved upwards to AbstractSimQueue.
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

  /** Invokes {@link #removeJobFromQueueUponRevokation}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time);
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

  /** Removes the job from the internal data structures, and if needed, cancels a pending departure event.
   * 
   * If the job is in service, its departure event is canceled through {@link #cancelDepartureEvent},
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
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
    {
      this.jobsInServiceArea.remove (job);
      cancelDepartureEvent (job);
    }
    this.jobQueue.remove (job);
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
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Adds the job to the tail of the service area.
   * 
   * @see #jobsInServiceArea
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null || (! getJobs ().contains (job)) || getJobsInServiceArea ().contains (job))
      throw new IllegalArgumentException ();
    this.jobsInServiceArea.add (job);
  }

  /** Depending on the job's requested service time, makes it depart immediately, or schedules a suitable departure event.
   * 
   * <p>
   * Performs sanity checks on the fly (job present; job not yet started; requested service time zero or positive).
   * 
   * @see #getServiceTimeForJob
   * @see #scheduleDepartureEvent
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null || (! getJobs ().contains (job)) || (! getJobsInServiceArea ().contains (job)))
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    if (jobServiceTime > 0)
      scheduleDepartureEvent (time + jobServiceTime, job);
    else
      depart (time, job);
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
   * a single job is selected through {@link #getFirstJobInWaitingArea}
   * and started through {@link #start}.
   * 
   * @see #hasServerAcccessCredits
   * @see #getNumberOfJobsInWaitingArea
   * @see #hasServerAvailable
   * @see #start
   * @see #getFirstJobInWaitingArea
   * 
   * @param time The time of rescheduling.
   * 
   */
  protected final void reschedule (final double time)
  {
    while (hasServerAcccessCredits ()
      && getNumberOfJobsInWaitingArea () > 0
      && hasServerAvailable ())
      start (time, getFirstJobInWaitingArea ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
