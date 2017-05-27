package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IC}.
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
public class SimQueuePredictor_IC
extends SimQueuePredictor_IS
{
  
  public SimQueuePredictor_IC ()
  {
    super (true, 0.0);
  }
  
  @Override
  public String toString ()
  {
    return "Predictor[IC]";
  }

}