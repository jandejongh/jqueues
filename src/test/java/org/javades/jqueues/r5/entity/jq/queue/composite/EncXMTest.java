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
import org.javades.jqueues.r5.entity.jq.queue.composite.enc.EncXM;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DROP;
import org.javades.jqueues.r5.entity.jq.queue.serverless.ZERO;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import org.javades.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link EncXM}.
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
public class EncXMTest
{
  
  public EncXMTest ()
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

//  // EncXM has no predictor (yet). XXX
//  public void testEncXMAux
//  (final EncXM.MappableExitMethod drMapping,
//   final EncXM.MappableExitMethod arMapping,
//   final EncXM.MappableExitMethod deMapping,
//   final SimQueue encQueue,
//   final AbstractSimQueuePredictor encQueuePredictor,
//   final int numberOfJobs,
//   final Set<LoadFactoryHint> hints,
//   final boolean silent,
//   final boolean deadSilent,
//   final double accuracy,
//   final Set<KnownLoadFactory_SQ_SV> omit,
//   final Set<KnownLoadFactory_SQ_SV> restrict,
//   final String message)    
//   throws SimQueuePredictionException
//  {
//    final SimQueue cQueue =
//      new EncXM (encQueue.getEventList (), encQueue, drMapping, arMapping, deMapping, null);
//    final SimQueuePredictor cQueuePredictor = new SimQueuePredictor_EncXM (encQueuePredictor);
//    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
//    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
//    DefaultSimQueueTests.doSimQueueTests_SQ_SV
//      (cQueue, cQueuePredictor, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
//  }
  
  public void testEncXMAux
  (final EncXM.MappableExitMethod drMapping,
   final EncXM.MappableExitMethod arMapping,
   final EncXM.MappableExitMethod deMapping,
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
      new EncXM (encQueue.getEventList (), encQueue, drMapping, arMapping, deMapping, null);
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    predictorQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  @Test
  public void testEncXM () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    final Set<KnownLoadFactory_SQ_SV> restrict = null;
    System.out.println ("============================");
    System.out.println ("EncXMTest");
    System.out.println ("============================");
    // EncXM(Dr->De)[DROP] == ZERO
    testEncXMAux (
      EncXM.MappableExitMethod.DEPARTURE, null, null,
      new DROP (eventList),
      new ZERO (eventList),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      restrict,
      null);
    // EncXM(De->Dr)[ZERO] == DROP
    testEncXMAux (
      null, null, EncXM.MappableExitMethod.DROP,
      new ZERO (eventList),
      new DROP (eventList),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      restrict,
      null);
    // EncXM()[FCFS_B[1]] == FCFS_B[1]
    testEncXMAux (
      null, null, null,
      new FCFS_B (eventList, 1),
      new FCFS_B (eventList, 1),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      restrict,
      null);
    // EncXM(AR->De)[EncXM(De->AR)[FCFS_B[1]]] == FCFS_B[1]
    testEncXMAux (
      null, EncXM.MappableExitMethod.DEPARTURE, null,
      new EncXM (eventList, new FCFS_B (eventList, 1), null, null, EncXM.MappableExitMethod.AUTO_REVOCATION, null),
      new FCFS_B (eventList, 1),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      restrict,
      null);
  }

}
