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
package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS_B_c;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link FCFS_B_c}.
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
public class SimQueuePredictor_FCFS_B_c
extends SimQueuePredictor_FCFS
{
  
  public SimQueuePredictor_FCFS_B_c (final int B, final int c)
  {
    super (true, B, true, c);
  }

  @Override
  public String toString ()
  {
    return "Predictor[FCFS_B_" + this.c + "[" + this.B + "]]";
  }

}