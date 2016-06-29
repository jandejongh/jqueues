package nl.jdj.jqueues.r5.misc.book.book_09_composite_queues;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.listener.DefaultSimEntityListener;
import nl.jdj.jqueues.r5.listener.StdOutSimEntityListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class CompositeQueues_120_Tandem_Dual_P_LCFS_SimQueue
{

  public static void main (final String[] args)
  {    
    final SimEventList el = new DefaultSimEventList ();
    el.reset (0);
    final P_LCFS lcfs_1 = new P_LCFS (el, null);
    lcfs_1.setName ("Q1");
    final P_LCFS lcfs_2 = new P_LCFS (el, null);
    lcfs_2.setName ("Q2");
    lcfs_1.registerSimEntityListener (new DefaultSimEntityListener ()
    {
      @Override
      public void notifyDeparture (final double time, final SimJob job, final SimQueue queue)
      {
        el.schedule (time, (SimEventAction) (SimEvent event) ->
        {
          lcfs_2.arrive (time, job);
        });
      }
    });
    for (int j = 1; j <= 5; j++)
    {
      final SimJob job = new DefaultSimJob (el, "J" + j, 10 * j);
      lcfs_1.scheduleJobArrival (j, job);
      job.registerSimEntityListener (new StdOutSimEntityListener ());
    }
    el.run ();
  }
  
}
