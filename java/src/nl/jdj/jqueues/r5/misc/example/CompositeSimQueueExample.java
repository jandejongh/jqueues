package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.jackson.Jackson;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.GeneralParallelSimQueues;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.single.feedback.FB_v;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.single.feedback.FB_p;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.RANDOM;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for (some) {@link SimQueueComposite}s.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public final class CompositeSimQueueExample
{
  
  /** Prevents instantiation.
   * 
   */
  private CompositeSimQueueExample ()
  {
  }
  
  /** DelegateSimJob implementation used in the examples.
   * 
   */
  public static class TestDelegateSimJob extends AbstractSimJob
  {
    
    private final boolean reported;
    
    private final int n;
    
    public TestDelegateSimJob (DefaultExampleSimJob realSimJob, boolean reported)
    {
      super (null, null);
      this.reported = reported;
      this.n = realSimJob.n;
      if (n < 0)
        throw new IllegalArgumentException ();
      setName ("DJ_" + this.n);
    }

    @Override
    public double getServiceTime (SimQueue queue) throws IllegalArgumentException
    {
      if (queue instanceof LCFS)
        return 2 * this.n;
      else if (queue instanceof FCFS)
        return this.n;
      else if (queue instanceof RANDOM)
        return this.n;
      else
        throw new IllegalStateException ();
    }

  }
  
  /** Main method.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR (some) for composite queues...");
    System.out.println ("-> Creating jobs...");
    final List<DefaultExampleSimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new DefaultExampleSimJob (true, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue = new FCFS (el);
    fcfsQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating LCFS queue...");
    final SimQueue lcfsQueue = new LCFS (el);
    lcfsQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating Tandem queue...");
    final Set<SimQueue> set = new LinkedHashSet<> ();
    set.add (fcfsQueue);
    set.add (lcfsQueue);
    final DelegateSimJobFactory delegateSimJobFactory =
      new DelegateSimJobFactory<TestDelegateSimJob, SimQueue, DefaultExampleSimJob, SimQueue> ()
      {
        @Override
        public TestDelegateSimJob newInstance (double time, DefaultExampleSimJob job, SimQueue queue)
        {
          return new TestDelegateSimJob (job, true);
        }
      };
    final SimQueue tandemQueue = new Tandem (el, set, delegateSimJobFactory);
    tandemQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Submitting jobs to Tandem queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          tandemQueue.arrive (arrTime, j);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue2 = new FCFS (el);
    fcfsQueue2.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating LCFS queue...");
    final SimQueue lcfsQueue2 = new LCFS (el);
    lcfsQueue2.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating Parallel queue...");
    final Set<SimQueue> set2 = new LinkedHashSet<> ();
    set2.add (fcfsQueue2);
    set2.add (lcfsQueue2);
    final SimQueue parallelQueue = new GeneralParallelSimQueues
      (el, set2,
      new SimQueueSelector<SimJob, SimQueue> ()
      {
        boolean toSecond = false;
        @Override
        public void resetSimQueueSelector ()
        {
        }
        @Override
        public SimQueue selectFirstQueue (double time, SimJob job)
        {
          final SimQueue dstQueue;
          if (toSecond)
            dstQueue = lcfsQueue2;
          else
            dstQueue = fcfsQueue2;
          toSecond = ! toSecond;
          return dstQueue;
        }
        @Override
        public SimQueue<SimJob, SimQueue> selectNextQueue (double time, SimJob job, SimQueue previousQueue)
        {
          return null;
        }
      }, delegateSimJobFactory);
    parallelQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Submitting jobs to Parallel queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          parallelQueue.arrive (arrTime, j);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue3 = new FCFS (el);
    fcfsQueue3.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating NumVisits feedback queue (5 visits)...");
    final SimQueue numVisitsFBQueue =
      new FB_v (el, fcfsQueue3, 5, delegateSimJobFactory);
    numVisitsFBQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Submitting jobs to NumVisitsFB queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          numVisitsFBQueue.arrive (arrTime, j);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating RANDOM queue...");
    final SimQueue randomQueue = new RANDOM (el);
    randomQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating probabilistic feedback queue (p=50%)...");
    final SimQueue pFBQueue =
      new FB_p (el, randomQueue, 0.5, null, delegateSimJobFactory);
    pFBQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Submitting jobs to probabilistic feedback queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          pFBQueue.arrive (arrTime, j);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("-> Creating 4 FCFS queues and putting them into a LinkedHashSet...");
    final SimQueue jFcfsQueue1 = new FCFS (el);
    jFcfsQueue1.setName (jFcfsQueue1.toStringDefault () + "1");
    jFcfsQueue1.registerSimEntityListener (new StdOutSimQueueListener ());
    final SimQueue jFcfsQueue2 = new FCFS (el);
    jFcfsQueue2.setName (jFcfsQueue2.toStringDefault () + "2");
    jFcfsQueue2.registerSimEntityListener (new StdOutSimQueueListener ());
    final SimQueue jFcfsQueue3 = new FCFS (el);
    jFcfsQueue3.setName (jFcfsQueue3.toStringDefault () + "3");
    jFcfsQueue3.registerSimEntityListener (new StdOutSimQueueListener ());
    final SimQueue jFcfsQueue4 = new FCFS (el);
    jFcfsQueue3.setName (jFcfsQueue4.toStringDefault () + "4");
    jFcfsQueue4.registerSimEntityListener (new StdOutSimQueueListener ());
    final Set<SimQueue> jacksonQueues = new LinkedHashSet<> ();
    jacksonQueues.add (jFcfsQueue1);
    jacksonQueues.add (jFcfsQueue2);
    jacksonQueues.add (jFcfsQueue3);
    jacksonQueues.add (jFcfsQueue4);
    System.out.println ("-> Creating Arrival and Transition probabilities...");
    final double[] pdfArrival = new double[] { 0.4, 0.4, 0, 0 };
    final double[][] pdfTransition = new double[][] { { 0.2, 0.2, 0.2, 0.2 },
                                                      { 0.2, 0.2, 0.2, 0.2 },
                                                      { 0.0, 0.0, 0.4, 0.4 },
                                                      { 0.0, 0.0, 0.4, 0.4 }};
    System.out.println ("-> Creating Jackson queueing network...");
    final SimQueue jacksonQueue =
      new Jackson
        (el, jacksonQueues, pdfArrival, pdfTransition, null, delegateSimJobFactory);
    jacksonQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Submitting jobs to Jackson queueing network...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          jacksonQueue.arrive (arrTime, j);
        }
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("=== FINISHED ===");
    try
    {
      Thread.sleep (5000l);    
    }
    catch (Exception e)
    {
    }
  }
  
}