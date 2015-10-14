package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** An abstract base class for non-preemptive single-server queueing disciplines
 *  for {@link SimJob}s.
 *
 * The class supports job revocations.
 * 
 * <p>
 * This class merely wraps the {@link AbstractNonPreemptiveMultipleServerSimQueue} with one server.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see AbstractNonPreemptiveMultipleServerSimQueue
 * 
 */
public abstract class AbstractNonPreemptiveSingleServerSimQueue
  <J extends SimJob, Q extends AbstractNonPreemptiveSingleServerSimQueue>
  extends AbstractNonPreemptiveMultipleServerSimQueue<J, Q>
  implements SimQueue<J, Q>
{

  /** Creates a non-preemptive single-server queue given an event list.
   *
   * <p>
   * Calls super constructor with unity argument for the number of servers.
   * 
   * @param eventList The event list to use.
   *
   * @see AbstractNonPreemptiveMultipleServerSimQueue#AbstractNonPreemptiveMultipleServerSimQueue
   * 
   */
  protected AbstractNonPreemptiveSingleServerSimQueue (final SimEventList eventList)
  {
    super (eventList, 1);
  }

}
