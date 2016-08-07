package nl.jdj.jqueues.r5.entity.queue.serverless;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGate;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGateListener;
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
  
  /** Creates a {@link GATE} queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public GATE (final SimEventList eventList)
  {
    super (eventList);
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
  // NoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code> if the gate is not closed ({@link #getGatePassageCredits} {@code > 0}).
   * 
   * @return True if the gate is not closed.
   * 
   * @see #setGatePassageCredits
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return this.gatePassageCredits > 0;
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
   * @see #jobQueue
   * @see #depart
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * 
   */
  @Override
  public final void setGatePassageCredits (final double time, final int gatePassageCredits)
  {
    update (time);
    if (! clearAndUnlockPendingNotificationsIfLocked ())
      throw new IllegalStateException ();
    if (gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    this.gatePassageCredits = gatePassageCredits;
    while (this.gatePassageCredits > 0 && ! this.jobQueue.isEmpty ())
    {
      depart (time, getFirstJobInWaitingArea ());
      if (this.gatePassageCredits < Integer.MAX_VALUE)
        this.gatePassageCredits--;
    }
    fireAndLockPendingNotifications ();
  }
  
  // Every SimQueueWithGate must have infinite gpcs upon construction and after reset.
  private boolean previousGatePassageCreditsAvailability = true;
  
  /** The pre-notification hook for gate-passage credits.
   * 
   * @see SimQueueWithGateSimpleEventType#GATE_CLOSED
   * @see SimQueueWithGateSimpleEventType#GATE_OPEN
   * 
   */
  private void gatePassageCreditsPreNotificationHook (final List<Map<SimEntitySimpleEventType.Member, J>> pendingNotifications)
  {
    if (pendingNotifications == null)
      throw new IllegalArgumentException ();
    for (final Map<SimEntitySimpleEventType.Member, J> entry : pendingNotifications)
    {
      final SimEntitySimpleEventType.Member notificationType = entry.keySet ().iterator ().next ();
      if (notificationType == SimQueueWithGateSimpleEventType.GATE_CLOSED
      ||  notificationType == SimQueueWithGateSimpleEventType.GATE_OPEN)
        throw new IllegalArgumentException ();
    }
    final boolean gatePassageCreditsAvailability = getGatePassageCredits () > 0;
    if (gatePassageCreditsAvailability != this.previousGatePassageCreditsAvailability )
    {
      if (gatePassageCreditsAvailability)
        pendingNotifications.add (Collections.singletonMap (SimQueueWithGateSimpleEventType.GATE_OPEN, null));
      else
        pendingNotifications.add (Collections.singletonMap (SimQueueWithGateSimpleEventType.GATE_CLOSED, null));
    }
    this.previousGatePassageCreditsAvailability = gatePassageCreditsAvailability;
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
  
  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
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
  
  /** Removes the job from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    this.jobQueue.remove (job);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Removes the job from the {@link #jobQueue}.
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
  private void fireGateOpen (final SimJob job)
  {
    final double time = getLastUpdateTime ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueWithGateListener)
        ((SimQueueWithGateListener) l).notifyNewGateStatus (time, (Q) this, true);
  }
  
  /** Notifies all relevant listeners that the gate is closed.
   * 
   * @see SimQueueWithGateListener#notifyNewGateStatus
   * 
   */
  private void fireGateClosed (final SimJob job)
  {
    final double time = getLastUpdateTime ();
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueWithGateListener)
        ((SimQueueWithGateListener) l).notifyNewGateStatus (time, (Q) this, false);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
