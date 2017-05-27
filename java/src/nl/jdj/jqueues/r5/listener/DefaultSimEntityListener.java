package nl.jdj.jqueues.r5.listener;

import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.SimJQListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;

/** A {@link SimJQListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class DefaultSimEntityListener<J extends SimJob, Q extends SimQueue>
implements SimJQListener<J, Q>
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
  public void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
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
  public void notifyAutoRevocation (final double time, final J job, final Q queue)
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
