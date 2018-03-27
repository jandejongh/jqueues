package nl.jdj.jqueues.r5.entity.jq.queue.serverless;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueGateEvent;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGate;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGateListener;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGateOperationUtils;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGateSimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link GATE} queue lets jobs depart without service conditionally ("gate is open") or lets them wait ("gate is closed").
 * 
 * <p>
 * The gate status is a {@link SimQueue} <i>state variable</i>, i.e., it can be changed from an event list being run.
 * 
 * <p>
 * The state of the gate is represented by a single non-negative number, the <i>gate passage credits</i>.
 * If the credits are zero, the gate is closed, and if infinite ({@link Integer#MAX_VALUE}), the gate is open without limits.
 * If the number of credits is in between, the gate is also open, but only for that number of job passages.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see DROP
 * @see SINK
 * @see ZERO
 * @see DELAY
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
public class GATE<J extends SimJob, Q extends GATE>
extends AbstractServerlessSimQueue<J, Q>
implements SimQueueWithGate<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link GATE} queue with infinite buffer size given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public GATE (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE);
    registerOperation (SimQueueWithGateOperationUtils.GatePassageCreditsOperation.getInstance ());
    registerNotificationType (SimQueueWithGateSimpleEventType.GATE_CLOSED, this::fireGateClosed);
    registerNotificationType (SimQueueWithGateSimpleEventType.GATE_OPEN, this::fireGateOpen);
    registerPreNotificationHook (this::gatePassageCreditsPreNotificationHook);
  }
  
  /** Returns a new {@link GATE} object on the same {@link SimEventList}.
   * 
   * @return A new {@link GATE} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public GATE<J, Q> getCopySimQueue ()
  {
    return new GATE<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "GATE".
   * 
   * @return "GATE".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "GATE";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private int gatePassageCredits = Integer.MAX_VALUE;

  @Override
  public int getGatePassageCredits ()
  {
    return this.gatePassageCredits;
  }
  
  /** Updates the internal administration and releases (makes depart) waiting jobs if applicable.
   * 
   * <p>
   * This must be a top-level event, at the expense of a {@link IllegalStateException}.
   * 
   * @see #getGatePassageCredits
   * @see #update
   * @see #hasJobsInWaitingArea
   * @see #depart
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * 
   */
  @Override
  public final void setGatePassageCredits (final double time, final int gatePassageCredits)
  {
    if (gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    final int oldGatePassageCredits = this.gatePassageCredits;
    if (oldGatePassageCredits != gatePassageCredits)
    {
      update (time);
      this.gatePassageCredits = gatePassageCredits;
      final boolean lostCredits = (oldGatePassageCredits > 0 && this.gatePassageCredits == 0);
      final boolean regainedCredits = (oldGatePassageCredits == 0 && this.gatePassageCredits > 0);
      final boolean needsNotification = lostCredits || regainedCredits;
      if (needsNotification)
      {
        if (! clearAndUnlockPendingNotificationsIfLocked ())
          throw new IllegalStateException ();
        while (this.gatePassageCredits > 0 && hasJobsInWaitingArea ())
        {
          depart (time, getFirstJobInWaitingArea ());
          if (this.gatePassageCredits < Integer.MAX_VALUE)
            this.gatePassageCredits--;
        }
        fireAndLockPendingNotifications ();
      }
    }
  }
  
  // Every SimQueueWithGate must have infinite gpcs upon construction and after reset.
  private boolean previousGatePassageCreditsAvailability = true;
  
  /** The pre-notification hook for gate-passage credits.
   * 
   * @see SimQueueWithGateSimpleEventType#GATE_CLOSED
   * @see SimQueueWithGateSimpleEventType#GATE_OPEN
   * 
   */
  private void gatePassageCreditsPreNotificationHook
  (final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> pendingNotifications)
  {
    if (pendingNotifications == null)
      throw new IllegalArgumentException ();
    for (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> entry : pendingNotifications)
    {
      final SimEntitySimpleEventType.Member notificationType = entry.keySet ().iterator ().next ();
      if (notificationType == SimQueueWithGateSimpleEventType.GATE_CLOSED
      ||  notificationType == SimQueueWithGateSimpleEventType.GATE_OPEN)
        throw new IllegalArgumentException ();
    }
    final boolean gatePassageCreditsAvailability = getGatePassageCredits () > 0;
    if (gatePassageCreditsAvailability != this.previousGatePassageCreditsAvailability )
    {
      final double time = getLastUpdateTime ();
      final int gac = getGatePassageCredits ();
      if (gatePassageCreditsAvailability)
        pendingNotifications.add (Collections.singletonMap
          (SimQueueWithGateSimpleEventType.GATE_OPEN, new SimQueueGateEvent (this, time, gac)));
      else
        pendingNotifications.add (Collections.singletonMap
          (SimQueueWithGateSimpleEventType.GATE_CLOSED, new SimQueueGateEvent (this, time, gac)));
    }
    this.previousGatePassageCreditsAvailability = gatePassageCreditsAvailability;
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
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and opens the gate (limitless).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.gatePassageCredits = Integer.MAX_VALUE;
    // Every SimQueueWithGate must have infinite gpcs upon construction and after reset.
    this.previousGatePassageCreditsAvailability = true;
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
  }

  /** Makes the job depart from the job queue if the gate is currently open
   * 
   * @see #getGatePassageCredits
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (this.gatePassageCredits == 0)
      return;
    if (this.gatePassageCredits < Integer.MAX_VALUE)
      this.gatePassageCredits--;
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
  // RECOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all relevant listeners that the gate is (re)open(ed).
   * 
   * @see SimQueueWithGateListener#notifyNewGateStatus
   * 
   */
  private void fireGateOpen (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueGateEvent))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ((SimQueueGateEvent) event).getGatePassageCredits () == 0)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueWithGateListener)
        ((SimQueueWithGateListener) l).notifyNewGateStatus (time, this, true);
  }
  
  /** Notifies all relevant listeners that the gate is closed.
   * 
   * @see SimQueueWithGateListener#notifyNewGateStatus
   * 
   */
  private void fireGateClosed (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimQueueGateEvent))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time || ((SimQueueGateEvent) event).getGatePassageCredits () > 0)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    if (queue == null || queue != this)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimQueueWithGateListener)
        ((SimQueueWithGateListener) l).notifyNewGateStatus (time, this, false);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
