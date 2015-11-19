package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.preemptive.SRTF;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link SRTF}.
 * 
 */
public class SimQueuePredictor_SRTF
extends AbstractSimQueuePredictor<SRTF>
{

  @Override
  protected double getNextQueueEventTimeBeyond
  (final SRTF queue,
   final SimQueueState<SimJob, SRTF> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    if (queueState.getRemainingServiceMap ().isEmpty ())
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final double smallestRs = queueState.getRemainingServiceMap ().firstKey ();
    if (smallestRs < 0)
      throw new RuntimeException ();
    queueEventTypes.add (SimEntitySimpleEventType.DEPARTURE);
    return time + smallestRs;
  }

  protected void preemptJob
  (final SRTF queue,
   final SimQueueState<SimJob, SRTF> queueState,
   final SimJob executingJob,
   final Set<JobQueueVisitLog<SimJob, SRTF>> visitLogsSet)
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
  
  @Override
  protected void doWorkloadEvents_SQ_SV_ROEL_U
  (final SRTF queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, SRTF> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, SRTF>> visitLogsSet)
   throws SimQueuePredictionException, WorkloadScheduleException
  {
    if ( queue == null
      || workloadSchedule == null
      || queueState == null
      || workloadEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (workloadEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimEntitySimpleEventType.Member eventType = (workloadEventTypes.isEmpty ()
      ? null
      : workloadEventTypes.iterator ().next ());
    final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
    final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
    final SimJob executingJob = (rsMap.isEmpty () ? null : rsMap.firstEntry ().getValue ().get (0));
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */
    }
    else if (eventType == SimQueueSimpleEventType.QUEUE_ACCESS_VACATION)
    {
      final boolean queueAccessVacation = workloadSchedule.getQueueAccessVacationMap_SQ_SV_ROEL_U ().get (time);
      queueState.setQueueAccessVacation (time, queueAccessVacation);
    }
    else if (eventType == SimEntitySimpleEventType.ARRIVAL)
    {
      final SimJob job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      final Set<SimJob> arrivals = new HashSet<> ();
      arrivals.add (job);
      queueState.doArrivals (time, arrivals, visitLogsSet);
      if ((! queueState.isQueueAccessVacation ()) && queueState.getServerAccessCredits () >= 1)
      {
        queueState.doStarts (time, arrivals);
        if (executingJob != null && rsMap.firstEntry ().getValue ().get (0) != executingJob)
          preemptJob (queue, queueState, executingJob, visitLogsSet);
      }
    }
    else if (eventType == SimEntitySimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final boolean interruptService =
          workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).get (job);
        // Make sure we do not revoke an executing job without the interruptService flag.
        if (interruptService || ! queueState.getJobsInServiceArea ().contains (job))
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
      if (oldSac == 0 && newSac > 0)
      {
        final Set<SimJob> starters = new LinkedHashSet<> ();
        final Iterator<SimJob> i_waiters = queueState.getJobsInWaitingAreaOrdered ().iterator ();
        int remainingSac = newSac;
        while ((remainingSac == Integer.MAX_VALUE || remainingSac > 0) && i_waiters.hasNext ())
        {
          starters.add (i_waiters.next ());
          if (remainingSac != Integer.MAX_VALUE)
            remainingSac--;
        }
        queueState.doStarts (time, starters);
        final Set<Double> rsTimes = new HashSet ();
        for (SimJob starter : starters)
          if (rsTimes.contains (rsTimeMap.get (starter)))
            throw new SimQueuePredictionAmbiguityException ();
          else
            rsTimes.add (rsTimeMap.get (starter));
        if (executingJob != null && rsMap.firstEntry ().getValue ().get (0) != executingJob)
          preemptJob (queue, queueState, executingJob, visitLogsSet);
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  protected void doQueueEvents_SQ_SV_ROEL_U
  (final SRTF queue,
   final SimQueueState<SimJob, SRTF> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, SRTF>> visitLogsSet)
   throws SimQueuePredictionException    
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (queueEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    final double time = queueState.getTime ();
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType == SimEntitySimpleEventType.DEPARTURE)
    {
      final Set<SimJob> departures = new HashSet<> ();
      final SimJob departingJob = queueState.getRemainingServiceMap ().firstEntry ().getValue ().get (0);
      departures.add (departingJob);
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }  
  
  @Override
  protected void updateToTime (final SRTF queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    final double oldTime = queueState.getTime ();
    if (! Double.isNaN (oldTime))
    {
      final double dT = newTime - oldTime;
      if (dT < 0)
        throw new RuntimeException ();
      final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
      final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
      if (dT > 0 && ! rsTimeMap.isEmpty ())
      { 
        final SimJob executingJob = rsMap.firstEntry ().getValue ().get (0);
        final double dS = dT;
        final double oldRs = rsTimeMap.get (executingJob);
        final double newRs =  oldRs - dS;
        rsTimeMap.put (executingJob, newRs);
        if (! rsMap.containsKey (oldRs))
          throw new IllegalStateException ();
        if (rsMap.get (oldRs).size () == 1)
        {
          rsMap.put (newRs, rsMap.get (oldRs));
          rsMap.remove (oldRs);
        }
        else
        {
          rsMap.get (oldRs).remove (0);
          rsMap.put (newRs, new ArrayList<> ());
          rsMap.get (newRs).add (executingJob);
        }
      }
    }
    queueState.setTime (newTime);
  }

}