package nl.jdj.jqueues.r4.processorsharing;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link PS} queue serves all jobs simultaneously, equally distributing its service capacity.
 *
 * <p>
 * WORK IN PROGRESS! ABSTRACT FOR NOW!
 * 
 * <p>
 * Processor Sharing.
 * 
 * <p>
 * This implementation has infinite buffer size.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class PS<J extends SimJob, Q extends PS> extends AbstractProcessorSharingSimQueue<J, Q>
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
    // return new PS<> (getEventList ());
    throw new UnsupportedOperationException ();
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

  private double virtualTime = 0;
  
  protected final double getVirtualTime ()
  {
    return this.virtualTime;
  }
  
  private final SortedMap<Double, Set<J>> departureEpochsInVirtualTime = new TreeMap<> ();
  
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
  
  /** Calls super method, resets the virtual time to zero and clears the departure-epochs mapping.
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
    this.virtualTime = 0;
    this.departureEpochsInVirtualTime.clear ();
  }
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

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
    if (! this.departureEpochsInVirtualTime.isEmpty ())
    {
      if (getNumberOfJobsExecuting () == 0)
        throw new IllegalStateException ();
      final double scheduleVirtualTime = this.departureEpochsInVirtualTime.firstKey ();
      final double deltaVirtualTime = scheduleVirtualTime - getVirtualTime ();
      if (deltaVirtualTime < 0)
        throw new IllegalStateException ();
      final double deltaTime = deltaVirtualTime * getNumberOfJobsExecuting ();
      final J job = this.departureEpochsInVirtualTime.get (scheduleVirtualTime).iterator ().next ();
      scheduleDepartureEvent (time + deltaTime, job);
    }
  }
  
  protected final void rescheduleAfterQueueEvent (final double time, final J aJob, final J dJob)
  {
    if (aJob != null && dJob != null)
      throw new IllegalArgumentException ();
    if (dJob != null)
    {
      // XXX TODO: This implicitly assumes departure; not revokation/drop...
      // Well, we never drop a job, so that solves one issue.
      // How do we know the virtual departure time of a job being revoked???
      // XXX -> Still need a mapping from J -> virtual departure time.
      // OR: Manually find the job in our Map, because we "do not revoke that often...".
      if (! this.departureEpochsInVirtualTime.firstKey ().equals (time)
        || ! this.departureEpochsInVirtualTime.get (time).contains (dJob))
        throw new IllegalStateException ();
      this.departureEpochsInVirtualTime.get (time).remove (dJob);
      if (this.departureEpochsInVirtualTime.get (time).isEmpty ())
        this.departureEpochsInVirtualTime.remove (time);
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
      if (! this.departureEpochsInVirtualTime.containsKey (jobVirtualDepartureTime))
        this.departureEpochsInVirtualTime.put (jobVirtualDepartureTime, new LinkedHashSet<J> ());
      this.departureEpochsInVirtualTime.get (jobVirtualDepartureTime).add (job);
      //scheduleDepartureEvent (time + jobServiceTime, job);
      // Defer notifications until we are in a valid state again.
      startedJobs.add (job);
    }
    if (getNumberOfJobsExecuting () > 0)
      rescheduleDepartureEvent (time, dJob == null && getNumberOfJobsExecuting () > 0, dJob != null);
    // Notification section.
    for (J j : startedJobs)
      // Be cautious here; previous invocation(s) of fireStart could have removed the job j already!
      if (this.jobsExecuting.contains (j))
        fireStart (time, j);
    fireIfOutOfServerAccessCredits (time);
    if (dJob != null || ! startedJobs.isEmpty ())
      fireNewNoWaitArmed (time, isNoWaitArmed ());
  }
  
}
