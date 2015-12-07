package nl.jdj.jqueues.r5.entity.queue.qos;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.preemptive.PreemptionStrategy;
import nl.jdj.jqueues.r5.extensions.qos.SimQueuePredictor_PQ;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
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
public class PQTest
{
  
  public PQTest ()
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
   * Test of PQ (Double).
   * 
   */
  @Test
  public void testPQ_Double () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList ();
    final int numberOfJobs = 100;
    final boolean silent = true;
    final boolean deadSilent = true;
    for (final PreemptionStrategy preemptionStrategy : PreemptionStrategy.values ())
      if (preemptionStrategy != PreemptionStrategy.REDRAW && preemptionStrategy != PreemptionStrategy.CUSTOM)
      {
        final SimQueuePredictor<PQ> predictor = new SimQueuePredictor_PQ ();
        final PQ queue = new PQ (eventList, preemptionStrategy, Double.class, Double.POSITIVE_INFINITY);
        DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
        eventList.reset ();
      }
  }

}
