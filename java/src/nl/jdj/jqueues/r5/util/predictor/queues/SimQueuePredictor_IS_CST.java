package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS_CST;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IS_CST}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_IS_CST<J extends SimJob>
extends SimQueuePredictor_IS<J>
{
  
  public SimQueuePredictor_IS_CST (final double serviceTime)
  {
    super (true, serviceTime);
  }
  
}