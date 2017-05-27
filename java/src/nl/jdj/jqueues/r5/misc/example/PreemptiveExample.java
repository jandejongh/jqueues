package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.SRTF;
import nl.jdj.jqueues.r5.listener.StdOutSimEntityListener;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for <code>nl.jdj.jqueues.nonpreemptive</code>.
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
public final class PreemptiveExample
{
  
  /** Prevents instantiation.
   * 
   */
  private PreemptiveExample ()
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
    System.out.println ("=== EXAMPLE PROGRAM FOR preemptive PACKAGE ===");
    System.out.println ("-> Creating jobs...");
    final List<SimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new DefaultExampleSimJob<> (false, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new DefaultSimEventList<> (SimEvent.class);
    System.out.println ("-> Creating P_LCFS queue (Preemptive/Drop) and registering System.out listener...");
    final SimQueue p_lcfsQueue_drop = new P_LCFS (el, PreemptionStrategy.DROP);
    p_lcfsQueue_drop.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to P_LCFS (Preemptive/Drop) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        p_lcfsQueue_drop.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating P_LCFS queue (Preemptive/Resume) and registering System.out listener...");
    final SimQueue p_lcfsQueue_resume = new P_LCFS (el, PreemptionStrategy.RESUME);
    p_lcfsQueue_resume.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to P_LCFS (Preemptive/Resume) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        p_lcfsQueue_resume.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating P_LCFS queue (Preemptive/Restart) and registering System.out listener...");
    final SimQueue p_lcfsQueue_restart = new P_LCFS (el, PreemptionStrategy.RESTART);
    p_lcfsQueue_restart.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to P_LCFS (Preemptive/Restart) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        p_lcfsQueue_restart.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating P_LCFS queue (Preemptive/Depart) and registering System.out listener...");
    final SimQueue p_lcfsQueue_depart = new P_LCFS (el, PreemptionStrategy.DEPART);
    p_lcfsQueue_depart.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to P_LCFS (Preemptive/Depart) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        p_lcfsQueue_depart.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating SRTF queue (Preemptive/Drop) and registering System.out listener...");
    final SimQueue srtfQueue_drop = new SRTF (el, PreemptionStrategy.DROP);
    srtfQueue_drop.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to SRTF (Preemptive/Drop) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = jobList.size () - 0.8 * i;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, arrTime, null, (SimEventAction) (final SimEvent event) ->
      {
        srtfQueue_drop.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating SRTF queue (Preemptive/Resume) and registering System.out listener...");
    final SimQueue srtfQueue_resume = new SRTF (el, PreemptionStrategy.RESUME);
    srtfQueue_resume.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to SRTF (Preemptive/Resume) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = jobList.size () - 0.8 * i;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, arrTime, null, (SimEventAction) (final SimEvent event) ->
      {
        srtfQueue_resume.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating SRTF queue (Preemptive/Restart) and registering System.out listener...");
    final SimQueue srtfQueue_restart = new SRTF (el, PreemptionStrategy.RESTART);
    srtfQueue_restart.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to SRTF (Preemptive/Restart) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = jobList.size () - 0.8 * i;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, arrTime, null, (SimEventAction) (final SimEvent event) ->
      {
        srtfQueue_restart.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating SRTF queue (Preemptive/Depart) and registering System.out listener...");
    final SimQueue srtfQueue_depart = new SRTF (el, PreemptionStrategy.DEPART);
    srtfQueue_depart.registerSimEntityListener (new StdOutSimEntityListener<> ());
    System.out.println ("-> Submitting jobs to SRTF (Preemptive/Depart) queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = jobList.size () - 0.8 * i;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, arrTime, null, (SimEventAction) (final SimEvent event) ->
      {
        srtfQueue_depart.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}