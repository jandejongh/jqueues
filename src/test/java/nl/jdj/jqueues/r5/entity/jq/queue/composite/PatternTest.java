package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.DefaultSimQueueTests;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.Pattern;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.SUR;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.WUR;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.LoadFactory_SQ_SV_0010;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LCFS;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_Pattern;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_ZERO;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link Pattern}.
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 *
 */
public class PatternTest
{
  
  public PatternTest ()
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
   * Test of Pattern.
   * 
   */
  @Test
  public void testPattern () throws SimQueuePredictionException
  {
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    final int numberOfJobs = 100;
    final Set<LoadFactoryHint> jitterHint = Collections.singleton (LoadFactory_SQ_SV_0010.SERVICE_TIME_JITTER);
    final boolean silent = true;
    final boolean deadSilent = true;
    // Pattern[ZERO,ZERO,ZERO]
    final ZERO zero0 = new ZERO (eventList);
    final ZERO zero1 = new ZERO (eventList);
    final ZERO zero2 = new ZERO (eventList);
    final AbstractSimQueuePredictor predictor_zero = new SimQueuePredictor_ZERO ();
    final Set<SimQueue> subQueues_pattern0 = new LinkedHashSet<> ();
    subQueues_pattern0.add (zero0);
    subQueues_pattern0.add (zero1);
    subQueues_pattern0.add (zero2);
    final Pattern pattern0 =
      new Pattern (eventList, subQueues_pattern0, null, new int[] { 0, 1, 2, -1, 2, 1, 0 });
    final SimQueuePredictor_Pattern predictor_pattern0 =
      new SimQueuePredictor_Pattern
            (Arrays.asList (new AbstractSimQueuePredictor[] {predictor_zero, predictor_zero, predictor_zero}));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (pattern0, predictor_pattern0, null, numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Pattern[FCFS,LCFS]
    final FCFS fcfs0 = new FCFS (eventList);
    final LCFS lcfs0 = new LCFS (eventList);
    final AbstractSimQueuePredictor predictor_fcfs0 = new SimQueuePredictor_FCFS ();
    final AbstractSimQueuePredictor predictor_lcfs0 = new SimQueuePredictor_LCFS ();
    final Set<SimQueue> subQueues_pattern1 = new LinkedHashSet<> ();
    subQueues_pattern1.add (fcfs0);
    subQueues_pattern1.add (lcfs0);
    final Pattern pattern1 =
      new Pattern (eventList, subQueues_pattern1, null, new int[] { 0, 1 });
    final SimQueuePredictor_Pattern predictor_pattern1 =
      new SimQueuePredictor_Pattern
            (Arrays.asList (new AbstractSimQueuePredictor[] {predictor_fcfs0, predictor_lcfs0}));
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (pattern1, predictor_pattern1, null, numberOfJobs, jitterHint, silent, deadSilent, 1.0e-12, null, null, null);
    // Pattern[IS,IS,IS] == IS
    final IS is0 = new IS (eventList);
    final IS is1 = new IS (eventList);
    final IS is2 = new IS (eventList);
    final Set<SimQueue> subQueues_pattern2 = new LinkedHashSet<> ();
    subQueues_pattern2.add (is0);
    subQueues_pattern2.add (is1);
    subQueues_pattern2.add (is2);
    final Pattern pattern2 =
      new Pattern (eventList, subQueues_pattern2, null, new int[] { 0, 1, 2, 2, 1, 0, 2, 1 });
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (pattern2, null, new IS (eventList), numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Pattern{}[DROP] == IC
    final DROP drop0 = new DROP (eventList);
    final Pattern pattern3 =
      new Pattern (eventList, Collections.singleton (drop0), null, new int[] { });
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (pattern3, null, new IC (eventList), numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
    // Pattern{0}[WUR] == SUR
    final WUR wur0 = new WUR (eventList);
    final Pattern pattern4 =
      new Pattern (eventList, Collections.singleton (wur0), null, new int[] { 0 });
    DefaultSimQueueTests.doSimQueueTests_SQ_SV
      (pattern4, null, new SUR (eventList), numberOfJobs, null, silent, deadSilent, 1.0e-12, null, null, null);
  }

}
