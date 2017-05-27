package nl.jdj.jqueues.r5.extensions.gate;

import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;

/** *  A simple representation of a {@link SimJQEvent} specific to {@link SimQueueWithGate}s.
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
public interface SimQueueWithGateSimpleEventType
extends SimQueueSimpleEventType
{

  /** The control of the gate of a queue.
   * 
   * @see SimQueueGateEvent
   * 
   */
  public static Member GATE = new Member ("GATE");
  
  /** Notification that the gate is open.
   * 
   */
  public static Member GATE_OPEN = new Member ("GATE_OPEN");
  
  /** Notification that the gate is closed.
   * 
   */
  public static Member GATE_CLOSED = new Member ("GATE_CLOSED");
  
}
