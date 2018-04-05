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
package nl.jdj.jqueues.r5.entity.jq.job.selflistening;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.qos.SimJobQoSFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultSelfListeningSimJobQoS}s.
 *
 * @param <Q> The queue type for jobs.
 * @param <P> The type used for QoS.
 * 
 * @see DefaultSelfListeningSimJobQoS
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
public class DefaultSelfListeningSimJobQoSFactory<Q extends SimQueue, P extends Comparable>
implements SimJobQoSFactory<DefaultSelfListeningSimJobQoS, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a new {@link DefaultSelfListeningSimJobQoS} without QoS support with given parameters.
   * 
   * @return A new {@link DefaultSelfListeningSimJobQoS} without QoS support with given parameters.
   * 
   * @see DefaultSelfListeningSimJobQoS#DefaultSelfListeningSimJobQoS
   * 
   */
  @Override
  public DefaultSelfListeningSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSelfListeningSimJobQoS (eventList, name, requestedServiceTimeMap, null, null);
  }

  /** Returns a new {@link DefaultSelfListeningSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @return A new {@link DefaultSelfListeningSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @see DefaultSelfListeningSimJobQoS#DefaultSelfListeningSimJobQoS
   * 
   */
  @Override
  public DefaultSelfListeningSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap,
    final Class<? extends P> qosClass,
    final P qos)
  {
    return new DefaultSelfListeningSimJobQoS (eventList, name, requestedServiceTimeMap, qosClass, qos);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
