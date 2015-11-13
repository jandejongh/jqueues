package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IC}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_IC<J extends SimJob>
extends SimQueuePredictor_IS<J>
{
  
  public SimQueuePredictor_IC ()
  {
    super (true, 0.0);
  }
  
}