package nl.jdj.jqueues.r5.entity.jq.queue.processorsharing;

import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.qos.HOL_PS;
import nl.jdj.jqueues.r5.util.collection.HashMapWithPreImageAndOrderedValueSet;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for egalitarian processor-sharing queueing disciplines.
 *
 * <p>
 * In <i>egalitarian</i> processor-sharing queueing disciplines,
 * all jobs in the service area are served simultaneously at equal rates,
 * until completion of one of them.
 * The service rate, naturally, is inversely proportional to the number of jobs in the service area.
 * 
 * <p>
 * The remaining degrees of freedom to sub-classes are when to start which jobs.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see PS
 * @see HOL_PS
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
public abstract class AbstractEgalitarianProcessorSharingSimQueue
  <J extends SimJob, Q extends AbstractEgalitarianProcessorSharingSimQueue>
  extends AbstractProcessorSharingSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a egalitarian processor-sharing queue given an event list, buffer size and number of servers.
   *
   * <p>
   * The constructor registers a pre-update hook that sets the virtual time upon updates.
   * 
   * @param eventList       The event list to use.
   * @param bufferSize      The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param numberOfServers The number of servers (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   *
   * @see #registerPreUpdateHook
   * @see #updateVirtualTime
   * 
   */
  protected AbstractEgalitarianProcessorSharingSimQueue
  (final SimEventList eventList, final int bufferSize, final int numberOfServers)
  {
    super (eventList, bufferSize, numberOfServers);
    registerPreUpdateHook (this::updateVirtualTime);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VIRTUAL TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The current virtual time; updates in {@link #update}.
   * 
   */
  private double virtualTime = 0;
  
  /** Returns the current virtual time.
   * 
   * <p>
   * The virtual time for an {@link AbstractEgalitarianProcessorSharingSimQueue}
   * is zero when no jobs are being executed, and increases in time inversely proportional to the number of jobs in execution.
   * Hence, if there is only one job in execution, the virtual time increases at the same rate as the ("real") time,
   * but if {@code N > 1} jobs are being executed, the virtual time increases linearly with slope {@code 1/N} in time.
   * 
   * <p>
   * The virtual time is kept consistent in {@link #update}.
   * 
   * <p>
   * Note that with an {@link AbstractEgalitarianProcessorSharingSimQueue},
   * the virtual departure time of a job can be calculated at the time the job is taken into service;
   * it does not change afterwards
   * (as the required service time cannot change during a queue visit, by contract of {@link SimQueue}).
   * 
   * @return The current virtual time.
   * 
   * @see #update
   * 
   */
  protected final double getVirtualTime ()
  {
    return this.virtualTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // VIRTUAL DEPARTURE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The mapping from jobs in {@link #getJobsInServiceArea} to their respective virtual departure times.
   * 
   * <p>
   * The special extensions to <code>TreeMap</code> allow for efficient  determination of the pre-images of
   * virtual departure times.
   * 
   */
  protected final HashMapWithPreImageAndOrderedValueSet<J, Double> virtualDepartureTime
    = new HashMapWithPreImageAndOrderedValueSet<> ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method, resets the virtual time to zero and clears the virtual departure time map.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.virtualTime = 0;
    this.virtualDepartureTime.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates the virtual time.
   * 
   * <p>
   * This method is called as an update hook, and not meant to be called from user code (in sub-classes).
   * It is left protected for {@code javadoc}.
   * 
   * @param newTime The new time.
   * 
   * @see #getNumberOfJobsInServiceArea
   * @see #getVirtualTime
   * @see #getLastUpdateTime
   * @see #registerPreUpdateHook
   * 
   */
  protected final void updateVirtualTime (final double newTime)
  {
    if (newTime < getLastUpdateTime ())
      throw new IllegalStateException ();
    final int numberOfJobsExecuting = getNumberOfJobsInServiceArea ();
    if (numberOfJobsExecuting == 0)
      this.virtualTime = 0;
    else if (newTime > getLastUpdateTime ())
      this.virtualTime += ((newTime - getLastUpdateTime ()) / numberOfJobsExecuting);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Performs sanity checks, and administers the job's virtual departure time.
   * 
   * @see #getServiceTimeForJob
   * @see #getVirtualTime
   * @see #virtualDepartureTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (this.virtualDepartureTime.containsKey (job))
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    final double jobVirtualDepartureTime = getVirtualTime () + jobServiceTime;
    this.virtualDepartureTime.put (job, jobVirtualDepartureTime);
  }

  /** Reschedules due to the start of a job.
   * 
   * <ul>
   * <li>If the requested service time of the job is zero, the job departs immediately.
   * <li>If the requested service time of the job is finite and the current time is positive of negative infinity,
   *       the job departs immediately.
   * <li>Otherwise, if the requested service time is finite,
   *       a single departure event for the job is scheduled.
   * <li>(If the required service time is infinite, this method does nothing.)
   * </ul>
   * 
   * @see #virtualDepartureTime
   * @see #getServiceTimeForJob
   * @see #rescheduleDepartureEvent
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job))
    || ! this.virtualDepartureTime.containsKey (job))
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    if (jobServiceTime == 0
    || (Double.isFinite (jobServiceTime) && Double.isInfinite (time)))
      depart (time, job);
    else
      rescheduleDepartureEvent ();
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return super.getServiceTimeForJob (job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #removeJobFromQueueUponRevokation} for the departed job.
   * 
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponRevokation (departingJob, time, false);
  }

  /** Calls {@link #rescheduleDepartureEvent}.
   * 
   * <p>
   * May be overridden/augmented by sub-classes
   * if additional actions/checks are required upon a departure (e.g., in {@link HOL_PS}.
   * 
   * @see #rescheduleDepartureEvent
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  @Override
  protected void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (departedJob == null)
      throw new IllegalArgumentException ();
    rescheduleDepartureEvent ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE DEPARTURE EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules the single departure event for this queue.
   * 
   * <p>
   * First, cancels a pending departure event.
   * Then, determines which job in the service area (if any) is to depart first through {@link #virtualDepartureTime},
   * and what its remaining service time is.
   * 
   * <ul>
   * <li>If the remaining service time of the job is zero, the job departs immediately.
   * <li>If the remaining service time of the job is finite and the current time is positive of negative infinity,
   *       the job departs immediately.
   * <li>Otherwise, if the remaining service time is finite,
   *       a single departure event for the job is scheduled.
   * <li>(If the remaining service time is infinite, this method does nothing.)
   * </ul>
   * 
   * @see #getLastUpdateTime
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #getJobsInServiceArea
   * @see #virtualDepartureTime
   * @see #getVirtualTime
   * @see #depart
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void rescheduleDepartureEvent ()
  {
    final Set<SimJQEvent.Departure> departureEvents = getDepartureEvents ();
    if (departureEvents == null)
      throw new RuntimeException ();
    if (departureEvents.size () > 1)
      throw new IllegalStateException ();
    if (departureEvents.size () > 0)
      cancelDepartureEvent (departureEvents.iterator ().next ());
    if (! this.virtualDepartureTime.isEmpty ())
    {
      if (! hasJobsInServiceArea ())
        throw new IllegalStateException ();
      if (Double.isInfinite (getLastUpdateTime ()))
      {
        // If time is either positive of negative infinity,
        // all jobs in the service area must have infinite service-time requirement,
        // because if finite, they should have departed upon start already.
        // Other than that, there is nothing to do at positive or negative infinity,
        // because jobs with infinite service-time requirement never depart.
        for (final J job : getJobsInServiceArea ())
          if (Double.isFinite (getServiceTimeForJob (job)))
            throw new IllegalStateException ();
      }
      else
      {
        // The current time is finite, schedule for departure the job with the earliest virtual departure time, unless finite.
        final double scheduleVirtualTime = this.virtualDepartureTime.firstValue ();
        final double deltaVirtualTime = scheduleVirtualTime - getVirtualTime ();
        if (deltaVirtualTime < 0)
          // XXX There is no protection in updating the virtual time, so at some point, we will hit a rounding error here...
          throw new IllegalStateException ();
        final J job = this.virtualDepartureTime.getPreImageForValue (scheduleVirtualTime).iterator ().next ();
        if (deltaVirtualTime == 0)
          depart (getLastUpdateTime (), job);
        else if (Double.isFinite (deltaVirtualTime))
        {
          final double deltaTime = deltaVirtualTime * getNumberOfJobsInServiceArea ();
          scheduleDepartureEvent (getLastUpdateTime () + deltaTime, job);
        }
        else
          // Our earliest departer (and thus all jobs in the service area) has infinite requested service time.
          // Nothing to do here, because such jobs never depart.
          ;
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
