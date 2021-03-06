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

import org.javades.jqueues.r5.entity.jq.SimJQListener;
import org.javades.jqueues.r5.entity.jq.job.SimJob;

/** A listener to state changes of one or multiple {@link SimQueue}s.
 *
 * <p>
 * This interface specifies callback methods for the most basic events that can change the state of a {@link SimQueue}.
 * More sophisticated queue types will require an extension to this interface in order to capture all relevant state-changing
 * events.
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
public interface SimQueueListener<J extends SimJob, Q extends SimQueue>
extends SimJQListener<J, Q>
{
 
  /** Notification of the start of a queue-access vacation.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#setQueueAccessVacation
   * 
   */
  public void notifyStartQueueAccessVacation (double time, Q queue);
  
  /** Notification of the end of a queue-access vacation.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#setQueueAccessVacation
   * 
   */
  public void notifyStopQueueAccessVacation (double time, Q queue);

  /** Notification that a {@link SimQueue} has run out of server-access credits.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   * @see SimQueue#setServerAccessCredits
   * 
   */
  public void notifyOutOfServerAccessCredits (double time, Q queue);
  
  /** Notification that a {@link SimQueue} has regained server-access credits.
   * 
   * @param time  The (current) time.
   * @param queue The queue.
   * 
   */
  public void notifyRegainedServerAccessCredits (double time, Q queue);
  
  /** Notification of a change of a {@link SimQueue} <code>startArmed</code> state.
   * 
   * @param time       The (current) time.
   * @param queue      The queue.
   * @param startArmed The new <code>startArmed</code> state.
   * 
   * @see SimQueue#isStartArmed
   * 
   */
  public void notifyNewStartArmed (double time, Q queue, boolean startArmed);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
