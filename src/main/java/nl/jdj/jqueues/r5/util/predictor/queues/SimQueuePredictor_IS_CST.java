package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IS_CST;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IS_CST}.
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
public class SimQueuePredictor_IS_CST
extends SimQueuePredictor_IS
{
  
  public SimQueuePredictor_IS_CST (final double serviceTime)
  {
    super (true, serviceTime);
  }
  
  @Override
  public String toString ()
  {
    return "Predictor[IS_CST[" + this.serviceTime + "]]";
  }

}