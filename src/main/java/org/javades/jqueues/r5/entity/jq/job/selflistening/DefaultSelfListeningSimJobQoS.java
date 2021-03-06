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
package org.javades.jqueues.r5.entity.jq.job.selflistening;

import java.util.Map;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.extensions.qos.SimJobQoS;
import org.javades.jsimulation.r5.SimEventList;

/** A {@link DefaultSelfListeningSimJob} with explicit QoS support.
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
public class DefaultSelfListeningSimJobQoS<J extends DefaultSelfListeningSimJobQoS, Q extends SimQueue, P extends Comparable>
extends DefaultSelfListeningSimJob<J, Q>
implements SimJobQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Creates a new {@link DefaultSelfListeningSimJobQoS} with given event list, name,
   *  requested service-time map, and QoS structure.
   * 
   * @param eventList               The event list to use, may be {@code null}.
   * @param name                    The name of the job, may be <code>null</code>.
   * @param requestedServiceTimeMap See {@link DefaultSelfListeningSimJob#DefaultSelfListeningSimJob}.
   * @param qosClass                The QoS class, may be {@code null}.
   * @param qos                     The QoS value, may be {@code null}.
   *                                The QoS value must be {@code null} or an instance of the QoS class.
   *                                The QoS value must be {@code null} if the QoS class is {@code null}.
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If the QoS class is {@code null} and the QoS value is <i>not</i>,
   *                                  or if the QoS value is not an instance of the QoS class.
   * 
   * @see Class#isInstance
   * 
   */
  public DefaultSelfListeningSimJobQoS
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap,
    final Class<P> qosClass,
    final P qos)
  {
    super (eventList, name, requestedServiceTimeMap);
    if (qosClass == null && qos != null)
      throw new IllegalArgumentException ();
    if (qosClass != null && qos != null && ! qosClass.isInstance (qos))
      throw new IllegalArgumentException ();
    this.qosClass = qosClass;
    this.qos = qos;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private Class<? extends P> qosClass = null;

  @Override
  public final Class<? extends P> getQoSClass ()
  {
    return this.qosClass;
  }

  @Override
  public final void setQoSClass (final Class qosClass)
  {
    this.qosClass = qosClass;
    this.qos = null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private P qos = null;
  
  @Override
  public final P getQoS ()
  {
    return this.qos;
  }

  @Override
  public final void setQoS (final P qos)
  {
    if (this.qosClass == null && qos != null)
      throw new IllegalArgumentException ();
    if (qos != null && ! this.qosClass.isInstance (qos))
      throw new IllegalArgumentException ();
    this.qos = qos;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
