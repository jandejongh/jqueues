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
package org.javades.jqueues.r5.entity.jq.queue;

import java.util.HashMap;
import java.util.Map;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.DefaultVisitsLoggingSimJob;

/** A (alternative) test {@link SimJob}.
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
public class TestJob2<Q extends SimQueue>
extends DefaultVisitsLoggingSimJob<TestJob2, Q>
{

  private final boolean reported;

  public final int n;

  public final double scheduledArrivalTime;
  
  public static Map<SimQueue, Double> createRequestedServiceTimeMap (final int n)
  {
    if (n <= 0)
      throw new IllegalArgumentException ();
    final Map<SimQueue, Double> requestedServiceTimeMap = new HashMap<> ();
    requestedServiceTimeMap.put (null, (double) n);
    return requestedServiceTimeMap;
  }
  
  public TestJob2 (boolean reported, int n)
  {
    super (null, "TestJob[" + n + "]", (Map<Q, Double>) createRequestedServiceTimeMap (n));
    this.reported = reported;
    this.n = n;
    this.scheduledArrivalTime = this.n;
  }

}