package nl.jdj.jqueues.r5.entity.queue.processorsharing;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_001;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_CUPS;
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
public class CUPSTest
{
  
  public CUPSTest ()
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
   * Test of CUPS.
   * 
   */
  @Test
  public void testCUPS () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final CUPS queue = new CUPS (eventList);
    final SimQueuePredictor<CUPS> predictor = new SimQueuePredictor_CUPS ();
    final int numberOfJobs = 100;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_001.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-6, null);
  }

}
