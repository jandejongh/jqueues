package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.composite.single.feedback.BlackNumVisitsFeedbackSimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.serverless.DELAY;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.queue.serverless.DLIMIT;
import nl.jdj.jqueues.r5.entity.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FB_v;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_SINK;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_ZERO;
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
public class FB_vTest
{
  
  public FB_vTest ()
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
  
  public void testFB_vAux
  (final SimQueue encQueue,
   final int numberOfVisits,
   final AbstractSimQueuePredictor encQueuePredictor,
   final int numberOfJobs,
   final Set<LoadFactoryHint> hints,
   final boolean silent,
   final boolean deadSilent,
   final double accuracy,
   final Set<KnownLoadFactory_SQ_SV> omit,
   final Set<KnownLoadFactory_SQ_SV> restrict,
   final String message)    
   throws SimQueuePredictionException
  {
    final BlackNumVisitsFeedbackSimQueue fb_v =
      new BlackNumVisitsFeedbackSimQueue (encQueue.getEventList (), encQueue, numberOfVisits, null);
    final SimQueuePredictor_FB_v predictor_fb_v =
      new SimQueuePredictor_FB_v (encQueuePredictor);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (fb_v, predictor_fb_v, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
  }
  
  /**
   * Test of BlackNumVisitsFeedbackSimQueue.
   * 
   */
  @Test
  public void testFB_v () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 10;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    final int[] numVisitsValues = {1, 10, 100};
    for (final int numVisits : numVisitsValues)
    {
      //
      // FB_numVisits[DROP]
      //
      testFB_vAux
      ( new DROP (eventList),
        numVisits,
        new SimQueuePredictor_DROP (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[ZERO]
      //
      testFB_vAux
      ( new ZERO (eventList),
        numVisits,
        new SimQueuePredictor_ZERO (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[SINK]
      //
      testFB_vAux
      ( new SINK (eventList),
        numVisits,
        new SimQueuePredictor_SINK (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[FCFS]
      //
      testFB_vAux
      ( new FCFS (eventList),
        numVisits,
        new SimQueuePredictor_FCFS (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[DELAY[3.3]]
      //
      testFB_vAux
      ( new DELAY (eventList, 3.3),
        numVisits,
        new SimQueuePredictor_DELAY (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
      //
      // FB_numVisits[DLIMIT[0.1]]
      //
      testFB_vAux
      (new DLIMIT (eventList, 0.1),
        numVisits,
        new SimQueuePredictor_DLIMIT (),
        numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    }
  }

}
