package nl.jdj.jqueues.r4;

import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestJob extends AbstractSimJob
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

  public boolean arrived = false;

  public boolean started = false;

  public boolean dropped = false;

  public boolean departed = false;

  public double arrivalTime = 0.0;

  public double startTime = 0.0;

  public double dropTime = 0.0;

  public double departureTime = 0.0;

  @Override
  public double getServiceTime (SimQueue queue) throws IllegalArgumentException
  {
    if (queue == null && getQueue () == null)
      return 0.0;
    else
      return (double) n;
  }

  public final SimEventAction<SimJob> QUEUE_ARRIVE_ACTION = new SimEventAction<SimJob> ()
  {
    @Override
    public void action (final SimEvent event)
    {
      if (TestJob.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " arrives.");
      if (TestJob.this.arrived)
        fail ("Already arrived!");
      TestJob.this.arrived = true;
      TestJob.this.arrivalTime = event.getTime ();
    }

  };

  @Override
  public SimEventAction<SimJob> getQueueArriveAction ()
  {
    return this.QUEUE_ARRIVE_ACTION;
  }

  public final SimEventAction<SimJob> QUEUE_START_ACTION = new SimEventAction<SimJob> ()
  {
    @Override
    public void action (final SimEvent event)
    {
      if (TestJob.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " starts.");
      if (TestJob.this.started)
        fail ("Already started!");
      if (!TestJob.this.arrived)
        fail ("Starting before arrival!");
      if (TestJob.this.departed)
        fail ("Starting after departure!");
      if (TestJob.this.dropped)
        fail ("Starting after drop!");
      TestJob.this.started = true;
      TestJob.this.startTime = event.getTime ();
    }

  };

  @Override
  public SimEventAction<SimJob> getQueueStartAction ()
  {
    return this.QUEUE_START_ACTION;
  }

  public final SimEventAction<SimJob> QUEUE_DROP_ACTION = new SimEventAction<SimJob> ()
  {
    @Override
    public void action (final SimEvent event)
    {
      if (TestJob.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " dropped.");
      if (!TestJob.this.arrived)
        fail ("Dropped before arrival!");
      if (TestJob.this.departed)
        fail ("Dropped after departure!");
      TestJob.this.dropped = true;
      TestJob.this.dropTime = event.getTime ();
    }

  };

  @Override
  public SimEventAction<SimJob> getQueueDropAction ()
  {
    return this.QUEUE_DROP_ACTION;
  }

  public final SimEventAction<SimJob> QUEUE_DEPART_ACTION = new SimEventAction<SimJob> ()
  {
    @Override
    public void action (final SimEvent event)
    {
      if (TestJob.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob.this.n + " departs.");
      if (TestJob.this.departed)
        fail ("Already departed!");
      if (!TestJob.this.arrived)
        fail ("Departure before arrival!");
      if (!TestJob.this.started)
        fail ("Departure before start!");
      TestJob.this.departed = true;
      TestJob.this.departureTime = event.getTime ();
    }

  };

  @Override
  public SimEventAction<SimJob> getQueueDepartAction ()
  {
    return this.QUEUE_DEPART_ACTION;
  }

}
