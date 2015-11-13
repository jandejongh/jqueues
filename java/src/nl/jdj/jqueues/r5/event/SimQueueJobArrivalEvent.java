package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** An arrival {@link SimEvent} of a job at a queue.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueJobArrivalEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent<J, Q>
{
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static
  <J extends SimJob, Q extends SimQueue>
  SimEventAction<J>
  createAction (final J job, final Q queue)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    return (final SimEvent<J> event) ->
    {
      queue.arrive (job, event.getTime ());
    };
  }
  
  /** Creates a job-arrival event at a specific queue.
   * 
   * @param job         The job that arrives.
   * @param queue       The queue at which the job arrives.
   * @param arrivalTime The scheduled arrival time.
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>.
   * 
   * @see SimQueue#arrive
   * 
   */
  public SimQueueJobArrivalEvent
  (final J job, final Q queue, final double arrivalTime)
  {
    super ("Arr[" + job + "]@" + queue, arrivalTime, queue, job, createAction (job, queue));
  }
  
}
