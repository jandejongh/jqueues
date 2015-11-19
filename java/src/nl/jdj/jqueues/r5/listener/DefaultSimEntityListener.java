package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimEntityListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimEntityListener<J extends SimJob, Q extends SimQueue>
implements SimEntityListener<J, Q>
{

  /** Does nothing.
   * 
   */
  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyUpdate (final double time, final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStateChanged (final double time, final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
  }

}
