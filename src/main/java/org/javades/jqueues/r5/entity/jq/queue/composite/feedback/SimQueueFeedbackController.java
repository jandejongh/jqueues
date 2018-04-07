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
package org.javades.jqueues.r5.entity.jq.queue.composite.feedback;

import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** An object capable of controlling feedback on a {@link SimQueue}.
 *
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 * 
 * @see AbstractFeedbackSimQueue
 * @see FB
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
@FunctionalInterface
public interface SimQueueFeedbackController<J extends SimJob, DQ extends SimQueue>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FEEDBACK CONTROLLER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns true if the delegate job is to be fed back into the queue.
   * 
   * @param time          The current time, i.e., the (latest) departure time of the job at its queue.
   * @param delegateQueue The "delegate" queue (i.e., the queue just left by the delegate job).
   * @param realJob       The real job, non-<code>null</code>.
   * @param visits        The number of visits of the delegate job to the (delegate) queue thus far (since the last reset).
   * 
   * @return True if the delegate job is to be fed back into the (delegate) queue.
   * 
   */
  boolean feedback (double time, DQ delegateQueue, J realJob, int visits);
  
  /** Resets the controller.
   * 
   * <p>
   * The default implementation does nothing.
   * 
   */
  default void resetFeedbackController ()
  {
    /* EMTPY */
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
