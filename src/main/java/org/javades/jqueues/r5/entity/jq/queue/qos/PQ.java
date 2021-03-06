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
package org.javades.jqueues.r5.entity.jq.queue.qos;

import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy;
import org.javades.jsimulation.r5.SimEventList;

/** The Priority-Queueing queueing discipline with a single server and infinite buffer size.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
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
public class PQ<J extends SimJob, Q extends PQ, P extends Comparable>
extends AbstractPreemptiveSimQueueQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a Priority Queue with infinite buffer size and a single server,
   *  given an event list, preemption strategy, and QoS structure.
   *
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * @param qosClass           The Java class to use for QoS behavior, non-{@code null}.
   * @param defaultJobQoS      The default QoS value to use for non-QoS jobs, non-{@code null}. 
   *
   * @throws IllegalArgumentException If the event list or one or both QoS arguments is <code>null</code>.
   *
   */
  public PQ
  (final SimEventList eventList,
    final PreemptionStrategy preemptionStrategy,
    final Class<P> qosClass,
    final P defaultJobQoS)
  {
    super (eventList, Integer.MAX_VALUE, 1, preemptionStrategy, qosClass, defaultJobQoS);
  }
  
  /** Returns a new {@link PQ} object on the same {@link SimEventList} with the same preemption strategy and QoS structure.
   * 
   * @return A new {@link PQ} object on the same {@link SimEventList} with the same preemption strategy and QoS structure.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * @see #getQoSClass
   * @see #getDefaultJobQoS
   * 
   */
  @Override
  public PQ<J, Q, P> getCopySimQueue ()
  {
    return new PQ (getEventList (), getPreemptionStrategy (), getQoSClass (), getDefaultJobQoS ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "PQ[preemption strategy]".
   * 
   * @return "PQ[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "PQ[" + getPreemptionStrategy () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE: JOBS QoS MAP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final NavigableMap<P, Set<J>> jobsQoSMap = new TreeMap<> ();
  
  @Override
  public final NavigableMap<P, Set<J>> getJobsQoSMap ()
  {
    return this.jobsQoSMap;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clear {@link #jobsQoSMap}.
   * 
   * @see #jobsQoSMap
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.jobsQoSMap.clear ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job into {@link #jobsQoSMap}.
   * 
   * @see  SimQueueQoSUtils#getAndCheckJobQoS
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (! this.jobsQoSMap.containsKey (qos))
      this.jobsQoSMap.put (qos, new LinkedHashSet<> ());
    this.jobsQoSMap.get (qos).add (job);
  }

  /** Starts the arrived job immediately if it is the executable job (respecting server-access credits) with highest priority.
   * 
   * @see #getExecutableJobWithHighestPriority
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (job, this);
    if (! this.jobsQoSMap.get (qos).contains (job))
      throw new IllegalStateException ();
    if (getExecutableJobWithHighestPriority () == job)
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER ACCCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
  }

  /** Invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    reschedule ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if there are no jobs present in the service area.
   * 
   * @return True if there are no jobs present in the service area.
   * 
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return getNumberOfJobsInServiceArea () == 0;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates {@link #remainingServiceTime}.
   * 
   * @see #getServiceTimeForJob
   * @see #remainingServiceTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! isJob (job))
    || isJobInServiceArea (job)
    || this.remainingServiceTime.containsKey (job))
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    this.remainingServiceTime.put (job, jobServiceTime);
  }

  /** Invokes {@link #reschedule}.
   * 
   * @see #remainingServiceTime
   * @see #reschedule
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! isJob (job))
    || (! isJobInServiceArea (job))
    || (! this.remainingServiceTime.containsKey (job)))
      throw new IllegalArgumentException ();
    final double jobServiceTime = this.remainingServiceTime.get (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    reschedule ();
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
  
  /** Removes the job from internal administration and cancels a pending departure event for it.
   * 
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see SimQueueQoSUtils#getAndCheckJobQoS
   * @see #getJobsQoSMap
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit (final J exitingJob, final double time)
  {
    if (exitingJob == null || ! isJob (exitingJob))
      throw new IllegalArgumentException ();
    if (isJobInServiceArea (exitingJob))
    {
      if (! this.remainingServiceTime.containsKey (exitingJob))
        throw new IllegalStateException ();
      this.remainingServiceTime.remove (exitingJob);
      if (this.jobsBeingServed.containsKey (exitingJob))
      {
        // Note: getDepartureEvents requires its argument to be present in this.jobQueue!
        if (! getDepartureEvents (exitingJob).isEmpty ())
        {
          if (getDepartureEvents (exitingJob).size () > 1)
            throw new IllegalStateException ();
          cancelDepartureEvent (exitingJob);
        }
        this.jobsBeingServed.remove (exitingJob);
      }
    }
    final P qos = SimQueueQoSUtils.getAndCheckJobQoS (exitingJob, this);
    if (! this.jobsQoSMap.containsKey (qos))
      throw new IllegalStateException ();
    if (! this.jobsQoSMap.get (qos).contains (exitingJob))
      throw new IllegalStateException ();
    this.jobsQoSMap.get (qos).remove (exitingJob);
    if (this.jobsQoSMap.get (qos).isEmpty ())
      this.jobsQoSMap.remove (qos);
  }
  
  /** Invokes {@link #reschedule}.
   * 
   */
  @Override
  protected final void rescheduleAfterExit (final double time)
  {
    reschedule ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the job (if any) eligible for execution that has highest priority, respecting server-access credits.
   * 
   * <p>
   * This method effectively determines the job that <i>should</i> be in service in {@link PQ}
   * by examining (solely)
   * {@link #getJobs},
   * {@link #jobsQoSMap},
   * {@link #getJobsInServiceArea},
   * and {@link #hasServerAcccessCredits}.
   * 
   * <p>
   * Note that ties between executable jobs with equal QoS values are broken by the order in which the jobs appear
   * in an iterator over the applicable value set in {@link #jobsQoSMap}
   * (in the obvious sense that the job that first appears in the iterator is given highest priority).
   * 
   * <p>
   * This method does (some) sanity checks on {@link #jobsQoSMap} on the fly.
   * 
   * @return The job (if any) eligible for execution that has highest priority, respecting server-access credits;
   *         {@code null} if there is no such job.
   * 
   */
  protected final J getExecutableJobWithHighestPriority ()
  {
    for (final Set<J> jobsP: this.jobsQoSMap.values ())
      if (jobsP == null || jobsP.isEmpty ())
        throw new IllegalStateException ();
      else
        for (final J job : jobsP)
          if (job == null || ! isJob (job))
            throw new IllegalStateException ();
          else if (isJobInServiceArea (job) || hasServerAcccessCredits ())
            return job;
    return null;
  }
  
  /** Reschedules through assessment of which job to serve.
   * 
   * <p>
   * Repeatedly (until they match) confronts
   * the job to serve as obtained through {@link #getExecutableJobWithHighestPriority}
   * with the job currently in service (the only job in {@link #jobsBeingServed}).
   * If there is a mismatch, and if there is a job currently being served,
   * it preempts the latter job through {@link #preemptJob}, and recurs.
   * Otherwise, if there is a mismatch but no job is currently being served,
   * it starts {@link #getExecutableJobWithHighestPriority}
   * either by {@link #start} if the job is not already in the service area,
   * or by {@link #startServiceChunk} otherwise.
   * 
   * @see #jobsBeingServed
   * @see #getExecutableJobWithHighestPriority
   * @see #preemptJob
   * @see #isJobInServiceArea
   * @see #start
   * @see #startServiceChunk
   * 
   */
  protected final void reschedule ()
  {
    if (this.jobsBeingServed.keySet ().size () > 1)
      throw new IllegalStateException ();
    final J jobBeingServed = (this.jobsBeingServed.isEmpty () ? null : this.jobsBeingServed.keySet ().iterator ().next ());
    final J jobToServe = getExecutableJobWithHighestPriority (); // Considers server-access credits!
    if (jobBeingServed != null && jobToServe == null)
      throw new IllegalStateException ();
    if (jobBeingServed != jobToServe)
    {
      if (jobBeingServed != null)
      {
        // Note that preemptJob may already reschedule in case of DROP and DEPART preemption policies (for instance)!
        preemptJob (getLastUpdateTime (), jobBeingServed);
        reschedule ();
      }
      else
      {
        if (! isJobInServiceArea (jobToServe))
          start (getLastUpdateTime (), jobToServe);
        else
          startServiceChunk (getLastUpdateTime (), jobToServe);
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
