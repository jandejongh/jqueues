package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for multiple-server processor-sharing queueing disciplines.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractProcessorSharingFiniteServerSimQueue
  <J extends SimJob, Q extends AbstractProcessorSharingFiniteServerSimQueue>
  extends AbstractProcessorSharingSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a multiple-server processor-sharing queue given an event list.
   *
   * @param eventList       The event list to use.
   * @param numberOfServers The number of servers (non-negative).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code> or the number of servers is negative.
   *
   */
  protected AbstractProcessorSharingFiniteServerSimQueue
  (final SimEventList eventList,
    final int numberOfServers)
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

}