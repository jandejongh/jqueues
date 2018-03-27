package nl.jdj.jqueues.r5.entity.jq.queue.processorsharing;

import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;

/** A simple representation of a {@link SimJQEvent} specific to a "catch up" in (e.g.) {@link CUPS}s.
 * 
 * @see CUPS
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
public interface SimQueueCatchUpSimpleEventType
extends SimQueueSimpleEventType
{

  /** A "catch up" in (e.g.) {@link CUPS}.
   * 
   * @see SimQueueCatchUpEvent
   * 
   */
  public static Member CATCH_UP = new Member ("CATCH UP");
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
