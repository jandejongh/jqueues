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
package nl.jdj.jqueues.r5.extensions.ost;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueStateHandler;

/** A {@link SimQueueStateHandler} for maintaining the per-job obtained service time,
 *  group the jobs with identical (within a tolerance) service time,
 *  and index these groups increasing in their members obtained service times.
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
public final class SimQueueOSTStateHandler
implements SimQueueStateHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueStateHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueOSTHandler".
   * 
   * @return "SimQueueOSTHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueOSTHandler";
  }

  @Override
  public final void initHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null)
      throw new IllegalArgumentException ();
    if (this.queueState != null)
      throw new IllegalStateException ();
    this.queueState = queueState;
    this.ost_j.clear ();
    this.jobs_ost.clear ();
  }
  
  @Override
  public void resetHandler (final DefaultSimQueueState queueState)
  {
    if (queueState == null || queueState != this.queueState)
      throw new IllegalArgumentException ();
    this.ost_j.clear ();
    this.jobs_ost.clear ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultSimQueueState queueState = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OBTAINED SERVICE-TIME MAPS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimJob, Double> ost_j = new LinkedHashMap<> ();
  
  private final NavigableMap<Double, Set<SimJob>> jobs_ost = new TreeMap<> ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OBTAINED SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns whether this handler has no jobs in its administration.
   * 
   * @return True if this handler has no jobs in its administration.
   * 
   */
  public final boolean isEmpty ()
  {
    return this.ost_j.isEmpty ();
  }
  
  /** Gets the number of groups of jobs with equal obtained service times.
   * 
   * @return The number of groups of jobs with equal obtained service times.
   * 
   */
  public final int getNumberOfOstGroups ()
  {
    return this.jobs_ost.size ();
  }

  /** Gets the obtained service time of given job.
   * 
   * @param job The job, non-{code null}.
   * 
   * @return The obtained service time of given job.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or non-present on the queue.
   * 
   */
  public final double getObtainedServiceTime (final SimJob job)
  {
    if (job == null || ! this.ost_j.containsKey (job))
      throw new IllegalArgumentException ();
    return this.ost_j.get (job);
  }
  
  /** Gets the minimum obtained service time.
   * 
   * @return The minimum obtained service time.
   * 
   * @throws NoSuchElementException If the queue is empty.
   * 
   * @see #isEmpty
   * 
   */
  public final double getMinimumObtainedServiceTime ()
  {
    return this.jobs_ost.firstKey ();
  }
  
  /** Gets the next-higher-to-minimum obtained service time.
   * 
   * @return The next-higher-to-minimum obtained service time.
   * 
   * @throws NoSuchElementException If the queue is empty, or if all jobs have equal obtained service-time.
   * 
   * @see #isEmpty
   * @see #getNumberOfOstGroups
   * 
   */
  public final double getNextHigherThanMinimumObtainedServiceTime ()
  {
    if (getNumberOfOstGroups () < 2)
      throw new NoSuchElementException ();
    return this.jobs_ost.higherKey (this.jobs_ost.firstKey ());
  }
  
  /** Gets the maximum obtained service time.
   * 
   * @return The maximum obtained service time.
   * 
   * @throws NoSuchElementException If the queue is empty.
   * 
   * @see #isEmpty
   * 
   */
  public final double getMaximumObtainedServiceTime ()
  {
    return this.jobs_ost.lastKey ();
  }
  
  /** Gets the next-lower-to-maximum obtained service time.
   * 
   * @return The next-lower-to-maximum obtained service time.
   * 
   * @throws NoSuchElementException If the queue is empty, or if all jobs have equal obtained service-time.
   * 
   * @see #isEmpty
   * @see #getNumberOfOstGroups
   * 
   */
  public final double getNextLowerThanMaximumObtainedServiceTime ()
  {
    if (getNumberOfOstGroups () < 2)
      throw new NoSuchElementException ();
    return this.jobs_ost.lowerKey (this.jobs_ost.lastKey ());
  }
  
  /** Adds a starting job to the obtained-service-time administration.
   * 
   * @param job The job to add.
   * 
   * @throws IllegalArgumentException If the job is {@code null} or already present.
   * 
   */
  public final void addStartingJob (final SimJob job)
  {
    if (job == null || this.ost_j.containsKey (job))
      throw new IllegalArgumentException ();
    this.ost_j.put (job, 0.0);
    if (! this.jobs_ost.containsKey (0.0))
      this.jobs_ost.put (0.0, new LinkedHashSet<> ()); // Reverse start order in the Set.
    this.jobs_ost.get (0.0).add (job);
  }
  
  /** Adds a set of starting jobs to the obtained-service-time administration.
   * 
   * @param jobs The jobs to add.
   * 
   * @throws IllegalArgumentException If the set is null, or a job in it is {@code null} or already present.
   * 
   * @see #addStartingJob
   * 
   */
  public final void addStartingJobs (final Set<SimJob> jobs)
  {
    if (jobs == null)
      throw new IllegalArgumentException ();
    for (final SimJob job : jobs)
      addStartingJob (job);
  }
  
  /** Removes a given job from the obtained-service-time administration.
   * 
   * @param job           The job to remove.
   * @param mustBePresent Whether the job must be present a priori (sanity check).
   * 
   * @throws IllegalArgumentException If the job is {@code null}.
   * @throws IllegalStateException    If the job is unexpectedly absent.
   * 
   */
  public final void removeJob (final SimJob job, boolean mustBePresent)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (mustBePresent && ! this.ost_j.containsKey (job))
      throw new IllegalStateException ();
    this.ost_j.remove (job);
    final Iterator<Entry<Double, Set<SimJob>>> jobs_ost_i = this.jobs_ost.entrySet ().iterator ();
    while (jobs_ost_i.hasNext ())
    {
      final Entry<Double, Set<SimJob>> entry = jobs_ost_i.next ();
      final Set<SimJob> jobs = entry.getValue ();
      if (jobs.contains (job))
      {
        jobs.remove (job);
        if (jobs.isEmpty ())
          jobs_ost_i.remove ();
        return;
      }
    }
    if (mustBePresent)
      throw new IllegalStateException ();
  }
  
  /** Increases the obtained service time on jobs with the minimum obtained service time.
   * 
   * @param time        The current time.
   * @param delta_ost   The increase in obtained service time per job.
   * @param mayOvertake Whether the jobs are allowed to overtake other jobs
   *                    in the ordering of jobs with equal obtained service times.
   * @param TOLERANCE   The numerical tolerance in case overtaking is allowed.
   * 
   * @throws IllegalArgumentException If {@code delta_ost < 0} or if an illegal take-over takes place in terms of
   *                                  obtained service times.
   * @throws IllegalStateException    If there are no jobs present at the queue.
   * 
   * @see #isEmpty
   * 
   */
  public final void increaseMinimumObtainedServiceTime
  (final double time, final double delta_ost, final boolean mayOvertake, final double TOLERANCE)
  {
    if (delta_ost < 0)
      throw new IllegalArgumentException ();
    if (this.jobs_ost.isEmpty ())
      throw new IllegalStateException ();
    if (delta_ost == 0)
      return;
    final double minOst = this.jobs_ost.firstKey ();
    final double newOst = minOst + delta_ost;
    if (mayOvertake || this.jobs_ost.size () == 1)
    {
      for (final SimJob job : this.jobs_ost.firstEntry ().getValue ())
        this.ost_j.put (job, newOst);
      final Set<SimJob> jobsMinOst = this.jobs_ost.remove (minOst);
      if (! this.jobs_ost.containsKey (newOst))
        this.jobs_ost.put (newOst, jobsMinOst);
      else
        // Maintains reverse start order!
        this.jobs_ost.get (newOst).addAll (jobsMinOst);
    }
    else if (newOst > this.jobs_ost.higherKey (minOst) + TOLERANCE)
      throw new IllegalArgumentException ();
    else if (newOst >= this.jobs_ost.higherKey (minOst) - TOLERANCE)
    {
      // Merge first and second entries; but make sure we do not ever decrease the obtained service time of any job (group).
      final double newMinOst = Math.max (newOst, this.jobs_ost.higherKey (minOst));
      // Removes second entry.
      final Set<SimJob> jobsMinOst = this.jobs_ost.remove (this.jobs_ost.higherKey (minOst));
      // Removes first entry as well; copy its jobs into jobMinOst.
      // Note that this preserves reverse start order.
      jobsMinOst.addAll (this.jobs_ost.remove (this.jobs_ost.firstKey ()));
      this.jobs_ost.put (newMinOst, jobsMinOst);
      for (final SimJob job : jobsMinOst)
        this.ost_j.put (job, newMinOst);
    }
    else
      // The increase in service time on the first entry does not cause a "catch up"/"take over" with the second entry,
      // even with the tolerance provided.
      // We can now safely recur while allowing take over.
      increaseMinimumObtainedServiceTime (time, delta_ost, true, TOLERANCE);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS WITH OBTAINED SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets all jobs with given obtained service time.
   * 
   * @param ost The obtained service time.
   * 
   * @return All jobs with given obtained service time.
   * 
   * @throws IllegalArgumentException If the job is not present at the queue.
   * 
   */
  public final Set<SimJob> getJobsWithObtainedServiceTime (final double ost)
  {
    if (! this.jobs_ost.containsKey (ost))
      throw new IllegalArgumentException ();
    return this.jobs_ost.get (ost);
  }
  
  /** Gets all jobs with minimum obtained service time.
   * 
   * @return All jobs with minimum obtained service time.
   * 
   * @throws NoSuchElementException If the queue is empty.
   * 
   */
  public final Set<SimJob> getJobsWithMinimumObtainedServiceTime ()
  {
    return this.jobs_ost.firstEntry ().getValue ();
  }
  
  /** Gets all jobs with maximum obtained service time.
   * 
   * @return All jobs with maximum obtained service time.
   * 
   * @throws NoSuchElementException If the queue is empty.
   * 
   */
  public final Set<SimJob> getJobsWithMaximumObtainedServiceTime ()
  {
    return this.jobs_ost.lastEntry ().getValue ();
  }
  
}
