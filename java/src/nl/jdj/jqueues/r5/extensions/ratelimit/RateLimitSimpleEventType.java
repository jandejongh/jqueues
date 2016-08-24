package nl.jdj.jqueues.r5.extensions.ratelimit;

import nl.jdj.jqueues.r5.entity.queue.serverless.LIMIT;
import nl.jdj.jqueues.r5.entity.queue.serverless.LeakyBucket;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LIMIT;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;

/** A simple representation of a {@link SimEntityEvent} specific to {@link LIMIT} and {@link LeakyBucket}.
 * 
 * @see LIMIT
 * @see SimQueuePredictor_LIMIT
 * @see LeakyBucket
 * @see SimQueuePredictor_LeakyBucket
 * 
 */
public interface RateLimitSimpleEventType
extends SimQueueSimpleEventType
{

  /** Notification that the rate limitation (period) has expired.
   * 
   */
  public static Member RATE_LIMIT_EXPIRATION = new Member ("RATE_LIMIT_EXPIRATION");
  
}
