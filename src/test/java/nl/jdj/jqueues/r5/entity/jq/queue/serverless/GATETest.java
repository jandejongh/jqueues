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
package nl.jdj.jqueues.r5.entity.jq.queue.serverless;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.extensions.gate.SimQueuePredictor_GATE;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGate;
import nl.jdj.jqueues.r5.extensions.gate.StdOutSimQueueWithGateListener;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link GATE}.
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
public class GATETest
{
  
  public GATETest ()
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

  private static final class TestListener<Q extends SimQueueWithGate>
  extends StdOutSimQueueListener<SimJob, Q>
  implements StdOutSimQueueWithGateListener<SimJob, Q>
  {
  }
  
  /**
   * Test of GATE.
   * 
   */
  @Test
  public void testGATE () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final GATE queue = new GATE (eventList);
    final SimQueuePredictor predictor = new SimQueuePredictor_GATE ();
    final int numberOfJobs = 100;
    final boolean silent = true;
    final boolean deadSilent = true;
    // Override the default StdOutSimQueueListener, and always switch it off in the test.
    if (! silent)
      queue.registerSimEntityListener (new TestListener<>  ());
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (queue, predictor, null, numberOfJobs, null, true, deadSilent, 1.0e-12, null, null, null);
  }

}
