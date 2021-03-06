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
package org.javades.jqueues.r5.entity.jq.queue.nonpreemptive;

import org.javades.jqueues.r5.entity.jq.SimQoS;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DROP;
import org.javades.jsimulation.r5.SimEventList;

/** The {@link NoBuffer_c} queueing system serves jobs with multiple servers but has no buffer space (i.c., no wait queue).
 *
 * <p>
 * Jobs that arrive while all servers are busy are dropped.
 * 
 * <p>
 * Although the queue will work with zero servers, the optimized {@link DROP} queuing system is specially
 * designed for "no-server no-buffer".
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see DROP
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
public class NoBuffer_c<J extends SimJob, Q extends NoBuffer_c>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a NoBuffer_c queue given an event list.
   *
   * @param eventList The event list to use.
   * @param c         The (non-negative) number of servers.
   *
   * @throws IllegalArgumentException If the number of servers is strictly negative.
   * 
   */
  public NoBuffer_c (final SimEventList eventList, final int c)
  {
    super (eventList, 0, c);
  }
  
  /** Returns a new {@link NoBuffer_c} object on the same {@link SimEventList} with the same number of servers.
   * 
   * @return A new {@link NoBuffer_c} object on the same {@link SimEventList} with the same number of servers.
   * 
   * @see #getEventList
   * @see #getNumberOfServers
   * 
   */
  @Override
  public NoBuffer_c<J, Q> getCopySimQueue ()
  {
    return new NoBuffer_c<> (getEventList (), getNumberOfServers ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "NoBuffer_numberOfServers".
   * 
   * @return "NoBuffer_numberOfServers".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "NoBuffer_" + getNumberOfServers ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Invokes super method and makes method implementation final.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    super.insertJobInQueueUponArrival (job, time);
  }

  /** Throws an exception.
   * 
   * @param  arrivingJob The arriving job.
   * @param  time        The arrival time.
   * 
   * @return This method does not return.
   * 
   * @throws IllegalStateException As invocation of this method is unexpected (there is no buffer).
   * 
   */
  @Override
  protected final J selectJobToDropAtFullQueue (final J arrivingJob, final double time)
  {
    if (getNumberOfJobsInWaitingArea () != 1 || ! isJobInWaitingArea (arrivingJob))
      throw new IllegalStateException ();
    return arrivingJob;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the result from {@link #getFirstJobInWaitingArea}.
   * 
   * @return The result from {@link #getFirstJobInWaitingArea}.
   * 
   */
  @Override
  protected final J selectJobToStart ()
  {
    return getFirstJobInWaitingArea ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return super.getServiceTimeForJob (job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit  (final J exitingJob, final double time)
  {
    super.removeJobFromQueueUponExit (exitingJob, time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
