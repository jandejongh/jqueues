package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.SINK;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS_c;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link FCFS_c}.
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
public class FCFS_cTest
{
  
  public FCFS_cTest ()
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
   * Test of FCFS_c.
   * 
   */
  @Test
  public void testFCFS_c () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final boolean silent = true;
    final boolean deadSilent = true;
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final int[] cValues = { 0, 1, 2, 10, 100 };
    for (final int c : cValues)
    {
      final FCFS_c queue = new FCFS_c (eventList, c);
      final SimQueuePredictor predictor = new SimQueuePredictor_FCFS_c (c);
      DefaultSimQueueTests.doSimQueueTests_SQ_SV
       (queue, predictor, null, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    }
    // FCFS_0 == SINK
    final FCFS_c fcfs_0 = new FCFS_c (eventList, 0);
    final SINK sink = new SINK (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
     (fcfs_0, null, sink, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
