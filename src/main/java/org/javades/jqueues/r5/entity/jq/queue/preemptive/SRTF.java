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
package org.javades.jqueues.r5.entity.jq.queue.preemptive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEventList;

/** The single-server preemptive Shortest-Remaining (Service) Time First (SRTF) queueing discipline.
 *
 * <p>
 * In SRTF, the job present (and admitted to the server in view of server-access credits) with
 * the minimum remaining service time is in service until completion.
 * In case of a tie between a job entering the service area and the job currently in service,
 * the job in service is <i>not</i> preempted.
 * In case of a tie between multiple jobs in the service area,
 * the jobs are served in arrival order.
 * 
 * <p>
 * This implementation admits waiting jobs to the service area (server) as soon as server-access credits are available,
 * irrespective of their remaining (i.c., required) service time,
 * in other words,
 * jobs are not held in the waiting area because their required service time is larger
 * that the remaining service time of the job currently in execution.
 * Thus, once admitted to the service area, jobs may have to wait <i>there</i>
 * until being served/executed exclusively by the (single) server.
 * 
 * <p>
 * Jobs <i>are</i>, however, admitted to the service area in increasing order of required service time.
 * In case of a tie, jobs are admitted in order of arrival.
 * (This is only relevant to the case of the grant of limited server-access credits; fewer that the number of jobs waiting.)
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
public class SRTF<J extends SimJob, Q extends SRTF>
extends AbstractPreemptiveSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive SRTF queue with infinite buffer size given an event list and preemption strategy.
   *
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public SRTF (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, Integer.MAX_VALUE, 1, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link SRTF} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public SRTF<J, Q> getCopySimQueue ()
  {
    return new SRTF<> (getEventList (), getPreemptionStrategy ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SRTF[preemption strategy]".
   * 
   * @return "SRTF[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SRTF[" + getPreemptionStrategy () + "]";
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
  
  /** Calls super method and clears the internal SRTF queue.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.srtfWaitingQueue.clear ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SRTF WAITING QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<J> srtfWaitingQueue = new ArrayList<> ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the head of an internal SRTF wait queue; maintaining non-decreasing (required) service-time ordering.
   * 
   * <p>
   * In case of ties, jobs are inserted in order of arrival.
   * 
   * @see #getServiceTimeForJob
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    int newPosition = 0;
    while (newPosition < this.srtfWaitingQueue.size ()
      && getServiceTimeForJob (this.srtfWaitingQueue.get (newPosition)) <= getServiceTimeForJob (job))
      newPosition++;
    this.srtfWaitingQueue.add (newPosition, job);    
  }
  
  /** Starts the arrived job if server-access credits are available.
   * 
   * @see #hasServerAcccessCredits
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null || ! isJobInWaitingArea (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
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

  /** Starts jobs in increasing service-time ordering as long as there are server-access credits and jobs waiting.
   * 
   * <p>
   * Jobs are started in increasing order of required service time through the use of an internal SRTF (SJF) wait queue.
   * In case of a tie, jobs are admitted to the service area in order of arrival.
   *
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, this.srtfWaitingQueue.get (0));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code>.
   * 
   * @return True.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
 /** Performs sanity checks, removes the job from the internal SRTF/SJF wait queue, and administers its remaining service time.
   * 
   * @see #getServiceTimeForJob
   * @see #remainingServiceTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || getJobsInServiceArea ().contains (job)
    || this.remainingServiceTime.containsKey (job))
      throw new IllegalArgumentException ();
    if (this.srtfWaitingQueue.get (0) != job)
      throw new IllegalStateException ();
    this.srtfWaitingQueue.remove (0);
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    this.remainingServiceTime.put (job, jobServiceTime);
  }

  /** Schedules the started job for immediate execution if it is the only job in the service area
   *  or if its (remaining) service time is strictly smaller than the remaining service of a job in execution,
   *  the latter of which is then preempted.
   * 
   * @see #remainingServiceTime
   * @see #preemptJob
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job))
    || ! this.remainingServiceTime.containsKey (job))
      throw new IllegalArgumentException ();
    final double jobServiceTime = this.remainingServiceTime.get (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    // Get the job currently being served; if any.
    final J jobBeingServed;
    if (! this.jobsBeingServed.isEmpty ())
    {
      if (this.jobsBeingServed.size () > 1)
        throw new IllegalStateException ();
      jobBeingServed = this.jobsBeingServed.keySet ().iterator ().next ();
    }
    else
      jobBeingServed = null;
    if (job == jobBeingServed)
      throw new IllegalStateException ();
    // Check whether job is eligible for (immediate) execution.
    if (jobBeingServed == null || jobServiceTime < this.remainingServiceTime.get (jobBeingServed))
    {
      // The job is eligible for immediate execution, hence we must preempt the job currently being executed.
      if (jobBeingServed != null)
        preemptJob (time, jobBeingServed);
      // The preemption could have scheduled 'job' already, so make sure we check!
      if (this.jobsBeingServed.isEmpty ())
        startServiceChunk (time, job);
      else if (this.jobsBeingServed.size () > 1)
        throw new IllegalStateException ();
      else if (this.jobsBeingServed.keySet ().iterator ().next () != job)
        throw new IllegalStateException ();
    }
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
  // EXIT (DEPARTURE / DROP / REVOKATION)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from the internal data structures (a.o., the SRTF/SJF wait queue)
   *  and removes its departure event, if needed.
   * 
   * @see #remainingServiceTime
   * @see #jobsBeingServed
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit (final J exitingJob, final double time)
  {
    if (exitingJob == null || ! isJob (exitingJob))
      throw new IllegalArgumentException ();
    if (isJobInServiceArea (exitingJob))
    {
      if (this.srtfWaitingQueue.contains (exitingJob))
        throw new IllegalStateException ();
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
    else if (! this.srtfWaitingQueue.remove (exitingJob))
      throw new IllegalStateException ();
  }

  /** If there are jobs in the service area but none in execution,
   *  starts a service-chunk for the job in the service area
   *  with the minimum remaining service time.
   * 
   * @see #remainingServiceTime
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void rescheduleAfterExit (final double time)
  {
    if (this.jobsBeingServed.isEmpty () && hasJobsInServiceArea ())
    {
      final double sRST = this.remainingServiceTime.firstValue ();
      final Set<J> jobsWithSRST = this.remainingServiceTime.getPreImageForValue (sRST);
      if (jobsWithSRST.isEmpty ())
        throw new IllegalStateException ();
      startServiceChunk (time, jobsWithSRST.iterator ().next ());
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
