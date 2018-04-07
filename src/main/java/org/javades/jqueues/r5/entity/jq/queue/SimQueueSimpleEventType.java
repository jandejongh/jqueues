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
package org.javades.jqueues.r5.entity.jq.queue;

import org.javades.jqueues.r5.entity.SimEntityEvent;
import org.javades.jqueues.r5.entity.jq.SimJQSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.QueueAccessVacation;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.ServerAccessCredits;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.StartArmed;

/** A simple representation of a {@link SimEntityEvent} specific to {@link SimQueue}s.
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
public interface SimQueueSimpleEventType
extends SimJQSimpleEventType
{

  /** The start or end of a queue-access vacation.
   * 
   * @see QueueAccessVacation
   * 
   */
  public static Member QUEUE_ACCESS_VACATION = new Member ("QUEUE_ACCESS_VACATION");
  
  /** The start of a queue-access vacation.
   * 
   * @see QueueAccessVacation
   * 
   */
  public static Member QAV_START = new Member ("QAV_START");
  
  /** The end of a queue-access vacation.
   * 
   * @see QueueAccessVacation
   * 
   */
  public static Member QAV_END = new Member ("QAV_END");
  
  /** The setting of the remaining server-access credits.
   * 
   * @see ServerAccessCredits
   * 
   */
  public static Member SERVER_ACCESS_CREDITS = new Member ("SERVER_ACCESS_CREDITS");
  
  /** The loss of remaining server-access credits.
   * 
   * @see ServerAccessCredits
   * 
   */
  public static Member OUT_OF_SAC = new Member ("OUT_OF_SAC");
  
  /** The regain of remaining server-access credits.
   * 
   * @see ServerAccessCredits
   * 
   */
  public static Member REGAINED_SAC = new Member ("REGAIN_SAC");
  
  /** The loss of {@code startArmed}.
   * 
   * @see StartArmed
   * 
   */
  public static Member STA_FALSE = new Member ("STA_FALSE");
  
  /** The gain of {@code startArmed}.
   * 
   * @see StartArmed
   * 
   */
  public static Member STA_TRUE = new Member ("STA_TRUE");
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
