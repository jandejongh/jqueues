package nl.jdj.jqueues.r5.listener;

import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;

/** A {@link SimEntityListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
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
public class DefaultSimEntityListener
implements SimEntityListener
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

}
