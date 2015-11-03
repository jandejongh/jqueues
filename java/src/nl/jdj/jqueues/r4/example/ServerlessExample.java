package nl.jdj.jqueues.r4.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.serverless.DELAY;
import nl.jdj.jqueues.r4.serverless.DROP;
import nl.jdj.jqueues.r4.serverless.GATE;
import nl.jdj.jqueues.r4.serverless.SINK;
import nl.jdj.jqueues.r4.serverless.ZERO;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** Example code for <code>nl.jdj.jqueues.serverless</code>.
 * 
 */
public final class ServerlessExample
{
  
  /** Prevents instantiation.
   * 
   */
  private ServerlessExample ()
  {
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
    final List<DefaultExampleSimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new DefaultExampleSimJob (true, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    System.out.println ("-> Creating SINK queue...");
    final SimQueue noneQueue = new SINK (el);
    System.out.println ("-> Submitting jobs to SINK ('Hotel California') queue...");
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
    System.out.println ("-> Creating DROP queue...");
    final SimQueue dropQueue = new DROP (el);
    System.out.println ("-> Submitting jobs to DROP queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          dropQueue.arrive (j, arrTime);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating GATE queue...");
    final SimQueue gateQueue = new GATE (el);
    System.out.println ("-> Submitting jobs to GATE queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          gateQueue.arrive (j, arrTime);
        }
      }));
    }
    // Close gate between t = 2.5 and t = 3.5.
    el.schedule (2.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).closeGate (event.getTime ());
    });
    el.schedule (3.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).openGate (event.getTime ());
    });
    // Open gate for two jobs at t=5.5.
    el.schedule (5.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).openGate (event.getTime (), 2);
    });
    // Open gate t=11.5.    
    el.schedule (11.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).openGate (event.getTime ());
    });
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}