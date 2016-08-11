package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.util.collection.HashMapWithPreImageAndOrderedValueSet;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server {@link PS} queue serves all jobs simultaneously, equally distributing its service capacity.
 *
 * <p>
 * Processor Sharing.
 * 
 * <p>
 * The (Egalitarian) Processor-Sharing queueing system distributes its service capacity equally among the jobs in execution.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class PS<J extends SimJob, Q extends PS>
extends AbstractProcessorSharingSingleServerSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a PS queue given an event list.
   *
   * <p>
   * The constructor registers a pre-update hook that sets the virtual time upon updates.
   * 
   * @param eventList The event list to use.
   *
   * @see #registerPreUpdateHook
   * @see #updateVirtualTime
   * 
   */
  public PS (final SimEventList eventList)
  {
    super (eventList);
    registerPreUpdateHook (this::updateVirtualTime);
  }
  
  /** Returns a new {@link PS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link PS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public PS<J, Q> getCopySimQueue ()
  {
    return new PS<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "PS".
   * 
   * @return "PS".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "PS";
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
   * The virtual time for a {@link PS} is zero when no jobs are being executed, and increases in time inversely proportional
   * to the number of jobs in execution.
   * Hence, if there is only one job in execution, the virtual time increases at the same rate as the ("real") time,
   * but if {@code N > 1} jobs are being executed, the virtual time increases linearly with slope {@code 1/N} in time.
   * 
   * <p>
   * The virtual time is kept consistent in {@link #update}.
   * 
   * <p>
   * Note that with {@link PS}, the virtual departure time of a job can be calculated at the time the job is taken into service;
   * it does not change afterwards (as the required service time cannot change during a queue visit, by contract of
   * {@link SimQueue}).
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
  protected final void resetEntitySubClass ()
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
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Starts the arrived job if server-access credits are available.
   * 
   * @see #hasServerAcccessCredits
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null || ! getJobsInWaitingArea ().contains (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not drop jobs.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not drop jobs.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the jobs from the internal data structures.
   * 
   * <p>
   * Removes the job from {@link #jobQueue},
   * and if needed from {@link #jobsInServiceArea} and {@link #virtualDepartureTime}.
   * 
   * @see #revoke
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (this.jobsInServiceArea.contains (job))
    {
      if (! this.virtualDepartureTime.containsKey (job))
        throw new IllegalStateException ();
      this.virtualDepartureTime.remove (job);
      this.jobsInServiceArea.remove (job);
    }
    this.jobQueue.remove (job);
  }

  /** Calls {@link #rescheduleDepartureEvent}.
   * 
   * @see #rescheduleDepartureEvent
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    rescheduleDepartureEvent ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Starts jobs as long as there are server-access credits and jobs waiting.
   * 
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, getFirstJobInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code>.
   * 
   * @return True.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job, after sanity checks, in the service area and administers its virtual departure time.
   * 
   * @see SimJob#getServiceTime
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
    final double jobServiceTime = job.getServiceTime (this);
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
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #removeJobFromQueueUponRevokation} for the departed job.
   * 
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponRevokation (departingJob, time);
  }

  /** Calls {@link #rescheduleDepartureEvent}.
   * 
   * @see #rescheduleDepartureEvent
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
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
