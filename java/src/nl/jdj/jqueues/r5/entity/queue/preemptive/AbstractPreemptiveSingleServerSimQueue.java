package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for preemptive single-server queueing disciplines.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractPreemptiveSingleServerSimQueue
  <J extends SimJob, Q extends AbstractPreemptiveSingleServerSimQueue>
  extends AbstractPreemptiveFiniteServerSimQueue<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive single-server queue given an event list.
   *
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * 
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  protected AbstractPreemptiveSingleServerSimQueue
  (final SimEventList eventList,
    final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, preemptionStrategy, 1);
  }
  
}