package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_NoBuffer_c;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
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
public class NoBuffer_cTest
{
  
  public NoBuffer_cTest ()
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
   * Test of NoBuffer_c.
   * 
   */
  @Test
  public void testNoBuffer_c () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (SimEvent.class);
    final boolean silent = true;
    final boolean deadSilent = true;
    final int numberOfJobs = 50;
    final int[] cValues = { 0, 1, 2, 10, 100 };
    for (final int c : cValues)
    {
      final NoBuffer_c queue = new NoBuffer_c (eventList, c);
      final SimQueuePredictor predictor = new SimQueuePredictor_NoBuffer_c (c);
      DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
    }
  }

}
