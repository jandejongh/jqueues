package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import java.util.LinkedHashSet;
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
   * but if <code>N > 1</code> jobs are being executed, the virtual time increases linearly with slope <code>1/N</code> in time.
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

  /** Calls {@link #rescheduleAfterQueueEvent} for the arriving job.
   * 
   * @see #arrive
   * @see #rescheduleAfterQueueEvent
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    rescheduleAfterQueueEvent (time, job, null);
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

  /** Calls {@link #rescheduleAfterQueueEvent} for the revoked job.
   * 
   * @see #revoke
   * @see #rescheduleAfterQueueEvent
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    rescheduleAfterQueueEvent (time, null, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #rescheduleAfterQueueEvent} with <code>null</code> job arguments.
   * 
   * @see #setServerAccessCredits
   * @see #rescheduleAfterQueueEvent
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    rescheduleAfterQueueEvent (time, null, null);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #removeJobFromQueueUponRevokation} for the departed job.
   * 
   * @see #departureFromEventList
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponRevokation (departingJob, time);
  }

  /** Calls {@link #rescheduleAfterQueueEvent} for the departed job.
   * 
   * @see #departureFromEventList
   * @see #rescheduleAfterQueueEvent
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (departedJob == null)
      throw new IllegalArgumentException ();
    rescheduleAfterQueueEvent (time, null, departedJob);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE DEPARTURE EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules the single departure event for this queue.
   * 
   * @param time          The current time.
   * @param mustBePresent For sanity checking; if <code>true</code>, a <i>single</i> departure event
   *                        <i>must</i> currently be scheduled on the event list.
   * @param mustBeAbsent  For sanity checking; if <code>true</code>, <i>no</i> departure event
   *                        must currently be scheduled on the event list.
   * 
   * @throws IllegalArgumentException If <code>mustBePresent</code> and <code>mustBeAbsent</code> are both <code>true</code>.
   * 
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #jobsInServiceArea
   * @see #virtualDepartureTime
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void rescheduleDepartureEvent (final double time, final boolean mustBePresent, final boolean mustBeAbsent)
  {
    if (mustBePresent && mustBeAbsent)
      throw new IllegalArgumentException ();
    final Set<DefaultDepartureEvent<J, Q>> departureEvents = getDepartureEvents ();
    if (departureEvents == null)
      throw new RuntimeException ();
    if (departureEvents.size () > 1)
      throw new IllegalStateException ();
    if (mustBePresent && departureEvents.size () != 1)
      throw new IllegalStateException ();
    if (mustBeAbsent && ! departureEvents.isEmpty ())
      throw new IllegalStateException ();
    if (departureEvents.size () > 0)
      // XXX Should reuse existing departure event.
      cancelDepartureEvent (departureEvents.iterator ().next ());
    if (! this.virtualDepartureTime.isEmpty ())
    {
      if (getNumberOfJobsInServiceArea () == 0)
        throw new IllegalStateException ();
      final double scheduleVirtualTime = this.virtualDepartureTime.firstValue ();
      final double deltaVirtualTime = scheduleVirtualTime - getVirtualTime ();
      if (deltaVirtualTime < 0)
        throw new IllegalStateException ();
      final double deltaTime = deltaVirtualTime * getNumberOfJobsInServiceArea ();
      final J job = this.virtualDepartureTime.getPreImageForValue (scheduleVirtualTime).iterator ().next ();
      scheduleDepartureEvent (time + deltaTime, job);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE AFTER QUEUE EVENT (CENTRAL RESCHEDULING METHOD)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules after a (major) queue event (arrival, departure, revocation or new server-access credits).
   * 
   * <p>
   * Core rescheduling method for {@link PS}.
   * 
   * <p>
   * This method
   * <ul>
   * <li>Removes any pending departure event for an exiting job (if any).
   *     This is not necessary for departing jobs, but <i>it is</i> for revoked jobs, and this method cannot tell the difference
   *     between the two.
   * <li>Admits jobs for service based upon the number of jobs waiting and the remaining number of server access credits.
   *     Waiting jobs are admitted to the server in order of arrival (which is only relevant in the presence of finite
   *     server-access credits). The server-access credits taken and the jobs started are <i>not</i> reported at this stage.
   *     The started jobs are inserted into {@link #jobsInServiceArea}, their virtual times are calculated and they
   *     are inserted into the {@link #virtualDepartureTime} mapping.
   *     If a started job has zero requested service time, it departs immediately.
   * <li>Reschedules if needed the single departure event for the job in service with the smallest
   *     virtual departure time through {@link #rescheduleDepartureEvent}.
   * <li>Reports started jobs (if applicable).
   * <li>Reports if out of server-access credits.
   * </ul>
   * 
   * @param time The current time (of the event).
   * @param aJob The job that arrived, <code>null</code> if the event was not an arrival.
   * @param eJob The job that exited (departed or successfully revoked), <code>null</code> if the event was not the exit of a job.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #virtualDepartureTime
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #rescheduleDepartureEvent
   * @see #fireStart
   * @see #fireDeparture
   * @see #fireIfNewServerAccessCreditsAvailability
   * @see #fireIfNewNoWaitArmed
   * 
   */
  protected final void rescheduleAfterQueueEvent (final double time, final J aJob, final J eJob)
  {
    if (aJob != null && eJob != null)
      throw new IllegalArgumentException ();
    boolean departureEventMustBePresent = ! this.jobsInServiceArea.isEmpty ();
    boolean departureEventMustBeAbsent = ! departureEventMustBePresent;
    final int creditsOld = getServerAccessCredits ();
    if (aJob != null)
    {
      if (! this.jobQueue.contains (aJob))
        throw new IllegalStateException ();
      if (this.jobsInServiceArea.contains (aJob))
        throw new IllegalStateException ();
      if (this.virtualDepartureTime.containsKey (aJob))
        throw new IllegalStateException ();
    }
    else if (eJob != null)
    {
      if (this.jobQueue.contains (eJob))
        throw new IllegalStateException ();
      if (this.jobsInServiceArea.contains (eJob))
        throw new IllegalStateException ();
      if (this.virtualDepartureTime.containsKey (eJob))
        throw new IllegalStateException ();
      final Set<DefaultDepartureEvent<J, Q>> departureEvents = getDepartureEvents ();
      if (departureEvents == null)
        throw new RuntimeException ();
      if (departureEvents.size () > 1)
        throw new IllegalStateException ();
      if (departureEvents.size () == 1 && departureEvents.iterator ().next ().getObject () == eJob)
        cancelDepartureEvent (eJob);
      departureEventMustBePresent = (departureEvents.size () > 0);
      departureEventMustBeAbsent = ! departureEventMustBePresent;
    }
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    final Set<J> departedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
    {
      takeServerAccessCredit (false);
      final J job = getFirstJobInWaitingArea ();
      if (job == null)
        throw new IllegalStateException ();
      this.jobsInServiceArea.add (job);
      final double jobServiceTime = job.getServiceTime (this);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      final double jobVirtualDepartureTime = getVirtualTime () + jobServiceTime;
      this.virtualDepartureTime.put (job, jobVirtualDepartureTime);
      // Defer notifications until we are in a valid state again.
      startedJobs.add (job);
      if (jobServiceTime == 0.0)
      {
        removeJobFromQueueUponDeparture (job, time);
        job.setQueue (null);
        departedJobs.add (job);
      }
    }
    if (getNumberOfJobsInServiceArea () > 0)
      rescheduleDepartureEvent (time,
        departureEventMustBePresent,
        departureEventMustBeAbsent);
    // Notification section.
    for (final J j : startedJobs)
      fireStart (time, j, (Q) this);
    for (final J j : departedJobs)
      fireDeparture (time, j, (Q) this);
    fireIfNewServerAccessCreditsAvailability (time);
    fireIfNewNoWaitArmed (time, isNoWaitArmed ());
  }

}
