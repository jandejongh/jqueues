package nl.jdj.jqueues.r4.util.schedule;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

/** A schedule of the revocation of a job at a queue.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class JobQueueVisitRevocationSchedule<J extends SimJob, Q extends SimQueue>
extends QueueExternalEvent<J, Q>
{
  
  /** Whether to request interruption of service (if applicable).
   * 
   */
  public final boolean interruptService;

  /** Creates a job-revocation schedule.
   * 
   * @param job                     The job to revoke.
   * @param queue                   The queue at which the job is to be revoked.
   * @param scheduledRevocationTime The scheduled revocation time.
   * @param interruptService        Whether to request interruption of service (if applicable).
   * 
   * @throws IllegalArgumentException If the job or the queue is <code>null</code>.
   * 
   */
  public JobQueueVisitRevocationSchedule
  (final J job, final Q queue, final double scheduledRevocationTime, final boolean interruptService)
  {
    super (scheduledRevocationTime, queue, job);
    this.interruptService = interruptService;
  }
  
  /**  Schedules the revocation.
   * 
   * @see SimQueue#revoke
   * 
   */
  @Override
  public final void schedule (final SimEventList eventList)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    eventList.schedule (JobQueueVisitRevocationSchedule.this.time, (SimEventAction) (final SimEvent event) ->
    {
      JobQueueVisitRevocationSchedule.this.queue.revoke (JobQueueVisitRevocationSchedule.this.job,
        JobQueueVisitRevocationSchedule.this.time,
        JobQueueVisitRevocationSchedule.this.interruptService);
    });
  }

}
