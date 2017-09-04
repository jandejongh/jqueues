package nl.jdj.jqueues.r5.misc.book.book_02_guided_tour;

import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.processorsharing.PS;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_200_SimExample2_ResetExample
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final PS queue = new PS (el);
    final JobSojournTimeListenerWithReset listener = new JobSojournTimeListenerWithReset ();
    queue.registerSimEntityListener (listener);
    System.out.println ("BEFORE RESET");
    System.out.println ("  Time on event list is " + el.getTime () + ".");
    System.out.println ("  Time on queue is " + queue.getLastUpdateTime () + ".");
    for (int resetTime = -3; resetTime <= 0; resetTime++)
    {
      el.reset (resetTime);
      for (int j = 1; j <= 10; j++)
      {
        final double jobServiceTime = (double) 2.2 * j;
        final double jobArrivalTime = resetTime + (double) (j - 1);
        final String jobName = Integer.toString (j);
        final SimJob job = new DefaultSimJob (null, jobName, jobServiceTime);
        SimJQEventScheduler.scheduleJobArrival (job, queue, jobArrivalTime);
      }
      el.run ();
      System.out.println ("AFTER PASS " + (resetTime + 4) + ".");
      System.out.println ("  Time on event list is " + el.getTime () + ".");
      System.out.println ("  Time on queue is " + queue.getLastUpdateTime () + ".");
      System.out.println ("  Average job sojourn time is " + listener.getAvgSojournTime () + ".");
    }
  }
  
}
