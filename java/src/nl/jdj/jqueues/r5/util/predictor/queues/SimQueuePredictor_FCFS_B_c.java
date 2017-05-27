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