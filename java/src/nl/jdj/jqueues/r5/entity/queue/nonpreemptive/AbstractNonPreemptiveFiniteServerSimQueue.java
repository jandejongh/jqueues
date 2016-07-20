package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for non-preemptive multiple-server queueing disciplines
 *  for {@link SimJob}s.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractNonPreemptiveFiniteServerSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveFiniteServerSimQueue>
  extends AbstractNonPreemptiveSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive multi-server queue given an event list.
   *
   * @param eventList The event list to use.
   * @param numberOfServers The number of servers (non-negative).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code> or the number of servers is negative.
   *
   */
  protected AbstractNonPreemptiveFiniteServerSimQueue (final SimEventList eventList, final int numberOfServers)
  {
    super (eventList);
    if (numberOfServers < 0)
      throw new IllegalArgumentException ();
    this.numberOfServers= numberOfServers;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF SERVERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The number of servers, non-negative.
   * 
   */
  private final int numberOfServers;
  
  /** Returns the number of servers (non-negative).
   * 
   * @return The number of servers, non-negative.
   * 
   */
  public final int getNumberOfServers ()
  {
    return this.numberOfServers;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER AVAILABLE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are strictly fewer jobs in the service area than servers present in the system.
   * 
   * @return True if there are strictly fewer jobs in the service area than servers present in the system.
   * 
   * @see #getNumberOfJobsInServiceArea
   * @see #getNumberOfServers
   * 
   */
  @Override
  public final boolean hasServerAvailable ()
  {
    return getNumberOfJobsInServiceArea () < getNumberOfServers ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns whether the number of jobs present is strictly smaller than the number of servers.
   * 
   * @return Whether the number of jobs present is strictly smaller than the number of servers.
   * 
   * @see #getNumberOfJobs
   * @see #getNumberOfServers
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getNumberOfJobs () < getNumberOfServers ();
  }
  
}
