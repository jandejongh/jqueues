package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link DELAY} queue induces a fixed waiting delay, after which jobs depart without service.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see ZERO
 * 
 */
public class DELAY<J extends SimJob, Q extends DELAY>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a DELAY queue given an event list and (fixed) wait time.
   *
   * @param eventList The event list to use.
   * @param waitTime  The (fixed) wait time for all jobs.
   *
   * @throws IllegalArgumentException If <code>waitTime</code> is strictly negative.
   * 
   */
  public DELAY (final SimEventList eventList, final double waitTime)
  {
    super (eventList);
    if (waitTime < 0)
      throw new IllegalArgumentException ();
    this.waitTime = waitTime;
  }
  
  /** Returns a new {@link DELAY} object on the same {@link SimEventList} with the same wait time.
   * 
   * @return A new {@link DELAY} object on the same {@link SimEventList} with the same wait time.
   * 
   * @see #getEventList
   * @see #getWaitTime
   * 
   */
  @Override
  public DELAY<J, Q> getCopySimQueue ()
  {
    return new DELAY<> (getEventList (), getWaitTime ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "DELAY[wait time]".
   * 
   * @return "DELAY[wait time]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "DELAY[" + getWaitTime () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROPERTIES:
  //   - waitTime
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double waitTime;
  
  /** Returns the wait time for all jobs.
   * 
   * @return The wait time for all jobs, non-negative.
   * 
   */
  public final double getWaitTime ()
  {
    return this.waitTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code> if and only if the wait-time for all jobs is zero.
   * 
   * @return True if and only if the wait-time for all jobs is zero.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getWaitTime () == 0;
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** If needed, schedules a departure event for the arrived job respecting the fixed wait time of this queue;
   * otherwise (zero wait time), removes the job from the job queue, set its queue to <code>null</code> and notifies
   * listeners of the departure.
   * 
   * @see #getWaitTime
   * @see #scheduleDepartureEvent
   * @see #jobQueue
   * @see SimJob#setQueue
   * @see #fireDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    final double waitTime = getWaitTime ();
    if (waitTime > 0)
      scheduleDepartureEvent (time + waitTime, job);
    else
    {
      this.jobQueue.remove (job);
      job.setQueue (null);
      fireDeparture (time, job, (Q) this);
    }
  }

  /** Invokes {@link #removeJobFromQueueUponRevokation}, requesting <code>interruptService</code>.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time, true);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Cancels the departure of the job and removes it, after passing sanity checks, from the job queue {@link #jobQueue}.
   * 
   * @return True.
   * 
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (! this.jobsExecuting.isEmpty ())
      throw new IllegalStateException ();
    cancelDepartureEvent (job);
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

  /** Removes the job, after several sanity checks, from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (departingJob == null || ! this.jobQueue.contains (departingJob))
      throw new IllegalArgumentException ();
    if (! this.jobsExecuting.isEmpty ())
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
