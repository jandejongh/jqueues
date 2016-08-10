package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS_B_c;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link FCFS_B_c}.
 *
 */
public class SimQueuePredictor_FCFS_B_c
extends SimQueuePredictor_FCFS
{
  
  public SimQueuePredictor_FCFS_B_c (final int B, final int c)
  {
    super (true, B, true, c);
  }

}