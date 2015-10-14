package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** An abstract base class for non-preemptive single-server queueing disciplines
 *  for {@link SimJob}s.
 *
 * The class supports job revocations.
 * 
 * <p>This abstract class relies heavily on the partial {@link SimQueue} implementation of {@link AbstractNonPreemptiveSimQueue}.
 * This class {@link AbstractNonPreemptiveSingleServerSimQueue} implements most remaining abstract methods of
 * {@link AbstractNonPreemptiveSimQueue},
 * and takes care of maintenance of the internal data structures {@link AbstractSimQueue#jobQueue} and
 * {@link AbstractSimQueue#jobsExecuting}, by automatically taking into service the first job in {@link AbstractSimQueue#jobQueue}
 * and serve it until completion repeatedly.
 * Concrete implementations only have to insert an arriving job into {@link AbstractSimQueue#jobQueue} by
 * implementing {@link #insertJobInQueueUponArrival}.
 * Note that this restricts the use of this class to those non-preemptive single-server queueing disciplines
 * in which the order of service of jobs can only change due to job arrivals.
 * It is legal to permute <i>other</i> jobs than the arriving job in {@link AbstractSimQueue#jobQueue}.
 * If this is required, implementers should be aware that in most cases, one of the jobs in
 * the list is currently being served. Moving the job in service in the list is allowed, but it will have no effect.
 * 
 * <p>All concrete subclasses of {@link AbstractNonPreemptiveSingleServerSimQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 *
 * <p>
 * The <code>noWaitArmed</code> state to any implementation is / must be equivalent to no jobs being present in the system,
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * @see SimEventList#run
 * 
 */
public abstract class AbstractNonPreemptiveSingleServerSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveSingleServerSimQueue>
  extends AbstractNonPreemptiveSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  /** Creates a non-preemptive queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  protected AbstractNonPreemptiveSingleServerSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }

  /** Returns true if there are no jobs present in the system.
   * 
   * {@inheritDoc}
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

  /** Takes the arrived job into service if the server is currently idle and provided there are server-access credits.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * If conditions above are met, one credit is taken,
   * the <code>noWaitArmed</code> state is set to false, and the job's service time is requested through
   * {@link SimJob#getServiceTime}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent},
   * and listeners are notified through {@link #fireStart}.
   * 
   * <p>
   * If server-access credits are absent, this method does nothing, relying on {@link #rescheduleForNewServerAccessCredits}
   * and {@link #rescheduleAfterDeparture} to eventually take the job into service.
   * 
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #jobsExecuting
   * @see #fireNewNoWaitArmed
   * @see SimJob#getServiceTime
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobQueue.size () == 1)
    {
      if (! this.jobsExecuting.isEmpty ())
        throw new IllegalStateException ();
      fireNewNoWaitArmed (time, false);
      if (hasServerAcccessCredits ())
      {
        // XXX
        takeServerAccessCredit (true);
        this.jobsExecuting.add (job);
        final double jobServiceTime = job.getServiceTime (this);
        if (jobServiceTime < 0)
          throw new RuntimeException ();
        scheduleDepartureEvent (time + jobServiceTime, job);
        fireStart (time, job);
      }
    }
  }
    
  /** Reschedules if the server is idle as if a departure just took place.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * If the server is currently idle, and jobs are awaiting service, this method invokes {@link #rescheduleAfterDeparture}
   * with a <code>null</code> argument for the <code>departedJob</code>.
   * 
   * @see #jobQueue
   * @see #jobsExecuting
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    if (this.jobsExecuting.isEmpty () && ! this.jobQueue.isEmpty ())
      rescheduleAfterDeparture (null, time);
  }

  /** Reschedules as if the revoked job just departed, provided that the server is idle.
   * 
   * {@inheritDoc}
   * 
   * @see #jobsExecuting
   * @see #rescheduleAfterDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    if (this.jobsExecuting.isEmpty ())
      rescheduleAfterDeparture (job, time);
  }

  /** Takes the next job into service provided there are server-access credits.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * If there are server-access credits and one or more jobs waiting, one credit is taken,
   * the first job in {@link #jobQueue} is selected for service, and that job's service time is requested through
   * {@link SimJob#getServiceTime}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent},
   * and listeners are notified through {@link #fireStart}.
   * 
   * <p>
   * If server-access credits are absent or if there are no waiting jobs,
   * this method does nothing (but see below on <code>noWaitArmed</code>),
   * relying on {@link #rescheduleForNewServerAccessCredits}
   * and {@link #rescheduleAfterArrival} to eventually take a job into service.
   * 
   * <p>
   * Subsequently, if a job really left the queueing system (i.e., <code>departedJob != null</code>),
   * and the systems {@link #jobQueue} is left empty,
   * the (new) <code>noWaitArmed</code> is fired (being <code>true</code>).
   * 
   * @see #eventsScheduled
   * @see #jobQueue
   * @see #jobsExecuting
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see SimJob#getServiceTime
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * @see #fireNewNoWaitArmed
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture
    (final J departedJob, final double time)
  {
    if (! (this.eventsScheduled.isEmpty () && this.jobsExecuting.isEmpty ()))
      throw new IllegalStateException ();
    if ((! this.jobQueue.isEmpty ()) && hasServerAcccessCredits ())
    {
      // XXX
      takeServerAccessCredit (true);
      final J job = this.jobQueue.get (0);
      this.jobsExecuting.add (job);
      final double jobServiceTime = job.getServiceTime (this);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      scheduleDepartureEvent (time + jobServiceTime, job);
      fireStart (time, job);
    }
    if (departedJob != null && this.jobQueue.isEmpty ())
      fireNewNoWaitArmed (time, true);
  }
    
}
