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
package org.javades.jqueues.r5.entity.jq.queue.composite;

import java.util.Collections;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.enc.EncHS;
import org.javades.jqueues.r5.entity.jq.queue.composite.feedback.FB_v;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy;
import org.javades.jqueues.r5.entity.jq.queue.qos.PQ;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DELAY;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DLIMIT;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DROP;
import org.javades.jqueues.r5.entity.jq.queue.serverless.SINK;
import org.javades.jqueues.r5.entity.jq.queue.serverless.ZERO;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import org.javades.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_FB_v;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_SINK;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_ZERO;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link FB_v}.
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
public class FB_vTest
{
  
  public FB_vTest ()
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
  
  public void testFB_vAux
  (final SimQueue encQueue,
   final int numberOfVisits,
   final AbstractSimQueuePredictor encQueuePredictor,
   final int numberOfJobs,
   final Set<LoadFactoryHint> hints,
   final boolean silent,
   final boolean deadSilent,
   final double accuracy,
   final Set<KnownLoadFactory_SQ_SV> omit,
   final Set<KnownLoadFactory_SQ_SV> restrict,
   final String message)    
   throws SimQueuePredictionException
  {
    final FB_v fb_v =
      new FB_v (encQueue.getEventList (), encQueue, numberOfVisits, null);
    final SimQueuePredictor_FB_v predictor_fb_v =
      new SimQueuePredictor_FB_v (encQueuePredictor);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (fb_v, predictor_fb_v, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testFB_vAux
  (final SimQueue encQueue,
   final int numberOfVisits,
   final SimQueue predictorQueue,
   final int numberOfJobs,
   final Set<LoadFactoryHint> hints,
   final boolean silent,
   final boolean deadSilent,
   final double accuracy,
   final Set<KnownLoadFactory_SQ_SV> omit,
   final Set<KnownLoadFactory_SQ_SV> restrict,
   final String message)    
   throws SimQueuePredictionException
  {
    final FB_v fb_v =
      new FB_v (encQueue.getEventList (), encQueue, numberOfVisits, null);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (fb_v, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  /**
   * Test of FB_v.
   * 
   */
  @Test
  public void testFB_v () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 10;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    final int[] numVisitsValues = {1, 10, 100};
    for (final int numVisits : numVisitsValues)
    {
      //
      // FB_numVisits[DROP]
      //
      testFB_vAux
      ( new DROP (eventList),
        numVisits,
        new SimQueuePredictor_DROP (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[ZERO]
      //
      testFB_vAux
      ( new ZERO (eventList),
        numVisits,
        new SimQueuePredictor_ZERO (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[SINK]
      //
      testFB_vAux
      ( new SINK (eventList),
        numVisits,
        new SimQueuePredictor_SINK (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[FCFS]
      //
      testFB_vAux
      ( new FCFS (eventList),
        numVisits,
        new SimQueuePredictor_FCFS (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[DELAY[3.3]]
      //
      testFB_vAux
      ( new DELAY (eventList, 3.3),
        numVisits,
        new SimQueuePredictor_DELAY (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[DLIMIT[0.1]]
      //
      testFB_vAux
      ( new DLIMIT (eventList, 0.1),
        numVisits,
        new SimQueuePredictor_DLIMIT (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // EncHS[FB_1[PQ]] == EncHS[PQ]
      //
      if (numVisits ==1)
        for (final PreemptionStrategy preemptionStrategy : PreemptionStrategy.values ())
          if (preemptionStrategy != PreemptionStrategy.REDRAW && preemptionStrategy != PreemptionStrategy.CUSTOM)
          {
            final SimQueue queueToTest =
              new EncHS (eventList,
                         new FB_v (eventList,
                                   new PQ (eventList, preemptionStrategy, Double.class, Double.POSITIVE_INFINITY),
                                   numVisits,
                                   null),
                         null);
            final SimQueue testQueue =
              new EncHS (eventList,
                         new PQ (eventList, preemptionStrategy, Double.class, Double.POSITIVE_INFINITY),
                         null);
            DefaultSimQueueTests.doSimQueueTests_SQ_SV (
              queueToTest,
              null,
              testQueue,
              numberOfJobs,
              jitterHint,
              silent, deadSilent,
              1.0e-12,
              null, null,
              null);
          }
    }
  }

}
