package nl.jdj.jqueues.r5.entity.queue.preemptive;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_P_LCFS;
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
public class P_LCFSTest
{
  
  public P_LCFSTest ()
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
   * Test of P_LCFS.
   * 
   */
  @Test
  public void testP_LCFS () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 100;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    for (final PreemptionStrategy preemptionStrategy : PreemptionStrategy.values ())
      if (preemptionStrategy != PreemptionStrategy.REDRAW && preemptionStrategy != PreemptionStrategy.CUSTOM)
      {
        final P_LCFS queue = new P_LCFS (eventList, preemptionStrategy);
        final SimQueuePredictor predictor = new SimQueuePredictor_P_LCFS ();
        DefaultSimQueueTests.doSimQueueTests_SQ_SV (queue, predictor, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null);
        eventList.reset ();
      }
  }

}
