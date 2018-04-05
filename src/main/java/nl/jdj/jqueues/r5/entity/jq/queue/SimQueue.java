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
package nl.jdj.jqueues.r5.entity.jq.queue;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.ctandem2.CTandem2;
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;


/** A (generic) queueing system capable of hosting and optionally serving jobs ({@link SimJob}s).
 *
 * <p> A {@link SimQueue} is an abstraction of a <i>queueing system</i> from queueing theory.
 * 
 * <p>
 * The following {@code javadoc} section aims at concisely specifying the {@link SimQueue} interface.
 * The assumptions and constraints in the sequel should not be interpreted as "agreed upon in the field",
 * but motivations for them are not given here in order to keep the section at (hopefully) pleasant length.
 * 
 * <p>
 * A {@link SimQueue} accepts so-called <i>jobs</i> (in our case {@link SimJob}s) for a <i>visit</i>.
 * Each job can visit at most one queue at a time,
 * and while it is visiting a queue,
 * it cannot initiate <i>another</i> visit to that same queue.
 * 
 * <p>
 * A visit is initiated by the <i>arrival</i> of a job at a queue, see {@link #arrive}.
 * 
 * <p>
 * During a visit, a job is either in the queue's so-called <i>waiting area</i>,
 * in which it always waits,
 * or in the queue's <i>service area</i>,
 * in which the job <i>can</i> receive <i>service</i> from
 * the servers in the service area.
 * At the beginning of a visit (the <i>arrival</i>), a job is either put
 * in the waiting area or directly into the service area
 * (or <i>dropped</i> immediately).
 * A job can move at most once from the waiting into the service area,
 * but not in reverse direction.
 * Entering the service area of a queue is called <i>starting</i> the job.
 * 
 * <p>
 * In itself, a {@link SimQueue} makes no assumption whatsoever about the server structure,
 * except for the fact that <i>only jobs in the service area can receive service</i>.
 * But other than that, there may be any number (including zero and infinity) of servers,
 * and the number of servers may change in time in the {@link SimQueue} interface.
 * This flexibility, however, comes at the expense of the absence of server-structure methods
 * on the (bare) {@link SimQueue} interface.
 * 
 * <p>
 * Also note that jobs in the service area do <i>not</i> have to be served all the time
 * (although many sub-interfaces/sub-classes impose this requirement).
 * 
 * <p>
 * A visit can end in three different ways:
 * <ul>
 * <li>a <i>departure</i> (the visit ends normally),
 * <li>a <i>drop</i> (the queue cannot complete the visit, e.g., because of limited buffer space or vacation),
 * <li>a <i>revocation</i> (the job is removed upon external request, or because a user-specified state condition is met,
 *                                                                    a so-called <i>auto-revocation</i>).
 * </ul>
 * 
 * <p>
 * If a visit ends, the job is said to
 * <i>exit</i> (<i>depart from</i>; <i>be dropped at</i>; <i>be (auto-)revoked at</i>) the queue.
 * Each way to exit the queue can be from the waiting area or from the service area
 * (but sub-classes may restrict the possibilities).
 * 
 * <p>
 * If a visit never ends, the job (or the visit) is named <i>sticky</i>;
 * again this can be at the waiting area or the service area.
 * 
 * <p>
 * Each {@link SimQueue} must support the notions of <i>queue-access vacations</i> during which all jobs are dropped upon arrival,
 * and of <i>server-access credits</i> that limit the remaining number of jobs that can be started
 * (i.e., moved from the waiting area into the service area).
 * 
 * <p>
 * The <i>state</i> of a {@link SimQueue} includes at least the set of jobs present (and in which area each resides),
 * its queue-access vacation state and its remaining number of server-access credits.
 * In addition, the so-called {@code startArmed} state has to be maintained, see {@link #isStartArmed}.
 * 
 * <p>
 * Each {@link SimQueue} (and {@link SimJob} for that matter) must notify all state changes,
 * see {@link SimEntityListener}.
 * 
 * <p>
 * A partial implementation of {@link SimQueue} is available in {@link AbstractSimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimEventList
 * @see SimEntity
 * @see SimJob
 * @see SimQueueListener
 * @see AbstractSimQueue
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
public interface SimQueue<J extends SimJob, Q extends SimQueue>
extends SimEntity, SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a functional copy of this {@link SimQueue}.
   *
   * <p>
   * The new object has the same (concrete) type as the original, but starts without jobs and without external listeners.
   * Its initial state must be as if {@link #resetEntity} was invoked on the queue.
   * 
   * <p>
   * Note that the semantics of this method are much less strict than the <code>Object.clone ()</code> method.
   * Typically, concrete classes will implement this by returning a new {@link SimQueue} object.
   * This way, we circumvent the problem of cloning objects with final (for good reasons) fields.
   * 
   * @return A functional copy of this {@link SimQueue}.
   * 
   * @throws UnsupportedOperationException If the operation is not supported (yet); this should be considered a software error.
   * 
   */
  public SimQueue<J, Q> getCopySimQueue () throws UnsupportedOperationException;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS / JOBS IN WAITING AREA / JOBS IN SERVICE AREA
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the set of jobs currently visiting this queue.
   *
   * @return The set of jobs currently visiting the queue, non-{@code null}.
   * 
   * @see #getNumberOfJobs
   * @see #getJobsInWaitingArea
   * @see #getJobsInServiceArea
   * 
   */
  public Set<J> getJobs ();

  /** Gets the number of jobs currently visiting the queue.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobs ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs currently visiting the queue, zero or positive.
   * 
   * @see #getJobs
   * @see #getNumberOfJobsInWaitingArea
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  public int getNumberOfJobs ();
  
  /** Checks for the presence of a job.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobs ().contains (job)},
   * but both methods must always yield the same result.
   * 
   * @param job The job.
   * 
   * @return Whether given job is currently visiting this queue.
   * 
   * @see #getJobs
   * 
   */
  public boolean isJob (SimJob job);
  
  /** Get the set of jobs in the waiting area.
   *
   * @return The set of jobs in the waiting area, non-{@code null}.
   * 
   * @see #getNumberOfJobsInWaitingArea
   * @see #getJobs
   * @see #getJobsInServiceArea
   * 
   */
  public Set<J> getJobsInWaitingArea ();

  /** Gets the number of jobs in the waiting area.
   * 
   * <p>
   * Typically, this method is more efficient than {@code getJobsInWaitingArea ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs in the waiting area.
   * 
   * @see #getJobsInWaitingArea
   * @see #getNumberOfJobs
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  public int getNumberOfJobsInWaitingArea ();

  /** Checks for the presence of a job in the waiting area.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobsInWaitingArea ().contains (job)},
   * but both methods must always yield the same result.
   * 
   * @param job The job.
   * 
   * @return Whether given job is currently present in the waiting area this queue.
   * 
   * @see #getJobsInWaitingArea
   * 
   */
  public boolean isJobInWaitingArea (final SimJob job);
  
  /** Get the set of jobs in the service area.
   *
   * @return The set of jobs in the service area, non-{@code null}.
   * 
   * @see #getNumberOfJobsInServiceArea
   * @see #getJobs
   * @see #getJobsInWaitingArea
   * 
   */
  public Set<J> getJobsInServiceArea ();

  /** Gets the number of jobs in the service area.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobsInServiceArea ().size ()},
   * but both methods must always yield the same result.
   * 
   * @return The number of jobs in the service area.
   * 
   * @see #getJobsInServiceArea
   * @see #getNumberOfJobs
   * @see #getNumberOfJobsInWaitingArea
   * 
   */
  public int getNumberOfJobsInServiceArea ();
  
  /** Checks for the presence of a job in the service area.
   *
   * <p>
   * Typically, this method is more efficient than {@code getJobsInServiceArea ().contains (job)},
   * but both methods must always yield the same result.
   * 
   * @param job The job.
   * 
   * @return Whether given job is currently present in the service area this queue.
   * 
   * @see #getJobsInWaitingArea
   * 
   */
  public boolean isJobInServiceArea (SimJob job);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns whether or not the queue is on queue-access vacation.
   * 
   * @return Whether or not the queue is on queue-access vacation.
   * 
   * @see #setQueueAccessVacation
   * 
   */
  public boolean isQueueAccessVacation ();
  
  /** Starts or ends a queue-access vacation.
   * 
   * <p>
   * During a queue-access vacation, all {@link SimJob}s will be dropped immediately upon arrival.
   * 
   * @param time  The time at which to start or end the queue-access vacation, i.c., the current time.
   * @param start Whether to start ({@code true}) or end ({@code false}) the vacation.
   * 
   * @see #arrive
   * @see #isQueueAccessVacation
   * 
   */
  public void setQueueAccessVacation (double time, boolean start);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Arrival of a job at the queue.
   *
   * <p>
   * This methods should be called from the {@link SimEventList} as a result of scheduling the job arrival.
   * Implementations can rely on the fact that the time argument supplied is actually the current time in the simulation.
   * 
   * <p>
   * Do not use this method to schedule job arrivals on the event list!
   * 
   * <p>
   * Note that during a <i>queue-access vacation</i>, all jobs will be dropped upon arrival.
   * 
   * @param time The time at which the job arrives, i.c., the current time.
   * @param job  The job.
   * 
   * @see #isQueueAccessVacation
   *
   */
  public void arrive (double time, J job);

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Revocation (attempt) of a job at a queue.
   *
   * <p>
   * If the job is not currently present at this {@link SimQueue}, {@code false} is returned.
   * 
   * <p>
   * If the job is present in the service area (has already started), and {@code interruptService == false},
   * this method returns {@code false}.
   * 
   * <p>
   * In all other case, the job is revoked from the queue and {@code true} is returned.
   * 
   * @param time             The time at which the request is issued, i.c., the current time.
   * @param job              The job to be revoked from the queue.
   * @param interruptService Whether to allow interruption of the job's
   *                           service if already started.
   *                         If {@code false}, revocation will only succeed if the
   *                           job has not started yet.
   *
   * @return True if revocation succeeded (returns {@code false} if the job is not present).
   *
   */
  public boolean revoke (double time, J job, boolean interruptService);

  /** Revocation of a job at a queue.
   *
   * <p>
   * Unlike {@link #revoke(double, nl.jdj.jqueues.r5.entity.jq.job.SimJob, boolean)}, this request can never fail
   * in the sense that upon return from this method, the job is no longer present in the queue.
   * This method does nothing if the job is not present a priori at the queue.
   * 
   * @param time The time at which the request is issued, i.c., the current time.
   * @param job  The job to be revoked from the queue.
   *
   */
  public default void revoke (final double time, final J job)
  {
    revoke (time, job, true);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AutoRevocationPolicy
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The auto-revocation policy.
   * 
   * <p>
   * Auto-revocation refers to the revocation of jobs upon a specific user-specified state condition.
   * It plays an essential role in composite queues, notably {@link CTandem2}.
   * 
   */
  public enum AutoRevocationPolicy
  {
    /** No auto-revocation (this is the mandatory default on each {@link SimQueue}).
     * 
     */
    NONE,
    /** Job auto-revocation upon start.
     * 
     */
    UPON_START
  }
  
  /** Gets the auto-revocation policy of this queue.
   * 
   * @return The auto-revocation policy of this queue.
   * 
   * @see AutoRevocationPolicy
   * 
   */
  AutoRevocationPolicy getAutoRevocationPolicy ();
  
  /** Sets the auto-revocation policy of this queue.
   * 
   * <p>
   * The auto-revocation policy on a queue should be set only once and before the queue's use.
   * It must survive queue resets.
   * 
   * @param autoRevocationPolicy The new auto-revocation policy, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the policy is {@code null}.
   * 
   * @see AutoRevocationPolicy
   * 
   */
  void setAutoRevocationPolicy (final AutoRevocationPolicy autoRevocationPolicy);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the (remaining) server-access credits.
   *
   * <p>
   * The number of server-access credits is the remaining number of jobs allowed to start.
   * They play an essential role in composite queues, notably {@link CTandem2}.
   * 
   * <p>
   * Upon reset, the initial value <i>must</i> be {@link Integer#MAX_VALUE},
   * which is treated as infinity.
   * 
   * @return The remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @see #setServerAccessCredits
   * 
   */
  public int getServerAccessCredits ();
  
  /** Sets the server-access credits.
   * 
   * @param time    The time at which to set the credits, i.c., the current time.
   * @param credits The new remaining server-access credits, non-negative, with {@link Integer#MAX_VALUE} treated as infinity.
   * 
   * @throws IllegalArgumentException If credits is (strictly) negative.
   * 
   * @see #getServerAccessCredits
   * 
   */
  public void setServerAccessCredits (double time, int credits);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Returns the {@code StartArmed} state of the queue.
   * 
   * <p>
   * Formally: A queue is in {@code StartArmed} state,
   * and this method returns {@code true},
   * if and only if any (hypothetical) arriving job will start service immediately
   * (i.e., enter the service area upon arrival immediately),
   * <i>assuming</i> the following (i.e., ignoring the actual state settings):
   * <ul>
   * <li>the absence of a queue access vacation,
   * <li>at least one server-access credit,
   * <li>an empty waiting area.
   * </ul>
   * 
   * <p>
   * Note that the actual values of the state properties above is irrelevant.
   * 
   * <p>
   * Informally, the {@code StartArmed} state of a queue reflects the fact that not all service capacity
   * of the queue is used at the present time (for whatever reason);
   * not used to such an extent that the queue <i>would</i> start an arriving job immediately
   * if the three requirements mentioned above <i>would</i> hold.
   * 
   * <p>
   * The {@code StartArmed} state of a queue is admittedly difficult to grasp and unlikely to find many uses in practice,
   * but it is essential for specific types of so-called <i>composite</i> queues,
   * i.e., queues that are composed of other queues.
   * See, for instance, {@link CTandem2}.
   * 
   * @return True if the queue is in {@code StartArmed} state.
   * 
   * @see SimQueueListener#notifyNewStartArmed
   * 
   */
  public boolean isStartArmed ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
