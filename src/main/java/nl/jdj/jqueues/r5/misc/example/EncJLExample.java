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
package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.EncJL;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.RANDOM;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for {@link EncJL}.
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
public final class EncJLExample
{
  
  /** Prevents instantiation.
   * 
   */
  private EncJLExample ()
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
      else if (queue instanceof IS)
        return this.n + 0.05;
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
    System.out.println ("=== EXAMPLE PROGRAM FOR  EncJL ===");
    System.out.println ();
    System.out.println ("-> Creating jobs...");
    final List<DefaultExampleSimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new DefaultExampleSimJob (false, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    System.out.println ("-> Creating IS queue...");
    final SimQueue isQueue = new IS (el);
    // isQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating EncJL...");
    final DelegateSimJobFactory delegateSimJobFactory =
      (DelegateSimJobFactory<TestDelegateSimJob, SimQueue, DefaultExampleSimJob, SimQueue>)
        (double time, DefaultExampleSimJob job, SimQueue queue) -> new TestDelegateSimJob (job, false);
    final EncJL encjlQueue =
      new EncJL (el, isQueue, delegateSimJobFactory, 1, 1, 2);
    final StdOutSimQueueListener encListener = new StdOutSimQueueListener ();
    encListener.setOnlyResetsAndUpdatesAndStateChanges (true);
    encjlQueue.registerSimEntityListener (encListener);
    System.out.println ("-> Submitting jobs to EncJL...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      j.resetEntity ();
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        encjlQueue.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
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