package nl.jdj.jqueues.r4.serverless;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.DefaultSimQueueVacationListener;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
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
    
    public boolean dropped = false;
    
    public boolean departed = false;
    
    public double arrivalTime = 0.0;
    
    public double startTime = 0.0;
    
    public double dropTime = 0.0;
    
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
        if (TestJob.this.departed)
          fail ("Starting after departure!");
        if (TestJob.this.dropped)
          fail ("Starting after drop!");
        TestJob.this.started = true;
        TestJob.this.startTime = event.getTime ();
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueStartAction ()
    {
      return this.QUEUE_START_ACTION;
    }

    public final SimEventAction<SimJob> QUEUE_DROP_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " dropped.");      
        if (! TestJob.this.arrived)
          fail ("Dropped before arrival!");
        if (TestJob.this.departed)
          fail ("Dropped after departure!");
        TestJob.this.dropped = true;
        TestJob.this.dropTime = event.getTime ();
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueDropAction ()
    {
      return this.QUEUE_DROP_ACTION;
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
        // The following check is an error; jobs may depart without receiving any service at all!
        // if (! TestJob.this.started)
        //  fail ("Departure before start!");
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
  
  public static List<TestJob> scheduleJobArrivals
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
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
   * Test of NONE.
   * 
   */
  @Test
  public void testNONE ()
  {
    System.out.println ("====");
    System.out.println ("NONE");
    System.out.println ("====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final NONE queue = new NONE (el);
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

  /**
   * Test of DELAY.
   * 
   */
  @Test
  public void testDELAY ()
  {
    System.out.println ("=====");
    System.out.println ("DELAY");
    System.out.println ("=====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final double waitTime = 5.0;
    final DELAY queue = new DELAY (el, waitTime);
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
      el.run ();
      assert el.isEmpty ();
      assertEquals (15.0, el.getTime (), 0.0);
      for (TestJob j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        assert ! j.started;
        assert j.departed;
        assertEquals (j.arrivalTime + waitTime, j.departureTime, 0.0);
      }
      // Test reset on the fly...
      el.reset ();
    }
  }
  
  /**
   * Test of ZERO.
   * 
   */
  @Test
  public void testZERO ()
  {
    System.out.println ("====");
    System.out.println ("ZERO");
    System.out.println ("====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final ZERO queue = new ZERO (el);
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
        assert j.departed;
        assertEquals (j.arrivalTime, j.departureTime, 0.0);
      }
      // Test reset on the fly...
      el.reset ();
    }
  }
  
  /**
   * Test of DROP.
   * 
   */
  @Test
  public void testDROP ()
  {
    System.out.println ("====");
    System.out.println ("DROP");
    System.out.println ("====");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final DROP queue = new DROP (el);
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
        assert j.dropped;
        assertEquals (j.arrivalTime, j.dropTime, 0.0);
        assert ! j.started;
        assert ! j.departed;
      }
      // Test reset on the fly...
      el.reset ();
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
    final NONE queue = new NONE (el);
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
      final List<TestJob> jobs = scheduleJobArrivals (true, 10, el, queue);
      // Schedule vacation from 1.5 to 5.5.
      // Jobs 2, 3, 4, and 5 should be dropped.
      // The other jobs should remain in the system, but may not start nor depart.
      scheduleQueueAccessVacation (el, queue, 1.5, 4.0);
      el.run ();
      assert el.isEmpty ();
      assertEquals (10.0, el.getTime (), 0.0);
      for (TestJob j : jobs)
      {
        assert j.arrived;
        assertEquals ((double) j.n, j.arrivalTime, 0.0);
        if (j.n == 1)
        {
          assert ! j.dropped;
          assert ! j.started;
          assert ! j.departed;
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
          assert ! j.started;
          assert ! j.departed;
        }
      }
      // Test reset on the fly...
      el.reset ();
    }
  }

}
