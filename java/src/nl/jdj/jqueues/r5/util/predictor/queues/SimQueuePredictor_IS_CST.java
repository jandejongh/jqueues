package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS_CST;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IS_CST}.
 *
 */
public class SimQueuePredictor_IS_CST
extends SimQueuePredictor_IS
{
  
  public SimQueuePredictor_IS_CST (final double serviceTime)
  {
    super (true, serviceTime);
  }
  
}