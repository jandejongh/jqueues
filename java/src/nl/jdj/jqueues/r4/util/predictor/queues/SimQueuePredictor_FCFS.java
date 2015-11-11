package nl.jdj.jqueues.r4.util.predictor.queues;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r4.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r4.nonpreemptive.FCFS;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link FCFS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_FCFS<J extends SimJob>
extends AbstractSimQueuePredictor<J, FCFS>
{

  @Override
  protected double getNextQueueEventTimeBeyond
  (final FCFS queue,
   final SimQueueState<J, FCFS> queueState,
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
    if (numberOfJobsExecuting > 1)
      throw new IllegalStateException ();
    if (queueState.getStartTimesMap ().size () != 1)
      throw new IllegalStateException ();
    final Entry<J, Double> jobExecutingEntry = queueState.getStartTimesMap ().entrySet ().iterator ().next ();
    final J jobExecuting = jobExecutingEntry.getKey ();
    final double startTime = jobExecutingEntry.getValue ();
    final double serviceTime = queueState.getJobRemainingServiceTimeMap ().get (jobExecuting);
    final double departureTime = startTime + serviceTime;
    if (departureTime < time)
      throw new RuntimeException ();
    queueEventTypes.add (SimEntitySimpleEventType.DEPARTURE);
    return departureTime;
  }

  @Override
  protected void doWorkloadEvents_SQ_SV_ROEL_U
  (final FCFS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U<J, FCFS> workloadSchedule,
   final SimQueueState<J, FCFS> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<J, FCFS>> visitLogsSet)
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
      if (queueAccessVacation)
        queueState.startQueueAccessVacation (time);
      else
        queueState.stopQueueAccessVacation (time);
    }
    else if (eventType == SimEntitySimpleEventType.ARRIVAL)
    {
      final J job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      final Set<J> arrivals = new HashSet<> ();
      arrivals.add (job);
      queueState.doArrivals (time, arrivals, visitLogsSet);
      if ((! queueState.isQueueAccessVacation ())
        && queueState.getJobsExecuting ().isEmpty ()
        && queueState.getServerAccessCredits () >= 1)
        queueState.doStarts (time, arrivals);
    }
    else if (eventType == SimEntitySimpleEventType.REVOCATION)
    {
      final J job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final boolean interruptService =
          workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).get (job);
        final boolean isExecutingJob = queueState.getJobsExecuting ().contains (job);
        // Make sure we do not revoke an executing job without the interruptService flag.
        if (interruptService || ! isExecutingJob)
        {
          final Set<J> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
          if (isExecutingJob)
          {
            final int sac = queueState.getServerAccessCredits ();
            if (sac > 0)
            {
              final Set<J> starters = new LinkedHashSet<> ();
              final Iterator<J> i_waiters = queueState.getJobsWaitingOrdered ().iterator ();
              if (i_waiters.hasNext ())
              {
                starters.add (i_waiters.next ());
                queueState.doStarts (time, starters);
              }
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
      if (queueState.getJobsExecuting ().isEmpty ()
        && oldSac == 0
        && newSac > 0)
      {
        final Set<J> starters = new LinkedHashSet<> ();
        final Iterator<J> i_waiters = queueState.getJobsWaitingOrdered ().iterator ();
        if (i_waiters.hasNext ())
        {
          starters.add (i_waiters.next ());
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
  (final FCFS queue,
   final SimQueueState<J, FCFS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<J, FCFS>> visitLogsSet)
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
      if (queueState.getJobsExecuting ().size () != 1)
        throw new IllegalStateException ();
      final Set<J> departures = new HashSet<> (queueState.getRemainingServiceMap ().firstEntry ().getValue ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      final int sac = queueState.getServerAccessCredits ();
      if (sac > 0)
      {
        final Set<J> starters = new LinkedHashSet<> ();
        final Iterator<J> i_waiters = queueState.getJobsWaitingOrdered ().iterator ();
        if (i_waiters.hasNext ())
        {
          starters.add (i_waiters.next ());
          queueState.doStarts (time, starters);
        }
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }
  
  @Override
  protected void updateToTime (final FCFS queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    queueState.setTime (newTime);
  }

}