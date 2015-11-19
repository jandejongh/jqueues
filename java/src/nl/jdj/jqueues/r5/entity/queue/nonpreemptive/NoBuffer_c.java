package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link NoBuffer_c} queueing system serves jobs with multiple servers but has no buffer space (i.c., no wait queue).
 *
 * <p>
 * Jobs that arrive while all servers are busy are dropped.
 * 
 * <p>
 * Although the queue will work with zero servers, the optimized {@link DROP} queuing system is specially
 * designed for "no-server no-buffer".
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see DROP
 * 
 */
public class NoBuffer_c<J extends SimJob, Q extends NoBuffer_c> extends AbstractNonPreemptiveFiniteServerSimQueue<J, Q>
{

  /** Creates a NoBuffer_c queue given an event list.
   *
   * @param eventList The event list to use.
   * @param c The (non-negative) number of servers.
   *
   * @throws IllegalArgumentException If the number of servers is strictly negative.
   * 
   */
  public NoBuffer_c (final SimEventList eventList, final int c)
  {
    super (eventList, c);
  }
  
  /** Returns a new {@link NoBuffer_c} object on the same {@link SimEventList} with the same number of servers.
   * 
   * @return A new {@link NoBuffer_c} object on the same {@link SimEventList} with the same number of servers.
   * 
   * @see #getEventList
   * @see #getNumberOfServers
   * 
   */
  @Override
  public NoBuffer_c<J, Q> getCopySimQueue ()
  {
    return new NoBuffer_c<> (getEventList (), getNumberOfServers ());
  }
  
  /** Inserts the job at the tail of the job queue if a server is available and there are server-access credits.
   * 
   * @see #getNumberOfJobs
   * @see #getNumberOfServers
   * @see #hasServerAcccessCredits
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (getNumberOfJobs () + 1 <= getNumberOfServers () && hasServerAcccessCredits ())
      this.jobQueue.add (job);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  /** Returns "NoBuffer_c".
   * 
   * @return "NoBuffer_c".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "NoBuffer_" + getNumberOfServers ();
  }

}
