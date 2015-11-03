package nl.jdj.jqueues.r4.util.schedule;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** A schedule of the arrival of a job at a queue.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class JobQueueVisitArrivalSchedule<J extends SimJob, Q extends SimQueue>
extends QueueExternalEvent<J, Q>
{
  
  /** Creates a job-arrival schedule at a specific queue.
   * 
   * @param job                  The job that arrives.
   * @param queue                The queue at which the job arrives.
   * @param arrivalTime          The scheduled arrival time.
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>.
   * 
   */
  public JobQueueVisitArrivalSchedule
  (final J job, final Q queue, final double arrivalTime)
  {
    super (arrivalTime, queue, job);
  }
  
  /** Schedules the arrival.
   * 
   * @see SimQueue#arrive
   * 
   */
  @Override
  public final void schedule (final SimEventList eventList)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    eventList.schedule (JobQueueVisitArrivalSchedule.this.time, (SimEventAction) (final SimEvent event) ->
    {
      JobQueueVisitArrivalSchedule.this.queue.arrive (JobQueueVisitArrivalSchedule.this.job,
        JobQueueVisitArrivalSchedule.this.time);
    });
  }

}
