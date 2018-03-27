package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.DELAY;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.DROP;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.GATE;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.SINK;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.WUR;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.ZERO;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for <code>nl.jdj.jqueues.serverless</code>.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
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
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    System.out.println ("-> Creating SINK queue...");
    final SimQueue sinkQueue = new SINK (el);
    System.out.println ("-> Submitting jobs to SINK ('Hotel California') queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        sinkQueue.arrive (arrTime, j);
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
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        delayQueue.arrive (arrTime, j);
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
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        delayQueue2.arrive (arrTime, j);
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
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        zeroQueue.arrive (arrTime, j);
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
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        dropQueue.arrive (arrTime, j);
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
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        gateQueue.arrive (arrTime, j);
      }));
    }
    // Close gate between t = 2.5 and t = 3.5.
    el.schedule (2.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).setGatePassageCredits (event.getTime (), 0);
    });
    el.schedule (3.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).setGatePassageCredits (event.getTime (), Integer.MAX_VALUE);
    });
    // Open gate for two jobs at t=5.5.
    el.schedule (5.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).setGatePassageCredits (event.getTime (), 2);
    });
    // Open gate t=11.5.    
    el.schedule (11.5, (SimEventAction) (SimEvent event) ->
    {
      ((GATE) gateQueue).setGatePassageCredits (event.getTime (), Integer.MAX_VALUE);
    });
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating WUR queue...");
    final SimQueue wurQueue = new WUR (el);
    ((AbstractSimQueue) wurQueue).registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to WUR queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        wurQueue.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}