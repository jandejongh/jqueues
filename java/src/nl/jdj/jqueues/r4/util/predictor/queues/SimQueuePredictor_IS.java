package nl.jdj.jqueues.r4.util.predictor.queues;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.nonpreemptive.IS;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link IS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_IS<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{
  
  public SimQueuePredictor_IS ()
  {
    super (false, 0, false, 0);
  }

}