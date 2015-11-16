package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for preemptive infinite-server queueing disciplines.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractPreemptiveInfiniteServerSimQueue
  <J extends SimJob, Q extends AbstractPreemptiveInfiniteServerSimQueue>
  extends AbstractPreemptiveSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive infinite-server queue given an event list.
   *
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  protected AbstractPreemptiveInfiniteServerSimQueue
  (final SimEventList eventList,
    final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, preemptionStrategy);
  }
  
}