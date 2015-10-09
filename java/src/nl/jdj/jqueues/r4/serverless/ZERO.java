package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link ZERO} queue induces no waiting, after which jobs depart without service.
 * 
 * <p>A {@link ZERO} queue is <i>always</i> <code>noWaitArmed</code>,
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class ZERO<J extends SimJob, Q extends ZERO> extends AbstractSimQueue<J, Q>
{

  /** Creates a ZERO queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public ZERO (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns <code>true</code>.
   * 
   * {@inheritDoc}
   * 
   * @return True.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return true;
  }

  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Schedules a departure event (now) for the arrived job.
   * 
   * {@inheritDoc}
   * 
   * @see #scheduleDepartureEvent
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    scheduleDepartureEvent (time, job);
  }

  /** Invokes {@link #removeJobFromQueueUponRevokation}, requesting <code>interruptService</code>.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    removeJobFromQueueUponRevokation (job, time, true);
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
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
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Removes the job, after several sanity checks, from the {@link #jobQueue}.
   * 
   * {@inheritDoc}
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
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    /* EMPTY */
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }  
  
  /** Returns "ZERO".
   * 
   * @return "ZERO".
   * 
   */
  @Override
  public String toString ()
  {
    return "ZERO";
  }

}
