package nl.jdj.jqueues.r4.util.predictor.queues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r4.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r4.processorsharing.PS;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link PS}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_PS<J extends SimJob>
extends AbstractSimQueuePredictor<J, PS>
{

  /**
   * 
   * @return True.
   * 
   */
  @Override
  protected boolean is_ROEL_U_UnderWorkloadQueueEventClashes (final PS queue)
  {
    return true;
  }

  @Override
  protected double getNextQueueEventTimeBeyond
  (final PS queue,
   final SimQueueState<J, PS> queueState,
   final double time,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || ((! Double.isNaN (time)) && time != queueState.getTime ())
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final int numberOfJobsExecuting = queueState.getJobRemainingServiceTimeMap ().size ();
    if (numberOfJobsExecuting == 0)
      return Double.NaN;
    final double smallestRs = queueState.getRemainingServiceMap ().firstKey ();
    if (smallestRs < 0)
      throw new RuntimeException ();
    queueEventTypes.add (SimEntitySimpleEventType.DEPARTURE);
    return time + (smallestRs * numberOfJobsExecuting);
  }

  @Override
  protected void doWorkloadEvents_SQ_SV_ROEL_U
  (final PS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U<J, PS> workloadSchedule,
   final SimQueueState<J, PS> queueState,
   final double nextWorkloadEventTime,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<J, PS>> visitLogsSet)
   throws SimQueuePredictionException, WorkloadScheduleException
  {
    if ( queue == null
      || workloadSchedule == null
      || queueState == null
      || Double.isNaN (nextWorkloadEventTime)
      || nextWorkloadEventTime < queueState.getTime ()
      || workloadEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (workloadEventTypes.isEmpty ())
      return;
    if (workloadEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    updatedRemainingServiceTimes (queue, queueState, nextWorkloadEventTime);
    final SimEntitySimpleEventType.Member eventType = workloadEventTypes.iterator ().next ();
    if (eventType == SimQueueSimpleEventType.QUEUE_ACCESS_VACATION)
    {
      final boolean queueAccessVacation = workloadSchedule.getQueueAccessVacationMap_SQ_SV_ROEL_U ().get (nextWorkloadEventTime);
      if (queueAccessVacation)
        queueState.startQueueAccessVacation (nextWorkloadEventTime);
      else
        queueState.stopQueueAccessVacation (nextWorkloadEventTime);
    }
    else if (eventType == SimEntitySimpleEventType.ARRIVAL)
    {
      final J job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (nextWorkloadEventTime);
      final Set<J> arrivals = new HashSet<> ();
      arrivals.add (job);
      queueState.doArrivals (nextWorkloadEventTime, arrivals, visitLogsSet);
      if ((! queueState.isQueueAccessVacation ()) && queueState.getServerAccessCredits () >= 1)
        queueState.doStarts (nextWorkloadEventTime, arrivals);
    }
    else if (eventType == SimEntitySimpleEventType.REVOCATION)
    {
      final J job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (nextWorkloadEventTime).entrySet ().iterator ().next ().getKey ();
      final Set<J> revocations = new HashSet<> ();
      revocations.add (job);
      queueState.doExits (nextWorkloadEventTime, null, revocations, null, null, visitLogsSet);
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (nextWorkloadEventTime);
      queueState.setServerAccessCredits (nextWorkloadEventTime, newSac);
      if (oldSac == 0)
      {
        final Set<J> starters = new HashSet<> ();
        // XXX DOES THIS PRESERVE INSERTION ORDER???
        final Iterator<J> i_waiters = new HashSet<> (queueState.getJobsWaiting ()).iterator ();
        int remainingSac = newSac;
        while (remainingSac > 0 && ! i_waiters.hasNext ())
        {
          starters.add (i_waiters.next ());
          remainingSac--;
        }
        queueState.doStarts (nextWorkloadEventTime, starters);
      }
    }
    else
      throw new RuntimeException ();
  }

  @Override
  protected void doQueueEvents_SQ_SV_ROEL_U
  (final PS queue,
   final SimQueueState<J, PS> queueState,
   final double nextQueueEventTime,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<J, PS>> visitLogsSet)
   throws SimQueuePredictionException    
  {
    if ( queue == null
      || queueState == null
      || Double.isNaN (nextQueueEventTime)
      || nextQueueEventTime < queueState.getTime ()
      || queueEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (queueEventTypes.isEmpty ())
      return;
    if (queueEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    updatedRemainingServiceTimes (queue, queueState, nextQueueEventTime);
    final SimEntitySimpleEventType.Member eventType = queueEventTypes.iterator ().next ();
    if (eventType == SimEntitySimpleEventType.DEPARTURE)
    {
      final Set<J> departures = new HashSet<> (queueState.getRemainingServiceMap ().firstEntry ().getValue ());
      queueState.doExits (nextQueueEventTime, null, null, departures, null, visitLogsSet);
    }
    else
      throw new RuntimeException ();
  }  
  
  /** Updates the remaining service times of running jobs.
   * 
   * @param queue      The queue, non-{@code null}.
   * @param queueState The queue state, non-{@code null}.
   * @param newTime    The new time.
   * 
   * @throws IllegalArgumentException If {@code queue} or {@code queueState} is {@code null},
   *                                  or the time argument is in the past.
   * 
   */
  protected void updatedRemainingServiceTimes (final PS queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final double oldTime = queueState.getTime ();
    final double dT = newTime - oldTime;
    if (dT < 0)
      throw new RuntimeException ();
    final Map<J, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
    final NavigableMap<Double,List<J>> rsMap = queueState.getRemainingServiceMap ();
    if (dT > 0 && ! rsTimeMap.isEmpty ())
    { 
      rsMap.clear ();
      final double dS = dT / rsTimeMap.keySet ().size ();
      for (final J job : new HashSet<> (rsTimeMap.keySet ()))
      {
        final double newRs = rsTimeMap.get (job) - dS;
        rsTimeMap.put (job, newRs);
        if (! rsMap.containsKey (newRs))
          rsMap.put (newRs, new ArrayList<> ());
        rsMap.get (newRs).add (job);
      }
    }
  }

}