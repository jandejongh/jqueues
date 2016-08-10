package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** A start {@link SimEvent} of a job at a queue.
 * 
 * <p>
 * Do not <i>ever</i> schedule this yourself unless for your own implementation;
 * it is for private use by {@link SimQueue} implementations.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueJobStartEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a job-start event at a specific queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
   * 
   * @param job       The job that is to start.
   * @param queue     The queue at which the job starts.
   * @param startTime The scheduled start time.
   * @param action    The {@link SimEventAction} to take; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>.
   * 
   */
  public SimQueueJobStartEvent
  (final J job, final Q queue, final double startTime, final SimEventAction<J> action)
  {
    super ("Start[" + job + "]@" + queue, startTime, queue, job, action);
    if (action == null)
      throw new IllegalArgumentException ();
  }
  
  /** Throws an {@link UnsupportedOperationException}.
   * 
   * <p>
   * A {@link SimQueueJobStartEvent} is a queue-internal event.
   * 
   * @throws UnsupportedOperationException Always.
   * 
   */
  @Override
  public final SimEntityEvent<J, Q> copyForQueue (final Q destQueue)
  {
    throw new UnsupportedOperationException ();
  }
  
}
