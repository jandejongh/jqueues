package nl.jdj.jqueues.r4;

import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestJob1 extends AbstractSimJob
{

  private final boolean reported;

  public final int n;

  public TestJob1 (boolean reported, int n)
  {
    if (n <= 0)
      throw new IllegalArgumentException ();
    this.reported = reported;
    this.n = n;
    this.scheduledArrivalTime = this.n;
    setName ("TestJob[" + this.n + "]");
  }

  public final double scheduledArrivalTime;
  
  public boolean arrived = false;

  public boolean started = false;

  public boolean dropped = false;
  
  public boolean revoked = false;

  public boolean departed = false;

  public double arrivalTime = 0.0;

  public double startTime = 0.0;

  public double dropTime = 0.0;

  public double revocationTime = 0.0;
  
  public double departureTime = 0.0;

  public boolean predicted = false;
  
  public boolean predictedArrived = false;

  public boolean predictedStarted = false;

  public boolean predictedDropped = false;

  public boolean predictedRevoked = false;

  public boolean predictedDeparted = false;

  public double predictedArrivalTime = 0.0;

  public double predictedStartTime = 0.0;

  public double predictedDropTime = 0.0;

  public double predictedRevocationTime = 0.0;
    
  public double predictedDepartureTime = 0.0;
    
  public void testPrediction (final double accuracy)
  {
    assert this.predicted;
    assert this.arrived  == this.predictedArrived;
    assert this.started  == this.predictedStarted;
    assert this.dropped  == this.predictedDropped;
    assert this.revoked  == this.predictedRevoked;
    assert this.departed == this.predictedDeparted;
    if (this.arrived)
      assertEquals (this.predictedArrivalTime, this.arrivalTime, accuracy);
    if (this.started)
      assertEquals (this.predictedStartTime, this.startTime, accuracy);
    if (this.dropped)
      assertEquals (this.predictedDropTime, this.dropTime, accuracy);
    if (this.revoked)
      assertEquals (this.predictedRevocationTime, this.revocationTime, accuracy);
    if (this.departed)
      assertEquals (this.predictedDepartureTime, this.departureTime, accuracy);
  }
  
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
      if (TestJob1.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob1.this.n + " arrives.");
      if (TestJob1.this.arrived)
        fail ("Already arrived!");
      TestJob1.this.arrived = true;
      TestJob1.this.arrivalTime = event.getTime ();
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
      if (TestJob1.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob1.this.n + " starts.");
      if (TestJob1.this.started)
        fail ("Already started!");
      if (! TestJob1.this.arrived)
        fail ("Starting before arrival!");
      if (TestJob1.this.dropped)
        fail ("Starting after drop!");
      if (TestJob1.this.revoked)
        fail ("Starting after revocation!");
      if (TestJob1.this.departed)
        fail ("Starting after departure!");
      TestJob1.this.started = true;
      TestJob1.this.startTime = event.getTime ();
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
      if (TestJob1.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob1.this.n + " dropped.");
      if (! TestJob1.this.arrived)
        fail ("Dropped before arrival!");
      if (TestJob1.this.departed)
        fail ("Dropped after departure!");
      TestJob1.this.dropped = true;
      TestJob1.this.dropTime = event.getTime ();
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
      if (TestJob1.this.reported)
        System.out.println ("t = " + event.getTime () + ": Job " + TestJob1.this.n + " departs.");
      if (TestJob1.this.departed)
        fail ("Already departed!");
      if (! TestJob1.this.arrived)
        fail ("Departure before arrival!");
      TestJob1.this.departed = true;
      TestJob1.this.departureTime = event.getTime ();
    }
  };

  @Override
  public SimEventAction<SimJob> getQueueDepartAction ()
  {
    return this.QUEUE_DEPART_ACTION;
  }

}
