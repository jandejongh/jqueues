package nl.jdj.jqueues.r4.processorsharing;

import nl.jdj.jqueues.r4.DefaultSimQueueTests;
import nl.jdj.jqueues.r4.util.jobfactory.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.queues.SimQueuePredictor_PS;
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
public class ProcessorSharingTest
{
  
  public ProcessorSharingTest ()
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
  public void testPS () throws SimQueuePredictionAmbiguityException
  {
    final SimEventList eventList = new SimEventList (SimEvent.class);
    final PS queue = new PS (eventList);
    final SimQueuePredictor<DefaultVisitsLoggingSimJob, PS> predictor = new SimQueuePredictor_PS<> ();
    final int numberOfJobs = 100;
    final boolean silent = true;
    DefaultSimQueueTests.doTest_01 (queue, predictor, numberOfJobs, silent);
  }

}
