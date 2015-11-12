package nl.jdj.jqueues.r4.util.predictor.queues;

import java.util.NavigableMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.nonpreemptive.SJF;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link SJF}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_SJF<J extends SimJob>
extends SimQueuePredictor_FCFS<J>
{

  @Override
  protected J getJobToStart (final SimQueue queue, final SimQueueState<J, SimQueue> queueState)
  throws SimQueuePredictionException
  {
    final NavigableMap<Double, J> serviceTimeMap = new TreeMap<> ();
    for (final J job : queueState.getJobsWaiting ())
    {
      final double serviceTime = ((DefaultSimQueueState) queueState).getServiceTime (queue, job);
      if (serviceTimeMap.containsKey (serviceTime))
        throw new SimQueuePredictionAmbiguityException ();
      serviceTimeMap.put (serviceTime, job);
    }
    return serviceTimeMap.firstEntry ().getValue ();
  }

  
  public SimQueuePredictor_SJF ()
  {
    super ();
  }

}