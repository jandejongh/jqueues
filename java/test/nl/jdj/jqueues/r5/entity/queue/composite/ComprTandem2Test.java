package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
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
   final Set<KnownLoadFactory_SQ_SV> omit)
   throws SimQueuePredictionException
  {
    final BlackCompressedTandem2SimQueue ctandem2 =
      new BlackCompressedTandem2SimQueue (waitQueue.getEventList (), waitQueue, serveQueue, null);
    final SimQueuePredictor_ComprTandem2 predictor_ctandem2 =
      new SimQueuePredictor_ComprTandem2 (waitQueuePredictor, serveQueuePredictor);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (ctandem2, predictor_ctandem2, numberOfJobs, hints, silent, deadSilent, accuracy, omit);
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
    final FCFS fcfs1 = new FCFS (eventList);
    fcfs1.setName ("fcfs1");
    final FCFS fcfs2 = new FCFS (eventList);
    fcfs2.setName ("fcfs2");
    final AbstractSimQueuePredictor predictor_fcfs1 = new SimQueuePredictor_FCFS ();
    final AbstractSimQueuePredictor predictor_fcfs2 = new SimQueuePredictor_FCFS ();
    testComprTandem2Aux
    ( fcfs1, fcfs2,
      predictor_fcfs1, predictor_fcfs2,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[LCFS, FCFS]
    //
    final LCFS lcfs = new LCFS (eventList);
    lcfs.setName ("lcfs");
    final FCFS fcfs3 = new FCFS (eventList);
    fcfs3.setName ("fcfs3");
    final AbstractSimQueuePredictor predictor_lcfs = new SimQueuePredictor_LCFS ();
    final AbstractSimQueuePredictor predictor_fcfs3 = new SimQueuePredictor_FCFS ();
    testComprTandem2Aux
    ( lcfs, fcfs3,
      predictor_lcfs, predictor_fcfs3,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[DROP, FCFS]
    //
    final DROP drop1 = new DROP (eventList);
    drop1.setName ("drop1");
    final FCFS fcfs4 = new FCFS (eventList);
    fcfs4.setName ("fcfs4");
    final AbstractSimQueuePredictor predictor_drop1 = new SimQueuePredictor_DROP ();
    final AbstractSimQueuePredictor predictor_fcfs4 = new SimQueuePredictor_FCFS ();
    testComprTandem2Aux
    ( drop1, fcfs4,
      predictor_drop1, predictor_fcfs4,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[FCFS, DROP]
    //
    final FCFS fcfs5 = new FCFS (eventList);
    fcfs5.setName ("fcfs5");
    final DROP drop2 = new DROP (eventList);
    drop2.setName ("drop2");
    final AbstractSimQueuePredictor predictor_fcfs5 = new SimQueuePredictor_FCFS ();
    final AbstractSimQueuePredictor predictor_drop2 = new SimQueuePredictor_DROP ();
    testComprTandem2Aux
    ( fcfs5, drop2,
      predictor_fcfs5, predictor_drop2,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[SINK, FCFS]
    //
    final SINK sink1 = new SINK (eventList);
    sink1.setName ("sink1");
    final FCFS fcfs6 = new FCFS (eventList);
    fcfs6.setName ("fcfs6");
    final AbstractSimQueuePredictor predictor_sink1 = new SimQueuePredictor_SINK ();
    final AbstractSimQueuePredictor predictor_fcfs6 = new SimQueuePredictor_FCFS ();
    testComprTandem2Aux
    ( sink1, fcfs6,
      predictor_sink1, predictor_fcfs6,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[FCFS, SINK]
    //
    final FCFS fcfs7 = new FCFS (eventList);
    fcfs7.setName ("fcfs7");
    final SINK sink2 = new SINK (eventList);
    sink2.setName ("sink2");
    final AbstractSimQueuePredictor predictor_fcfs7 = new SimQueuePredictor_FCFS ();
    final AbstractSimQueuePredictor predictor_sink2 = new SimQueuePredictor_SINK ();
    testComprTandem2Aux
    ( fcfs7, sink2,
      predictor_fcfs7, predictor_sink2,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[ZERO, FCFS]
    //
    final ZERO zero1 = new ZERO (eventList);
    zero1.setName ("zero1");
    final FCFS fcfs8 = new FCFS (eventList);
    fcfs8.setName ("fcfs8");
    final AbstractSimQueuePredictor predictor_zero1 = new SimQueuePredictor_ZERO ();
    final AbstractSimQueuePredictor predictor_fcfs8 = new SimQueuePredictor_FCFS ();
    testComprTandem2Aux
    ( zero1, fcfs8,
      predictor_zero1, predictor_fcfs8,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[FCFS, ZERO]
    //
    final FCFS fcfs9 = new FCFS (eventList);
    fcfs9.setName ("fcfs9");
    final ZERO zero2 = new ZERO (eventList);
    zero2.setName ("zero2");
    final AbstractSimQueuePredictor predictor_fcfs9 = new SimQueuePredictor_FCFS ();
    final AbstractSimQueuePredictor predictor_zero2 = new SimQueuePredictor_ZERO ();
    testComprTandem2Aux
    ( fcfs9, zero2,
      predictor_fcfs9, predictor_zero2,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    //
    // ComprTandem2[ZERO, ZERO]
    //
    final ZERO zero3 = new ZERO (eventList);
    zero3.setName ("zero3");
    final ZERO zero4 = new ZERO (eventList);
    zero4.setName ("zero4");
    final AbstractSimQueuePredictor predictor_zero3 = new SimQueuePredictor_ZERO ();
    final AbstractSimQueuePredictor predictor_zero4 = new SimQueuePredictor_ZERO ();
    testComprTandem2Aux
    ( zero3, zero4,
      predictor_zero3, predictor_zero4,
      numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
  }

}
