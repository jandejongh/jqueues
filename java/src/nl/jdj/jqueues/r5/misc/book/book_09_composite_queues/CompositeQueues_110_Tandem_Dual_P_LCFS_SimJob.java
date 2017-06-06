package nl.jdj.jqueues.r5.misc.book.book_09_composite_queues;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.selflistening.DefaultSelfListeningSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.listener.StdOutSimJQListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class CompositeQueues_110_Tandem_Dual_P_LCFS_SimJob
{

  private static final class TandemJob
  extends DefaultSelfListeningSimJob
  {

    final List<SimQueue> queues;
    
    public TandemJob (final SimEventList eventList,
      final String name,
      final double requestedServiceTime,
      final List<SimQueue> queues)
    {
      super (eventList, name, requestedServiceTime);
      this.queues = queues;
      this.registerSimEntityListener (new StdOutSimJQListener ());
    }

    @Override
    public void notifyDeparture (final double time,
      final DefaultSelfListeningSimJob job,
      final SimQueue queue)
    {
      if (this.queues.indexOf (queue) < this.queues.size () - 1)
        getEventList ().schedule (time, (SimEventAction) (SimEvent event) ->
      {
        queues.get (queues.indexOf (queue) + 1).arrive (time, job);
      });
    }

  }
  
  public static void main (final String[] args)
  {    
    final SimEventList el = new DefaultSimEventList ();
    el.reset (0);
    final P_LCFS lcfs_1 = new P_LCFS (el, null);
    lcfs_1.setName ("Q1");
    final P_LCFS lcfs_2 = new P_LCFS (el, null);
    lcfs_2.setName ("Q2");
    final List<SimQueue> queueSequence = new ArrayList<> ();
    queueSequence.add (lcfs_1);
    queueSequence.add (lcfs_2);
    for (int j = 1; j <= 5; j++)
      lcfs_1.scheduleJobArrival (j, new TandemJob (el, "J" + j, 10 * j, queueSequence));
    el.run ();
  }
  
}
