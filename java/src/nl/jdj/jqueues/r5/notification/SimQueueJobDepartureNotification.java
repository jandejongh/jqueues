package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A departure notification.
 *
 */
public class SimQueueJobDepartureNotification
extends AbstractSimQueueJobNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** An departure notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * @param job   The job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue or job is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueJobDepartureNotification (final SimQueue queue, final SimJob job)
  {
    super (queue, job);
    setName ("@" + getTime () + ": Dep[" + job + "]@" + queue);
  }

}
