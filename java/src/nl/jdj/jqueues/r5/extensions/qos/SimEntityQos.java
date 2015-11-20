package nl.jdj.jqueues.r5.extensions.qos;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimEntity} with explicit QoS support.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public interface SimEntityQos<J extends SimJob, Q extends SimQueue, P extends Comparable>
extends SimEntity<J, Q>, SimQoS<J, Q>
{

  /** Overridden in order to restrict the return type.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public P getQoS ();

  /** Overridden in order to restrict the return type.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public Class<? extends P> getQoSClass ();
  
}
