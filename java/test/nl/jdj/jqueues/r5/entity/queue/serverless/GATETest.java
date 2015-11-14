package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.extensions.gate.SimQueuePredictor_GATE;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGate;
import nl.jdj.jqueues.r5.extensions.gate.StdOutSimQueueWithGateListener;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
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
public class GATETest
{
  
  public GATETest ()
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

  private static final class TestListener<Q extends SimQueueWithGate>
  extends StdOutSimQueueListener<SimJob, Q>
  implements StdOutSimQueueWithGateListener<SimJob, Q>
  {
  }
  
  /**
   * Test of GATE.
   * 
   */
  @Test
  public void testGATE () throws SimQueuePredictionException
  {
    final SimEventList eventList = new SimEventList (SimEvent.class);
    final GATE queue = new GATE (eventList);
    final SimQueuePredictor predictor = new SimQueuePredictor_GATE ();
    final int numberOfJobs = 100;
    final boolean silent = true;
    final boolean deadSilent = true;
    // Override the default StdOutSimQueueListener, and always switch it off in the test.
    if (! silent)
      queue.registerSimEntityListener (new TestListener<>  ());
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, true, deadSilent, 1.0e-12, null);
  }

}
