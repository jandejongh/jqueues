package nl.jdj.jqueues.r4.nonpreemptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  private List<TestJob1> scheduleJobArrivalsSJF (final boolean reported,
    final int n,
    final SimEventList eventList,
    final AbstractNonPreemptiveSingleServerSimQueue queue)
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

}
