package nl.jdj.jqueues.r5.entity.queue.preemptive;

import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_SRTF;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
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
public class SRTFTest
{
  
  public SRTFTest ()
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
   * Test of SRTF.
   * 
   */
  @Test
  public void testSRTF () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final boolean silent = true;
    final boolean deadSilent = true;
    for (final PreemptionStrategy preemptionStrategy : PreemptionStrategy.values ())
      if (preemptionStrategy != PreemptionStrategy.REDRAW && preemptionStrategy != PreemptionStrategy.CUSTOM)
      {
        final SRTF queue = new SRTF (eventList, preemptionStrategy);
        final SimQueuePredictor predictor = new SimQueuePredictor_SRTF ();
        DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, null, numberOfJobs, null, silent, deadSilent, 1.0e-9, null);
        eventList.reset ();
      }
  }

}
