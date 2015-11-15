package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS_B;
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
public class FCFS_BTest
{
  
  public FCFS_BTest ()
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
   * Test of FCFS_B.
   * 
   */
  @Test
  public void testFCFS_B () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (SimEvent.class);
    final boolean silent = true;
    final boolean deadSilent = true;
    final int numberOfJobs = 50;
    final int[] bValues = { 0, 1, 2, 100 };
    for (final int B : bValues)
    {
      final FCFS_B queue = new FCFS_B (eventList, B);
      final SimQueuePredictor predictor = new SimQueuePredictor_FCFS_B (B);
      DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
    }
  }

}
