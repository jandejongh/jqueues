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
  
}
