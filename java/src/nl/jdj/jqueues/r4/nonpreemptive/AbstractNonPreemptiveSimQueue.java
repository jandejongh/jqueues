package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.serverless.NONE;
import nl.jdj.jsimulation.r4.SimEventList;

/** An abstract base class for non-preemptive queueing disciplines
 *  for {@link SimJob}s.
 *
 * The class supports job revocations.
 * 
 * <p>This abstract class relies heavily on the partial {@link SimQueue} implementation of {@link AbstractSimQueue}.
 * This class {@link AbstractNonPreemptiveSimQueue} implements those abstract methods of {@link AbstractSimQueue} that
 * do not depend on the service structure, apart from it being non-preemptive.
 * In particular, the methods implemented (and made final) do not depend on the number of servers (if any) in the queueing system.
 * 
 * <p>All concrete subclasses of {@link AbstractNonPreemptiveSimQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * @see SimEventList#run
 * @see AbstractNonPreemptiveSingleServerSimQueue
 * @see AbstractNonPreemptiveInfiniteServerSimQueue
 * @see NONE
 * 
 */
public abstract class AbstractNonPreemptiveSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveSimQueue>
  extends AbstractSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  /** Creates a non-preemptive queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  protected AbstractNonPreemptiveSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }

  /** Invokes {@link #removeJobFromQueueUponRevokation}, requesting <code>interruptService</code>.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time, true);
  }

  /** Invokes {@link #rescheduleAfterRevokation}.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    rescheduleAfterRevokation (job, time);
  }

  /** If possible, removes the job from the internal data structures, and cancels a pending departure event.
   * 
   * {@inheritDoc}
   * 
   * If the job is already in service, and the <code>interruptService</code> argument is set to <code>false</code>,
   * this method returns <code>false</code>, by contract of {@link SimQueue}.
   * Otherwise, if the job is in service, its departure event is canceled through {@link #cancelDepartureEvent},
   * and the job is removed from {@link #jobsExecuting} and {@link #jobQueue}.
   * Subsequently, whether the job was in service or not, it is removed from {@link #jobQueue},
   * and <code>true</code> is returned.
   * 
   * @see #jobQueue
   * @see #jobsExecuting
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsExecuting.contains (job))
    {
      if (interruptService)
      {
        this.jobsExecuting.remove (job);
        cancelDepartureEvent (job);
      }
      else
        return false;
    }
    this.jobQueue.remove (job);
    return true;
  }

  /** Checks the presence of the departing job in {@link #jobQueue} and {@link #jobsExecuting},
   * and removes the job from those lists.
   * 
   * {@inheritDoc}
   * 
   * @throws IllegalStateException If the <code>departingJob</code> is not in one of the lists.
   * 
   * @see #jobQueue
   * @see #jobsExecuting
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (! this.jobQueue.contains (departingJob))
      throw new IllegalStateException ();
    if (! this.jobsExecuting.contains (departingJob))
      throw new IllegalStateException ();
    this.jobQueue.remove (departingJob);
    this.jobsExecuting.remove (departingJob);
  }
    
}
