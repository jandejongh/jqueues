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
package org.javades.jqueues.r5.extensions.qos;

import java.util.Map;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** A {@link SimQueue} with explicit QoS support.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
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
public interface SimQueueQoS<J extends SimJob, Q extends SimQueueQoS, P>
extends SimQueueOrJobQoS<J, Q, P>, SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Overridden in order to restrict the return type.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public SimQueueQoS<J, Q, P> getCopySimQueue () throws UnsupportedOperationException;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the mapping QoS values onto jobs currently visiting this queue with that particular QoS value.
   *
   * <p>
   * Each {@link SimJob} present returning (valid) non-{@code null} QoS value from {@link SimJob#getQoS}
   * must be in present in only that value set,
   * and the union of these value sets must exactly match {@link #getJobs}.
   * Jobs returning {@code null} from {@link SimJob#getQoS} must be put in a value set corresponding to
   * {@link #getDefaultJobQoS}.
   * 
   * <p>
   * Note that many concrete subclasses will impose an ordering on the keys and/or on the value sets.
   * 
   * @return The mapping QoS values onto jobs currently visiting this queue with that particular QoS value.
   * 
   * @see #getJobs
   * @see SimJob#getQoS
   * @see #getDefaultJobQoS
   * 
   */
  public Map<P, Set<J>> getJobsQoSMap ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT JOB QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the default QoS value used by this queue for jobs that have {@code null} QoS value.
   * 
   * @return The default QoS value used by this queue for jobs that have {@code null} QoS value.
   * 
   * @see SimJob#getQoS
   * 
   */
  public P getDefaultJobQoS ();
  
}
