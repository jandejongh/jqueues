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
import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.enc.EncTL;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.IS_CST;
import org.javades.jqueues.r5.entity.jq.queue.processorsharing.PS;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DELAY;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import org.javades.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_EncTL;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_IS_CST;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_PS;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link EncTL}.
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
public class EncTLTest
{
  
  public EncTLTest ()
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

  public void testEncTLAux
  (final double maxWaitingTime,
   final double maxServiceTime,
   final double maxSojournTime,
   final SimQueue encQueue,
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
    final SimQueue cQueue =
      new EncTL (encQueue.getEventList (), encQueue, null, maxWaitingTime, maxServiceTime, maxSojournTime);
    final SimQueuePredictor cQueuePredictor = new SimQueuePredictor_EncTL (encQueuePredictor);
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, cQueuePredictor, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testEncTLAux
  (final double maxWaitingTime,
   final double maxServiceTime,
   final double maxSojournTime, 
   final SimQueue encQueue,
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
    final SimQueue cQueue =
      new EncTL (encQueue.getEventList (), encQueue, null, maxWaitingTime, maxServiceTime, maxSojournTime);
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    predictorQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  @Test
  public void testEncTL () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    final Set<KnownLoadFactory_SQ_SV> restrict = null;
    System.out.println ("============================");
    System.out.println ("EncTLTest");
    System.out.println ("============================");
    final double[] maxWaitingTimeValues = { 0.0, 2.01, 4.01, 30.01, Double.POSITIVE_INFINITY };
    final double[] maxServiceTimeValues = { 0.0, 1.1, 3.76, 5.95, 35.9, Double.POSITIVE_INFINITY };
    final double[] maxSojournTimeValues = { 0.0, 0.63, 1.78, 2.94, 10.39, 20.454, 30.72, 40.945, Double.POSITIVE_INFINITY };
    // EncTL(x,infty,infty)[DELAY(d)] == DELAY(min(x,d))
    final double[] waitingTimeValues    = { 0.0, 3.39, 27.833, Double.POSITIVE_INFINITY };
    for (final double waitingTime : waitingTimeValues)
      for (final double maxWaitingTime : maxWaitingTimeValues)
      {
        testEncTLAux (
          maxWaitingTime, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
          new DELAY (eventList, waitingTime),
          new SimQueuePredictor_DELAY (),
          numberOfJobs,
          null,
          silent,
          deadSilent,
          1.0e-12,
          null,
          restrict,
          null);
        testEncTLAux (
          maxWaitingTime, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
          new DELAY (eventList, waitingTime),
          new DELAY (eventList, Math.min (waitingTime, maxWaitingTime)),
          numberOfJobs,
          null,
          silent,
          deadSilent,
          1.0e-12,
          null,
          restrict,
          null);
      }
    // EncTL(infty,x,infty)[IS_CST(s)] == IS_CST(min(x,s))
    final double[] serviceTimeValues = { 0.0, 0.68, 5.89, 22.55, 89.4, Double.POSITIVE_INFINITY };
    for (final double serviceTime : serviceTimeValues)
      for (final double maxServiceTime : maxServiceTimeValues)
      {
        testEncTLAux (
          Double.POSITIVE_INFINITY, maxServiceTime, Double.POSITIVE_INFINITY,
          new IS_CST (eventList, serviceTime),
          new SimQueuePredictor_IS_CST (serviceTime),
          numberOfJobs,
          null,
          silent,
          deadSilent,
          1.0e-12,
          null,
          restrict,
          null);
        testEncTLAux (
          Double.POSITIVE_INFINITY, maxServiceTime, Double.POSITIVE_INFINITY,
          new IS_CST (eventList, serviceTime),
          new IS_CST (eventList, Math.min (serviceTime, maxServiceTime)),
          numberOfJobs,
          null,
          silent,
          deadSilent,
          1.0e-12,
          null,
          restrict,
          null);
      }
    // EncTL(x,y,z)[FCFS] == Predictor_EncTL
    for (final double maxWaitingTime : maxWaitingTimeValues)
      for (final double maxServiceTime : maxServiceTimeValues)
        for (final double maxSojournTime : maxSojournTimeValues)
          testEncTLAux (
            maxWaitingTime, maxServiceTime, maxSojournTime,
            new FCFS (eventList),
            new SimQueuePredictor_FCFS (),
            numberOfJobs,
            jitterHint,
            silent,
            deadSilent,
            1.0e-12,
            null,
            restrict,
            null);
    // EncTL(x,y,z)[PS] == Predictor_EncTL
    for (final double maxWaitingTime : maxWaitingTimeValues)
      for (final double maxServiceTime : maxServiceTimeValues)
        for (final double maxSojournTime : maxSojournTimeValues)
        {
          testEncTLAux (
            maxWaitingTime, maxServiceTime, maxSojournTime,
            new PS (eventList),
            new SimQueuePredictor_PS (),
            numberOfJobs,
            jitterHint,
            silent,
            deadSilent,
            1.0e-6,
            null,
            restrict,
            null);
        }
  }

}
