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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** A representation of a schedule of workload and state-setting events for a single {@link SimQueue} (SQ) with jobs visiting that
 *  queue exactly once (SV).
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
public interface WorkloadSchedule_SQ_SV
extends WorkloadSchedule_SQ
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the arrival time for each job visiting the queue.
   * 
   * @return The arrival time for each job visiting the queue in arrival order and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws WorkloadScheduleInvalidException If the workload schedule is somehow invalid.
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #getArrivalTimesMap_SQ
   * 
   */
  public default Map<SimJob, Double> getArrivalTimesMap_SQ_SV ()
  throws WorkloadScheduleInvalidException
  {
    if (! (isSingleQueue () && isSingleVisit ()))
      throw new WorkloadScheduleInvalidException ();
    final Map<SimJob, Double> arrivalTimesMap_SQ_SV = new HashMap<> ();
    for (final Entry<SimJob, List<Double>> entry : getArrivalTimesMap_SQ ().entrySet ())
      arrivalTimesMap_SQ_SV.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableMap (arrivalTimesMap_SQ_SV);
  }
  
}
