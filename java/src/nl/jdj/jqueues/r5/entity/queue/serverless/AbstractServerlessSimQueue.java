package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.AbstractSimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueue} that does not provide service to {@link SimJob}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractServerlessSimQueue<J extends SimJob, Q extends AbstractServerlessSimQueue>
extends AbstractSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a server-less queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public AbstractServerlessSimQueue (final SimEventList eventList)
  {
    super (eventList);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // StartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Returns <code>false</code> since jobs cannot start.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing, since we are server-less.
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    /* EMPTY */
  }

}
