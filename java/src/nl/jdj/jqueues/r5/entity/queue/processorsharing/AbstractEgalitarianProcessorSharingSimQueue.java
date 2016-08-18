package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.qos.HOL_PS;
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
  
  /** The mapping from jobs in {@link #jobsInServiceArea} to their respective virtual departure times.
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
  
  /** Inserts the job, after sanity checks, in the service area and administers its virtual departure time.
   * 
   * @see #getServiceTimeForJob
   * @see #getVirtualTime
   * @see #virtualDepartureTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || getJobsInServiceArea ().contains (job)
    || this.virtualDepartureTime.containsKey (job))
      throw new IllegalArgumentException ();
    this.jobsInServiceArea.add (job);
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    final double jobVirtualDepartureTime = getVirtualTime () + jobServiceTime;
    this.virtualDepartureTime.put (job, jobVirtualDepartureTime);
  }

  /** Reschedules due to the start of a job, making it depart immediately if its requested service time is zero,
   *  or rescheduling the (single) departure event of this queue otherwise.
   * 
   * @see #virtualDepartureTime
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
    final double jobServiceTime = this.virtualDepartureTime.get (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    if (jobServiceTime > 0)
      rescheduleDepartureEvent ();
    else
      depart (time, job);
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
    removeJobFromQueueUponRevokation (departingJob, time);
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
   * and schedules a departure event for it (or makes the job depart immediately through {@link #depart} if its
   * remaining service time is zero.
   * 
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #jobsInServiceArea
   * @see #virtualDepartureTime
   * @see #getVirtualTime
   * @see #depart
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void rescheduleDepartureEvent ()
  {
    final Set<DefaultDepartureEvent<J, Q>> departureEvents = getDepartureEvents ();
    if (departureEvents == null)
      throw new RuntimeException ();
    if (departureEvents.size () > 1)
      throw new IllegalStateException ();
    if (departureEvents.size () > 0)
      cancelDepartureEvent (departureEvents.iterator ().next ());
    if (! this.virtualDepartureTime.isEmpty ())
    {
      if (getNumberOfJobsInServiceArea () == 0)
        throw new IllegalStateException ();
      final double scheduleVirtualTime = this.virtualDepartureTime.firstValue ();
      final double deltaVirtualTime = scheduleVirtualTime - getVirtualTime ();
      if (deltaVirtualTime < 0)
        throw new IllegalStateException ();
      final J job = this.virtualDepartureTime.getPreImageForValue (scheduleVirtualTime).iterator ().next ();
      if (deltaVirtualTime == 0)
        depart (getLastUpdateTime (), job);
      else
      {
        final double deltaTime = deltaVirtualTime * getNumberOfJobsInServiceArea ();
        scheduleDepartureEvent (getLastUpdateTime () + deltaTime, job);
      }
    }
  }
  
}
