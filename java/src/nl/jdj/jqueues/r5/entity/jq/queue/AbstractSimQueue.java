package nl.jdj.jqueues.r5.entity.jq.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue.AutoRevocationPolicy;
import nl.jdj.jqueues.r5.entity.jq.AbstractSimJQ;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Departure;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueue}.
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
public abstract class AbstractSimQueue<J extends SimJob, Q extends AbstractSimQueue>
  extends AbstractSimJQ<J, Q>
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
    registerOperation (SimQueueOperation.QueueAccessVacation.getInstance ());
    registerOperation (SimQueueOperation.Arrival.getInstance ());
    registerOperation (SimQueueOperation.Revocation.getInstance ());
    registerOperation (SimQueueOperation.ServerAccessCredits.getInstance ());
    registerPreEventHook (this::setInitStartArmed);
    registerPreNotificationHook (this::serverAccessCreditsPreNotificationHook);
    registerPreNotificationHook (this::startArmedPreNotificationHook);
    registerNotificationType (SimQueueSimpleEventType.QAV_START, this::fireStartQueueAccessVacation);
    registerNotificationType (SimQueueSimpleEventType.QAV_END, this::fireStopQueueAccessVacation);
    registerNotificationType (SimQueueSimpleEventType.OUT_OF_SAC, this::fireOutOfServerAccessCredits);
    registerNotificationType (SimQueueSimpleEventType.REGAINED_SAC, this::fireRegainedServerAccessCredits);
    registerNotificationType (SimQueueSimpleEventType.STA_FALSE, this::fireLostStartArmed);
    registerNotificationType (SimQueueSimpleEventType.STA_TRUE, this::fireRegainedStartArmed);
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A private instance of a {@link StdOutSimQueueListener}.
   * 
   * @see #registerStdOutSimQueueListener
   * @see #unregisterStdOutSimQueueListener
   * 
   */
  private final StdOutSimQueueListener<J, Q> stdOutSimQueueListener = new StdOutSimQueueListener<> ();
  
  /** Registers the (private) {@link StdOutSimQueueListener} as listener (convenience method).
   * 
   * @see #unregisterStdOutSimQueueListener
   * @see #registerSimEntityListener
   * 
   */
  public final void registerStdOutSimQueueListener ()
  {
    registerSimEntityListener (this.stdOutSimQueueListener);
  }
  
  /** Unregisters the (private) {@link StdOutSimQueueListener} as listener, if registered (convenience method).
   * 
   * @see #registerStdOutSimQueueListener
   * @see #unregisterSimEntityListener
   * 
   */
  public final void unregisterStdOutSimQueueListener ()
  {
    unregisterSimEntityListener (this.stdOutSimQueueListener);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AutoRevocationPolicy
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The auto-revocation policy; {@link AutoRevocationPolicy#NONE} is the mandatory default.
   * 
   */
  private AutoRevocationPolicy autoRevocationPolicy = AutoRevocationPolicy.NONE;
  
  @Override
  public final AutoRevocationPolicy getAutoRevocationPolicy ()
  {
    return this.autoRevocationPolicy;
  }
  
  @Override
  public final void setAutoRevocationPolicy (final AutoRevocationPolicy autoRevocationPolicy)
  {
    if (autoRevocationPolicy == null)
      throw new IllegalArgumentException ();
    this.autoRevocationPolicy = autoRevocationPolicy;
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

  @Override
  public final boolean isJob (final SimJob job)
  {
    return job != null && this.jobQueue.contains ((J) job); // Note: futile cast, but stops compiler from complaining.
  }

  /** Jobs currently in the service area.
   *
   * <p>
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

  @Override
  public final boolean isJobInServiceArea (final SimJob job)
  {
    return job != null && this.jobsInServiceArea.contains ((J) job); // Note: futile cast, but stops compiler from complaining.
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

  /** Overridden to make (default) implementation final.
   * 
   */
  @Override
  public final boolean isJobInWaitingArea (final SimJob job)
  {
    return SimQueue.super.isJobInWaitingArea (job);
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

  /** Returns the last job in {@link #getJobs} that <i>is not</i> in {@link #getJobsInServiceArea}.
   * 
   * @return The last job in {@link #getJobs} that is not in {@link #getJobsInServiceArea},
   *         <code>null</code> if there are no waiting jobs.
   * 
   */
  protected final J getLastJobInWaitingArea ()
  {
    if (getNumberOfJobsInWaitingArea () == 0)
      return null;
    for (int i = this.jobQueue.size () - 1; i >= 0; i--)
      if (! this.jobsInServiceArea.contains (this.jobQueue.get (i)))
        return this.jobQueue.get (i);
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

  /** Returns the last job in {@link #getJobs} that <i>is</i> in {@link #getJobsInServiceArea}.
   * 
   * @return The last job in {@link #getJobs} that is in {@link #getJobsInServiceArea},
             <code>null</code> if there are no jobs in the service area.
   * 
   */
  protected final J getLastJobInServiceArea ()
  {
    if (getNumberOfJobsInServiceArea () == 0)
      return null;
    for (int i = this.jobQueue.size () - 1; i >= 0; i--)
      if (this.jobsInServiceArea.contains (this.jobQueue.get (i)))
        return this.jobQueue.get (i);
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
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Invokes super method, removes all jobs without notifications,
   *  ends all vacations, restores the number of server-access credits (to infinity)
   *  and resets all caches.
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
    this.previousStartArmedSet = false;
    this.isQueueAccessVacation = false;
    this.serverAccessCredits = Integer.MAX_VALUE;
    this.previousSacAvailability = true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed (CACHING)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean previousStartArmedSet = false;
  
  private boolean previousStartArmed = false;
  
  /** Sets the initial state (after construction or after a reset) of {@code StartArmed}.
   * 
   * <p>
   * This method is called as a pre-event hook, and not meant to be called from user code (in sub-classes).
   * 
   * @param newTime The new time.
   * 
   */
  private void setInitStartArmed (final double newTime)
  {
    if (! this.previousStartArmedSet)
    {
      this.previousStartArmedSet = true;
      this.previousStartArmed = isStartArmed ();
    }
  }

  /** The registered pre-notification hook for {@code startArmed} state-change notifications.
   * 
   * @param pendingNotifications The pending notifications.
   * 
   */
  protected final void startArmedPreNotificationHook
  (final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> pendingNotifications)
  {
    if (pendingNotifications == null)
      throw new IllegalArgumentException ();
    if (! this.previousStartArmedSet)
      // Our initial StartArmed has not been set yet, even though we registered a pre-event hook.
      // We may safely assume that we are still in our reset procedures,
      // and that our sub-class has presented us with a notification while resetting.
      // Our only option now is the set the initial StartArmed here...
      // Note, by the way, that the (time) argument is ignored.
      setInitStartArmed (getLastUpdateTime ());
    final boolean startArmed = isStartArmed ();
    final Iterator<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> i_pendingNotifications
      = pendingNotifications.iterator ();
    boolean hasStaNotification = false;
    while (i_pendingNotifications.hasNext ())
    {
      final SimEntitySimpleEventType.Member notificationType = i_pendingNotifications.next ().keySet ().iterator ().next ();
      if (notificationType == SimQueueSimpleEventType.STA_FALSE || notificationType == SimQueueSimpleEventType.STA_TRUE)
      {
        if ((notificationType == SimQueueSimpleEventType.STA_TRUE) != startArmed)
          throw new IllegalArgumentException ();
        else
          i_pendingNotifications.remove ();
        hasStaNotification = true;
      }
    }
    if (hasStaNotification || (startArmed != this.previousStartArmed))
    {
      final double time = getLastUpdateTime ();
      if (startArmed)
        pendingNotifications.add (Collections.singletonMap
          (SimQueueSimpleEventType.STA_TRUE, new SimQueueEvent.StartArmed<> (this, time, true)));
      else
        pendingNotifications.add (Collections.singletonMap
          (SimQueueSimpleEventType.STA_FALSE, new SimQueueEvent.StartArmed<> (this, time, false)));
    }
    this.previousStartArmed = startArmed;
  }
  
  /** Triggers a potential, autonomous (top-level) change in the {@link #isStartArmed} status (for sub-class use).
   * 
   * <p>
   * In most cases, a change in the {@link #isStartArmed} state will be the result of operations in this {@link SimQueue},
   * and will be noted and reported automatically
   * through the use of the pre-notification hook {@link #startArmedPreNotificationHook}.
   * 
   * <p>
   * It is, however, perfectly legal that the {@link #isStartArmed} state of a {@link SimQueue}
   * changes independently from external or other monitored internal events.
   * Concrete subclasses must therefore invoke this method upon suspected autonomous changes in the {@link #isStartArmed} state,
   * in order to make sure that they are properly notified to listeners.
   * 
   * <p>
   * The implementation checks if the current {@link #isStartArmed} state is different from the cached value
   * as maintained by {@link #startArmedPreNotificationHook}.
   * If so, and if it is a top-level event, as assessed with {@link #clearAndUnlockPendingNotificationsIfLocked},
   * it invokes {@link #update}, adds and fires a proper notification, i.c.,
   * {@link SimQueueSimpleEventType#STA_TRUE} or
   * {@link SimQueueSimpleEventType#STA_FALSE},
   * and fires a notification through {@link #fireAndLockPendingNotifications}.
   * 
   * <p>
   * In all other cases, this method does nothing,
   * relying on the pre-notification hook {@link #startArmedPreNotificationHook}.
   * 
   * @param time The current time.
   * 
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #isStartArmed
   * @see #update
   * @see #addPendingNotification
   * @see SimQueueSimpleEventType#STA_TRUE
   * @see SimQueueSimpleEventType#STA_FALSE
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void triggerPotentialNewStartArmed (final double time)
  {
    final boolean startArmed = isStartArmed ();
    if ((! this.previousStartArmedSet) || startArmed != this.previousStartArmed)
    {
      final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
      if (isTopLevel)
      {
        update (time);
        this.previousStartArmedSet = true;
        this.previousStartArmed = startArmed;
        addPendingNotification (startArmed ? SimQueueSimpleEventType.STA_TRUE : SimQueueSimpleEventType.STA_FALSE, null);
        fireAndLockPendingNotifications ();
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL [EXTERNAL TOP-LEVEL OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Handles an arrival at this queue.
   * 
   * <p>
   * This (final) implementation is as described below.
   * 
   * <p>
   * It first makes sanity checks (e.g., job not already present).
   * 
   * <p>
   * It then invokes {@link #update}
   * and {@link #clearAndUnlockPendingNotificationsIfLocked},
   * insisting to be a top-level event (at the expense of an {@link IllegalStateException}).
   * 
   * 
   * <p>
   * Subsequently, if the queue is on queue-access vacation ({@link #isQueueAccessVacation}),
   * it adds a {@link SimQueueSimpleEventType#DROP} pending notification; bypassing {@link #drop}
   * since subclasses have no knowledge of the job yet (and they do not need to have),
   * and invokes {@link #queueAccessVacationDropSubClass}.
   * The latter method allows for (the rare case of) specific sub-class handling. 
   * 
   * <p>
   * Otherwise, it then invokes the subclass-specific {@link #insertJobInQueueUponArrival},
   * and checks the presence of the job in {@link #jobQueue}.
   * 
   * <p>
   * If <i>not</i> present, sets the jobs queue to <code>null</code>
   * and adds a {@link SimQueueSimpleEventType#DROP} pending notification,
   * again bypassing {@link #drop} because the job is not present in the system at this point.
   * Also, in this case, there is no call to {@link #rescheduleAfterArrival}, since
   * {@link #insertJobInQueueUponArrival} has declined immediately the queue visit.
   * 
   * <p>
   * If however the job is still present, it sets this queue to be the visited queue on the job (with {@link SimJob#setQueue}),
   * and invokes the subclass-specific {@link #rescheduleAfterArrival}.
   * 
   * <p>
   * Finally, it notifies listeners of the pending notifications.
   * 
   * @see #update
   * @see #isQueueAccessVacation
   * @see #queueAccessVacationDropSubClass
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #insertJobInQueueUponArrival
   * @see SimJob#setQueue
   * @see #rescheduleAfterArrival
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * @see SimQueueSimpleEventType#ARRIVAL
   * @see SimQueueSimpleEventType#DROP
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
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (! isTopLevel)
      throw new IllegalStateException ();
    addPendingNotification (SimQueueSimpleEventType.ARRIVAL, new SimJQEvent.Arrival<> (job, this, time));
    if (this.isQueueAccessVacation)
    {
      addPendingNotification (SimQueueSimpleEventType.DROP, new SimJQEvent.Drop<> (job, this, time));
      queueAccessVacationDropSubClass (time, job);
    }
    else
    {
      insertJobInQueueUponArrival (job, time);
      //
      // XXX At some point, we used this approach (checking for presence in #jobQueue)
      // in order to obtain atomicity for "drop-on-arrival" cases.
      // With the present framework, it might be obsolete; subclasses can simply invoke #drop
      // from within rescheduleAfterArrival without losing atomicity.
      //
      if (! this.jobQueue.contains (job))
      {
        if (this.jobsInServiceArea.contains (job))
          throw new IllegalStateException ();
        job.setQueue (null);  // Just in case it was erroneously set by our subclass...
        addPendingNotification (SimQueueSimpleEventType.DROP, new SimJQEvent.Drop<> (job, this, time));
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
    fireAndLockPendingNotifications ();
  }

  /** Inserts a job that just arrived (at given time) into the internal queue(s).
   * 
   * <p>
   * To be implemented by concrete queue types.
   * 
   * <p>
   * Implementations <i>must</i> (at least) add the job to {@link #jobQueue}. If not, the job is immediately marked for dropping,
   * and {@link #rescheduleAfterArrival} is not invoked!
   * 
   * <p>
   * Implementations must ignore any queue-access vacation as this is taken care of already by the base class.
   * 
   * <p>
   * Implementations must <i>not</i> reschedule on the event list, or make changes to {@link #jobsInServiceArea},
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
   * @param job  The job that arrived.
   * @param time The current time (i.e., arrival time of the job).
   * 
   * @see #arrive
   * @see #rescheduleAfterArrival
   * 
   */
  protected abstract void insertJobInQueueUponArrival (J job, double time);
  
  /** Reschedules after a job arrival.
   * 
   * <p>
   * The job has already been put into the internal data structures, and notifications are already pending for the
   * arrival. Also, it is guaranteed that there are no queue-access vacations, as these are being dealt with by the
   * base class, and {@link #update} has already been called.
   * 
   * <p>
   * Typically, but not necessarily, implementations must check whether or not to start the job immediately,
   * and invoke {@link #start} if so.
   * 
   * <p>
   * Implementations must <i>not</i> insert a {@link SimQueueSimpleEventType#ARRIVAL} notification,
   * as this is already done by the base class {@link AbstractSimQueue}.
   * They must, however, add appropriate notifications for other internal state-changing events.
   * 
   * <p>
   * Upon return, the job may have left {@link #jobQueue} already (in which case it <i>must</i> not be present in
   * {@link #jobsInServiceArea} either), but the caller then assumes that all appropriate notifications
   * are added (to the pending notifications) in this method.
   * 
   * @param job  The job that arrived (and is present in {@link #jobQueue}).
   * @param time The current time (i.e., the arrival time of the job).
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
   * @param time The arrival time of the job, which must be in the future,
   *             relative to both the last update time of this {@link SimQueue} as to the time on the event list.
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
    SimJQEventScheduler.scheduleJobArrival (job, this, time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION [EXTERNAL TOP-LEVEL OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean isQueueAccessVacation = false;
  
  /** Returns whether or not this queue is on queue-access vacation (from an internal flag).
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by this base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses, but see {@link #queueAccessVacationDropSubClass}.
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
   * The a-priori call to {@link #update} and the a-posteriori pending notification
   * of either {@link SimQueueSimpleEventType#QAV_START} or {@link SimQueueSimpleEventType#QAV_END}
   * are only effectuated if the vacation status queue actually changed.
   * 
   * <p>
   * This method insists to be a top-level event (at the expense of an {@link IllegalStateException}).
   * 
   * <p>
   * Note that queue-access vacations are entirely dealt with by the base class {@link AbstractSimQueue}; there is
   * no interaction (needed) with concrete subclasses, but see {@link #queueAccessVacationDropSubClass}.
   * 
   * @see SimQueueSimpleEventType#QUEUE_ACCESS_VACATION
   * @see SimQueueSimpleEventType#QAV_START
   * @see SimQueueSimpleEventType#QAV_END
   * @see #queueAccessVacationDropSubClass
   * @see #arrive
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * 
   */
  @Override
  public final void setQueueAccessVacation (final double time, final boolean start)
  {
    if (this.isQueueAccessVacation != start)
    {
      update (time);
      final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
      if (! isTopLevel)
        throw new IllegalStateException ();
      this.isQueueAccessVacation = start;
      if (this.isQueueAccessVacation)
        addPendingNotification (SimQueueSimpleEventType.QAV_START, new SimQueueEvent.QueueAccessVacation (this, time, true));
      else
        addPendingNotification (SimQueueSimpleEventType.QAV_END, new SimQueueEvent.QueueAccessVacation (this, time, false));
      fireAndLockPendingNotifications ();
    }
  }

  /** Specific sub-class handling upon job-drop events due to queue-access vacations.
   * 
   * <p>
   * Normally, queue-access vacations, their effects on arriving jobs, as well as the required notifications
   * are entirely dealt with by this abstract class.
   * However, this current method is invoked from within {@link #arrive} when a job is dropped due to a queue-access vacation,
   * for instance, because the concrete sub-class needs to issue dedicated notifications
   * upon job-drops.
   * 
   * <p>
   * No changes to the state of this {@link SimQueue} should result from this method,
   * and no {@link SimEvent}s should be scheduled or canceled.
   * 
   * <p>
   * The default implementation does nothing.
   * 
   * @param time The time the job was dropped, i.e., the current time.
   * @param job  The dropped job.
   * 
   * @see #arrive
   * 
   */
  protected void queueAccessVacationDropSubClass (final double time, final J job)
  {
    /* EMPTY */
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP [INTERNAL NON-TOP-LEVEL OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Drops a job from this queue, on the queue's initiative.
   * 
   * <p>
   * This (final) implementation is as described below.
   * 
   * <p>
   * It first makes makes sanity checks (e.g., job present),
   * and assures that {@link #update} and {@link #clearAndUnlockPendingNotificationsIfLocked}
   * have been invoked by the caller (since this is an <i>internal</i> operation).
   * 
   * <p>
   * It invokes the subclass-specific {@link #removeJobFromQueueUponDrop},
   * and checks the absence of the job in {@link #jobQueue} and {@link #jobsInServiceArea}
   * (throwing an {@link IllegalStateException} if not).
   * 
   * <p>
   * It sets the visited queue on the job (with {@link SimJob#setQueue}) to <code>null</code>,
   * and adds a {@link SimQueueSimpleEventType#DROP} pending notification.
   * 
   * <p>
   * It then invokes the subclass-specific {@link #rescheduleAfterDrop}.
   * 
   * <p>
   * This method does <i>not</i> notify listeners through {@link #fireAndLockPendingNotifications}.
   * 
   * @param job  The job to be dropped.
   * @param time The current time, i.e., the drop time of the job.
   * 
   * @throws IllegalArgumentException If the job is <code>null</code> or not found.
   * @throws IllegalStateException    If the internal administration of this queue has become inconsistent. 
   * 
   * @see SimQueueSimpleEventType#DROP
   * @see #removeJobFromQueueUponDrop
   * @see #rescheduleAfterDrop
   * @see #update
   * @see #getLastUpdateTime
   * @see SimJob#setQueue
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void drop (final J job, final double time)
  {
    if (job == null || job.getQueue () != this || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (time != getLastUpdateTime ())
      throw new IllegalStateException ();
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (isTopLevel)
      throw new IllegalStateException ();
    removeJobFromQueueUponDrop (job, time);
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    job.setQueue (null);
    addPendingNotification (SimQueueSimpleEventType.DROP, new SimJQEvent.Drop<> (job, this, time));
    rescheduleAfterDrop (job, time);
  }

  /** Removes a job from the internal queue(s) because it is to be dropped.
   * 
   * <p>
   * To be implemented by concrete queue types.
   * 
   * <p>
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsInServiceArea} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i> reschedule events for <i>other</i> jobs on the event list,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterDrop} for that.
   * 
   * @param job  The job that is to be dropped.
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
   * Implementations must <i>not</i> insert a {@link SimQueueSimpleEventType#DROP} notification,
   * as this is already done by the base class {@link AbstractSimQueue}.
   * They must, however, add appropriate notifications for other internal state-changing events.
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
  // REVOKE [EXTERNAL TOP-LEVEL OPERATION]
  //
  // AUTO-REVOKE [INTERNAL NON-TOP-LEVEL OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Revokes a job from this queue.
   * 
   * <p>
   * This (final) implementation is as described below.
   * 
   * <p>
   * It first makes makes sanity checks (e.g., job is non-{@code null}).
   * 
   * <p>
   * It then checks whether the revocation is to be refused
   * which is the case when the job is not present, or has already been started and {@code interruptService == false}.
   * If revocation is to be refused, this method simple returns {@code false}.
   * 
   * <p>
   * Otherwise,
   * it invokes {@link #update}
   * and {@link #clearAndUnlockPendingNotificationsIfLocked},
   * insisting to be a top-level event (at the expense of an {@link IllegalStateException}).
   * 
   * <p>
   * It invokes the subclass-specific {@link #removeJobFromQueueUponRevokation},
   * and checks the absence of the job in {@link #jobQueue} and {@link #jobsInServiceArea}
   * (throwing an {@link IllegalStateException} if not).
   * 
   * <p>
   * It sets the visited queue on the job (with {@link SimJob#setQueue}) to <code>null</code>,
   * and adds a {@link SimQueueSimpleEventType#REVOCATION} pending notification.
   * 
   * <p>
   * It then invokes the subclass-specific {@link #rescheduleAfterRevokation}.
   * 
   * <p>
   * Finally, it notifies listeners of the pending notifications.
   * 
   * @see SimQueueSimpleEventType#REVOCATION
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * @see #autoRevoke
   * @see #update
   * @see #getLastUpdateTime
   * @see SimJob#setQueue
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
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
    if ((! interruptService) && getJobsInServiceArea ().contains (job))
      return false;
    update (time);
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (! isTopLevel)
      throw new IllegalStateException ();
    removeJobFromQueueUponRevokation (job, time, false);
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();    
    job.setQueue (null);
    addPendingNotification
      (SimQueueSimpleEventType.REVOCATION, new SimJQEvent.Revocation<> (job, this, time, interruptService));
    rescheduleAfterRevokation (job, time, false);
    fireAndLockPendingNotifications ();
    return true;
  }

  /** Calls super method on {@link SimQueue} (and makes it final).
   * 
   * @param time The time at which the request is issued, i.c., the current time.
   * @param job  The job to be revoked from the queue.
   * 
   * @see SimQueue#revoke(double, nl.jdj.jqueues.r5.entity.jq.job.SimJob, boolean)
   * 
   */
  @Override
  public final void revoke (final double time, final J job)
  {
    SimQueue.super.revoke (time, job);
  }
  
  /** Auto-revokes a job at this queue.
   * 
   * <p>
   * The final implementation of this method is identical to that of {@link #revoke},
   * with the exceptions that
   * <ul>
   * <li>
   * It first makes makes sanity checks (e.g., job present),
   * and assures that {@link #update} and {@link #clearAndUnlockPendingNotificationsIfLocked}
   * have been invoked by the caller (since this is an <i>internal</i> operation),
   * <li>
   * the job has to be present a priori (at the expense of an {@link IllegalStateException}),
   * <li>
   * a {@link SimQueueSimpleEventType#AUTO_REVOCATION} is added as pending notification
   * (instead of a {@link SimQueueSimpleEventType#REVOCATION}),
   * <li>
   * this method does <i>not</i> notify listeners through {@link #fireAndLockPendingNotifications}.
   * </ul>
   * 
   * @param job  The job to be revoked.
   * @param time The current time, i.e., the auto-revocation time of the job.
   * 
   * @see SimQueueSimpleEventType#AUTO_REVOCATION
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * @see #revoke
   * @see #update
   * @see #getLastUpdateTime
   * @see SimJob#setQueue
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void autoRevoke (final double time, final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job) && job.getQueue () == this)
      throw new IllegalStateException ();
    if (time != getLastUpdateTime ())
      throw new IllegalStateException ();
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (isTopLevel)
      throw new IllegalStateException ();
    removeJobFromQueueUponRevokation (job, time, true);
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();    
    job.setQueue (null);
    addPendingNotification
      (SimQueueSimpleEventType.AUTO_REVOCATION, new SimJQEvent.AutoRevocation<> (job, this, time));
    rescheduleAfterRevokation (job, time, true);
  }

  /** Removes a job from the internal queue(s) since it is revoked.
   * 
   * <p>To be implemented by concrete queue types.
   *
   * <p>
   * This method is shared between {@link #revoke} and {@link #autoRevoke}.
   * 
   * <p>
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsInServiceArea} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i> reschedule events for <i>other</i> jobs on the event list,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterRevokation} for that.
   * 
   * @param job  The job that is to be revoked.
   * @param time The current time (i.e., revocation time of the job).
   * @param auto Whether or not this applies to an auto-revocation.
   * 
   * @see #revoke
   * @see #autoRevoke
   * @see #rescheduleAfterRevokation
   * @see AutoRevocationPolicy
   * 
   */
  protected abstract void removeJobFromQueueUponRevokation (J job, double time, boolean auto);
  
  /** Reschedules if needed after a job has been revoked from this queue.
   * 
   * <p>
   * This method is shared between {@link #revoke} and {@link #autoRevoke}.
   * 
   * <p>
   * Implementations can rely on the fact that the job is no longer present in the internal data structures,
   * that it has no pending events on the event list,
   * and that this method is invoked immediately after a successful {@link #removeJobFromQueueUponRevokation}.
   * 
   * <p>
   * Implementations must <i>not</i> insert a {@link SimQueueSimpleEventType#REVOCATION}
   * or {@link SimQueueSimpleEventType#AUTO_REVOCATION} notification,
   * as this is already done by the base class {@link AbstractSimQueue}.
   * They must, however, add appropriate notifications for other internal state-changing events.
   * 
   * @param job  The jobs that was successfully revoked.
   * @param time The current time (i.e., revocation time of the job).
   * @param auto Whether or not this applies to an auto-revocation.
   * 
   * @see #revoke
   * @see #autoRevoke
   * @see #removeJobFromQueueUponRevokation
   * @see AutoRevocationPolicy
   * 
   */
  protected abstract void rescheduleAfterRevokation (J job, double time, boolean auto);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS [EXTERNAL TOP-LEVEL OPERATION]
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
   * <p>
   * This final implementation invokes {@link #update} only if the number of credits passed in the argument differs from the
   * current number of credits, to serve statistics on the number of server-access credits.
   * The method does nothing if the number has not changed.
   * 
   * <p>
   * Otherwise, it updates the internal administration of the number of server-access credits.
   * It then checks whether we either lost server-access credits, or regained them,
   * and invokes {@link #rescheduleForNewServerAccessCredits} in the latter case,
   * and sets up the proper notification of listeners in both cases,
   * insisting to be a top-level event (at the expense of an {@link IllegalStateException}).
   * 
   * <p>
   * In any case (including a mere change of value of the server-access credits),
   * it then invokes {@link #setServerAccessCreditsSubClass} to notify interested subclasses
   * of the new value for the server-access-credits (but note that this value may have changed already due to rescheduling).
   * 
   * <p>
   * Note that the insertion of {@link SimQueueSimpleEventType#OUT_OF_SAC}
   * or {@link SimQueueSimpleEventType#REGAINED_SAC} if needed is picked up by a registered pre-notification hook;
   * see {@link #registerPreNotificationHook}.
   * 
   * @see #getServerAccessCredits
   * @see #rescheduleForNewServerAccessCredits
   * @see SimQueueSimpleEventType#OUT_OF_SAC
   * @see SimQueueSimpleEventType#REGAINED_SAC
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
      final boolean lostCredits = (oldCredits > 0 && this.serverAccessCredits == 0);
      final boolean regainedCredits = (oldCredits == 0 && this.serverAccessCredits > 0);
      final boolean needsNotification = lostCredits || regainedCredits;
      if (needsNotification)
      {
        final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
        if (! isTopLevel)
          throw new IllegalStateException ();
        if (regainedCredits)
          rescheduleForNewServerAccessCredits (time);
        setServerAccessCreditsSubClass ();
        fireAndLockPendingNotifications ();
      }
      else
        setServerAccessCreditsSubClass ();        
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
  
  /** Takes a single server-access credit, which must be  available (for subclass use).
   * 
   * Throws an {@link IllegalStateException} if there are no server-access credits, in other words,
   * caller must check this first.
   * 
   * @throws IllegalStateException If there are no server-access credits left upon entry.
   * 
   * @see #hasServerAcccessCredits
   * @see SimQueueSimpleEventType#OUT_OF_SAC
   * 
   */
  protected final void takeServerAccessCredit ()
  {
    if (this.serverAccessCredits <= 0)
      throw new IllegalStateException ("queue=" + this
                                     + ", t=" + getLastUpdateTime ()
                                     + ", sac=" + this.serverAccessCredits + ".");
    // Integer.MAX_VALUE is treated as infinity.
    if (this.serverAccessCredits < Integer.MAX_VALUE)
      this.serverAccessCredits--;
  }
  
  /** Reschedules if needed due to the (new) availability of server-access credits.
   * 
   * <p>
   * Implementations can rely on the availability of server-access credits.
   * 
   * <p>
   * Implementations must not insert server-access-credits notifications.
   * This is done by the base class {@link AbstractSimQueue}.
   * They must, however, add appropriate notifications for other internal state-changing events.
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
   * and should <i>not</i> reschedule
   * (but instead implement such required rescheduling in {@link #rescheduleForNewServerAccessCredits} to that purpose).
   * 
   * <p>
   * The default implementation does nothing.
   * 
   * @see #getServerAccessCredits
   * @see #setServerAccessCredits
   * @see #rescheduleForNewServerAccessCredits
   * 
   */
  protected void setServerAccessCreditsSubClass ()
  {
  }
  
  /** The previous reported SAC availability.
   * 
   * <p>
   * For internal use by the {@link #serverAccessCreditsPreNotificationHook}.
   * 
   * <p>
   * Set to {@code true} upon construction and upon reset, since by contract,
   * the number of server-access credits is infinite then.
   * 
   */
  private boolean previousSacAvailability = true; // Every SimQueue must have infinite sacs upon construction and after reset.
  
  /** The pre-notification hook for server-access credits availability (using caching).
   * 
   */
  private void serverAccessCreditsPreNotificationHook
  (final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> pendingNotifications)
  {
    if (pendingNotifications == null)
      throw new IllegalArgumentException ();
    for (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> entry : pendingNotifications)
    {
      final SimEntitySimpleEventType.Member notificationType = entry.keySet ().iterator ().next ();
      if (notificationType == SimQueueSimpleEventType.OUT_OF_SAC
      ||  notificationType == SimQueueSimpleEventType.REGAINED_SAC)
        throw new IllegalArgumentException ();
    }
    final boolean sacAvailability = hasServerAcccessCredits ();
    if (sacAvailability != this.previousSacAvailability)
    {
      final double time = getLastUpdateTime ();
      final int sac = getServerAccessCredits ();
      if (sacAvailability)
        pendingNotifications.add (Collections.singletonMap
          (SimQueueSimpleEventType.REGAINED_SAC, new SimQueueEvent.ServerAccessCredits<> (this, time, sac)));
      else
        pendingNotifications.add (Collections.singletonMap
          (SimQueueSimpleEventType.OUT_OF_SAC, new SimQueueEvent.ServerAccessCredits<> (this, time, sac)));
    }
    this.previousSacAvailability = sacAvailability;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START [INTERNAL NON-TOP-LEVEL OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Starts a job at this queue, unconditionally (for subclass use).
   * 
   * <p>
   * This (final) implementation is as described below.
   * 
   * <p>
   * It first makes sanity checks (e.g., job present and not already started),
   * and assures that {@link #update} and {@link #clearAndUnlockPendingNotificationsIfLocked}
   * have been invoked by the caller (since this is an <i>internal</i> operation).
   * 
   * <p>
   * It then takes a server-access credit through {@link #takeServerAccessCredit},
   * throwing a {@link IllegalStateException} if not credits are available.
   * 
   * <p>
   * If the auto-revocation policy is {@link AutoRevocationPolicy#UPON_START},
   * it adds a {@link SimQueueSimpleEventType#START} pending notification,
   * yet immediately revokes the job through {@link #autoRevoke}.
   * Note that in this case, {@link #insertJobInQueueUponStart}
   * and {@link #rescheduleAfterStart} are not used at all! 
   * 
   * <p>
   * Otherwise, it invokes the subclass-specific {@link #insertJobInQueueUponStart},
   * checks the (mandatory) presence of the job in {@link #jobsInServiceArea},
   * adds a {@link SimQueueSimpleEventType#START} pending notification
   * and invokes the subclass-specific {@link #rescheduleAfterStart}.
   * 
   * <p>
   * This method does <i>not</i> notify listeners through {@link #fireAndLockPendingNotifications}.
   * 
   * @param job  The job that is to be started.
   * @param time The current time (i.e., start time of the job).
   * 
   * @see #update
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #insertJobInQueueUponStart
   * @see #rescheduleAfterStart
   * @see #getAutoRevocationPolicy
   * @see AutoRevocationPolicy#UPON_START
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * @see SimQueueSimpleEventType#START
   * 
   */
  protected final void start (final double time, final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (job.getQueue () != this)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new RuntimeException ();
    if (this.jobsInServiceArea.contains (job))
      throw new RuntimeException ();
    if (time != getLastUpdateTime ())
      throw new IllegalStateException ();
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (isTopLevel)
      throw new IllegalStateException ();
    takeServerAccessCredit ();
    if (this.autoRevocationPolicy == AutoRevocationPolicy.UPON_START)
    {
      addPendingNotification (SimQueueSimpleEventType.START, new SimJQEvent.Start<> (job, this, time));
      autoRevoke (time, job);
    }
    else
    {
      insertJobInQueueUponStart (job, time);
      if (! this.jobQueue.contains (job))
        throw new RuntimeException ();
      if (! this.jobsInServiceArea.contains (job))
        throw new RuntimeException ();
      addPendingNotification (SimQueueSimpleEventType.START, new SimJQEvent.Start<> (job, this, time));
      rescheduleAfterStart (job, time);
    }
  }
  
  /** Updates the internal data structures upon the start of a job.
   * 
   * <p>
   * To be implemented by concrete queue types.
   * 
   * <p>
   * Implementations must (at least) add the job to {@link #jobsInServiceArea}, even if the job is to depart or be dropped
   * immediately.
   * 
   * <p>
   * More generally, implementations must <i>not</i> reschedule,
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterStart} for that.
   * 
   * <p>
   * Implementations should not care about server-access credits or auto-revocation;
   * this is taken care of by {@link #start}.
   * 
   * @param job  The job that starts.
   * @param time The current time (i.e., start time of the job).
   * 
   * @see #start
   * @see #rescheduleAfterStart
   * 
   */
  protected abstract void insertJobInQueueUponStart (J job, double time);
  
  /** Reschedules after a job start.
   * 
   * <p>
   * The job has already been put into the internal data structures, and notifications are already pending for the
   * start. Also, {@link #update} has already been called.
   * 
   * <p>
   * Upon return, the job may have left {@link #jobQueue} already (in which case it <i>must</i> not be present in
   * {@link #jobsInServiceArea} either), but the caller then assumes that all appropriate notifications
   * are added (to the pending notifications) in this method.
   * 
   * <p>
   * Implementations must <i>not</i> insert a {@link SimQueueSimpleEventType#START} notification,
   * as this is already done by the base class {@link AbstractSimQueue}.
   * They must, however, add appropriate notifications for other internal state-changing events.
   * 
   * <p>
   * Implementations should not care about server-access credits or auto-revocation;
   * this is taken care of by {@link #start}.
   * 
   * @param job  The job that started (and is present in {@link #jobQueue} and {@link #jobsInServiceArea}).
   * @param time The current time (i.e., the start time of the job).
   * 
   * @see #start
   * @see #insertJobInQueueUponStart
   * 
   */
  protected abstract void rescheduleAfterStart (J job, double time);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the service time for a job at this queue.
   * 
   * <p>
   * Intended to be the core method for obtaining the service time of a job during a visit.
   * 
   * <p>
   * Implementations must always return a non-negative value.
   * 
   * <p>
   * Implementations must always return the same value prior to a <i>single</i> visit,
   * and during the visit itself.
   * This method can only change its return value immediately after a job visit.
   * 
   * <p>
   * Note that this base class does not use this method; it is for sub-class use.
   * Also note that certain queueing disciplines may not support the notion
   * of a job's service time, and thus completely ignore this method.
   * At the discretion of the implementation,
   * an {@link UnsupportedOperationException} may even the thrown then.
   * 
   * @param job The job, non-<code>null</code>.
   * 
   * @return The required service time, non-negative.
   * 
   * @throws UnsupportedOperationException If the notion of the service time of a job is not supported by this queue type
   *                                       (e.g., because it does not allow jobs to start).
   * 
   */
  protected double getServiceTimeForJob (final J job)
  {
    return job.getServiceTime (this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE FROM EVENT LIST [INTERNAL TOP-LEVEL OPERATION]
  //
  // DEPARTURE [INTERNAL NON-TOP-LEVEL OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Deals with a departure event from the event list (for internal use only).
   * 
   * <p>
   * Do not invoke this method directly from sub-classes; it is meant to be invoked from a scheduled {@link Departure}.
   * 
   * <p>
   * This method (final) check the presence of the departure event in {@link #eventsScheduled},
   * throwing an exception if absent,
   * and removes the event from that collection.
   * 
   * <p>
   * It then invokes {@link #update} with the event time,
   * and {@link #clearAndUnlockPendingNotificationsIfLocked},
   * insisting to be a top-level event (at the expense of an {@link IllegalStateException}).
   * 
   * <p>
   * Finally, it grabs the time and job parameters from the event argument,
   * invokes {@link #depart},
   * and notifies listeners through {@link #fireAndLockPendingNotifications}.
   * 
   * @param event The departure event; must be non-<code>null</code> and present in {@link #eventsScheduled}.
   * 
   * @see #eventsScheduled
   * @see Departure
   * @see #scheduleDepartureEvent
   * @see #depart
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void departureFromEventList (final SimJQEvent.Departure<J, Q> event)
  {
    if (event == null)
      throw new RuntimeException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalStateException ();
    this.eventsScheduled.remove (event);
    final double time = event.getTime ();
    update (time);
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (! isTopLevel)
      throw new IllegalStateException ();
    final J job = event.getJob ();
    depart (time, job);
    fireAndLockPendingNotifications ();
  }
  
  /** Departure (unconditionally) of a job (for subclass and departure-event use).
   * 
   * <p>
   * This (final) implementation is as described below.
   * 
   * <p>
   * It first makes makes sanity checks (e.g., job present),
   * and assures that {@link #update} and {@link #clearAndUnlockPendingNotificationsIfLocked}
   * have been invoked by the caller (since this is an <i>internal</i> operation).
   * 
   * <p>
   * It sets the visited queue on the job (with {@link SimJob#setQueue}) to <code>null</code>.
   * 
   * <p>
   * It invokes the subclass-specific {@link #removeJobFromQueueUponDeparture},
   * and checks the absence of the job in {@link #jobQueue} and {@link #jobsInServiceArea}
   * (throwing an {@link IllegalStateException} if not).
   * 
   * <p>
   * It adds a {@link SimQueueSimpleEventType#DEPARTURE} pending notification.
   * 
   * <p>
   * It then invokes the subclass-specific {@link #rescheduleAfterDeparture}.
   * 
   * <p>
   * This method does <i>not</i> notify listeners through {@link #fireAndLockPendingNotifications}.
   * 
   * <p>
   * Note that this method does <i>not</i> (attempt to) cancel a {@link Departure}
   * for the job, nor does it maintain {@link #eventsScheduled}!
   * 
   * @param time The departure time.
   * @param job  The job that departs.
   * 
   * @see SimQueueSimpleEventType#DEPARTURE
   * @see #removeJobFromQueueUponDeparture
   * @see #rescheduleAfterDeparture
   * @see #update
   * @see #getLastUpdateTime
   * @see SimJob#setQueue
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #addPendingNotification(SimEntitySimpleEventType.Member, SimEntityEvent)
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void depart (final double time, final J job)
  {
    if (job.getQueue () != this)
      throw new IllegalStateException ();
    if (time != getLastUpdateTime ())
      throw new IllegalStateException ();
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (isTopLevel)
      throw new IllegalStateException ();
    job.setQueue (null);
    removeJobFromQueueUponDeparture (job, time);
    if (this.jobQueue.contains (job)
      || this.jobsInServiceArea.contains (job))
      throw new IllegalStateException ();
    addPendingNotification (SimQueueSimpleEventType.DEPARTURE, new SimJQEvent.Departure<> (job, this, time));
    rescheduleAfterDeparture (job, time);
  }
  
  /** Removes a job from the internal queues upon departure.
   * 
   * <p>
   * Implementations <i>must</i> (at least) remove the job from {@link #jobQueue} (this is actually checked).
   * They should also remove any job-specific events (like a queue-specific departure event) from the event-list and remove the
   * job (if needed) from the {@link #jobsInServiceArea} set (this is also checked).
   * 
   * <p>
   * Implementations must <i>not</i> reschedule (e.g., events for <i>other</i> jobs on the event list),
   * but instead wait for the imminent invocation of
   * {@link #rescheduleAfterDeparture} for that.
   * 
   * <p>
   * Implementations must not fire departure events. This is done by the base class {@link AbstractSimQueue}.
   * 
   * @param departingJob The job that departs.
   * @param time         The departure (current) time.
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
   * Implementations must <i>not</i> insert a {@link SimQueueSimpleEventType#DEPARTURE} notification,
   * as this is already done by the base class {@link AbstractSimQueue}.
   * They must, however, add appropriate notifications for other internal state-changing events.
   * 
   * @param departedJob The departed job.
   * @param time        The departure (current) time.
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
  
  /** Schedules a suitable {@link SimEvent} for a job's future departure on the event list.
   * 
   * The implementation requires passing several rigorous sanity checks,
   * after which it creates a new {@link Departure},
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
   * @see SimJQEventScheduler#scheduleJQ(SimEventList, SimJQEvent)
   * 
   */
  protected final SimJQEvent.Departure<J, Q> scheduleDepartureEvent (final double time, final J job)
  {
    if (time < getLastUpdateTime () || job == null || job.getQueue () != this)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    final SimJQEvent.Departure<J, Q>  event = new SimJQEvent.Departure<> (job, (Q) this, time,
      (SimEventAction) (final SimEvent e) ->
      {
        AbstractSimQueue.this.departureFromEventList ((SimJQEvent.Departure) e);
      });
    SimJQEventScheduler.scheduleJQ (getEventList (), event);
    this.eventsScheduled.add (event);
    return event;
  }
  
  /** Cancels a pending departure event on the event list.
   * 
   * <p>
   * After sanity checks on the event not being {@code null},
   * and on the event's presence in {@link #getEventList} and {@link #eventsScheduled},
   * this method removes the event from the event list and from {@link #eventsScheduled}.
   * 
   * <p>
   * The base class {@link AbstractSimQueue} does not use this method; it is provided as a service to subclasses.
   * 
   * @param event The departure event to cancel, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the argument is {@code null}, or not present
   *                                  in both {@link #getEventList} and {@link #eventsScheduled}.
   * 
   * @see #eventsScheduled
   * @see #getEventList
   * 
   */
  protected final void cancelDepartureEvent (final SimJQEvent.Departure<J, Q> event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! getEventList ().contains (event))
      throw new IllegalArgumentException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalArgumentException ();
    // The following check seems a bit too much...
    // This method is often called from the need to reschedule, e.g., after a revocation,
    // and thus we should not insist that the job is still being present in the jobQueue.
    // if (! this.jobQueue.contains (event.getJob ()))
    //   throw new IllegalArgumentException ();
    this.eventsScheduled.remove (event);
    getEventList ().remove (event);
  }
  
  /** Cancels a pending departure event for given job on the event list.
   * 
   * <p>
   * After several rigorous sanity checks, this default implementation
   * removes the event from the event list and from {@link #eventsScheduled}.
   * Note that a unique {@link Departure} must be found in {@link #eventsScheduled} for the job supplied,
   * otherwise a {@link IllegalArgumentException} is thrown.
   * 
   * <p>
   * The base class {@link AbstractSimQueue} does not use this method; it is provided as a service to subclasses.
   * 
   * @param job The job for which the unique departure event to cancel.
   * 
   * @throws IllegalArgumentException If zero or multiple {@link Departure}s
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
    final Set<SimJQEvent.Departure> set = getDepartureEvents (job);
    if (set == null || set.size () != 1)
      throw new IllegalArgumentException ();
    cancelDepartureEvent (set.iterator ().next ());
  }
  
  /** Gets all departure events.
   * 
   * The (final) implementation returns all {@link Departure}s in {@link #eventsScheduled}.
   * 
   * @return A non-<code>null</code> {@link Set} holding all future departure events.
   * 
   */
  protected final Set<SimJQEvent.Departure> getDepartureEvents ()
  {
    final Set<SimJQEvent.Departure> set = new LinkedHashSet<> ();
    for (final SimEvent e : this.eventsScheduled)
      if (e == null)
        throw new IllegalStateException ();
      // JdJ20150913: I have no clue why the next statement does not work...
      // XXX TBD
      // else if (! (e instanceof Departure))
      //  continue;
      else if (! SimJQEvent.Departure.class.isAssignableFrom (e.getClass ()))
        /* continue */ ;
      else
        set.add ((SimJQEvent.Departure) e);
    return set;
  }
  
  /** Gets all departure events for given job.
   * 
   * The (final) implementation returns all {@link Departure}s for the given job in {@link #eventsScheduled}.
   * 
   * @param job The job.
   * 
   * @return A non-<code>null</code> {@link Set} holding all scheduled departure events for the job.
   * 
   */
  protected final Set<SimJQEvent.Departure> getDepartureEvents (final J job)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    final Set<SimJQEvent.Departure> set = new LinkedHashSet<> ();
    for (SimEvent e : this.eventsScheduled)
      if (e == null)
        throw new IllegalStateException ();
      // JdJ20150913: I have no clue why the next statement does not work...
      // XXX TBD
      // else if (! (e instanceof Departure))
      //  continue;
      else if (! SimJQEvent.Departure.class.isAssignableFrom (e.getClass ()))
        /* continue */ ;
      else if (((SimJQEvent.Departure) e).getJob () != job)
        /* continue */ ;
      else
        set.add ((SimJQEvent.Departure) e);
    return set;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NOTIFICATIONS (PRIVATE)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all queue listeners of the start of a queue-access vacation.
   * 
   */
  private void fireStartQueueAccessVacation (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueEvent.QueueAccessVacation))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ! ((SimQueueEvent.QueueAccessVacation) event).getVacation ())
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyStartQueueAccessVacation (time, this);
  }
  
  /** Notifies all queue listeners of the end of a queue-access vacation.
   * 
   */
  private void fireStopQueueAccessVacation (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueEvent.QueueAccessVacation))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ((SimQueueEvent.QueueAccessVacation) event).getVacation ())
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyStopQueueAccessVacation (time, this);
  }
  
  /** Notifies all queue listeners that this queue has run out of server-access credits.
   * 
   */
  private void fireOutOfServerAccessCredits (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueEvent.ServerAccessCredits))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ((SimQueueEvent.ServerAccessCredits) event).getCredits () > 0)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyOutOfServerAccessCredits (time, this);
  }
  
  /** Notifies all queue listeners that this queue has regained server-access credits.
   * 
   */
  private void fireRegainedServerAccessCredits (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueEvent.ServerAccessCredits))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ((SimQueueEvent.ServerAccessCredits) event).getCredits () == 0)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyRegainedServerAccessCredits (time, this);
  }

  /** Notifies all queue listeners of a change in the <code>startArmed</code> property, turning {@code false}.
   * 
   */
  private void fireLostStartArmed (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueEvent.StartArmed))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ((SimQueueEvent.StartArmed) event).isStartArmed ())
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyNewStartArmed (time, this, false);
  }

  /** Notifies all queue listeners of a change in the <code>startArmed</code> property, turning {@code true}.
   * 
   */
  private void fireRegainedStartArmed (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueEvent.StartArmed))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ! ((SimQueueEvent.StartArmed) event).isStartArmed ())
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyNewStartArmed (time, this, true);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
