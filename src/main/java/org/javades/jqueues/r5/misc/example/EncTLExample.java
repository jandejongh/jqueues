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
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import org.javades.jqueues.r5.entity.jq.queue.composite.enc.EncTL;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.RANDOM;
import org.javades.jqueues.r5.listener.StdOutSimQueueListener;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;

/** Example code for {@link EncTL}.
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
public final class EncTLExample
{
  
  /** Prevents instantiation.
   * 
   */
  private EncTLExample ()
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
    System.out.println ("=== EXAMPLE PROGRAM FOR  EncTL ===");
    System.out.println ();
    System.out.println ("-> Creating jobs...");
    final List<DefaultExampleSimJob> jobList = new ArrayList<>  ();
    for (int n = 1; n <= 10; n++)
      jobList.add (new DefaultExampleSimJob (false, n));
    System.out.println ("-> Creating event list...");
    final SimEventList<DefaultSimEvent> el = new DefaultSimEventList<> (DefaultSimEvent.class);
    System.out.println ("-> Creating FCFS queue...");
    final SimQueue fcfsQueue = new FCFS (el);
    // fcfsQueue.registerSimEntityListener (new StdOutSimQueueListener ());
    System.out.println ("-> Creating EncTL...");
    final DelegateSimJobFactory delegateSimJobFactory =
      (DelegateSimJobFactory<TestDelegateSimJob, SimQueue, DefaultExampleSimJob, SimQueue>)
        (double time, DefaultExampleSimJob job, SimQueue queue) -> new TestDelegateSimJob (job, false);
    final EncTL enctlQueue =
      new EncTL (el, fcfsQueue, delegateSimJobFactory, 20.1, 5.2, 21.3);
    final StdOutSimQueueListener encListener = new StdOutSimQueueListener ();
    encListener.setOnlyResetsAndUpdatesAndStateChanges (true);
    enctlQueue.registerSimEntityListener (encListener);
    for (final EncTL.ExpirationMethod expirationMethod :
      EncTL.ExpirationMethod.values ())
    {
      System.out.println ();
      System.out.println ("=== Setting expiration method to " + expirationMethod + ".");
      System.out.println ();
      enctlQueue.setExprirationMethod (expirationMethod);
      System.out.println ("-> Submitting jobs to EncTL...");
      for (int i = 0; i < jobList.size (); i++)
      {
        final SimJob j = jobList.get (i);
        j.resetEntity ();
        final double arrTime = i + 1;
        el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
        {
          enctlQueue.arrive (arrTime, j);
        }));
      }
      System.out.println ("-> Executing event list...");
      System.out.println ();
      el.run ();
      System.out.println ();
      System.out.println ("-> Resetting event list...");
      el.reset ();
    }
    System.out.println ();
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