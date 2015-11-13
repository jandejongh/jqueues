package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.NoBuffer_c;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link NoBuffer_c}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_NoBuffer_c<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{
  
  public SimQueuePredictor_NoBuffer_c (final int c)
  {
    super (true, 0, true, c);
  }

}