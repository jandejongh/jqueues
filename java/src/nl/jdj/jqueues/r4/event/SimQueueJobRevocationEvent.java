package nl.jdj.jqueues.r4.event;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;

/** A revocation {@link SimEvent} of a job at a queue.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class SimQueueJobRevocationEvent<J extends SimJob, Q extends SimQueue>
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
  createAction (final J job, final Q queue, final boolean interruptService)
  {
    if (job == null || queue == null)
      throw new IllegalArgumentException ();
    return new SimEventAction<J> ()
    {
      @Override
      public void action (final SimEvent<J> event)
      {
        queue.revoke (job, event.getTime (), interruptService);
      }
    };
  }
  
  /** Creates a job-revocation event at a specific queue.
   * 
   * @param job              The job that is to be revoked.
   * @param queue            The queue from which the job is to be revoked.
   * @param revocationTime   The scheduled revocation time.
   * @param interruptService Whether to request interruption of service (if applicable).
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>.
   * 
   * @see SimQueue#revoke
   * 
   */
  public SimQueueJobRevocationEvent
  (final J job, final Q queue, final double revocationTime, final boolean interruptService)
  {
    super ("Rev[" + job + "]@" + queue, revocationTime, queue, job, createAction (job, queue, interruptService));
    this.interruptService = interruptService;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INTERRUPT SERVICE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final boolean interruptService;

  /** Returns whether to request interruption of service (if applicable).
   * 
   * @return Whether to request interruption of service (if applicable).
   * 
   */
  public final boolean isInterruptService ()
  {
    return this.interruptService;
  }
  
}
