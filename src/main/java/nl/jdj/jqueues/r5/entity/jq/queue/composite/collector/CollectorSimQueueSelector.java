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
package nl.jdj.jqueues.r5.entity.jq.queue.composite.collector;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for collector queues.
 *
 * <p>
 * A drop-collector is a composite queue with two queues,
 * a main one and one selectively collecting all jobs exiting from the main queue.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 *
 * @see AbstractCollectorSimQueue
 * @see Col
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
public class CollectorSimQueueSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} for a collector queue.
   * 
   * @param mainQueue      The main queue.
   * @param collectorQueue The collector queue.
   * 
   * @throws IllegalArgumentException If one of or both queues are <code>null</code>.
   * 
   */
  public CollectorSimQueueSelector (final DQ mainQueue, final DQ collectorQueue)
  {
    if (mainQueue == null || collectorQueue == null)
      throw new IllegalArgumentException ();
    this.mainQueue = mainQueue;
    this.collectorQueue = collectorQueue;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN AND COLLECTOR QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final DQ mainQueue;
  
  private final DQ collectorQueue;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public void resetSimQueueSelector ()
  {
  }

  @Override
  public DQ selectFirstQueue (final double time, final J job)
  {
    return this.mainQueue;
  }

  @Override
  public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (previousQueue == null || (previousQueue != this.mainQueue && previousQueue != this.collectorQueue))
      throw new IllegalStateException ();
    return null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
