package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractClassicSimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for (generalized) processor-sharing queueing disciplines
 *  for {@link SimJob}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * 
 */
public abstract class AbstractProcessorSharingSimQueue
  <J extends SimJob, Q extends AbstractProcessorSharingSimQueue>
  extends AbstractClassicSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a (generalized) processor-sharing queue given an event list, buffer size and number of servers.
   *
   * @param eventList       The event list to use.
   * @param bufferSize      The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param numberOfServers The number of servers (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   *
   */
  protected AbstractProcessorSharingSimQueue (final SimEventList eventList, final int bufferSize, final int numberOfServers)
  {
    super (eventList, bufferSize, numberOfServers);
  }

}
