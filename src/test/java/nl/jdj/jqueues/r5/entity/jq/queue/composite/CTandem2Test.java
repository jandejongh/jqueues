package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.ctandem2.CTandem2;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B_c;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.DLIMIT;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_CTandem2;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_SINK;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_ZERO;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link CTandem2}.
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
public class CTandem2Test
{
  
  public CTandem2Test ()
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
  
  public void testCTandem2Aux
  (final SimQueue waitQueue,
   final SimQueue serveQueue,
   final AbstractSimQueuePredictor waitQueuePredictor,
   final AbstractSimQueuePredictor serveQueuePredictor,
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
    final CTandem2 ctandem2 =
      new CTandem2 (waitQueue.getEventList (), waitQueue, serveQueue, null);
    final SimQueuePredictor_CTandem2 predictor_ctandem2 =
      new SimQueuePredictor_CTandem2 (waitQueuePredictor, serveQueuePredictor);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (ctandem2, predictor_ctandem2, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testCTandem2Aux
  (final SimQueue waitQueue,
   final SimQueue serveQueue,
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
    final CTandem2 ctandem2 =
      new CTandem2 (waitQueue.getEventList (), waitQueue, serveQueue, null);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (ctandem2, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  /**
   * Test of CTandem2.
   * 
   */
  @Test
  public void testCTandem2 () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    //
    // CTandem2[FCFS, FCFS]
    //
    testCTandem2Aux
    ( new FCFS (eventList), new FCFS (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[LCFS, FCFS]
    //
    testCTandem2Aux
    ( new LCFS (eventList), new FCFS (eventList),
      new SimQueuePredictor_LCFS (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[DROP, FCFS]
    //
    testCTandem2Aux
    ( new DROP (eventList), new FCFS (eventList),
      new SimQueuePredictor_DROP (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[FCFS, DROP]
    //
    testCTandem2Aux
    ( new FCFS (eventList), new DROP (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_DROP (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[SINK, FCFS]
    //
    testCTandem2Aux
    ( new SINK (eventList), new FCFS (eventList),
      new SimQueuePredictor_SINK (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[FCFS, SINK]
    //
    testCTandem2Aux
    ( new FCFS (eventList),new SINK (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_SINK (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[ZERO, FCFS]
    //
    testCTandem2Aux
    ( new ZERO (eventList), new FCFS (eventList),
      new SimQueuePredictor_ZERO (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[FCFS, ZERO]
    //
    testCTandem2Aux
    ( new FCFS (eventList), new ZERO (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_ZERO (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[ZERO, ZERO]
    //
    testCTandem2Aux
    ( new ZERO (eventList), new ZERO (eventList),
      new SimQueuePredictor_ZERO (), new SimQueuePredictor_ZERO (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[DLIMIT[0.5], ZERO]
    //
    testCTandem2Aux
    ( new DLIMIT (eventList, 0.5), new ZERO (eventList),
      new SimQueuePredictor_DLIMIT (), new SimQueuePredictor_ZERO (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[DLIMIT[0.5], DLIMIT[0.1]]
    //
    testCTandem2Aux
    ( new DLIMIT (eventList, 0.5), new DLIMIT (eventList, 0.1),
      new SimQueuePredictor_DLIMIT (), new SimQueuePredictor_DLIMIT (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[FCFS_B[0],FCFS_2] == NoBuffer_2
    //
    testCTandem2Aux
    ( new FCFS_B (eventList, 0), new FCFS_c (eventList, 2),
      new NoBuffer_c (eventList, 2),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // CTandem2[FCFS_B, FCFS_c] == FCFS_B_c
    //
    final int[] bValues = { 0, 1, 2, 100 };
    final int[] cValues = { 0, 1, 2, 10, 100 };
    for (final int B : bValues)
      for (final int c : cValues)
        testCTandem2Aux
        ( new FCFS_B (eventList, B), new FCFS_c (eventList, c),
          new FCFS_B_c (eventList, B, c),
          numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
