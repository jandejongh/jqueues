package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server preemptive Shortest-Remaining (Service) Time First (SRTF) queueing discipline.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class SRTF<J extends SimJob, Q extends SRTF>
extends AbstractPreemptiveSingleServerSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive SRTF queue given an event list and preemption strategy.
   *
   * @param eventList The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public SRTF (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public SRTF<J, Q> getCopySimQueue ()
  {
    // XXX
    return null;
    // return new SRTF<> (getEventList (), getPreemptionStrategy ());
  }
  
}
