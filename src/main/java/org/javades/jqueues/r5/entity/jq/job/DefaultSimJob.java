/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.entity.jq.job;

import java.util.HashMap;
import java.util.Map;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEventList;

/** A reasonable first-order implementation of {@link SimJob} with support for naming, per-queue requested service times
 *  and a default service time.
 * 
 * <p>
 * All other methods in this class aim at influencing the result from the {@link SimJob#getServiceTime} implementation.
 * 
 * @param <J> The job type.
 * @param <Q> The queue type for jobs.
 *
 * @see DefaultSimJobFactory
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
public class DefaultSimJob<J extends DefaultSimJob, Q extends SimQueue>
extends AbstractSimJob<J, Q>
implements SimJob<J, Q>
{
  
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
      this.requestedServiceTimeMap = new HashMap<> ();
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
  public final static double DEFAULT_FALLBACK_REQUESTED_SERVICE_TIME = 1.0;
  
  /** The fallback requested service time, in case a value could not be obtained from the internal mapping of {@link SimQueue}s
   *  onto requested service times.
   * 
   */
  private double fallbackRequestedServiceTime = DefaultSimJob.DEFAULT_FALLBACK_REQUESTED_SERVICE_TIME;
  
  /** Returns the fallback requested service time,
   *  in case a value could not be obtained from the internal mapping of {@link SimQueue}s
   *  onto requested service times.
   * 
   * @return The fallback requested service time,
   *           in case a value could not be obtained from the internal mapping of {@link SimQueue}s
   *           onto requested service times.
   * 
   * @see #DEFAULT_FALLBACK_REQUESTED_SERVICE_TIME
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
   * Note that certain {@link SimQueue} types may <i>not</i> consult the jobs for the requested service time, but
   * decide it themselves!
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
  
  /** Sets the requested service-time for future visits to a specific queue.
   * 
   * <p>
   * This method is intended to be used before the job is actually used in a simulation.
   * For instance, note that the setting imposed by this method survives resets of the job.
   * 
   * @param queue       The queue, non-{@code null}.
   * @param serviceTime The requested service time for that queue, zero or positive.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or the requested service time is strictly negative.
   * @throws IllegalStateException    If the queue is the currently visited queue.
   * 
   * @see #getServiceTime
   * @see #resetEntity
   * @see #getQueue
   * 
   */
  public final void setRequestedServiceTimeMappingForQueue (final Q queue, final double serviceTime)
  {
    if (queue == null || serviceTime < 0)
      throw new IllegalArgumentException ();
    if (getQueue () == queue)
      throw new IllegalStateException ();
    this.requestedServiceTimeMap.put (queue, serviceTime);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
