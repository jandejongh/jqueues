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
import java.util.LinkedHashSet;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.collector.Col;
import org.javades.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.processorsharing.PS;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DROP;
import org.javades.jqueues.r5.entity.jq.queue.serverless.SINK;
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

/** Tests for {@link Col}.
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
public class ColTest
{
  
  public ColTest ()
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
  
  public void testColAux
  (final boolean collectDrops,
   final boolean collectAutoRevocations,
   final boolean collectDepartures,
   final SimQueue mainQueue,
   final SimQueue colQueue,
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
    final Col col =
      new Col (mainQueue.getEventList (), mainQueue, colQueue, collectDrops, collectAutoRevocations, collectDepartures, null);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (col, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  /**
   * Test of Col.
   * 
   */
  @Test
  public void testCol () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    //
    // Col(None)[FCFS->DROP] == Tandem[FCFS]
    //
    testColAux
    ( false, false, false,
      new FCFS<> (eventList), new DROP<> (eventList),
      new Tandem<> (eventList, Collections.singleton (new FCFS<> (eventList)), null),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    //
    // Col(Dr)[DROP->FCFS] == Tandem[FCFS]
    //
    testColAux
    ( true, false, false,
      new DROP<> (eventList), new FCFS<> (eventList),
      new Tandem<> (eventList, Collections.singleton (new FCFS<> (eventList)), null),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    //
    // Col(De)[PS->SINK] == Tandem[SINK]
    //
    testColAux
    ( false, false, true,
      new PS<> (eventList), new SINK<> (eventList),
      new Tandem<> (eventList, Collections.singleton (new SINK<> (eventList)), null),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    //
    // Col(Dr,Ar,De)[SINK->self] == Tandem[SINK]
    //
    final SINK sink = new SINK<> (eventList);
    testColAux
    ( true, true, true,
      sink, sink,
      new Tandem<> (eventList, Collections.singleton (new SINK<> (eventList)), null),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    //
    // Col(De)[FCFS->PS] == Tandem[FCFS,PS]
    //
    final FCFS fcfs = new FCFS<> (eventList);
    final PS ps = new PS<> (eventList);
    final Set<SimQueue> subQueues = new LinkedHashSet<>  ();
    subQueues.add (fcfs);
    subQueues.add (ps);
    testColAux
    ( false, false, true,
      new FCFS<> (eventList), new PS<> (eventList),
      new Tandem<> (eventList, subQueues, null),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
  }

}
