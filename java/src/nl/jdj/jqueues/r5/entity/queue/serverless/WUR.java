package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.SUR;
import nl.jdj.jsimulation.r5.SimEventList;

/** In the {@link WUR} queue jobs wait until the arrival of the next job, at which point they depart from the waiting area.
 * 
 * <p>
 * Wait-Until-Relieved; this {@link SimQueue} is server-less.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see SUR
 * 
 */
public class WUR<J extends SimJob, Q extends WUR>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link WUR} queue with infinite buffer size given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public WUR (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE);
  }
  
  /** Returns a new {@link WUR} object on the same {@link SimEventList}.
   * 
   * @return A new {@link WUR} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public WUR<J, Q> getCopySimQueue ()
  {
    return new WUR<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "WUR".
   * 
   * @return "WUR".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "WUR";
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
  
  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   * @see #jobQueue
   * @see #arrive
   * @see #rescheduleAfterArrival
   * 
   * @throws IllegalStateException If more than one job resides (a-priori) in the waiting area upon arrival of the job.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    if (this.jobQueue.size () > 1)
      throw new IllegalStateException ();
    this.jobQueue.add (job);
  }

  /** Makes the current waiting job depart, if present.
   * 
   * @see #jobQueue
   * @see #depart
   * @see #insertJobInQueueUponArrival
   * 
   * @throws IllegalStateException If more than two jobs resides (a-priori) in the waiting area.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (this.jobQueue.size () > 2)
      throw new IllegalStateException ();
    else if (this.jobQueue.size () == 2)
      depart (time, this.jobQueue.get (0));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job, after passing sanity checks, from the job queue {@link #jobQueue}.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (! this.jobsInServiceArea.isEmpty ())
      throw new IllegalStateException ();
    this.jobQueue.remove (job);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (! this.jobQueue.contains (departingJob))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (departingJob))
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
