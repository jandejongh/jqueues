package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

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
  extends AbstractSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  /** Creates a processor-sharing queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  protected AbstractProcessorSharingSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }

}
