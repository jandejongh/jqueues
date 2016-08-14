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
  // RESET
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
  // ARRIVAL
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

  /** Starts the arrived job if server-access credits are available and if there are no jobs in the service area.
   * 
   * @see #hasServerAcccessCredits
   * @see #getNumberOfJobsInServiceArea
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (hasServerAcccessCredits ()
    &&  getNumberOfJobsInServiceArea () == 0)
      start (time, job);
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
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
  // REVOCATION
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

  /** Starts the next job in the waiting area if server-access credits are available and if there are no jobs in the service area.
   * 
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #getNumberOfJobsInServiceArea
   * @see #start
   * @see #getNextJobToServeInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    if (hasServerAcccessCredits ()
    &&  hasJobsInWaitingArea ()
    &&  getNumberOfJobsInServiceArea () == 0)
      start (time, getNextJobToServeInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER ACCCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Starts the next job in the waiting area if there are no jobs in the service area.
   * 
   * @see #hasJobsInWaitingArea
   * @see #getNumberOfJobsInServiceArea
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    if (hasJobsInWaitingArea ()
    &&  getNumberOfJobsInServiceArea () == 0)
      start (time, getNextJobToServeInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are no jobs present in the service area.
   * 
   * @return True if there are no jobs present in the service area.
   * 
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return getNumberOfJobsInServiceArea () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Adds the job to the tail of the service area.
   * 
   * @see #jobsInServiceArea
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || getJobsInServiceArea ().contains (job))
      throw new IllegalArgumentException ();
    this.jobsInServiceArea.add (job);
  }

  /** Reschedules due to the start of a job, making it depart immediately if its requested service time is zero,
   *  or rescheduling the (single) departure event of this queue otherwise.
   * 
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
    || (! getJobsInServiceArea ().contains (job)))
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
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
  
  /** Checks the presence of the departing job in {@link #jobQueue}, {@link #jobsInServiceArea}, and {@link #jobsQoSMap},
   *  and removes the job from those collections.
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

  /** Starts the next job in the waiting area (if available) if server-access credits are available.
   * 
   * @throws IllegalStateException If there are jobs in the service area (i.e., being served).
   * 
   * @see #getNumberOfJobsInServiceArea
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getNextJobToServeInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (getNumberOfJobsInServiceArea () > 0)
      throw new IllegalStateException ();
    if (hasServerAcccessCredits ()
    &&  hasJobsInWaitingArea ())
      start (time, getNextJobToServeInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE DEPARTURE EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules the single departure event for this queue, departing a job immediately if its required service time is zero.
   * 
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #jobsInServiceArea
   * @see #getFirstJobInServiceArea
   * @see #getServiceTimeForJob
   * @see #scheduleDepartureEvent
   * @see #depart
   * @see #getLastUpdateTime
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
    if (getNumberOfJobsInServiceArea () > 1)
      throw new IllegalStateException ();
    if (getNumberOfJobsInServiceArea () == 1)
    {
      final J job = getFirstJobInServiceArea ();
      final double jobServiceTime = getServiceTimeForJob (job);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      if (jobServiceTime > 0)
        scheduleDepartureEvent (getLastUpdateTime () + jobServiceTime, job);
      else
        depart (getLastUpdateTime (), job);
    }
  }
  
}
