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

import java.util.ArrayList;
import java.util.List;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.IS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import org.javades.jqueues.r5.util.stat.AutoSimQueueStat;
import org.javades.jqueues.r5.util.stat.AutoSimQueueStatEntry;
import org.javades.jqueues.r5.util.stat.SimQueueProbe;
import org.javades.jqueues.r5.util.stat.SimpleSimQueueStat;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;

/** Example code for <code>nl.jdj.jqueues.stat</code>.
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
public final class StatExample
{
  
  /** Prevents instantiation.
   * 
   */
  private StatExample ()
  {
  }
  
  private static class MyAutoSimQueueStat
  extends AutoSimQueueStat<SimJob, SimQueue>
  {
    
    private static List<AutoSimQueueStatEntry<SimQueue>> createEntries ()
    {
      List<AutoSimQueueStatEntry<SimQueue>> list = new ArrayList<> ();
      list.add (new AutoSimQueueStatEntry<> ("number of jobs", new SimQueueProbe<SimQueue>  ()
      {

        @Override
        public double get (SimQueue queue)
        {
          return queue.getNumberOfJobs ();
        }
      }));
      list.add (new AutoSimQueueStatEntry<> ("number of jobs in service area", new SimQueueProbe<SimQueue>  ()
      {

        @Override
        public double get (SimQueue queue)
        {
          return queue.getNumberOfJobsInServiceArea ();
        }
      }));
      return list;
    }
    
    public MyAutoSimQueueStat (SimQueue queue)
    {
      super (queue, createEntries ());
    }
    
  }
  
  private static final SimEventList<DefaultSimEvent> EVENT_LIST = new DefaultSimEventList<> (DefaultSimEvent.class);
  
  private static final SimQueue FCFS_QUEUE = new FCFS (EVENT_LIST);
  private static final SimQueue LCFS_QUEUE = new LCFS (EVENT_LIST);
  private static final SimQueue IS_QUEUE = new IS (EVENT_LIST);
      
  /** Main method.
   * 
   * Creates a (reusable) event list, some queues and jobs and shows the main feature of the package.
   * Results are sent to {@link System#out}.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR nl.jdj.jqueues.stat PACKAGE ===");
    final int N = 100000;
    System.out.println ("-> Simulating queue with " + N + " jobs, arriving at 1, 2, 3, ...");
    System.out.println ("   Requesting service times 1, 2, 3, ...");
    System.out.println ("-> FCFS...");
    EVENT_LIST.reset (1);
    final List<DefaultExampleSimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= N; n++)
      jobList.add (new DefaultExampleSimJob (false, n));
    final SimpleSimQueueStat fcfsStat = new SimpleSimQueueStat (FCFS_QUEUE);
    final MyAutoSimQueueStat autoFcfsStat = new MyAutoSimQueueStat (FCFS_QUEUE);
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      EVENT_LIST.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          FCFS_QUEUE.arrive (arrTime, j);
        }
      }));
    }
    EVENT_LIST.run ();
    System.out.println ("   ==> SimpleSimQueueStat");
    System.out.println ("     ==> Average number of jobs in queue:  " + fcfsStat.getAvgNrOfJobs () + ".");
    System.out.println ("     ==> Average number of jobs in service area: " + fcfsStat.getAvgNrOfJobsInServiceArea () + ".");
    System.out.println ("   ==> AutoSimQueueStat");
    autoFcfsStat.report (9);
    System.out.println ("-> LCFS...");
    EVENT_LIST.reset (1);
    final SimpleSimQueueStat lcfsStat = new SimpleSimQueueStat (LCFS_QUEUE);
    final MyAutoSimQueueStat autoLcfsStat = new MyAutoSimQueueStat (LCFS_QUEUE);
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      EVENT_LIST.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          LCFS_QUEUE.arrive (arrTime, j);
        }
      }));
    }
    EVENT_LIST.run ();
    System.out.println ("   ==> SimpleSimQueueStat");
    System.out.println ("     ==> Average number of jobs in queue:  " + lcfsStat.getAvgNrOfJobs () + ".");
    System.out.println ("     ==> Average number of jobs in service area: " + lcfsStat.getAvgNrOfJobsInServiceArea () + ".");
    System.out.println ("   ==> AutoSimQueueStat");
    autoLcfsStat.report (9);
    System.out.println ("-> IS...");
    EVENT_LIST.reset (1);
    final SimpleSimQueueStat isStat = new SimpleSimQueueStat (IS_QUEUE);
    final MyAutoSimQueueStat autoIsStat = new MyAutoSimQueueStat (IS_QUEUE);
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      EVENT_LIST.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          IS_QUEUE.arrive (arrTime, j);
        }
      }));
    }
    EVENT_LIST.run ();
    System.out.println ("   ==> SimpleSimQueueStat");
    System.out.println ("     ==> Average number of jobs in queue:  " + isStat.getAvgNrOfJobs () + ".");
    System.out.println ("     ==> Average number of jobs in service area: " + isStat.getAvgNrOfJobsInServiceArea () + ".");
    System.out.println ("   ==> AutoSimQueueStat");
    autoIsStat.report (9);
    System.out.println ("=== FINISHED ===");
  }
  
}