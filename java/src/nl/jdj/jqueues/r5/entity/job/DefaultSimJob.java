package nl.jdj.jqueues.r5.entity.job;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A reasonable first-order implementation of {@link SimJob} with support for naming, per-queue requested service times
 *  and a default service time.
 * 
 * <p>
 * Note that despite the flexibility of setting per-queue requested service-times, you cannot change this for a specific
 * {@link SimQueue} in between visits.
 * 
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 *
 * @see DefaultSimJobFactory
 * 
 */
public class DefaultSimJob<J extends DefaultSimJob, Q extends SimQueue>
extends AbstractSimJob<J, Q>
implements SimJob<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REQUESTED SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The mapping of {@link SimQueue}s visited onto requested service times.
   * 
   */
  private final Map<Q, Double> requestedServiceTimeMap;

  /** The default fallback requested service time.
   * 
   * <p>
   * This is the value returned by {@link #getFallbackRequestedServiceTime} by default, i.e.,
   * in absence of invocations of {@link #setFallbackRequestedServiceTime}.
   * 
   */
  public final static double DEFAULT_FALLBACK_REQUESTED_SERIVE_TIME = 1.0;
  
  /** The fallback requested service time, in case a value could not be obtained from the internal mapping of {@link SimQueue}s
   *  onto requested service times.
   * 
   */
  private double fallbackRequestedServiceTime = DefaultSimJob.DEFAULT_FALLBACK_REQUESTED_SERIVE_TIME;
  
  /** Returns the fallback requested service time,
   *  in case a value could not be obtained from the internal mapping of {@link SimQueue}s
   *  onto requested service times.
   * 
   * @return The fallback requested service time,
   *           in case a value could not be obtained from the internal mapping of {@link SimQueue}s
   *           onto requested service times.
   * 
   * @see #DEFAULT_FALLBACK_REQUESTED_SERIVE_TIME
   * 
   */
  public final double getFallbackRequestedServiceTime ()
  {
    return this.fallbackRequestedServiceTime;
  }
  
  /** Sets the fallback requested service time.
   * 
   * @param fallbackRequestedServiceTime The new fallback requested service time.
   * 
   * @see #getFallbackRequestedServiceTime
   * 
   * @throws IllegalArgumentException If the argument is strictly negative.
   * 
   */
  public final void setFallbackRequestedServiceTime (final double fallbackRequestedServiceTime)
  {
    if (fallbackRequestedServiceTime < 0)
      throw new IllegalArgumentException ();
    this.fallbackRequestedServiceTime = fallbackRequestedServiceTime;
  }
  
  /** Returns the service-time for this job for a queue visit.
   * 
   * <p>
   * For <code>null</code> arguments, this method follows the contract of {@link SimJob#getServiceTime}.
   * Otherwise, it inspects the internal mapping of {@link SimQueue}s onto service times, as explained in
   * {@link DefaultSimJob#DefaultSimJob}.
   * If the map fails to yield a service time, this method returns the result from {@link #getFallbackRequestedServiceTime}.
   * 
   * <p>
   * Note that the default implementation accepts any {@link SimQueue}.
   * 
   */
  @Override
  public double getServiceTime (final Q queue)
  {
    if (queue == null)
    {
      // By contract of SimJob.getServiceTime.
      if (getQueue () != null)
        return getServiceTime (getQueue ());
      else
        return 0.0;
    }
    if (this.requestedServiceTimeMap != null && this.requestedServiceTimeMap.containsKey (queue))
      return this.requestedServiceTimeMap.get (queue);
    else if (this.requestedServiceTimeMap != null && this.requestedServiceTimeMap.containsKey (null))
      return this.requestedServiceTimeMap.get (null);
    else
      return this.fallbackRequestedServiceTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link DefaultSimJob}.
   *
   * <p>
   * Note that an internal copy is made of the <code>requestedServiceTimeMap</code>.
   * 
   * @param eventList               The event list to use, may be {@code null}.
   * @param name                    The name of the new job; may be <code>null</code>.
   * @param requestedServiceTimeMap The requested service time for each key {@link SimQueue},
   *                                a <code>null</code> key can be used for unlisted {@link SimQueue}s.
   *                                If the map is <code>null</code> or no value could be found,
   *                                {@link  #getFallbackRequestedServiceTime} is used for the requested service time.
   * 
   */
  public DefaultSimJob (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    super (eventList, name);
    if (requestedServiceTimeMap != null)
      this.requestedServiceTimeMap = new HashMap<> (requestedServiceTimeMap);
    else
      this.requestedServiceTimeMap = null;
  }

  /** Creates a new {@link DefaultSimJob} with fixed service time request at any {@link SimQueue}.
   * 
   * <p>
   * Sets the fallback requested service time, so actually,
   * you can later change the requested service time through {@link #setFallbackRequestedServiceTime}.
   * 
   * @param eventList            The event list to use, may be {@code null}.
   * @param name                 The name of the new job; may be <code>null</code>.
   * @param requestedServiceTime The fixed requested service time of this job at any queue.
   * 
   * @throws IllegalArgumentException If the requested service time is (strictly) negative.
   * 
   */
  public DefaultSimJob (final SimEventList eventList, final String name, final double requestedServiceTime)
  {
    this (eventList, name, null);
    setFallbackRequestedServiceTime (requestedServiceTime);
  }

}
