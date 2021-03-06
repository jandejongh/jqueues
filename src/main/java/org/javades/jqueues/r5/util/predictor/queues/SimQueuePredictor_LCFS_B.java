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
package org.javades.jqueues.r5.util.predictor.queues;

import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS_B;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link LCFS_B}.
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
public class SimQueuePredictor_LCFS_B
extends SimQueuePredictor_FCFS
{
  
  public SimQueuePredictor_LCFS_B (final int B)
  {
    super (true, B, true, 1, true);
  }

  @Override
  public String toString ()
  {
    return "Predictor[LCFS_B[" + this.B + "]]";
  }

}