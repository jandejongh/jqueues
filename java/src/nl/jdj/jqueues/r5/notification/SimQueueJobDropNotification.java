package nl.jdj.jqueues.r5.notification;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A drop notification.
 *
 */
public class SimQueueJobDropNotification
extends AbstractSimQueueJobNotification
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A drop notification with time taken from the event list of the queue.
   * 
   * @param queue The queue, non-{@code null}.
   * @param job   The job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue or job is {@code null} or the queue has {@code null} event list.
   * 
   */
  public SimQueueJobDropNotification (final SimQueue queue, final SimJob job)
  {
    super (queue, job);
    setName ("@" + getTime () + ": Drop[" + job + "]@" + queue);
  }

}
