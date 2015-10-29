package nl.jdj.jqueues.r4.nonpreemptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.jdj.jqueues.r4.DefaultSimQueueVacationListener;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.TestJob1;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;
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
public class NonPreemptiveTest
{
  
  public NonPreemptiveTest ()
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

  public static List<TestJob1> scheduleJobArrivals
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    final List<TestJob1> jobList = new ArrayList<>  ();
    for (int i = 1; i <= n; i++)
    {
      final TestJob1 j = new TestJob1 (reported, i);
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
  
  public static List<TestJob1> scheduleJobArrivals2
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    final List<TestJob1> jobList = new ArrayList<>  ();
    for (int i = 1; i <= n; i++)
    {
      final TestJob1 j = new TestJob1 (reported, i);
      jobList.add (j);
      final double arrTime = i + i*0.1;
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
  
  private int triangular (int n)
  {
    if (n < 0)
      throw new RuntimeException ();
    if (n == 0)
      return 0;
    return (n * (n+1)) / 2;
  }
  
  /**
   * Test of FCFS.
   * 
   */
  @Test
  public void testFCFS ()
  {
    System.out.println ("====");
    System.out.println ("FCFS");
    System.out.println ("====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final FCFS queue = new FCFS (el);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
      el.run ();
      assert el.isEmpty ();
      assertEquals (56.0, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
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
   * Test of FCFS_B.
   * 
   */
  @Test
  public void testFCFS_B ()
  {
    System.out.println ("======");
    System.out.println ("FCFS_B");
    System.out.println ("======");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final FCFS_B queue = new FCFS_B (el, 2);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals2 (true, 10, el, queue);
      // Arrival times:     Service   Waiting upon arrival
      //  1:  1.1           1.1-2.1   0
      //                    2.1: 1 departs
      //  2:  2.2           2.2-4.2   0
      //  3:  3.3           4.2-7.2   0
      //                    4.2: 2 departs
      //  4:  4.4           7.2-11.2  0
      //  5:  5.5           11.2-16.2 1
      //  6:  6.6           DROPPED   2
      //                    7.2: 3 departs
      //  7:  7.7           16.2-23.2 1
      //  8:  8.8           DROPPED   2
      //  9:  9.9           DROPPED   2
      // 10: 11.0           DROPPED   2
      //                    11.2: 4 departs
      //                    16.2  5 departs
      //                    23.2  7 departs
      //              
      el.run ();
      assert el.isEmpty ();
      assertEquals (23.2, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n + j.n * 0.1, j.arrivalTime, 0.0);
        boolean dropped = j.n == 6 || j.n == 8 || j.n == 9 || j.n == 10;
        if (dropped)
        {
          assert j.dropped;
          assertEquals (j.arrivalTime, j.dropTime, 0.0);
          assert ! j.started;
          assert ! j.departed;
        }
        else
        {
          assert ! j.dropped;
          assert j.started;
          if (j.n == 1)
            assertEquals (1.1, j.startTime, 0.0);
          else if (j.n == 2)
            assertEquals (2.2, j.startTime, 0.0);
          else if (j.n == 3)
            assertEquals (4.2, j.startTime, 0.0);
          else if (j.n == 4)
            assertEquals (7.2, j.startTime, 0.0);
          else if (j.n == 5)
            assertEquals (11.2, j.startTime, 0.0);
          else if (j.n == 7)
            assertEquals (16.2, j.startTime, 0.0);
          else
            // Should not reach...
            assert false;
          assert j.departed;
          assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
        }
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  /**
   * Test of FCFS_c.
   * 
   */
  @Test
  public void testFCFS_c ()
  {
    System.out.println ("======");
    System.out.println ("FCFS_c");
    System.out.println ("======");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final FCFS_c queue = new FCFS_c (el, 3);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
      // Arrival times:     Service   Serving upon arrival
      //  1:  1             1-2       0
      //  2:  2             2-4       0
      //  3:  3             3-6       1
      //  4:  4             4-8       1
      //  5:  5             5-10      2
      //  6:  6             6-12      2
      //  7:  7           * 8-15      3
      //  8:  8           * 10-18     3
      //  9:  9           * 12-21     3
      // 10: 10           * 15-25     3
      //
      el.run ();
      assert el.isEmpty ();
      assertEquals (25.0, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        assert j.started;
        if (j.n <= 6)
          assertEquals (j.arrivalTime, j.startTime, 0.0);
        else if (j.n == 7)
          assertEquals (8.0, j.startTime, 0.0);
        else if (j.n == 8)
          assertEquals (10.0, j.startTime, 0.0);
        else if (j.n == 9)
          assertEquals (12.0, j.startTime, 0.0);
        else if (j.n == 10)
          assertEquals (15.0, j.startTime, 0.0);
        else
          assert false;
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  /**
   * Test of NoBuffer_c.
   * 
   */
  @Test
  public void testNoBuffer_c ()
  {
    System.out.println ("==========");
    System.out.println ("NoBuffer_c");
    System.out.println ("==========");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final NoBuffer_c queue = new NoBuffer_c (el, 2);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals2 (true, 10, el, queue);
      // Arrival times:     Service   Present upon arrival
      //  1:  1.1           1.1-2.1   0
      //                    2.1: 1 departs
      //  2:  2.2           2.2-4.2   0
      //  3:  3.3           3.3-6.3   1
      //                    4.2: 2 departs
      //  4:  4.4           4.4-8.4   1
      //  5:  5.5           DROPPED   2
      //                    6.3: 3 departs
      //  6:  6.6           6.6-12.6  1
      //  7:  7.7           DROPPED   2
      //                    8.4: 4 departs
      //  8:  8.8           8.8-16.8  1
      //  9:  9.9           DROPPED   2
      // 10: 11.0           DROPPED   2
      //                    12.6: 6 departs
      //                    16.8  8 departs
      //              
      el.run ();
      assert el.isEmpty ();
      assertEquals (16.8, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n + j.n * 0.1, j.arrivalTime, 0.0);
        boolean dropped = j.n == 5 || j.n == 7 || j.n == 9 || j.n == 10;
        if (dropped)
        {
          assert j.dropped;
          assertEquals (j.arrivalTime, j.dropTime, 0.0);
          assert ! j.started;
          assert ! j.departed;
        }
        else
        {
          assert ! j.dropped;
          assert j.started;
          assertEquals (j.arrivalTime, j.startTime, 0.0);
          assert j.departed;
          assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
        }
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  /**
   * Test of LCFS.
   * 
   */
  @Test
  public void testLCFS ()
  {
    System.out.println ("====");
    System.out.println ("LCFS");
    System.out.println ("====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final LCFS queue = new LCFS (el);
    final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
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
   * Test of RANDOM.
   * 
   */
  @Test
  public void testRANDOM ()
  {
    System.out.println ("======");
    System.out.println ("RANDOM");
    System.out.println ("======");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final RANDOM queue = new RANDOM (el);
    final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
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

  private List<TestJob1> scheduleJobArrivalsSJF (final boolean reported, final int n, final SimEventList eventList, final AbstractNonPreemptiveSingleServerSimQueue queue)
  {
    // Job 1 is scheduled at t = 1, req service time = 1.
    final TestJob1 j1 = new TestJob1 (reported, 1);
    eventList.add (new SimEvent ("ARRIVAL_" + 1, 1.0, null, new SimEventAction ()
    {
      @Override
      public void action (final SimEvent event)
      {
        queue.arrive (j1, 1.0);
      }
    }));
    final List<TestJob1> jobList = new ArrayList<>  ();
    for (int i = 2; i <= n; i++)
    {
      final TestJob1 j = new TestJob1 (reported, i);
      jobList.add (j);
    }
    Collections.shuffle (jobList);
    for (int i = 0; i < (n - 1); i++)
    {
      final TestJob1 j = jobList.get (i);
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
   * Test of SJF.
   * 
   */
  @Test
  public void testSJF ()
  {
    System.out.println ("===");
    System.out.println ("SJF");
    System.out.println ("===");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final SJF queue = new SJF (el);
    final List<TestJob1> jobs = scheduleJobArrivalsSJF (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
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

  private List<TestJob1> scheduleJobArrivalsLJF (final boolean reported, final int n, final SimEventList eventList, final AbstractNonPreemptiveSingleServerSimQueue queue)
  {
    return scheduleJobArrivalsSJF (reported, n, eventList, queue);
  }
  
  /**
   * Test of LJF.
   * 
   */
  @Test
  public void testLJF ()
  {
    System.out.println ("===");
    System.out.println ("LJF");
    System.out.println ("===");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final LJF queue = new LJF (el);
    final List<TestJob1> jobs = scheduleJobArrivalsLJF (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (56.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
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
   * Test of IS.
   * 
   */
  @Test
  public void testIS ()
  {
    System.out.println ("==");
    System.out.println ("IS");
    System.out.println ("==");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final IS queue = new IS (el);
    final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (20.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
    {
      assert j.arrived;
      assertEquals ((double) j.n, j.arrivalTime, 0.0);
      assert j.started;
      assertEquals ((double) j.n, j.startTime, 0.0);
      assert j.departed;
      assertEquals (j.startTime + (double) j.n, j.departureTime, 0.0);
    }
  }

  /**
   * Test of IS_CST.
   * 
   */
  @Test
  public void testIS_CST ()
  {
    System.out.println ("======");
    System.out.println ("IS_CST");
    System.out.println ("======");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final IS_CST queue = new IS_CST (el, 4.0);
    final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (14.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
    {
      assert j.arrived;
      assertEquals ((double) j.n, j.arrivalTime, 0.0);
      assert j.started;
      assertEquals ((double) j.n, j.startTime, 0.0);
      assert j.departed;
      assertEquals (j.startTime + 4.0, j.departureTime, 0.0);
    }
  }

  /**
   * Test of IC.
   * 
   */
  @Test
  public void testIC ()
  {
    System.out.println ("==");
    System.out.println ("IC");
    System.out.println ("==");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final IC queue = new IC (el);
    final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
    el.run ();
    assert el.isEmpty ();
    assertEquals (10.0, el.getTime (), 0.0);
    for (TestJob1 j : jobs)
    {
      assert j.arrived;
      assertEquals ((double) j.n, j.arrivalTime, 0.0);
      assert j.started;
      assertEquals (j.startTime, j.arrivalTime, 0.0);
      assert j.departed;
      assertEquals (j.departureTime, j.startTime, 0.0);
    }
  }

  private void scheduleQueueAccessVacation (SimEventList el, final SimQueue queue, double startTime, final double duration)
  {
    el.add (new SimEvent (startTime, null, new SimEventAction ()
    {

      @Override
      public void action (SimEvent event)
      {
        queue.startQueueAccessVacation (duration);
      }
    }));
  }
  
  /**
   * Test of queue-access vacation.
   * 
   */
  @Test
  public void testQueueAccessVacation ()
  {
    System.out.println ("======================");
    System.out.println ("Queue Access Vacation ");
    System.out.println ("======================");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final FCFS queue = new FCFS (el);
    queue.registerQueueListener (new DefaultSimQueueVacationListener<SimJob, SimQueue> ()
    {

      @Override
      public void notifyStartQueueAccessVacation (double t, SimQueue queue)
      {
        System.out.println ("t = " + t + ": Queue " + queue + " STARTS queue-access vacation!");
      }
      
      @Override
      public void notifyStopQueueAccessVacation (double t, SimQueue queue)
      {
        System.out.println ("t = " + t + ": Queue " + queue + " ENDS queue-access vacation!");
      }

    });
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
      // Schedule vacation from 1.5 to 5.5.
      // Jobs 2, 3, 4, and 5 should be dropped.
      // Job 1 should depart at t = 2.
      // Job 6 starts at t = 6.
      //     7               12
      //     8               19
      //     9               27
      //     10              36
      // Job 10 ends at t = 46.
      scheduleQueueAccessVacation (el, queue, 1.5, 4.0);
      el.run ();
      assert el.isEmpty ();
      assertEquals (46.0, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        if (j.n == 1)
        {
          assert ! j.dropped;
          assert j.started;
          assert j.departed;          
          assertEquals (j.departureTime, j.arrivalTime + (double) j.n, 0.0);
        }
        else if (j.n >= 2 && j.n <= 5)
        {
          assert j.dropped;
          assert ! j.started;
          assert ! j.departed;
          assertEquals (j.dropTime, j.arrivalTime, 0.0);
        }
        else
        {
          assert ! j.dropped;
          assert j.started;
          assert j.departed;
        }
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  private void scheduleServerAccessCredits (SimEventList el, final SimQueue queue, double time, final int credits)
  {
    el.add (new SimEvent (time, null, new SimEventAction ()
    {

      @Override
      public void action (SimEvent event)
      {
        queue.setServerAccessCredits (credits);
      }
    }));
  }
  
  /**
   * Test of server-access credits
   * 
   */
  @Test
  public void testServerAccessCredits ()
  {
    System.out.println ("======================");
    System.out.println ("Server Access Credits ");
    System.out.println ("======================");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final FCFS queue = new FCFS (el);
    queue.registerQueueListener (new DefaultSimQueueVacationListener<SimJob, SimQueue> ()
    {

      @Override
      public void notifyRegainedServerAccessCredits (double t, SimQueue queue)
      {
        System.out.println ("t = " + t + ": Queue " + queue + " REGAINED server-access credits!");
      }

      @Override
      public void notifyOutOfServerAccessCredits (double t, SimQueue queue)
      {
        System.out.println ("t = " + t + ": Queue " + queue + " OUT OF server-access credits!");
      }

    });
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
      // At t = 1.5, set remaining credits to 2.
      // Job 1 starts at t=1, leaves at t=2.
      // Job 2             2              4
      // Job 3             4              7
      // No more server credits until t=10.5 -> 4 additional credits.
      // Job 4            10.5            14.5
      // Job 5            14.5            19.5
      // Job 6            19.5            25.5
      // Job 7            25.5            32.5
      // No more credits until t=100 -> 2 more credits.
      // Job 8            100             108
      // Job 9            108             117
      // No credits for job 10 -> does not start/depart.
      scheduleServerAccessCredits (el, queue, 1.5, 2);
      scheduleServerAccessCredits (el, queue, 10.5, 4);
      scheduleServerAccessCredits (el, queue, 100, 2);      
      el.run ();
      assert el.isEmpty ();
      assertEquals (117.0, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
      {
        assert j.arrived;
        assert ! j.dropped;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        if (j.n != 10)
        {
          assert j.started;
          assert j.departed;
        }
        else
        {
          assert ! j.started;
          assert ! j.departed;
        }
        if (j.n == 1)      assertEquals (j.departureTime, 2.0, 0.0);
        else if (j.n == 2) assertEquals (j.departureTime, 4.0, 0.0);
        else if (j.n == 3) assertEquals (j.departureTime, 7.0, 0.0);
        else if (j.n == 4) assertEquals (j.departureTime, 14.5, 0.0);
        else if (j.n == 5) assertEquals (j.departureTime, 19.5, 0.0);
        else if (j.n == 6) assertEquals (j.departureTime, 25.5, 0.0);
        else if (j.n == 7) assertEquals (j.departureTime, 32.5, 0.0);
        else if (j.n == 8) assertEquals (j.departureTime, 108.0, 0.0);
        else if (j.n == 9) assertEquals (j.departureTime, 117.0, 0.0);
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

  
}
