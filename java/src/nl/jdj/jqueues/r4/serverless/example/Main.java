package nl.jdj.jqueues.r4.serverless.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.serverless.DELAY;
import nl.jdj.jqueues.r4.serverless.NONE;
import nl.jdj.jqueues.r4.serverless.ZERO;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** Example code for <code>nl.jdj.jqueues.serverless</code>.
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
  
  /** Main method.
   * 
   * Creates a (reusable) event list, some queues and jobs and shows the main feature of the package.
   * Results are sent to {@link System#out}.
   * 
   * <p>
   * In order to understand the generated output for the different queue types, we note that
   * to each concrete {@link SimQueue}, exactly 10 jobs, numbered 1 through 10 inclusive are submitted.
   * The arrival time and the requested service time of each job are equal to the job index.
   * In other words, job 1 arrives at t=1 and requests S=1, job 2 arrives at t=2 and requests S=2, etc.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues PACKAGE ===");
    System.out.println ("-> Creating jobs...");
    final List<TestJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new TestJob (true, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    System.out.println ("-> Creating NONE queue...");
    final SimQueue noneQueue = new NONE (el);
    System.out.println ("-> Submitting jobs to NONE ('Hotel California') queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          noneQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating DELAY queue with fixed waiting time 4.0...");
    final SimQueue delayQueue = new DELAY (el, 4.0);
    System.out.println ("-> Submitting jobs to DELAY queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          delayQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating other DELAY queue with fixed waiting time ZERO...");
    final SimQueue delayQueue2 = new DELAY (el, 0);
    System.out.println ("-> Submitting jobs to (second) DELAY queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          delayQueue2.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating ZERO queue...");
    final SimQueue zeroQueue = new ZERO (el);
    System.out.println ("-> Submitting jobs to ZERO queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          zeroQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}