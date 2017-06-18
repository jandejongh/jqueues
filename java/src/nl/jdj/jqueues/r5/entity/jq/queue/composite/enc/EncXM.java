package nl.jdj.jqueues.r5.entity.jq.queue.composite.enc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue}
 *  with options to change the ways in which a job exits the queue.
 *
 * <p>
 * This composite queue (precisely) mimics the {@link SimQueue} interface of the encapsulated queue,
 * including non-standard operations and notifications,
 * yet optionally maps e.g. a departure onto a drop or vice versa.
 * Support is provided for mapping (all) departures, drops and/or auto-revocations.
 * Note that mapping <i>revocations</i> is not supported,
 * since that would violate the {@link SimQueue} interface.
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
 * @see MappableExitMethod
 * @see SimQueueComposite
 * @see Enc
 * @see #drop
 * @see #autoRevoke
 * @see #depart
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
public class EncXM
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends EncXM>
  extends AbstractEncapsulatorSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue with options to change the ways in which a job exits the queue,
   *  given an event list and a queue.
   *
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param dropMapping           The exit method for drops; if {@code null}, drops are not mapped
   *                                (and thus result in the drop of the real job).
   * @param autoRevocationMapping The exit method for auto-revocations; if {@code null}, auto-revocations are not mapped
   *                                (and thus result in the auto-revocation of the real job).
   * @param departureMapping      The exit method for departures; if {@code null}, departures are not mapped
   *                                (and thus result in the departure of the real job).
   * @param delegateSimJobFactory An optional factory for the delegate {@code SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public EncXM
  (final SimEventList eventList,
   final DQ queue,
   final MappableExitMethod dropMapping,
   final MappableExitMethod autoRevocationMapping,
   final MappableExitMethod departureMapping,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, delegateSimJobFactory);
    this.dropMapping = (dropMapping != null ? dropMapping : MappableExitMethod.DROP);
    this.autoRevocationMapping = (autoRevocationMapping != null ? autoRevocationMapping : MappableExitMethod.AUTO_REVOCATION);
    this.departureMapping = (departureMapping != null ? departureMapping : MappableExitMethod.DEPARTURE);
  }
  
  /** Returns a new {@link EncXM} object on the same {@link SimEventList}
   *  with a copy of the encapsulated queue, the same exit mappings, and the same delegate-job factory.
   * 
   * @return A new {@link EncXM} object on the same {@link SimEventList}
   *         with a copy of the encapsulated queue, the same exit mappings, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDropMapping
   * @see #getAutoRevocationMapping
   * @see #getDepartureMapping
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public EncXM<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new EncXM (getEventList (),
                      encapsulatedQueueCopy,
                      getDropMapping (), getAutoRevocationMapping (), getDepartureMapping (),
                      getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "EncXM(Dr-&gt;XMDrop,AR-&gt;XMAR,De-&gt;XMDep)[encapsulated queue]".
   * 
   * <p>
   * The three exit-method mappings are shown as comma-separated list;
   * for drops, auto-revocations, and departures, respectively.
   * If however, an exit method is mapped onto itself,
   * the corresponding entry is left out.
   * So {@code EncXM()[encQueue] == Enc[encQueue]}.
   * 
   * @return "EncXM(Dr-&gt;XMDrop,AR-&gt;XMAR,De-&gt;XMDep)[encapsulated queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    String mapping = "";
    boolean first = true;
    if (getDropMapping () != MappableExitMethod.DROP)
    {
      mapping = "Dr->" + getDropMapping ();
      first = false;
    }
    if (getAutoRevocationMapping () != MappableExitMethod.AUTO_REVOCATION)
    {
      if (first)
        mapping += "AR->";
      else
        mapping += ",AR->";
      mapping += getAutoRevocationMapping ();
      first = false;
    }
    if (getDepartureMapping () != MappableExitMethod.DEPARTURE)
    {
      if (first)
        mapping += "De->";
      else
        mapping += ",De->";
      mapping += getDepartureMapping ();
    }
    return "EncXM(" + mapping + ")[" + getEncapsulatedQueue () + "]";
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
  // EXIT METHOD MAPPING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The mappable exit methods.
   * 
   * <p>
   * The values represent three (out of four) different methods a job can exit the encapsulated queue.
   * Note that the fourth method, a <i>revocation</i>,
   * is not supported by {@link EncXM},
   * in other words,
   * you cannot map this onto one of the other exit methods
   * (which would violate the {@link SimQueue} interface).
   * 
   * @see EncXM
   *
   */
  public static enum MappableExitMethod
  {
    
    /** A job drop (on the encapsulated queue).
     * 
     */
    DROP ("Dr"),
    
    /** A job auto-revocation (on the encapsulated queue).
     * 
     */
    AUTO_REVOCATION ("AR"),
    
    /** A job departure (on the encapsulated queue).
     * 
     */
    DEPARTURE ("De");

    private MappableExitMethod (final String name)
    {
      this.name = name;
    }
    
    private final String name;

    /** Returns the name of this enum value; fixed at construction.
     * 
     * @return The name of this enum value; fixed at construction.
     * 
     */
    @Override
    public final String toString ()
    {
      return this.name;
    }
    
  }
  
  private final MappableExitMethod dropMapping;

  /** Returns the mapping of job drops on the encapsulated queue.
   * 
   * @return The mapping of job drops on the encapsulated queue, non-{@code null}.
   * 
   */
  public final MappableExitMethod getDropMapping ()
  {
    return this.dropMapping;
  }

  private final MappableExitMethod autoRevocationMapping;
  
  /** Returns the mapping of auto-revocations on the encapsulated queue.
   * 
   * @return The mapping of auto-revocations on the encapsulated queue, non-{@code null}.
   * 
   */
  public final MappableExitMethod getAutoRevocationMapping ()
  {
    return this.autoRevocationMapping;
  }

  private final MappableExitMethod departureMapping;
   
  /** Returns the mapping of departures on the encapsulated queue.
   * 
   * @return The mapping of departures on the encapsulated queue, non-{@code null}.
   * 
   */
  public final MappableExitMethod getDepartureMapping ()
  {
    return this.departureMapping;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    super.insertJobInQueueUponArrival (job, time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    super.rescheduleAfterArrival (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    super.removeJobFromQueueUponDrop (job, time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    super.rescheduleAfterDrop (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    super.removeJobFromQueueUponRevokation (job, time, auto);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    super.rescheduleAfterRevokation (job, time, auto);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super call.
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    super.insertJobInQueueUponStart (job, time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    super.rescheduleAfterStart (job, time);
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

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    super.removeJobFromQueueUponDeparture (departingJob, time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    super.rescheduleAfterDeparture (departedJob, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Replaces applicable exit sub-notifications from the encapsulated queue,
   *  and, subsequently, invokes the super method.
   * 
   * @see #getDropMapping
   * @see #getAutoRevocationMapping
   * @see #getDepartureMapping
   * 
   */
  @Override
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    //
    // Empty or null notifications => let super-class throw the exception.
    //
    if (notifications == null || notifications.isEmpty ())
    {
      super.processSubQueueNotifications (notifications);
      return;
    }
    //
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    //
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    final SimQueue<DJ, DQ> encQueue = getEncapsulatedQueue ();
    //
    // Iterate over all notifications.
    //
    for (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification : notifications)
    {
      //
      // Sanity checks on notification time and source queue.
      //
      if (notification.getTime () != getLastUpdateTime ())
        throw new IllegalStateException ();
      if (notification.getQueue () != getEncapsulatedQueue ())
        throw new IllegalStateException ();
      //
      // Iterate over the notification's sub-notifications.
      // Use an index so we can replace sub-notifications in situ.
      //
      final List<Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>>> subNotifications = notification.getSubNotifications ();
      for (int i = 0; i < subNotifications.size (); i++)
      {
        final Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>> subNotification = subNotifications.get (i);
        //
        // Sanity check on sub-notification.
        //
        if (subNotification.size () != 1)
          throw new IllegalArgumentException ();
        //
        // Replace a DROP if needed.
        //
        if (getDropMapping () != MappableExitMethod.DROP
        &&  subNotification.containsKey (SimJQSimpleEventType.DROP))
        {
          if (subNotification.get (SimJQSimpleEventType.DROP).getTime () != getLastUpdateTime ())
            throw new IllegalArgumentException ();
          final DJ job = subNotification.get (SimJQSimpleEventType.DROP).getJob ();
          switch (getDropMapping ())
          {
            case AUTO_REVOCATION:
              subNotifications.set (i,
                Collections.singletonMap (SimJQSimpleEventType.AUTO_REVOCATION,
                                          new SimJQEvent.AutoRevocation (job, encQueue, notification.getTime ())));
              break;
            case DEPARTURE:
              subNotifications.set (i,
                Collections.singletonMap (SimJQSimpleEventType.DEPARTURE,
                                          new SimJQEvent.Departure (job, encQueue, notification.getTime ())));
              break;
            default:
              throw new RuntimeException ();
          }
        }
        //
        // Replace an AUTO_REVOCATION if needed.
        //
        else if (getAutoRevocationMapping () != MappableExitMethod.AUTO_REVOCATION
             &&  subNotification.containsKey (SimJQSimpleEventType.AUTO_REVOCATION))
        {
          if (subNotification.get (SimJQSimpleEventType.AUTO_REVOCATION).getTime () != getLastUpdateTime ())
            throw new IllegalArgumentException ();
          final DJ job = subNotification.get (SimJQSimpleEventType.AUTO_REVOCATION).getJob ();
          switch (getAutoRevocationMapping ())
          {
            case DROP:
              subNotifications.set (i,
                Collections.singletonMap (SimJQSimpleEventType.DROP,
                                          new SimJQEvent.Drop (job, encQueue, notification.getTime ())));
              break;
            case DEPARTURE:
              subNotifications.set (i,
                Collections.singletonMap (SimJQSimpleEventType.DEPARTURE,
                                          new SimJQEvent.Departure (job, encQueue, notification.getTime ())));
              break;
            default:
              throw new RuntimeException ();
          }
        }
        //
        // Replace a DEPARTURE if needed.
        //
        else if (getDepartureMapping () != MappableExitMethod.DEPARTURE
             &&  subNotification.containsKey (SimJQSimpleEventType.DEPARTURE))
        {
          if (subNotification.get (SimJQSimpleEventType.DEPARTURE).getTime () != getLastUpdateTime ())
            throw new IllegalArgumentException ();
          final DJ job = subNotification.get (SimJQSimpleEventType.DEPARTURE).getJob ();
          switch (getDepartureMapping ())
          {
            case DROP:
              subNotifications.set (i,
                Collections.singletonMap (SimJQSimpleEventType.DROP,
                                          new SimJQEvent.Drop (job, encQueue, notification.getTime ())));
              break;
            case AUTO_REVOCATION:
              subNotifications.set (i,
                Collections.singletonMap (SimJQSimpleEventType.AUTO_REVOCATION,
                                          new SimJQEvent.AutoRevocation (job, encQueue, notification.getTime ())));
              break;
            default:
              throw new RuntimeException ();
          }
        }
      }
    }
    //
    // Invoke super method for (sub-)notifications to process.
    //
    super.processSubQueueNotifications (notifications);
    //
    // Fire notification as we are most likely a top-level notification.
    //
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
