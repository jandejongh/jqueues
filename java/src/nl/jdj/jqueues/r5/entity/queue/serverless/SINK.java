package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link SINK} queue has unlimited waiting capacity, but does not provide
 *  any service and jobs can only leave through revocations.
 *
 * <p>
 * This {@link SimQueue} is server-less.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class SINK<J extends SimJob, Q extends SINK>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SINK} queue given an event list.
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SINK".
   * 
   * @return "SINK".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SINK";
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

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    /* EMPTY */
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
