/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.extensions.gate;

import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;

/** A simple representation of a {@link SimJQEvent} specific to {@link SimQueueWithGate}s.
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
