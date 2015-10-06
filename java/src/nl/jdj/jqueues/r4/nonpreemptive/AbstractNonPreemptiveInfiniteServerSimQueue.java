package nl.jdj.jqueues.r4.nonpreemptive;

import java.util.Iterator;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a non-preemptive queueing system with an infinite number of servers.
 *
 * <p>Concrete implementations only have to provide the service-time for a job at the moment it is taken into service,
 * by implementing {@link #getServiceTime}.
 * 
 * <p>
 * In the presence of vacations, i.e., jobs are not immediately admitted to the servers,
 * this implementation respects the arrival order of jobs.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractNonPreemptiveInfiniteServerSimQueue
<J extends SimJob, Q extends AbstractNonPreemptiveInfiniteServerSimQueue>
extends AbstractNonPreemptiveSimQueue<J, Q>
{

  /** Creates a new {@link AbstractNonPreemptiveInfiniteServerSimQueue} with given {@link SimEventList}.
   * 
   * @param eventList The event list to use.
   * 
   */
  public AbstractNonPreemptiveInfiniteServerSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns the service time for a job at this {@link AbstractNonPreemptiveInfiniteServerSimQueue}.
   * 
   * Implementations must always return a non-negative value.
   * Otherwise, a {@link RuntimeException} is thrown in {@link #rescheduleAfterArrival}.
   * 
   * <p>
   * Implementations are free to <i>not</i> use {@link SimJob#getServiceTime}.
   * 
   * @param job The job, non-<code>null</code>.
   * @return The required service time, non-negative.
   * 
   */
  protected abstract double getServiceTime (J job);
  
  /** Inserts the job at the tail of {@link #jobQueue}.
   * 
   * <p>
   * This method is left non-<code>final</code>.
   * 
   * <p>
   * Overriding methods are free to insert the job in a different location in order to impose a non-FCFS job ordering
   * in presence of server-access vacations.
   * However, care is needed.
   * Implementations <i>must</i> insert the job in {@link #jobQueue}, yet must be aware that this collection contains
   * both jobs "waiting" and jobs "in service".
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Takes the arrived job into service immediately, provided there are server-access credits.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * If server-access credits are present, one credit is taken and the job's service time is requested through
   * {@link #getServiceTime}; throwing a {@link RuntimeException} if a negative service time is returned.
   * Subsequently, an appropriate departure event is scheduled through {@link #scheduleDepartureEvent},
   * and listeners are notified through {@link #fireStart}.
   * 
   * <p>
   * If server-access credits are absent, this method does nothing, relying on {@link #rescheduleForNewServerAccessCredits}
   * to eventually take the job into service.
   * 
   * @see #hasServerAcccessCredits
   * @see #takeServerAccessCredit
   * @see #jobsExecuting
   * @see #getServiceTime
   * @see #scheduleDepartureEvent
   * @see #fireStart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (hasServerAcccessCredits ())
    {
      takeServerAccessCredit ();
      this.jobsExecuting.add (job);
      final double jobServiceTime = getServiceTime (job);
      if (jobServiceTime < 0)
        throw new RuntimeException ();
      scheduleDepartureEvent (time + jobServiceTime, job);
      fireStart (time, job);
    }
  }

  /** Repeatedly starts jobs waiting as long as there are server-access credits.
   * 
   * {@inheritDoc}
   * 
   * <p>
   * As long as there are service-access credits,
   * jobs are started one at a time, as if they are new arrivals, through {@link #rescheduleAfterArrival}.
   * Jobs are started in order of appearance in an {@link Iterator} over {@link #jobQueue}.
   * 
   * @see #jobQueue
   * @see #jobsExecuting
   * @see #hasServerAcccessCredits
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (this.jobsExecuting.size () < this.jobQueue.size () && hasServerAcccessCredits ())
      for (J j : this.jobQueue)
      {
        if (! this.jobsExecuting.contains (j))
        {
          rescheduleAfterArrival (j, time);
          break;
        }
        throw new IllegalStateException ();
      }
  }

  /** Does nothing.
   * 
   * No rescheduling is required after a successful revocation.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   * No rescheduling is required after a departure.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

}
