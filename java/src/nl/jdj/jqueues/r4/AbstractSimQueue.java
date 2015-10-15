package nl.jdj.jqueues.r4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a {@link SimQueue}.
 * 
 * <p>All concrete subclasses of {@link AbstractSimQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
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
  // INTERNAL STORAGE OF JOBS IN SYSTEM AND JOBS EXECUTING
  // - jobQueue
  // - jobsExecuting (subset of jobQueue)
  //
  // TO BE MAINTAINED BY CONCRETE SUBCLASSES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Jobs currently in queue.
   * 
   * Note: this includes jobs in service (executing).
   *
   */
  protected final List<J> jobQueue = new ArrayList<> ();

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
  protected final Set<J> jobsExecuting
    = new HashSet<> ();

  @Override
  public final int getNumberOfJobsExecuting ()
  {
    return this.jobsExecuting.size ();
  }
  
  /** Returns a {@link LinkedHashSet} holding the jobs waiting.
   * 
   * In case there are no jobs waiting, an empty set is returned.
   * 
   * <p>
   * The jobs are inserted into the return set in order of appearance in {@link #jobQueue}.
   * 
   * @return A new {@link LinkedHashSet} holding the jobs waiting.
   * 
   */
  public final Set<J> getJobsWaiting ()
  {
    final Set<J> set = new LinkedHashSet<> (this.jobQueue);
    set.removeAll (this.jobsExecuting);
    return set;
  }

  /** Returns the number of jobs waiting.
   * 
   * @return The number of jobs waiting.
   * 
   */
  public final int getNumberOfJobsWaiting ()
  {
    return this.jobQueue.size () - this.jobsExecuting.size ();
  }
  
  /** Returns whether or not this queue has at least one job waiting.
   * 
   * @return True if there are jobs waiting.
   * 
   * @see #getNumberOfJobsWaiting
   * 
   */
  protected final boolean hasJobsWaiting ()
  {
    return getNumberOfJobsWaiting () > 0;
  }
  
  /** Returns the first job waiting found in {@link #jobQueue}.
   * 
   * @return The first job waiting found in {@link #jobQueue}, <code>null</code> if there is no waiting job.
   * 
   */
  protected final J getFirstJobWaiting ()
  {
    if (getNumberOfJobsWaiting () == 0)
      return null;
    for (J j : this.jobQueue)
      if (! this.jobsExecuting.contains (j))
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
  protected final Set<SimEvent<J>> eventsScheduled
    = new HashSet<> ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Resets the last update time to negative infinity, removes all jobs without notifications,
   * and ends all vacations.
   * 
   */
  @Override
  public void reset ()
  {
    final double oldTime = this.lastUpdateTime;
    this.lastUpdateTime = Double.NEGATIVE_INFINITY;
    for (SimJob j : this.jobQueue)
      j.setQueue (null);
    this.jobQueue.clear ();
    this.jobsExecuting.clear ();
    for (SimEvent e : this.eventsScheduled)
      getEventList ().remove (e);
    this.eventsScheduled.clear ();
    getEventList ().remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = false;
    this.serverAccessCredits = Integer.MAX_VALUE;
    fireReset (oldTime);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** The last update time of this queue.
   * 
   */
  private double lastUpdateTime = Double.NEGATIVE_INFINITY;

  /** Gets the time of the last update of this queue.
   * 
   * @return The time of the last update of this queue.
   * 
   * @see #update
   * @see SimQueueListener#notifyUpdate
   * 
   */
  public final double getLastUpdateTime ()
  {
    return this.lastUpdateTime;
  }
  
  /** Updates this queue (primarily for internal use).
   * 
   * For a precise definition of an update of a queue, refer to {@link SimQueueListener#notifyUpdate}.
   * 
   * <p>
   * This method can be safely called by external parties at any time, at the expense of listener notifications.
   * Appropriate occasions for it are at the start and at the end of a simulation.
   * 
   * <p>
   * Because subclasses may present a more refined model of queue updates, this method is not <code>final</code>.
   * 
   * <p>
   * This implementation only notifies the queues listeners, and updates its internal time (in that order!).
   * 
   * @param time The time of the update (i.c., the current time).
   * 
   * @see SimQueueListener#notifyUpdate
   * @see #fireUpdate
   * 
   */
  public void update (final double time)
  {
    if (time < this.lastUpdateTime)
      throw new IllegalStateException ();
    if (time > this.lastUpdateTime)
    {
      fireUpdate (time);
      this.lastUpdateTime = time;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * {@inheritDoc}
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
  public final void arrive (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (job.getQueue () != null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (job) || this.jobsExecuting.contains (job))
      throw new RuntimeException ();
    update (time);
    fireArrival (time, job);
    if (this.isQueueAccessVacation)
      fireDrop (time, job);
    else
    {
      insertJobInQueueUponArrival (job, time);
      if (! this.jobQueue.contains (job))
      {
        if (this.jobsExecuting.contains (job))
          throw new IllegalStateException ();
        job.setQueue (null);  // Just in case it was erroneously set by our subclass...
        fireDrop (time, job);
      }
      else
      {
        if (this.jobsExecuting.contains (job))
          throw new IllegalStateException ();
        job.setQueue (this);
        rescheduleAfterArrival (job, time);
        if ((! this.jobQueue.contains (job))
        && (this.jobsExecuting.contains (job) || job.getQueue () == this))
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
   * Implementations must <i>not</i>reschedule on the event list, or make changes to {@link #jobsExecuting},
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterArrival} for that.
   * 
   * <p>
   * Implementations should not set the queue on the job, as this is done by the base class.
   * See {@link SimJob#setQueue}.
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
   * <p>This method should maintain the {@link #jobsExecuting} data.
   * Normally it should <i>not</i>mangle the {@link #jobQueue} members, as the job set cannot change as a result of this
   * method. However, the only exception is that the callee may remove the jobs from {@link #jobQueue}, <i>leaving all
   * other jobs untouched</i>, which is considered by the caller {@link #arrive} that the job is to depart immediately.
   * Note that in that particular case, the caller assumes that <i>no</i> departure events have been scheduled for this job,
   * and it does </not> invoke {@link #rescheduleAfterDeparture} or {@link #removeJobFromQueueUponDeparture} for this job.
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
   * Convenience method.
   * 
   * @param time The arrival time of the job, which must be in the future.
   * @param job The job to arrive.
   * 
   * @return The event created (and already scheduled).
   * 
   * @throws IllegalArgumentException If <code>time</code> is in the past, or <code>job</code> is <code>null</code>.
   * 
   * @see #arrive
   * @see #getLastUpdateTime
   * @see SimEventList#getTime
   * 
   */
  public final SimEvent<J> scheduleJobArrival (final double time, final J job)
  {
    if (time < this.lastUpdateTime || time < getEventList ().getTime () || job == null)
      throw new IllegalArgumentException ();
    final SimEvent arrivalEvent = new SimEvent<> (time, null, new SimEventAction ()
    {
      @Override
      public void action (SimEvent event)
      {
        AbstractSimQueue.this.arrive (job, time);
      }
    });
    getEventList ().add (arrivalEvent);
    return arrivalEvent;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean isQueueAccessVacation = false;
  
  /**
   * {@inheritDoc}
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
  
  /** The single {@link SimEventAction} used to wakeup the queue from queue-access vacations.
   * 
   * The class implements {@link SimEventAction#action} by calling {@link AbstractSimQueue#stopQueueAccessVacationFromEventList},
   * with time argument taken from the event passed in {@link SimEventAction#action}.
   * 
   */
  protected final SimEventAction END_QUEUE_ACCESS_VACATION_ACTION
    = new SimEventAction<J> ()
          {
            @Override
            public void action
              (final SimEvent<J> event)
            {
              AbstractSimQueue.this.stopQueueAccessVacationFromEventList (event.getTime ());
            }
          };

  /** The single {@link SimEvent} used to wakeup the queue from queue-access vacations.
   * 
   * The event has {@link #END_QUEUE_ACCESS_VACATION_ACTION} as its immutable {@link SimEventAction}.
   * 
   */
  protected final SimEvent END_QUEUE_ACCESS_VACATION_EVENT
    = new SimEvent (0.0, null, this.END_QUEUE_ACCESS_VACATION_ACTION);

  /**
   * {@inheritDoc}
   * 
   * <p>
   * This final implementation just removes any scheduled {@link #END_QUEUE_ACCESS_VACATION_EVENT} from the event list,
   * and sets the internal flag indicating a queue-access vacation.
   * The a-priori call to {@link #update} and the a-posteriori call to {@link #fireStartQueueAccessVacation} are only
   * effectuated if the queue was not already in queue-access vacation.
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses.
   * 
   */
  @Override
  public final void startQueueAccessVacation ()
  {
    boolean notify = ! this.isQueueAccessVacation;
    if (notify)
      update (getEventList ().getTime ());
    getEventList ().remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = true;
    if (notify)
      fireStartQueueAccessVacation (getEventList ().getTime ());
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * This final implementation just (re)schedules {@link #END_QUEUE_ACCESS_VACATION_EVENT} on the event list,
   * and sets the internal flag indicating a queue-access vacation.
   * The a-priori call to {@link #update} and the a-posteriori call to {@link #fireStartQueueAccessVacation} are only
   * effectuated if the queue was not already in queue-access vacation.
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses.
   * 
   */
  @Override
  public final void startQueueAccessVacation (final double duration)
  {
    boolean notify = ! this.isQueueAccessVacation;
    if (duration < 0)
      throw new IllegalArgumentException ();
    if (notify)
      update (getEventList ().getTime ());
    getEventList ().remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.END_QUEUE_ACCESS_VACATION_EVENT.setTime (getEventList ().getTime () + duration);
    getEventList ().add (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = true;
    if (notify)
      fireStartQueueAccessVacation (getEventList ().getTime ());
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * This final implementation just removes {@link #END_QUEUE_ACCESS_VACATION_EVENT} from the event list,
   * and resets the internal flag indicating queue-access vacation.
   * The a-priori call to {@link #update} and the a-posteriori call to {@link #fireStartQueueAccessVacation} are only
   * effectuated if the queue was not already in queue-access vacation.
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses.
   * 
   */
  @Override
  public final void stopQueueAccessVacation ()
  {
    boolean notify = this.isQueueAccessVacation;
    if (notify)
      update (getEventList ().getTime ());
    getEventList ().remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = false;
    if (notify)
      fireStopQueueAccessVacation (getEventList ().getTime ());
  }
  
  /** Stops an ongoing queue-access vacation as a result of an expiration event on the event list.
   * 
   * Invokes {@link #update}, resets the internal queue-access vacation flag, and notifies listeners of the change.
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses.
   * 
   * @param time The time of the queue-access vacation expiration.
   * 
   * @throws IllegalStateException If the internal administration indicates that there is no queue-access vacation ongoing.
   * 
   * @see #update
   * @see #fireStopQueueAccessVacation
   * 
   */
  protected final void stopQueueAccessVacationFromEventList (final double time)
  {
    if (! this.isQueueAccessVacation)
      throw new IllegalStateException ();
    update (time);
    this.isQueueAccessVacation = false;
    fireStopQueueAccessVacation (time);    
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
   * and checks the absence of the job in {@link #jobQueue} and {@link #jobsExecuting}
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
    if (this.jobQueue.contains (job) || this.jobsExecuting.contains (job))
      throw new IllegalStateException ();
    job.setQueue (null);
    fireDrop (time, job);
    rescheduleAfterDrop (job, time);
  }

  /** Removes a job from the internal queue(s) because it is to be dropped.
   * 
   * <p>To be implemented by concrete queue types.
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsExecuting} set (this is also checked).
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
   * and update {@link #jobsExecuting} accordingly.
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
  
  /**
   * {@inheritDoc}
   * 
   * The final implementation makes sanity checks (e.g., job present),
   * and invokes {@link #update}.
   * It then invokes the subclass {@link #removeJobFromQueueUponRevokation},
   * and returns from this method with return value <code>false</code>
   * if the subclass refuses the revocation (i.e., returns <code>false</code> from {@link #removeJobFromQueueUponRevokation}).
   * Otherwise, it checks the absence of the job in {@link #jobQueue} and {@link #jobsExecuting}
   * (throwing an {@link IllegalStateException} if not).
   * It sets the visited queue on the job (with {@link SimJob#setQueue}) to <code>null</code>,
   * and notifies the drop to listeners and actions
   * ({@link #fireRevocation}.
   * Finally, it invokes the queue-discipline specific {@link #rescheduleAfterRevokation}.
   * 
   */
  @Override
  public final boolean revoke
    (final J job,
    final double time,
    final boolean interruptService)
  {
    if (job == null || job.getQueue () != this || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    update (time);
    final boolean revoked = removeJobFromQueueUponRevokation (job, time, interruptService);
    if (! revoked)
      return false;
    if (this.jobQueue.contains (job) || this.jobsExecuting.contains (job))
      throw new IllegalStateException ();    
    job.setQueue (null);
    fireRevocation (time, job);
    rescheduleAfterRevokation (job, time);
    return true;
  }
    
  /** Removes a job from the internal queue(s) if it can be revoked.
   * 
   * <p>To be implemented by concrete queue types.
   * Implementation may refuse the revocation, in which case they must return <code>false</code>.
   *
   * <p>
   * Otherwise, implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsExecuting} set (this is also checked).
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
   * @param interruptService Whether to allow interruption of the job's
   *                           service if already started.
   *                         If false, revocation will only succeed if the
   *                           job has not received any service yet.
   * 
   * @return True if revocation succeeded, and the job was indeed removed from {@link #jobQueue}.
   * 
   * @see #revoke
   * @see #rescheduleAfterRevokation
   * 
   */
  protected abstract boolean removeJobFromQueueUponRevokation (J job, double time, boolean interruptService);
  
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
   * and update {@link #jobsExecuting} accordingly.
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
  
  /**
   * {@inheritDoc}
   * 
   * This final implementation invokes {@link #update} if the number of credits passed in the argument differs from the
   * current number of credits, to serve statistics on the number of server-access credits.
   * Then, it updates the internal administration of the number of server-access credits.
   * Subsequently, it invokes {@link #fireIfOutOfServerAccessCredits} or {@link #fireRegainedServerAccessCredits} if appropriate.
   * In the latter case, it also invokes {@link #rescheduleForNewServerAccessCredits}.
   * 
   * @see #getServerAccessCredits
   * 
   */
  @Override
  public final void setServerAccessCredits (final int credits)
  {
    if (credits < 0)
      throw new IllegalArgumentException ();
    final int oldCredits = this.serverAccessCredits;
    if (oldCredits != credits)
      update (getEventList ().getTime ()); 
    this.serverAccessCredits = credits;
    if (oldCredits > 0 && credits == 0)
      fireIfOutOfServerAccessCredits (this.lastUpdateTime);
    else if (oldCredits == 0 && credits > 0)
    {
      fireRegainedServerAccessCredits (this.lastUpdateTime);
      rescheduleForNewServerAccessCredits (this.lastUpdateTime);
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
   * 
   */
  protected final void takeServerAccessCredit (final boolean fireIfOut)
  {
    if (this.serverAccessCredits <= 0)
      throw new IllegalStateException ();
    // Integer.MAX_VALUE is treated as infinity.
    if (this.serverAccessCredits < Integer.MAX_VALUE)
    {
      update (getEventList ().getTime ());      
      this.serverAccessCredits--;
      if (fireIfOut && this.serverAccessCredits == 0)
        fireIfOutOfServerAccessCredits (this.lastUpdateTime);
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
   * and update {@link #jobsExecuting} accordingly.
   * 
   * @param time The current time (i.e., the time at which new server-access credits became available).
   * 
   * @see #setServerAccessCredits
   * @see #hasServerAcccessCredits
   * 
   */
  protected abstract void rescheduleForNewServerAccessCredits (double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The default {@link SimEvent} used internally for scheduling {@link SimJob} departures.
   * 
   * <p>The {@link DefaultDepartureEvent} (actually, its {@link SimEventAction}), once activated,
   * calls {@link AbstractSimQueue#departureFromEventList}.
   * The event puts the jobs passed in the constructor in its user object
   * {{@link SimEvent#getObject}).
   * 
   * <p>
   * Implementations are encouraged to avoid creation of {@link DefaultDepartureEvent}s
   * for each departure, but instead reuse instances.
   * 
   */
  protected final class DefaultDepartureEvent extends SimEvent<J>
  {
    public DefaultDepartureEvent
      (final double time,
      final J job)
    {
      super (time, job,
        new SimEventAction<J> ()
          {
            @Override
            public void action
              (final SimEvent<J> event)
            {
              AbstractSimQueue.this.departureFromEventList (event);
            }
          }
      );
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
   * 
   */
  protected final DefaultDepartureEvent scheduleDepartureEvent (final double time, final J job)
  {
    if (time < this.lastUpdateTime || job == null || job.getQueue () != this)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    // The following check is an error; jobs may depart without receiving any service at all!
    // if (! this.jobsExecuting.contains (job))
    //   throw new IllegalArgumentException ();
    final DefaultDepartureEvent event = new DefaultDepartureEvent (time, job);
    this.eventsScheduled.add (event);
    getEventList ().add (event);
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
  protected final void cancelDepartureEvent (final DefaultDepartureEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! getEventList ().contains (event))
      throw new IllegalArgumentException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (event.getObject ()))
      throw new IllegalArgumentException ();
    // The following check is an error; jobs may depart without receiving any service at all!
    // if (! this.jobsExecuting.contains (event.getObject ()))
    //   throw new IllegalArgumentException ();
    this.eventsScheduled.remove (event);
    getEventList ().remove (event);
  }
  
  /** Cancels a pending departure event for given job on the event list.
   * 
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
    final Set<DefaultDepartureEvent> set = getDepartureEvents (job);
    if (set == null || set.size () != 1)
      throw new IllegalArgumentException ();
    cancelDepartureEvent (set.iterator ().next ());
  }
  
  /** Gets all (should be at most one) departure event for given job.
   * 
   * The (final) implementation returns all {@link DefaultDepartureEvent}s in {@link #eventsScheduled}.
   * 
   * @param job The job.
   * 
   * @return A non-<code>null</code> {@link Set} holding all future departure events for the job.
   * 
   */
  protected final Set<DefaultDepartureEvent> getDepartureEvents (final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    // The following check is an error; jobs may depart without receiving any service at all!
    // if (! this.jobsExecuting.contains (job))
    //   throw new IllegalArgumentException ();
    final Set<DefaultDepartureEvent> set = new LinkedHashSet<> ();
    for (SimEvent<J> e : this.eventsScheduled)
      if (e == null)
        throw new IllegalStateException ();
      // JdJ20150913: I have no clue why the next statement does not work...
      // XXX TBD
      // else if (! (e instanceof DefaultDepartureEvent))
      //  continue;
      else if (! DefaultDepartureEvent.class.isAssignableFrom (e.getClass ()))
        /* continue */ ;
      else if (((DefaultDepartureEvent) e).getObject () != job)
        /* continue */ ;
      else
        set.add ((DefaultDepartureEvent) e);
    return set;
  }
  
  /** Deals with a departure event from the event list.
   * 
   * This method check the presence of the departure event in {@link #eventsScheduled},
   * throwing an exception if absent,
   * and removes the event from that collection.
   * Then it invokes {@link #update},
   * and takes care of administration of the internal data, i.e.,
   * clearing the job's queue {@link SimJob#setQueue},
   * invoking {@link #removeJobFromQueueUponDeparture}
   * (which must remove the job from the {@link #jobQueue}
   * and the {@link #jobsExecuting} lists).
   * It then invokes the discipline-specific {@link #rescheduleAfterDeparture}
   * followed by {@link #fireDeparture}.
   * 
   * @param event The departure event; must be non-<code>null</code> and present in {@link #eventsScheduled}.
   * 
   * @see #eventsScheduled
   * @see DefaultDepartureEvent
   * @see #scheduleDepartureEvent
   * 
   */
  protected final void departureFromEventList (final SimEvent<J> event)
  {
    if (event == null)
      throw new RuntimeException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalStateException ();
    this.eventsScheduled.remove (event);
    final double time = event.getTime ();
    final J job = event.getObject ();
    if (job.getQueue () != this)
      throw new IllegalStateException ();
    update (time);
    job.setQueue (null);
    removeJobFromQueueUponDeparture (job, time);
    if (this.jobQueue.contains (job)
      || this.jobsExecuting.contains (job))
      throw new IllegalStateException ();
    rescheduleAfterDeparture (job, time);    
    fireDeparture (job, event);
  }
  
  /** Removes a job from the internal queues upon departure.
   * 
   * <p>
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a queue-specific departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsExecuting} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i>reschedule events for <i>other</i> jobs on the event list,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterDeparture} for that.
   * 
   * <p>
   * Implementations must not fire departure events. This is done by the base class {@link AbstractSimQueue}.
   * 
   * 
   * @param departingJob The job that departs.
   * @param time The departure (current) time.
   * 
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
   * and update {@link #jobsExecuting} accordingly.
   * 
   * @param departedJob The departed job.
   * @param time The departure (current) time.
   * 
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  protected abstract void rescheduleAfterDeparture (J departedJob, double time);
  
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
    getEventList ().addListener (this);
  }

}
