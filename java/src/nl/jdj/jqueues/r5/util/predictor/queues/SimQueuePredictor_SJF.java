package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.NavigableMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive.SJF;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link SJF}.
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class SimQueuePredictor_SJF
extends SimQueuePredictor_FCFS
{

  @Override
  public String toString ()
  {
    return "Predictor[SJF]";
  }

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
        // Since SJF breaks ties through arrival-time ordering, we can skip this job.
        continue;
      serviceTimeMap.put (serviceTime, job);
    }
    return serviceTimeMap.firstEntry ().getValue ();
  }

  
  public SimQueuePredictor_SJF ()
  {
    super ();
  }

}