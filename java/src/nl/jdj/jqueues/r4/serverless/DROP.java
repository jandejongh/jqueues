package nl.jdj.jqueues.r4.serverless;

import nl.jdj.jqueues.r4.AbstractSimQueue;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link DROP} queue drops all jobs upon arrival.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class DROP<J extends SimJob, Q extends DROP> extends AbstractSimQueue<J, Q>
{

  /** Creates a DROP queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public DROP (final SimEventList eventList)
  {
    super (eventList);
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

  /** Does nothing (effectively forcing the job to be dropped).
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not allow revocations.
   * 
   */
  @Override
  protected final boolean removeJobFromQueueUponRevokation (final J job, final double time, final boolean interruptService)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this {@link SimQueue} does not allow revocations.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    throw new IllegalStateException ();
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
  
  /** Returns "DROP".
   * 
   * @return "DROP".
   * 
   */
  @Override
  public String toString ()
  {
    return "DROP";
  }

}
