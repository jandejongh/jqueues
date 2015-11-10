package nl.jdj.jqueues.r4.util.predictor.queues;

import java.util.HashSet;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r4.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r4.serverless.DELAY;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r4.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link DELAY}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_DELAY<J extends SimJob>
extends AbstractSimQueuePredictor<J, DELAY>
{

  /**
   * 
   * @return False.
   * 
   */
  @Override
  protected boolean is_ROEL_U_UnderWorkloadQueueEventClashes (final DELAY queue)
  {
    return false;
  }

  @Override
  protected double getNextQueueEventTimeBeyond
  (final DELAY queue,
   final SimQueueState<J, DELAY> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    if (queueState.getJobs ().isEmpty ())
      return Double.NaN;
    else
    {
      queueEventTypes.add (SimEntitySimpleEventType.DEPARTURE);
      return queueState.getJobArrivalsMap ().firstKey () + queue.getWaitTime ();
    }
  }

  @Override
  protected void doWorkloadEvents_SQ_SV_ROEL_U
  (final DELAY queue,
   final WorkloadSchedule_SQ_SV_ROEL_U<J, DELAY> workloadSchedule,
   final SimQueueState<J, DELAY> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<J, DELAY>> visitLogsSet)
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
      final double waitTime = queue.getWaitTime ();
      final Set<J> arrivals = new HashSet<> ();
      arrivals.add (job);
      // Abuse queueState.doArrivals for dropping arrivals upon queue-access vacation.
      if (waitTime > 0 || queueState.isQueueAccessVacation ())
        queueState.doArrivals (time, arrivals, visitLogsSet);
      else
        // Departure.
        queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
    }
    else if (eventType == SimEntitySimpleEventType.REVOCATION)
    {
      final J job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final Set<J> revocations = new HashSet<> ();
        revocations.add (job);
        queueState.doExits (time, null, revocations, null, null, visitLogsSet);
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  protected void doQueueEvents_SQ_SV_ROEL_U
  (final DELAY queue,
   final SimQueueState<J, DELAY> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<J, DELAY>> visitLogsSet)
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
      final Set<J> departures = new HashSet<> (queueState.getJobArrivalsMap ().firstEntry ().getValue ());
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }  
  
  @Override
  protected void updateToTime (final DELAY queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    queueState.setTime (newTime);
  }

}