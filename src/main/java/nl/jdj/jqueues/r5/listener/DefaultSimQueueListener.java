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
package nl.jdj.jqueues.r5.listener;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueListener;

/** A {@link SimQueueListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public class DefaultSimQueueListener<J extends SimJob, Q extends SimQueue>
extends DefaultSimJQListener<J, Q>
implements SimQueueListener<J, Q>
{

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
  }
  
  /** Does nothing.
   * 
   */
  @Override
  public void notifyNewStartArmed (final double time, final Q queue, final boolean startArmed)
  {
  }
  
}
