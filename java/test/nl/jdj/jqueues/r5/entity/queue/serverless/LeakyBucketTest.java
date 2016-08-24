package nl.jdj.jqueues.r5.entity.queue.serverless;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
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
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    final int[] bValues = { 0, 1, 2, 100 };
    // Be careful with the rate-limits, as somw workloads schedule arrivals at exactly one-second intervals,
    // which can lead to ambiguity with the expiration of the rate-limitation period.
    final double[] rateLimitValues = { 0.0, 0.0011, 0.11, 0.51, 2.01, 10.01, Double.POSITIVE_INFINITY };
    for (final int B : bValues)
      for (final double rateLimit : rateLimitValues)
      {
        final LeakyBucket queue = new LeakyBucket (eventList, B, rateLimit);
        final SimQueuePredictor predictor = new SimQueuePredictor_LeakyBucket ();
        DefaultSimQueueTests.doSimQueueTests_SQ_SV
          (queue, predictor, null, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      }
    for (final double rateLimit : rateLimitValues)
    {
      // LeakyBucket[infinity,rateLimit] == DLIMIT[rateLimit]
      final LeakyBucket queue = new LeakyBucket (eventList, Integer.MAX_VALUE, rateLimit);
      final DLIMIT limit = new DLIMIT<> (eventList, rateLimit);
      DefaultSimQueueTests.doSimQueueTests_SQ_SV
        (queue, null, limit, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    }
    // LeakyBucket[infinity,0.0] == SINK
    final LeakyBucket lb_zero = new LeakyBucket<> (eventList, Integer.MAX_VALUE, 0);
    final SINK sink = new SINK<> (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (lb_zero, null, sink, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    // LeakyBucket[infinity,infinity] == ZERO
    final LeakyBucket lb_inf = new LeakyBucket<> (eventList, Integer.MAX_VALUE, Double.POSITIVE_INFINITY);
    final ZERO zero = new ZERO<> (eventList);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (lb_inf, null, zero, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
