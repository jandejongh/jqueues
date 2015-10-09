package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link DELAY} queue induces a fixed waiting delay, after which jobs depart without service.
 * 
 * <p>A {@link DELAY} queue is <i>only</i> <code>noWaitArmed</code> if the fixed delay time is zero,
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public final class DELAY<J extends SimJob, Q extends DELAY> extends AbstractSimQueue<J, Q>
{

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
  
  private final double waitTime;
  
  /** Returns the wait time for all jobs.
   * 
   * @return The wait time for all jobs, non-negative.
   * 
   */
  public /* final */ double getWaitTime ()
  {
    return this.waitTime;
  }
  
  /** Returns <code>true</code> if and only if the wait-time for all jobs is zero.
   * 
   * {@inheritDoc}
   * 
   * @return True if and only if the wait-time for all jobs is zero.
   * 
   */
  @Override
  public /* final */ boolean isNoWaitArmed ()
  {
    return getWaitTime () == 0;
  }

  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Schedules a departure event for the arrived job respecting the fixed wait time of this queue.
   * 
   * {@inheritDoc}
   * 
   * @see #scheduleDepartureEvent
   * @see #getWaitTime
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterArrival (final J job, final double time)
  {
    scheduleDepartureEvent (time + getWaitTime (), job);
  }

  /** Invokes {@link #removeJobFromQueueUponRevokation}, requesting <code>interruptService</code>.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time, true);
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Cancels the departure of the job and removes it, after passing sanity checks, from the job queue {@link #jobQueue}.
   * 
   * {@inheritDoc}
   * 
   * @return True.
   * 
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected /* final */ boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
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
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Removes the job, after several sanity checks, from the {@link #jobQueue}.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (departingJob == null || ! this.jobQueue.contains (departingJob))
      throw new IllegalArgumentException ();
    if (! this.jobsExecuting.isEmpty ())
      throw new IllegalStateException ();
    this.jobQueue.remove (departingJob);
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected /* final */ void rescheduleForNewServerAccessCredits (final double time)
  {
    /* EMPTY */
  }

  /** Returns "DELAY[wait time]".
   * 
   * @return "DELAY[wait time]".
   * 
   */
  @Override
  public String toString ()
  {
    return "DELAY[" + getWaitTime () + "]";
  }

}
