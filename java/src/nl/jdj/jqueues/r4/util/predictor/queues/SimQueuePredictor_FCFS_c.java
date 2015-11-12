package nl.jdj.jqueues.r4.util.predictor.queues;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.nonpreemptive.FCFS_c;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link FCFS_c}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_FCFS_c<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{
  
  public SimQueuePredictor_FCFS_c (final int c)
  {
    super (false, 0, true, c);
  }

}