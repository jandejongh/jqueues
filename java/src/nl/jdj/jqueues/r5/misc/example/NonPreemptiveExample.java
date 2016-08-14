package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS_CST;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LJF;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.RANDOM;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.SJF;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.SUR;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for <code>nonpreemptive</code> package.
 * 
 */
public final class NonPreemptiveExample
{
  
  /** Prevents instantiation.
   * 
   */
  private NonPreemptiveExample ()
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
    System.out.println ("=== EXAMPLE PROGRAM FOR nonpreemptive PACKAGE ===");
    System.out.println ("-> Creating jobs...");
    final List<SimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      // If you want job-centered reports, use the line below (instead of the line after that).
      // jobList.add (new DefaultExampleSimJob<> (true, n));
      jobList.add (new DefaultExampleSimJob<> (false, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new DefaultSimEventList<> (SimEvent.class);
    System.out.println ("-> Creating FCFS queue...");
    final AbstractSimQueue fcfsQueue = new FCFS (el);
    System.out.println ("-> Registering queue listener...");
    fcfsQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to FCFS queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      // Note: it is tempting to directly use fcfsQueue.arrive () here, but that would immediately
      // trigger the queue, whereas that is supposed to happen from the event list.
      // Hence, for initial job arrivals (i.e., before the evant list is being processed), one should
      // always schedule appropriate events on the event list.
      // Below is just one way of doing this.
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        fcfsQueue.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating FCFS_B queue with buffer size 2...");
    // See below, we now declare the queue as an AbstractSimQueue, which gives us more (scheduling) options.
    final AbstractSimQueue fcfs_bQueue = new FCFS_B (el, 2);
    System.out.println ("-> Registering queue listener...");
    fcfs_bQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to FCFS_B queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      // Here is an easier to way to schedule job arrivals to a SimQueue.
      // Note that this only works if the SimQueue is an AbstractSimQueue.
      fcfs_bQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating FCFS_c queue with 3 servers...");
    final AbstractSimQueue fcfs_cQueue = new FCFS_c (el, 3);
    System.out.println ("-> Registering queue listener...");
    fcfs_cQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to FCFS_c queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      fcfs_cQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating NoBuffer_c queue with 3 servers...");
    final AbstractSimQueue noBuffer_cQueue = new NoBuffer_c (el, 3);
    System.out.println ("-> Registering queue listener...");
    noBuffer_cQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to NoBuffer_c queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      noBuffer_cQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating LCFS queue...");
    final AbstractSimQueue lcfsQueue = new LCFS (el);
    System.out.println ("-> Registering queue listener...");
    lcfsQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to LCFS queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      lcfsQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating IS queue...");
    final AbstractSimQueue isQueue = new IS (el);
    System.out.println ("-> Registering queue listener...");
    isQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to IS queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      isQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating IS_CST queue with service time 4...");
    final AbstractSimQueue is_cstQueue = new IS_CST (el, 4.0);
    System.out.println ("-> Registering queue listener...");
    is_cstQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to IS_CST queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      is_cstQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating IC queue...");
    final AbstractSimQueue icQueue = new IC (el);
    System.out.println ("-> Registering queue listener...");
    icQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to IC queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      icQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating RANDOM queue...");
    final AbstractSimQueue randomQueue = new RANDOM (el);
    System.out.println ("-> Registering queue listener...");
    randomQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to RANDOM queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      randomQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating SJF queue...");
    final AbstractSimQueue sjfQueue = new SJF (el);
    System.out.println ("-> Registering queue listener...");
    sjfQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to SJF queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = jobList.size () - i;
      sjfQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    el.reset ();
    System.out.println ("-> Creating LJF queue...");
    final AbstractSimQueue ljfQueue = new LJF (el);
    System.out.println ("-> Registering queue listener...");
    ljfQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to LJF queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      ljfQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    el.reset ();
    System.out.println ("-> Creating SUR queue...");
    final AbstractSimQueue surQueue = new SUR (el);
    System.out.println ("-> Registering queue listener...");
    surQueue.registerStdOutSimQueueListener ();
    System.out.println ("-> Submitting jobs to SUR queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      surQueue.scheduleJobArrival (arrTime, j);
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("=== FINISHED ===");
  }
  
}