package nl.jdj.jqueues.r4.util.predictor.queues;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.nonpreemptive.FCFS_B;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link FCFS_B}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_FCFS_B<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{
  
  public SimQueuePredictor_FCFS_B (final int B)
  {
    super (true, B, true, 1);
  }

}