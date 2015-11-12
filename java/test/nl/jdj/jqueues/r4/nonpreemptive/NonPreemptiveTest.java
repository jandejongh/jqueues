package nl.jdj.jqueues.r4.nonpreemptive;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.TestJob1;
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

}
