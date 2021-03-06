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
package org.javades.jqueues.r5.misc.example;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.job.selflistening.DefaultSelfListeningSimJob;
import org.javades.jqueues.r5.entity.jq.job.selflistening.DefaultSelfListeningSimJobFactory;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.DefaultVisitsLoggingSimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.DefaultVisitsLoggingSimJobFactory;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.listener.StdOutSimJobListener;
import org.javades.jqueues.r5.listener.StdOutSimQueueListener;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;

/** Example code for <code>nl.jdj.jqueues.util.jobfactory</code>.
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
public final class JobFactoryExample
{
  
  /** Prevents instantiation.
   * 
   */
  private JobFactoryExample ()
  {
  }
  
  /** Main method.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.util.jobfactory PACKAGE ===");
    // Create an event list and attach a new FCFS queue to it.
    final SimEventList eventList = new DefaultSimEventList<> (DefaultSimEvent.class);
    final SimQueue<SimJob, FCFS> queue = new FCFS (eventList);
    // Create a factory for self-listening jobs.
    final SimJobFactory<DefaultSelfListeningSimJob, SimQueue> factory_1 = new DefaultSelfListeningSimJobFactory<> ();
    // Create a job that is not attached to the event list, with default service-time requirement.
    final SimJob job1_1 = factory_1.newInstance (null, "Job-1-1", null);
    // Turn on logging to System.out for the job.
    job1_1.registerSimEntityListener (new StdOutSimJobListener ());
    // Schedule it for arrival at the queue at t=14.
    // Notice that we have to smuggle a bit to get access to the convenience method on AbstractSimQueue.
    ((AbstractSimQueue) queue).scheduleJobArrival (14, job1_1);
    // Do another one; attaching it to the event list, and listening to the queue instead.
    // Schedule the registration of the queue listener on the event list.
    // This is not really recommended...
    eventList.schedule (20.0, (SimEventAction) (final SimEvent event) ->
    {
      queue.registerSimEntityListener (new StdOutSimQueueListener ());
    });
    final SimJob job1_2 = factory_1.newInstance (eventList, "Job-1-2", null);
    ((AbstractSimQueue) queue).scheduleJobArrival (27, job1_2);
    // Do another one with different service time using a service-time map.
    // The null key means the service time is for all queues visited by the job.
    final Map<SimQueue, Double> serviceTimeMap = new HashMap <> ();
    serviceTimeMap.put (null, 9.5);
    final SimJob job1_3 = factory_1.newInstance (eventList, "Job-1-3", serviceTimeMap);
    ((AbstractSimQueue) queue).scheduleJobArrival (40, job1_3);
    // Create a factory for a visits-logging job and create an instance.
    final SimJobFactory<DefaultVisitsLoggingSimJob, SimQueue> factory_2 = new DefaultVisitsLoggingSimJobFactory<> ();
    final DefaultVisitsLoggingSimJob job2_1 = factory_2.newInstance (eventList, "Job-2-1", null);
    // Schedule the visits-logging job.
    ((AbstractSimQueue) queue).scheduleJobArrival (60, job2_1);
    eventList.run ();
    // Retrieve the visit logs.
    final TreeMap<Double, TreeMap<Integer, JobQueueVisitLog>> visitLogMap = job2_1.getVisitLogs ();
    for (final double arrivalTime : visitLogMap.keySet ())
      for (final int sequenceNumber : visitLogMap.get (arrivalTime).keySet ())
      {
        System.out.println ("Obtained visit log, arrivalTime=" + arrivalTime + ", sequenceNumber=" + sequenceNumber + ":");
        visitLogMap.get (arrivalTime).get (sequenceNumber).print (null);
      }
  }
  
}