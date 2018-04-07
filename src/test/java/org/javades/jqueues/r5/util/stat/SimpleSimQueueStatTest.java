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
package org.javades.jqueues.r5.util.stat;

import org.javades.jqueues.r5.entity.jq.job.DefaultSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.IS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.RANDOM;
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

/** Tests for {@link SimpleSimQueueStat}.
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
public class SimpleSimQueueStatTest
{
  
  public SimpleSimQueueStatTest ()
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

  private static void scheduleJobArrivals
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    for (int i = 1; i <= n; i++)
    {
      final SimJob job = new DefaultSimJob (eventList, Integer.toString (i), i);
      final double arrTime = i;
      eventList.add (new DefaultSimEvent ("ARRIVAL_" + i, arrTime, null, (SimEventAction) (final SimEvent event) ->
      {
        queue.arrive (event.getTime (), job);
      }));
    }
  }
  
  /**
   * Test of getQueue/setQueue methods, of class SimpleSimQueueStat.
   */
  @Test
  public void testQueue ()
  {
    System.out.println ("Queue");
    SimpleSimQueueStat instance = new SimpleSimQueueStat ();
    SimQueue expResult = null;
    SimQueue result = instance.getQueue ();
    assertEquals (expResult, result);
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    expResult = new LCFS<> (eventList);
    instance.setQueue (expResult);
    result = instance.getQueue ();
    assertEquals (expResult, result);
    instance.setQueue (null);
    result = instance.getQueue ();
    assertEquals (null, result);
    expResult = new IS<> (new DefaultSimEventList (DefaultSimEvent.class));
    instance = new SimpleSimQueueStat (expResult);
    result = instance.getQueue ();
    assertEquals (expResult, result);
  }

  /**
   * Test of statisticsValid method, of class SimpleSimQueueStat.
   */
  @Test
  public void testStatisticsValid ()
  {
    System.out.println ("StatisticsValid");
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    eventList.reset (0);
    final SimQueue random  = new RANDOM<>(eventList);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (random);
    Boolean expResult = false;
    Boolean result = instance.getStatisticsValid ();
    assertEquals (expResult, result);
    double dummy = instance.getAvgNrOfJobs ();
    expResult = true;
    result = instance.getStatisticsValid ();
    assertEquals (expResult, result);
  }

  /**
   * Test of getLastUpdateTime method, of class SimpleSimQueueStat.
   */
  @Test
  public void testGetLastUpdateTime ()
  {
    System.out.println ("LastUpdateTime");
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    eventList.reset (-10.0);
    final SimQueue lifo  = new LCFS<> (eventList);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (lifo);
    Double expResult = -10.0;
    Double result = instance.getLastUpdateTime ();
    assertEquals (expResult, result, 0.0);
    scheduleJobArrivals (false, 10, eventList, lifo);
    eventList.run ();
    expResult = eventList.getTime ();
    result = instance.getLastUpdateTime ();
    assertEquals (expResult, result, 0.0);
    instance.reset ();
    assertEquals (expResult, result, 0.0);
  }

  /**
   * Test of statistics of class SimpleSimQueueStat.
   */
  @Test
  public void testStatistcs ()
  {
    System.out.println ("Stats: Avg/Min/Max NumberOfJobs and Avg/Min/Max NumberOfJobsInServiceArea");
    final SimEventList eventList = new DefaultSimEventList (DefaultSimEvent.class);
    SimQueue queue  = new LCFS<> (eventList);
    eventList.reset (0);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (queue);
    // 1 Job arriving at t = 1, S = 1.
    // So: [0,1)->0 job, [1,2]->1 job.
    // Average expected: 0.5.
    // Minimum expected: 0.0.
    // Maximum expected: 1.0;
    scheduleJobArrivals (false, 1, eventList, queue);
    eventList.run ();
    Double expResult;
    Double result;
    expResult = 0.5;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    result = instance.getAvgNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    expResult = 0.0;
    result = instance.getMinNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 1.0;
    result = instance.getMaxNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    // Compared to the first case: start at -2.
    // Average expected: 0.25.
    // Minimum expected: 0.0.
    // Maximum expected: 1.0;
    eventList.reset (-2.0);
    queue = new FCFS<> (eventList);
    instance = new SimpleSimQueueStat (queue);
    scheduleJobArrivals (false, 1, eventList, queue);
    eventList.run ();
    expResult = 0.25;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    result = instance.getAvgNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    expResult = 0.0;
    result = instance.getMinNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 1.0;
    result = instance.getMaxNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 0.0;
    result = instance.getMinNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    expResult = 1.0;
    result = instance.getMaxNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    // Try a few more (4) jobs with FCFS.
    // Start sim at 0.
    // t = 0   1   2   3   4   5   6   7   8   9   10   11
    // 1:      XXXX
    // 2:          XXXXXXXX
    // 3:              XXXXXXXXXXXXXXXX
    // 4:                  XXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // J =   0   1   1   2   2   2   2   1   1   1   1
    // AvgJ = 14/11.
    // minJ: 0.
    // maxJ: 2.
    // minJX: 0.
    // maxJX: 1.
    eventList.reset (0.0);
    queue = new FCFS<> (eventList);
    instance = new SimpleSimQueueStat (queue);
    scheduleJobArrivals (false, 4, eventList, queue);
    eventList.run ();
    expResult = 14.0/11.0;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.000000001);
    expResult = 10.0/11.0;
    result = instance.getAvgNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.000000001);
    expResult = 0.0;
    result = instance.getMinNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 2.0;
    result = instance.getMaxNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 0.0;
    result = instance.getMinNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    expResult = 1.0;
    result = instance.getMaxNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    // Try the same scenario with IS.
    // Start sim at 0.
    // t = 0   1   2   3   4   5   6   7   8
    // 1:      XXXX
    // 2:          XXXXXXXX
    // 3:              XXXXXXXXXXXX
    // 4:                  XXXXXXXXXXXXXXXX
    // J =   0   1   1   2   2   2   1   1
    // AvgJ = 10/8.
    // minJ: 0.
    // maxJ: 2.
    // minJX: 0.
    // maxJX: 2.
    // XXX In terms of maxJ/maxJX, this test is ambiguous...
    eventList.reset (0.0);
    queue = new IS<> (eventList);
    instance = new SimpleSimQueueStat (queue);
    scheduleJobArrivals (false, 4, eventList, queue);
    eventList.run ();
    expResult = 10.0/8.0;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.000000001);
    result = instance.getAvgNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.000000001);
    expResult = 0.0;
    result = instance.getMinNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 2.0;
    result = instance.getMaxNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    expResult = 0.0;
    result = instance.getMinNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
    expResult = 2.0;
    result = instance.getMaxNrOfJobsInServiceArea ();
    assertEquals (expResult, result, 0.0);
  }

}
