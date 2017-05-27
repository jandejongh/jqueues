package nl.jdj.jqueues.r5.misc.book.book_11_statistics;

import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.util.stat.SimpleSimQueueVisitsStat;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class Statistics_160_SimpleSimQueueVisitsStat
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
    // avgWaitJ = 0 + 0 + 0 + 1 + 3 / 5 = 0.8.
    // avgSojJ  = 0 + 1 + 2 + 4 + 7 / 5 = 14 / 5 = 2.8.
    //
    // WITH P_LCFS: AMBIGUITY!
    //
    // Departure times with P_LCFS [2 arrives after departure of 1]: 0, 2, 8, 10, 11.
    // cumJ = 0 + 1 + 1 + 2 + 3 + 3 + 3 + 3 + 2 + 2 + 1 = 21.
    // avgJ = 21 / 11 = 1.9090.
    // avgWaitJ = 0.
    // avgSojJ  = 0 + 1 + 9 + 7 + 4 / 5 = 21 / 5 = 4.2.
    //
    // Departure times with P_LCFS [2 arrives before departure of 1]: 0, 8, 10, 11, 11.
    // cumJ = 0 + 1 + 2 + 3 + 4 + 4 + 4 + 4 + 3 + 3 + 2 = 30.
    // avgJ = 30 / 11 = 2.7272.    
    // avgWaitJ = 0.
    // avgSojJ  = 0 + 10 + 9 + 7 + 4 / 5 = 30 / 5 = 6.0.
    //
    final FCFS fcfs = new FCFS (el);
    final P_LCFS lcfs = new P_LCFS (el, null);
    for (int j = 0; j <= 4; j++)
    {
      fcfs.scheduleJobArrival (j, new DefaultSimJob (el, "JobF " + j, j));
      lcfs.scheduleJobArrival (j, new DefaultSimJob (el, "JobL " + j, j));
    }
    final SimpleSimQueueVisitsStat visitsStatListener_fcfs = new SimpleSimQueueVisitsStat (fcfs);
    final SimpleSimQueueVisitsStat visitsStatListener_lcfs = new SimpleSimQueueVisitsStat (lcfs);
    el.run ();
    System.out.println ("FCFS:");
    System.out.println ("  Start Time            : "
      + visitsStatListener_fcfs.getStartTime ()           + ".");
    System.out.println ("  End Time              : "
      + visitsStatListener_fcfs.getLastUpdateTime ()      + ".");
    System.out.println ("  Number of Arrivals    : "
      + visitsStatListener_fcfs.getNumberOfArrivals ()    + ".");
    System.out.println ("  Number of Started Jobs: "
      + visitsStatListener_fcfs.getNumberOfStartedJobs () + ".");
    System.out.println ("  Number of Departures  : "
      + visitsStatListener_fcfs.getNumberOfDepartures ()  + ".");
    System.out.println ("  Minimum Waiting Time  : "
      + visitsStatListener_fcfs.getMinWaitingTime ()      + ".");
    System.out.println ("  Maximum Waiting Time  : "
      + visitsStatListener_fcfs.getMaxWaitingTime ()      + ".");
    System.out.println ("  Average Waiting Time  : "
      + visitsStatListener_fcfs.getAvgWaitingTime ()      + ".");
    System.out.println ("  Minimum Sojourn Time  : "
      + visitsStatListener_fcfs.getMinSojournTime ()      + ".");
    System.out.println ("  Maximum Sojourn Time  : "
      + visitsStatListener_fcfs.getMaxSojournTime ()      + ".");
    System.out.println ("  Average Sojourn Time  : "
      + visitsStatListener_fcfs.getAvgSojournTime ()      + ".");
    System.out.println ("P_LCFS:");
    System.out.println ("  Start Time            : "
      + visitsStatListener_lcfs.getStartTime ()           + ".");
    System.out.println ("  End Time              : "
      + visitsStatListener_lcfs.getLastUpdateTime ()      + ".");
    System.out.println ("  Number of Arrivals    : "
      + visitsStatListener_lcfs.getNumberOfArrivals ()    + ".");
    System.out.println ("  Number of Started Jobs: "
      + visitsStatListener_lcfs.getNumberOfStartedJobs () + ".");
    System.out.println ("  Number of Departures  : "
      + visitsStatListener_lcfs.getNumberOfDepartures ()  + ".");
    System.out.println ("  Minimum Waiting Time  : "
      + visitsStatListener_lcfs.getMinWaitingTime ()      + ".");
    System.out.println ("  Maximum Waiting Time  : "
      + visitsStatListener_lcfs.getMaxWaitingTime ()      + ".");
    System.out.println ("  Average Waiting Time  : "
      + visitsStatListener_lcfs.getAvgWaitingTime ()      + ".");
    System.out.println ("  Minimum Sojourn Time  : "
      + visitsStatListener_lcfs.getMinSojournTime ()      + ".");
    System.out.println ("  Maximum Sojourn Time  : "
      + visitsStatListener_lcfs.getMaxSojournTime ()      + ".");
    System.out.println ("  Average Sojourn Time  : "
      + visitsStatListener_lcfs.getAvgSojournTime ()      + ".");
  }
  
}
