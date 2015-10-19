package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.serverless.SINK;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link FCFS_c} queueing system serves jobs in order of arrival times with multiple servers.
 *
 * First Come First Served with c servers and infinite buffer size.
 *
 * <p>
 * Although the queue will work with zero servers, the optimized {@link SINK} queuing system is specially
 * designed for "no server" systems.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see FCFS
 * @see FCFS_B
 * @see SINK
 * 
 */
public class FCFS_c<J extends SimJob, Q extends FCFS_c> extends AbstractNonPreemptiveMultipleServerSimQueue<J, Q>
{

  /** Creates a FCFS_c queue given an event list.
   *
   * @param eventList The event list to use.
   * @param c The (non-negative) number of servers.
   *
   * @throws IllegalArgumentException If the number of servers is strictly negative.
   * 
   */
  public FCFS_c (final SimEventList eventList, final int c)
  {
    super (eventList, c);
  }
  
  /** Returns a new {@link FCFS_c} object on the same {@link SimEventList} with the same number of servers.
   * 
   * @return A new {@link FCFS_c} object on the same {@link SimEventList} with the same number of servers.
   * 
   * @see #getEventList
   * @see #getNumberOfServers
   * 
   */
  @Override
  public FCFS_c<J, Q> getCopySimQueue ()
  {
    return new FCFS_c<> (getEventList (), getNumberOfServers ());
  }
  
  /** Inserts the job at the tail of the job queue.
   * 
   * {@inheritDoc}
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }  
  
  /** Returns "FCFS_c".
   * 
   * @return "FCFS_c".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "FCFS_" + getNumberOfServers ();
  }

}
