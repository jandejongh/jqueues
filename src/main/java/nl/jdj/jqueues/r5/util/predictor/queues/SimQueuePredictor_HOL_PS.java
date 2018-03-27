package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.qos.HOL_PS;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoSStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link HOL_PS}.
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
public class SimQueuePredictor_HOL_PS<J extends SimJob, Q extends HOL_PS, P>
extends AbstractSimQueuePredictor<HOL_PS>
{

  /** Registers a new {@link SimQueueQoSStateHandler} at the object created by super method.
   *
   * @return The object created by the super method with a new registered {@link SimQueueQoSStateHandler}.
   * 
   */
  @Override
  public SimQueueState<SimJob, HOL_PS> createQueueState (final HOL_PS queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueQoSStateHandler<> (false));
    return queueState;
  }

  @Override
  public String toString ()
  {
    return "Predictor[HOL-PS]";
  }

  @Override
  public boolean isStartArmed (final HOL_PS queue, final SimQueueState<SimJob, HOL_PS> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return queueState.getJobs ().isEmpty ();
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final HOL_PS queue,
   final SimQueueState<SimJob, HOL_PS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    final int numberOfJobsExecuting = queueState.getJobRemainingServiceTimeMap ().size ();
    if (numberOfJobsExecuting == 0)
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final double smallestRs = queueState.getRemainingServiceMap ().firstKey ();
    if (smallestRs < 0)
      throw new RuntimeException ();
    if (Double.isFinite (smallestRs))
    {
      queueEventTypes.add (SimQueueSimpleEventType.DEPARTURE);
      return time + (smallestRs * numberOfJobsExecuting);
    }
    else
      return Double.NaN;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final HOL_PS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, HOL_PS> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, HOL_PS>> visitLogsSet)
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
    final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
      (SimQueueQoSStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
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
    else if (eventType == SimQueueSimpleEventType.ARRIVAL)
    {
      final SimJob job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      final Set<SimJob> arrivals = new HashSet<> ();
      arrivals.add (job);
      queueState.doArrivals (time, arrivals, visitLogsSet);
      queueStateHandler.updateJobsQoSMap ();
      final P defaultQoS = (P) queue.getDefaultJobQoS ();
      if (defaultQoS == null)
        throw new IllegalStateException ();
      final P qos = (job.getQoS () == null ? defaultQoS : ((P) job.getQoS ()));
      if ((! queueState.isQueueAccessVacation ())
        && queueState.getServerAccessCredits () >= 1
        && queueStateHandler.getJobsQoSMap ().get (qos).size () == 1)
      {
        queueState.doStarts (time, arrivals);
        // Check whether job did not already leave!
        if (queueState.getJobs ().contains (job))
        {
          final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (job);
          if (remainingServiceTime == 0)
          {
            queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
            queueStateHandler.updateJobsQoSMap ();
          }
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final boolean interruptService =
          workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).get (job);
        final boolean jobInService = queueState.getJobsInServiceArea ().contains (job);
        // Make sure we do not revoke an executing job without the interruptService flag.
        if (interruptService || ! jobInService)
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
          queueStateHandler.updateJobsQoSMap ();
          // Note that there is only potential for rescheduling if the job was revoked from the service area!
          if (jobInService && queueState.getServerAccessCredits () >= 1)
          {
            final P defaultQoS = (P) queue.getDefaultJobQoS ();
            if (defaultQoS == null)
              throw new IllegalStateException ();
            final P qos = (job.getQoS () == null ? defaultQoS : ((P) job.getQoS ()));
            if (queueStateHandler.getJobsQoSMap ().containsKey (qos))
            {
              final SimJob starter = queueStateHandler.getJobsQoSMap ().get (qos).iterator ().next ();
              final Set<SimJob> starters = new HashSet<> ();
              starters.add (starter);
              queueState.doStarts (time, starters);
              // Check whether job did not already leave!
              if (queueState.getJobs ().contains (starter))
              {
                final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (starter);
                if (remainingServiceTime == 0)
                {
                  queueState.doExits (time, null, null, starters, null, visitLogsSet);
                  queueStateHandler.updateJobsQoSMap ();
                }
              }
            }
          }
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      queueState.setServerAccessCredits (time, workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time));
      final P defaultQoS = (P) queue.getDefaultJobQoS ();
      if (defaultQoS == null)
        throw new IllegalStateException ();
      boolean tryToStart = queueState.getJobsInWaitingArea ().size () > 0;
      while (queueState.getServerAccessCredits () > 0 && tryToStart)
      {
        // Look at all jobs in the waiting area, in arrival order.
        // (Yes, starting from scratch over and over again...)
        // Reset our indicator to try to start more jobs
        // for the case we fall through the for-loop (i.e., no jobs eligible to start).
        tryToStart = false;
        for (final SimJob job : queueState.getJobsInWaitingAreaOrdered ())
        {
          final P qos = (job.getQoS () == null ? defaultQoS : ((P) job.getQoS ()));
          if (queueStateHandler.getJobsQoSMap ().get (qos).iterator ().next () == job)
          {
            // The job is waiting, yet it is the first job in its QoS class, so it should be in service.
            final Set<SimJob> starters = new LinkedHashSet<> ();
            starters.add (job);
            queueState.doStarts (time, starters);
            // Check whether job did not already leave!
            if (queueState.getJobs ().contains (job))
            {
              final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (job);
              if (remainingServiceTime == 0)
              {
                queueState.doExits (time, null, null, starters, null, visitLogsSet);
                queueStateHandler.updateJobsQoSMap ();
              }
            }
            // Indication to try to start more jobs (since we have not seen them all yet...).
            tryToStart = true;
            // We must break here because our iterator over the waiting jobs has become useless (ConcurrentModificationException).
            break;
          }
        }
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final HOL_PS queue,
   final SimQueueState<SimJob, HOL_PS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, HOL_PS>> visitLogsSet)
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
    final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
      (SimQueueQoSStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType == SimQueueSimpleEventType.DEPARTURE)
    {
      final Set<SimJob> departures = new HashSet<> (queueState.getRemainingServiceMap ().firstEntry ().getValue ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      queueStateHandler.updateJobsQoSMap ();
      if (queueState.getServerAccessCredits () >= 1)
      {
        if (departures.size () > 1)
          throw new SimQueuePredictionAmbiguityException ();
        final J job = (J) departures.iterator ().next ();
        final P defaultQoS = (P) queue.getDefaultJobQoS ();
        if (defaultQoS == null)
          throw new IllegalStateException ();
        final P qos = (job.getQoS () == null ? defaultQoS : ((P) job.getQoS ()));
        if (queueStateHandler.getJobsQoSMap ().containsKey (qos))
        {
          final SimJob starter = queueStateHandler.getJobsQoSMap ().get (qos).iterator ().next ();
          final Set<SimJob> starters = new HashSet<> ();
          starters.add (starter);
          queueState.doStarts (time, starters);
          // Check whether job did not already leave!
          if (queueState.getJobs ().contains (starter))
          {
            final double remainingServiceTime = queueState.getJobRemainingServiceTimeMap ().get (starter);
            if (remainingServiceTime == 0)
            {
              queueState.doExits (time, null, null, starters, null, visitLogsSet);
              queueStateHandler.updateJobsQoSMap ();
            }
          }
        }
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }  
  
  @Override
  public void updateToTime (final HOL_PS queue, final SimQueueState queueState, final double newTime)
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
        final double dS = dT / rsTimeMap.keySet ().size ();
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