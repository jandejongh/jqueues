package nl.jdj.jqueues.r5.misc.book.book_11_statistics;

import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.listener.DefaultSimEntityListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class Statistics_110_SimStatAverageNumberOfJobsAtQueue
{

  private final static class AvgJStatListener
  extends DefaultSimEntityListener
  {
    
    public AvgJStatListener (SimQueue queue)
    {
      notifyResetEntity (queue);
      queue.registerSimEntityListener (this);
    }
    
    private double tStart = Double.NEGATIVE_INFINITY;
    private double tLast = Double.NEGATIVE_INFINITY;
    private double cumJ = 0;

    @Override
    public void notifyResetEntity (SimEntity entity)
    {
      tStart = entity.getEventList ().getTime ();
      tLast = tStart;
      cumJ = 0;
    }

    @Override
    public void notifyUpdate (double time, SimEntity entity)
    {
      cumJ += ((SimQueue) entity).getNumberOfJobs () * (time - tLast);
      tLast = time;
    }
      
    public final double getStartTime ()
    {
      return this.tStart;
    }
    
    public final double getEndTime ()
    {
      return this.tLast;
    }
    
    public final double calculate ()
    {
      if (tLast > tStart)
      {
        if (! Double.isInfinite (tLast - tStart))
          return cumJ / (tLast - tStart);
        else
          return 0;
      }
      else
        return 0;
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
    System.out.println ("  End Time  : " + avgJStatListener_fcfs.getEndTime () + ".");
    System.out.println ("  Average number of jobs: " + avgJStatListener_fcfs.calculate () + ".");
    System.out.println ("P_LCFS:");
    System.out.println ("  Start time: " + avgJStatListener_lcfs.getStartTime () + ".");
    System.out.println ("  End Time  : " + avgJStatListener_lcfs.getEndTime () + ".");
    System.out.println ("  Average number of jobs: " + avgJStatListener_lcfs.calculate () + ".");
  }
  
}
