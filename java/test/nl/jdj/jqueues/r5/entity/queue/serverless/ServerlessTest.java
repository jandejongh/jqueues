package nl.jdj.jqueues.r5.entity.queue.serverless;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.TestJob1;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class ServerlessTest
{
  
  public ServerlessTest ()
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
  
  /**
   * Test of GATE.
   * 
   */
  @Test
  public void testGATE ()
  {
    System.out.println ("====");
    System.out.println ("GATE");
    System.out.println ("====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final GATE queue = new GATE (el);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals (true, 10, el, queue);
      // Close gate between t = 2.5 and t = 3.5.
      el.schedule (2.5, (SimEventAction) (SimEvent event) ->
      {
        queue.closeGate (event.getTime ());
      });
      el.schedule (3.5, (SimEventAction) (SimEvent event) ->
      {
        queue.openGate (event.getTime ());
      });
      // Open gate for two jobs at t=5.5.
      el.schedule (5.5, (SimEventAction) (SimEvent event) ->
      {
        queue.openGate (event.getTime (), 2);
      });
      // Open gate t=11.5.    
      el.schedule (11.5, (SimEventAction) (SimEvent event) ->
      {
        queue.openGate (event.getTime ());
      });
      el.run ();
      assert el.isEmpty ();
      assertEquals (11.5, el.getTime (), 0.0);
      for (TestJob1 j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        assert ! j.started;
        assert ! j.dropped;
        assert ! j.revoked;
        assert j.departed;
        if (j.n == 1 || j.n == 2)
          assertEquals (j.arrivalTime, j.departureTime, 0.0);
        else if (j.n == 3)
          assertEquals (3.5, j.departureTime, 0.0);
        else if (j.n >= 4 && j.n <= 7)
          assertEquals (j.arrivalTime, j.departureTime, 0.0);
        else
          assertEquals (11.5, j.departureTime, 0.0);
      }
      // Test reset on the fly...
      el.reset ();
    }
  }
  
}
