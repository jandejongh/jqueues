package nl.jdj.jqueues.r4.serverless;

import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link GATE} queue lets jobs depart without service conditionally ("gate is open") or lets them wait ("gate is closed").
 * 
 * <p>
 * By default, the gate is open without limit on the number of jobs passing.
 * It can be closed ({@link #closeGate}),
 * opened limitless ({@link #openGate(double)}),
 * or opened with a limit on the number of jobs to pass before closing ({@link #openGate(double,int)}).
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
public class GATE<J extends SimJob, Q extends GATE> extends AbstractSimQueue<J, Q>
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
    return this.gateState != GateState.CLOSED;
  }

  /** The gate states.
   * 
   * When CLOSED, the number of passages is irrelevant (should be zero).
   * When OPEN_LIMITED, the number of passages must be strictly positive.
   * When OPEN, the number of passages is irrelevant.
   * 
   */
  private enum GateState
  {
    CLOSED,
    OPEN_LIMITED,
    OPEN
  }
  
  private GateState gateState = GateState.OPEN;
  
  private int numberOfPassages = 0;
  
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
  public final void openGate (final double time)
  {
    update (time);
    this.gateState = GateState.OPEN;
    final Set<J> jobsReleased = new LinkedHashSet<>  ();
    jobsReleased.addAll (this.jobQueue);
    for (final J job : jobsReleased)
      job.setQueue (null);
    this.jobQueue.clear ();
    for (final J job : jobsReleased)
      fireDeparture (time, job);
    fireNewNoWaitArmed (time, isNoWaitArmed ());
  }
  
  /** Opens the gate with a limits on the number of jobs allowed to pass.
   * 
   * <p>
   * If allowed by the <code>numberOfPassages</code> parameter, some waiting jobs will depart.
   * 
   * @param time The current time.
   * @param numberOfPassages The (remaining) number of passages to allow (will override, not add to, any previous value).
   * 
   * @see #openGate(double)
   * @see #closeGate
   * 
   */
  public final void openGate (final double time, final int numberOfPassages)
  {
    update (time);
    if (numberOfPassages < 0)
      throw new IllegalArgumentException ();
    final Set<J> jobsReleased = new LinkedHashSet<>  ();
    if (numberOfPassages == 0)
    {
      this.gateState = GateState.CLOSED;
      this.numberOfPassages = 0;
    }
    else
    {
      this.gateState = GateState.OPEN_LIMITED;
      this.numberOfPassages = numberOfPassages;
      while (this.numberOfPassages > 0 && ! this.jobQueue.isEmpty ())
      {
        jobsReleased.add (jobQueue.remove (0));
        this.numberOfPassages--;
      }
      if (this.numberOfPassages == 0)
        this.gateState = GateState.CLOSED;
      for (final J job : jobsReleased)
        job.setQueue (null);
      for (final J job : jobsReleased)
        fireDeparture (time, job);
      fireNewNoWaitArmed (time, isNoWaitArmed ());
    }
  }
  
  /** Closes the gate.
   * 
   * @param time The current time.
   * 
   */
  public final void closeGate (final double time)
  {
    update (time);
    this.gateState = GateState.CLOSED;
    fireNewNoWaitArmed (time, isNoWaitArmed ());
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
  public final void reset ()
  {
    super.reset ();
    this.gateState = GateState.OPEN;
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
    if (this.gateState == GateState.CLOSED)
      return;
    if (this.gateState == GateState.OPEN_LIMITED)
    {
      if (this.numberOfPassages <= 0)
        throw new IllegalStateException ();
      this.numberOfPassages--;
      if (this.numberOfPassages == 0)
        this.gateState = GateState.CLOSED;
    }
    this.jobQueue.remove (job);
    job.setQueue (null);
    fireDeparture (time, job);
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
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
