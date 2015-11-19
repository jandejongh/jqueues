package nl.jdj.jqueues.r5.entity.queue.serverless;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGate;
import nl.jdj.jqueues.r5.extensions.gate.SimQueueWithGateListener;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link GATE} queue lets jobs depart without service conditionally ("gate is open") or lets them wait ("gate is closed").
 * 
 * <p>
 * The gate status is a {@link SimQueue} <i>state variable</i>, i.e., it can be changed from an event list being run.
 * 
 * <p>
 * By default, the gate is open without limit on the number of jobs passing.
 * It can be closed ({@link #closeGate}),
 * opened limitless ({@link #openGate(double)}),
 * or opened with a limit on the number of jobs to pass before closing ({@link #openGate(double,int)}).
 * 
 * <p>
 * The state of the gate can in fact be represented by a single non-negative number, the <i>gate passage credits</i>.
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
  
  /** Creates a GATE queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public GATE (final SimEventList eventList)
  {
    super (eventList);
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
  public final String toStringDefault ()
  {
    return "GATE";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code> if the gate is not closed.
   * 
   * @return True if the gate is not closed.
   * 
   * @see #openGate(double)
   * @see #openGate(double, int)
   * @see #closeGate
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return this.gatePassageCredits > 0;
  }

  private int gatePassageCredits = Integer.MAX_VALUE;

  /** Returns the remaining gate-passage credits, i.e., the number of jobs still allowed to pass before the gate closes.
   * 
   * @return The remaining gate-passage credits, i.e., the number of jobs still allowed to pass before the gate closes.
   * 
   */
  @Override
  public int getGatePassageCredits ()
  {
    return this.gatePassageCredits;
  }
  
  /** Opens the gate without limits on the number of jobs allowed to pass.
   * 
   * <p>
   * All waiting jobs depart.
   * 
   * @param time The current time.
   * 
   * @see #openGate(double, int)
   * @see #closeGate
   * 
   */
  @Override
  public final void openGate (final double time)
  {
    update (time);
    openGate (time, Integer.MAX_VALUE);
  }
  
  /** Opens the gate with a limit on the number of jobs allowed to pass.
   * 
   * <p>
   * If allowed by the <code>gatePassageCredits</code> parameter, some waiting jobs will depart.
   * 
   * @param time               The current time.
   * @param gatePassageCredits The (remaining) number of passages to allow (will override, not add to, any previous value);
   *                           {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @see #openGate(double)
   * @see #closeGate
   * 
   */
  @Override
  public final void openGate (final double time, final int gatePassageCredits)
  {
    update (time);
    if (gatePassageCredits < 0)
      throw new IllegalArgumentException ();
    final int oldGatePassageCredits = this.gatePassageCredits;
    final Set<J> jobsReleased = new LinkedHashSet<>  ();
    this.gatePassageCredits = gatePassageCredits;
    while (this.gatePassageCredits > 0 && ! this.jobQueue.isEmpty ())
    {
      jobsReleased.add (jobQueue.remove (0));
      if (this.gatePassageCredits < Integer.MAX_VALUE)
        this.gatePassageCredits--;
    }
    for (final J job : jobsReleased)
      job.setQueue (null);
    for (final J job : jobsReleased)
      fireDeparture (time, job, (Q) this);
    fireNewNoWaitArmed (time, isNoWaitArmed ());
    fireNewGateStatus (time, oldGatePassageCredits);
  }
  
  /** Closes the gate.
   * 
   * @param time The current time.
   * 
   * @see #openGate(double)
   * @see #openGate(double, int)
   * 
   */
  @Override
  public final void closeGate (final double time)
  {
    openGate (time, 0);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and opens the gate (limitless).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.gatePassageCredits = Integer.MAX_VALUE;
  }  
  
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

  /** Removes the job from the job queue if the gate is currently open, resets the job's queue
   * and fires a notification of the departure.
   * 
   * <p>
   * If needed, this method updates the gate-passage credits.
   * 
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (this.gatePassageCredits == 0)
      return;
    if (this.gatePassageCredits < Integer.MAX_VALUE)
      this.gatePassageCredits--;
    this.jobQueue.remove (job);
    job.setQueue (null);
    fireDeparture (time, job, (Q) this);
    fireNewNoWaitArmed (time, isNoWaitArmed ());
  }

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

  /** Removes the job from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    this.jobQueue.remove (job);
    return true;
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all gate listeners of a gate status change, if needed.
   *
   * @param time                  The current time.
   * @param oldGatePassageCredits The old number of gate-passage credits (last reported or implicit).
   *
   * @see SimQueueWithGateListener#notifyNewGateStatus
   * 
   */
  protected final void fireNewGateStatus (final double time, final int oldGatePassageCredits)
  {
    if (oldGatePassageCredits > 0 && this.gatePassageCredits == 0)
      for (SimEntityListener<J, Q> l : getSimEntityListeners ())
        if (l instanceof SimQueueWithGateListener)
          ((SimQueueWithGateListener) l).notifyNewGateStatus (time, (Q) this, false);
    if (oldGatePassageCredits == 0 && this.gatePassageCredits > 0)
      for (SimEntityListener<J, Q> l : getSimEntityListeners ())
        if (l instanceof SimQueueWithGateListener)
          ((SimQueueWithGateListener) l).notifyNewGateStatus (time, (Q) this, true);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
