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
package org.javades.jqueues.r5.util.predictor;

import java.util.List;
import java.util.Map;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** A prediction of the behavior of a single {@link SimQueue}
 *  under a (presumed) workload in which each {@link SimJob} visits the queue at most once.
 *
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
public interface SimQueuePrediction_SQ_SV<Q extends SimQueue>
{
  
  /** Returns the {@link SimQueue} for which this prediction was generated.
   * 
   * @return The {@link SimQueue} for which this prediction was generated.
   * 
   */
  Q getQueue ();
  
  /** Gets the predicted of job-visits (at most one per job).
   * 
   * @return A map from every job predicted to visit the queue onto its {@link JobQueueVisitLog} .
   * 
   */
  Map<SimJob, JobQueueVisitLog<SimJob, Q>> getVisitLogs ();

  /** Returns the predicted queue-access vacation (changes).
   * 
   * @return A list with singleton maps holding the time of change in the QAV state (key) and the new state (value).
   *         The list must be ordered non-decreasing in event time.
   * 
   */
  List<Map<Double, Boolean>> getQueueAccessVacationLog ();

  /** Returns the predicted server-access-credits availability.
   * 
   * @return A list with singleton maps holding the time of change in the SAC availability state (key) and the new state (value).
   *         The list must be ordered non-decreasing in event time.
   * 
   */
  List<Map<Double, Boolean>> getServerAccessCreditsAvailabilityLog ();
  
  /** Returns the predicted {@code StartArmed} (changes).
   * 
   * @return A list with singleton maps holding the time of change in the STA state (key) and the new state (value).
   *         The list must be ordered non-decreasing in event time.
   * 
   */
  List<Map<Double, Boolean>> getStartArmedLog ();

}
