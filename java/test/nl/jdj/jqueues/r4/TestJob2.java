package nl.jdj.jqueues.r4;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r4.util.jobfactory.DefaultVisitsLoggingSimJob;

/**
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class TestJob2<Q extends SimQueue>
extends DefaultVisitsLoggingSimJob<TestJob2, Q>
{

  private final boolean reported;

  public final int n;

  public final double scheduledArrivalTime;
  
  public static Map<SimQueue, Double> createRequestedServiceTimeMap (final int n)
  {
    if (n <= 0)
      throw new IllegalArgumentException ();
    final Map<SimQueue, Double> requestedServiceTimeMap = new HashMap<> ();
    requestedServiceTimeMap.put (null, (double) n);
    return requestedServiceTimeMap;
  }
  
  public TestJob2 (boolean reported, int n)
  {
    super (null, "TestJob[" + n + "]", (Map<Q, Double>) createRequestedServiceTimeMap (n));
    this.reported = reported;
    this.n = n;
    this.scheduledArrivalTime = this.n;
  }

}
