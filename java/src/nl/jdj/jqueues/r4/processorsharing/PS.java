package nl.jdj.jqueues.r4.processorsharing;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link PS} queue serves all jobs simultaneously, equally distributing its service capacity.
 *
 * <p>
 * Processor Sharing.
 * 
 * <p>
 * The Processor-Sharing queueing system distributes its service capacity equally among the jobs in execution.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class PS<J extends SimJob, Q extends PS> extends AbstractProcessorSharingSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a PS queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public PS (final SimEventList eventList)
  {
    super (eventList);
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
  public final String toStringDefault ()
  {
    return "PS";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
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
  
  /** The mapping from jobs executing (in {@link #jobsExecuting}) to their respective virtual departure times.
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
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates the virtual time and calls super method (in that order!).
   * 
   * @see #getNumberOfJobsExecuting
   * @see #getVirtualTime
   * @see #getLastUpdateTime
   * 
   */
  @Override
  public final void update (final double time)
  {
    if (time < getLastUpdateTime ())
      throw new IllegalStateException ();
    final int numberOfJobsExecuting = getNumberOfJobsExecuting ();
    if (numberOfJobsExecuting == 0)
      this.virtualTime = 0;
    else if (time > getLastUpdateTime ())
      this.virtualTime += ((time - getLastUpdateTime ()) / numberOfJobsExecuting);
    // Super method will set this.lastUpdateTime to time.
    // That is why it should not be called first, or we will lose this.lastUpdateTime.
    super.update (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method, resets the virtual time to zero and clears the virtual departure time map.
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.virtualTime = 0;
    this.virtualDepartureTime.clear ();
  }
  
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

  /** Removes the jobs from the internal data structures.
   * 
   * <p>
   * Returns <code>false</code> if the job is currently in service (in {@link #jobsExecuting})
   * and <code>interruptService==true</code>.
   * Otherwise, removes the job from {@link #jobQueue},
   * and if needed from {@link #jobsExecuting} and {@link #virtualDepartureTime}.
   * 
   * @see #revoke
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (this.jobsExecuting.contains (job))
    {
      if (! interruptService)
        return false;
      if (! this.virtualDepartureTime.containsKey (job))
        throw new IllegalMonitorStateException ();
      this.virtualDepartureTime.remove (job);
      this.jobsExecuting.remove (job);
    }
    this.jobQueue.remove (job);
    return true;
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

  /** Calls {@link #removeJobFromQueueUponRevokation} for the departed job
   *  with <code>true</code> value for the <code>interruptService</code> argument.
   * 
   * @see #departureFromEventList
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponRevokation (departingJob, time, true);
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
   * @see #jobsExecuting
   * @see #virtualDepartureTime
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void rescheduleDepartureEvent (final double time, final boolean mustBePresent, final boolean mustBeAbsent)
  {
    if (mustBePresent && mustBeAbsent)
      throw new IllegalArgumentException ();
    final Set<DefaultDepartureEvent> departureEvents = getDepartureEvents ();
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
      if (getNumberOfJobsExecuting () == 0)
        throw new IllegalStateException ();
      final double scheduleVirtualTime = this.virtualDepartureTime.firstValue ();
      final double deltaVirtualTime = scheduleVirtualTime - getVirtualTime ();
      if (deltaVirtualTime < 0)
        throw new IllegalStateException ();
      final double deltaTime = deltaVirtualTime * getNumberOfJobsExecuting ();
      final J job = this.virtualDepartureTime.getPreImageForValue (scheduleVirtualTime).iterator ().next ();
      scheduleDepartureEvent (time + deltaTime, job);
    }
  }
  
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
   *     The started jobs are inserted into {@link #jobsExecuting}, their virtual times are calculated and they
   *     are inserted into the {@link #virtualDepartureTime} mapping.
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
   * @see #jobsExecuting
   * @see #virtualDepartureTime
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #rescheduleDepartureEvent
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * 
   */
  protected final void rescheduleAfterQueueEvent (final double time, final J aJob, final J eJob)
  {
    if (aJob != null && eJob != null)
      throw new IllegalArgumentException ();
    boolean departureEventMustBePresent = ! this.jobsExecuting.isEmpty ();
    boolean departureEventMustBeAbsent = ! departureEventMustBePresent;
    final int creditsOld = getServerAccessCredits ();
    if (aJob != null)
    {
      if (! this.jobQueue.contains (aJob))
        throw new IllegalStateException ();
      if (this.jobsExecuting.contains (aJob))
        throw new IllegalStateException ();
      if (this.virtualDepartureTime.containsKey (aJob))
        throw new IllegalStateException ();
    }
    else if (eJob != null)
    {
      if (this.jobQueue.contains (eJob))
        throw new IllegalStateException ();
      if (this.jobsExecuting.contains (eJob))
        throw new IllegalStateException ();
      if (this.virtualDepartureTime.containsKey (eJob))
        throw new IllegalStateException ();
      final Set<DefaultDepartureEvent> departureEvents = getDepartureEvents ();
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
    while (hasServerAcccessCredits () && hasJobsWaiting ())
    {
      takeServerAccessCredit (false);
      final J job = getFirstJobWaiting ();
      if (job == null)
        throw new IllegalStateException ();
      this.jobsExecuting.add (job);
      final double jobServiceTime = job.getServiceTime (this);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      final double jobVirtualDepartureTime = getVirtualTime () + jobServiceTime;
      this.virtualDepartureTime.put (job, jobVirtualDepartureTime);
      // Defer notifications until we are in a valid state again.
      startedJobs.add (job);
    }
    if (getNumberOfJobsExecuting () > 0)
      rescheduleDepartureEvent (time,
        departureEventMustBePresent,
        departureEventMustBeAbsent);
    // Notification section.
    for (J j : startedJobs)
      // Be cautious here; previous invocation(s) of fireStart could have removed the job j already!
      if (this.jobsExecuting.contains (j))
        fireStart (time, j, (Q) this);
    if (creditsOld > 0 && getServerAccessCredits () == 0)
      fireIfOutOfServerAccessCredits (time);
  }
  
}
