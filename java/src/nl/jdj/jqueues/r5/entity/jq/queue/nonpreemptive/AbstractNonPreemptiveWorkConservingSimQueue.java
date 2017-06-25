package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractClassicSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for non-preemptive work-conserving queueing disciplines.
 *
 * <p>
 * The work model for this class is that of a waiting area of possibly limited size
 * and a service area that consists of a fixed (but possibly infinite) number of servers
 * of equal capacity and "inter-changeable" to visiting jobs
 * (i.e., jobs do not care by which server they are served).
 * Arriving jobs that need to wait but find the waiting area full are dropped by default;
 * however, sub-classes may override this and select another job from the waiting area to be dropped.
 * Each server can only serve at most one job at a time.
 * Once taken into service on one of the servers, jobs are served by that server until completion
 * (unless they are revoked from service), after which they depart from the system.
 * 
 * <p>
 * In addition, implementations of this class are always <i>work-conserving</i>,
 * which in this context is interpreted as follows:
 * <ul>
 * <li> No work for the queueing system can be created or destroyed other than by the arrival of jobs,
 *        and the service provided by the server(s) to jobs.
 * <li> The queueing system always attempts to deploy its maximum service capacity to its jobs;
 *        it never lets jobs wait unnecessarily.
 * </ul>
 * 
 * <p>
 * Note that the potential lack of server-access credits inhibits <i>any</i> implementation of {@link SimQueue} to
 * be truly work-conserving.
 * The requirement of work-conservation should therefore be annotated with "given sufficient server-access credits".
 * 
 * <p>
 * Implementations allow (through inheritance) the job requested service time to be (positive) infinite.
 * Jobs with that feature will never depart (through internal scheduling), even if time itself is positive or negative infinity.
 * If time is infinite, jobs with finite service time requirement will always start and depart immediately upon arrival.
 * 
 * <p>
 * Despite the constraints and conditions outlined above, many classical non-preemptive queueing systems
 * like {@link FCFS}, {@link FCFS_B_c}, {@link SJF} and {@link IS} are in fact work-conserving.
 * 
 * <p>
 * Implementations only need worry about:
 * <ul>
 * <li> Resetting the queueing system, if queue-specific data structures are maintained for visiting jobs,
 *      by augmenting {@link #resetEntitySubClass}.
 * <li> Inserting a job in the waiting area (possibly imposing a queue-specific structure)
 *      by implementing {@link #insertAdmittedJobInQueueUponArrival}.
 * <li> If needed, override {@link #selectJobToDropAtFullQueue} in order to impose a non-default drop policy.
 * <li> Selecting which job to start by implementing {@link #selectJobToStart}
 *      (the moments to start jobs are entirely managed by this class).
 * <li> If needed, override {@link #getServiceTimeForJob}.
 * <li> Removing a job from the system, if queue-specific data structures are maintained for visiting jobs,
 *      by overriding {@link #removeJobFromQueueUponExit}.
 * <li> If applicable, exposing the queue's QoS structure (which can only affect the waiting-area behavior)
 *      by overriding {@link #getQoS} and {@link #getQoSClass}
 *      and advertising the appropriate {@link SimQueueQoS} generic-type structure.
 * </ul>
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public abstract class AbstractNonPreemptiveWorkConservingSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveWorkConservingSimQueue>
  extends AbstractClassicSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive work-conserving queue given an event list, buffer size and number of servers.
   *
   * @param eventList       The event list to use.
   * @param bufferSize      The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param numberOfServers The number of servers (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   *
   */
  protected AbstractNonPreemptiveWorkConservingSimQueue
  (final SimEventList eventList, final int bufferSize, final int numberOfServers)
  {
    super (eventList, bufferSize, numberOfServers);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Invokes super-method and revokes a pending job drop.
   * 
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.jobToDropAtFullQueue = null;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER AVAILABLE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are strictly fewer jobs in the service area than servers present in the system,
   *  or if the number of servers is infinite.
   * 
   * @return True if there are strictly fewer jobs in the service area than servers present in the system,
   *         or if the number of servers is infinite.
   * 
   * @see #getNumberOfServers
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  public final boolean hasServerAvailable ()
  {
    return getNumberOfServers () == Integer.MAX_VALUE
        || getNumberOfJobsInServiceArea () < getNumberOfServers ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A flag used between {@link #insertJobInQueueUponArrival}
   *  and {@link #rescheduleAfterArrival}
   *  to indicate that a job from the waiting area must be dropped.
   * 
   * <p>
   * The flag is cleared upon construction,
   * upon a reset,
   * and just before the actual drop takes place.
   * 
   */
  private J jobToDropAtFullQueue = null;
  
  /** Enters the job into the queue administration if admitted;
   *  if needed, prepares the drop of another job in the waiting area.
   * 
   * <P>
   * If the arriving job can be taken into service immediately,
   * or if there is sufficient waiting room,
   * it is admitted and entered into the
   * internal administration through {@link #insertAdmittedJobInQueueUponArrival}.
   * 
   * <p>
   * Note that we must temporarily accept the fact that in case there is no waiting room left, but we know that the job will
   * be taken into service immediately, we leave the queue in an inconsistent state by adding the job to {@link #jobQueue},
   * having more jobs waiting than allowed.
   * Here we rely on the fact that by contract of {@link AbstractSimQueue#arrive}, between corresponding calls to
   * {@link #insertJobInQueueUponArrival} and {@link #rescheduleAfterArrival} there can be no event handling from the event list
   * or from notifications from elsewhere.
   * 
   * <p>
   * If the arriving job finds a full (and non-empty) waiting area,
   * this method invokes {@link #selectJobToDropAtFullQueue}
   * by which sub-classes indicate the drop policy.
   * If the arriving job is to be dropped,
   * it is simply not entered into the {@link #jobQueue}
   * and the {@link AbstractSimQueue} infrastructure will automatically drop the job.
   * If, however, another job is selected from the waiting area,
   * the arriving job is entered through {@link #insertAdmittedJobInQueueUponArrival}
   * (again temporarily leaving the system in a consistent state),
   * and an internal flag is raised for {@link #rescheduleAfterArrival}
   * in order to carry out the actual drop.
   * 
   * @see #hasServerAcccessCredits
   * @see #isStartArmed
   * @see #getNumberOfJobsInWaitingArea
   * @see #getBufferSize
   * @see #jobQueue
   * @see #insertAdmittedJobInQueueUponArrival
   * @see #selectJobToDropAtFullQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if ((hasServerAcccessCredits () && isStartArmed ())
      || getBufferSize () == Integer.MAX_VALUE
      || getNumberOfJobsInWaitingArea () < getBufferSize ())
    {
      insertAdmittedJobInQueueUponArrival (job, time);
      if (! this.jobQueue.contains (job))
        throw new IllegalStateException ();
    }
    else if (hasJobsInWaitingArea ())
    {
      final J jobToDrop = selectJobToDropAtFullQueue (job, time);
      if (jobToDrop == null)
        throw new RuntimeException ();
      if (jobToDrop != job)
      {
        insertAdmittedJobInQueueUponArrival (job, time);
        if (this.jobToDropAtFullQueue != null)
          throw new IllegalStateException ();
        if (! this.jobQueue.contains (job))
          throw new IllegalStateException ();
        if (! this.jobQueue.contains (jobToDrop))
          throw new IllegalStateException ();
        this.jobToDropAtFullQueue = jobToDrop;
      }
    }
  }

  /** Inserts an admitted job into {@link #jobQueue} upon arrival.
   * 
   * <p>
   * Auxiliary method to the final {@link #insertJobInQueueUponArrival}.
   * 
   * <p>
   * Caller has already checked queue-access vacations and admission to the waiting area (in view of the buffer size).
   * Note that implementations must temporarily admit a single job more that the available waiting area,
   * to account for the fact that there may not be waiting area available, must an arriving job
   * is taken into service immediately.
   * Another such case arises when an arriving job must be entered into the {@link #jobQueue}
   * before another job must be dropped (due to a full buffer).
   * 
   * <p>
   * Obviously, implementations are free to maintain more complicated structures for the waiting area next to just
   * {@link #jobQueue} and {@link #jobsInServiceArea}.
   * However, at the expense of an exception upon return, implementations must <i>always</i> insert the
   * job in {@link #jobQueue}.
   * 
   * @param job  The job to insert, non-{@code null}.
   * @param time The current time, i.e., the job's arrival time.
   * 
   * @see #insertJobInQueueUponArrival
   * @see #rescheduleAfterArrival
   * 
   */
  protected abstract void insertAdmittedJobInQueueUponArrival (final J job, final double time);
  
  /** Selects the job to drop in case the waiting area is full (and non-empty) when a job arrives.
   * 
   * <p>
   * Implementations can rely on the fact that the arriving job has not yet entered the
   * {@link #jobQueue}, and that the waiting area is full and non-empty
   * (the latter rules out the case of a waiting area of zero size).
   * 
   * <p>
   * Implementations only have to select either the arriving job,
   * or a job from the waiting area to be dropped,
   * i.e., they should not alter the internal administration
   * and they should not reschedule.
   * 
   * <p>
   * The default implementation returns the {@code arrivingJob} argument;
   * which causes the arriving job to be dropped.
   * 
   * @param arrivingJob The job that arrives, non-{@code null}.
   * @param time        The job's arrival time.
   * 
   * @return The job to drop which must be either the arriving job, or a job currently in the waiting area.
   * 
   * @see #insertJobInQueueUponArrival
   * 
   */
  protected J selectJobToDropAtFullQueue (final J arrivingJob, final double time)
  {
    return arrivingJob;
  }
  
  /** Carries out a pending job drop, if present; otherwise invokes {@link #reschedule}.
   * 
   * @see #insertJobInQueueUponArrival
   * @see #selectJobToDropAtFullQueue
   * @see #drop
   * @see #reschedule
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (this.jobToDropAtFullQueue != null)
    {
      final J jobToDrop = this.jobToDropAtFullQueue;
      this.jobToDropAtFullQueue = null;
      drop (jobToDrop, time);
    }
    else
      reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void queueAccessVacationDropSubClass (double time, J job)
  {
    super.queueAccessVacationDropSubClass (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Invokes {@link #removeJobFromQueueUponDeparture}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponDeparture (job, time);
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

  /** Invokes {@link #removeJobFromQueueUponDeparture}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    removeJobFromQueueUponDeparture (job, time);
  }

  /** Invokes {@link #reschedule} passing the time argument.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    reschedule (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }

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
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns whether a server is available.
   * 
   * @return Whether a server is available, i.e., the result from {@link #hasServerAvailable}.
   * 
   * @see #hasServerAvailable
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return hasServerAvailable ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Selects which job in the waiting area to start next.
   * 
   * <p>
   * Implementations can assume that the waiting area is non-empty.
   * Moreover, they are free to chose whichever job from the waiting area to start,
   * irrespective of the structure implied by {@link #insertAdmittedJobInQueueUponArrival}.
   * 
   * @return The job in the waiting area to start next, must not be {@code null} and <i>must</i> be present in the waiting area.
   * 
   * @see #insertAdmittedJobInQueueUponArrival
   * @see #reschedule
   * @see #getFirstJobInWaitingArea
   * 
   */
  protected abstract J selectJobToStart ();
  
  /** Adds the job to the tail of the service area (but its order is irrelevant).
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

  /** Depending on the job's requested service time, makes it depart immediately, schedules a suitable departure event,
   *  or does nothing if the job requires infinite service time.
   * 
   * <p>
   * Performs sanity checks on the fly (job present; job not yet started; requested service time zero or positive).
   * The time argument must match the result from {@link #getLastUpdateTime} (and is thus only present for sanity checking).
   * 
   * <p>
   * If a job has infinite requested service time, it will start but never depart,
   * even if the start is scheduled at positive or negative infinity.
   * 
   * <p>
   * With zero requested service time, a job departs immediately.
   * This is also the case if the start is at positive or negative infinity
   * AND the job has finite requested service time.
   * 
   * <p>
   * In all other cases, a suitable departure event is scheduled through {@link #scheduleDepartureEvent}.
   * 
   * <p>
   * Caveat: the specification above implies that NOT all jobs in the service area will have a departure event
   * scheduled for them!
   * 
   * @see #getServiceTimeForJob
   * @see #scheduleDepartureEvent
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job))
    || time != getLastUpdateTime ())
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    if (Double.isFinite (jobServiceTime))
    {
      if (jobServiceTime == 0 || ! Double.isFinite (time))
        depart (time, job);
      else
        scheduleDepartureEvent (time + jobServiceTime, job);
    }
    else
      // Jobs with infinite requested service time never depart.
      ;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from the system and cancels its departure event (if present).
   * 
   * <p>
   * Checks the presence of the departing job in {@link #jobQueue} (jobs must be present),
   * cancels pending departure events for the job (if present),
   * invokes {@link #removeJobFromQueueUponExit}
   * and checks the absence of the job in {@link #jobQueue} and {@link #jobsInServiceArea}.
   * 
   * <p>
   * This method also serves as entry point for
   * {@link #removeJobFromQueueUponDrop} and
   * {@link #removeJobFromQueueUponRevokation}.
   * 
   * @throws IllegalStateException If sanity checks fail.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #removeJobFromQueueUponExit
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (! this.jobQueue.contains (departingJob))
      throw new IllegalStateException ();
    if (! getDepartureEvents (departingJob).isEmpty ())
      cancelDepartureEvent (departingJob);
    removeJobFromQueueUponExit (departingJob, time);
    if (this.jobQueue.contains (departingJob))
      throw new RuntimeException ();
    if (this.jobsInServiceArea.contains (departingJob))
      throw new RuntimeException ();
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
  // EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes a job that ends its visit (in whatever way) from the internal administration.
   * 
   * <p>
   * The default implementation removes the job from {@link #jobsInServiceArea} and {@link #jobQueue},
   * which is also the minimum requirement for overrides.
   * 
   * <p>
   * Implementations do not have to cancel departure events; this has been done by caller already.
   * 
   * @param exitingJob The jobs that ends its visit, non-{@code null} and present in {@link #jobQueue}.
   * @param time       The current time, i.e., the time the job ends its visit.
   * 
   * @see #removeJobFromQueueUponDrop
   * @see #removeJobFromQueueUponRevokation
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  protected void removeJobFromQueueUponExit  (final J exitingJob, final double time)
  {
    this.jobQueue.remove (exitingJob);
    this.jobsInServiceArea.remove (exitingJob);    
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
   * a single job is selected through (the sub-class specific) {@link #selectJobToStart}
   * and started through {@link #start}.
   * 
   * @see #hasServerAcccessCredits
   * @see #getNumberOfJobsInWaitingArea
   * @see #hasServerAvailable
   * @see #start
   * @see #selectJobToStart
   * 
   * @param time The time of rescheduling.
   * 
   */
  protected final void reschedule (final double time)
  {
    while (hasServerAcccessCredits ()
      && getNumberOfJobsInWaitingArea () > 0
      && hasServerAvailable ())
      // We rely on the sanity checks in AbstractSimQueue.start here...
      start (time, selectJobToStart ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
