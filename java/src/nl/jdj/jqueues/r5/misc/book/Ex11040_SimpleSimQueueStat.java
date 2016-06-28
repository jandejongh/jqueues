package nl.jdj.jqueues.r5.misc.book;

import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.util.stat.SimpleSimQueueStat;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class Ex11040_SimpleSimQueueStat
{

  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    el.reset (0);
    // Schedule 5 jobs, 0 through 4 at t = 0, 1, 2, 3, 4
    // and service times 0, 1, 2, 3, 4, respectively.
    //
    // Departure times with FCFS: 0, 2, 4, 7, 11.
    // cumJ = 0 + 1 + 1 + 2 + 2 + 2 + 2 + 1 + 1 + 1 + 1 = 14.
    // avgJ = 14 / 11 = 1.2727.
    //
    // WITH P_LCFS: AMBIGUITY!
    //
    // Departure times with P_LCFS [2 arrives after departure of 1]: 0, 2, 8, 10, 11.
    // cumJ = 0 + 1 + 1 + 2 + 3 + 3 + 3 + 3 + 2 + 2 + 1 = 21.
    // avgJ = 21 / 11 = 1.9090.
    //
    // Departure times with P_LCFS [2 arrives before departure of 1]: 0, 8, 10, 11, 11.
    // cumJ = 0 + 1 + 2 + 3 + 4 + 4 + 4 + 4 + 3 + 3 + 2 = 30.
    // avgJ = 30 / 11 = 2.7272.    
    //
    final FCFS fcfs = new FCFS (el);
    final P_LCFS lcfs = new P_LCFS (el, null);
    for (int j = 0; j <= 4; j++)
    {
      fcfs.scheduleJobArrival (j, new DefaultSimJob (el, "JobF " + j, j));
      lcfs.scheduleJobArrival (j, new DefaultSimJob (el, "JobL " + j, j));
    }
    final SimpleSimQueueStat avgJStatListener_fcfs = new SimpleSimQueueStat (fcfs);
    final SimpleSimQueueStat avgJStatListener_lcfs = new SimpleSimQueueStat (lcfs);
    el.run ();
    System.out.println ("FCFS:");
    System.out.println ("  Start time: " + avgJStatListener_fcfs.getStartTime () + ".");
    System.out.println ("  End Time  : " + avgJStatListener_fcfs.getLastUpdateTime () + ".");
    System.out.println ("  Number of updates                     : "
      + avgJStatListener_fcfs.getNumberOfUpdates () + ".");
    System.out.println ("  Average number of jobs                : "
      + avgJStatListener_fcfs.getAvgNrOfJobs () + ".");
    System.out.println ("  Average number of jobs in service area: "
      + avgJStatListener_fcfs.getAvgNrOfJobsInServiceArea () + ".");
    System.out.println ("  Maximum number of jobs                : "
      + avgJStatListener_fcfs.getMaxNrOfJobs () + ".");
    System.out.println ("P_LCFS:");
    System.out.println ("  Start time: " + avgJStatListener_lcfs.getStartTime () + ".");
    System.out.println ("  End Time  : " + avgJStatListener_lcfs.getLastUpdateTime () + ".");
    System.out.println ("  Number of updates                     : "
      + avgJStatListener_fcfs.getNumberOfUpdates () + ".");
    System.out.println ("  Average number of jobs                : "
      + avgJStatListener_lcfs.getAvgNrOfJobs () + ".");
    System.out.println ("  Average number of jobs in service area: "
      + avgJStatListener_lcfs.getAvgNrOfJobsInServiceArea ()+ ".");
    System.out.println ("  Maximum number of jobs                : "
      + avgJStatListener_lcfs.getMaxNrOfJobs () + ".");
  }
  
}
