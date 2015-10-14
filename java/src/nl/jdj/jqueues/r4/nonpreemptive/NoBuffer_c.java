package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

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
public class NoBuffer_c<J extends SimJob, Q extends NoBuffer_c> extends AbstractNonPreemptiveMultipleServerSimQueue<J, Q>
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
  
  /** Inserts the job at the tail of the job queue if a server is available and there are server-access credits.
   * 
   * {@inheritDoc}
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
  
  /** Returns "NoBuffer_c".
   * 
   * @return "NoBuffer_c".
   * 
   */
  @Override
  public String toString ()
  {
    return "NoBuffer_" + getNumberOfServers ();
  }

}
