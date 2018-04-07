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
package org.javades.jqueues.r5.util.predictor.workload;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** A representation of a schedule of workload and state-setting events for a single {@link SimQueue}.
 *
 * <p>
 * The {@link SimQueue} to which the workload applies must be fixed upon construction.
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
public interface WorkloadSchedule_SQ
extends WorkloadSchedule
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the queue to which this workload representation applies.
   * 
   * <p>
   * The queue must be fixed upon construction.
   * 
   * @return The queue to which this workload representation applies.
   * 
   * @throws WorkloadScheduleInvalidException If the object is invalid (e.g., due to internal inconsistencies).
   * 
   */
  public default SimQueue getQueue ()
  throws WorkloadScheduleInvalidException
  {
    final Set<? extends SimQueue> queues = getQueues ();
    if (queues.size () != 1 || queues.contains (null))
      throw new WorkloadScheduleInvalidException ();
    return queues.iterator ().next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the queue-access vacation settings in time for the queue.
   * 
   * @return The queue-access vacation settings in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #getQueueAccessVacationMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<Boolean>> getQueueAccessVacationMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getQueueAccessVacationMap (getQueue ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the arrival times indexed by job at the queue.
   * 
   * @return The job arrival times indexed by job and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #getArrivalTimesMap(SimQueue) 
   * @see #getQueue
   * 
   */
  public default Map<SimJob, List<Double>> getArrivalTimesMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getArrivalTimesMap (getQueue ());
  }
  
  /** Gets the job arrivals indexed by time at the queue.
   * 
   * @return The job arrivals in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #getJobArrivalsMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<SimJob>> getJobArrivalsMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getJobArrivalsMap (getQueue ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the revocation times indexed by job at the queue.
   * 
   * @return The job revocation times indexed by job and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #getRevocationTimesMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default Map<SimJob, List<Map<Double, Boolean>>> getRevocationTimesMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getRevocationTimesMap (getQueue ());
  }
  
  /** Gets the job revocations indexed by time at the queue.
   * 
   * @return The job revocations in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #getJobRevocationsMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<Map<SimJob, Boolean>>> getJobRevocationsMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getJobRevocationsMap (getQueue ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the server-access-credits settings in time for the queue.
   * 
   * @return The server-access-credits settings in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #getServerAccessCreditsMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<Integer>> getServerAccessCreditsMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getServerAccessCreditsMap (getQueue ());
  }
  
}
