package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** The "catch-up" single-server {@link CUPS} queue serves all jobs with the least obtained service time simultaneously,
 *  equally distributing its service capacity among them.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class CUPS<J extends SimJob, Q extends CUPS>
extends AbstractProcessorSharingSingleServerSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOGGER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOGGER = Logger.getLogger (CUPS.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a CUPS queue given an event list.
   *
   * <p>
   * The constructor registers a pre-update hook that sets the virtual time upon updates.
   * 
   * @param eventList The event list to use.
   *
   * @see #registerPreUpdateHook
   * @see #updateObtainedServiceTime
   * 
   */
  public CUPS (final SimEventList eventList)
  {
    super (eventList);
    registerPreUpdateHook (this::updateObtainedServiceTime);
  }
  
  /** Returns a new {@link CUPS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link CUPS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public CUPS<J, Q> getCopySimQueue ()
  {
    return new CUPS<> (getEventList ());
  }
  
  /** The tolerance for rounding error in the obtained service time.
   * 
   */
  public final static double TOLERANCE_OST = 1.0E-9;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "CUPS".
   * 
   * @return "CUPS".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "CUPS";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INTERNAL (STATE) ADMINISTRATION
  // - requiredServiceTime
  // - obtainedServiceTimeMap
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<J, Double> requiredServiceTime = new LinkedHashMap<> ();
  private final NavigableMap<Double, Set<J>> obtainedServiceTimeMap = new TreeMap<> ();

  /** Gets the jobs currently in execution.
   * 
   * @return The jobs currently in execution (non-{@code null}).
   * 
   */
  protected final Set<J> getJobsExecuting ()
  {
    if (hasJobsInServiceArea ())
      return this.obtainedServiceTimeMap.firstEntry ().getValue ();
    else
      return Collections.EMPTY_SET;
  }
  
  /** Gets the number of jobs in execution.
   * 
   * @return The number of jobs in execution.
   * 
   */
  protected final int getNumberOfJobsExecuting ()
  {
    return getJobsExecuting ().size ();
  }
  
  /** Internally administers the start of a job.
   * 
   * <p>
   * Does <i>not</i> affect super-class structures, does <i>not</i> schedule and does <i>not</i> notify.
   * 
   * @param job                    The job to start, non-{@code null}.
   * @param jobRequiredServiceTime The required service time of the job, non-negative.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or the required service time is strictly negative.
   * 
   */
  protected final void startJobInternalAdministration (final J job, final double jobRequiredServiceTime)
  {
    if (jobRequiredServiceTime < 0)
      throw new IllegalArgumentException ();
    sanityInternalAdministration ();
    this.requiredServiceTime.put (job, jobRequiredServiceTime);
    final Set<J> jobs_zero_ost;
    if (! this.obtainedServiceTimeMap.containsKey (0.0))
      this.obtainedServiceTimeMap.put (0.0, new LinkedHashSet<> ());
    this.obtainedServiceTimeMap.get (0.0).add (job);
  }
  
  /** Returns the minimum obtained service time (i.e., of all the jobs currently in execution).
   * 
   * @return The minimum obtained service time, non-negative.
   * 
   * @throws IllegalStateException If there are currently no jobs in execution (i.e., no jobs in the service area).
   * 
   * @see #hasJobsInServiceArea
   * 
   */
  protected final double getMinimumObtainedServiceTime ()
  {
    if (! hasJobsInServiceArea ())
      throw new IllegalStateException ();
    return this.obtainedServiceTimeMap.firstKey ();
  }
  
  /** Removes the given job, if present, from the internal administration.
   * 
   * <p>
   * Does <i>not</i> remove the job from super-class structures.
   * 
   * @param job The job to remove, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null}.
   * 
   */
  protected final void removeJobFromInternalAdministration (final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    sanityInternalAdministration ();
    this.requiredServiceTime.remove (job);
    if (! getJobsInServiceArea ().contains (job))
      return;
    final Iterator<Entry<Double, Set<J>>> ost_i = this.obtainedServiceTimeMap.entrySet ().iterator ();
    while (ost_i.hasNext ())
    {
      final Entry<Double, Set<J>> entry = ost_i.next ();
      final Set<J> jobs = entry.getValue ();
      if (jobs.contains (job))
      {
        jobs.remove (job);
        if (jobs.isEmpty ())
          ost_i.remove ();
        return;
      }
    }
    throw new IllegalStateException ();
  }
  
  /** Calculates the time until the next catch-up.
   * 
   * @return The time until the next catch-up.
   * 
   * @throws IllegalStateException If the current state of the queue does not allow a catch-up.
   * 
   */
  protected final double getTimeToCatchUp ()
  {
    sanityInternalAdministration ();
    if (this.obtainedServiceTimeMap.size () < 2)
      throw new IllegalStateException ();
    final Iterator<Entry<Double, Set<J>>> ost_i = this.obtainedServiceTimeMap.entrySet ().iterator ();
    final Entry<Double, Set<J>> entry_first = ost_i.next ();
    final Entry<Double, Set<J>> entry_second = ost_i.next ();
    final double ost_first = entry_first.getKey ();
    final double ost_second = entry_second.getKey ();
    final double diff_ost = ost_second - ost_first;
    if (diff_ost <= 0)
      throw new IllegalStateException ();
    return diff_ost * getNumberOfJobsExecuting ();
  }
  
  /** Performs sanity checks on the internal administration.
   * 
   * <p>
   * Used for debugging.
   * The checks, however, are quite expensive.
   * 
   * <p>
   * This method has no effect if all sanity checks pass.
   * 
   * <p>
   * Unless commented out due to code maturity and/or performance considerations,
   * invocations are logged at level {@link Level#FINER}.
   * 
   * @throws IllegalStateException If the internal administration is inconsistent.
   * 
   */
  protected final void sanityInternalAdministration ()
  {
    LOGGER.log (Level.FINER, "t={0}, sanity on {1}: jobsInServiceArea={2}; obtainedServiceTimeMap={3}.", new Object[]{
      getLastUpdateTime (),
      this,
      getJobsInServiceArea (),
      this.obtainedServiceTimeMap});
    if (this.obtainedServiceTimeMap == null)
      throw new IllegalStateException ();
    if (this.requiredServiceTime.size () != getNumberOfJobsInServiceArea ())
      throw new IllegalStateException ();
    int numberOfJobsInObtainedServiceTimeMap = 0;
    for (final Set<J> jobs : this.obtainedServiceTimeMap.values ())
      numberOfJobsInObtainedServiceTimeMap += jobs.size ();
    if (numberOfJobsInObtainedServiceTimeMap != getNumberOfJobsInServiceArea ())
      throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clears the internal administration.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.requiredServiceTime.clear ();
    this.obtainedServiceTimeMap.clear ();
    this.lastCatchUpTime = Double.NaN;
    // Note that we use eventsScheduled in order to automatically have departure and catch-up events cancelled.
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates the internal administration (i.c., obtained service times) for executing jobs in the service area.
   * 
   * <p>
   * This method is called as an update hook, and not meant to be called from user code (in sub-classes).
   * It is left protected for {@code javadoc}.
   * 
   * <p>
   * This method automatically performs "catch-ups".
   * 
   * @param newTime The new time.
   * 
   * @see #hasJobsInServiceArea
   * @see #getJobsExecuting
   * @see #getLastUpdateTime
   * @see #registerPreUpdateHook
   * 
   */
  protected final void updateObtainedServiceTime (final double newTime)
  {
    if (newTime < getLastUpdateTime ())
      throw new IllegalStateException ();
    sanityInternalAdministration ();
    if (newTime == getLastUpdateTime ())
      return;
    if (hasJobsInServiceArea ())
    {
      final double ost_old = this.obtainedServiceTimeMap.firstKey ();
      final int numberOfJobsExecuting = this.obtainedServiceTimeMap.firstEntry ().getValue ().size ();
      final double ost_new = ost_old + ((newTime - getLastUpdateTime ()) / numberOfJobsExecuting);
      final Set<J> jobs_ost_old = this.obtainedServiceTimeMap.get (ost_old);
      if (this.obtainedServiceTimeMap.size () > 1)
      {
        final double ost_runner_up = this.obtainedServiceTimeMap.higherKey (ost_old);
        this.obtainedServiceTimeMap.remove (ost_old);
        if (ost_new > ost_runner_up + CUPS.TOLERANCE_OST)
          throw new IllegalStateException ();
        else if (ost_new >= ost_runner_up - CUPS.TOLERANCE_OST)
        {
          // We have "catch-up".
          this.obtainedServiceTimeMap.get (ost_runner_up).addAll (jobs_ost_old);
          this.lastCatchUpTime = newTime;
        }
        else
          this.obtainedServiceTimeMap.put (ost_new, jobs_ost_old);
      }
      else
      {
        this.obtainedServiceTimeMap.remove (ost_old);
        this.obtainedServiceTimeMap.put (ost_new, jobs_ost_old);
      }
    }
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
    rescheduleAfterQueueEvent (time, job, null, false);
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
   * Also removes the job from {@link #jobQueue} and {@link #jobsInServiceArea}.
   * 
   * @see #revoke
   * @see #removeJobFromInternalAdministration
   * @see #jobQueue
   * @see #jobsInServiceArea
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    removeJobFromInternalAdministration (job);
    this.jobQueue.remove (job);
    this.jobsInServiceArea.remove (job);
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
    rescheduleAfterQueueEvent (time, null, job, false);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #rescheduleAfterQueueEvent} with <code>null</code> job and {@code false} catch-up arguments.
   * 
   * @see #setServerAccessCredits
   * @see #rescheduleAfterQueueEvent
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    rescheduleAfterQueueEvent (time, null, null, false);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CATCH-UP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private double lastCatchUpTime = Double.NaN;
  
  /** Calls {@link #update}
   *  followed by {@link #rescheduleAfterQueueEvent} with arguments indicating a catch-up.
   * 
   * <p>
   * Always invoked from (only) {@link CatchUpEvent}.
   * 
   * <p>
   * Note that the actual catch-up is performed in {@link #updateObtainedServiceTime} through the pre-update hook.
   * This method does however check that there are actually two groups of jobs in the service area with different obtained service
   * times.
   * Otherwise, we are illegally scheduled unless a recent update took place without rescheduling (e.g., a Queue Access Vacation).
   * It also checks that after the {@link #update}, the number of such groups has decreased by exactly one,
   * again considering that we may have missed the catch-up due to a recent {@link #update}.
   * 
   * <p>
   * For internal use but made protected for documentation purposes.
   * 
   * @param time The time of the catch-up event (current time).
   * 
   * @throws IllegalStateException If this method is invoked at an inappropriate moment,
   *                               or the pre-update hook fails to perform the expected catch-up
   *                               (in both cases, taking into consideration the possibility of missed updates).
   * 
   * @see #CatchUpEvent
   * @see #catchUp
   * @see #rescheduleAfterQueueEvent
   * 
   */
  protected final void catchUp (final double time)
  {
    // Determine whether we had a recent (enough) catch-up.
    if ((! Double.isNaN (this.lastCatchUpTime)) && this.lastCatchUpTime > time)
      throw new IllegalStateException ();
    final boolean recentCatchUp = (! Double.isNaN (this.lastCatchUpTime)) && this.lastCatchUpTime >= time - CUPS.TOLERANCE_OST;
    final int oldObtainedServiceTimeMapSize = this.obtainedServiceTimeMap.size ();
    if (oldObtainedServiceTimeMapSize < 2 && ! recentCatchUp)
      // We are mis-scheduled and there has been no recent catch-up to justify it.
      throw new IllegalStateException ("illegally scheduled catch-up event at t=" + time
        + ", old obtained service-time map: " + this.obtainedServiceTimeMap + ".");
    update (time);
    if (this.obtainedServiceTimeMap.size () != oldObtainedServiceTimeMapSize - 1 && ! recentCatchUp)
      // No catch up took place in the update and there has been no recent catch-up to justify it.
      throw new IllegalStateException ("missed catch-up at t=" + time
        + ": old number of groups: " + oldObtainedServiceTimeMapSize
        + ", new obtained service-time map: " + this.obtainedServiceTimeMap + ".");
    rescheduleAfterQueueEvent (time, null, null, true);
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
    rescheduleAfterQueueEvent (time, null, departedJob, false);
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
      cancelDepartureEvent (departureEvents.iterator ().next ());
    sanityInternalAdministration ();
    if (hasJobsInServiceArea ())
    {
      final Set<J> jobsExecuting = getJobsExecuting ();
      if (jobsExecuting.isEmpty ())
        throw new IllegalStateException ();
      boolean first = true;
      J leaver = null;
      double rst_leaver = Double.POSITIVE_INFINITY;
      for (final J job : jobsExecuting)
        if (first)
        {
          first = false;
          leaver = job;
          rst_leaver = this.requiredServiceTime.get (job); 
        }
        else
        {
          final double rst_job = this.requiredServiceTime.get (job);
          if (rst_job < rst_leaver)
          {
            leaver = job;
            rst_leaver = rst_job;
          }
        }
      final int numberOfJobsExecuting = jobsExecuting.size ();
      final double ost = getMinimumObtainedServiceTime ();
      final double timeToDeparture = Math.max (rst_leaver - ost, 0.0) * numberOfJobsExecuting;
      scheduleDepartureEvent (time + timeToDeparture, leaver);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CATCH-UP EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** A schedule-able {@link SimEvent} for catch-ups of which a single instance is used throughout in this class.
   * 
   * @see #catchUpEvent
   * 
   */
  protected final static class CatchUpEvent 
  extends SimQueueCatchUpEvent
  {

    /** Creates a new catch-up event.
     * 
     * @param queue       The {@link CUPS} queue for which to create the event.
     * @param catchUpTime The scheduled time of catch-up (event can be reused, so time may be changed on the event).
     * @param action      The action to take upon the scheduled catch-up.
     * 
     */
    public CatchUpEvent (final CUPS queue, final double catchUpTime, SimEventAction action)
    {
      super (queue, catchUpTime, action);
    }
    
  }
  
  /** The single catch-up event.
   * 
   * Invokes {@link CUPS#catchUp} on the enclosing instance.
   * 
   * @see CatchUpEvent
   * @see CUPS#catchUp
   * 
   */
  protected final CatchUpEvent catchUpEvent =
    new CatchUpEvent (this, Double.NEGATIVE_INFINITY, (SimEventAction) (final SimEvent event) ->
    {
      CUPS.this.catchUp (event.getTime ());
    });
  
  /** Cancels a pending catch-up event (if scheduled).
   * 
   * <p>
   * The implementation simply removes the single catch-up event from the event list and from {@link #eventsScheduled}.
   * 
   * @param time The current time.
   * 
   * @see CatchUpEvent
   * @see #catchUpEvent
   * @see #getEventList
   * @see #eventsScheduled
   * 
   */
  protected final void cancelCatchUpEvent (final double time)
  {
    getEventList ().remove (this.catchUpEvent);
    this.eventsScheduled.remove (this.catchUpEvent);
  }
  
  /** Reschedules a catch-up event.
   * 
   * @param time          The current time.
   * @param mustBePresent For sanity checking; if <code>true</code>, a <i>single</i> catch-up event
   *                        <i>must</i> currently be scheduled on the event list.
   * @param mustBeAbsent  For sanity checking; if <code>true</code>, <i>no</i> catch-up event
   *                        must currently be scheduled on the event list.
   * 
   * @throws IllegalArgumentException If <code>mustBePresent</code> and <code>mustBeAbsent</code> are both <code>true</code>.
   * 
   * @see #getTimeToCatchUp
   * @see CatchUpEvent
   * @see #catchUpEvent
   * 
   */
  protected final void rescheduleCatchUpEvent (final double time, final boolean mustBePresent, final boolean mustBeAbsent)
  {
    if (mustBePresent && mustBeAbsent)
      throw new IllegalArgumentException ();
    final boolean isPresent = this.eventsScheduled.contains (this.catchUpEvent);
    if (mustBePresent && ! isPresent)
      throw new IllegalStateException ();
    if (mustBeAbsent && isPresent)
      throw new IllegalStateException ();
    if (isPresent)
      getEventList ().remove (this.catchUpEvent);
    getEventList ().schedule (time + getTimeToCatchUp (), this.catchUpEvent);
    this.eventsScheduled.add (this.catchUpEvent);
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE AFTER QUEUE EVENT (CENTRAL RESCHEDULING METHOD)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules after a (major) queue event (arrival, catch-up, departure, revocation or new server-access credits).
   * 
   * <p>
   * Core rescheduling method for {@link CUPS}.
   * 
   * <p>
   * This method
   * <ul>
   * <li>Removes any pending departure event for an exiting job (if any).
   *     This is not necessary for departing jobs, but <i>it is</i> for revoked jobs, and this method cannot tell the difference
   *     between the two.
   * <li>Removes any pending catch-up event.
   * <li>Unless we are invoked as a result of a catch-up,
   *     admits jobs for service based upon the number of jobs waiting and the remaining number of server access credits.
   *     Waiting jobs are admitted to the server in order of arrival (which is only relevant in the presence of finite
   *     server-access credits). The server-access credits taken and the jobs started are <i>not</i> reported at this stage.
   *     The started jobs are inserted into {@link #jobsInServiceArea} and into the internal administration.
   * <li>Reschedules if needed (at least one job in the service area) the single departure event for the job
   *     in {@link #getJobsExecuting} with the smallest required service time through {@link #rescheduleDepartureEvent}.
   * <li>Reschedules if needed (at least two job in the service area with different obtained service times)
   *     the single catch-up event through {@link #rescheduleCatchUpEvent}.
   * <li>Reports started jobs (if applicable).
   * <li>Reports if out of server-access credits.
   * </ul>
   * 
   * @param time    The current time (of the event).
   * @param aJob    The job that arrived, <code>null</code> if the event was not an arrival.
   * @param eJob    The job that exited (departed or successfully revoked),
   *                <code>null</code> if the event was not the exit of a job.
   * @param catchUp Whether rescheduling due to a catch-up.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #cancelDepartureEvent
   * @see #cancelCatchUpEvent
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #rescheduleDepartureEvent
   * @see #rescheduleCatchUpEvent
   * @see #fireStart
   * @see #fireIfOutOfServerAccessCredits
   * 
   */
  protected final void rescheduleAfterQueueEvent (final double time, final J aJob, final J eJob, final boolean catchUp)
  {
    if (  (aJob != null && eJob != null)
       || (aJob != null && catchUp)
       || (eJob != null && catchUp))
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
    }
    else if (eJob != null)
    {
      if (this.jobQueue.contains (eJob))
        throw new IllegalStateException ();
      if (this.jobsInServiceArea.contains (eJob))
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
    else if (catchUp)
    {
      departureEventMustBePresent = true;
      departureEventMustBeAbsent = false;
    }
    cancelCatchUpEvent (time);
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    if (! catchUp)
    {
      while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      {
        takeServerAccessCredit (false);
        final J job = getFirstJobInWaitingArea ();
        if (job == null)
          throw new IllegalStateException ();
        final double jobServiceTime = job.getServiceTime (this);
        if (jobServiceTime < 0)
          throw new RuntimeException ();
        startJobInternalAdministration (job, jobServiceTime);
        this.jobsInServiceArea.add (job);
        // Defer notifications until we are in a valid state again.
        startedJobs.add (job);
      }
    }
    if (getNumberOfJobsInServiceArea () > 0)
    {
      rescheduleDepartureEvent (time, departureEventMustBePresent, departureEventMustBeAbsent);
      if (this.obtainedServiceTimeMap.size () >= 2)
        rescheduleCatchUpEvent (time, false, true);
    }
    sanityInternalAdministration ();
    // Notification section.
    for (J j : startedJobs)
      // Be cautious here; previous invocation(s) of fireStart could have removed the job j already!
      if (this.jobsInServiceArea.contains (j))
        fireStart (time, j, (Q) this);
    if (creditsOld > 0 && getServerAccessCredits () == 0)
      fireIfOutOfServerAccessCredits (time);
  }
  
}
