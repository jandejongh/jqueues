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
package nl.jdj.jqueues.r5.entity.jq.queue.preemptive;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The single-server Preemptive Last-Come First-Served (P_LCFS) queueing discipline.
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
public class P_LCFS<J extends SimJob, Q extends P_LCFS>
extends AbstractPreemptiveSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server preemptive LCFS queue with infinite buffer size given an event list and preemption strategy.
   *
   * @param eventList          The event list to use.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   *
   */
  public P_LCFS (final SimEventList eventList, final PreemptionStrategy preemptionStrategy)
  {
    super (eventList, Integer.MAX_VALUE, 1, preemptionStrategy);
  }
  
  /** Returns a new (preemptive) {@link P_LCFS} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @return A new (preemptive) {@link P_LCFS} object on the same {@link SimEventList} and the same preemption strategy.
   * 
   * @see #getEventList
   * @see #getPreemptionStrategy
   * 
   */
  @Override
  public P_LCFS<J, Q> getCopySimQueue ()
  {
    return new P_LCFS<> (getEventList (), getPreemptionStrategy ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "P_LCFS[preemption strategy]".
   * 
   * @return "P_LCFS[preemption strategy]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "P_LCFS[" + getPreemptionStrategy () + "]";
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
  
  /** Calls super method and clears the internal LIFO queues.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.lifoJobQueue.clear ();
    this.lifoWaitQueue.clear ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LIFO JOB AND LIFO WAITING QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<J> lifoJobQueue = new ArrayList<> ();
  
  private final List<J> lifoWaitQueue = new ArrayList<> ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the head of an internal LIFO job queue (for selecting which job to resume)
   *  and at the head of an internal LIFO wait queue (for selecting which job to start).
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.lifoJobQueue.add (0, job);
    this.lifoWaitQueue.add (0, job);
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
    if (! isJob (job))
      throw new IllegalStateException ();
    if (isJobInServiceArea (job))
      throw new IllegalStateException ();
    if (this.lifoJobQueue.get (0) != job || this.lifoWaitQueue.get (0) != job)
      throw new IllegalStateException ();
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

  /** Starts jobs as long as there are server-access credits and jobs waiting.
   * 
   * <p>
   * Jobs are started in reverse order of arrival through the use of an internal LIFO wait queue.
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
      start (time, this.lifoWaitQueue.get (0));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code true}.
   * 
   * @return {@code true}.
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
  
  /** Performs sanity checks, removes the job from the internal LIFO wait queue, and administers its remaining service time.
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
    if (this.lifoWaitQueue.get (0) != job)
      throw new IllegalStateException ();
    this.lifoWaitQueue.remove (0);
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    this.remainingServiceTime.put (job, jobServiceTime);
  }

  /** Schedules the started job for immediate execution if it is the only job in the service area
   *  or if its arrival time is strictly smaller than the arrival time of a job in execution,
   *  the latter of which is then preempted.
   * 
   * <p>
   * The reverse order of arrival time is achieved with the internal LIFO job queue.
   * 
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
    if (this.jobsBeingServed.containsKey (job))
      throw new IllegalStateException ();
    if (this.jobsBeingServed.size () > 1)
      throw new IllegalStateException ();
    // Find the job currently being served; if any.
    final J jobBeingServed = (this.jobsBeingServed.isEmpty () ? null : this.jobsBeingServed.keySet ().iterator ().next ());
    if (jobBeingServed == null || this.lifoJobQueue.indexOf (job) < this.lifoJobQueue.indexOf (jobBeingServed))
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
  
  /** Removes the job from internal administration (a.o., LIFO job and LIFO wait queues)
   *  and cancels any pending departure event for it.
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
    if (! this.lifoJobQueue.remove (exitingJob))
      throw new IllegalStateException ();
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
    else if (! this.lifoWaitQueue.remove (exitingJob))
      throw new IllegalStateException ();
  }
  
  /** If there are jobs in the service area but none in execution,
   *  starts a service-chunk for the job in the service area
   *  that arrived last at the queue.
   * 
   * <p>
   * The reverse order of arrival time is achieved with the internal LIFO job queue.
   * 
   * @see #startServiceChunk
   * 
   */
  @Override
  protected final void rescheduleAfterExit (final double time)
  {
    if (this.jobsBeingServed.isEmpty () && hasJobsInServiceArea ())
    {
      // Iterate over lifoJobQueue, and starts a service chunk for the first job found that is in the service area.
      J jobToStartServiceChunk = null;
      for (final J j : this.lifoJobQueue)
        if (isJobInServiceArea (j))
        {
          jobToStartServiceChunk = j;
          break;
        }
      if (jobToStartServiceChunk == null)
        throw new IllegalStateException ();
      startServiceChunk (time, jobToStartServiceChunk);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
