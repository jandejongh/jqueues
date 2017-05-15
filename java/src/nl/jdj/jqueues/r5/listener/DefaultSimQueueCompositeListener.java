package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueCompositeListener;

/** A {@link SimQueueCompositeListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimQueueCompositeListener<J extends SimJob, Q extends SimQueue>
extends DefaultSimQueueListener<J, Q>
implements SimQueueCompositeListener<J, Q>
{

  /** Does nothing.
   * 
   */
  @Override
  public void notifyPseudoArrival (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyPseudoDrop (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyPseudoRevocation (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyPseudoAutoRevocation (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyPseudoStart (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyPseudoDeparture (final double time, final J job, final Q queue)
  {
  }

}
