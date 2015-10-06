package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue; /* Forced for javadoc. */
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link FCFS} queue serves jobs one at a time in order of arrival times.
 *
 * First Come First Served.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public final class FCFS<J extends SimJob, Q extends FCFS> extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{

  /** Creates a FCFS queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public FCFS (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Inserts the job at the tail of the job queue.
   * 
   * {@inheritDoc}
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected /* final */ void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

}
