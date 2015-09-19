package nl.jdj.jqueues.r3.mac.example;

import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.StdOutSimQueueListener;
import nl.jdj.jqueues.r4.StdOutSimQueueVacationListener;
import nl.jdj.jqueues.r3.mac.DCF;
import nl.jdj.jqueues.r3.mac.DCFSimJob;
import nl.jdj.jqueues.r3.mac.MediumPhyState;
import nl.jdj.jqueues.r3.mac.MediumPhyStateMonitor;
import nl.jdj.jqueues.r3.mac.MediumPhyStateObserver;
import nl.jdj.jqueues.r3.mac.StdOutDCFStateListener;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** Example code for {@link nl.jdj.jqueues}.
 * 
 */
public final class Main
{
  
  /** Prevents instantiation.
   * 
   */
  private Main ()
  {
  }
  
  public static class TestJob extends DCFSimJob
  {
    
    private final boolean reported;
    
    public final int n;
    
    public TestJob (boolean reported, int n)
    {
      if (n <= 0)
        throw new IllegalArgumentException ();
      this.reported = reported;
      this.n = n;
    }

    public final SimEventAction QUEUE_ARRIVE_ACTION = new SimEventAction ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("[TestJob] t=" + event.getTime () + ": ARRIVAL of " + TestJob.this + ".");      
      }
    };
    
    @Override
    public SimEventAction<DCFSimJob> getQueueArriveAction ()
    {
      return this.QUEUE_ARRIVE_ACTION;
    }
    
    public final SimEventAction QUEUE_DEPART_ACTION = new SimEventAction ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("[TestJob] t=" + event.getTime () + ": DEPARTURE of " + TestJob.this + ".");      
      }
    };
    
    @Override
    public SimEventAction getQueueDepartAction ()
    {
      return this.QUEUE_DEPART_ACTION;
    }

    public final SimEventAction QUEUE_START_ACTION = new SimEventAction ()
    {
      @Override
      public void action (final SimEvent event)
      {
        if (TestJob.this.reported)
          System.out.println ("[TestJob] t=" + event.getTime () + ": START of " + TestJob.this + ".");      
      }
    };
    
    @Override
    public SimEventAction getQueueStartAction ()
    {
      return this.QUEUE_START_ACTION;
    }

    @Override
    public String toString ()
    {
      return "TestJob_" + this.n;
    }

  }
  
  /** Main method.
   * 
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.mac PACKAGE ===");
    System.out.println ("-> Creating single DCF job...");
    final DCFSimJob job = new TestJob (true, 1);
    final DCFSimJob job2 = new TestJob (true, 2);
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    System.out.println ("-> Creating simple medium...");
    final MediumPhyStateMonitor mediumPhyStateMonitor = new MediumPhyStateMonitor ()
    {
      private MediumPhyStateObserver observer;
      @Override
      public void registerMediumPhyStateObserver (final MediumPhyStateObserver observer)
      {
        this.observer = observer;
      }
      @Override
      public void unregisterMediumPhyStateObserver (final MediumPhyStateObserver observer)
      {
        this.observer = null;
      }
      @Override
      public MediumPhyState getMediumPhyState (final double time, final MediumPhyStateObserver observer)
      {
        if (transmitting)
          return MediumPhyState.TX_BUSY;
        else
          return MediumPhyState.IDLE;
      }
      private boolean transmitting = false;
      @Override
      public void startTransmission (final double time, final MediumPhyStateObserver observer, final DCFSimJob job)
      {
        this.transmitting = true;
        this.observer.mediumPhyStateUpdate (time, MediumPhyState.TX_BUSY);
        el.add (new SimEvent (time + 5.0, null, new SimEventAction ()
        {
          @Override
          public void action (final SimEvent event)
          {
            transmitting = false;
            observer.mediumPhyStateUpdate (event.getTime (), MediumPhyState.IDLE);
          }
        }));
      }
    };
    System.out.println ("-> Creating DCF queue...");
    final double slotTime_s = 1.0e-5; // 10 mus.
    final int aifs_slots = 3;
    final double difs_mus = 1.0;
    final double eifs_mus = 2.0;
    final int cw = 16;
    final DCF dcfQueue = new DCF (el, mediumPhyStateMonitor, slotTime_s, aifs_slots, difs_mus, eifs_mus, cw);
    dcfQueue.registerQueueListener (new StdOutSimQueueListener<DCFSimJob, DCF> ());
    for (SimQueue queue : dcfQueue.getQueues ())
      queue.registerQueueListener (new StdOutSimQueueVacationListener ());
    dcfQueue.registerDCFStateListener (new StdOutDCFStateListener ());
    System.out.println ("-> Submitting job to DCF queue at t = 10.0...");
    dcfQueue.scheduleJobArrival (10.0, job);
    dcfQueue.scheduleJobArrival (100.0, job2);
    System.out.println ("-> Executing event list...");
    el.run ();
    //System.out.println ("-> Resetting event list...");
    //el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}