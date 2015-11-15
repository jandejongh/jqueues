package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_PS;
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
public class PSTest
{
  
  public PSTest ()
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
   * Test of PS.
   * 
   */
  @Test
  public void testPS () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final PS queue = new PS (eventList);
    final SimQueuePredictor<PS> predictor = new SimQueuePredictor_PS ();
    final int numberOfJobs = 50;
    final boolean silent = false;
    final boolean deadSilent = true;
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-6, null);
  }

}
