package nl.jdj.jqueues.r5.misc.book.book_09_composite_queues;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.queue.composite.tandem.BlackTandemSimQueue;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.listener.StdOutSimEntityListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class CompositeQueues_130_Tandem_Dual_P_LCFS_CompositeSimQueue
{

  public static void main (final String[] args)
  {    
    final SimEventList el = new DefaultSimEventList ();
    el.reset (0);
    final P_LCFS lcfs_1 = new P_LCFS (el, null);
    lcfs_1.setName ("Q1");
    final P_LCFS lcfs_2 = new P_LCFS (el, null);
    lcfs_2.setName ("Q2");
    final Set<SimQueue> subQueues = new LinkedHashSet<>  ();
    subQueues.add (lcfs_1);
    subQueues.add (lcfs_2);
    final BlackTandemSimQueue compositeQueue = new BlackTandemSimQueue (el, subQueues, null);
    for (int j = 1; j <= 5; j++)
    {
      final SimJob job = new DefaultSimJob (el, "J" + j, 10 * j);
      compositeQueue.scheduleJobArrival (j, job);
      job.registerSimEntityListener (new StdOutSimEntityListener ());
    }
    el.run ();
  }
  
}
