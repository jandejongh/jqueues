package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link FCFS}.
 *
 */
public class SimQueuePredictor_FCFS
extends AbstractSimQueuePredictor<SimQueue>
{
  
  final boolean hasB;
  
  final int B;
  
  final boolean hasc;
  
  final int c;
  
  protected SimQueuePredictor_FCFS (final boolean hasB, final int B, final boolean hasc, final int c)
  {
    if (hasB && B < 0)
      throw new IllegalArgumentException ();
    if (hasc && c < 0)
      throw new IllegalArgumentException ();
    this.hasB = hasB;
    this.B = B;
    this.hasc = hasc;
    this.c = c;
  }
  
  public SimQueuePredictor_FCFS ()
  {
    this (false, 0, true, 1);
  }

  @Override
  protected double getNextQueueEventTimeBeyond
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    final int numberOfJobsInServiceArea = queueState.getJobRemainingServiceTimeMap ().size ();
    if (numberOfJobsInServiceArea == 0)
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    if (this.hasc && numberOfJobsInServiceArea > this.c)
      throw new IllegalStateException ();
    if (queueState.getStartTimesMap ().isEmpty ())
      throw new IllegalStateException ();
    if (this.hasc && queueState.getStartTimesMap ().size () > this.c)
      throw new IllegalStateException ();
    double minDepartureTime = Double.NaN;
    for (final Entry<SimJob, Double> jobInServiceAreaEntry : queueState.getStartTimesMap ().entrySet ())
    {
      final SimJob jobInServiceArea = jobInServiceAreaEntry.getKey ();
      final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (jobInServiceArea);
      final double departureTime = time + remainingServiceTime;
      if (departureTime < time)
        throw new RuntimeException ();
      if (Double.isNaN (minDepartureTime) || departureTime < minDepartureTime)
        minDepartureTime = departureTime;
    }
    queueEventTypes.add (SimEntitySimpleEventType.DEPARTURE);
    return minDepartureTime;
  }

  protected SimJob getJobToStart
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState)
   throws SimQueuePredictionException
  {
    return queueState.getJobsInWaitingAreaOrdered ().iterator ().next ();
  }
  
  @Override
  protected void doWorkloadEvents_SQ_SV_ROEL_U
  (final SimQueue queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, SimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, SimQueue>> visitLogsSet)
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
      if (queueState.isQueueAccessVacation ())
        queueState.doArrivals (time, arrivals, visitLogsSet);
      else
      {
        if (this.hasB && queueState.getJobsInWaitingArea ().size () > this.B)
          throw new IllegalStateException ();
        if (this.hasB && queueState.getJobsInWaitingArea ().size () == this.B
          && ! ((queueState.getServerAccessCredits () > 0)
                && ((! this.hasc) || queueState.getJobsInServiceArea ().size () < this.c)))
          // Drops.
          queueState.doExits (time, arrivals, null, null, null, visitLogsSet);
        else
        {
          queueState.doArrivals (time, arrivals, visitLogsSet);
          if (((! this.hasc) || queueState.getJobsInServiceArea ().size () < this.c)
            && queueState.getServerAccessCredits () >= 1)
            queueState.doStarts (time, arrivals);
        }
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
        final boolean isJobInServiceArea = queueState.getJobsInServiceArea ().contains (job);
        // Make sure we do not revoke a job in the service area without the interruptService flag.
        if (interruptService || ! isJobInServiceArea)
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
          if (isJobInServiceArea)
          {
            final int sac = queueState.getServerAccessCredits ();
            if (sac > 0 && queueState.getJobsInWaitingArea ().size () > 0)
            {
              final Set<SimJob> starters = new LinkedHashSet<> ();
              starters.add (getJobToStart (queue, queueState));
              queueState.doStarts (time, starters);
            }            
          }
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
      if (((! this.hasc) || queueState.getJobsInServiceArea ().size () < this.c)
        && oldSac == 0
        && newSac > 0)
      {
        int remainingMaxStarters = newSac;
        if (this.hasc)
          remainingMaxStarters = Math.min (remainingMaxStarters, this.c - queueState.getJobsInServiceArea ().size ());
        remainingMaxStarters = Math.min (remainingMaxStarters, queueState.getJobsInWaitingArea ().size ());
        while (remainingMaxStarters > 0)
        {
          final Set<SimJob> starters = new LinkedHashSet<> ();
          starters.add (getJobToStart (queue, queueState));
          if (remainingMaxStarters != Integer.MAX_VALUE)
            remainingMaxStarters--;
          queueState.doStarts (time, starters);
        }
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  protected void doQueueEvents_SQ_SV_ROEL_U
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, SimQueue>> visitLogsSet)
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
      if (queueState.getJobsInServiceArea ().size () < 1)
        throw new IllegalStateException ();
      if (this.hasc && queueState.getJobsInServiceArea ().size () > this.c)
        throw new IllegalStateException ();
      final Set<SimJob> departures = new HashSet<> (queueState.getRemainingServiceMap ().firstEntry ().getValue ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      final int sac = queueState.getServerAccessCredits ();
      if (sac > 0 && queueState.getJobsInWaitingArea ().size () > 0)
      {
        final Set<SimJob> starters = new LinkedHashSet<> ();
        starters.add (getJobToStart (queue, queueState));
        queueState.doStarts (time, starters);
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }
  
  @Override
  protected void updateToTime (final SimQueue queue, final SimQueueState queueState, final double newTime)
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
        rsMap.clear ();
        final double dS = dT;
        for (final SimJob job : new HashSet<> (rsTimeMap.keySet ()))
        {
          final double newRs = rsTimeMap.get (job) - dS;
          rsTimeMap.put (job, newRs);
          if (! rsMap.containsKey (newRs))
            rsMap.put (newRs, new ArrayList<> ());
          rsMap.get (newRs).add (job);
        }
      }
    }
    queueState.setTime (newTime);
  }

}