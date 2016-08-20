package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link FCFS_c}.
 *
 */
public class SimQueuePredictor_FCFS_c
extends SimQueuePredictor_FCFS
{
  
  public SimQueuePredictor_FCFS_c (final int c)
  {
    super (false, 0, true, c);
  }

  @Override
  public String toString ()
  {
    return "Predictor[FCFS_" + this.c + "]";
  }

}