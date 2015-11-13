package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IC}.
 *
 */
public class SimQueuePredictor_IC
extends SimQueuePredictor_IS
{
  
  public SimQueuePredictor_IC ()
  {
    super (true, 0.0);
  }
  
}