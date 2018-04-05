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
package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** An object capable of selecting the first and next {@link SimQueue}s to visit for a (delegate) job.
 *
 * <p>
 * This class is used by {@link SimQueueComposite} and derivatives.
 * Note that the selection methods only expect <i>real</i> jobs as input argument,
 * i.e., <i>not delegate</i> jobs.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for (delegate) jobs.
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
public interface SimQueueSelector<J extends SimJob, DQ extends SimQueue>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE SELECTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  // XXX Need some cloning mechanism!
  
  /** Resets this selector.
   * 
   */
  public void resetSimQueueSelector ();
  
  /** Returns the first queue to visit for an arriving job.
   * 
   * @param time The time of arrival of the job.
   * @param job  The job, non-<code>null</code>.
   * 
   * @return The first queue to visit, if <code>null</code>, the job is to depart immediately.
   * 
   */
  public DQ selectFirstQueue (double time, J job);
  
  /** Returns the next queue to visit for a job.
   * 
   * @param time          The current time, i.e., the departure time of the job at its previous queue.
   * @param job           The job, non-<code>null</code>.
   * @param previousQueue The previous queue the job visited, and just departed from.
   * 
   * @return The next queue to visit, if <code>null</code>, the job is to depart immediately.
   * 
   */
  public DQ selectNextQueue (double time, J job, DQ previousQueue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
