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

/** A factory for delegate {@link SimJob}s, as used in composite queues.
 * 
 * <p>
 * A delegate job visits {@link SimQueue}s on behalf of another job, the "real" job.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
@FunctionalInterface
public interface DelegateSimJobFactory<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this factory.
   * 
   * <p>
   * The default implementation does nothing.
   * Delegate {@code SimJob} factories are automatically reset upon a reset at the (composite) queue to which they are attached.
   * 
   * @param time  The (simulation) time of the reset (the "new" current time).
   * @param queue The queue at which the reset occurs; may be {@code null} for autonomous resets.
   * 
   */
  default void resetFactory (final double time, final Q queue)
  {
    /* EMPTY */
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new delegate {@link SimJob} for given "real" {@link SimJob}.
   * 
   * @param time  The current time.
   * @param job   The "real" job for which a delegate job is to be created.
   * @param queue The queue the "real" job is visiting and for which creation of a delegate job is required.
   * 
   * @return The delegate job.
   * 
   * @throws IllegalArgumentException If (e.g.) time is in the past, or if a <code>null</code> job or queue is passed.
   * 
   */
  public DJ newInstance (double time, J job, Q queue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
