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
package org.javades.jqueues.r5.entity.jq.queue.processorsharing;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;

/** The "catch-up" single-server {@link CUPS} queue serves all jobs with the least obtained service time simultaneously,
 *  equally distributing its service capacity among them.
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
public class CUPS<J extends SimJob, Q extends CUPS>
extends AbstractProcessorSharingSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOGGER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOGGER = Logger.getLogger (CUPS.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server CUPS queue with infinite buffer size given an event list.
   *
   * <p>
   * The constructor registers a pre-update hook that sets the virtual time upon updates.
   * 
   * @param eventList The event list to use.
   *
   * @see #registerPreUpdateHook
   * @see #updateObtainedServiceTime
   * 
   */
  public CUPS (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE, 1);
    registerPreUpdateHook (this::updateObtainedServiceTime);
  }
  
  /** Returns a new {@link CUPS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link CUPS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public CUPS<J, Q> getCopySimQueue ()
  {
    return new CUPS<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TOLERANCE IN OBTAINED SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The tolerance for rounding errors in the obtained service time.
   * 
   */
  public final static double TOLERANCE_OST = 1.0E-9;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "CUPS".
   * 
   * @return "CUPS".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "CUPS";
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
  // INTERNAL (STATE) ADMINISTRATION
  // - obtainedServiceTimeMap
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final NavigableMap<Double, Set<J>> obtainedServiceTimeMap = new TreeMap<> ();

  /** Gets the jobs currently in execution.
   * 
   * @return The jobs currently in execution (non-{@code null}).
   * 
   */
  protected final Set<J> getJobsExecuting ()
  {
    if (hasJobsInServiceArea ())
      return this.obtainedServiceTimeMap.firstEntry ().getValue ();
    else
      return Collections.EMPTY_SET;
  }
  
  /** Gets the number of jobs in execution.
   * 
   * @return The number of jobs in execution.
   * 
   */
  protected final int getNumberOfJobsExecuting ()
  {
    return getJobsExecuting ().size ();
  }
  
  /** Returns the minimum obtained service time (i.e., of all the jobs currently in execution).
   * 
   * @return The minimum obtained service time, non-negative.
   * 
   * @throws IllegalStateException If there are currently no jobs in execution (i.e., no jobs in the service area).
   * 
   * @see #hasJobsInServiceArea
   * 
   */
  protected final double getMinimumObtainedServiceTime ()
  {
    if (! hasJobsInServiceArea ())
      throw new IllegalStateException ();
    return this.obtainedServiceTimeMap.firstKey ();
  }
  
  /** Removes the given job, if present, from the internal administration.
   * 
   * <p>
   * Does <i>not</i> remove the job from super-class structures.
   * 
   * @param job The job to remove, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job is {@code null}.
   * 
   */
  protected final void removeJobFromInternalAdministration (final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    sanityInternalAdministration ();
    if (! isJobInServiceArea (job))
      return;
    final Iterator<Entry<Double, Set<J>>> ost_i = this.obtainedServiceTimeMap.entrySet ().iterator ();
    while (ost_i.hasNext ())
    {
      final Entry<Double, Set<J>> entry = ost_i.next ();
      final Set<J> jobs = entry.getValue ();
      if (jobs.contains (job))
      {
        jobs.remove (job);
        if (jobs.isEmpty ())
          ost_i.remove ();
        return;
      }
    }
    throw new IllegalStateException ();
  }
  
  /** Calculates the time until the next catch-up.
   * 
   * @return The time until the next catch-up.
   * 
   * @throws IllegalStateException If the current state of the queue does not allow a catch-up.
   * 
   */
  protected final double getTimeToCatchUp ()
  {
    sanityInternalAdministration ();
    if (this.obtainedServiceTimeMap.size () < 2)
      throw new IllegalStateException ();
    final Iterator<Entry<Double, Set<J>>> ost_i = this.obtainedServiceTimeMap.entrySet ().iterator ();
    final Entry<Double, Set<J>> entry_first = ost_i.next ();
    final Entry<Double, Set<J>> entry_second = ost_i.next ();
    final double ost_first = entry_first.getKey ();
    final double ost_second = entry_second.getKey ();
    final double diff_ost = ost_second - ost_first;
    if (diff_ost <= 0)
      throw new IllegalStateException ();
    return diff_ost * getNumberOfJobsExecuting ();
  }
  
  /** Performs sanity checks on the internal administration.
   * 
   * <p>
   * Used for debugging.
   * The checks, however, are quite expensive.
   * 
   * <p>
   * This method has no effect if all sanity checks pass.
   * 
   * <p>
   * Unless commented out due to code maturity and/or performance considerations,
   * invocations are logged at level {@link Level#FINER}.
   * 
   * @throws IllegalStateException If the internal administration is inconsistent.
   * 
   */
  protected final void sanityInternalAdministration ()
  {
    LOGGER.log (Level.FINER, "t={0}, sanity on {1}: jobsInServiceArea={2}; obtainedServiceTimeMap={3}.", new Object[]{
      getLastUpdateTime (),
      this,
      getJobsInServiceArea (),
      this.obtainedServiceTimeMap});
    if (this.obtainedServiceTimeMap == null)
      throw new IllegalStateException ();
    int numberOfJobsInObtainedServiceTimeMap = 0;
    for (final Set<J> jobs : this.obtainedServiceTimeMap.values ())
      numberOfJobsInObtainedServiceTimeMap += jobs.size ();
    if (numberOfJobsInObtainedServiceTimeMap != getNumberOfJobsInServiceArea ())
      throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clears the internal administration.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.obtainedServiceTimeMap.clear ();
    this.lastCatchUpTime = Double.NaN;
    // Note that we use eventsScheduled in order to automatically have departure and catch-up events cancelled.
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates the internal administration (i.c., obtained service times) for executing jobs in the service area.
   * 
   * <p>
   * This method is called as an pre-update hook, and not meant to be called from user code (in sub-classes).
   * It is left protected for {@code javadoc}.
   * 
   * <p>
   * This method automatically performs "catch-ups".
   * 
   * @param newTime The new time.
   * 
   * @see #hasJobsInServiceArea
   * @see #getJobsExecuting
   * @see #getLastUpdateTime
   * @see #registerPreUpdateHook
   * 
   */
  protected final void updateObtainedServiceTime (final double newTime)
  {
    if (newTime < getLastUpdateTime ())
      throw new IllegalStateException ();
    sanityInternalAdministration ();
    if (newTime == getLastUpdateTime ())
      return;
    if (hasJobsInServiceArea ())
    {
      final double ost_old = this.obtainedServiceTimeMap.firstKey ();
      final int numberOfJobsExecuting = this.obtainedServiceTimeMap.firstEntry ().getValue ().size ();
      final double ost_new = ost_old + ((newTime - getLastUpdateTime ()) / numberOfJobsExecuting);
      final Set<J> jobs_ost_old = this.obtainedServiceTimeMap.get (ost_old);
      if (this.obtainedServiceTimeMap.size () > 1)
      {
        final double ost_runner_up = this.obtainedServiceTimeMap.higherKey (ost_old);
        this.obtainedServiceTimeMap.remove (ost_old);
        if (ost_new > ost_runner_up + CUPS.TOLERANCE_OST)
          throw new IllegalStateException ();
        else if (ost_new >= ost_runner_up - CUPS.TOLERANCE_OST)
        {
          // We have "catch-up".
          this.obtainedServiceTimeMap.get (ost_runner_up).addAll (jobs_ost_old);
          this.lastCatchUpTime = newTime;
        }
        else
          this.obtainedServiceTimeMap.put (ost_new, jobs_ost_old);
      }
      else
      {
        this.obtainedServiceTimeMap.remove (ost_old);
        this.obtainedServiceTimeMap.put (ost_new, jobs_ost_old);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing, and makes that final.
   * 
   * @see #arrive
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
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
    if (job == null || ! getJobsInWaitingArea ().contains (job))
      throw new IllegalArgumentException ();
    if (hasServerAcccessCredits ())
      start (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not drop jobs.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not drop jobs.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the jobs from the internal data structures.
   * 
   * @see #revoke
   * @see #removeJobFromInternalAdministration
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    removeJobFromInternalAdministration (job);
  }

  /** Calls {@link #rescheduleDepartureEvent} and {@link #rescheduleCatchUpEvent}.
   * 
   * @see #revoke
   * @see #rescheduleDepartureEvent
   * @see #rescheduleCatchUpEvent
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    rescheduleDepartureEvent ();
    rescheduleCatchUpEvent ();
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
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
      start (time, getFirstJobInWaitingArea ());
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
  
  /** Performs sanity checks and administers the job's initial obtained service time.
   * 
   * @see #getServiceTimeForJob
   * @see #obtainedServiceTimeMap
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null
    || (! isJob (job))
    || isJobInServiceArea (job))
      throw new IllegalArgumentException ();
    sanityInternalAdministration ();
    final double jobRequiredServiceTime = getServiceTimeForJob (job);
    if (jobRequiredServiceTime < 0)
      throw new RuntimeException ();
    if (! this.obtainedServiceTimeMap.containsKey (0.0))
      this.obtainedServiceTimeMap.put (0.0, new LinkedHashSet<> ());
    this.obtainedServiceTimeMap.get (0.0).add (job);
  }

  /** Reschedules due to the start of a job,
   *  making it depart immediately if its requested service time is zero
   *  (or finite and time itself is infinite),
   *  or rescheduling through {@link #rescheduleDepartureEvent} and {@link #rescheduleCatchUpEvent} otherwise.
   * 
   * @see #getServiceTimeForJob
   * @see #rescheduleDepartureEvent
   * @see #rescheduleCatchUpEvent
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null
    || (! getJobs ().contains (job))
    || (! getJobsInServiceArea ().contains (job)))
      throw new IllegalArgumentException ();
    final double jobServiceTime = getServiceTimeForJob (job);
    if (jobServiceTime < 0)
      throw new RuntimeException ();
    if (jobServiceTime == 0
    || (Double.isFinite (jobServiceTime) && Double.isInfinite (time)))
      depart (time, job);
    else
    {
      rescheduleDepartureEvent ();
      rescheduleCatchUpEvent ();
    }
    sanityInternalAdministration ();
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
  // CATCH-UP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private double lastCatchUpTime = Double.NaN;
  
  /** Calls {@link #update}
   *  followed by {@link #rescheduleDepartureEvent} and {@link #rescheduleCatchUpEvent}.
   * 
   * <p>
   * Always invoked from (only) {@link CatchUpEvent}.
   * 
   * <p>
   * Note that the actual catch-up is performed in {@link #updateObtainedServiceTime} through the pre-update hook.
   * This method does however check that there are actually two groups of jobs in the service area with different obtained service
   * times.
   * Otherwise, we are illegally scheduled unless a recent update took place without rescheduling (e.g., a Queue Access Vacation).
   * It also checks that after the {@link #update}, the number of such groups has decreased by exactly one,
   * again considering that we may have missed the catch-up due to a recent {@link #update}.
   * 
   * <p>
   * For internal use but made protected for documentation purposes.
   * 
   * @param time The time of the catch-up event (current time).
   * 
   * @throws IllegalStateException If this method is invoked at an inappropriate moment,
   *                               or the pre-update hook fails to perform the expected catch-up
   *                               (in both cases, taking into consideration the possibility of missed updates).
   * 
   * @see CatchUpEvent
   * @see #rescheduleDepartureEvent
   * @see #rescheduleCatchUpEvent
   * 
   */
  protected final void catchUp (final double time)
  {
    // Determine whether we had a recent (enough) catch-up.
    if ((! Double.isNaN (this.lastCatchUpTime)) && this.lastCatchUpTime > time)
      throw new IllegalStateException ();
    final boolean recentCatchUp = (! Double.isNaN (this.lastCatchUpTime)) && this.lastCatchUpTime >= time - CUPS.TOLERANCE_OST;
    final int oldObtainedServiceTimeMapSize = this.obtainedServiceTimeMap.size ();
    if (oldObtainedServiceTimeMapSize < 2 && ! recentCatchUp)
      // We are mis-scheduled and there has been no recent catch-up to justify it.
      throw new IllegalStateException ("illegally scheduled catch-up event at t=" + time
        + ", old obtained service-time map: " + this.obtainedServiceTimeMap + ".");
    update (time);
    if (this.obtainedServiceTimeMap.size () != oldObtainedServiceTimeMapSize - 1 && ! recentCatchUp)
      // No catch up took place in the update and there has been no recent catch-up to justify it.
      throw new IllegalStateException ("missed catch-up at t=" + time
        + ": old number of groups: " + oldObtainedServiceTimeMapSize
        + ", new obtained service-time map: " + this.obtainedServiceTimeMap + ".");
    rescheduleDepartureEvent ();
    rescheduleCatchUpEvent ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #removeJobFromQueueUponRevokation} for the departed job.
   * 
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobFromQueueUponRevokation (departingJob, time, false);
  }

  /** Calls {@link #rescheduleDepartureEvent} and {@link #rescheduleCatchUpEvent}.
   * 
   * @see #depart
   * @see #rescheduleDepartureEvent
   * @see #rescheduleCatchUpEvent
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (departedJob == null)
      throw new IllegalArgumentException ();
    rescheduleDepartureEvent ();
    rescheduleCatchUpEvent ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE DEPARTURE EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Reschedules the single departure event for this queue.
   * 
   * <p>
   * Refuses to schedule departure events at positive or negative infinity.
   * 
   * @see #getDepartureEvents
   * @see #cancelDepartureEvent
   * @see #getJobsInServiceArea
   * @see #getServiceTimeForJob
   * @see #getMinimumObtainedServiceTime
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void rescheduleDepartureEvent ()
  {
    final Set<SimJQEvent.Departure> departureEvents = getDepartureEvents ();
    if (departureEvents == null)
      throw new RuntimeException ();
    if (departureEvents.size () > 1)
      throw new IllegalStateException ();
    if (departureEvents.size () > 0)
      cancelDepartureEvent (departureEvents.iterator ().next ());
    sanityInternalAdministration ();
    if (hasJobsInServiceArea ())
    {
      final Set<J> jobsExecuting = getJobsExecuting ();
      if (jobsExecuting.isEmpty ())
        throw new IllegalStateException ();
      if (Double.isInfinite (getLastUpdateTime ()))
      {
        // If time is either positive of negative infinity,
        // all jobs in the service area must have infinite service-time requirement,
        // because if finite, they should gave departed upon start already.
        // Other than that, there is nothing to do at positive or negative infinity,
        // because jobs with infinite service-time requirement never depart.
        for (final J job : getJobsInServiceArea ())
          if (Double.isFinite (getServiceTimeForJob (job)))
            throw new IllegalStateException ();
      }
      else
      {
        // The current time is finite.
        // Find the job among those executing that is first to leave.
        // In fact, given the CUPS scheduling, that is always the candidate job with the minimum REQUIRED service time.
        // (Since all jobs executing have identical obtained service times.)
        boolean first = true;
        J leaver = null;
        double rst_leaver = Double.POSITIVE_INFINITY;
        for (final J job : jobsExecuting)
          if (first)
          {
            first = false;
            leaver = job;
            rst_leaver = getServiceTimeForJob (job); 
          }
          else
          {
            final double rst_job = getServiceTimeForJob (job);
            if (rst_job < rst_leaver)
            {
              leaver = job;
              rst_leaver = rst_job;
            }
          }
        if (Double.isFinite (rst_leaver))
        {
          final int numberOfJobsExecuting = jobsExecuting.size ();
          final double ost = getMinimumObtainedServiceTime ();
          final double timeToDeparture = Math.max (rst_leaver - ost, 0.0) * numberOfJobsExecuting;
          scheduleDepartureEvent (getLastUpdateTime () + timeToDeparture, leaver);
        }
        else
          // Our earliest departer has infinite requested service time.
          // Nothing to do here, because such jobs never depart.
          ;
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CATCH-UP EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** A schedule-able {@link SimEvent} for catch-ups of which a single instance is used throughout in this class.
   * 
   * @see #catchUpEvent
   * 
   */
  protected final static class CatchUpEvent 
  extends SimQueueCatchUpEvent
  {

    /** Creates a new catch-up event.
     * 
     * @param queue       The {@link CUPS} queue for which to create the event.
     * @param catchUpTime The scheduled time of catch-up (event can be reused, so time may be changed on the event).
     * @param action      The action to take upon the scheduled catch-up.
     * 
     */
    public CatchUpEvent (final CUPS queue, final double catchUpTime, SimEventAction action)
    {
      super (queue, catchUpTime, action);
    }
    
  }
  
  /** The single catch-up event.
   * 
   * Invokes {@link CUPS#catchUp} on the enclosing instance.
   * 
   * @see CatchUpEvent
   * @see CUPS#catchUp
   * 
   */
  protected final CatchUpEvent catchUpEvent =
    new CatchUpEvent (this, Double.NEGATIVE_INFINITY, (SimEventAction) (final SimEvent event) ->
    {
      CUPS.this.catchUp (event.getTime ());
    });
  
  /** Cancels a pending catch-up event (if scheduled).
   * 
   * <p>
   * The implementation simply removes the single catch-up event from the event list and from {@link #eventsScheduled}.
   * 
   * @see CatchUpEvent
   * @see #catchUpEvent
   * @see #getEventList
   * @see #eventsScheduled
   * 
   */
  protected final void cancelCatchUpEvent ()
  {
    getEventList ().remove (this.catchUpEvent);
    this.eventsScheduled.remove (this.catchUpEvent);
  }
  
  /** Reschedules a catch-up event.
   * 
   * <p>
   * Refuses to schedule catch-up events at positive or negative infinity.
   * 
   * @see #cancelCatchUpEvent
   * @see #getTimeToCatchUp
   * @see CatchUpEvent
   * @see #catchUpEvent
   * 
   */
  protected final void rescheduleCatchUpEvent ()
  {
    cancelCatchUpEvent ();
    if (this.obtainedServiceTimeMap.size () >= 2)
    {
      final double time = getLastUpdateTime ();
      final double timeToCatchUp = getTimeToCatchUp ();
      if (Double.isFinite (time) && Double.isFinite (timeToCatchUp))
      {
        getEventList ().schedule (time + timeToCatchUp, this.catchUpEvent);
        this.eventsScheduled.add (this.catchUpEvent);
      }
    }
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
