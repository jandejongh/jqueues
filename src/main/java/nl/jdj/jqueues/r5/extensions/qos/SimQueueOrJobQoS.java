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
package nl.jdj.jqueues.r5.extensions.qos;

import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A {@link SimEntity} with explicit QoS support.
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
public interface SimQueueOrJobQoS<J extends SimJob, Q extends SimQueue, P>
extends SimEntity, SimQoS<J, Q>
{

  /** Overridden in order to restrict the return type.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public Class<? extends P> getQoSClass ();

  /** Sets the Java class used for QoS behavior.
   * 
   * @param qosClass The new Java class used for QoS behavior, may be {@code null}.
   * 
   * @throws UnsupportedOperationException Always thrown by the default implementation.
   * 
   */
  default void setQoSClass (final Class<? extends P> qosClass)
  {
    throw new UnsupportedOperationException ();
  }
  
  /** Overridden in order to restrict the return type.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public P getQoS ();

  /** Sets the QoS value.
   * 
   * @param qos The new QoS value, may be {@code null}.
   * 
   * @throws UnsupportedOperationException Always thrown by the default implementation.
   * 
   */
  default void setQoS (final P qos)
  {
    throw new UnsupportedOperationException ();
  }
  
}
