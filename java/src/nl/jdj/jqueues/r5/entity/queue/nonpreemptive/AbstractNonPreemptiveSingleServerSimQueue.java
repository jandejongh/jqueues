package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for non-preemptive single-server queueing disciplines
 *  for {@link SimJob}s.
 *
 * The class supports job revocations.
 * 
 * <p>
 * This class merely wraps the {@link AbstractNonPreemptiveFiniteServerSimQueue} with one server.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see AbstractNonPreemptiveFiniteServerSimQueue
 * 
 */
public abstract class AbstractNonPreemptiveSingleServerSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveSingleServerSimQueue>
  extends AbstractNonPreemptiveFiniteServerSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  /** Creates a non-preemptive single-server queue given an event list.
   *
   * <p>
   * Calls super constructor with unity argument for the number of servers.
   * 
   * @param eventList The event list to use.
   *
   * @see AbstractNonPreemptiveFiniteServerSimQueue#AbstractNonPreemptiveFiniteServerSimQueue
   * 
   */
  protected AbstractNonPreemptiveSingleServerSimQueue (final SimEventList eventList)
  {
    super (eventList, 1);
  }

}
