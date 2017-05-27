package nl.jdj.jqueues.r5.entity.jq.queue.qos;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The Head-of-the-Line (HOL) queueing discipline.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
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
  // JOB QoS MAP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final NavigableMap<P, Set<J>> jobsQoSMap = new TreeMap<> ();
  
  @Override
  public final NavigableMap<P, Set<J>> getJobsQoSMap ()
  {
    return this.jobsQoSMap;
  }
  
  /** Gets the job in the waiting area that is next to serve.
   * 
   * <p>
   * Iterates over the job sets in increasing order of QoS value,
   * and iterates over the jobs within each set in order as enforced by the standard Java {@link Set} iterator,
   * and returns the first job it finds that is <i>not</i> in {@link #jobsInServiceArea}.
   * 
   * <p>
   * This method does (some) sanity checks on {@link #jobsQoSMap} on the fly.
   * 
   * @return The job in the waiting area that is next to serve, {@code null} if there are no waiting jobs.
   * 
   * @throws IllegalStateException If the {@link #jobsQoSMap} is in an illegal state.
   * 
   * @see #jobsQoSMap
   * @see #jobQueue
   * @see #jobsInServiceArea
   * 
   */
  protected final J getNextJobToServeInWaitingArea ()
  {
    for (final Set<J> jobsP: jobsQoSMap.values ())
      if (jobsP == null || jobsP.isEmpty ())
        throw new IllegalStateException ();
      else
        for (final J job : jobsP)
          if (job == null || ! this.jobQueue.contains (job))
            throw new IllegalStateException ();
          else if (! this.jobsInServiceArea.contains (job))
            return job;
    return null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method and clear {@link #jobsQoSMap}.
   * 
   * @see #jobsQoSMap
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.jobsQoSMap.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job into {@link #jobQueue} (tail) and {@link #jobsQoSMap}.
   * 
   * @see SimQueueQoSUtils#getAndCheckJobQoS
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
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
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
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
    {
      this.jobsInServiceArea.remove (job);
      cancelDepartureEvent (job);
    }
    this.jobQueue.remove (job);
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
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
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }

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

  /** Depending on the job's requested service time, makes it depart immediately, or schedules a suitable departure event.
   * 
   * <p>
   * Performs sanity checks on the fly (job present; job not yet started; requested service time zero or positive).
   * The time argument must match the result from {@link #getLastUpdateTime} (and is thus only present for sanity checking).
   * 
   * <p>
   * If a job has infinite requested service time, it will start but never depart,
   * even if the start is scheduled at positive or negative infinity.
   * 
   * <p>
   * With zero requested service time, a job departs immediately.
   * This is also the case if the start is at positive or negative infinity
   * AND the job has finite requested service time.
   * 
   * <p>
   * In all other cases, a suitable departure event is scheduled through {@link #rescheduleDepartureEvent}.
   * 
   * <p>
   * Caveat: the specification above implies that NOT all jobs in the service area will have a departure event
   * scheduled for them!
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
    if (Double.isFinite (jobServiceTime))
    {
      if (jobServiceTime == 0 || ! Double.isFinite (time))
        depart (time, job);
      else
        rescheduleDepartureEvent ();
    }
    else
      // Jobs with infinite requested service time never depart.
      ;
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
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (departingJob, this);
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
  
  /** Reschedules the single departure event for this queue.
   * 
   * <p>
   * Cancels a pending departure event and schedules the first job in the service area for departure (if ever).
   * 
   * <p>
   * If the job has infinite requested service time, this method does nothing,
   * even if the start is scheduled at positive or negative infinity.
   * 
   * <p>
   * With zero requested service time, the job departs immediately.
   * This is also the case if time ({@link #getLastUpdateTime}) is at positive or negative infinity
   * AND the job has finite requested service time.
   * 
   * <p>
   * In all other cases, a suitable departure event is scheduled through {@link #rescheduleDepartureEvent}.
   * 
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #jobsInServiceArea
   * @see #getFirstJobInServiceArea
   * @see #getServiceTimeForJob
   * @see #getLastUpdateTime
   * @see #scheduleDepartureEvent
   * @see #depart
   * @see #getLastUpdateTime
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
    if (getNumberOfJobsInServiceArea () > 1)
      throw new IllegalStateException ();
    if (getNumberOfJobsInServiceArea () == 1)
    {
      final J job = getFirstJobInServiceArea ();
      final double time = getLastUpdateTime ();
      final double jobServiceTime = getServiceTimeForJob (job);
      if (Double.isFinite (jobServiceTime))
      {
        if (jobServiceTime == 0 || Double.isInfinite (time))
          depart (time, job);
        else
          scheduleDepartureEvent (time + jobServiceTime, job);
      }
      else
        // Jobs with infinite requested service time never depart.
        ;
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
