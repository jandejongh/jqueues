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
package org.javades.jqueues.r5.entity.jq.job.visitslogging;

import java.util.Map;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultVisitsLoggingSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultVisitsLoggingSimJob
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
public class DefaultVisitsLoggingSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultVisitsLoggingSimJob, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a new {@link DefaultVisitsLoggingSimJob} with given parameters.
   * 
   * @return A new {@link DefaultVisitsLoggingSimJob} with given parameters.
   * 
   * @see DefaultVisitsLoggingSimJob#DefaultVisitsLoggingSimJob
   * 
   */
  @Override
  public DefaultVisitsLoggingSimJob newInstance
  (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultVisitsLoggingSimJob (eventList, name, requestedServiceTimeMap);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
