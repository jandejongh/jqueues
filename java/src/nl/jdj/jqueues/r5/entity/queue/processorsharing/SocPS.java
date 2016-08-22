package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server "social processor-sharing" queue serves jobs in the service area simultaneously
 *  such that they all depart at the same time,
 *  distributing its service capacity to that effect.
 *
 * <p>
 * At each time, the service rate of a job in the service area is linearly proportional to its remaining service time.
 * (Note that this specification is actually essential; one can achieve simultaneous departures in many ways!)
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SocPS<J extends SimJob, Q extends SocPS>
extends AbstractProcessorSharingSimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOGGER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOGGER = Logger.getLogger (SocPS.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server SocPS queue with infinite buffer size given an event list.
   *
   * <p>
   * The constructor registers a pre-update hook that maintains the remaining service times.
   * 
   * @param eventList The event list to use.
   *
   * @see #registerPreUpdateHook
   * @see #updateRemainingServiceTimes
   * 
   */
  public SocPS (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE, 1);
    registerPreUpdateHook (this::updateRemainingServiceTimes);
  }
  
  /** Returns a new {@link SocPS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link SocPS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public SocPS<J, Q> getCopySimQueue ()
  {
    return new SocPS<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TOLERANCE IN REMAINING SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The tolerance for rounding errors in the remaining service time.
   * 
   */
  public final static double TOLERANCE_RST = 1.0E-9;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SocPS".
   * 
   * @return "SocPS".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SocPS";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INTERNAL (STATE) ADMINISTRATION
  // - remainingServiceTime
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The per-job (in the service area) remaining service time.
   * 
   * <p>
   * Not intended to be modified by sub-classes!
   * 
   */
  protected final Map<J, Double> remainingServiceTime = new HashMap<> ();

  /** Gets the total remaining work, i.e., the sum of remaining service times of all jobs in the service area.
   * 
   * @return The total remaining work, non-negative.
   * 
   * @see #remainingServiceTime
   * 
   */
  protected final double getRemainingWork ()
  {
    double remainingWork = 0;
    for (double remainingWork_j : this.remainingServiceTime.values ())
      remainingWork += remainingWork_j;
    return remainingWork;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clears the internal administration.
   * 
   * @see #remainingServiceTime
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.remainingServiceTime.clear ();
    // Note that we use eventsScheduled in order to automatically have departure events cancelled.
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates the internal administration (i.c., remaining service times) for executing jobs in the service area.
   * 
   * <p>
   * This method is called as an pre-update hook, and not meant to be called from user code (in sub-classes).
   * It is left protected for {@code javadoc}.
   * 
   * @param newTime The new time.
   * 
   * @see #hasJobsInServiceArea
   * @see #getLastUpdateTime
   * @see #remainingServiceTime
   * @see #registerPreUpdateHook
   * 
   */
  protected final void updateRemainingServiceTimes (final double newTime)
  {
    if (newTime < getLastUpdateTime ())
      throw new IllegalStateException ();
    if (newTime == getLastUpdateTime ())
      return;
    final double dT = newTime - getLastUpdateTime ();
    final double oldRemainingWork = getRemainingWork ();
    for (final J job : getJobsInServiceArea ())
    {
      if (! this.remainingServiceTime.containsKey (job))
        throw new IllegalStateException ();
      double newRst_j;
      if (oldRemainingWork < SocPS.TOLERANCE_RST)
        newRst_j = 0;
      else
      {
        final double oldRst_j = this.remainingServiceTime.get (job);
        if (oldRst_j < 0)
          throw new IllegalStateException ();
        newRst_j = oldRst_j - (oldRst_j / oldRemainingWork) * dT;
        if (newRst_j < - SocPS.TOLERANCE_RST)
          throw new IllegalStateException ();
        if (newRst_j < 0)
          newRst_j = 0;
      }
      this.remainingServiceTime.put (job, newRst_j);
    }
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
   * Removes the job from {@link #jobQueue}, {@link #jobsInServiceArea} and {@link #remainingServiceTime}.
   * 
   * @see #revoke
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #remainingServiceTime
   * @see #rescheduleAfterRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    this.jobQueue.remove (job);
    this.jobsInServiceArea.remove (job);
    this.remainingServiceTime.remove (job);
  }

  /** Calls {@link #rescheduleDepartureEvent}.
   * 
   * @see #revoke
   * @see #rescheduleDepartureEvent
   * @see #removeJobFromQueueUponRevokation
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }

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
  
  /** Inserts the job, after sanity checks, in the service area and administers its initial remaining service time.
   * 
   * @see #jobsInServiceArea
   * @see #getServiceTimeForJob
   * @see #remainingServiceTime
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || getJobsInServiceArea ().contains (job)
    || this.remainingServiceTime.containsKey (job))
      throw new IllegalArgumentException ();
    this.jobsInServiceArea.add (job);
    final double jobRequiredServiceTime = getServiceTimeForJob (job);
    if (jobRequiredServiceTime < 0)
      throw new RuntimeException ();
    this.remainingServiceTime.put (job, jobRequiredServiceTime);
  }

  /** Invokes {@link #rescheduleDepartureEvent}.
   * 
   * @see #rescheduleDepartureEvent
   * @see #insertJobInQueueUponStart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job))
    || (! this.remainingServiceTime.containsKey (job)))
      throw new IllegalArgumentException ();
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
    removeJobFromQueueUponRevokation (departingJob, time);
  }

  /** Makes sure all jobs left in the service area depart as well (through recursion).
   * 
   * <p>
   * All jobs in the service area depart simultaneously, if at all.
   * Hence, if a job departs, all other jobs in the service area must depart as well.
   * The main scheduling method, {@link #rescheduleDepartureEvent},
   * schedules (or invokes) a departure through {@link #depart} on a single job,
   * and it is up to us to make sure all jobs in the service area depart.
   * 
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (departedJob == null
    ||  this.jobQueue.contains (departedJob)
    ||  this.jobsInServiceArea.contains (departedJob)
    ||  this.remainingServiceTime.containsKey (departedJob))
      throw new IllegalArgumentException ();
    if (hasJobsInServiceArea ())
      depart (time, getFirstJobInServiceArea ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE DEPARTURE EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules the single departure event for this queue.
   * 
   * <p>
   * First, this method cancels any pending departure event.
   * 
   * <p>
   * If the remaining work of this queue is negligible, in view of {@link #TOLERANCE_RST},
   * or if the remaining work is finite but time is negative of positive infinity,
   * this method triggers the departure of all jobs in the service area, if any,
   * by invoking {@link #depart} on the first job in the service area,
   * relying on the departure handling for the other jobs to depart.
   * 
   * <p>
   * Otherwise, if the remaining work is finite and time is finite,
   * this method schedules a suitable departure event through {@link #scheduleDepartureEvent}
   * for the first job in the service area, if present,
   * again relying on the departure handling for the simultaneous departure of the other jobs in the service area.
   * 
   * <p>
   * Otherwise, the remaining work is infinite (or there are no jobs) and nothing needs to be done.
   * 
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #getLastUpdateTime
   * @see #getRemainingWork
   * @see #hasJobsInServiceArea
   * @see #getNumberOfJobsInServiceArea
   * @see #depart
   * @see #hasJobsInServiceArea
   * @see #TOLERANCE_RST
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
    final double time = getLastUpdateTime ();
    final double remainingWork = getRemainingWork ();
    if (hasJobsInServiceArea () && Double.isFinite (remainingWork))
    {
      // There are jobs in the service area and the remaining work is not infinite.
      // Note that with infinite remaining work, none of the jobs in the service area can depart
      // until all jobs with infinite service time have been revoked.
      if (Double.isInfinite (time))
      {
        // If time is positive or negative infinity, and the remaining work is finite,
        // the job in the service area is to depart immediately.
        // In this case, only a single job can be present in the service area!
        // Make it depart.
        if (getNumberOfJobsInServiceArea () > 1)
          throw new IllegalStateException ();
        // We only have to depart the first, or any, job in the service area.
        // The departure handlers will take care of the rest.
        depart (time, getFirstJobInServiceArea ());
        if (hasJobsInServiceArea ())
          throw new IllegalStateException ();
      }
      else
      {
        // We have finite time and finite remaining work.
        if (remainingWork < SocPS.TOLERANCE_RST)
        {
          // The remaining work is too small to justify a departure event.
          // Let all jobs in the service area depart now.
          // We only have to depart the first, or any, job in the service area.
          // The departure handlers will take care of the rest.
          depart (time, getFirstJobInServiceArea ());
          if (hasJobsInServiceArea ())
            throw new IllegalStateException ();
        }
        else
          // There is non-trivial work to do, so we schedule a departure event (in finite time).
          // We only schedule the departure of the first (could be any) job in the service area;
          // the departure handlers will make sure ALL job in the service area depart at that time.
          scheduleDepartureEvent (time + remainingWork, getFirstJobInServiceArea ());
      }
    }
    else
      // We either have no jobs in the service area, or at least one with infinite required service time.
      // No job can depart, so we have nothing to do...
      ;
  }
  
}
