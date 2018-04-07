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
package org.javades.jqueues.r5.entity.jq.queue.composite.parallel;

import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for parallel queues.
 * 
 * <p>
 * With parallel queues, a (delegate) job visits no more than one sub-queue and then leaves.
 * 
 * <p>
 * This selector embeds the user-supplied selector for parallel queues, and makes sure that
 * a (delegate) job leaves the queueing system after one (zero) visit.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
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
public class ParallelSimQueuesSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} for parallel queues.
   * 
   * @param userSimQueueSelector The user-supplied {@link SimQueueSelector}, may be {@code null},
   *                             in which case a visiting jobs leaves upon arrival.
   * 
   */
  public ParallelSimQueuesSelector (final SimQueueSelector<J, DQ> userSimQueueSelector)
  {
    this.userSimQueueSelector = userSimQueueSelector;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // USER-SUPPLIED SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final SimQueueSelector<J, DQ> userSimQueueSelector;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public void resetSimQueueSelector ()
  {
    if (this.userSimQueueSelector != null)
      this.userSimQueueSelector.resetSimQueueSelector ();
  }
        
  /** Returns the result from the user selector if present, {@code null} otherwise.
   * 
   * @return The result from the user sub-queue selector if present, {@code null} otherwise.
   * 
   */
  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    return (this.userSimQueueSelector != null ? this.userSimQueueSelector.selectFirstQueue (time, job) : null);
  }
      
  /** Returns {@code null}.
   * 
   * @return {@code null}.
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code>.
   * 
   */
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (previousQueue == null)
      throw new IllegalStateException ();
    return null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
