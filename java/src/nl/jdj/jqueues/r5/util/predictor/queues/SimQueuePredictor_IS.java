package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.function.ToDoubleBiFunction;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IS;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link IS}.
 *
 */
public class SimQueuePredictor_IS
extends SimQueuePredictor_FCFS
{
  
  final boolean overrideServiceTime;
  
  final double serviceTime;

  @Override
  protected SimQueueState<SimJob, SimQueue> createQueueState (SimQueue queue, boolean isROEL)
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
  
  final ToDoubleBiFunction<SimQueue, SimJob> serviceTimeProvider =
    (final SimQueue queue, final SimJob job) -> SimQueuePredictor_IS.this.serviceTime;
  
}