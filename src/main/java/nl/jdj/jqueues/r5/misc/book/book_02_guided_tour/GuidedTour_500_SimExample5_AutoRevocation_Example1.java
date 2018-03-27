package nl.jdj.jqueues.r5.misc.book.book_02_guided_tour;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.SJF;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_500_SimExample5_AutoRevocation_Example1
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final SJF queue = new SJF (el);
    queue.registerStdOutSimEntityListener ();
    // Set auto-revocation upon start.
    queue.setAutoRevocationPolicy (SimQueue.AutoRevocationPolicy.UPON_START);
    el.reset (0.0);
    final List<SimJob> jobs = new ArrayList<>  ();
    // Do not allow jobs to start until t=10.
    SimQueueEventScheduler.scheduleServerAccessCredits (queue, 0.0, 0);
    for (int j = 1; j <= 4; j++)
    {
      final double jobServiceTime = 12.0 / j;
      final double jobArrivalTime = (double) j;
      final String jobName = Integer.toString (j);
      final SimJob job = new DefaultSimJob (null, jobName, jobServiceTime);
      jobs.add (job);
      SimJQEventScheduler.scheduleJobArrival (job, queue, jobArrivalTime);
    }
    // Allow two jobs to start at t=10.
    // These will be immediately auto-revoked.
    SimQueueEventScheduler.scheduleServerAccessCredits (queue, 10.0, 2);
    // Allow the other two jobs to start at t=13.
    // These, again, will be immediately auto-revoked.
    SimQueueEventScheduler.scheduleServerAccessCredits (queue, 13.0, Integer.MAX_VALUE);    
    el.run ();
  }
  
}
