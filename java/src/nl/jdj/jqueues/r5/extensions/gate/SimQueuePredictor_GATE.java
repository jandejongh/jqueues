package nl.jdj.jqueues.r5.extensions.gate;

import java.util.HashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.queue.serverless.GATE;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link GATE}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * 
 */
public class SimQueuePredictor_GATE<J extends SimJob>
extends AbstractSimQueuePredictor<J, GATE>
{

  @Override
  protected SimQueueWithGateState<J, GATE> createQueueState (final GATE queue, final boolean isROEL)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    if (! isROEL)
      throw new UnsupportedOperationException ();
    return new DefaultSimQueueWithGateState<> (queue);
  }

  @Override
  protected double getNextQueueEventTimeBeyond
  (final GATE queue,
   final SimQueueState<J, GATE> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    return Double.NaN;
  }

  @Override
  protected void doWorkloadEvents_SQ_SV_ROEL_U
  (final GATE queue,
   final WorkloadSchedule_SQ_SV_ROEL_U<J, GATE> workloadSchedule,
   final SimQueueState<J, GATE> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<J, GATE>> visitLogsSet)
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
      // Abuse queueState.doArrivals for dropping arrivals upon queue-access vacation.
      if (queueState.isQueueAccessVacation ())
        queueState.doArrivals (time, arrivals, visitLogsSet);
      else if (((SimQueueWithGateState) queueState).getGatePassageCredits () > 0)
      {
        final int oldPassages = ((SimQueueWithGateState) queueState).getGatePassageCredits ();
        // Departure.
        queueState.doExits (time, null, null, arrivals, null, visitLogsSet);
        if (oldPassages < Integer.MAX_VALUE)
          ((SimQueueWithGateState) queueState).setGatePassageCredits (time, oldPassages - 1);
      }
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
    else if (eventType == SimQueueWithGateSimpleEventType.GATE)
    {
      final int oldPassages = ((SimQueueWithGateState) queueState).getGatePassageCredits ();
      throw new UnsupportedOperationException ();
//      int remainingPassages = workloadSchedule.getGatePassagesMap_SQ_SV_ROEL_U ().get (time);
//      if (oldPassages == 0)
//      {
//        final Set<J> departures = new LinkedHashSet<> ();
//        final Iterator<J> i_waiters = queueState.getJobsWaitingOrdered ().iterator ();
//        while ((remainingPassages == Integer.MAX_VALUE || remainingPassages > 0) && i_waiters.hasNext ())
//        {
//          departures.add (i_waiters.next ());
//          if (remainingPassages != Integer.MAX_VALUE)
//            remainingPassages--;
//        }
//        ((SimQueueWithGateState) queueState).setGateRemainingPassages (time, remainingPassages);
//        queueState.doExits (time, null, null, departures, null, visitLogsSet);
//      }
//      ((SimQueueWithGateState) queueState).setGateRemainingPassages (time, remainingPassages);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  protected void doQueueEvents_SQ_SV_ROEL_U
  (final GATE queue,
   final SimQueueState<J, GATE> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<J, GATE>> visitLogsSet)
   throws SimQueuePredictionException    
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null
      || visitLogsSet == null)
      throw new IllegalArgumentException ();
    if (queueEventTypes.size () > 1)
      throw new SimQueuePredictionAmbiguityException ();
    throw new IllegalStateException ();
  }  
  
  @Override
  protected void updateToTime (final GATE queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    queueState.setTime (newTime);
  }

}