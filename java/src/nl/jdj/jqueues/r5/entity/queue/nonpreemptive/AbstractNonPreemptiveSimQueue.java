package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.queue.serverless.SINK;
import nl.jdj.jsimulation.r5.SimEventList;

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
 * @see SINK
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
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time, true);
  }

  /** Invokes {@link #rescheduleAfterRevokation}.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    rescheduleAfterRevokation (job, time);
  }

  /** If possible, removes the job from the internal data structures, and cancels a pending departure event.
   * 
   * If the job is already in service, and the <code>interruptService</code> argument is set to <code>false</code>,
   * this method returns <code>false</code>, by contract of {@link SimQueue}.
   * Otherwise, if the job is in service, its departure event is canceled through {@link #cancelDepartureEvent},
   * and the job is removed from {@link #jobsInServiceArea} and {@link #jobQueue}.
   * Subsequently, whether the job was in service or not, it is removed from {@link #jobQueue},
   * and <code>true</code> is returned.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (! this.jobQueue.contains (job))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (job))
    {
      if (interruptService)
      {
        this.jobsInServiceArea.remove (job);
        cancelDepartureEvent (job);
      }
      else
        return false;
    }
    this.jobQueue.remove (job);
    return true;
  }

  /** Checks the presence of the departing job in {@link #jobQueue} and {@link #jobsInServiceArea},
   * and removes the job from those lists.
   * 
   * @throws IllegalStateException If the <code>departingJob</code> is not in one of the lists.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
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
  }
    
}
