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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.IC;
import org.javades.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DELAY;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DLIMIT;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DROP;
import org.javades.jqueues.r5.entity.jq.queue.serverless.SINK;
import org.javades.jqueues.r5.entity.jq.queue.serverless.ZERO;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_IC;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_P_LCFS;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_SINK;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_Tandem;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_ZERO;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link Tandem}.
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
public class TandemTest
{
  
  public TandemTest ()
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
   * Test of a Tandem.
   * 
   */
  @Test
  public void testTandem () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    // Tandem[ZERO]
    final ZERO zero = new ZERO (eventList);
    final SimQueuePredictor predictor_zero = new SimQueuePredictor_ZERO ();
    final Tandem tandem_zero = new Tandem (eventList, Collections.singleton (zero), null);
    final SimQueuePredictor_Tandem predictor_tandem_zero =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_zero));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_zero, predictor_tandem_zero, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[DROP]
    final DROP drop = new DROP (eventList);
    final SimQueuePredictor predictor_drop = new SimQueuePredictor_DROP ();
    final Tandem tandem_drop = new Tandem (eventList, Collections.singleton (drop), null);
    final SimQueuePredictor_Tandem predictor_tandem_drop =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_drop));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_drop, predictor_tandem_drop, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[IC]
    final IC ic = new IC (eventList);
    final SimQueuePredictor predictor_ic = new SimQueuePredictor_IC ();
    final Tandem tandem_ic = new Tandem (eventList, Collections.singleton (ic), null);
    final SimQueuePredictor_Tandem predictor_tandem_ic =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_ic));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_ic, predictor_tandem_ic, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[IC, IC]
    final IC ic1 = new IC (eventList);
    final IC ic2 = new IC (eventList);
    final SimQueuePredictor predictor_ic1 = new SimQueuePredictor_IC ();
    final SimQueuePredictor predictor_ic2 = new SimQueuePredictor_IC ();
    final Set<SimQueue> ic1_ic2_set = new LinkedHashSet<> ();
    ic1_ic2_set.add (ic1);
    ic1_ic2_set.add (ic2);
    final Tandem tandem_ic1_ic2 = new Tandem (eventList, ic1_ic2_set, null);
    final List<SimQueuePredictor> predictor_tandem_ic1_ic2_set = new ArrayList ();
    predictor_tandem_ic1_ic2_set.add (predictor_ic1);
    predictor_tandem_ic1_ic2_set.add (predictor_ic2);
    final SimQueuePredictor_Tandem predictor_tandem_ic1_ic2 =
      new SimQueuePredictor_Tandem (predictor_tandem_ic1_ic2_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_ic1_ic2, predictor_tandem_ic1_ic2, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[Tandem[IC, SINK]]
    final IC ic3 = new IC (eventList);
    final SINK sink = new SINK (eventList);
    final SimQueuePredictor predictor_ic3 = new SimQueuePredictor_IC ();
    final SimQueuePredictor predictor_sink = new SimQueuePredictor_SINK ();
    final Set<SimQueue> ic3_sink_set = new LinkedHashSet<> ();
    ic3_sink_set.add (ic3);
    ic3_sink_set.add (sink);
    final Tandem tandem_ic3_sink = new Tandem (eventList, ic3_sink_set, null);
    final List<SimQueuePredictor> predictor_tandem_ic3_sink_set = new ArrayList ();
    predictor_tandem_ic3_sink_set.add (predictor_ic3);
    predictor_tandem_ic3_sink_set.add (predictor_sink);
    final SimQueuePredictor predictor_tandem_ic3_sink =
      new SimQueuePredictor_Tandem (predictor_tandem_ic3_sink_set);
    final Tandem tandem_tandem_ic3_sink = 
      new Tandem (eventList, Collections.singleton (tandem_ic3_sink), null);
    final List<SimQueuePredictor> predictor_tandem_tandem_ic3_sink_set = new ArrayList ();
    predictor_tandem_tandem_ic3_sink_set.add (predictor_tandem_ic3_sink);
    final SimQueuePredictor predictor_tandem_tandem_ic3_sink =
      new SimQueuePredictor_Tandem (predictor_tandem_tandem_ic3_sink_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_tandem_ic3_sink, predictor_tandem_tandem_ic3_sink, null,
       numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[FCFS]
    final FCFS fcfs = new FCFS (eventList);
    final SimQueuePredictor predictor_fcfs = new SimQueuePredictor_FCFS ();
    final Tandem tandem_fcfs = new Tandem (eventList, Collections.singleton (fcfs), null);
    final SimQueuePredictor_Tandem predictor_tandem_fcfs =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_fcfs));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_fcfs, predictor_tandem_fcfs, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[P_LCFS, DROP]
    final P_LCFS p_lcfs = new P_LCFS (eventList, null);
    final SimQueuePredictor predictor_p_lcfs = new SimQueuePredictor_P_LCFS ();
    final DROP drop2 = new DROP (eventList);
    final SimQueuePredictor predictor_drop2 = new SimQueuePredictor_DROP ();
    final Set<SimQueue> p_lcfs_drop2_set = new LinkedHashSet<> ();
    p_lcfs_drop2_set.add (p_lcfs);
    p_lcfs_drop2_set.add (drop2);
    final Tandem tandem_p_lcfs_drop2 = new Tandem (eventList, p_lcfs_drop2_set, null);
    final List<SimQueuePredictor> predictor_tandem_p_lcfs_drop2_set = new ArrayList ();
    predictor_tandem_p_lcfs_drop2_set.add (predictor_p_lcfs);
    predictor_tandem_p_lcfs_drop2_set.add (predictor_drop2);
    final SimQueuePredictor_Tandem predictor_tandem_p_lcfs_drop2 =
      new SimQueuePredictor_Tandem (predictor_tandem_p_lcfs_drop2_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_p_lcfs_drop2, predictor_tandem_p_lcfs_drop2, null,
       numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[DELAY[0.17], DELAY[0.33]]
    final DELAY delay1 = new DELAY (eventList, 0.17);
    final DELAY delay2 = new DELAY (eventList, 0.33);
    final SimQueuePredictor predictor_delay1 = new SimQueuePredictor_DELAY ();
    final SimQueuePredictor predictor_delay2 = new SimQueuePredictor_DELAY ();
    final Set<SimQueue> delay1_delay2_set = new LinkedHashSet<> ();
    delay1_delay2_set.add (delay1);
    delay1_delay2_set.add (delay2);
    final Tandem tandem_delay1_delay2 = new Tandem (eventList, delay1_delay2_set, null);
    final List<SimQueuePredictor> predictor_tandem_delay1_delay2_set = new ArrayList ();
    predictor_tandem_delay1_delay2_set.add (predictor_delay1);
    predictor_tandem_delay1_delay2_set.add (predictor_delay2);
    final SimQueuePredictor_Tandem predictor_tandem_delay1_delay2 =
      new SimQueuePredictor_Tandem (predictor_tandem_delay1_delay2_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_delay1_delay2, predictor_tandem_delay1_delay2, null,
       numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[DLIMIT[0.12]]
    final DLIMIT dlimit1 = new DLIMIT (eventList, 0.12);
    final SimQueuePredictor predictor_dlimit1 = new SimQueuePredictor_DLIMIT ();
    final Set<SimQueue> dlimit1_set = new LinkedHashSet<> ();
    dlimit1_set.add (dlimit1);
    final Tandem tandem_dlimit1 = new Tandem (eventList, dlimit1_set, null);
    final List<SimQueuePredictor> predictor_tandem_dlimit1_set = new ArrayList ();
    predictor_tandem_dlimit1_set.add (predictor_dlimit1);
    final SimQueuePredictor_Tandem predictor_tandem_dlimit1 =
      new SimQueuePredictor_Tandem (predictor_tandem_dlimit1_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_dlimit1, predictor_tandem_dlimit1, null, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[DLIMIT[0.33], DLIMIT[0.1]]
    final DLIMIT dlimit2 = new DLIMIT (eventList, 0.33);
    final SimQueuePredictor predictor_dlimit2 = new SimQueuePredictor_DLIMIT ();
    final DLIMIT dlimit3 = new DLIMIT (eventList, 0.1);
    final SimQueuePredictor predictor_dlimit3 = new SimQueuePredictor_DLIMIT ();
    final Set<SimQueue> dlimit2_dlimit3_set = new LinkedHashSet<> ();
    dlimit2_dlimit3_set.add (dlimit2);
    dlimit2_dlimit3_set.add (dlimit3);
    final Tandem tandem_dlimit2_dlimit3 = new Tandem (eventList, dlimit2_dlimit3_set, null);
    final List<SimQueuePredictor> predictor_tandem_dlimit2_dlimit3_set = new ArrayList ();
    predictor_tandem_dlimit2_dlimit3_set.add (predictor_dlimit2);
    predictor_tandem_dlimit2_dlimit3_set.add (predictor_dlimit3);
    final SimQueuePredictor_Tandem predictor_tandem_dlimit2_dlimit3 =
      new SimQueuePredictor_Tandem (predictor_tandem_dlimit2_dlimit3_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_dlimit2_dlimit3, predictor_tandem_dlimit2_dlimit3, null,
       numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
