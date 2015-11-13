package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** An departure {@link SimEvent} of a job at a queue.
 * 
 * <p>
 * Do not <i>ever</i> schedule this yourself unless for your own implementation;
 * it is for private use by {@link SimQueue} implementations.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueJobDepartureEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a job-departure event at a specific queue.
   * 
   * <p>
   * Do not schedule this yourself; it is for private use by {@link SimQueue} implementations.
   * 
   * @param job           The job that is to depart.
   * @param queue         The queue at which the job departs.
   * @param departureTime The scheduled departure time.
   * @param action        The {@link SimEventAction} to take; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job, queue or action is <code>null</code>.
   * 
   */
  public SimQueueJobDepartureEvent
  (final J job, final Q queue, final double departureTime, final SimEventAction<J> action)
  {
    super ("Dep[" + job + "]@" + queue, departureTime, queue, job, action);
    if (action == null)
      throw new IllegalArgumentException ();
  }
  
}
