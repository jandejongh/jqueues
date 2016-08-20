package nl.jdj.jqueues.r5.entity.queue.qos;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_HOL_PS;
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
public class HOL_PSTest
{
  
  public HOL_PSTest ()
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
   * Test of HOL_PS (Double).
   * 
   */
  @Test
  public void testHOL_PS_Double () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList ();
    final HOL_PS<SimJob, HOL_PS, Double> queue = new HOL_PS<> (eventList, Double.class, Double.POSITIVE_INFINITY);
    final SimQueuePredictor<HOL_PS> predictor = new SimQueuePredictor_HOL_PS ();
    final int numberOfJobs = 120;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (queue, predictor, null, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-09, null, null, null);
  }

}
