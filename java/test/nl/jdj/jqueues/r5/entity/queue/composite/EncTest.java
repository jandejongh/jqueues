package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.composite.single.enc.BlackEncapsulatorHideStartSimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.single.enc.BlackEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS_CST;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.SUR;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.entity.queue.preemptive.PreemptionStrategy;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.CUPS;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.PS;
import nl.jdj.jqueues.r5.entity.queue.serverless.DELAY;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.queue.serverless.GATE;
import nl.jdj.jqueues.r5.entity.queue.serverless.DLIMIT;
import nl.jdj.jqueues.r5.entity.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.queue.serverless.WUR;
import nl.jdj.jqueues.r5.entity.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_CUPS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_Enc;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_EncHS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS_B;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_PS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_P_LCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_SINK;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_SUR;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_WUR;
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

  public void testEncAux
  (final boolean hideStart, 
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
    final SimQueue cQueue;
    final SimQueuePredictor cQueuePredictor;
    if (hideStart)
    {
      cQueue = new BlackEncapsulatorHideStartSimQueue (encQueue.getEventList (), encQueue, null);
      cQueuePredictor = new SimQueuePredictor_EncHS (encQueuePredictor);
    }
    else
    {
      cQueue = new BlackEncapsulatorSimQueue (encQueue.getEventList (), encQueue, null);
      cQueuePredictor = new SimQueuePredictor_Enc (encQueuePredictor);
    }
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, cQueuePredictor, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testEncAux
  (final boolean hideStart, 
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
    final SimQueue cQueue;
    if (hideStart)
      cQueue = new BlackEncapsulatorHideStartSimQueue (encQueue.getEventList (), encQueue, null);
    else
      cQueue = new BlackEncapsulatorSimQueue (encQueue.getEventList (), encQueue, null);
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    predictorQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testEncAux (final boolean hideStart) throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    // Enc[DELAY]
    // EncHS[DELAY]
    // Enc[DELAY] == DELAY
    // EncHS[DELAY] == DELAY
    final double[] waitingTimeValues = { 0.0, 3.39, 27.833 };
    for (final double waitingTime : waitingTimeValues)
    {
      testEncAux (hideStart,
        new DELAY (eventList, waitingTime),
        new SimQueuePredictor_DELAY (),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
      testEncAux (hideStart,
        new DELAY (eventList, waitingTime),
        new DELAY (eventList, waitingTime),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    }
    // Enc[DROP]
    // EncHS[DROP]
    testEncAux (hideStart,
      new DROP (eventList),
      new SimQueuePredictor_DROP (),
      numberOfJobs,
      null,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[SINK]
    // EncHS[SINK]
    testEncAux (hideStart,
      new SINK (eventList),
      new SimQueuePredictor_SINK (),
      numberOfJobs,
      null,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[ZERO]
    // EncHS[ZERO]
    testEncAux (hideStart,
      new ZERO (eventList),
      new SimQueuePredictor_ZERO (),
      numberOfJobs,
      null,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[FCFS]
    // EncHS[FCFS]
    testEncAux (hideStart,
      new FCFS (eventList),
      new SimQueuePredictor_FCFS (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[FCFS_B]
    // EncHS[FCFS_B]
    final int[] bValues = { 0, 1, 2, 100 };
    for (final int B : bValues)
    {
      testEncAux (hideStart,
        new FCFS_B (eventList, B),
        new SimQueuePredictor_FCFS_B (B),
        numberOfJobs,
        jitterHint,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    }
    // Enc[LCFS]
    // EncHS[LCFS]
    testEncAux (hideStart,
      new LCFS (eventList),
      new SimQueuePredictor_LCFS (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[P_LCFS]
    // EncHS[P_LCFS]
    testEncAux (hideStart,
      new P_LCFS (eventList, PreemptionStrategy.RESUME),
      new SimQueuePredictor_P_LCFS (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[PS]
    // EncHS[PS]
    testEncAux (hideStart,
      new PS (eventList),
      new SimQueuePredictor_PS (),
      numberOfJobs,
      null,
      silent,
      deadSilent,
      1.0e-9,
      null,
      null,
      null);
    // Enc[CUPS]
    // EncHS[CUPS]
    testEncAux (hideStart,
      new CUPS (eventList),
      new SimQueuePredictor_CUPS (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-9,
      null,
      null,
      null);
    // Enc[Enc[FCFS]]
    // EncHS[Enc[FCFS]]
    testEncAux (hideStart,
      new BlackEncapsulatorSimQueue (eventList, new FCFS (eventList), null),
      new SimQueuePredictor_Enc (new SimQueuePredictor_FCFS ()),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    if (hideStart)
      // EncHS[Enc[FCFS]] == Enc[EncHS[FCFS]]
      testEncAux (hideStart,
        new BlackEncapsulatorSimQueue (eventList, new FCFS (eventList), null),
        new BlackEncapsulatorSimQueue (eventList,
          new BlackEncapsulatorHideStartSimQueue (eventList,
            new FCFS (eventList), null), null),
        numberOfJobs,
        jitterHint,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    else
      // Enc[EncHS[FCFS]] == EncHS[Enc[FCFS]]
      testEncAux (hideStart,
        new BlackEncapsulatorHideStartSimQueue (eventList, new FCFS (eventList), null),
        new BlackEncapsulatorHideStartSimQueue (eventList,
          new BlackEncapsulatorSimQueue (eventList,
            new FCFS (eventList), null), null),
        numberOfJobs,
        jitterHint,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    // Enc[Enc[Enc[P_LCFS]]]
    // EncHS[Enc[Enc[P_LCFS]]]
    testEncAux (hideStart,
      new BlackEncapsulatorSimQueue (eventList,
        new BlackEncapsulatorSimQueue (eventList,
          new P_LCFS (eventList, null), null), null),
      new SimQueuePredictor_Enc (new SimQueuePredictor_Enc (new SimQueuePredictor_P_LCFS ())),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[DLIMIT[0.1]]
    // EncHS[DLIMIT[0.1]]
    testEncAux (hideStart,
      new DLIMIT (eventList, 0.1),
      new SimQueuePredictor_DLIMIT (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-9,
      null,
      null,
      null);
    // Enc[WUR]
    // EncHS[WUR]
    testEncAux (hideStart,
      new WUR (eventList),
      new SimQueuePredictor_WUR (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    // Enc[SUR]
    // EncHS[SUR]
    testEncAux (hideStart,
      new SUR (eventList),
      new SimQueuePredictor_SUR (),
      numberOfJobs,
      jitterHint,
      silent,
      deadSilent,
      1.0e-12,
      null,
      null,
      null);
    if (hideStart)
      // EncHS[SUR] == WUR
      testEncAux (hideStart,
        new SUR (eventList),
        new WUR (eventList),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    else
      // Enc[SUR] == SUR
      testEncAux (hideStart,
        new SUR (eventList),
        new SUR (eventList),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    if (hideStart)
      // EncHS[IS_CST[0.0]] == ZERO
      testEncAux (hideStart,
        new IS_CST (eventList, 0.0),
        new ZERO (eventList),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    else
      // EncHS[IS_CST[0.0]] == IS_CST[0.0]
      testEncAux (hideStart,
        new IS_CST (eventList, 0.0),
        new IS_CST (eventList, 0.0),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    for (final double waitingTime : waitingTimeValues)
      if (hideStart)
        // EncHS[IS_CST[x]] == DELAY[x]
        testEncAux (hideStart,
          new IS_CST (eventList, waitingTime),
          new DELAY (eventList, waitingTime),
          numberOfJobs,
          null,
          silent,
          deadSilent,
          1.0e-12,
          null,
          null,
          null);
      else
        // Enc[IS_CST[x]] == IS_CST[x]
        testEncAux (hideStart,
          new IS_CST (eventList, waitingTime),
          new IS_CST (eventList, waitingTime),
          numberOfJobs,
          null,
          silent,
          deadSilent,
          1.0e-12,
          null,
          null,
          null);
    if (hideStart)
      // EncHS[GATE] == GATE
      testEncAux (hideStart,
        new GATE (eventList),
        new GATE (eventList),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
    else
      // Enc[GATE] == GATE
      testEncAux (hideStart,
        new GATE (eventList),
        new GATE (eventList),
        numberOfJobs,
        null,
        silent,
        deadSilent,
        1.0e-12,
        null,
        null,
        null);
  }

  /**
   * Test of BlackEncapsulatorSimQueue.
   * 
   */
  @Test
  public void testEnc () throws SimQueuePredictionException
  {
    testEncAux (false);
  }
  
  /**
   * Test of BlackEncapsulatorHideStartSimQueue.
   * 
   */
  @Test
  public void testEncHS () throws SimQueuePredictionException
  {
    testEncAux (true);
  }
  
}
