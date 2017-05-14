package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link GraySimQueueComposite}.
 *
 * @param <DQ> The (base) type for sub-queues.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public abstract class AbstractGraySimQueueComposite
<DQ extends SimQueue, J extends SimJob, Q extends AbstractGraySimQueueComposite>
extends AbstractSimQueueComposite<J, DQ, J, Q>
implements GraySimQueueComposite<DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract gray network of queues.
   * 
   * <p>
   * After calling the super constructor,
   * this methods creates a new {@link MultiSimQueueNotificationProcessor} for all sub-queues,
   * and registers {@link #processSubQueueNotifications} as its processor.
   * Finally, it resets the local part of the object through a (private) variant of {@link #resetEntitySubClass}
   * that does not invoke its super method.
   * 
   * @param eventList        The event list to be shared between this queue and the sub-queues.
   * @param queues           A set holding the sub-queues.
   * @param simQueueSelector The object for routing jobs through the network of sub-queues;
   *                           if {@code null}, no sub-queues will be visited.
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    or the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processSubQueueNotifications
   * @see #resetEntitySubClass
   * 
   */
  protected AbstractGraySimQueueComposite
  (final SimEventList eventList,
    final Set<DQ> queues,
    final SimQueueSelector simQueueSelector)
  {
    super (eventList, queues, simQueueSelector);
    final MultiSimQueueNotificationProcessor<J, DQ>  subQueueEventProcessor =
      new MultiSimQueueNotificationProcessor<> (getQueues ());
    subQueueEventProcessor.setProcessor (this::processSubQueueNotifications);
    resetEntitySubClassLocal ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // COLOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@link Color#GRAY}.
   * 
   * @return {@link Color#GRAY}.
   * 
   */
  @Override
  public final Color getColor ()
  {
    return Color.GRAY;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MANAGED JOBS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Set<J> managedJobs = new LinkedHashSet<> ();
  
  /** Returns an unmodifiable set holding the managed jobs.
   * 
   * @return An unmodifiable set holding the managed jobs.
   * 
   */
  @Override
  public final Set<J> getManagedJobs ()
  {
    return Collections.unmodifiableSet (this.managedJobs);
  }

  /** Returns the size of {@link #getManagedJobs}.
   * 
   * @return The size of {@link #getManagedJobs}.
   * 
   */
  @Override
  public final int getNumberOfManagedJobs ()
  {
    return this.managedJobs.size ();
  }
  
  /** Checks whether a given job is present at or managed by any of the sub-queues.
   * 
   * <p>
   * Note that this method does <i>not</i> check the presence of the job on or being managed by the present queue.
   * 
   * @param j The job, if <code>null</code>, <code>false</code> is returned.
   * 
   * @return <code>True</code> if and only if the job is present on one of the sub-queues,
   *                             or managed by it in case the queue is composite.
   * 
   * @see #getQueues
   * @see SimQueue#getJobs
   * @see SimQueueComposite#getManagedJobs
   * 
   */
  protected final boolean isPresentOrManagedAtSubQueue (final J j)
  {
    if (j == null)
      return false;
    for (final DQ q : getQueues ())
      if (q.getJobs ().contains (j))
        return true;
      else if ((q instanceof SimQueueComposite) && ((SimQueueComposite) q).getManagedJobs ().contains (j))
        return true;
    return false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractGraySimQueueComposite}.
   * 
   * <p>
   * Calls super method (not if called from constructor, for which a private variant for local resets is used),
   * and clears the set of managed jobs.
   * 
   * <p>
   * Note that this method does <i>not</i> reset the sub-queues!
   * 
   * @see SimQueue#resetEntity
   * @see #getManagedJobs
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    // Implemented in private method to make it accessible from the constructor(s).
    resetEntitySubClassLocal ();
  }
  
  private void resetEntitySubClassLocal ()
  {
    this.managedJobs.clear ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isStartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code false} since a gray composite queue is server-less.
   * 
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Performs sanity checks, reverses the managed state of the job and puts the job into {@link #jobQueue}.
   * 
   * @see #jobQueue
   * @see #getManagedJobs
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (job) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    if (this.managedJobs.contains (job))
      this.managedJobs.remove (job);
    else
      this.managedJobs.add (job);
    this.jobQueue.add (job);
  }

  /** Performs sanity checks and makes the job depart.
   * 
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    depart (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing, since we are server-less.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   * @return A {@link IllegalStateException} at your face.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Performs sanity checks and removes the job from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (! this.jobQueue.contains (departingJob))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (departingJob))
      throw new IllegalStateException ();
    this.jobQueue.remove (departingJob);
  }

  /** Performs sanity checks, and if the job is managed, routes it to the first sub-queue to visit if available.
   * 
   * <p>
   * Whether or not the job is being managed, is determined by its presence in {@link #getManagedJobs}.
   * 
   * <p>
   * If the job is <i>not</i> being managed, this method does nothing; the job simply leaves this {@link SimQueue};
   * assuming this is a second-time visit (i.e., a visit that indicates the "departure" of the white composite queue).
   * 
   * <p>
   * If it is being managed, this method invokes {@link #selectFirstQueue} in order to find the first sub-queue
   * the job should visit. If non-<code>null</code>, it invokes {@link SimQueue#arrive} on the sub-queue found.
   * Otherwise, it simply invokes {@link #arrive} (on the present queue) such that the job makes a second visit to the
   * white composite queue, and will eventually depart in the white-composite-queue sense.
   * 
   * @see #jobQueue
   * @see #jobsInServiceArea
   * @see #getManagedJobs
   * @see #selectFirstQueue
   * @see SimQueue#arrive
   * @see #arrive
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (this.jobQueue.contains (departedJob) || this.jobsInServiceArea.contains (departedJob))
      throw new IllegalStateException ();
    if (this.managedJobs.contains (departedJob))
    {
      final DQ firstQueue = selectFirstQueue (time, departedJob);
      if (firstQueue != null)
        firstQueue.arrive (time, departedJob);
      else
        arrive (time, departedJob);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESS (AND SANITY ON) SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queues, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * XXX FIX THIS COMMENT!
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} notifications from all sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for all sub-queues) created upon construction,
   * see {@link MultiSimQueueNotificationProcessor.Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * Before processing any event, this method checks for {@link SimEntitySimpleEventType#RESET}
   * (sub-)notifications.
   * If it finds <i>any</i>, the notifications list is cleared and immediate return from this method follows.
   * 
   * <p>
   * Otherwise, this method takes one notification at a time, starting at the head of the list, removes it,
   * and processes it as described below.
   * While processing, new notifications may be added to the list; the list is processed until it is empty.
   * The method is encapsulated in a
   * {@link #clearAndUnlockPendingNotificationsIfLocked} and {@link #fireAndLockPendingNotifications} pair,
   * to make sure we create atomic notifications in case of a top-level event.
   * 
   * <p>
   * A notification consists of a (fixed) sequence of sub-notifications,
   * see {@link MultiSimQueueNotificationProcessor.Notification#getSubNotifications},
   * each of which is processed in turn as follows:
   * <ul>
   * <li>With {@link SimEntitySimpleEventType#RESET},
   *       an {@link IllegalStateException} is thrown.
   * <li>Otherwise, if the notification is not job-related, or the job is not managed by the enclosing gray composite queue,
   *       the sub-notification is ignored.
   * <li>With {@link SimEntitySimpleEventType#QAV_START} and {@link SimEntitySimpleEventType#QAV_END},
   *       this method does nothing (note that these notification types are already covered by the previous item).
   * <li>With {@link SimEntitySimpleEventType#ARRIVAL} and {@link SimEntitySimpleEventType#START},
   *       this method does nothing.
   * <li>With {@link SimEntitySimpleEventType#DROP},
   *          {@link SimQueueSimpleEventType#AUTO_REVOCATION},
   *      and {@link SimQueueSimpleEventType#REVOCATION},
   *       this method invokes for the job in question,
   *       {@link AbstractGraySimQueueComposite#arrive} on the enclosing {@link AbstractGraySimQueueComposite}
   *       thus enforcing a (second) arrival at the enclosing {@link AbstractGraySimQueueComposite},
   *       and therefore, a departure in the gray-composite-queue sense.
   * <li>With {@link SimQueueSimpleEventType#DEPARTURE}, we invoke {@link #selectNextQueue} on the job,
   *                                                     and let the job arrive at the next queue if provided,
   *                                                     or makes the job depart in the gray-composite-queue sense
   *                                                     if not through {@link GraySimQueueComposite#arrive}.
   * <li>With any other non-standard notification type, this method does nothing.
   * </ul>
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty,
   *                                  or contains a notification from another queue than the a sub-queue,
   *                                  or if some other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor.Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #sanitySubQueueNotification
   * @see SimEntitySimpleEventType#RESET
   * @see SimEntitySimpleEventType#QAV_START
   * @see SimEntitySimpleEventType#QAV_END
   * @see SimEntitySimpleEventType#ARRIVAL
   * @see SimEntitySimpleEventType#DROP
   * @see SimEntitySimpleEventType#REVOCATION
   * @see SimEntitySimpleEventType#AUTO_REVOCATION
   * @see SimEntitySimpleEventType#START
   * @see SimEntitySimpleEventType#DEPARTURE
   * @see #addPendingNotification
   * @see SimQueue#arrive
   * @see #start
   * @see #selectNextQueue
   * @see #depart
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<J, DQ>> notifications)
  {
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ();
    // Special treatment of RESET notifications: clear everthing and return immediately.
    boolean containsResetNotification = false;
    for (final MultiSimQueueNotificationProcessor.Notification<J, DQ> notification : notifications)
    {
      for (final Map<SimEntitySimpleEventType.Member, J> subNotification : notification.getSubNotifications ())
        if (subNotification.keySet ().iterator ().next () == SimEntitySimpleEventType.RESET)
        {
          containsResetNotification = true;
          break;
        }
      if (containsResetNotification)
        break;
    }
    if (containsResetNotification)
    {
      notifications.clear ();
      return;
    }
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    while (! notifications.isEmpty ())
    {
      final MultiSimQueueNotificationProcessor.Notification<J, DQ> notification = notifications.remove (0);
      final double notificationTime = notification.getTime ();
      if (notificationTime != getLastUpdateTime ())
        throw new IllegalArgumentException ("on " + this + ": notification time [" + notificationTime
          + "] != last update time [" + getLastUpdateTime () + "], subnotifications: "
          + notification.getSubNotifications () + ".");
      final DQ subQueue = notification.getQueue ();
      if (subQueue == null || ! AbstractGraySimQueueComposite.this.getQueues ().contains (subQueue))
        throw new IllegalStateException ();
      for (final Map<SimEntitySimpleEventType.Member, J> subNotification : notification.getSubNotifications ())
      {
        final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
        final J job = subNotification.values ().iterator ().next ();
        if (notificationType == SimEntitySimpleEventType.RESET)
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.QAV_START
              || notificationType == SimQueueSimpleEventType.QAV_END
              || notificationType == SimQueueSimpleEventType.ARRIVAL
              || notificationType == SimQueueSimpleEventType.START)
          ; /* NOTHING TO DO */          
        else if (job == null || ! AbstractGraySimQueueComposite.this.managedJobs.contains (job))
          ; /* NOTHING TO DO */
        else if (notificationType == SimEntitySimpleEventType.DROP
              || notificationType == SimEntitySimpleEventType.AUTO_REVOCATION
              || notificationType == SimEntitySimpleEventType.REVOCATION)
          AbstractGraySimQueueComposite.this.arrive (notificationTime, job);
        else if (notificationType == SimEntitySimpleEventType.DEPARTURE)
        {
          final SimQueue<J, DQ> nextQueue = selectNextQueue (notificationTime, job, subQueue);
          if (nextQueue == null)
            AbstractGraySimQueueComposite.this.arrive (notificationTime, job);
          else
            nextQueue.arrive (notificationTime, job);
        }
      }
    }
    if (! notifications.isEmpty ())
      throw new IllegalStateException ();
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
   
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE UPDATE NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls {@link #update} in order to update our own time in response to an increase in time on one of the sub-queues.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * @throws IllegalStateException    If time is in the past.
   * 
   * @see #getQueues
   * @see #update
   * @see #getLastUpdateTime
   * 
   */
  @Override
  public final void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    update (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE NOTIFICATIONS OTHER THAN UPDATE (ALL EMPTY; SEE SUB-QUEUE NOTIFICATION PROCESSOR)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override  
  public final void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, J>> notifications)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyArrival (final double time, final J job, final DQ queue)
  {
  }
  
  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStartQueueAccessVacation (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStopQueueAccessVacation (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyNewStartArmed (final double time, final DQ queue, final boolean startArmed)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyOutOfServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyRegainedServerAccessCredits (final double time, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyStart (final double time, final J job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyDrop (final double time, final J job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyRevocation (final double time, final J job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyAutoRevocation (final double time, final J job, final DQ queue)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyDeparture (final double time, final J job, final DQ queue)
  {
  }

}
