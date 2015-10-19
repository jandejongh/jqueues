package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link SINK} queue has unlimited waiting capacity, but does not provide
 *  any service.
 *
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * <p>
 * Obviously, the {@link SINK} queue does not schedule any events on the
 * {@link #eventList} and never invokes actions in
 * {@link #startActions} or {@link #departureActions}.
 * It does support job revocations though.
 * 
 * <p>
 * A {@link SINK} queue is <i>never</i> <code>noWaitArmed</code>,
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class SINK<J extends SimJob, Q extends SINK> extends AbstractSimQueue<J, Q>
{

  /** Creates a SINK queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public SINK (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns a new {@link SINK} object on the same {@link SimEventList}.
   * 
   * @return A new {@link SINK} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public SINK<J, Q> getCopySimQueue ()
  {
    return new SINK<> (getEventList ());
  }
  
  /** Returns <code>false</code>.
   * 
   * @return False.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return false;
  }

  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    /* EMPTY */
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

  /** Removes the job, after passing sanity checks, from the job queue {@link #jobQueue}.
   * 
   * @return True.
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (! this.jobsExecuting.isEmpty ())
      throw new IllegalStateException ();
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
   * @throws IllegalStateException Always, as this {@link SimQueue} does not allow departures.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not allow departures.
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

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }  
  
  /** Returns "SINK".
   * 
   * @return "SINK".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "SINK";
  }

}
