package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.extensions.gate.SimQueueWithGate;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.SimEntityListener;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.extensions.gate.SimQueueWithGateListener;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link GATE} queue lets jobs depart without service conditionally ("gate is open") or lets them wait ("gate is closed").
 * 
 * <p>
 * By default, the gate is open without limit on the number of jobs passing.
 * It can be closed ({@link #closeGate}),
 * opened limitless ({@link #openGate(double)}),
 * or opened with a limit on the number of jobs to pass before closing ({@link #openGate(double,int)}).
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
    return this.numberOfPassages > 0;
  }

  private int numberOfPassages = Integer.MAX_VALUE;
  
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
   * If allowed by the <code>numberOfPassages</code> parameter, some waiting jobs will depart.
   * 
   * @param time The current time.
   * @param numberOfPassages The (remaining) number of passages to allow (will override, not add to, any previous value);
   *                         {@link Integer#MAX_VALUE} is treated as infinity.
   * 
   * @see #openGate(double)
   * @see #closeGate
   * 
   */
  @Override
  public final void openGate (final double time, final int numberOfPassages)
  {
    update (time);
    if (numberOfPassages < 0)
      throw new IllegalArgumentException ();
    final int oldNumberOfPassages = this.numberOfPassages;
    final Set<J> jobsReleased = new LinkedHashSet<>  ();
    this.numberOfPassages = numberOfPassages;
    while (this.numberOfPassages > 0 && ! this.jobQueue.isEmpty ())
    {
      jobsReleased.add (jobQueue.remove (0));
      if (this.numberOfPassages < Integer.MAX_VALUE)
        this.numberOfPassages--;
    }
    for (final J job : jobsReleased)
      job.setQueue (null);
    for (final J job : jobsReleased)
      fireDeparture (time, job, (Q) this);
    fireNewNoWaitArmed (time, isNoWaitArmed ());
    fireNewGateStatus (time, oldNumberOfPassages, this.numberOfPassages);
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
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
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
    this.numberOfPassages = Integer.MAX_VALUE;
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
   * If needed, this method updates the number of remaining passages allowed.
   * If zero, changes the internal gate state to closed.
   * 
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (this.numberOfPassages == 0)
      return;
    if (this.numberOfPassages < Integer.MAX_VALUE)
      this.numberOfPassages--;
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

  /** Removes the jobs from the {@link #jobQueue}.
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
   * @param time                The current time.
   * @param oldNumberOfPassages The old number of passages (last reported or implicit).
   * @param newNumberOfPassages The new number of passages.
   *
   * @see SimQueueGateListener#notifyNewGateStatus 
   * 
   */
  protected final void fireNewGateStatus (final double time, final int oldNumberOfPassages, final int newNumberOfPassages)
  {
    if (oldNumberOfPassages > 0 && newNumberOfPassages == 0)
      for (SimEntityListener<J, Q> l : getSimEntityListeners ())
        if (l instanceof SimQueueWithGateListener)
          ((SimQueueWithGateListener) l).notifyNewGateStatus (time, (Q) this, false);
    if (oldNumberOfPassages == 0 && newNumberOfPassages > 0)
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
