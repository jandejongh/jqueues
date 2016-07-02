package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for single-server processor-sharing queueing disciplines.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractProcessorSharingSingleServerSimQueue
  <J extends SimJob, Q extends AbstractProcessorSharingSingleServerSimQueue>
  extends AbstractProcessorSharingFiniteServerSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server processor-sharing queue given an event list.
   *
   * @param eventList The event list to use.
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  protected AbstractProcessorSharingSingleServerSimQueue (final SimEventList eventList)
  {
    super (eventList, 1);
  }
  
}