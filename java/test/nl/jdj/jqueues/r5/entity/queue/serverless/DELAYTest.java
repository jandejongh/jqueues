package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
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
    final double[] waitingTimeValues = { 0.0, 3.39, 27.833 };
    for (final double waitingTime : waitingTimeValues)
    {
      final DELAY queue = new DELAY (eventList, waitingTime);
      final SimQueuePredictor predictor = new SimQueuePredictor_DELAY ();
      final int numberOfJobs = 50;
      final boolean silent = true;
      final boolean deadSilent = true;
      DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, null, silent, deadSilent, 1.0e-12, null);
    }
  }

}
