package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.NavigableMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.LJF;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link LJF}.
 *
 */
public class SimQueuePredictor_LJF
extends SimQueuePredictor_FCFS
{

  @Override
  protected SimJob getJobToStart (final SimQueue queue, final SimQueueState<SimJob, SimQueue> queueState)
  throws SimQueuePredictionException
  {
    final NavigableMap<Double, SimJob> serviceTimeMap = new TreeMap<> ();
    for (final SimJob job : queueState.getJobsInWaitingAreaOrdered ())
    {
      final double serviceTime = ((DefaultSimQueueState) queueState).getServiceTime (queue, job);
      if (serviceTimeMap.containsKey (serviceTime))
        // We already found a job with equal requested service time.
        // Since LJF breaks ties through arrival-time ordering, we can skip this job.
        continue;
      serviceTimeMap.put (serviceTime, job);
    }
    return serviceTimeMap.lastEntry ().getValue ();
  }

  
  public SimQueuePredictor_LJF ()
  {
    super ();
  }

  @Override
  public String toString ()
  {
    return "Predictor[LJF]";
  }

}