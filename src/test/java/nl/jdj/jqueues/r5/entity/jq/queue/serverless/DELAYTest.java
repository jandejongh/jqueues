package nl.jdj.jqueues.r5.entity.jq.queue.serverless;

import nl.jdj.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link DELAY}.
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
public class DELAYTest
{
  
  public DELAYTest ()
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
   * Test of DELAY.
   * 
   */
  @Test
  public void testDELAY () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final boolean silent = true;
    final boolean deadSilent = true;
    final double[] waitingTimeValues = { 0.0, 3.39, 27.833, Double.POSITIVE_INFINITY };
    for (final double waitingTime : waitingTimeValues)
    {
      final DELAY queue = new DELAY (eventList, waitingTime);
      final SimQueuePredictor predictor = new SimQueuePredictor_DELAY ();
      DefaultSimQueueTests.doSimQueueTests_SQ_SV
        (queue, predictor, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    }
    // DELAY[0.0] == ZERO
    final DELAY delay0 = new DELAY (eventList, 0);
    final ZERO zero = new ZERO (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (delay0, null, zero, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // DELAY[infinity] == SINK
    final DELAY delay_inf = new DELAY (eventList, Double.POSITIVE_INFINITY);
    final SINK sink = new SINK (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (delay_inf, null, sink, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
