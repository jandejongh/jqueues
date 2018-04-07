/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.extensions.ratelimit;

import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.entity.jq.queue.serverless.DLIMIT;
import org.javades.jqueues.r5.entity.jq.queue.serverless.LeakyBucket;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_DLIMIT;
import org.javades.jqueues.r5.util.predictor.queues.SimQueuePredictor_LeakyBucket;

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
