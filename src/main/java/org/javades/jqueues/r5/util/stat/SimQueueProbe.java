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
package org.javades.jqueues.r5.util.stat;

import org.javades.jqueues.r5.entity.jq.queue.SimQueue;

/** Object capable of obtaining a value (<code>double</code>) from a {@link SimQueue}.
 * 
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
public interface SimQueueProbe<Q extends SimQueue>
{
  
  /** Gets the value from given queue.
   * 
   * @param queue The queue.
   * 
   * @return The value.
   * 
   * @throws IllegalArgumentException If the argument is <code>null</code> or of invalid type for this probe.
   * 
   */
  public double get (Q queue);
  
}
