package nl.jdj.jqueues.r5.misc.book.book_010_guided_tour;

import java.util.List;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueListener;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class GuidedTour_130_SimExample1_Listener1_Suppress1
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final int bufferSize = 2;
    final FCFS_B queue = new FCFS_B (el, bufferSize);
    final SimQueueListener listener = new StdOutSimQueueListener ()
    {
      
      @Override
      public void notifyStateChanged (double time, SimEntity entity, List notifications) {}

      @Override
      public void notifyUpdate (double time, SimEntity entity) {}
      
      @Override
      public void notifyStartQueueAccessVacation (double time, SimQueue queue) {}

      @Override
      public void notifyStopQueueAccessVacation (double time, SimQueue queue) {}

      @Override
      public void notifyNewStartArmed (double time, SimQueue queue, boolean startArmed) {}
      
      @Override
      public void notifyOutOfServerAccessCredits (double time, SimQueue queue) {}

      @Override
      public void notifyRegainedServerAccessCredits (double time, SimQueue queue) {}
      
    };
    queue.registerSimEntityListener (listener);
    for (int j = 0; j < 10; j++)
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
