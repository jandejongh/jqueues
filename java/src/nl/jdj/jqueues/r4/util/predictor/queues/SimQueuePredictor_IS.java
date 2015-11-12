package nl.jdj.jqueues.r4.util.predictor.queues;

import java.util.function.ToDoubleBiFunction;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.nonpreemptive.IS;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link IS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_IS<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{
  
  final boolean overrideServiceTime;
  
  final double serviceTime;

  @Override
  protected SimQueueState<J, SimQueue> createQueueState (SimQueue queue, boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    if (this.overrideServiceTime)
      queueState.setServiceTimeProvider (this.serviceTimeProvider);
    return queueState;
  }
  
  protected SimQueuePredictor_IS (final boolean overrideServiceTime, final double serviceTime)
  {
    super (false, 0, false, 0);
    this.overrideServiceTime = overrideServiceTime;
    this.serviceTime = serviceTime;
  }
  
  public SimQueuePredictor_IS ()
  {
    this (false, Double.NaN);
  }
  
  final ToDoubleBiFunction<SimQueue, J> serviceTimeProvider =
    (final SimQueue queue, final J job) -> SimQueuePredictor_IS.this.serviceTime;
  
}