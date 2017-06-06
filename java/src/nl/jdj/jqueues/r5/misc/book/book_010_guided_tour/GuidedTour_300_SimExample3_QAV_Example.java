package nl.jdj.jqueues.r5.misc.book.book_010_guided_tour;

import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.processorsharing.SocPS;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_300_SimExample3_QAV_Example
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final SocPS queue = new SocPS (el);
    queue.registerStdOutSimEntityListener ();
    el.reset (1.0);
    SimQueueEventScheduler.scheduleQueueAccessVacation (queue, 1.75, true);
    SimQueueEventScheduler.scheduleQueueAccessVacation (queue, 2.25, false);
    for (int j = 1; j <= 4; j++)
    {
      final double jobServiceTime = (double) 2.2 * j;
      final double jobArrivalTime = (double) j;
      final String jobName = Integer.toString (j);
      final SimJob job = new DefaultSimJob (null, jobName, jobServiceTime);
      SimJQEventScheduler.scheduleJobArrival (job, queue, jobArrivalTime);
    }
    el.run ();
  }
  
}
