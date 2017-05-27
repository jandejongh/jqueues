package nl.jdj.jqueues.r5.misc.book.book_11_statistics;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.util.stat.AutoSimQueueStat;
import nl.jdj.jqueues.r5.util.stat.AutoSimQueueStatEntry;
import nl.jdj.jqueues.r5.util.stat.SimQueueProbe;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class Statistics_150_AutoSimQueueStat
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
    final SimQueueProbe probeJ  =
      (SimQueueProbe) (SimQueue queue) -> queue.getNumberOfJobs ();
    final SimQueueProbe probeJX =
      (SimQueueProbe) (SimQueue queue) -> queue.getNumberOfJobsInServiceArea ();
    final List<AutoSimQueueStatEntry> entries_fcfs = new ArrayList<> ();
    final List<AutoSimQueueStatEntry> entries_lcfs = new ArrayList<> ();
    entries_fcfs.add (new AutoSimQueueStatEntry ("J",  probeJ));
    entries_fcfs.add (new AutoSimQueueStatEntry ("JX", probeJX));
    entries_lcfs.add (new AutoSimQueueStatEntry ("J",  probeJ));
    entries_lcfs.add (new AutoSimQueueStatEntry ("JX", probeJX));
    final AutoSimQueueStat stat_fcfs = new AutoSimQueueStat (fcfs, entries_fcfs);
    final AutoSimQueueStat stat_lcfs = new AutoSimQueueStat (lcfs, entries_lcfs);
    el.run ();
    System.out.println ("FCFS:");
    stat_fcfs.report (2);
    System.out.println ("P_LCFS:");
    stat_lcfs.report (2);
  }
  
}
