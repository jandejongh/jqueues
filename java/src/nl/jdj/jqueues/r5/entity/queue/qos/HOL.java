package nl.jdj.jqueues.r5.entity.queue.qos;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The Head-of-the-Line (HOL) queueing discipline.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public class HOL<J extends SimJob, Q extends HOL, P extends Comparable>
extends AbstractSimQueueQoS<J, Q, P>
implements SimQueueQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link HOL}.
   * 
   * @param eventList     The event list to use, non-{@code null}.
   * @param qosClass      The Java class to use for QoS behavior, non-{@code null}.
   * @param defaultJobQoS The default QoS value to use for non-QoS jobs, non-{@code null}. 
   * 
   * @throws IllegalArgumentException If any of the arguments is <code>null</code>.
   * 
   */
  public HOL (final SimEventList eventList, final Class<P> qosClass, final P defaultJobQoS)
  {
    super (eventList, qosClass, defaultJobQoS);
  }

  /** Returns a new {@link HOL} object on the same {@link SimEventList} with the same QoS structure.
   * 
   * @return A new {@link HOL} object on the same {@link SimEventList} with the same QoS structure.
   * 
   * @see #getEventList
   * @see #getQoSClass
   * @see #getDefaultJobQoS
   * 
   */
  @Override
  public HOL<J, Q, P> getCopySimQueue () throws UnsupportedOperationException
  {
    return new HOL (getEventList (), getQoSClass (), getDefaultJobQoS ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "HOL".
   * 
   * @return "HOL".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "HOL";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: RESET ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: NoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are no jobs present in the system.
   * 
   * @return True if there are no jobs present in the system.
   * 
   * @see #getNumberOfJobs
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getNumberOfJobs () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job into {@link #jobQueue} (tail) and {@link #jobsQoSMap}.
   * 
   * @see #getAndCheckJobQoS
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (job == null || this.jobQueue.contains (job) || job.getQueue () != null)
      throw new IllegalArgumentException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    this.jobQueue.add (job);
    final P qos = getAndCheckJobQoS (job);
    if (! this.jobsQoSMap.containsKey (qos))
      this.jobsQoSMap.put (qos, new LinkedHashSet<> ());
    this.jobsQoSMap.get (qos).add (job);
  }

  /** Invokes {@link #rescheduleAfterDeparture} passing <code>null</code> as job argument.
   * 
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    rescheduleAfterDeparture (null, time);
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Throws an {@link IllegalStateException} because drops are unexpected.
   * 
   * @throws IllegalStateException Always.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws an {@link IllegalStateException} because drops are unexpected.
   * 
   * @throws IllegalStateException Always.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from the internal data structures, and if needed, cancels a pending departure event.
   * 
   * <p>
   * If the job is in service, its departure event is canceled through {@link #cancelDepartureEvent},
   * and the job is removed from {@link #jobsInServiceArea}.
   * Subsequently, whether the job was in service or not,
   * it is removed from {@link #jobQueue},
   * and {@link #jobsQoSMap}
   * and <code>true</code> is returned.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #cancelDepartureEvent
   * @see #jobsQoSMap
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
    final P qos = getAndCheckJobQoS (job);
    if (! this.jobsQoSMap.containsKey (qos))
      throw new IllegalStateException ();
    if (this.jobsQoSMap.get (qos) == null || ! this.jobsQoSMap.get (qos).contains (job))
      throw new IllegalStateException ();
    this.jobsQoSMap.get (qos).remove (job);
    if (this.jobsQoSMap.get (qos).isEmpty ())
      this.jobsQoSMap.remove (qos);
  }

  /** Invokes {@link #rescheduleAfterDeparture} passing revoked job as argument.
   * 
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    rescheduleAfterDeparture (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: SERVER ACCCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Invokes {@link #rescheduleAfterDeparture} passing <code>null</code> as job argument.
   * 
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    rescheduleAfterDeparture (null, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Checks the presence of the departing job in {@link #jobQueue}, {@link #jobsInServiceArea}, and {@link #jobsQoSMap},
   * and removes the job from those collections.
   * 
   * @throws IllegalStateException If the <code>departingJob</code> is not in one of the lists.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #jobsQoSMap
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
    final P qos = getAndCheckJobQoS (departingJob);
    if (! this.jobsQoSMap.containsKey (qos))
      throw new IllegalStateException ();
    if (this.jobsQoSMap.get (qos) == null || ! this.jobsQoSMap.get (qos).contains (departingJob))
      throw new IllegalStateException ();
    this.jobsQoSMap.get (qos).remove (departingJob);
    if (this.jobsQoSMap.get (qos).isEmpty ())
      this.jobsQoSMap.remove (qos);
  }
    
  /** Takes a job into service if the server is idle and server-access credits available.
   * 
   * <p>
   * In the current implementation, this method serves as the central point for rescheduling,
   * including after arrivals, revocations and new server-access credits.
   * 
   * <p>
   * If there are server-access credits, at least one jobs waiting, and the server is available,
   * one credit is taken, and the first waiting job in {@link #jobQueue} is selected for service
   * by using {@link #getNextJobToServeInWaitingArea}.
   * The job's service time is requested through
   * {@link SimJob#getServiceTime}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent},
   * but no start notifications are sent at this point.
   * If, however, the job requests zero service time, it departs immediately,
   * and the next job in the waiting area is considered.
   * 
   * <p>Subsequently, listeners are notified through {@link #fireStart} if a job started
   * (and is still present as jobs may have left due to previous notifications), and, if applicable,
   * {@link #fireDeparture}.
   * 
   * <p>
   * Finally, the server-access-credits availability and {@code noWaitArmed} state are reassessed,
   * and changes are notified through
   * {@link #fireIfNewServerAccessCreditsAvailability}
   * and
   * {@link #fireIfNewNoWaitArmed},
   * respectively.
   * 
   * @see #jobQueue
   * @see #getNumberOfJobs
   * @see #jobsInServiceArea
   * @see #getNumberOfJobsInServiceArea
   * @see #getNextJobToServeInWaitingArea
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see SimJob#getServiceTime
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * @see #fireDeparture
   * @see #fireIfNewServerAccessCreditsAvailability
   * @see #fireIfNewNoWaitArmed
   * 
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    // Scheduling section; make sure we do not issue notifications.
    final Set<J> startedJobs = new LinkedHashSet<> ();
    final Set<J> departedJobs = new LinkedHashSet<> ();
    while (hasServerAcccessCredits ()
      && hasJobsInWaitingArea ()
      && getNumberOfJobsInServiceArea () < 1)
    {
      takeServerAccessCredit (false);
      final J job = getNextJobToServeInWaitingArea ();
      if (job == null)
        throw new IllegalStateException ();
      this.jobsInServiceArea.add (job);
      final double jobServiceTime = job.getServiceTime (this);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      if (jobServiceTime > 0)
        scheduleDepartureEvent (time + jobServiceTime, job);
      else
      {
        removeJobFromQueueUponDeparture (job, time);
        job.setQueue (null);
        departedJobs.add (job);
      }
      // Defer notifications until we are in a valid state again.
      startedJobs.add (job);
    }
    // Notification section.
    for (J j : startedJobs)
      fireStart (time, j, (Q) this);
    for (final J j : departedJobs)
      fireDeparture (time, j, (Q) this);
    fireIfNewServerAccessCreditsAvailability (time);
    fireIfNewNoWaitArmed (time, isNoWaitArmed ());
  }

}
