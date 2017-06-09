package nl.jdj.jqueues.r5.misc.book.book_09_composite_queues;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.listener.StdOutSimJQListener;
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
    final Tandem compositeQueue = new Tandem (el, subQueues, null);
    for (int j = 1; j <= 5; j++)
    {
      final SimJob job = new DefaultSimJob (el, "J" + j, 10 * j);
      compositeQueue.scheduleJobArrival (j, job);
      job.registerSimEntityListener (new StdOutSimJQListener ());
    }
    el.run ();
  }
  
}
