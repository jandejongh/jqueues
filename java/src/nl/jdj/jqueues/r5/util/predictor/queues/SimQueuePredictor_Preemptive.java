package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.preemptive.AbstractPreemptiveSimQueue;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** An abstract {@link SimQueuePredictor} for preemptive queues.
 * 
 */
public abstract class SimQueuePredictor_Preemptive<Q extends AbstractPreemptiveSimQueue>
extends AbstractSimQueuePredictor<Q>
{

  protected void preemptJob
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final SimJob executingJob,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
   throws SimQueuePredictionException
  {
    final double time = queueState.getTime ();
    final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
    final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
    switch (queue.getPreemptionStrategy ())
    {
      case DROP:
        final Set<SimJob> drops = new HashSet<> ();
        final SimJob droppedJob = executingJob;
        drops.add (droppedJob);
        queueState.doExits (time, drops, null, null, null, visitLogsSet);
        break;
      case RESUME:
        // Nothing to do.
        break;
      case RESTART:
        final double oldRs = rsTimeMap.get (executingJob);
              // RESTART: Must always take the service time from the job!
        // final double newRs = ((DefaultSimQueueState) queueState).getServiceTime (queue, executingJob);
        final double newRs = executingJob.getServiceTime (queue);
        rsTimeMap.put (executingJob, newRs);
        rsMap.get (oldRs).remove (executingJob);
        if (rsMap.get (oldRs).isEmpty ())
          rsMap.remove (oldRs);
        if (rsMap.containsKey (newRs))
        {
          // Start time of executing job.
          final double startTime = queueState.getStartTimesMap ().get (executingJob);
          // Jobs with equal remaining service time, ordered increasing in start time.
          final List<SimJob> equalRsJobs = rsMap.get (newRs);
          int indexToInsert = 0;
          for (final SimJob equalRsJob: equalRsJobs)
          {
            final double equalRsJobStartTime = queueState.getStartTimesMap ().get (equalRsJob);
            if (equalRsJobStartTime < startTime)
              indexToInsert++;
            else if (equalRsJobStartTime == startTime)
              throw new SimQueuePredictionAmbiguityException ();
            else
              break;
          }
          rsMap.get (newRs).add (indexToInsert, executingJob);
        }
        else
        {
          rsMap.put (newRs, new ArrayList<> ());
          rsMap.get (newRs).add (executingJob);
        }
        throw new UnsupportedOperationException ();
      case REDRAW:
        break;
      case DEPART:
        final Set<SimJob> departures = new HashSet<> ();
        final SimJob departingJob = executingJob;
        departures.add (departingJob);
        queueState.doExits (time, null, null, departures, null, visitLogsSet);
        break;
      case CUSTOM:
        throw new UnsupportedOperationException ();
      default:
        throw new UnsupportedOperationException ();
    }
  }
  
}