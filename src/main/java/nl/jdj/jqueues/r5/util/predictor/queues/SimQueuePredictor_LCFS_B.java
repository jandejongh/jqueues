package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.LCFS_B;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

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