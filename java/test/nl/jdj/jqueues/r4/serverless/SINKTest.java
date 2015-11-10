package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.DefaultSimQueueTests;
import nl.jdj.jqueues.r4.util.jobfactory.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.queues.SimQueuePredictor_SINK;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class SINKTest
{
  
  public SINKTest ()
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
   * Test of SINK.
   * 
   */
  @Test
  public void testSINK () throws SimQueuePredictionException
  {
    final SimEventList eventList = new SimEventList (SimEvent.class);
    final SINK queue = new SINK (eventList);
    final SimQueuePredictor<DefaultVisitsLoggingSimJob, SINK> predictor = new SimQueuePredictor_SINK<> ();
    final int numberOfJobs = 100;
    final boolean silent = false;
    final boolean deadSilent = false;
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
  }

}
