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
package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link IS_CST} queue serves all jobs simultaneously with fixed job-independent service times.
 *
 * <p>
 * Infinite Server with Constant Service Times.
 *
 * <P>
 * Each job has the same queue-determined service time.
 * The job is <i>not</i> consulted for its service time through {@link SimJob#getServiceTime}.
 * 
 * <p>
 * In the presence of vacations, i.e., jobs are not immediately admitted to the servers,
 * this implementation respects the arrival order of jobs.
 *
 * <p>
 * For jobs with identical arrival times, it is <i>not</i> guaranteed that they will depart in order of arrival.
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
public class IS_CST<J extends SimJob, Q extends IS_CST>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link IS_CST} queue with given {@link SimEventList} and (fixed) service time.
   * 
   * @param eventList The event list to use.
   * @param serviceTime The service time for all jobs.
   * 
   * @throws IllegalArgumentException If <code>serviceTime</code> is strictly negative.
   * 
   */
  public IS_CST (final SimEventList eventList, final double serviceTime)
  {
    super (eventList, Integer.MAX_VALUE, Integer.MAX_VALUE);
    if (serviceTime < 0)
      throw new IllegalArgumentException ();
    this.serviceTime = serviceTime;
  }
  
  /** Returns a new {@link IS_CST} object on the same {@link SimEventList} with the same service time.
   * 
   * @return A new {@link IS_CST} object on the same {@link SimEventList} with the same service time.
   * 
   * @see #getEventList
   * @see #getServiceTime
   * 
   */
  @Override
  public IS_CST<J, Q> getCopySimQueue ()
  {
    return new IS_CST<> (getEventList (), getServiceTime ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "IS_CST[service time]".
   * 
   * @return "IS_CST[service time]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "IS_CST[" + getServiceTime () + "]";
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
  
  /** Throws an exception.
   * 
   * @param  arrivingJob The arriving job.
   * @param  time        The arrival time.
   * 
   * @return This method does not return.
   * 
   * @throws IllegalStateException As invocation of this method is unexpected (buffer cannot be full).
   * 
   */
  @Override
  protected final J selectJobToDropAtFullQueue (final J arrivingJob, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double serviceTime;
  
  /** Returns the service time for all jobs.
   * 
   * @return The service time for all jobs, non-negative.
   * 
   */
  public final double getServiceTime ()
  {
    return this.serviceTime;
  }
  
  /** Returns the service time for all jobs.
   * 
   * @return The service time for all jobs as obtained through {@link #getServiceTime()}.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return getServiceTime ();
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
