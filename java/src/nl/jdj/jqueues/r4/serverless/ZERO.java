package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link ZERO} queue induces no waiting, after which jobs depart without service.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * <p>
 * A {@link ZERO} queue is <i>always</i> <code>noWaitArmed</code>,
 * see {@link SimQueue#isNoWaitArmed} and its final implementation in this class {@link #isNoWaitArmed}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see DROP
 * @see SINK
 * @see DELAY
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
  
  /** Returns a new {@link ZERO} object on the same {@link SimEventList}.
   * 
   * @return A new {@link ZERO} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public ZERO<J, Q> getCopySimQueue ()
  {
    return new ZERO<> (getEventList ());
  }
  
  /** Returns <code>true</code>.
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
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Removes the job from the job queue, resets the job's queue and fires a notification of the departure.
   * 
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    this.jobQueue.remove (job);
    job.setQueue (null);
    fireDeparture (time, job);
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

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    throw new IllegalStateException ();
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
  
  /** Returns "ZERO".
   * 
   * @return "ZERO".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "ZERO";
  }

}
