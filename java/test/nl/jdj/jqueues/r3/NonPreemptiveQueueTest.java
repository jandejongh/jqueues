package nl.jdj.jqueues.r3;

import nl.jdj.jqueues.r3.AbstractSimJob;
import nl.jdj.jqueues.r3.SimQueue;
import nl.jdj.jqueues.r3.NonPreemptiveQueue;
import nl.jdj.jqueues.r3.SimJob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.jdj.jsimulation.r3.SimEvent;
import nl.jdj.jsimulation.r3.SimEventAction;
import nl.jdj.jsimulation.r3.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class NonPreemptiveQueueTest
{
  
  public NonPreemptiveQueueTest ()
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

  public static class TestJob extends AbstractSimJob
  {
    
    private final boolean reported;
    
    public final int n;
    
    public TestJob (boolean reported, int n)
    {
      if (n <= 0)
        throw new IllegalArgumentException ();
      this.reported = reported;
      this.n = n;
    }

    public boolean arrived = false;
    
    public boolean started = false;
    
    public boolean departed = false;
    
    public double arrivalTime = 0.0;
    
    public double startTime = 0.0;
    
    public double departureTime = 0.0;
    
    @Override
    public double getServiceTime (SimQueue queue) throws IllegalArgumentException
    {
      if (queue == null && getQueue () == null)
        return 0.0;
      else
        return (double) n;
    }
    
    public final SimEventAction<SimJob> QUEUE_ARRIVE_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " arrives.");
        if (TestJob.this.arrived)
          fail ("Already arrived!");
        TestJob.this.arrived = true;
        TestJob.this.arrivalTime = event.getTime ();
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueArriveAction ()
    {
      return this.QUEUE_ARRIVE_ACTION;
    }
    
    public final SimEventAction<SimJob> QUEUE_START_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " starts.");      
        if (TestJob.this.started)
          fail ("Already started!");
        if (! TestJob.this.arrived)
          fail ("Starting before arrival!");
        TestJob.this.started = true;
        TestJob.this.startTime = event.getTime ();
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueStartAction ()
    {
      return this.QUEUE_START_ACTION;
    }

    public final SimEventAction<SimJob> QUEUE_DEPART_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " departs.");      
        if (TestJob.this.departed)
          fail ("Already departed!");
        if (! TestJob.this.arrived)
          fail ("Departure before arrival!");
        if (! TestJob.this.started)
          fail ("Departure before start!");
        TestJob.this.departed = true;
        TestJob.this.departureTime = event.getTime ();
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueDepartAction ()
    {
      return this.QUEUE_DEPART_ACTION;
    }

  }
  
  public static List<TestJob> scheduleJobArrivals (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    final List<TestJob> jobList = new ArrayList<>  ();
    for (int i = 1; i <= n; i++)
    {
      final TestJob j = new TestJob (reported, i);
      jobList.add (j);
      final double arrTime = i;
      eventList.add (new SimEvent ("ARRIVAL_" + i, arrTime, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          queue.arrive (j, arrTime);
        }
      }));
    }
    return jobList;
  }
  
  /**
   * Test of NonPreemptiveQueue.NONE.
   * 
   */
  @Test
  public void testNonPreemptiveQueueNONE ()
  {
    System.out.println ("=======================");
    System.out.println ("NonPreemptiveQueue.NONE");
    System.out.println ("=======================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.NONE queue = new NonPreemptiveQueue.NONE (el);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
      el.run ();
      assert el.isEmpty ();
      assertEquals (10.0, el.getTime (), 0.0);
      for (TestJob j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        assert ! j.started;
        assert ! j.departed;
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  private int triangular (int n)
  {
    if (n < 0)
      throw new RuntimeException ();
    if (n == 0)
      return 0;
    return (n * (n+1)) / 2;
  }
  
  /**
   * Test of NonPreemptiveQueue.FIFO.
   * 
   */
  @Test
  public void testNonPreemptiveQueueFIFO ()
  {
    System.out.println ("=======================");
    System.out.println ("NonPreemptiveQueue.FIFO");
    System.out.println ("=======================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.FIFO queue = new NonPreemptiveQueue.FIFO (el);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
      el.run ();
      assert el.isEmpty ();
      assertEquals (56.0, el.getTime (), 0.0);
      for (TestJob j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        assert j.started;
        assertEquals (1.0 + (double) (triangular (j.n - 1)), j.startTime, 0.0);
        assert j.departed;
        assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  /**
   * Test of NonPreemptiveQueue.LIFO.
   * 
   */
  @Test
  public void testNonPreemptiveQueueLIFO ()
  {
    System.out.println ("=======================");
    System.out.println ("NonPreemptiveQueue.LIFO");
    System.out.println ("=======================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.LIFO queue = new NonPreemptiveQueue.LIFO (el);
    final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob j : jobs)
    {
      assert j.arrived;
      assertEquals ((double) j.n, j.arrivalTime, 0.0);
      assert j.started;
      assert j.departed;
      if (j.n == 1)
        assertEquals (1.0, j.startTime, 0.0);
      else if (j.n == 2)
        assertEquals (2.0, j.startTime, 0.0);
      // Job 3 arrives at t=3, 2 still being in service.
      // Job 4 arrives exactly at the same time as the departure of job 2.
      // So, either job 3 or job 4 is taken into service at t = 4.
      // Given the nature of the underlying event list, both options have equal probability.
      // We check for them both.
      else if (jobs.get (2).startTime < jobs.get (3).startTime)
      {
        // Job 3 (!) started at t = 4, running from 4 to 7 inclusive.
        if (j.n == 3)
          assertEquals (4.0, j.startTime, 0.0);
        // At t=7 two valid options: 6 from 7 to 13, or 7 from 7 to 14.
        else if (jobs.get (5).startTime < jobs.get (6).startTime)
        {
          // t=7: Job 6 from 7 to 13.
          if (j.n == 6)
            assertEquals (7.0, j.startTime, 0.0);
          // The remaining jobs are now served from t=13 onwards without ties.
          else switch (j.n)
          {
            case 10:
              assertEquals (13.0, j.startTime, 0.0);
              break;
            case 9:
              assertEquals (23.0, j.startTime, 0.0);
              break;
            case 8:
              assertEquals (32.0, j.startTime, 0.0);
              break;
            case 7:
              assertEquals (40.0, j.startTime, 0.0);
              break;
            case 5:
              assertEquals (47.0, j.startTime, 0.0);
              break;
            case 4:
              assertEquals (52.0, j.startTime, 0.0);
              break;
            default:
              fail ("Unexpected index.");
          }
        }
        else
        {
          // t=7: Job 7 from 7 to 14.
          if (j.n == 7)
            assertEquals (7.0, j.startTime, 0.0);
          // The remaining jobs are now served from t=14 onwards without ties.
          else switch (j.n)
          {
            case 10:
              assertEquals (14.0, j.startTime, 0.0);
              break;
            case 9:
              assertEquals (24.0, j.startTime, 0.0);
              break;
            case 8:
              assertEquals (33.0, j.startTime, 0.0);
              break;
            case 6:
              assertEquals (41.0, j.startTime, 0.0);
              break;
            case 5:
              assertEquals (47.0, j.startTime, 0.0);
              break;
            case 4:
              assertEquals (52.0, j.startTime, 0.0);
              break;
            default:
              fail ("Unexpected index.");
          }
        }
      }
      else
      {
        // Job 4 (!) started at t = 4, running from 4 to 8 inclusive.
        if (j.n == 4)
          assertEquals (4.0, j.startTime, 0.0);
        // t=8: either 7 (from 8 to 15) or 8 (from 8 to 16).
        else if (jobs.get (6).startTime < jobs.get (7).startTime)
        {
          // t=8: Job 7 from 8 to 15.
          if (j.n == 7)
            assertEquals (8.0, j.startTime, 0.0);
          // The remaining jobs are now served from t=15 onwards without ties.
          else switch (j.n)
          {
            case 10:
              assertEquals (15.0, j.startTime, 0.0);
              break;
            case 9:
              assertEquals (25.0, j.startTime, 0.0);
              break;
            case 8:
              assertEquals (34.0, j.startTime, 0.0);
              break;
            case 6:
              assertEquals (42.0, j.startTime, 0.0);
              break;
            case 5:
              assertEquals (48.0, j.startTime, 0.0);
              break;
            case 3:
              assertEquals (53.0, j.startTime, 0.0);
              break;
            default:
              fail ("Unexpected index.");
          }          
        }
        else
        {
          // t=8: Job 8 from 8 to 16.
          if (j.n == 8)
            assertEquals (8.0, j.startTime, 0.0);
          // The remaining jobs are now served from t=16 onwards without ties.
          else switch (j.n)
          {
            case 10:
              assertEquals (16.0, j.startTime, 0.0);
              break;
            case 9:
              assertEquals (26.0, j.startTime, 0.0);
              break;
            case 7:
              assertEquals (35.0, j.startTime, 0.0);
              break;
            case 6:
              assertEquals (42.0, j.startTime, 0.0);
              break;
            case 5:
              assertEquals (48.0, j.startTime, 0.0);
              break;
            case 3:
              assertEquals (53.0, j.startTime, 0.0);
              break;
            default:
              fail ("Unexpected index.");
          }          
        }
      }
      assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
    }
  }

  /**
   * Test of NonPreemptiveQueue.RANDOM.
   * 
   */
  @Test
  public void testNonPreemptiveQueueRANDOM ()
  {
    System.out.println ("=========================");
    System.out.println ("NonPreemptiveQueue.RANDOM");
    System.out.println ("=========================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.RANDOM queue = new NonPreemptiveQueue.RANDOM (el);
    final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob j : jobs)
    {
      assert j.arrived;
      assertEquals ((double) j.n, j.arrivalTime, 0.0);
      assert j.started;
      // XXX: We do not consider all possible schedules and test for this.
      // However, a more complete test should be realized...
      assert j.departed;
      assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
    }
  }

  private List<TestJob> scheduleJobArrivalsSJF (final boolean reported, final int n, final SimEventList eventList, final NonPreemptiveQueue queue)
  {
    // Job 1 is scheduled at t = 1, req service time = 1.
    final TestJob j1 = new TestJob (reported, 1);
    eventList.add (new SimEvent ("ARRIVAL_" + 1, 1.0, null, new SimEventAction ()
    {
      @Override
      public void action (final SimEvent event)
      {
        queue.arrive (j1, 1.0);
      }
    }));
    final List<TestJob> jobList = new ArrayList<>  ();
    for (int i = 2; i <= n; i++)
    {
      final TestJob j = new TestJob (reported, i);
      jobList.add (j);
    }
    Collections.shuffle (jobList);
    for (int i = 0; i < (n - 1); i++)
    {
      final TestJob j = jobList.get (i);
      final double startTime = 1.5 + 0.05*i;
      eventList.add (new SimEvent ("ARRIVAL_" + j.n, startTime, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          queue.arrive (j, startTime);
        }
      }));
    }
    jobList.add (0, j1);
    return jobList;
  }
  
  /**
   * Test of NonPreemptiveQueue.SJF.
   * 
   */
  @Test
  public void testNonPreemptiveQueueSJF ()
  {
    System.out.println ("======================");
    System.out.println ("NonPreemptiveQueue.SJF");
    System.out.println ("======================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.SJF queue = new NonPreemptiveQueue.SJF (el);
    final List<TestJob> jobs = scheduleJobArrivalsSJF (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob j : jobs)
    {
      assert j.arrived;
      if (j.n == 1)
        assertEquals (1.0, j.arrivalTime, 0.0);
      else
        assertEquals (1.75, j.arrivalTime, 0.25);        
      assert j.started;
      assertEquals (1.0 + (double) (triangular (j.n - 1)), j.startTime, 0.0);
      assert j.departed;
      assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
    }
  }

  private List<TestJob> scheduleJobArrivalsLJF (final boolean reported, final int n, final SimEventList eventList, final NonPreemptiveQueue queue)
  {
    return scheduleJobArrivalsSJF (reported, n, eventList, queue);
  }
  
  /**
   * Test of NonPreemptiveQueue.LJF.
   * 
   */
  @Test
  public void testNonPreemptiveQueueLJF ()
  {
    System.out.println ("======================");
    System.out.println ("NonPreemptiveQueue.LJF");
    System.out.println ("======================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.LJF queue = new NonPreemptiveQueue.LJF (el);
    final List<TestJob> jobs = scheduleJobArrivalsLJF (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob j : jobs)
    {
      assert j.arrived;
      if (j.n == 1)
        assertEquals (1.0, j.arrivalTime, 0.0);
      else
        assertEquals (1.75, j.arrivalTime, 0.25);        
      assert j.started;
      switch (j.n)
      {
        case 1:
          // 1: 1..2
          assertEquals (1.0, j.startTime, 0.0);
          break;
        case 10:
          // 10: 2..12
          assertEquals (2.0, j.startTime, 0.0);
          break;
        case 9:
          // 9: 12..21
          assertEquals (12.0, j.startTime, 0.0);
          break;
        case 8:
          // 8: 21..29
          assertEquals (21.0, j.startTime, 0.0);
          break;
        case 7:
          // 7: 29..36
          assertEquals (29.0, j.startTime, 0.0);
          break;
        case 6:
          // 6: 36..42
          assertEquals (36.0, j.startTime, 0.0);
          break;
        case 5:
          // 5: 42..47
          assertEquals (42.0, j.startTime, 0.0);
          break;
        case 4:
          // 4: 47..51
          assertEquals (47.0, j.startTime, 0.0);
          break;
        case 3:
          // 3: 51..54
          assertEquals (51.0, j.startTime, 0.0);
          break;
        case 2:
          // 2: 54..56
          assertEquals (54.0, j.startTime, 0.0);
          break;
        default:
          fail ("Unexpected job number: " + j.n + ".");        
      }
      assert j.departed;
      assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
    }
  }

  /**
   * Test of NonPreemptiveQueue.IS.
   * 
   */
  @Test
  public void testNonPreemptiveQueueIS ()
  {
    System.out.println ("=====================");
    System.out.println ("NonPreemptiveQueue.IS");
    System.out.println ("=====================");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    final NonPreemptiveQueue.IS queue = new NonPreemptiveQueue.IS (el);
    final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (20.0, el.getTime (), 0.0);
    for (TestJob j : jobs)
    {
      assert j.arrived;
      assertEquals ((double) j.n, j.arrivalTime, 0.0);
      assert j.started;
      assertEquals ((double) j.n, j.startTime, 0.0);
      assert j.departed;
      assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
    }
  }

}