package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LJF;
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
public class LJFTest
{
  
  public LJFTest ()
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
   * Test of LJF.
   * 
   */
  @Test
  public void testLJF () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final LJF queue = new LJF (eventList);
    final SimQueuePredictor predictor = new SimQueuePredictor_LJF ();
    final int numberOfJobs = 50;
    final boolean silent = true;
    final boolean deadSilent = true;
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
  }

}
