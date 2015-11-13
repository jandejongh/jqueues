package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.job.visitslogging.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
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
public class DROPTest
{
  
  public DROPTest ()
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
   * Test of DROP.
   * 
   */
  @Test
  public void testDROP () throws SimQueuePredictionException
  {
    final SimEventList eventList = new SimEventList (SimEvent.class);
    final DROP queue = new DROP (eventList);
    final SimQueuePredictor<DefaultVisitsLoggingSimJob, DROP> predictor = new SimQueuePredictor_DROP<> ();
    final int numberOfJobs = 50;
    final boolean silent = true;
    final boolean deadSilent = true;
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
  }

}
