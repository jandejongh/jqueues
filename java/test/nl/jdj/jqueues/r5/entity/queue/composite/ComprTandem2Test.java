package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B_c;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.queue.serverless.DLIMIT;
import nl.jdj.jqueues.r5.entity.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_ComprTandem2;
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

/**
 *
 *
 */
public class ComprTandem2Test
{
  
  public ComprTandem2Test ()
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
  
  public void testComprTandem2Aux
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
    final BlackCompressedTandem2SimQueue ctandem2 =
      new BlackCompressedTandem2SimQueue (waitQueue.getEventList (), waitQueue, serveQueue, null);
    final SimQueuePredictor_ComprTandem2 predictor_ctandem2 =
      new SimQueuePredictor_ComprTandem2 (waitQueuePredictor, serveQueuePredictor);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (ctandem2, predictor_ctandem2, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testComprTandem2Aux
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
    final BlackCompressedTandem2SimQueue ctandem2 =
      new BlackCompressedTandem2SimQueue (waitQueue.getEventList (), waitQueue, serveQueue, null);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (ctandem2, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  /**
   * Test of BlackCompressedTandem2SimQueue.
   * 
   */
  @Test
  public void testComprTandem2 () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    //
    // ComprTandem2[FCFS, FCFS]
    //
    testComprTandem2Aux
    ( new FCFS (eventList), new FCFS (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[LCFS, FCFS]
    //
    testComprTandem2Aux
    ( new LCFS (eventList), new FCFS (eventList),
      new SimQueuePredictor_LCFS (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[DROP, FCFS]
    //
    testComprTandem2Aux
    ( new DROP (eventList), new FCFS (eventList),
      new SimQueuePredictor_DROP (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[FCFS, DROP]
    //
    testComprTandem2Aux
    ( new FCFS (eventList), new DROP (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_DROP (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[SINK, FCFS]
    //
    testComprTandem2Aux
    ( new SINK (eventList), new FCFS (eventList),
      new SimQueuePredictor_SINK (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[FCFS, SINK]
    //
    testComprTandem2Aux
    ( new FCFS (eventList),new SINK (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_SINK (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[ZERO, FCFS]
    //
    testComprTandem2Aux
    ( new ZERO (eventList), new FCFS (eventList),
       new SimQueuePredictor_ZERO (), new SimQueuePredictor_FCFS (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[FCFS, ZERO]
    //
    testComprTandem2Aux
    ( new FCFS (eventList), new ZERO (eventList),
      new SimQueuePredictor_FCFS (), new SimQueuePredictor_ZERO (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[ZERO, ZERO]
    //
    testComprTandem2Aux
    ( new ZERO (eventList), new ZERO (eventList),
      new SimQueuePredictor_ZERO (), new SimQueuePredictor_ZERO (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[DLIMIT[0.5], ZERO]
    //
    testComprTandem2Aux
    (new DLIMIT (eventList, 0.5), new ZERO (eventList),
      new SimQueuePredictor_DLIMIT (), new SimQueuePredictor_ZERO (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[DLIMIT[0.5], DLIMIT[0.1]]
    //
    testComprTandem2Aux
    (new DLIMIT (eventList, 0.5), new DLIMIT (eventList, 0.1),
      new SimQueuePredictor_DLIMIT (), new SimQueuePredictor_DLIMIT (),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[FCFS_B[0],FCFS_2] == NoBuffer_2
    //
    testComprTandem2Aux
    ( new FCFS_B (eventList, 0), new FCFS_c (eventList, 2),
      new NoBuffer_c (eventList, 2),
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    //
    // ComprTandem2[FCFS_B, FCFS_c] == FCFS_B_c
    //
    final int[] bValues = { 0, 1, 2, 100 };
    final int[] cValues = { 0, 1, 2, 10, 100 };
    for (final int B : bValues)
      for (final int c : cValues)
      {
        testComprTandem2Aux
        ( new FCFS_B (eventList, B), new FCFS_c (eventList, c),
          new FCFS_B_c (eventList, B, c),
          numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      }
  }

}
