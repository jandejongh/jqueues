package nl.jdj.jqueues.r5.misc.book.book_07_statistics;

import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.util.stat.AbstractSimQueueStat;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class Statistics_130_AbstractSimQueueStat
{

  private static class AvgJStatListener
  extends AbstractSimQueueStat
  {

    public AvgJStatListener (SimQueue queue)
    {
      super (queue);
    }
    
    private double cumJ = 0;
    
    private double avgJ = 0;
    
    @Override
    protected void resetStatistics ()
    {
      cumJ = 0;
      avgJ = 0;
    }

    @Override
    protected void updateStatistics (double time, double dt)
    {
      cumJ += getQueue ().getNumberOfJobs () * dt;
    }

    @Override
    protected void calculateStatistics (double startTime, double endTime)
    {
      if (startTime == endTime)
        return;
      if (! Double.isInfinite (endTime - startTime))
        avgJ = cumJ / (endTime - startTime);
      else
        avgJ = 0;
    }
    
    public double getAvgJ ()
    {
      calculate ();
      return this.avgJ;
    }
    
  }
  
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
    final AvgJStatListener avgJStatListener_fcfs = new AvgJStatListener (fcfs);
    final AvgJStatListener avgJStatListener_lcfs = new AvgJStatListener (lcfs);
    el.run ();
    System.out.println ("FCFS:");
    System.out.println ("  Start time: " + avgJStatListener_fcfs.getStartTime () + ".");
    System.out.println ("  End Time  : " + avgJStatListener_fcfs.getLastUpdateTime () + ".");
    System.out.println ("  Average number of jobs: " + avgJStatListener_fcfs.getAvgJ () + ".");
    System.out.println ("P_LCFS:");
    System.out.println ("  Start time: " + avgJStatListener_lcfs.getStartTime () + ".");
    System.out.println ("  End Time  : " + avgJStatListener_lcfs.getLastUpdateTime () + ".");
    System.out.println ("  Average number of jobs: " + avgJStatListener_lcfs.getAvgJ () + ".");
  }
  
}
