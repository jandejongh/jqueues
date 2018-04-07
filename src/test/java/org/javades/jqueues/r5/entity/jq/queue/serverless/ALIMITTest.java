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
package org.javades.jqueues.r5.entity.jq.queue.serverless;

import org.javades.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_ALIMIT;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link ALIMIT}.
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
public class ALIMITTest
{
  
  public ALIMITTest ()
  {
  }
  
  @BeforeClass
  public static void setUpClass ()
  {
  }
  
  @AfterClass
  public static void tearDownClass ()
  {
  }
  
  @Before
  public void setUp ()
  {
  }
  
  @After
  public void tearDown ()
  {
  }

  /**
   * Test of ALIMIT.
   * 
   */
  @Test
  public void testALIMIT () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final boolean silent = true;
    final boolean deadSilent = true;
    // Be careful with the rate-limits, as some workloads schedule arrivals at exactly one-second intervals,
    // which can lead to ambiguity with the expiration of the rate-limitation period.
    final double[] rateLimitValues = { 0.0, 0.0011, 0.11, 0.51, 2.01, 10.01, Double.POSITIVE_INFINITY };
    for (final double rateLimit : rateLimitValues)
    {
      final ALIMIT queue = new ALIMIT (eventList, rateLimit);
      final SimQueuePredictor predictor = new SimQueuePredictor_ALIMIT ();
      DefaultSimQueueTests.doSimQueueTests_SQ_SV
        (queue, predictor, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    }
    // ALIMIT[0.0] == DROP
    final ALIMIT alimit_zero = new ALIMIT<> (eventList, 0);
    final DROP drop = new DROP<> (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (alimit_zero, null, drop, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // ALIMIT[infinity] == ZERO
    final ALIMIT alimit_inf = new ALIMIT<> (eventList, Double.POSITIVE_INFINITY);
    final ZERO zero = new ZERO<> (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (alimit_inf, null, zero, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
