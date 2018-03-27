package nl.jdj.jqueues.r5.misc.book.book_02_guided_tour;

import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueListener;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_127_SimExample1_Infinity
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final int bufferSize = 4;
    final FCFS_B queue = new FCFS_B (el, bufferSize);
    final SimQueueListener listener = new StdOutSimQueueListener ();
    queue.registerSimEntityListener (listener);
    for (int j = 1; j <= 2; j++)
    {
      final double jobServiceTime = Double.POSITIVE_INFINITY;
      final double jobArrivalTime = Double.NEGATIVE_INFINITY;
      final String jobName = Integer.toString (j);
      final SimJob job = new DefaultSimJob (null, jobName, jobServiceTime);
      SimJQEventScheduler.scheduleJobArrival (job, queue, jobArrivalTime);
    }
    final SimJob job3 = new DefaultSimJob (null, "3", Double.POSITIVE_INFINITY);
    SimJQEventScheduler.scheduleJobArrival (job3, queue, 0.0);
    final SimJob job4 = new DefaultSimJob (null, "4", Double.POSITIVE_INFINITY);
    SimJQEventScheduler.scheduleJobArrival (job4, queue, Double.POSITIVE_INFINITY);
    el.run ();
  }
  
}
