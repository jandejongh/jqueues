package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link NoBuffer_c}.
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