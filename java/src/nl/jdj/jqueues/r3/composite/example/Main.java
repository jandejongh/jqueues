package nl.jdj.jqueues.r3.composite.example;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r3.AbstractSimJob;
import nl.jdj.jqueues.r3.NonPreemptiveQueue;
import nl.jdj.jqueues.r3.SimJob;
import nl.jdj.jqueues.r3.SimQueue;
import nl.jdj.jqueues.r3.composite.BlackTandemSimQueue;
import nl.jdj.jqueues.r3.composite.DelegateSimJobFactory;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;
import sun.org.mozilla.javascript.NodeTransformer;

/** Example code for {@link nl.jdj.jqueues.r3.composite}.
 * 
 */
public final class Main
{
  
  /** Prevents instantiation.
   * 
   */
  private Main ()
  {
  }
  
  /** SimJob implementation used in the examples.
   * 
   * Each job has a public index 'n', set upon construction.
   * The requested service time for the job equals its index.
   * This is merely to create interesting examples.
   * 
   */
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
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueArriveAction ()
    {
      return this.QUEUE_ARRIVE_ACTION;
    }
    
    public final SimEventAction<SimJob> QUEUE_DEPART_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " departs.");      
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueDepartAction ()
    {
      return this.QUEUE_DEPART_ACTION;
    }

    public final SimEventAction<SimJob> QUEUE_START_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " starts.");      
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueStartAction ()
    {
      return this.QUEUE_START_ACTION;
    }

  }
  
  /** DelegateSimJob implementation used in the examples.
   * 
   */
  public static class TestDelegateSimJob extends AbstractSimJob
  {
    
    private final boolean reported;
    
    private final int n;
    
    public TestDelegateSimJob (TestJob realSimJob, boolean reported)
    {
      super ();
      this.reported = reported;
      this.n = realSimJob.n;
      if (n < 0)
        throw new IllegalArgumentException ();
    }

    @Override
    public double getServiceTime (SimQueue queue) throws IllegalArgumentException
    {
      if (queue instanceof NonPreemptiveQueue.LIFO)
        return 2 * this.n;
      else if (queue instanceof NonPreemptiveQueue.FIFO)
        return this.n;
      else
        throw new IllegalStateException ();
    }

    public final SimEventAction<SimJob> QUEUE_ARRIVE_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestDelegateSimJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": DelegateJob " + TestDelegateSimJob.this.n + " arrives.");
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueArriveAction ()
    {
      return this.QUEUE_ARRIVE_ACTION;
    }
    
    public final SimEventAction<SimJob> QUEUE_DEPART_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestDelegateSimJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": DelegateJob " + TestDelegateSimJob.this.n + " departs.");      
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueDepartAction ()
    {
      return this.QUEUE_DEPART_ACTION;
    }

    public final SimEventAction<SimJob> QUEUE_START_ACTION = new SimEventAction<SimJob> ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestDelegateSimJob.this.reported)
          System.out.println ("t = " + event.getTime () + ": DelegateJob " + TestDelegateSimJob.this.n + " starts.");      
      }
    };
    
    @Override
    public SimEventAction<SimJob> getQueueStartAction ()
    {
      return this.QUEUE_START_ACTION;
    }

  }
  
  /** Main method.
   * 
   * Creates a (reusable) event list, some queues and jobs and shows the main feature of the package.
   * Results are sent to {@link System#out}.
   * 
   * <p>
   * In order to understand the generated output for the different queue types, we note that
   * to each concrete {@link NonPreemptiveQueue}, exactly 10 jobs, numbered 1 through 10 inclusive are submitted.
   * The arrival time and the requested service time of each job are equal to the job index.
   * In other words, job 1 arrives at t=1 and requests S=1, job 2 arrives at t=2 and requests S=2, etc.
   * 
   * <p>
   * Despite the arrival scheme, all queue types except NONE and IS can actually serve all jobs until completion without
   * interruption from t=1 onwards. Since for these cases, the total requested service time is 55 (1+2+3+...+10), the simulation will end
   * at t=56 (since we "start" at t=1) having completed all jobs.
   * For NONE, jobs arrive, but never leave.
   * For IS, the only multi-server discipline implemented so far, the case is different, since IS can process multiple jobs at the
   * same time, effectively increasing its overall capacity while doing so. It is easily verified that with IS,
   * job n (arriving at t=n) leaves the queue at t=2*n. Hence, at t=20, job 10 departs and the simulation ends.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.r3.composite PACKAGE ===");
    System.out.println ("-> Creating jobs...");
    final List<TestJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new TestJob (true, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue = new NonPreemptiveQueue.FIFO (el);
    System.out.println ("-> Creating LCFS queue...");
    final SimQueue lcfsQueue = new NonPreemptiveQueue.LIFO (el);
    System.out.println ("-> Creating Tandem queue...");
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (fcfsQueue);
    set.add (lcfsQueue);
    final DelegateSimJobFactory delegateSimJobFactory =
      new DelegateSimJobFactory<TestDelegateSimJob, SimQueue, TestJob, SimQueue> ()
      {
        @Override
        public TestDelegateSimJob newInstance (double time, TestJob job)
        {
          return new TestDelegateSimJob (job, true);
        }
      };
    final SimQueue tandemQueue = new BlackTandemSimQueue (el, set, delegateSimJobFactory);
    System.out.println ("-> Submitting jobs to Tandem queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          tandemQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    // System.out.println ("-> Resetting event list...");
    // el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}