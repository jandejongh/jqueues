package nl.jdj.jqueues.r5.extensions.lastdeptime;

import nl.jdj.jqueues.r5.entity.queue.serverless.LeakyBucket;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;

/** A simple representation of a {@link SimEntityEvent} specific to {@link LeakyBucket}.
 * 
 * @see LeakyBucket
 * @see SimQueuePredictor_LeakyBucket
 * 
 */
public interface LeakyBucketSimpleEventType
extends SimQueueSimpleEventType
{

  /** Notification that the rate limitation has expired.
   * 
   */
  public static Member RATE_LIMIT_EXPIRATION = new Member ("RATE_LIMIT_EXPIRATION");
  
}
