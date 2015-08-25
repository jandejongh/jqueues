package nl.jdj.jqueues.r3.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r3.AbstractSimJob;
import nl.jdj.jqueues.r3.NonPreemptiveQueue;
import nl.jdj.jqueues.r3.SimJob;
import nl.jdj.jqueues.r3.SimQueue;
import nl.jdj.jsimulation.r3.SimEvent;
import nl.jdj.jsimulation.r3.SimEventAction;
import nl.jdj.jsimulation.r3.SimEventList;

/** Example code for {@link nl.jdj.jqueues}.
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
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.r1 PACKAGE ===");
    System.out.println ("-> Creating jobs...");
    final List<TestJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new TestJob (true, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new SimEventList<> ();
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue = new NonPreemptiveQueue.FIFO (el);
    System.out.println ("-> Submitting jobs to FCFS queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      // Note: it is tempting to directly use fcfsQueue.arrive () here, but that would immediately
      // trigger the queue, whereas that is supposed to happen from the event list.
      // Hence, for initial job arrivals (i.e., before the evant list is being processed), one should
      // always schedule appropriate events on the event list.
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          fcfsQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating LCFS queue...");
    final SimQueue lcfsQueue = new NonPreemptiveQueue.LIFO (el);
    System.out.println ("-> Submitting jobs to LCFS queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          lcfsQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating IS queue...");
    final SimQueue isQueue = new NonPreemptiveQueue.IS (el);
    System.out.println ("-> Submitting jobs to IS queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          isQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating NONE queue...");
    final SimQueue noneQueue = new NonPreemptiveQueue.NONE (el);
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
    System.out.println ("-> Creating RANDOM queue...");
    final SimQueue randomQueue = new NonPreemptiveQueue.RANDOM (el);
    System.out.println ("-> Submitting jobs to RANDOM queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          randomQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating SJF queue...");
    final SimQueue sjfQueue = new NonPreemptiveQueue.SJF (el);
    System.out.println ("-> Submitting jobs to SJF queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = jobList.size () - i;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, arrTime, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          sjfQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    el.reset ();
    System.out.println ("-> Creating LJF queue...");
    final SimQueue ljfQueue = new NonPreemptiveQueue.LJF (el);
    System.out.println ("-> Submitting jobs to LJF queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, arrTime, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          ljfQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("=== FINISHED ===");
  }
  
}