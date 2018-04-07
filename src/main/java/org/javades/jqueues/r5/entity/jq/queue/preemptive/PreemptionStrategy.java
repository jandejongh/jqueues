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
package org.javades.jqueues.r5.entity.jq.queue.preemptive;

import org.javades.jqueues.r5.entity.jq.job.SimJob;

/** A list of possible strategies at {@link SimJob} preemption.
 *
 * @see AbstractPreemptiveSimQueue
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
public enum PreemptionStrategy
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PREEMPTION STRATEGY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Drops the preempted job.
   * 
   */
  DROP,
  /** Puts the preempted job on hold; future service resumption continues at the point where the previous service was interrupted.
   * 
   */
  RESUME,
  /** Puts the preempted job on hold; future service resumption requires the job to be served from scratch.
   * 
   */
  RESTART,
  /** Puts the preempted job on hold; future service resumption requires the job to be served from scratch
   *  with a new required service time.
   * 
   */
  REDRAW,
  /** Departs the preempted job, even though may not have finished its service requirements.
   * 
   */
  DEPART,
  /** Takes a different approach at job preemption than mentioned in this list.
   * 
   */
  CUSTOM

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
