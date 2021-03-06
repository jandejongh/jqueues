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

import java.util.List;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.composite.tandem.Tandem;
import org.javades.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite_LocalStart;
import org.javades.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link Tandem}.
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
public class SimQueuePredictor_Tandem<Q extends Tandem>
extends AbstractSimQueuePredictor_Composite_LocalStart<Q>
implements SimQueuePredictor<Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public SimQueuePredictor_Tandem (final List<AbstractSimQueuePredictor> subQueuePredictors)
  {
    super (subQueuePredictors);
  }

  @Override
  public String toString ()
  {
    return "Predictor[Tandem[?]]";
  }

  @Override
  public boolean isStartArmed (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return true;
  }

}