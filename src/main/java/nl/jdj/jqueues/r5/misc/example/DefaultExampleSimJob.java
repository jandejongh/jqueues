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

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.listener.StdOutSimJQListener;
import nl.jdj.jsimulation.r5.SimEventList;

/** Implementation of {@link SimJob} used (as starting point) in (most of) the examples.
 * 
 * <p>
 * Each job has a public index 'n', set upon construction ({@code n > 0}).
 * The requested service time for the job equals its index.
 * This is merely to create interesting examples.
 * 
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
public class DefaultExampleSimJob<J extends SimJob, Q extends SimQueue>
extends AbstractSimJob<J, Q>
{
 
  /** Whether or not this job reports main queue operations to {@link System#out}
   *  through a {@link StdOutSimJQListener}.
   * 
   */
  private final boolean reported;
    
  /** The index of the job, strictly positive.
   * 
   */
  public final int n;

  /** Creates a new {@link DefaultExampleSimJob}.
   * 
   * <p>
   * The {@link SimJob} created is <i>not</i> attached to a {@link SimEventList} (i.e., it does not receive reset events
   * from the event list, nor does it have to; subclasses may override this).
   * 
   * @param reported Whether or not this job reports main queue operations to {@link System#out}.
   * @param n        The index of the job, strictly positive.
   * 
   */
  public DefaultExampleSimJob (final boolean reported, final int n)
  {
    super (null, Integer.toString (n));
    if (n <= 0)
      throw new IllegalArgumentException ();
    this.reported = reported;
    this.n = n;
    if (this.reported)
      registerSimEntityListener (new StdOutSimJQListener ());
  }

  /** Returns the index number as service time at given (any non-{@code null}) queue,
   *  unless the {@link SimJob#getServiceTime} contract orders otherwise.
   * 
   * @param queue The queue to visit; any non-{@code null} value of the argument
   *              returns the index number as requested service time.
   * 
   * @return The index number of this job, or zero if mandated by the {@link SimJob#getServiceTime} contract.
   * 
   */
  @Override
  public double getServiceTime (final SimQueue queue)
  {
    if (queue == null && getQueue () == null)
      return 0.0;
    else
      return (double) n;
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  /** Returns {@code "DefaultExampleSimJob"}.
   * 
   * @return {@code "DefaultExampleSimJob"}.
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DefaultExampleSimJob";
  }
  
}
