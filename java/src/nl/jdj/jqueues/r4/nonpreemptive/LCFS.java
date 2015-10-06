package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue; /* Forced for javadoc. */
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link LCFS} queue serves jobs one at a time in reverse order of arrival times.
 *
 * Last Come First Served.
 * 
 * <p>
 * Note that this is the non-preemptive version of the queueing discipline:
 * Once a job is taken into service, it is not preempted in favor of a new arrival.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public final class LCFS<J extends SimJob, Q extends LCFS> extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{

  /** Creates a LCFS queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public LCFS (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Inserts the job at the head of the job queue.
   * 
   * {@inheritDoc}
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected /* final */ void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (0, job);
  }
  
}
