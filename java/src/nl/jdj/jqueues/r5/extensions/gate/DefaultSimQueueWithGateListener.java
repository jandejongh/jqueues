package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimQueueWithGateListener} logging events on <code>System.out</code>.
 *
 * <p>
 * This "class" is implemented as an interface with only default methods, allowing multiple inheritance of implementation.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface DefaultSimQueueWithGateListener<J extends SimJob, Q extends SimQueueWithGate>
extends SimQueueWithGateListener<J, Q>
{

  /** Does nothing.
   * 
   */
  @Override
  public default void notifyNewGateStatus (final double time, final Q queue, final boolean open)
  {
  }
  
}
