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
package nl.jdj.jqueues.r5.entity.jq.queue.qos;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueOrJobQoS;
import nl.jdj.jqueues.r5.extensions.qos.SimJobQoS;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;

/** Utility methods related to QoS.
 * 
 * @see SimQueueOrJobQoS
 * @see SimJobQoS
 * @see SimQueueQoS
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
public final class SimQueueQoSUtils
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Prevents instantiation.
   * 
   */
  private SimQueueQoSUtils ()
  {  
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the (validated) QoS value for given job at given queue (where the job does not have to be present on the queue yet).
   * 
   * <p>
   * The QoS value is validated in the sense that if the {@link SimJob} returns a non-{@code null}
   * {@link SimJob#getQoSClass}, the class or interface returned must be a sub-class or sub-interface
   * of the queue's {@link SimQueueQoS#getQoSClass}, in other words,
   * the job's QoS structure must be compatible with that of the queue.
   * In addition, if the job returns a non-{@code null} {@link SimJob#getQoSClass},
   * it must return a non-{@code null} QoS value from {@link SimJob#getQoS},
   * and this QoS value must be an instance of the reported job QoS class.
   * In all other case, including the case in which the job is {@code null},
   * an {@link IllegalArgumentException} is thrown.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueueQoS}s supported.
   * @param <P> The type used for QoS.
   * 
   * @param job   The job, non-{@code null}.
   * @param queue The queue, non-{@code null}.
   * 
   * @return The validated QoS value of the job, taking the default (only) if the job reports {@code null} QoS class and value.
   * 
   * @throws IllegalArgumentException If the job or queue is {@code null} or if one or more QoS-related sanity checks fail.
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueueQoS, P>
  P getAndCheckJobQoS (final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    if (job.getQoSClass () == null)
    {
      if (job.getQoS () != null)
        throw new IllegalArgumentException ();
      else
        return (P) queue.getDefaultJobQoS ();
    }
    else
    {
      if (! queue.getQoSClass ().isAssignableFrom (job.getQoSClass ()))
        throw new IllegalArgumentException ();
      if (job.getQoS () == null)
        return (P) queue.getDefaultJobQoS ();
      if (! queue.getQoSClass ().isInstance (job.getQoS ()))
        throw new IllegalArgumentException ();
      return (P) job.getQoS ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
