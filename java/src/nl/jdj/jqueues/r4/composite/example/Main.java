package nl.jdj.jqueues.r4.composite.example;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.NonPreemptiveQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.composite.BlackParallelSimQueues;
import nl.jdj.jqueues.r4.composite.BlackTandemSimQueue;
import nl.jdj.jqueues.r4.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r4.composite.SimQueueSelector;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** Example code for <code>nl.jdj.jqueues.composite</code>.
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
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.composite PACKAGE ===");
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
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue2 = new NonPreemptiveQueue.FIFO (el);
    System.out.println ("-> Creating LCFS queue...");
    final SimQueue lcfsQueue2 = new NonPreemptiveQueue.LIFO (el);
    System.out.println ("-> Creating Parallel queue...");
    final Set<SimQueue> set2 = new LinkedHashSet<> ();
    set2.add (fcfsQueue2);
    set2.add (lcfsQueue2);
    final SimQueue parallelQueue = new BlackParallelSimQueues (el, set2, delegateSimJobFactory, new SimQueueSelector<SimJob, SimJob, SimQueue> ()
    {
      boolean toSecond = false;
      @Override
      public SimQueue<SimJob, SimQueue> selectFirstQueue (double time, SimJob job)
      {
        final SimQueue dstQueue;
        if (toSecond)
          dstQueue = lcfsQueue2;
        else
          dstQueue = fcfsQueue2;
        toSecond = ! toSecond;
        return dstQueue;
      }
      @Override
      public SimQueue<SimJob, SimQueue> selectNextQueue (double time, SimJob job, SimQueue previousQueue)
      {
        return null;
      }
    });
    System.out.println ("-> Submitting jobs to Parallel queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          parallelQueue.arrive (j, arrTime);
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