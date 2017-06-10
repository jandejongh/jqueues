package nl.jdj.jqueues.r5.entity.jq.queue.composite.enc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite.StartModel;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.entity.jq.queue.processorsharing.PS;
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
 * By default, real jobs for which waiting, service or sojourn time expire will <i>depart</i>,
 * and in that sense, cannot be distinguished from regular departures on the encapsulated queue.
 * However, one can control this behavior through {@link ExpirationMethod} and {@link #setExprirationMethod}.
 * 
 * <p>
 * The start model is set to (fixed) {@link StartModel#ENCAPSULATOR_QUEUE}.
 * 
 * <p>
 * The semantics of either expiration time being zero are a bit tricky, but in essence, the encapsulated queue takes precedence.
 * In other words, setting an expiration time to zero, does not (always) <i>enforce</i>
 * the applicable expiration events to take place.
 * For instance, if the encapsulated queue takes a job into service immediately upon arrival (like {@link PS} does),
 * {@code maxWaitingTime = 0} has no effect, i.e., no revocation (due to expiration) takes place.
 * If in addition the encapsulated queue serves jobs in zero time (like {@link IC} does),
 * {@code maxSojournTime = 0} has no effect either.
 * These arguments also imply that we can safely inherit {@link AbstractSimQueueComposite#isStartArmed}
 * (which is {@code final} anyway...).
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
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
 * @see Enc
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
   * The constructor sets the {@link StartModel} to {@link StartModel#ENCAPSULATOR_QUEUE},
   * and {@link ExpirationMethod} to {@link ExpirationMethod#DEPARTURE}.
   * In addition, it registers private listeners on relevant sub-queue events,
   * viz., arrival, drop, revocation, auto-revocation, start, and departure,
   * through {@link #registerSubQueueSubNotificationProcessor}.
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
   * @see StartModel
   * @see StartModel#ENCAPSULATOR_QUEUE
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
    super (eventList, queue, delegateSimJobFactory, false);
    if (maxWaitingTime < 0 || maxServiceTime < 0 || maxSojournTime < 0)
      throw new IllegalArgumentException ();
    this.maxWaitingTime = maxWaitingTime;
    this.maxServiceTime = maxServiceTime;
    this.maxSojournTime = maxSojournTime;
    this.exprirationMethod = ExpirationMethod.DEPARTURE;
    registerSubQueueSubNotificationProcessor (SimJQSimpleEventType.ARRIVAL,         queue, this::processArrival);
    registerSubQueueSubNotificationProcessor (SimJQSimpleEventType.DROP,            queue, this::processExit);
    registerSubQueueSubNotificationProcessor (SimJQSimpleEventType.REVOCATION,      queue, this::processExit);
    registerSubQueueSubNotificationProcessor (SimJQSimpleEventType.AUTO_REVOCATION, queue, this::processExit);
    registerSubQueueSubNotificationProcessor (SimJQSimpleEventType.START,           queue, this::processStart);
    registerSubQueueSubNotificationProcessor (SimJQSimpleEventType.DEPARTURE,       queue, this::processExit);
  }
  
  /** Returns a new {@link EncTL} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue, the same delegate-job factory and equal respective expiration times.
   * 
   * @return A new {@link EncTL} object on the same {@link SimEventList} with a copy of the encapsulated
   *           queue, the same delegate-job factory and equal respective expiration times.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * @see #getMaxWaitingTime
   * @see #getMaxServiceTime
   * @see #getMaxSojournTime
   * 
   */
  @Override
  public EncTL<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new EncTL (getEventList (),
                      encapsulatedQueueCopy,
                      getDelegateSimJobFactory (),
                      getMaxWaitingTime (),
                      getMaxServiceTime (),
                      getMaxSojournTime ());
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

  /** The method for "presenting" jobs for which a time expired at the composite (this) queue.
   * 
   * <p>
   * Note: This method <i>only</i> applies to jobs that "expire"; all other events from the encapsulated queue are
   *       processed as described in {@link AbstractSimQueueComposite} and {@link StartModel#ENCAPSULATOR_QUEUE}.
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
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method.
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    super.setServerAccessCreditsSubClass ();
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
  // RESCHEDULE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void reschedule ()
  {
    // If we are currently processing an event, we just skip.
    // The event handler will reschedule once it has finished.
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
  // EVENTS FROM SUB-QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void processArrival
    (final double time,
     final SimEntitySimpleEventType.Member notificationType,
     final SimJQEvent<DJ, DQ> event,
     final DQ subQueue,
     final DJ delegateJob)
    {
      // Insane sanity check...
      if (time != getLastUpdateTime ()
      || notificationType != SimJQSimpleEventType.ARRIVAL
      || event == null
      || (! (event instanceof SimJQEvent.Arrival))
      || event.getTime () != getLastUpdateTime ()
      || ((SimJQEvent.Arrival) event).getQueue () != subQueue
      || ((SimJQEvent.Arrival) event).getJob () != delegateJob
      || subQueue != getEncapsulatedQueue ()
      || delegateJob == null
      || (! isDelegateJob (delegateJob)))
        throw new IllegalArgumentException ();
      // We always put the delegate job into our arrival-times map upon arrival.
      if (this.arrivalTimes.containsKey (delegateJob))
        throw new IllegalStateException ();
      this.arrivalTimes.put (delegateJob, time);
      // Calculate the expiration time for this visit.
      // First, if both MaxWaitingTime and MaxSojournTime are infinite,
      // we ignore both at this point.
      if (Double.isInfinite (getMaxWaitingTime ()) && Double.isInfinite (getMaxSojournTime ()))
        return;
      // Now we have a finite maximum waiting or sojourn time, but if time is infinite, we must alreay auto-revoke the job.
      if (Double.isInfinite (getLastUpdateTime ()))
        autoRevoke (time, getRealJob (delegateJob));
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
      // Check to see if we need to reschedule the auto-revocation event.
      reschedule ();
    }
  
  private void processStart
    (final double time,
     final SimEntitySimpleEventType.Member notificationType,
     final SimJQEvent<DJ, DQ> event,
     final DQ subQueue,
     final DJ delegateJob)
    {
      // Insane sanity check...
      if (time != getLastUpdateTime ()
      || notificationType != SimJQSimpleEventType.START
      || event == null
      || (! (event instanceof SimJQEvent.Start))
      || event.getTime () != getLastUpdateTime ()
      || ((SimJQEvent.Start) event).getQueue () != subQueue
      || ((SimJQEvent.Start) event).getJob () != delegateJob
      || subQueue != getEncapsulatedQueue ()
      || delegateJob == null
      || (! isDelegateJob (delegateJob)))
        throw new IllegalArgumentException ();
      // We always put the delegate job into our arrival-times map upon arrival.
      if (! this.arrivalTimes.containsKey (delegateJob))
        throw new IllegalStateException ();
      // For the started job, we simply need to recalculate the expiration time,
      // because we do not know whether the current expiration time, if applicable, is due to waiting time restraints or
      // due to sojourn time restraints.
      // Note that we cannot a priori assume that an auto-revocation is scheduled for the job at all...
      // Hence, first, let's remove all expirations for the current job.
      if (containsJobInAutoRevocationSchedule (delegateJob))
        removeJobFromAutoRevocationSchedule (delegateJob);
      // Calculate the expiration time for this visit.
      // First, if both MaxServiceTime and MaxSojournTime are infinite,
      // we ignore both at this point (but we still may have to reschedule!).
      // Note that MaxWaitingTime is no longer relevant at this point.
      if (Double.isFinite (getMaxServiceTime ()) || Double.isFinite (getMaxSojournTime ()))
      {
        // Now we have a finite maximum service or sojourn time, but if time is infinite, we must already auto-revoke the job.
        if (Double.isInfinite (getLastUpdateTime ()))
          autoRevoke (time, getRealJob (delegateJob));
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
      // Check to see if we need to reschedule the auto-revocation event.
      reschedule ();      
    }
  
  private void processExit
    (final double time,
     final SimEntitySimpleEventType.Member notificationType,
     final SimJQEvent<DJ, DQ> event,
     final DQ subQueue,
     final DJ delegateJob)
    {
      // Insane sanity check...
      if (time != getLastUpdateTime ()
      || event == null
      || (! (
                 (notificationType == SimJQSimpleEventType.DROP            && event instanceof SimJQEvent.Drop)
              || (notificationType == SimJQSimpleEventType.REVOCATION      && event instanceof SimJQEvent.Revocation)
              || (notificationType == SimJQSimpleEventType.AUTO_REVOCATION && event instanceof SimJQEvent.AutoRevocation)
              || (notificationType == SimJQSimpleEventType.DEPARTURE       && event instanceof SimJQEvent.Departure)
            )
         )
      || event.getTime () != getLastUpdateTime ()
      || ((SimJQEvent) event).getQueue () != subQueue
      || ((SimJQEvent) event).getJob () != delegateJob
      || subQueue != getEncapsulatedQueue ()
      || delegateJob == null
//      || (! isDelegateJob (delegateJob))
        )
        throw new IllegalArgumentException ("notificationType=" + notificationType + ", job=" + delegateJob + ".");
      // We always put the delegate job into our arrival-times map upon arrival.
      if (! this.arrivalTimes.containsKey (delegateJob))
        throw new IllegalStateException ();
      this.arrivalTimes.remove (delegateJob);
      // Remove all expirations for the current job (if any).
      if (containsJobInAutoRevocationSchedule (delegateJob))
        removeJobFromAutoRevocationSchedule (delegateJob);
      // Check to see if we need to reschedule the auto-revocation event.      
      reschedule ();
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
      switch (this.exprirationMethod)
      {
        // XXX The code for DROP could better be done at our super-class...
        case DROP:
          removeJobFromQueueUponRevokation (realJob, time, true);
          rescheduleAfterRevokation (realJob, time, true);
          addPendingNotification (SimQueueSimpleEventType.DROP, new SimJQEvent.Drop<> (realJob, this, time));
          break;
        case AUTO_REVOCATION:
          autoRevoke (time, realJob);
          break;
        case DEPARTURE:
          depart (time, realJob);
          break;
        default:
          throw new RuntimeException ();
      }
    }
    this.eventsScheduled.remove (this.scheduledRevocationEvent);
    this.scheduledRevocationEvent = null;
    this.processingOwnAutoRevocationEvent = false;
    reschedule ();
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
