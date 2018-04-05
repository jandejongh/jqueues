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
package nl.jdj.jqueues.r5.entity.jq.queue.composite.enc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.processorsharing.PS;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue}
 *  equipped with fixed expiration limits on waiting time, service time and sojourn time.
 *
 * <p>
 * This composite queue mimics (precisely) the {@link SimQueue} interface of the encapsulated queue,
 * yet "removes" real jobs if their waiting, service of sojourn time exceeds a given, fixed limit.
 * On the encapsulated queue, the delegate job is removed through {@link SimQueue#revoke}.
 * By default, real jobs for which its waiting, service or sojourn time expires will <i>depart</i>,
 * and in that sense, cannot be distinguished from regular departures on the encapsulated queue.
 * However, one can control this behavior through {@link ExpirationMethod} and {@link #setExprirationMethod}.
 * 
 * <p>
 * The semantics of either expiration time being zero are a bit tricky, but in essence, the encapsulated queue takes precedence.
 * In other words, setting an expiration time to zero, does not (always) <i>enforce</i>
 * the applicable expiration events to take place.
 * For instance, if the encapsulated queue takes a job into service immediately upon arrival (like {@link PS} does),
 * {@code maxWaitingTime = 0} has no effect, i.e., no revocation (due to expiration) takes place.
 * 
 * <p>
 * However, setting an expiration time to {@link Double#POSITIVE_INFINITY} is legal,
 * and effectively disables monitoring that particular (waiting/service/sojourn) time on the encapsulated queue.
 * 
 * <p>
 * Refer to {@link AbstractEncapsulatorSimQueue},
 * {@link AbstractSimQueueComposite}
 * and {@link SimQueueComposite}
 * for more details on encapsulated queues.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
 * @see Enc
 * @see EncJL
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
public class EncTL
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends Enc>
  extends AbstractEncapsulatorSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue given an event list and a queue and limits on waiting time, service time and sojourn time.
   *
   * <p>
   * The constructor sets the {@link ExpirationMethod} to {@link ExpirationMethod#DEPARTURE}.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param maxWaitingTime        The maximum waiting time, non-negative, {@link Double#POSITIVE_INFINITY} is allowed.
   * @param maxServiceTime        The maximum service time, non-negative, {@link Double#POSITIVE_INFINITY} is allowed.
   * @param maxSojournTime        The maximum sojourn time, non-negative, {@link Double#POSITIVE_INFINITY} is allowed.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>,
   *                                    or any of the time arguments is strictly negative.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public EncTL
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory,
   final double maxWaitingTime,
   final double maxServiceTime,
   final double maxSojournTime)
  {
    super (eventList, queue, delegateSimJobFactory);
    if (maxWaitingTime < 0 || maxServiceTime < 0 || maxSojournTime < 0)
      throw new IllegalArgumentException ();
    this.maxWaitingTime = maxWaitingTime;
    this.maxServiceTime = maxServiceTime;
    this.maxSojournTime = maxSojournTime;
    this.exprirationMethod = ExpirationMethod.DEPARTURE;
  }
  
  /** Returns a new {@link EncTL} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue, the same delegate-job factory, equal respective expiration times, and the same expiration method.
   * 
   * @return A new {@link EncTL} object on the same {@link SimEventList} with a copy of the encapsulated
   *           queue, the same delegate-job factory, equal respective expiration times, and the same expiration method.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * @see #getMaxWaitingTime
   * @see #getMaxServiceTime
   * @see #getMaxSojournTime
   * @see #getExprirationMethod
   * @see #setExprirationMethod
   * 
   */
  @Override
  public EncTL<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    final EncTL<DJ, DQ, J, Q> copy =
      new EncTL (getEventList (),
                 encapsulatedQueueCopy,
                 getDelegateSimJobFactory (),
                 getMaxWaitingTime (),
                 getMaxServiceTime (),
                 getMaxSojournTime ());
    copy.setExprirationMethod (getExprirationMethod ());
    return copy;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "EncTL(maxWai,maxSer,maxSoj)[encapsulated queue]".
   * 
   * @return "EncTL(maxWai,maxSer,maxSoj)[encapsulated queue]".
   * 
   * @see #getEncapsulatedQueue
   * @see #getMaxWaitingTime
   * @see #getMaxServiceTime
   * @see #getMaxSojournTime
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "EncTL("
      + getMaxWaitingTime ()
      + ","
      + getMaxServiceTime ()
      + ","
      + getMaxSojournTime ()
      + ")"
      + "[" + getEncapsulatedQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXPIRATION METHOD
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The method for "presenting" jobs for which a time expires at the composite (this) queue.
   * 
   * <p>
   * Note: This method <i>only</i> applies to jobs that "expire"; all other job events from the encapsulated queue are
   *       processed as described in {@link AbstractSimQueueComposite} and {@link AbstractEncapsulatorSimQueue}.
   * 
   */
  public enum ExpirationMethod
  {
    /** The real job is dropped.
     * 
     */
    DROP,
    /** The real job is auto-revoked.
     * 
     */
    AUTO_REVOCATION,
    /** The real job departs.
     * 
     * <p>
     * This is the default setting.
     * 
     */
    DEPARTURE
  };

  private ExpirationMethod exprirationMethod;

  /** Returns the expiration method.
   * 
   * @return The expiration method, non-{@code null}.
   * 
   * @see ExpirationMethod
   * 
   */
  public final ExpirationMethod getExprirationMethod ()
  {
    return this.exprirationMethod;
  }

  /** Sets the expiration method.
   * 
   * @param exprirationMethod The new expiration method, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the method is {@code null}.
   * 
   * @see ExpirationMethod
   * 
   */
  public final void setExprirationMethod (final ExpirationMethod exprirationMethod)
  {
    if (exprirationMethod == null)
      throw new IllegalArgumentException ();
    this.exprirationMethod = exprirationMethod;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX WAITING TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double maxWaitingTime;
  
  /** Returns the maximum waiting time.
   * 
   * <p>
   * Setting this maximum to {@link Double#POSITIVE_INFINITY} effectively disables (the applicable) expirations.
   * 
   * @return The maximum waiting time, non-negative, zero and {@link Double#POSITIVE_INFINITY} are allowed.
   * 
   */
  public final double getMaxWaitingTime ()
  {
    return this.maxWaitingTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double maxServiceTime;
  
  /** Returns the maximum service time.
   * 
   * <p>
   * Setting this maximum to {@link Double#POSITIVE_INFINITY} effectively disables (the applicable) expirations.
   * 
   * @return The maximum service time, non-negative, zero and {@link Double#POSITIVE_INFINITY} are allowed.
   * 
   */
  public final double getMaxServiceTime ()
  {
    return this.maxServiceTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX SOJOURN TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double maxSojournTime;
  
  /** Returns the maximum sojourn time.
   * 
   * <p>
   * Setting this maximum to {@link Double#POSITIVE_INFINITY} effectively disables (the applicable) expirations.
   * 
   * @return The maximum sojourn time, non-negative, zero and {@link Double#POSITIVE_INFINITY} are allowed.
   * 
   */
  public final double getMaxSojournTime ()
  {
    return this.maxSojournTime;
  }  

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE [SUBJECT TO RESET]:
  //   * (DELEGATE) JOBS CURRENTLY PRESENT AND THEIR ARRIVAL TIMES
  //   * SORTED-MAP HOLDING AUTO-REVOCATION TIMES FOR DELEGATE JOBS
  //   * THE CURRENTLY SCHEDULED AUTO-REVOCATION EVENT [IF PRESENT; ALSO IN eventsScheduled]
  //   * WHETHER OR NOT WE ARE CURRENTY PROCESSING AN AUTO-REVOCATION EVENT WE SCHEDULED OURSELVES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<DJ, Double> arrivalTimes = new HashMap<> ();
  
  private final SortedMap<Double, Set<DJ>> autoRevocationSchedule = new TreeMap<> ();
  
  private SimEvent scheduledRevocationEvent = null;
  
  /** Prevents rescheduling while processing our own revocations.
   * 
   */
  private boolean processingOwnAutoRevocationEvent = false;
  
  private boolean containsJobInAutoRevocationSchedule (final DJ job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    for (final Set<DJ> jSet : this.autoRevocationSchedule.values ())
      if (jSet.contains (job))
        return true;
    return false;
  }
  
  private void addNewJobToAutoRevocationSchedule (final DJ job, final double time)
  {
    if (job == null || time < getLastUpdateTime ())
      throw new IllegalArgumentException ();
    if (containsJobInAutoRevocationSchedule (job))
      throw new IllegalArgumentException ();
    if (! this.autoRevocationSchedule.containsKey (time))
      this.autoRevocationSchedule.put (time, new LinkedHashSet<> ());
    this.autoRevocationSchedule.get (time).add (job);    
  }
  
  private void removeJobFromAutoRevocationSchedule (final DJ job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! containsJobInAutoRevocationSchedule (job))
      throw new IllegalArgumentException ();
    final Set<Double> keysToRemove = new HashSet<> ();
    for (Entry<Double, Set<DJ>> entry : this.autoRevocationSchedule.entrySet ())
      if (entry.getValue ().contains (job))
      {
        entry.getValue ().remove (job);
        if (entry.getValue ().isEmpty ())
          keysToRemove.add (entry.getKey ());
      }
    for (final double d : keysToRemove)
      this.autoRevocationSchedule.remove (d);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE EXPIRATION EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void rescheduleExpirationEvent ()
  {
    // If we are currently processing an event, we just skip.
    // The event handler will rescheduleExpirationEvent once it has finished.
    if (this.processingOwnAutoRevocationEvent)
      return;
    // Check whether the currently scheduled auto-revocation event is still valid.
    if (this.scheduledRevocationEvent != null)
    {
      // We have a scheduled auto-revocation event; it should also be registered at our super-class, so let's check.
      if (! this.eventsScheduled.contains (this.scheduledRevocationEvent))
        throw new IllegalStateException ();
      if (this.autoRevocationSchedule.isEmpty ()
      ||  this.autoRevocationSchedule.firstKey () != this.scheduledRevocationEvent.getTime ())
      {
        // Our currently scheduled auto-revocation event is no longer valid;
        // remove it from the event list, our super-class administration of scheduled events and our local admin.
        getEventList ().remove (this.scheduledRevocationEvent);
        this.eventsScheduled.remove (this.scheduledRevocationEvent);
        this.scheduledRevocationEvent = null;
      }
      else
        // We have an auto-revocation event scheduled, but it is still OK, so we bail out.
        return;
    }
    // At this point no auto-revocation event is scheduled.
    if (! this.autoRevocationSchedule.isEmpty ())
    {
      // In view of our local admin, we have to schedule a new auto-revocation event.
      // We create the event, put it into our local admin and our super-class administration, and schedule it onto the event list.
      this.scheduledRevocationEvent
        = new DefaultSimEvent (this.autoRevocationSchedule.firstKey (), this::uponScheduledRevocationEvent);
      this.eventsScheduled.add (this.scheduledRevocationEvent);
      getEventList ().schedule (this.scheduledRevocationEvent);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXPIRE JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void expireJob (final double time, final J job)
  {
    switch (this.exprirationMethod)
    {
      case DROP:
        drop (job, time);
        break;
      case AUTO_REVOCATION:
        autoRevoke (time, job);
        break;
      case DEPARTURE:
        depart (time, job);
        break;
      default:
        throw new RuntimeException ();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AUTO-REVOCATION PROCESSER INVOKED FROM EVENT LIST
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void uponScheduledRevocationEvent (final SimEvent event)
  {
    if (event == null)
      throw new IllegalStateException ();
    if (event != this.scheduledRevocationEvent)
      throw new IllegalStateException ();    
    if (this.autoRevocationSchedule.isEmpty ())
      throw new IllegalStateException ("Illegal (empty) auto-revocation schedule; time=" + event.getTime () + ".");
    if (this.autoRevocationSchedule.firstKey () != event.getTime ())
      throw new IllegalStateException ();
    if (this.processingOwnAutoRevocationEvent)
      throw new IllegalStateException ();
    this.processingOwnAutoRevocationEvent = true;
    // Update; we are a top-level event and invoked from the event list.
    update (event.getTime ());
    final double time = event.getTime ();
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (! isTopLevel)
      // We must be a top-level event, because we are always invoked from the event list.
      throw new IllegalArgumentException ();
    final Set<DJ> jobsToRevoke = new LinkedHashSet<> (this.autoRevocationSchedule.get (this.autoRevocationSchedule.firstKey ()));
    for (final SimJob job : jobsToRevoke)
      if (! isDelegateJob ((DJ) job))
        throw new IllegalStateException ();
      else if (! getEncapsulatedQueue ().getJobs ().contains ((DJ) job))
        throw new IllegalStateException ();
    for (final SimJob job : jobsToRevoke)
    {
      if (! isDelegateJob ((DJ) job))
        continue;
      final J realJob = getRealJob ((DJ) job);
      expireJob (time, realJob);
    }
    this.eventsScheduled.remove (this.scheduledRevocationEvent);
    this.scheduledRevocationEvent = null;
    this.processingOwnAutoRevocationEvent = false;
    rescheduleExpirationEvent ();
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and resets the internal administration.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.autoRevocationSchedule.clear ();
    // Note: our super-class takes care of removing the event from the event list through the use of #eventsScheduled.
    this.scheduledRevocationEvent = null;
    this.processingOwnAutoRevocationEvent = false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void queueAccessVacationDropSubClass (double time, J job)
  {
    super.queueAccessVacationDropSubClass (time, job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and inserts the job in the local administration.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    super.insertJobInQueueUponArrival (job, time);
    final DJ delegateJob = getDelegateJobMild (job);
    // We always put the delegate job into our arrival-times map upon arrival.
    if (this.arrivalTimes.containsKey (delegateJob))
      throw new IllegalStateException ();
    this.arrivalTimes.put (delegateJob, time);
  }

  /** Assesses the expiration settings for the newly arrived job,
   *  and either makes it exit or reschedules the expiration event and calls super method.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (getMaxSojournTime () > 0
    &&  (getMaxWaitingTime () > 0
         || (getEncapsulatedQueue ().getNumberOfJobsInWaitingArea () == 0
             && getEncapsulatedQueue ().getServerAccessCredits () > 0
             && getEncapsulatedQueue ().isStartArmed ())))
    {
      // Calculate the expiration time for this visit.
      final DJ delegateJob = getDelegateJob (job);
      if (Double.isInfinite (getMaxWaitingTime ()) && Double.isInfinite (getMaxSojournTime ()))
        // First, if both MaxWaitingTime and MaxSojournTime are infinite,
        // we ignore both at this point.
        super.rescheduleAfterArrival (job, time);
      else if (Double.isInfinite (getLastUpdateTime ()))
        // Now we have a finite maximum waiting or sojourn time, but if time is infinite, we must alreay expire the job.
        expireJob (time, job);
      else
      {
        // At this point, time is finite, and so is the minimum of waiting-time and sojourn-time expiration.
        final double waiExpTime = time + getMaxWaitingTime ();
        final double sojExpTime = time + getMaxSojournTime ();
        final double expTime = Math.min (waiExpTime, sojExpTime);
        // Let's check...
        if (Double.isInfinite (expTime))
          throw new IllegalStateException ();
        // Time is finite, and the expiration time is finite.
        // Enter the job into the scheduled-auto-revocations administration.
        addNewJobToAutoRevocationSchedule (delegateJob, expTime);
        // Check to see if we need to rescheduleExpirationEvent the expiration event.
        // Note that we can safely do that already; before invoking super's rescheduling.
        rescheduleExpirationEvent ();
        // Let our super method pick up from here.
        super.rescheduleAfterArrival (job, time);
      }
    }
    else
      expireJob (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from the local administration and calls super method.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobUponExitLocal (job, time);
    super.removeJobFromQueueUponDrop (job, time);
  }

  /** Calls super method and reschedules the expiration event if needed.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    super.rescheduleAfterDrop (job, time);
    rescheduleUponExitLocal (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from the local administration and calls super method.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    removeJobUponExitLocal (job, time);
    super.removeJobFromQueueUponRevokation (job, time, auto);
  }

  /** Calls super method and reschedules the expiration event if needed.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    super.rescheduleAfterRevokation (job, time, auto);
    rescheduleUponExitLocal (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return super.isStartArmed ();
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    super.rescheduleForNewServerAccessCredits (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and performs sanity checks.
   * 
   * @throws IllegalStateException If sanity checks fail.
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    super.insertJobInQueueUponStart (job, time);
    // We always put the delegate job into our arrival-times map upon arrival.
    final DJ delegateJob = getDelegateJob (job);
    if (! this.arrivalTimes.containsKey (delegateJob))
      throw new IllegalStateException ();
  }

  /** Calls super method and assesses the expiration settings for the newly started job,
   *  and either makes it exit when necessary; always reschedules the expiration event.
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    super.rescheduleAfterStart (job, time);
    // For the started job, we simply need to recalculate the expiration time,
    // because we do not know whether the current expiration time, if applicable, is due to waiting time restraints or
    // due to sojourn time restraints.
    // Note that we cannot a priori assume that an auto-revocation is scheduled for the job at all...
    // Hence, first, let's remove all expirations for the current job.
    final DJ delegateJob = getDelegateJob (job);
    if (containsJobInAutoRevocationSchedule (delegateJob))
      removeJobFromAutoRevocationSchedule (delegateJob);
    // Calculate the expiration time for this visit.
    // First, if both MaxServiceTime and MaxSojournTime are infinite,
    // we ignore both at this point (but we still may have to rescheduleExpirationEvent!).
    // Note that MaxWaitingTime is no longer relevant at this point.
    if (Double.isFinite (getMaxServiceTime ()) || Double.isFinite (getMaxSojournTime ()))
    {
      // Now we have a finite maximum service or sojourn time, but if time is infinite, we must already exit the job.
      if (Double.isInfinite (getLastUpdateTime ()))
        expireJob (time, getRealJob (delegateJob));
      // At this point, time is finite, and so is the minimum of service-time and sojourn-time expiration.
      final double serExpTime = time + getMaxServiceTime ();
      final double sojExpTime = this.arrivalTimes.get (delegateJob) + getMaxSojournTime ();
      final double expTime = Math.min (serExpTime, sojExpTime);
      // Let's check...
      if (Double.isInfinite (expTime))
        throw new IllegalStateException ();
      // Time is finite, and the expiration time is finite.
      // Enter the job into the scheduled-auto-revocations administration.
      addNewJobToAutoRevocationSchedule (delegateJob, expTime);
    }
    // Check to see if we need to rescheduleExpirationEvent the auto-revocation event.
    rescheduleExpirationEvent ();      
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
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from the local administration and calls super method.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    removeJobUponExitLocal (departingJob, time);
    super.removeJobFromQueueUponDeparture (departingJob, time);
  }

  /** Calls super method and reschedules the expiration event if needed.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    super.rescheduleAfterDeparture (departedJob, time);
    rescheduleUponExitLocal (departedJob, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REMOVE JOB(LOCAL) UPON EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void removeJobUponExitLocal (final J job, final double time)
  {
    if (isJob (job))
    {
      // We always put the delegate job into our arrival-times map upon arrival.
      final DJ delegateJob = getDelegateJob (job);
      if (this.arrivalTimes.containsKey (delegateJob))
        this.arrivalTimes.remove (delegateJob);
      // Remove all expirations for the current job (if any).
      if (containsJobInAutoRevocationSchedule (delegateJob))
        removeJobFromAutoRevocationSchedule (delegateJob); 
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESCHEDULE (LOCAL) UPON EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void rescheduleUponExitLocal (final J job, final double time)
  {
    // Check to see if we need to rescheduleExpirationEvent the auto-revocation event.      
    rescheduleExpirationEvent ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    super.processSubQueueNotifications (notifications);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
