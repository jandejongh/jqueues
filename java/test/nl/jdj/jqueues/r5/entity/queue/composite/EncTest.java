package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.composite.single.encap.BlackEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.entity.queue.preemptive.PreemptionStrategy;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.CUPS;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.PS;
import nl.jdj.jqueues.r5.entity.queue.serverless.DELAY;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_CUPS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_Enc;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS_B;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_PS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_P_LCFS;
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
public class EncTest
{
  
  public EncTest ()
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
   * Test of BlackEncapsulatorSimQueue.
   * 
   */
  @Test
  public void testEnc () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    // Enc[DELAY]
    final double[] waitingTimeValues = { 0.0, 3.39, 27.833 };
    for (final double waitingTime : waitingTimeValues)
    {
      final DELAY delay = new DELAY (eventList, waitingTime);
      final SimQueuePredictor predictor_delay = new SimQueuePredictor_DELAY ();
      final BlackEncapsulatorSimQueue enc_delay = new BlackEncapsulatorSimQueue (eventList, delay, null);
      final SimQueuePredictor_Enc predictor_enc_delay = new SimQueuePredictor_Enc (predictor_delay);
      DefaultSimQueueTests.doSimQueueTests_SQ_SV
        (enc_delay, predictor_enc_delay, numberOfJobs, null, silent, deadSilent, 1.0e-12, null);
    }
    // Enc[DROP]
    final DROP drop = new DROP (eventList);
    final SimQueuePredictor predictor_drop = new SimQueuePredictor_DROP ();
    final BlackEncapsulatorSimQueue enc_drop = new BlackEncapsulatorSimQueue (eventList, drop, null);
    final SimQueuePredictor_Enc predictor_enc_drop = new SimQueuePredictor_Enc (predictor_drop);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_drop, predictor_enc_drop, numberOfJobs, null, silent, deadSilent, 1.0e-12, null);
    // Enc[SINK]
    final SINK sink = new SINK (eventList);
    final SimQueuePredictor predictor_sink = new SimQueuePredictor_SINK ();
    final BlackEncapsulatorSimQueue enc_sink = new BlackEncapsulatorSimQueue (eventList, sink, null);
    final SimQueuePredictor_Enc predictor_enc_sink = new SimQueuePredictor_Enc (predictor_sink);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_sink, predictor_enc_sink, numberOfJobs, null, silent, deadSilent, 1.0e-12, null);
    // Enc[ZERO]
    final ZERO zero = new ZERO (eventList);
    final SimQueuePredictor predictor_zero = new SimQueuePredictor_ZERO ();
    final BlackEncapsulatorSimQueue enc_zero = new BlackEncapsulatorSimQueue (eventList, zero, null);
    final SimQueuePredictor_Enc predictor_enc_zero = new SimQueuePredictor_Enc (predictor_zero);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_zero, predictor_enc_zero, numberOfJobs, null, silent, deadSilent, 1.0e-12, null);
    // Enc[FCFS]
    final FCFS fcfs = new FCFS (eventList);
    final SimQueuePredictor predictor_fcfs = new SimQueuePredictor_FCFS ();
    final BlackEncapsulatorSimQueue enc_fcfs = new BlackEncapsulatorSimQueue (eventList, fcfs, null);
    final SimQueuePredictor_Enc predictor_enc_fcfs = new SimQueuePredictor_Enc (predictor_fcfs);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_fcfs, predictor_enc_fcfs, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    // Enc[FCFS_B]
    final int[] bValues = { 0, 1, 2, 100 };
    for (final int B : bValues)
    {
      final FCFS_B queue = new FCFS_B (eventList, B);
      final SimQueuePredictor predictor = new SimQueuePredictor_FCFS_B (B);
      final BlackEncapsulatorSimQueue enc_queue = new BlackEncapsulatorSimQueue (eventList, queue, null);
      final SimQueuePredictor_Enc predictor_enc_queue = new SimQueuePredictor_Enc (predictor);
      DefaultSimQueueTests.doSimQueueTests_SQ_SV
        (enc_queue, predictor_enc_queue, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    }
    // Enc[LCFS]
    final LCFS lcfs = new LCFS (eventList);
    final SimQueuePredictor predictor_lcfs = new SimQueuePredictor_LCFS ();
    final BlackEncapsulatorSimQueue enc_lcfs = new BlackEncapsulatorSimQueue (eventList, lcfs, null);
    final SimQueuePredictor_Enc predictor_enc_lcfs = new SimQueuePredictor_Enc (predictor_lcfs);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_lcfs, predictor_enc_lcfs, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    // Enc[P_LCFS]
    final P_LCFS p_lcfs = new P_LCFS (eventList, PreemptionStrategy.RESUME);
    final SimQueuePredictor predictor_p_lcfs = new SimQueuePredictor_P_LCFS ();
    final BlackEncapsulatorSimQueue enc_p_lcfs = new BlackEncapsulatorSimQueue (eventList, p_lcfs, null);
    final SimQueuePredictor_Enc predictor_enc_p_lcfs = new SimQueuePredictor_Enc (predictor_p_lcfs);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_p_lcfs, predictor_enc_p_lcfs, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    // Enc[PS]
    final PS ps = new PS (eventList);
    final SimQueuePredictor predictor_ps = new SimQueuePredictor_PS ();
    final BlackEncapsulatorSimQueue enc_ps = new BlackEncapsulatorSimQueue (eventList, ps, null);
    final SimQueuePredictor_Enc predictor_enc_ps = new SimQueuePredictor_Enc (predictor_ps);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (enc_ps, predictor_enc_ps, numberOfJobs, null, silent, deadSilent, 1.0e-9, null);
    // Enc[CUPS]
    final CUPS cups = new CUPS (eventList);
    final SimQueuePredictor predictor_cups = new SimQueuePredictor_CUPS ();
    final BlackEncapsulatorSimQueue enc_cups = new BlackEncapsulatorSimQueue (eventList, cups, null);
    final SimQueuePredictor_Enc predictor_enc_cups = new SimQueuePredictor_Enc (predictor_cups);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_cups, predictor_enc_cups, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-9, null);
    // Enc[Enc[FCFS]]
    final FCFS fcfs2 = new FCFS (eventList);
    final SimQueuePredictor predictor_fcfs2 = new SimQueuePredictor_FCFS ();
    final BlackEncapsulatorSimQueue enc_fcfs2 = new BlackEncapsulatorSimQueue (eventList, fcfs2, null);
    final BlackEncapsulatorSimQueue enc_enc_fcfs2 = new BlackEncapsulatorSimQueue (eventList, enc_fcfs2, null);
    final SimQueuePredictor_Enc predictor_enc_fcfs2 = new SimQueuePredictor_Enc (predictor_fcfs2);
    final SimQueuePredictor_Enc predictor_enc_enc_fcfs2 = new SimQueuePredictor_Enc (predictor_enc_fcfs2);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_enc_fcfs2, predictor_enc_enc_fcfs2, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
    // Enc[Enc[Enc[P_LCFS]]]
    final P_LCFS p_lcfs2 = new P_LCFS (eventList, null);
    final BlackEncapsulatorSimQueue enc_p_lcfs2 = new BlackEncapsulatorSimQueue (eventList, p_lcfs2, null);
    final BlackEncapsulatorSimQueue enc_enc_p_lcfs2 = new BlackEncapsulatorSimQueue (eventList, enc_p_lcfs2, null);
    final BlackEncapsulatorSimQueue enc_enc_enc_p_lcfs2 = new BlackEncapsulatorSimQueue (eventList, enc_enc_p_lcfs2, null);
    final SimQueuePredictor predictor_p_lcfs2 = new SimQueuePredictor_P_LCFS ();
    final SimQueuePredictor_Enc predictor_enc_p_lcfs2 = new SimQueuePredictor_Enc (predictor_p_lcfs2);
    final SimQueuePredictor_Enc predictor_enc_enc_p_lcfs2 = new SimQueuePredictor_Enc (predictor_enc_p_lcfs2);
    final SimQueuePredictor_Enc predictor_enc_enc_enc_p_lcfs2 = new SimQueuePredictor_Enc (predictor_enc_enc_p_lcfs2);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (enc_enc_enc_p_lcfs2, predictor_enc_enc_enc_p_lcfs2, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
  }

}
