package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for preemptive multiple-server queueing disciplines.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractPreemptiveFiniteServerSimQueue
  <J extends SimJob, Q extends AbstractPreemptiveFiniteServerSimQueue>
  extends AbstractPreemptiveSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive multi-server queue given an event list.
   *
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * @param numberOfServers The number of servers (non-negative).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code> or the number of servers is negative.
   *
   */
  protected AbstractPreemptiveFiniteServerSimQueue
  (final SimEventList eventList,
    final PreemptionStrategy preemptionStrategy,
    final int numberOfServers)
  {
    super (eventList, preemptionStrategy);
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

}