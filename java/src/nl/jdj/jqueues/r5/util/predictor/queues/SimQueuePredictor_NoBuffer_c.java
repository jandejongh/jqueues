package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link NoBuffer_c}.
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
public class SimQueuePredictor_NoBuffer_c
extends SimQueuePredictor_FCFS
{
  
  public SimQueuePredictor_NoBuffer_c (final int c)
  {
    super (true, 0, true, c);
  }

  @Override
  public String toString ()
  {
    return "Predictor[NoBuffer_" + this.c + "]";
  }

}