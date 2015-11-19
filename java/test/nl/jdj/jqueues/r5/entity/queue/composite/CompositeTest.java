package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.TestJob1;
import nl.jdj.jqueues.r5.entity.queue.composite.tandem.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;
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
public class CompositeTest
{
  
  public CompositeTest ()
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
      eventList.add (new DefaultSimEvent ("ARRIVAL_" + i, arrTime, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          queue.arrive (arrTime, j);
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
   * Test of BlackCompressedTandem2SimQueue (FCFS+FCFS).
   * 
   */
  @Test
  public void testBlackCompressedTandem2SimQueue_FCFS_FCFS ()
  {
    System.out.println ("==========================================");
    System.out.println ("BlackCompressedTandem2SimQueue (FCFS+FCFS)");
    System.out.println ("==========================================");
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    final BlackCompressedTandem2SimQueue queue = new BlackCompressedTandem2SimQueue (el, new FCFS (el), new FCFS (el), null);
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
   * Test of BlackCompressedTandem2SimQueue (LCFS+FCFS).
   * 
   */
  @Test
  public void testBlackCompressedTandem2SimQueue_LCFS_FCFS ()
  {
    System.out.println ("==========================================");
    System.out.println ("BlackCompressedTandem2SimQueue (LCFS+FCFS)");
    System.out.println ("==========================================");
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    final BlackCompressedTandem2SimQueue queue = new BlackCompressedTandem2SimQueue (el, new LCFS (el), new FCFS (el), null);
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
  
}
