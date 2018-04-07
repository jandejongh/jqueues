/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.entity.jq.queue.nonpreemptive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.DefaultVisitsLoggingSimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link RANDOM}.
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
public class RANDOMTest
{
  
  public RANDOMTest ()
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

  private static class TestJob extends DefaultVisitsLoggingSimJob<TestJob, RANDOM>
  {

    public TestJob (final SimEventList eventList, final int i)
    {
      super (eventList, Integer.toString (i), 1.0);
    }
    
    public final double getStartTime ()
    {
      final JobQueueVisitLog jvl = getVisitLogs ().values ().iterator ().next ().values ().iterator ().next ();
      return jvl.startTime;
    }
    
  }
  
  // dof -> (alpha -> critical value).
  private static final Map<Integer, Map<Double, Double>> CSCriticalValue = new HashMap<> ();
  static
  {
    final Map<Double, Double> dof3Map = new HashMap<> ();
    CSCriticalValue.put (3, dof3Map);
    dof3Map.put (0.99,  0.115);
    dof3Map.put (0.95,  0.352);
    dof3Map.put (0.90,  0.584);    
    dof3Map.put (0.10,  6.251);
    dof3Map.put (0.05,  7.815);
    dof3Map.put (0.01, 11.345);
    final Map<Double, Double> dof9Map = new HashMap<> ();
    CSCriticalValue.put (9, dof9Map);
    dof9Map.put (0.99,  2.088);
    dof9Map.put (0.95,  3.325);
    dof9Map.put (0.90,  4.168);    
    dof9Map.put (0.10, 14.684);
    dof9Map.put (0.05, 16.919);
    dof9Map.put (0.01, 21.666);
  }
  
  private static List<TestJob> scheduleJobArrivals
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    if (n >= 100)
      throw new UnsupportedOperationException ();
    final List<TestJob> jobList = new ArrayList<>  ();
    eventList.schedule (0.0, (SimEventAction) (SimEvent event) ->
    {
      queue.setServerAccessCredits (event.getTime (), 0);
    });
    for (int i = 1; i <= n; i++)
    {
      final TestJob job = new TestJob (eventList, i);
      jobList.add (job);
      final double arrTime = i;
      eventList.add (new DefaultSimEvent ("ARRIVAL_" + i, arrTime, null, (SimEventAction) (final SimEvent event) ->
      {
        queue.arrive (event.getTime (), job);
      }));
    }
    eventList.schedule (100.0, (SimEventAction) (SimEvent event) ->
    {
      queue.setServerAccessCredits (event.getTime (), Integer.MAX_VALUE);
    });
    return jobList;
  }
  
  /**
   * Test of RANDOM.
   * 
   */
  @Test
  public void testRANDOM ()
  {
    System.out.println ("SimQueue Test [Custom RANDOM]");
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    final RANDOM queue = new RANDOM (el);
    final int NR_OF_JOBS = 10;
    final int NR_OF_SAMPLES = 100 * NR_OF_JOBS;
    final Map<Double, Integer> startTimes = new HashMap<> ();
    for (int n = 0; n < NR_OF_JOBS; n++)
      startTimes.put (100.0 + n, 0);
    final Map<Integer, Map<Integer, Integer>> frequencies = new HashMap<> ();
    for (int n = 1; n <= NR_OF_JOBS; n++)
    {
      frequencies.put (n, new HashMap<> ());
      for (int ord = 1; ord <= NR_OF_JOBS; ord++)
        frequencies.get (n).put (ord, 0);
    }
    for (int sample = 1; sample <= NR_OF_SAMPLES; sample++)
    {
      el.reset (0);
      final List<TestJob> jobs = scheduleJobArrivals (false, NR_OF_JOBS, el, queue);
      el.run ();
      assert el.isEmpty ();
      assertEquals (100.0 + NR_OF_JOBS, el.getTime (), 0.0);
      int j = 1; // Job number.
      for (TestJob job : jobs)
      {
        final double startTime = job.getStartTime ();
        assert startTimes.containsKey (startTime);
        startTimes.put (startTime, startTimes.get (startTime) + 1);
        final int ord = 1 + (int) Math.round (startTime - 100.0);
        frequencies.get (j).put (ord, frequencies.get (j).get (ord) + 1);
        j++;
      }
      for (int jStarted : startTimes.values ())
        assertEquals (jStarted, sample);
    }
    // Calculate the Chi-Square statistic (Pearson's cumulative test statistic) for each job index.
    // For a specific job (index), each possible start time (there are NR_OF_JOBS of them) has equal probability.
    final Set<Double> chiSquares = new LinkedHashSet<>  ();
    final double expFrequency = ((double) NR_OF_SAMPLES) / NR_OF_JOBS;
    for (final Map<Integer, Integer> frequencies_j : frequencies.values ())
    {
      double chiSquare_j = 0;
      for (final int frequency : frequencies_j.values ())
      {
        final double dFrequency = frequency - expFrequency;
        chiSquare_j += Math.pow (dFrequency, 2);
      }
      chiSquare_j = chiSquare_j / expFrequency;
      chiSquares.add (chiSquare_j);
    }
    // Each Chi-Square statistic found has approximately Chi-Square distribution with NR_OF_JOBS - 1 degrees of freedom.
    final double SIGNIFICANCE = 0.99;
    final double ONE_MINUS_SIGNIFICANCE = 0.01; // Sigh, 1.0 - 0.99 is not exactly 0.01...
    if (CSCriticalValue.containsKey (NR_OF_JOBS - 1))
    {
      if (CSCriticalValue.get (NR_OF_JOBS - 1).containsKey (SIGNIFICANCE)
       && CSCriticalValue.get (NR_OF_JOBS - 1).containsKey (ONE_MINUS_SIGNIFICANCE))
      {
        final Set<Double> chiSquaresRejected = new LinkedHashSet<> ();
        for (final double chiSquare : chiSquares)
          if (chiSquare < CSCriticalValue.get (NR_OF_JOBS - 1).get (SIGNIFICANCE)
           || chiSquare > CSCriticalValue.get (NR_OF_JOBS - 1).get (ONE_MINUS_SIGNIFICANCE))
            chiSquaresRejected.add (chiSquare);
        if (! chiSquaresRejected.isEmpty ())
          {
            System.err.println ("  => Found RANDOM H-reject(s) with Chi-Square GOF Test:");
            System.err.println ("     Chi-Square Statistics         : "
              + chiSquares);
            System.err.println ("     Chi-Square Statistics Rejected: "
              + chiSquaresRejected);
            System.err.println ("     Critical Lower Value          : "
              + CSCriticalValue.get (NR_OF_JOBS - 1).get (SIGNIFICANCE));
            System.err.println ("     Critical Upper Value          : "
              + CSCriticalValue.get (NR_OF_JOBS - 1).get (ONE_MINUS_SIGNIFICANCE));
            System.err.println ("     Significance                  : "
              + SIGNIFICANCE);
          }
      }
      else
        System.err.println ("  => Unsupported significance level for Chi-Square GOF Test.");
    }
    else
      System.err.println ("  => Unsupported number of jobs for Chi-Square GOF Test.");
  }

}
