package nl.jdj.jqueues.r5.misc.book.book_07_statistics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.feedback.FB_p;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.SINK;
import nl.jdj.jqueues.r5.listener.DefaultSimJobListener;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;
import nl.jdj.jsimulation.r5.SimEventListResetListener;

final class Statistics_120_TotalSojournTimeOfAJobAtVisitedQueues
{

  private final static class AvgSojStatListener
  extends DefaultSimJobListener
  implements SimEventListResetListener
  {
    
    public AvgSojStatListener (final SimJob job)
    {
      this.job = job;
      notifyResetEntity (job);
    }
    
    private final SimJob job;
    
    private final Map<SimQueue, Map<Integer, Double>> visitsMap = new LinkedHashMap<> ();
    
    private double lastArrTime = Double.NaN;

    private final Map<SimQueue, Double> sojournTimeMap = new LinkedHashMap<> ();
    
    @Override
    public final void notifyEventListReset (final SimEventList eventList)
    {
      notifyResetEntity (null);
    }
    
    @Override
    public void notifyResetEntity (final SimEntity entity_dummy)
    {
      this.visitsMap.clear ();
      this.lastArrTime = Double.NaN;
      this.sojournTimeMap.clear ();
    }

    @Override
    public void notifyArrival (final double time, final SimJob job, final SimQueue queue)
    {
      if (! Double.isNaN (this.lastArrTime))
        throw new IllegalStateException ();
      this.lastArrTime = time;
    }

    @Override
    public void notifyDrop (final double time, final SimJob job, final SimQueue queue)
    {
      notifyDeparture (time, job, queue);
    }

    @Override
    public void notifyRevocation (final double time, final SimJob job, final SimQueue queue)
    {
      notifyDeparture (time, job, queue);
    }

    @Override
    public void notifyAutoRevocation (final double time, final SimJob job, final SimQueue queue)
    {
      notifyDeparture (time, job, queue);
    }

    @Override
    public void notifyDeparture (final double time, final SimJob job, final SimQueue queue)
    {
      if (Double.isNaN (this.lastArrTime))
        throw new IllegalStateException ();
      final double newSojTime = time - this.lastArrTime;
      if (! this.visitsMap.containsKey (queue))
      {
        this.visitsMap.put (queue, new HashMap<> ());
        this.visitsMap.get (queue).put (1, newSojTime);
      }
      else
      {
        final int oldVisits = this.visitsMap.get (queue).keySet ().iterator ().next ();
        final double oldCumSojJ = this.visitsMap.get (queue).get (oldVisits);
        this.visitsMap.get (queue).clear ();
        this.visitsMap.get (queue).put (oldVisits + 1, oldCumSojJ + newSojTime);
      }
      this.lastArrTime = Double.NaN;
    }

    private void calculate (final double time)
    {
      this.sojournTimeMap.clear ();
      for (final Map.Entry<SimQueue, Map<Integer, Double>> entry : this.visitsMap.entrySet ())
      {
        final SimQueue queue = entry.getKey ();
        int visits = entry.getValue ().keySet ().iterator ().next ();
        double cumSojJ = entry.getValue ().get (visits);
        if ((! Double.isNaN (this.lastArrTime))
        &&  this.job.getQueue () == queue)
        {
          visits++;
          cumSojJ += (time - this.lastArrTime);
        }
        this.sojournTimeMap.put (entry.getKey (), cumSojJ / visits);
      }
      if ((! Double.isNaN (this.lastArrTime))
      &&  (! this.sojournTimeMap.containsKey (this.job.getQueue ())))
        this.sojournTimeMap.put (this.job.getQueue (), time - this.lastArrTime);
    }
    
    public final void report (final double time)
    {
      calculate (time);
      System.out.println ("Time = " + time + ":");
      if (this.sojournTimeMap.isEmpty ())
        System.out.println ("  No visits recorded!");
      else
        for (final Map.Entry<SimQueue, Double> entry : this.sojournTimeMap.entrySet ())
          System.out.println ("  Queue = " + entry.getKey ()
            + ", avg sojourn time = " + entry.getValue ());
    }
    
  }
  
  public static void main (final String[] args)
  {
    
    final SimEventList el = new DefaultSimEventList ();
    el.reset (0);

    final SimJob job = new DefaultSimJob (el, "job", 1.0);
    final AvgSojStatListener statListener = new AvgSojStatListener (job);
    job.registerSimEntityListener (statListener);
    el.addListener (statListener);
    
    final FB_p fb_25 = new FB_p (el, new FCFS (el), 0.25, null, null);
    final FB_p fb_50 = new FB_p (el, new FCFS (el), 0.50, null, null);
    final FB_p fb_75 = new FB_p (el, new FCFS (el), 0.75, null, null);
    final SINK sink  = new SINK (el);
        
    final int FEEDBACK_TANDEM_VISITS = 10000;
    
    job.registerSimEntityListener (new DefaultSimJobListener ()
    {

      private int fbVisitCycles = 0;
      
      @Override
      public void notifyDrop (final double time, final SimJob job, final SimQueue queue)
      {
        notifyDeparture (time, job, queue);
      }
      
      @Override
      public void notifyRevocation (final double time, final SimJob job, final SimQueue queue)
      {
        notifyDeparture (time, job, queue);
      }

      @Override
      public void notifyAutoRevocation (final double time, final SimJob job, final SimQueue queue)
      {
        notifyDeparture (time, job, queue);
      }

      @Override
      public void notifyDeparture (final double time, final SimJob job, final SimQueue queue)
      {
        if (queue == fb_25)
          SimJQEventScheduler.scheduleJobArrival (job, fb_50, time);
        else if (queue == fb_50)
          SimJQEventScheduler.scheduleJobArrival (job, fb_75, time);
        else if (queue == fb_75)
        {
          if (this.fbVisitCycles++ < FEEDBACK_TANDEM_VISITS)
            SimJQEventScheduler.scheduleJobArrival (job, fb_25, time);
          else
            SimJQEventScheduler.scheduleJobArrival (job, sink, time);
        }
        else
          throw new IllegalStateException ();
      }
    });
    
    fb_25.scheduleJobArrival (0, job);
    
    el.run ();
    statListener.report (el.getTime ());
    
    el.runUntil (el.getTime () + 1000, true, true);
    statListener.report (el.getTime ());    
    
    el.reset ();
    statListener.report (el.getTime ());
    
  }
  
}
