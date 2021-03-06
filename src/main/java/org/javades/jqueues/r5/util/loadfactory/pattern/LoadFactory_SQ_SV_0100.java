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
package org.javades.jqueues.r5.util.loadfactory.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQEventScheduler;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.extensions.qos.SimJobQoS;
import org.javades.jqueues.r5.util.loadfactory.AbstractLoadFactory_SQ_SV;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import org.javades.jsimulation.r5.SimEventList;

/** A concrete {@link LoadFactory_SQ_SV}, pattern 0100.
 *
 * @see #generate
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public class LoadFactory_SQ_SV_0100<J extends SimJob, Q extends SimQueue>
extends AbstractLoadFactory_SQ_SV<J, Q>
{

  @Override
  public String getDescription ()
  {
    return "Jobs with U[0.95, 9.5] requested service times and random Double QoS values (incl. +/- infinity).";
  }

  private final Random rngRequestedServiceTime = new Random ();
  
  /** Creates a suitable map for the requested service time for a job visit to a queue.
   * 
   * @param queue The queue.
   * @param n     The job number.
   * 
   * @return A map holding the service time (U[0.5, 9.5]) at the queue.
   * 
   * @see SimJobFactory#newInstance For the use of the map generated.
   * 
   */
  protected Map<Q, Double> generateRequestedServiceTimeMap (final Q queue, final int n)
  {
    final Map<Q, Double> requestedServiceTimeMap = new HashMap ();
    final double requestedServiceTime = 0.5 + 9 * this.rngRequestedServiceTime.nextDouble ();
    requestedServiceTimeMap.put (queue, requestedServiceTime);
    return requestedServiceTimeMap;
  }
  
  /** Generates the load.
   * 
   * <p>
   * This method
   * <ul>
   * <li> generates the requested number of jobs, and number them starting with one;
   * <li> draws from U[0.5, 9.5] the requested service time for each job;
   * <li> sets the QoS class to {@link Double} for each job;
   * <li> sets the QoS value to one of ten preselected yet random double values
   *      (however, including 0, {@link Double#NEGATIVE_INFINITY}, and {@link Double#POSITIVE_INFINITY}).
   * <li> schedules a single arrival for each job at time equal to its job number.
   * </ul>
   * 
   * <p>
   * Jobs are returned in a {@link LinkedHashSet}, preserving the creation order of the jobs.
   * 
   * @see SimJQEventScheduler#scheduleJQ
   * 
   */
  @Override
  public Set<J> generate
  (final SimEventList eventList,
    boolean attachSimJobsToEventList,
    final Q queue,
    final SimJobFactory<J, Q> jobFactory,
    final int numberOfJobs,
    final boolean reset,
    final double resetTime,
    final Set<LoadFactoryHint> hints,
    final NavigableMap<Double, Set<SimJQEvent>> queueExternalEvents)
  {
    if (eventList == null || queue == null || jobFactory == null)
      throw new IllegalArgumentException ();
    if (numberOfJobs < 0)
      throw new IllegalArgumentException ();
    final Set<J> jobs = new LinkedHashSet<> ();
    final NavigableMap<Double, Set<SimJQEvent>> realQueueExternalEvents =
      ((queueExternalEvents != null) ? queueExternalEvents : new TreeMap<> ());
    final Set<SimJQEvent<J, Q>> eventsToSchedule = new LinkedHashSet<> ();
    final SimEventList jobEventList = (attachSimJobsToEventList ? eventList : null);
    final List<Double> qosList = new ArrayList<> ();
    qosList.add (null);
    final Random rngQoS = new Random ();
    for (int i = 0; i < 7; i++)
      qosList.add (2.0 * (- 0.5 * Double.MAX_VALUE + Double.MAX_VALUE * rngQoS.nextDouble ()));
    qosList.add (0.0);
    qosList.add (Double.NEGATIVE_INFINITY);
    qosList.add (Double.POSITIVE_INFINITY);
    final Random rngQoSSelect = new Random ();
    for (int i = 1; i <= numberOfJobs; i++)
    {
      final J job = jobFactory.newInstance (jobEventList, Integer.toString (i), generateRequestedServiceTimeMap (queue, i));
      ((SimJobQoS) job).setQoSClass (Double.class);
      ((SimJobQoS) job).setQoS (qosList.get (rngQoSSelect.nextInt (qosList.size ())));
      final SimJQEvent<J, Q> arrivalSchedule = new SimJQEvent.Arrival<> (job, queue, (double) i);
      if (! realQueueExternalEvents.containsKey ((double) i))
        realQueueExternalEvents.put ((double) i, new LinkedHashSet<> ());
      realQueueExternalEvents.get ((double) i).add (arrivalSchedule);
      eventsToSchedule.add (arrivalSchedule);
      jobs.add (job);
    }
    SimJQEventScheduler.scheduleJQ (eventList, reset, resetTime, eventsToSchedule);
    return jobs;
  }
  
}
