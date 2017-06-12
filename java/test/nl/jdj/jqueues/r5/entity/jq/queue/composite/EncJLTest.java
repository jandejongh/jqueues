package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.EncJL;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B_c;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IS;
import nl.jdj.jqueues.r5.entity.jq.queue.processorsharing.PS;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_EncJL;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_IS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_PS;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link EncJL}.
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
public class EncJLTest
{
  
  public EncJLTest ()
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

  public void testEncJLAux
  (final int maxJw,
   final int maxJs,
   final int maxJ,
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
      new EncJL (encQueue.getEventList (), encQueue, null, maxJw, maxJs, maxJ);
    final SimQueuePredictor cQueuePredictor = new SimQueuePredictor_EncJL (encQueuePredictor);
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, cQueuePredictor, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  public void testEncJLAux
  (final int maxJw,
   final int maxJs,
   final int maxJ,
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
      new EncJL (encQueue.getEventList (), encQueue, null, maxJw, maxJs, maxJ);
    encQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    cQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    predictorQueue.setUnknownOperationPolicy (SimEntity.UnknownOperationPolicy.REPORT);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (cQueue, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  @Test
  public void testEncJL () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    final Set<KnownLoadFactory_SQ_SV> restrict = null;
    System.out.println ("============================");
    System.out.println ("EncJLTest");
    System.out.println ("============================");
    final int[] maxJwValues = { 0, 1, 3, 6, Integer.MAX_VALUE };
    final int[] maxJsValues = { 0,  1, 3, 6, Integer.MAX_VALUE };
    final int[] maxJValues = { 0, 1, 2, 3, 4, 5, 6, 7, 10, 30, Integer.MAX_VALUE };
    // EncJL(x,y,infty)[IS] == FCFS_B_c[B=x,c=y]
    for (final int maxJw : maxJwValues)
      for (final int maxJs : maxJsValues)
      {
        testEncJLAux (maxJw, maxJs, Integer.MAX_VALUE,
          new IS (eventList),
          new SimQueuePredictor_IS (),
          numberOfJobs,
          jitterHint,
          silent,
          deadSilent,
          1.0e-12,
          null,
          restrict,
          null);
        testEncJLAux (maxJw, maxJs, Integer.MAX_VALUE,
          new IS (eventList),
          new FCFS_B_c (eventList, maxJw, maxJs),
          numberOfJobs,
          jitterHint,
          silent,
          deadSilent,
          1.0e-12,
          null,
          restrict,
          null);
      }
    // EncJL(x,y,z)[FCFS] == Predictor_EncJL
    for (final int maxJw : maxJwValues)
      for (final int maxJs : maxJsValues)
        for (final int maxJ : maxJValues)
          testEncJLAux (
            maxJw, maxJs, maxJ,
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
    // EncJL(x,y,z)[PS] == Predictor_EncJL
    for (final int maxJw : maxJwValues)
      for (final int maxJs : maxJsValues)
        for (final int maxJ : maxJValues)
        {
          testEncJLAux (
            maxJw, maxJs, maxJ,
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
    // EncJL(inf,1,inf)[PS] == FCFS
    testEncJLAux (
      Integer.MAX_VALUE, 1, Integer.MAX_VALUE,
      new PS (eventList),
      new FCFS (eventList),
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
