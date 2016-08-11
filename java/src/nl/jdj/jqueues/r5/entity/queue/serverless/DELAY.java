package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

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
  
  /** Creates a {@link DELAY} queue given an event list and (fixed) wait time.
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
  public String toStringDefault ()
  {
    return "DELAY[" + getWaitTime () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WAIT TIME
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
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** If needed, schedules a departure event for the arrived job respecting the fixed wait time of this queue;
   *  otherwise (zero wait time), makes the job depart immediately.
   * 
   * <p>
   * If the wait time is {@link Double#POSITIVE_INFINITY}, no departure events are scheduled, in other words,
   * this queue does not schedule departures at infinity.
   * 
   * @see #getWaitTime
   * @see #scheduleDepartureEvent
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    final double waitTime = getWaitTime ();
    if (waitTime < 0)
      throw new IllegalStateException ();
    else if (waitTime == 0)
      depart (time, job);
    else if (! Double.isInfinite (waitTime))
      scheduleDepartureEvent (time + waitTime, job);
    else
      // waitTime is positive infinity; do not schedule a departure event!
      ;
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
  
  /** Cancels the departure of the job (if present) and removes it,
   *  after passing sanity checks, from the job queue {@link #jobQueue}.
   * 
   * @see #cancelDepartureEvent
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (! this.jobsInServiceArea.isEmpty ())
      throw new IllegalStateException ();
    if (! getDepartureEvents (job).isEmpty ())
      cancelDepartureEvent (job);
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
  
  /** Removes the job, after several sanity checks, from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (departingJob == null || ! this.jobQueue.contains (departingJob))
      throw new IllegalArgumentException ();
    if (! this.jobsInServiceArea.isEmpty ())
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
