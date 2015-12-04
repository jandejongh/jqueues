package nl.jdj.jqueues.r5.entity.queue.qos;

import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
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
    throw new UnsupportedOperationException ();
// Below: taken from HOL; needs loop over preemption stragegies as in P_LCFS.
//    final SimEventList eventList = new DefaultSimEventList ();
//    final HOL<SimJob, HOL, Double> queue = new HOL<> (eventList, Double.class, Double.POSITIVE_INFINITY);
//    final SimQueuePredictor<HOL> predictor = new SimQueuePredictor_HOL ();
//    final int numberOfJobs = 50;
//    final boolean silent = true;
//    final boolean deadSilent = true;
//    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, silent, deadSilent, 1.0e-12, null);
  }

}
