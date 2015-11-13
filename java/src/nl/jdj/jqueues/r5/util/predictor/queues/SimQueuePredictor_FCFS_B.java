package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link FCFS_B}.
 *
 */
public class SimQueuePredictor_FCFS_B
extends SimQueuePredictor_FCFS
{
  
  public SimQueuePredictor_FCFS_B (final int B)
  {
    super (true, B, true, 1);
  }

}