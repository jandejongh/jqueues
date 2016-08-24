package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.queue.composite.tandem.BlackTandemSimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.entity.queue.serverless.DELAY;
import nl.jdj.jqueues.r5.entity.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.queue.serverless.LIMIT;
import nl.jdj.jqueues.r5.entity.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DELAY;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DROP;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_IC;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LIMIT;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_P_LCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_SINK;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_Tandem;
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
public class TandemTest
{
  
  public TandemTest ()
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
   * Test of BlackTandemSimQueue.
   * 
   */
  @Test
  public void testTandem () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 50;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    // Tandem[ZERO]
    final ZERO zero = new ZERO (eventList);
    final SimQueuePredictor predictor_zero = new SimQueuePredictor_ZERO ();
    final BlackTandemSimQueue tandem_zero = new BlackTandemSimQueue (eventList, Collections.singleton (zero), null);
    final SimQueuePredictor_Tandem predictor_tandem_zero =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_zero));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_zero, predictor_tandem_zero, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[DROP]
    final DROP drop = new DROP (eventList);
    final SimQueuePredictor predictor_drop = new SimQueuePredictor_DROP ();
    final BlackTandemSimQueue tandem_drop = new BlackTandemSimQueue (eventList, Collections.singleton (drop), null);
    final SimQueuePredictor_Tandem predictor_tandem_drop =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_drop));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_drop, predictor_tandem_drop, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[IC]
    final IC ic = new IC (eventList);
    final SimQueuePredictor predictor_ic = new SimQueuePredictor_IC ();
    final BlackTandemSimQueue tandem_ic = new BlackTandemSimQueue (eventList, Collections.singleton (ic), null);
    final SimQueuePredictor_Tandem predictor_tandem_ic =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_ic));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_ic, predictor_tandem_ic, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[IC, IC]
    final IC ic1 = new IC (eventList);
    final IC ic2 = new IC (eventList);
    final SimQueuePredictor predictor_ic1 = new SimQueuePredictor_IC ();
    final SimQueuePredictor predictor_ic2 = new SimQueuePredictor_IC ();
    final Set<SimQueue> ic1_ic2_set = new LinkedHashSet<> ();
    ic1_ic2_set.add (ic1);
    ic1_ic2_set.add (ic2);
    final BlackTandemSimQueue tandem_ic1_ic2 = new BlackTandemSimQueue (eventList, ic1_ic2_set, null);
    final List<SimQueuePredictor> predictor_tandem_ic1_ic2_set = new ArrayList ();
    predictor_tandem_ic1_ic2_set.add (predictor_ic1);
    predictor_tandem_ic1_ic2_set.add (predictor_ic2);
    final SimQueuePredictor_Tandem predictor_tandem_ic1_ic2 =
      new SimQueuePredictor_Tandem (predictor_tandem_ic1_ic2_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_ic1_ic2, predictor_tandem_ic1_ic2, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[Tandem[IC, SINK]]
    final IC ic3 = new IC (eventList);
    final SINK sink = new SINK (eventList);
    final SimQueuePredictor predictor_ic3 = new SimQueuePredictor_IC ();
    final SimQueuePredictor predictor_sink = new SimQueuePredictor_SINK ();
    final Set<SimQueue> ic3_sink_set = new LinkedHashSet<> ();
    ic3_sink_set.add (ic3);
    ic3_sink_set.add (sink);
    final BlackTandemSimQueue tandem_ic3_sink = new BlackTandemSimQueue (eventList, ic3_sink_set, null);
    final List<SimQueuePredictor> predictor_tandem_ic3_sink_set = new ArrayList ();
    predictor_tandem_ic3_sink_set.add (predictor_ic3);
    predictor_tandem_ic3_sink_set.add (predictor_sink);
    final SimQueuePredictor predictor_tandem_ic3_sink =
      new SimQueuePredictor_Tandem (predictor_tandem_ic3_sink_set);
    final BlackTandemSimQueue tandem_tandem_ic3_sink = 
      new BlackTandemSimQueue (eventList, Collections.singleton (tandem_ic3_sink), null);
    final List<SimQueuePredictor> predictor_tandem_tandem_ic3_sink_set = new ArrayList ();
    predictor_tandem_tandem_ic3_sink_set.add (predictor_tandem_ic3_sink);
    final SimQueuePredictor predictor_tandem_tandem_ic3_sink =
      new SimQueuePredictor_Tandem (predictor_tandem_tandem_ic3_sink_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_tandem_ic3_sink, predictor_tandem_tandem_ic3_sink, null,
       numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[FCFS]
    final FCFS fcfs = new FCFS (eventList);
    final SimQueuePredictor predictor_fcfs = new SimQueuePredictor_FCFS ();
    final BlackTandemSimQueue tandem_fcfs = new BlackTandemSimQueue (eventList, Collections.singleton (fcfs), null);
    final SimQueuePredictor_Tandem predictor_tandem_fcfs =
      new SimQueuePredictor_Tandem (Collections.singletonList (predictor_fcfs));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_fcfs, predictor_tandem_fcfs, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[P_LCFS, DROP]
    final P_LCFS p_lcfs = new P_LCFS (eventList, null);
    final SimQueuePredictor predictor_p_lcfs = new SimQueuePredictor_P_LCFS ();
    final DROP drop2 = new DROP (eventList);
    final SimQueuePredictor predictor_drop2 = new SimQueuePredictor_DROP ();
    final Set<SimQueue> p_lcfs_drop2_set = new LinkedHashSet<> ();
    p_lcfs_drop2_set.add (p_lcfs);
    p_lcfs_drop2_set.add (drop2);
    final BlackTandemSimQueue tandem_p_lcfs_drop2 = new BlackTandemSimQueue (eventList, p_lcfs_drop2_set, null);
    final List<SimQueuePredictor> predictor_tandem_p_lcfs_drop2_set = new ArrayList ();
    predictor_tandem_p_lcfs_drop2_set.add (predictor_p_lcfs);
    predictor_tandem_p_lcfs_drop2_set.add (predictor_drop2);
    final SimQueuePredictor_Tandem predictor_tandem_p_lcfs_drop2 =
      new SimQueuePredictor_Tandem (predictor_tandem_p_lcfs_drop2_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_p_lcfs_drop2, predictor_tandem_p_lcfs_drop2, null,
       numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[DELAY[0.17], DELAY[0.33]]
    final DELAY delay1 = new DELAY (eventList, 0.17);
    final DELAY delay2 = new DELAY (eventList, 0.33);
    final SimQueuePredictor predictor_delay1 = new SimQueuePredictor_DELAY ();
    final SimQueuePredictor predictor_delay2 = new SimQueuePredictor_DELAY ();
    final Set<SimQueue> delay1_delay2_set = new LinkedHashSet<> ();
    delay1_delay2_set.add (delay1);
    delay1_delay2_set.add (delay2);
    final BlackTandemSimQueue tandem_delay1_delay2 = new BlackTandemSimQueue (eventList, delay1_delay2_set, null);
    final List<SimQueuePredictor> predictor_tandem_delay1_delay2_set = new ArrayList ();
    predictor_tandem_delay1_delay2_set.add (predictor_delay1);
    predictor_tandem_delay1_delay2_set.add (predictor_delay2);
    final SimQueuePredictor_Tandem predictor_tandem_delay1_delay2 =
      new SimQueuePredictor_Tandem (predictor_tandem_delay1_delay2_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_delay1_delay2, predictor_tandem_delay1_delay2, null,
       numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[LIMIT[0.12]]
    final LIMIT limit1 = new LIMIT (eventList, 0.12);
    final SimQueuePredictor predictor_limit1 = new SimQueuePredictor_LIMIT ();
    final Set<SimQueue> limit1_set = new LinkedHashSet<> ();
    limit1_set.add (limit1);
    final BlackTandemSimQueue tandem_limit1 = new BlackTandemSimQueue (eventList, limit1_set, null);
    final List<SimQueuePredictor> predictor_tandem_limit1_set = new ArrayList ();
    predictor_tandem_limit1_set.add (predictor_limit1);
    final SimQueuePredictor_Tandem predictor_tandem_limit1 =
      new SimQueuePredictor_Tandem (predictor_tandem_limit1_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_limit1, predictor_tandem_limit1, null, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    // Tandem[LIMIT[0.33], LIMIT[0.1]]
    final LIMIT limit2 = new LIMIT (eventList, 0.33);
    final SimQueuePredictor predictor_limit2 = new SimQueuePredictor_LIMIT ();
    final LIMIT limit3 = new LIMIT (eventList, 0.1);
    final SimQueuePredictor predictor_limit3 = new SimQueuePredictor_LIMIT ();
    final Set<SimQueue> limit2_limit3_set = new LinkedHashSet<> ();
    limit2_limit3_set.add (limit2);
    limit2_limit3_set.add (limit3);
    final BlackTandemSimQueue tandem_limit2_limit3 = new BlackTandemSimQueue (eventList, limit2_limit3_set, null);
    final List<SimQueuePredictor> predictor_tandem_limit2_limit3_set = new ArrayList ();
    predictor_tandem_limit2_limit3_set.add (predictor_limit2);
    predictor_tandem_limit2_limit3_set.add (predictor_limit3);
    final SimQueuePredictor_Tandem predictor_tandem_limit2_limit3 =
      new SimQueuePredictor_Tandem (predictor_tandem_limit2_limit3_set);
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (tandem_limit2_limit3, predictor_tandem_limit2_limit3, null,
       numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
