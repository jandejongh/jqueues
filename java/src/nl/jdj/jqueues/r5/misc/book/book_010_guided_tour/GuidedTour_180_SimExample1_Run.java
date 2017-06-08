package nl.jdj.jqueues.r5.misc.book.book_010_guided_tour;

import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_180_SimExample1_Run
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final FCFS queue = new FCFS (el);
    queue.registerStdOutSimEntityListener ();
    for (int j = 0; j < 10; j++)
    {
      final double jobServiceTime = 100.0; // Double.POSITIVE_INFINITY;
      final double jobArrivalTime = (double) j;
      final String jobName = Integer.toString (j);
      final SimJob job = new DefaultSimJob (null, jobName, jobServiceTime);
      SimJQEventScheduler.scheduleJobArrival (job, queue, jobArrivalTime);
    }
    // Run the event list until t=3.0 (inclusive; set time to given time).
    el.runUntil (3.0, true, true);
    System.out.println ("Time on event list: " + el.getTime () + ".");
    // Run the event list until t=3.5 (inclusive; set time to last event processed).
    el.runUntil (3.5, true, false);
    System.out.println ("Time on event list: " + el.getTime () + ".");
    // Run the event list until t=3.7 (inclusive; set time to given time).
    el.runUntil (3.7, true, true);
    System.out.println ("Time on event list: " + el.getTime () + ".");
    // Run the event list until t=5.0 (exclusive; set time to last event processed).
    el.runUntil (5.0, false, false);
    System.out.println ("Time on event list: " + el.getTime () + ".");
    // Run the event list until t=7.0 (exclusive; set time to given time => DOES NOT WORK).
    el.runUntil (7.0, false, true);
    System.out.println ("Time on event list: " + el.getTime () + ".");
    // Process remaining events, one at a time.
    while (! el.isEmpty ())
    {
      el.runSingleStep ();
      System.out.println ("Time on event list: " + el.getTime () + ".");
    }
    System.out.println ("Finished!");
  }
  
}
