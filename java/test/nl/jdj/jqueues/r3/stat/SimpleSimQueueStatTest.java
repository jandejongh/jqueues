/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.jdj.jqueues.r3.stat;

import nl.jdj.jqueues.r3.stat.SimpleSimQueueStat;
import nl.jdj.jqueues.r3.NonPreemptiveQueue;
import nl.jdj.jqueues.r3.NonPreemptiveQueueTest;
import nl.jdj.jqueues.r3.SimQueue;
import nl.jdj.jsimulation.r3.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jan
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
    final SimEventList eventList = new SimEventList ();
    expResult = new NonPreemptiveQueue.LIFO<> (eventList);
    instance.setQueue (expResult);
    result = instance.getQueue ();
    assertEquals (expResult, result);
    instance.setQueue (null);
    result = instance.getQueue ();
    assertEquals (null, result);
    expResult = new NonPreemptiveQueue.IS<> (new SimEventList ());
    instance = new SimpleSimQueueStat (expResult, 500.0);
    result = instance.getQueue ();
    assertEquals (expResult, result);
  }

  /**
   * Test of getStartTime/setStartTime methods, of class SimpleSimQueueStat.
   */
  @Test
  public void testStartTime ()
  {
    System.out.println ("StartTime");
    final SimEventList eventList = new SimEventList ();
    final SimQueue lifo  = new NonPreemptiveQueue.LIFO<> (eventList);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (lifo, 400.5);
    Double expResult = 400.5;
    Double result = instance.getStartTime ();
    assertEquals (expResult, result, 0.0);
    instance.setStartTime (-123.87);
    expResult = -123.87;
    result = instance.getStartTime ();
    assertEquals (expResult, result, 0.0);
    NonPreemptiveQueueTest.scheduleJobArrivals (false, 10, eventList, lifo);
    eventList.run ();
    // Should not have changed...
    result = instance.getStartTime ();
    assertEquals (expResult, result, 0.0);
    instance.reset ();
    // Should pick the last update time now as start time.
    expResult = eventList.getTime ();
    result = instance.getStartTime ();
    assertEquals (expResult, result, 0.0);
    expResult = Double.NEGATIVE_INFINITY;
    instance.setStartTime (expResult);
    result = instance.getStartTime ();
    assertEquals (expResult, result, 0.0);
  }

  /**
   * Test of statisticsValid method, of class SimpleSimQueueStat.
   */
  @Test
  public void testStatisticsValid ()
  {
    System.out.println ("StatisticsValid");
    final SimEventList eventList = new SimEventList ();
    final SimQueue random  = new NonPreemptiveQueue.RANDOM<>(eventList);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (random, 400.5);
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
    final SimEventList eventList = new SimEventList ();
    final SimQueue lifo  = new NonPreemptiveQueue.LIFO<> (eventList);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (lifo, -10.0);
    Double expResult = -10.0;
    Double result = instance.getLastUpdateTime ();
    assertEquals (expResult, result, 0.0);
    NonPreemptiveQueueTest.scheduleJobArrivals (false, 10, eventList, lifo);
    eventList.run ();
    expResult = eventList.getTime ();
    result = instance.getLastUpdateTime ();
    assertEquals (expResult, result, 0.0);
    instance.reset ();
    assertEquals (expResult, result, 0.0);
  }

  /**
   * Test of getAvgNrOfJobs method, of class SimpleSimQueueStat.
   */
  @Test
  public void testGetAvgNrOfJobs ()
  {
    System.out.println ("Stats: AvgNumberOfJobs/AvgNumberOfJobsExecuting");
    final SimEventList eventList = new SimEventList ();
    SimQueue queue  = new NonPreemptiveQueue.LIFO<> (eventList);
    SimpleSimQueueStat instance = new SimpleSimQueueStat (queue, 0.0);
    // 1 Job arriving at t = 1, S = 1.
    // So: [0,1)->0 job, [1,2]->1 job.
    // Average expected: 0.5.
    NonPreemptiveQueueTest.scheduleJobArrivals (false, 1, eventList, queue);
    eventList.run ();
    Double expResult;
    Double result;
    expResult = 0.5;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    result = instance.getAvgNrOfJobsExecuting ();
    assertEquals (expResult, result, 0.0);
    // Compared to the first case: start at -2.
    // Average expected: 0.25.
    eventList.reset ();
    queue = new NonPreemptiveQueue.FCFS<> (eventList);
    instance = new SimpleSimQueueStat (queue, -2.0);
    NonPreemptiveQueueTest.scheduleJobArrivals (false, 1, eventList, queue);
    eventList.run ();
    expResult = 0.25;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.0);
    result = instance.getAvgNrOfJobsExecuting ();
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
    eventList.reset ();
    queue = new NonPreemptiveQueue.FCFS<> (eventList);
    instance = new SimpleSimQueueStat (queue, 0.0);
    NonPreemptiveQueueTest.scheduleJobArrivals (false, 4, eventList, queue);
    eventList.run ();
    expResult = 14.0/11.0;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.000000001);
    expResult = 10.0/11.0;
    result = instance.getAvgNrOfJobsExecuting ();
    assertEquals (expResult, result, 0.000000001);
    // Try the same scenario with IS.
    // Start sim at 0.
    // t = 0   1   2   3   4   5   6   7   8
    // 1:      XXXX
    // 2:          XXXXXXXX
    // 3:              XXXXXXXXXXXX
    // 4:                  XXXXXXXXXXXXXXXX
    // J =   0   1   1   2   2   2   1   1
    // AvgJ = 10/8.
    eventList.reset ();
    queue = new NonPreemptiveQueue.IS<> (eventList);
    instance = new SimpleSimQueueStat (queue, 0.0);
    NonPreemptiveQueueTest.scheduleJobArrivals (false, 4, eventList, queue);
    eventList.run ();
    expResult = 10.0/8.0;
    result = instance.getAvgNrOfJobs ();
    assertEquals (expResult, result, 0.000000001);
    result = instance.getAvgNrOfJobsExecuting ();
    assertEquals (expResult, result, 0.000000001);
  }

}