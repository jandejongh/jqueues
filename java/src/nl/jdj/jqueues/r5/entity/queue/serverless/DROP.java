package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link DROP} queue drops all jobs upon arrival.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class DROP<J extends SimJob, Q extends DROP>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link DROP} queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public DROP (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns a new {@link DROP} object on the same {@link SimEventList}.
   * 
   * @return A new {@link DROP} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public DROP<J, Q> getCopySimQueue ()
  {
    return new DROP<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "DROP".
   * 
   * @return "DROP".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DROP";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Adds the job to the {@link #jobQueue} (as single member).
   * 
   * @throws IllegalStateException If the job-queue is non-empty upon entry.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (! this.jobQueue.isEmpty ())
      throw new IllegalStateException ();
    this.jobQueue.add (job);
  }

  /** Drops the job.
   * 
   * @see #drop
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    drop (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from the {@link #jobQueue}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    this.jobQueue.remove (job);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as this method is not expected to be invoked.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
