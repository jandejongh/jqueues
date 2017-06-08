package nl.jdj.jqueues.r5.misc.book.book_010_guided_tour;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.SJF;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_500_SimExample5_Revocation_Example1
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final SJF queue = new SJF (el);
    queue.registerStdOutSimEntityListener ();
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
    // Allow jobs to start at t=10.
    SimQueueEventScheduler.scheduleServerAccessCredits (queue, 10.0, Integer.MAX_VALUE);
    // At t=10, the SJF will select job 4 for service since it has the shortest service time (3.0).
    // The next revocation attempt will therefore fail, because job 4 (index 3) is in service,
    // and we do not allow the revocation from the service area.
    SimJQEventScheduler.scheduleJobRevocation (jobs.get (3), queue, 11.0, false);
    // But this attempt will succeed, because this time we allow interruption of service.
    SimJQEventScheduler.scheduleJobRevocation (jobs.get (3), queue, 12.0, true);
    // Because at t=12, job 4 (index 3) is revoked, the queue will take
    // job 3 (index 2) into service, with service time 4.0.
    // This attempt will succeed; job 1 (index 0) is in the waiting queue.
    SimJQEventScheduler.scheduleJobRevocation (jobs.get (0), queue, 13.0, false);
    // However, the next attempt will fail (silently) because job 4 (index 3)
    // is not longer present..
    SimJQEventScheduler.scheduleJobRevocation (jobs.get (3), queue, 15.0, true);
    el.run ();
  }
  
}
