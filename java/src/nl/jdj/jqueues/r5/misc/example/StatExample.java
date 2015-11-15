package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.util.stat.AutoSimQueueStat;
import nl.jdj.jqueues.r5.util.stat.AutoSimQueueStatEntry;
import nl.jdj.jqueues.r5.util.stat.SimQueueProbe;
import nl.jdj.jqueues.r5.util.stat.SimpleSimQueueStat;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for <code>nl.jdj.jqueues.stat</code>.
 * 
 */
public final class StatExample
{
  
  /** Prevents instantiation.
   * 
   */
  private StatExample ()
  {
  }
  
  private static class MyAutoSimQueueStat
  extends AutoSimQueueStat<SimJob, SimQueue>
  {
    
    private static List<AutoSimQueueStatEntry<SimQueue>> createEntries ()
    {
      List<AutoSimQueueStatEntry<SimQueue>> list = new ArrayList<> ();
      list.add (new AutoSimQueueStatEntry<> ("number of jobs", new SimQueueProbe<SimQueue>  ()
      {

        @Override
        public double get (SimQueue queue)
        {
          return queue.getNumberOfJobs ();
        }
      }));
      list.add (new AutoSimQueueStatEntry<> ("number of jobs executing", new SimQueueProbe<SimQueue>  ()
      {

        @Override
        public double get (SimQueue queue)
        {
          return queue.getNumberOfJobsExecuting ();
        }
      }));
      return list;
    }
    
    public MyAutoSimQueueStat (SimQueue queue, double startTime)
    {
      super (queue, startTime, createEntries ());
    }
    
  }
  
  private static final SimEventList<SimEvent> EVENT_LIST = new DefaultSimEventList<> (SimEvent.class);
  
  private static final SimQueue FCFS_QUEUE = new FCFS (EVENT_LIST);
  private static final SimQueue LCFS_QUEUE = new LCFS (EVENT_LIST);
  private static final SimQueue IS_QUEUE = new IS (EVENT_LIST);
      
  /** Main method.
   * 
   * Creates a (reusable) event list, some queues and jobs and shows the main feature of the package.
   * Results are sent to {@link System#out}.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.stat PACKAGE ===");
    final int N = 100000;
    System.out.println ("-> Simulating queue with " + N + " jobs, arriving at 1, 2, 3, ...");
    System.out.println ("   Requesting service times 1, 2, 3, ...");
    System.out.println ("   Taking statistics over the busy period.");
    System.out.println ("-> FCFS...");
    final List<DefaultExampleSimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= N; n++)
      jobList.add (new DefaultExampleSimJob (false, n));
    final SimpleSimQueueStat fcfsStat = new SimpleSimQueueStat (FCFS_QUEUE, 1.0);
    final MyAutoSimQueueStat autoFcfsStat = new MyAutoSimQueueStat (FCFS_QUEUE, 1.0);
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      EVENT_LIST.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          FCFS_QUEUE.arrive (j, arrTime);
        }
      }));
    }
    EVENT_LIST.run ();
    System.out.println ("   ==> SimpleSimQueueStat");
    System.out.println ("     ==> Average number of jobs in queue:  " + fcfsStat.getAvgNrOfJobs () + ".");
    System.out.println ("     ==> Average number of jobs executing: " + fcfsStat.getAvgNrOfJobsExecuting () + ".");
    System.out.println ("   ==> AutoSimQueueStat");
    autoFcfsStat.report (9);
    System.out.println ("-> LCFS...");
    EVENT_LIST.reset ();
    final SimpleSimQueueStat lcfsStat = new SimpleSimQueueStat (LCFS_QUEUE, 1.0);
    final MyAutoSimQueueStat autoLcfsStat = new MyAutoSimQueueStat (LCFS_QUEUE, 1.0);
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      EVENT_LIST.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          LCFS_QUEUE.arrive (j, arrTime);
        }
      }));
    }
    EVENT_LIST.run ();
    System.out.println ("   ==> SimpleSimQueueStat");
    System.out.println ("     ==> Average number of jobs in queue:  " + lcfsStat.getAvgNrOfJobs () + ".");
    System.out.println ("     ==> Average number of jobs executing: " + lcfsStat.getAvgNrOfJobsExecuting () + ".");
    System.out.println ("   ==> AutoSimQueueStat");
    autoLcfsStat.report (9);
    System.out.println ("-> IS...");
    EVENT_LIST.reset ();
    final SimpleSimQueueStat isStat = new SimpleSimQueueStat (IS_QUEUE, 1.0);
    final MyAutoSimQueueStat autoIsStat = new MyAutoSimQueueStat (IS_QUEUE, 1.0);
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      EVENT_LIST.add (new SimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          IS_QUEUE.arrive (j, arrTime);
        }
      }));
    }
    EVENT_LIST.run ();
    System.out.println ("   ==> SimpleSimQueueStat");
    System.out.println ("     ==> Average number of jobs in queue:  " + isStat.getAvgNrOfJobs () + ".");
    System.out.println ("     ==> Average number of jobs executing: " + isStat.getAvgNrOfJobsExecuting () + ".");
    System.out.println ("   ==> AutoSimQueueStat");
    autoIsStat.report (9);
    System.out.println ("=== FINISHED ===");
  }
  
}