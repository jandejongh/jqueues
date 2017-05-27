package nl.jdj.jqueues.r5.extensions.ratelimit;

import nl.jdj.jqueues.r5.entity.jq.queue.serverless.DLIMIT;
import nl.jdj.jqueues.r5.entity.jq.queue.serverless.LeakyBucket;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;

/** A simple representation of a {@link SimJQEvent} specific to {@link DLIMIT} and {@link LeakyBucket}.
 * 
 * @see DLIMIT
 * @see SimQueuePredictor_DLIMIT
 * @see LeakyBucket
 * @see SimQueuePredictor_LeakyBucket
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
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
