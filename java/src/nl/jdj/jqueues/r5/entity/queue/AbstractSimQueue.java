package nl.jdj.jqueues.r5.entity.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jqueues.r5.event.SimQueueJobDepartureEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractSimQueue<J extends SimJob, Q extends AbstractSimQueue>
  extends AbstractSimQueueBase<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Creates an abstract queue given an event list.
   *
   * @param eventList The event list to use.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   * 
   */
  protected AbstractSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INTERNAL STORAGE OF JOBS IN SYSTEM AND JOBS IN THE SERVIVE AREA
  // - jobQueue
  // - jobsInServiceArea (subset of jobQueue)
  //
  // TO BE MAINTAINED BY CONCRETE SUBCLASSES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Jobs currently in queue.
   * 
   * Note: this includes jobs in service (in the service area).
   *
   */
  protected final List<J> jobQueue = new ArrayList<> ();

  @Override
  public final Set<J> getJobs ()
  {
    return Collections.unmodifiableSet (new LinkedHashSet (this.jobQueue));
  }

  @Override
  public final int getNumberOfJobs ()
  {
    return this.jobQueue.size ();
  }

  /** Jobs currently being executed by the server(s).
   *
   * Any job in this set must also be in {@link #jobQueue}.
   * 
   */
  protected final Set<J> jobsInServiceArea
    = new HashSet<> ();

  @Override
  public final Set<J> getJobsInServiceArea ()
  {
    return Collections.unmodifiableSet (new LinkedHashSet (this.jobsInServiceArea));
  }

  @Override
  public final int getNumberOfJobsInServiceArea ()
  {
    return this.jobsInServiceArea.size ();
  }
  
  /** Overridden to make (default) implementation final.
   * 
   */
  @Override
  public final Set<J> getJobsInWaitingArea ()
  {
    return SimQueue.super.getJobsInWaitingArea ();
  }

  /** Overridden to make (default) implementation final.
   * 
   */
  @Override
  public final int getNumberOfJobsInWaitingArea ()
  {
    return SimQueue.super.getNumberOfJobsInWaitingArea ();
  }
  
  /** Returns whether or not this queue has at least one job waiting.
   * 
   * @return True if there are jobs waiting.
   * 
   * @see #getNumberOfJobsInWaitingArea
   * 
   */
  protected final boolean hasJobsInWaitingArea ()
  {
    return getNumberOfJobsInWaitingArea () > 0;
  }
  
  /** Returns the first job in {@link #getJobs} that <i>is not</i> in {@link #getJobsInServiceArea}.
   * 
   * @return The first job in {@link #getJobs} that is not in {@link #getJobsInServiceArea},
   *         <code>null</code> if there are no waiting jobs.
   * 
   */
  protected final J getFirstJobInWaitingArea ()
  {
    if (getNumberOfJobsInWaitingArea () == 0)
      return null;
    for (J j : this.jobQueue)
      if (! this.jobsInServiceArea.contains (j))
        return j;
    throw new IllegalStateException ();
  }

  /** Returns whether or not this queue has at least one job in the service area.
   * 
   * @return True if there are jobs in the service area.
   * 
   * @see #getNumberOfJobsInServiceArea
   * 
   */
  protected final boolean hasJobsInServiceArea ()
  {
    return ! this.jobsInServiceArea.isEmpty ();
  }
  
  /** Returns the first job in {@link #getJobs} that <i>is</i> in {@link #getJobsInServiceArea}.
   * 
   * @return The first job in {@link #getJobs} that is in {@link #getJobsInServiceArea},
             <code>null</code> if there are no jobs in the service area.
   * 
   */
  protected final J getFirstJobInServiceArea ()
  {
    if (getNumberOfJobsInServiceArea () == 0)
      return null;
    for (J j : this.jobQueue)
      if (this.jobsInServiceArea.contains (j))
        return j;
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENTS SCHEDULED
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Events scheduled on behalf of this {@link SimQueue}.
   * 
   * Any events in this set must also be in the {@link #eventList}.
   *
   */
  protected final Set<SimEvent> eventsScheduled
    = new HashSet<> ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Invokes super method, resets the last update time to negative infinity, removes all jobs without notifications,
   *  ends all vacations, and notifies listeners.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    for (SimJob j : this.jobQueue)
      j.setQueue (null);
    this.jobQueue.clear ();
    this.jobsInServiceArea.clear ();
    for (SimEvent e : this.eventsScheduled)
      getEventList ().remove (e);
    this.eventsScheduled.clear ();
    this.isQueueAccessVacation = false;
    this.serverAccessCredits = Integer.MAX_VALUE;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Handles an arrival at this queue.
   * 
   * <p>
   * This (final) implementation is as follows:
   * <ul>
   * <li> It first
   * <ul>
   * <li>makes sanity checks (e.g., job not already present),
   * <li>invokes {@link #update},
   * <li>invokes {@link #fireArrival} (notifying listeners of the arrival).
   * </ul>
   * 
   * <li>
   * Subsequently, if the queue is on queue-access vacation ({@link #isQueueAccessVacation}),
   * this method invokes {@link #fireDrop} and returns immediately;  bypassing {@link #drop}
   * since subclasses have no knowledge of the job yet (and they do not need to have).
   * 
   * <li>
   * Otherwise, it then invokes the subclass {@link #insertJobInQueueUponArrival},
   * and checks the presence of the job in {@link #jobQueue}:
   * 
   * <ul>
   * <li>If <i>not</i> present, sets the jobs queue to <code>null</code> and invokes {@link #fireDrop},
   *     again bypassing {@link #drop} because the job is not present in the system at this point.
   *     Also, in this case, there is no call to {@link #rescheduleAfterArrival}, since
   *     {@link #insertJobInQueueUponArrival} has denied immediately the queue visit.
   * <li>If present, sets this queue to be the visited queue on the job (with {@link SimJob#setQueue}),
   *     and invokes the queue-discipline specific {@link #rescheduleAfterArrival}.
   *     Subsequently, it performs sanity checks on the job in case it is no longer in {@link #jobQueue},
   *       which is a legal outcome of {@link #rescheduleAfterArrival}.
   *     Note however that if {@link #rescheduleAfterArrival} removes the job from {@link #jobQueue},
   *       it is itself responsible for notifying listeners of (either) drop, revocation or departure
   *       (and start for that matter, if applicable).
   * </ul>
   * 
   * </ul>
   * 
   * @see #update
   * @see #fireArrival
   * @see #isQueueAccessVacation
   * @see #fireDrop
   * @see #insertJobInQueueUponArrival
   * @see SimJob#setQueue
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  public final void arrive (final double time, final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (job.getQueue () != null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new RuntimeException ();
    update (time);
    fireArrival (time, job, (Q) this);
    if (this.isQueueAccessVacation)
      fireDrop (time, job, (Q) this);
    else
    {
      insertJobInQueueUponArrival (job, time);
      if (! this.jobQueue.contains (job))
      {
        if (this.jobsInServiceArea.contains (job))
          throw new IllegalStateException ();
        job.setQueue (null);  // Just in case it was erroneously set by our subclass...
        fireDrop (time, job, (Q) this);
      }
      else
      {
        if (this.jobsInServiceArea.contains (job))
          throw new IllegalStateException ();
        job.setQueue (this);
        rescheduleAfterArrival (job, time);
        if ((! this.jobQueue.contains (job))
        && (this.jobsInServiceArea.contains (job) || job.getQueue () == this))
          throw new IllegalStateException ();
      }
    }
  }

  /** Inserts a job that just arrived (at given time) into the internal queue(s).
   * 
   * <p>To be implemented by concrete queue types.
   * 
   * <p>
   * Implementations <i>must</i> (at least) add the job to {@link #jobQueue}. If not, the job is immediately marked for dropping,
   * and {@link #rescheduleAfterArrival} is not invoked!
   * 
   * <p>
   * Implementations must ignore any queue-access vacation as this is taken care of already by the base class.
   * 
   * <p>
   * Implementations must <i>not</i>reschedule on the event list, or make changes to {@link #jobsInServiceArea},
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterArrival} for that.
   * 
   * <p>
   * Implementations should not set the queue on the job, as this is done by the base class.
   * See {@link SimJob#setQueue}.
   * 
   * <p>
   * Implementations do no have to invoke {@link #update}; this has been done already before the call to this method.
   * 
   * @param job The job that arrived.
   * @param time The current time (i.e., arrival time of the job).
   * 
   * @see #arrive
   * @see #rescheduleAfterArrival
   * 
   */
  protected abstract void insertJobInQueueUponArrival (J job, double time);
  
  /** Reschedules after a job arrival.
   * 
   * The job has already been put into the internal data structures, and listeners/actions have already been notified of the
   * arrival. Also, it is guaranteed that there are no queue-access vacations, as these are being dealt with by the
   * base class.
   * 
   * <p>
   * {@link #update} should not be called.
   * 
   * <p>This method should maintain the {@link #jobsInServiceArea} data.
   * Normally it should <i>not</i>mangle the {@link #jobQueue} members, as the job set cannot change as a result of this
   * method. However, the only exception is that the callee may remove the jobs from {@link #jobQueue}, <i>leaving all
   * other jobs untouched</i>, which is considered by the caller {@link #arrive} that the job is to depart immediately.
   * Note that in that particular case, the caller assumes that <i>no</i> departure events have been scheduled for this job,
   * and it does <i>not</i> invoke {@link #rescheduleAfterDeparture} or {@link #removeJobFromQueueUponDeparture} for this job.
   * 
   * @param job The job that arrived (and is present in {@link #jobQueue}).
   * @param time  The current time (i.e., the arrival time of the job).
   * 
   * @see #arrive
   * @see #insertJobInQueueUponArrival
   * 
   */
  protected abstract void rescheduleAfterArrival (J job, double time);

  /** Schedules a job arrival at this {@link AbstractSimQueue} on its {@link SimEventList}.
   * 
   * <p>
   * Convenience method.
   * 
   * @param time The arrival time of the job, which must be in the future.
   * @param job  The job to arrive.
   * 
   * @throws IllegalArgumentException If <code>time</code> is in the past, or <code>job</code> is <code>null</code>.
   * 
   * @see #arrive
   * @see #getLastUpdateTime
   * @see SimEventList#getTime
   * 
   */
  public final void scheduleJobArrival (final double time, final J job)
  {
    if (time < this.getLastUpdateTime () || time < getEventList ().getTime () || job == null)
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.scheduleJobArrival (job, this, time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean isQueueAccessVacation = false;
  
  /** Returns whether or not this queue is on queue-access vacation (from an internal flag).
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses.
   * 
   */
  @Override
  public final boolean isQueueAccessVacation ()
  {
    return this.isQueueAccessVacation;
  }
  
  /** Starts or ends a queue-access vacation.
   * 
   * <p>
   * This final implementation just sets the internal flag indicating a queue-access vacation or not.
   * The a-priori call to {@link #update} and the a-posteriori call to
   * {@link #fireStartQueueAccessVacation} or {@link #fireStopQueueAccessVacation}
   * are only effectuated if the vacation status queue actually changed.
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses.
   * 
   */
  @Override
  public final void setQueueAccessVacation (final double time, final boolean start)
  {
    boolean notify = (this.isQueueAccessVacation != start);
    if (notify)
      update (time);
    this.isQueueAccessVacation = start;
    if (notify)
    {
      if (start)
        fireStartQueueAccessVacation (time);
      else
        fireStopQueueAccessVacation (time);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Drops a job from this queue, on the queue's initiative.
   * 
   * The final implementation makes sanity checks (e.g., job present),
   * and invokes {@link #update}.
   * It then invokes the subclass {@link #removeJobFromQueueUponDrop},
   * and checks the absence of the job in {@link #jobQueue} and {@link #jobsInServiceArea}
   * (throwing an {@link IllegalStateException} if not).
   * It sets the visited queue on the job (with {@link SimJob#setQueue}) to <code>null</code>,
   * and notifies the drop to listeners and actions
   * ({@link #fireDrop}.
   * Finally, it invokes the queue-discipline specific {@link #rescheduleAfterDrop}.
   * 
   * @param job The job to be dropped.
   * @param time The current time, i.e., the drop time of the job.
   * 
   * @throws IllegalArgumentException If the job is <code>null</code> or not found.
   * @throws IllegalStateException    If the internal administration of this queue has become inconsistent. 
   * 
   */
  protected final void drop (final J job, final double time)
  {
    if (job == null || job.getQueue () != this || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    update (time);
    removeJobFromQueueUponDrop (job, time);
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    job.setQueue (null);
    fireDrop (time, job, (Q) this);
    rescheduleAfterDrop (job, time);
  }

  /** Removes a job from the internal queue(s) because it is to be dropped.
   * 
   * <p>To be implemented by concrete queue types.
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsInServiceArea} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i>reschedule events for <i>other</i> jobs on the event list,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterDrop} for that.
   * 
   * <p>
   * Implementations must not fire drop events. This is done by the base class {@link AbstractSimQueue}.
   * 
   * @param job The job that is to be dropped.
   * @param time The current time (i.e., drop time of the job).
   * 
   * @see #drop
   * @see #rescheduleAfterDrop
   * 
   */
  protected abstract void removeJobFromQueueUponDrop (J job, double time);
  
  /** Reschedules if needed after a job has been dropped from this queue.
   * 
   * <p>
   * Implementations can rely on the fact that the job is no longer present in the internal data structures,
   * that it has no pending events on the event list,
   * and that this method is invoked immediately after {@link #removeJobFromQueueUponDrop}.
   * 
   * <p>
   * Implementations must not fire drop events. This is done by the base class {@link AbstractSimQueue}.
   * They must, however, fire appropriate events for jobs that start in this method,
   * and update {@link #jobsInServiceArea} accordingly.
   * 
   * @param job  The jobs that was dropped.
   * @param time The current time (i.e., drop time of the job).
   * 
   * @see #drop
   * @see #removeJobFromQueueUponDrop
   * 
   */
  protected abstract void rescheduleAfterDrop (J job, double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOKE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Revokes a job from this queue.
   * 
   * <p>
   * The final implementation makes sanity checks (e.g., job present),
   * invokes {@link #update}.
   * It then checks whether the revocation is to be refused
   * which is the case when the job has already started and {@code interruptService == false}.
   * If revocation is to be refused, this method simple returns {@code false}.
   * 
   * <p>
   * Otherwise, this method invokes the subclass-specific {@link #removeJobFromQueueUponRevokation}.
   * Upon return, it checks the absence of the job in {@link #jobQueue} and {@link #jobsInServiceArea}
   * (throwing an {@link IllegalStateException} if not).
   * It sets the visited queue on the job (with {@link SimJob#setQueue}) to <code>null</code>,
   * and notifies the drop to listeners and actions
   * ({@link #fireRevocation}.
   * 
   * <p>
   * Finally, it invokes the queue-discipline specific {@link #rescheduleAfterRevokation}.
   * 
   */
  @Override
  public final boolean revoke
    (final double time, final J job, final boolean interruptService)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
    {
      if (job.getQueue () == this)
        throw new IllegalStateException ();
      else
        return false;
    }
    update (time);
    if ((! interruptService) && getJobsInServiceArea ().contains (job))
      return false;
    removeJobFromQueueUponRevokation (job, time);
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();    
    job.setQueue (null);
    fireRevocation (time, job, (Q) this);
    rescheduleAfterRevokation (job, time);
    return true;
  }

  /** Calls super method (and makes it final).
   * 
   * @param time The time at which the request is issued, i.c., the current time.
   * @param job  The job to be revoked from the queue.
   * 
   */
  @Override
  public final void revoke (final double time, final J job)
  {
    super.revoke (time, job);
  }
    
  /** Removes a job from the internal queue(s) since it is revoked.
   * 
   * <p>To be implemented by concrete queue types.
   *
   * <p>
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsInServiceArea} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i>reschedule events for <i>other</i> jobs on the event list,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterRevokation} for that.
   * 
   * <p>
   * Implementations must not fire revocation events. This is done by the base class {@link AbstractSimQueue}.
   * 
   * @param job The job that is to be revoked.
   * @param time The current time (i.e., revocation time of the job).
   * 
   * @see #revoke
   * @see #rescheduleAfterRevokation
   * 
   */
  protected abstract void removeJobFromQueueUponRevokation (J job, double time);
  
  /** Reschedules if needed after a job has been revoked from this queue.
   * 
   * <p>
   * Implementations can rely on the fact that the job is no longer present in the internal data structures,
   * that it has no pending events on the event list,
   * and that this method is invoked immediately after a successful {@link #removeJobFromQueueUponRevokation}.
   * 
   * <p>
   * Implementations must not fire revocation events. This is done by the base class {@link AbstractSimQueue}.
   * They must, however, fire appropriate events for jobs that start in this method,
   * and update {@link #jobsInServiceArea} accordingly.
   * 
   * @param job  The jobs that was successfully revoked.
   * @param time The current time (i.e., revocation time of the job).
   * 
   * @see #revoke
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  protected abstract void rescheduleAfterRevokation (J job, double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private int serverAccessCredits = Integer.MAX_VALUE;
  
  @Override
  public final int getServerAccessCredits ()
  {
    return this.serverAccessCredits;
  }
  
  /** Sets the remaining number of server-access credits.
   * 
   * This final implementation invokes {@link #update} only if the number of credits passed in the argument differs from the
   * current number of credits, to serve statistics on the number of server-access credits.
   * The method does nothing if the number has not changed.
   * Otherwise, it updates the internal administration of the number of server-access credits.
   * Subsequently, it invokes {@link #setServerAccessCreditsSubClass} to notify interested subclasses of the new value for
   * the server-access-credits,
   * and {@link #fireIfOutOfServerAccessCredits} or {@link #fireRegainedServerAccessCredits} if appropriate.
   * In the latter case, it also invokes {@link #rescheduleForNewServerAccessCredits}.
   * 
   * @see #getServerAccessCredits
   * 
   */
  @Override
  public final void setServerAccessCredits (final double time, final int credits)
  {
    if (credits < 0)
      throw new IllegalArgumentException ();
    final int oldCredits = this.serverAccessCredits;
    if (oldCredits != credits)
    {
      update (time);
      this.serverAccessCredits = credits;
      if (oldCredits == 0 && credits > 0)
        rescheduleForNewServerAccessCredits (time);
      setServerAccessCreditsSubClass ();
      fireIfNewServerAccessCreditsAvailability (time);
    }
  }
  
  /** Returns <code>true</code> if this queue has server-access credits left.
   * 
   * @return <code>True</code> if this queue has server-access credits left.
   * 
   */
  protected final boolean hasServerAcccessCredits ()
  {
    return this.serverAccessCredits > 0;
  }
  
  /** Takes a single server-access credit, which must be  available.
   * 
   * Throws an {@link IllegalStateException} if there are no server-access credits, in other words,
   * caller must check this first.
   * 
   * <p>
   * This method has no effect (and does not fire notifications) if the number of
   * server-access credits is {@link Integer#MAX_VALUE}, which is treated as infinity.
   * Otherwise, it invokes {@link #update} to serve statistics on the number of server-access credits.
   * 
   * <p>
   * If the boolean argument is <code>true</code>
   * and if the last server-access credit is granted in an invocation,
   * {@link #fireIfOutOfServerAccessCredits} is fired.
   * 
   * @param fireIfOut If <code>true</code>, fires a notification if there are no server-access credits left after taking one.
   * 
   * @throws IllegalStateException If there are no server-access credits left upon entry.
   * 
   * @see #hasServerAcccessCredits
   * @see #getLastUpdateTime
   * 
   */
  protected final void takeServerAccessCredit (final boolean fireIfOut)
  {
    if (this.serverAccessCredits <= 0)
      throw new IllegalStateException ();
    // Integer.MAX_VALUE is treated as infinity.
    if (this.serverAccessCredits < Integer.MAX_VALUE)
    {
      // XXX Why are we calling update here? Supposed to have been called already!
      update (getEventList ().getTime ());      
      this.serverAccessCredits--;
      if (fireIfOut && this.serverAccessCredits == 0)
        fireIfOutOfServerAccessCredits (getLastUpdateTime ());
    }
  }
  
  /** Reschedules if needed due to the (new) availability of server-access credits.
   * 
   * <p>
   * Implementations can rely on the availability of server-access credits previously (conform the current schedule) unavailable.
   * 
   * <p>
   * Implementations must not fire server-access-credits events. This is done by the base class {@link AbstractSimQueue}.
   * They must, however, fire appropriate events for jobs that start in this method,
   * and update {@link #jobsInServiceArea} accordingly.
   * 
   * @param time The current time (i.e., the time at which new server-access credits became available).
   * 
   * @see #setServerAccessCredits
   * @see #hasServerAcccessCredits
   * 
   */
  protected abstract void rescheduleForNewServerAccessCredits (double time);

  /** Notifies subclasses of a new externally-set value for the server-access credits through {@link #setServerAccessCredits}.
   * 
   * <p>
   * Typical implementations would not be very interested in the <i>actual</i> value of the server-access credits,
   * as long as it is strictly positive and properly maintained by this abstract base class.
   * Hence, changes to the actual server-access credits are only reported
   * if the credits become zero (prohibiting the start of jobs),
   * or become non-zero (allowing waiting jobs to start).
   * 
   * <p>
   * This method, however, allows subclasses to closely follow (and take action upon) the exact value of the server-access
   * credits if changed through {@link #setServerAccessCredits}, i.e., changed by an external entity.
   * 
   * <p>
   * The method is called from {@link #setServerAccessCredits} after the new value has been effectuated into the internal
   * administration, rescheduling has taken place but (right) <i>before</i> any listeners have been notified.
   * Concrete implementations should not change the server-access-credits, obviously,
   * and should try to refrain from issuing {@link SimQueue}-level notifications.
   * 
   * 
   * <p>
   * The default implementation does nothing.
   * 
   * @see #getServerAccessCredits
   * @see #setServerAccessCredits
   * @see #rescheduleForNewServerAccessCredits
   * @see #fireIfOutOfServerAccessCredits
   * @see #fireRegainedServerAccessCredits
   * 
   */
  protected void setServerAccessCreditsSubClass ()
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Deals with a departure event from the event list.
   * 
   * This method check the presence of the departure event in {@link #eventsScheduled},
   * throwing an exception if absent,
   * and removes the event from that collection.
   * It then grabs the time and job parameters from the event argument, and
   * invokes {@link #depart} with a {@code true} value for
   * the {@code notify} argument.
   * 
   * @param event The departure event; must be non-<code>null</code> and present in {@link #eventsScheduled}.
   * 
   * @see #eventsScheduled
   * @see DefaultDepartureEvent
   * @see #scheduleDepartureEvent
   * @see #depart
   * 
   */
  protected final void departureFromEventList (final DefaultDepartureEvent<J, Q> event)
  {
    if (event == null)
      throw new RuntimeException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalStateException ();
    this.eventsScheduled.remove (event);
    final double time = event.getTime ();
    final J job = event.getJob ();
    depart (time, job, true);
  }
  
  /** Deals with a departure.
   * 
   * <p>
   * Invokes {@link #update},
   * and takes care of administration of the internal data, i.e.,
   * clearing the job's queue {@link SimJob#setQueue},
   * invoking {@link #removeJobFromQueueUponDeparture}
   * (which must remove the job from the {@link #jobQueue}
   * and the {@link #jobsInServiceArea} lists).
   * It then invokes the discipline-specific {@link #rescheduleAfterDeparture}
   * followed by {@link #fireDeparture}.
   * 
   * <p>
   * Note that this method does <i>not</i> (attempt to) cancel a {@link DefaultDepartureEvent}
   * for the job, nor does it maintain {@link #eventsScheduled}!
   * 
   * @param time   The departure time.
   * @param job    The job that departs.
   * @param notify Whether a notification should be sent to listeners.
   * 
   * @see #removeJobFromQueueUponDeparture
   * @see #rescheduleAfterDeparture
   * @see #fireDeparture
   * 
   */
  protected final void depart (final double time, final J job, final boolean notify)
  {
    if (job.getQueue () != this)
      throw new IllegalStateException ();
    update (time);
    job.setQueue (null);
    removeJobFromQueueUponDeparture (job, time);
    if (this.jobQueue.contains (job)
      || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    rescheduleAfterDeparture (job, time);    
    if (notify)
      fireDeparture (time, job, (Q) this);
  }
  
  /** Removes a job from the internal queues upon departure.
   * 
   * <p>
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a queue-specific departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsInServiceArea} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i>reschedule events for <i>other</i> jobs on the event list,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterDeparture} for that.
   * 
   * <p>
   * Implementations must not fire departure events. This is done by the base class {@link AbstractSimQueue}.
   * 
   * @param departingJob The job that departs.
   * @param time The departure (current) time.
   * 
   * @see #depart
   * @see #rescheduleAfterDeparture
   * 
   */
  protected abstract void removeJobFromQueueUponDeparture (J departingJob, double time);
  
  /** Reschedules if needed after a job departure has been effectuated by the base class.
   * 
   * <p>
   * Implementations can rely on the fact that the job is no longer present in the internal data structures,
   * that it has no pending events on the event list,
   * and that this method is invoked immediately after {@link #removeJobFromQueueUponDeparture}.
   * 
   * <p>
   * Implementations must not fire departure events. This is done by the base class {@link AbstractSimQueue}.
   * They must, however, fire appropriate events for jobs that start in this method,
   * and update {@link #jobsInServiceArea} accordingly.
   * 
   * @param departedJob The departed job.
   * @param time The departure (current) time.
   * 
   * @see #depart
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  protected abstract void rescheduleAfterDeparture (J departedJob, double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The default {@link SimEvent} used internally for scheduling {@link SimJob} departures.
   * 
   * <p>The {@link DefaultDepartureEvent} (actually, its {@link SimEventAction}), once activated,
   * calls {@link #departureFromEventList}.
   * 
   * <p>
   * Implementations are encouraged to avoid creation of {@link DefaultDepartureEvent}s
   * for each departure, but instead reuse instances whenever possible.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  protected final static class DefaultDepartureEvent<J extends SimJob, Q extends AbstractSimQueue>
  extends SimQueueJobDepartureEvent<J, Q>
  {
    
    /** Creates the actions that invokes {@link AbstractSimQueue#departureFromEventList} on the queue,
     *  and invokes the super method.
     * 
     * @param departureTime The scheduled departure time.
     * @param queue         The queue for which the departure is scheduled.
     * @param job           The job that is to depart.
     * 
     * @throws IllegalArgumentException If the job or the queue is {@code null}.
     * 
     */
    public DefaultDepartureEvent
      (final double departureTime,
      final Q queue,
      final J job)
    {
      super (job, queue, departureTime, new SimEventAction ()
      {
        @Override
        public final void action (final SimEvent event)
        {
          queue.departureFromEventList ((DefaultDepartureEvent) event);
        }
      });
    }
      
  }

  /** Schedules a suitable {@link SimEvent} for a job's future departure on the event list.
   * 
   * The implementation requires passing several rigorous sanity checks,
   * after which it creates a new {@link DefaultDepartureEvent},
   * adds it to {@link #eventsScheduled} and schedules the new event on the event list.
   * Effectively, this ensures that unless the event is canceled,
   * the method {@link #departureFromEventList} is invoked upon reaching the departure event.
   * 
   * <p>
   * The base class {@link AbstractSimQueue} does not use this method; it is provided as a service to subclasses.
   * 
   * @param time The departure time.
   * @param job  The job to depart.
   * 
   * @return The event created and scheduled on the event list.
   * 
   * @see #getEventList
   * @see #getLastUpdateTime
   * 
   */
  protected final DefaultDepartureEvent scheduleDepartureEvent (final double time, final J job)
  {
    if (time < getLastUpdateTime () || job == null || job.getQueue () != this)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    // The following check is an error; jobs may depart without receiving any service at all!
    // if (! this.jobsInServiceArea.contains (job))
    //   throw new IllegalArgumentException ();
    final DefaultDepartureEvent<J, Q> event = new DefaultDepartureEvent (time, this, job);
    SimEntityEventScheduler.schedule (getEventList (), event);
    this.eventsScheduled.add (event);
    return event;
  }
  
  /** Cancels a pending departure event on the event list.
   * 
   * After several rigorous sanity checks, this default implementation
   * removes the event from the event list and from {@link #eventsScheduled}.
   * 
   * <p>
   * The base class {@link AbstractSimQueue} does not use this method; it is provided as a service to subclasses.
   * 
   * @param event The departure event to cancel.
   * 
   * @see #eventsScheduled
   * @see #getEventList
   * 
   */
  protected final void cancelDepartureEvent (final DefaultDepartureEvent<J, Q> event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! getEventList ().contains (event))
      throw new IllegalArgumentException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (event.getJob ()))
      throw new IllegalArgumentException ();
    this.eventsScheduled.remove (event);
    getEventList ().remove (event);
  }
  
  /** Cancels a pending departure event for given job on the event list.
   * 
   * <p>
   * After several rigorous sanity checks, this default implementation
   * removes the event from the event list and from {@link #eventsScheduled}.
   * Note that a unique {@link DefaultDepartureEvent} must be found in {@link #eventsScheduled} for the job supplied,
   * otherwise a {@link IllegalArgumentException} is thrown.
   * 
   * <p>
   * The base class {@link AbstractSimQueue} does not use this method; it is provided as a service to subclasses.
   * 
   * @param job The job for which the unique departure event to cancel.
   * 
   * @throws IllegalArgumentException If zero or multiple {@link DefaultDepartureEvent}s
   *                                  were found for the job in {@link #eventsScheduled}.
   * 
   * @see #eventsScheduled
   * @see #getEventList
   * @see #getDepartureEvents
   * 
   */
  protected final void cancelDepartureEvent (final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    final Set<DefaultDepartureEvent<J, Q>> set = getDepartureEvents (job);
    if (set == null || set.size () != 1)
      throw new IllegalArgumentException ();
    cancelDepartureEvent (set.iterator ().next ());
  }
  
  /** Gets all departure events.
   * 
   * The (final) implementation returns all {@link DefaultDepartureEvent}s in {@link #eventsScheduled}.
   * 
   * @return A non-<code>null</code> {@link Set} holding all future departure events.
   * 
   */
  protected final Set<DefaultDepartureEvent<J, Q>> getDepartureEvents ()
  {
    final Set<DefaultDepartureEvent<J, Q>> set = new LinkedHashSet<> ();
    for (final SimEvent e : this.eventsScheduled)
      if (e == null)
        throw new IllegalStateException ();
      // JdJ20150913: I have no clue why the next statement does not work...
      // XXX TBD
      // else if (! (e instanceof DefaultDepartureEvent))
      //  continue;
      else if (! DefaultDepartureEvent.class.isAssignableFrom (e.getClass ()))
        /* continue */ ;
      else
        set.add ((DefaultDepartureEvent) e);
    return set;
  }
  
  /** Gets all departure events for given job.
   * 
   * The (final) implementation returns all {@link DefaultDepartureEvent}s for the given job in {@link #eventsScheduled}.
   * 
   * @param job The job.
   * 
   * @return A non-<code>null</code> {@link Set} holding all scheduled departure events for the job.
   * 
   */
  protected final Set<DefaultDepartureEvent<J, Q>> getDepartureEvents (final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    final Set<DefaultDepartureEvent<J, Q>> set = new LinkedHashSet<> ();
    for (SimEvent e : this.eventsScheduled)
      if (e == null)
        throw new IllegalStateException ();
      // JdJ20150913: I have no clue why the next statement does not work...
      // XXX TBD
      // else if (! (e instanceof DefaultDepartureEvent))
      //  continue;
      else if (! DefaultDepartureEvent.class.isAssignableFrom (e.getClass ()))
        /* continue */ ;
      else if (((DefaultDepartureEvent) e).getJob () != job)
        /* continue */ ;
      else
        set.add ((DefaultDepartureEvent) e);
    return set;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME, toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "AbstractSimQueue".
   * 
   * @return "AbstractSimQueue".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "AbstractSimQueue";
  }
  
}
