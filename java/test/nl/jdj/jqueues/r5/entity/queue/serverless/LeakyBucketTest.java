package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;
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
public class LeakyBucketTest
{
  
  public LeakyBucketTest ()
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
   * Test of LeakyBucket.
   * 
   */
  @Test
  public void testLeakyBucket () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final double[] rateLimitValues = { 0.0, 0.001, 0.1, 0.5, 2.0, 10.0, Double.POSITIVE_INFINITY };
    for (final double rateLimit : rateLimitValues)
    {
      final LeakyBucket queue = new LeakyBucket (eventList, rateLimit);
      final SimQueuePredictor predictor = new SimQueuePredictor_LeakyBucket ();
      final int numberOfJobs = 50;
      final boolean silent = true;
      final boolean deadSilent = true;
      DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null);
    }
  }

}
