package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.composite.single.enc.BlackEncapsulatorHideStartSimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimQueueAccessVacationEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.SimQueueServerAccessCreditsEvent;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.extensions.composite.SimQueueCompositeStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.DefaultSimQueuePrediction_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionInvalidInputException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePrediction_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link BlackEncapsulatorHideStartSimQueue}.
 *
 */
public class SimQueuePredictor_EncHS
extends AbstractSimQueuePredictor<BlackEncapsulatorHideStartSimQueue>
implements SimQueuePredictor<BlackEncapsulatorHideStartSimQueue>
{
  
  final SimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_EncHS (final SimQueuePredictor encQueuePredictor)
  {
    if (encQueuePredictor == null)
      throw new IllegalArgumentException ();
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public String toString ()
  {
    return "Predictor[EncHS[?]]";
  }

  @Override
  public SimQueueState<SimJob, BlackEncapsulatorHideStartSimQueue> createQueueState
  (final BlackEncapsulatorHideStartSimQueue queue,
   final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    final SimQueue encQueue = queue.getEncapsulatedQueue ();
    final DefaultSimQueueState encQueueState = (DefaultSimQueueState) this.encQueuePredictor.createQueueState (encQueue, isROEL);
    queueState.registerHandler (new SimQueueCompositeStateHandler (queue.getQueues (), Collections.singleton (encQueueState)));
    return queueState;
  }

  @Override
  public boolean isStartArmed
  (final BlackEncapsulatorHideStartSimQueue queue,
   final SimQueueState<SimJob, BlackEncapsulatorHideStartSimQueue> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return false;
  }

  @Override
  public SimQueuePrediction_SQ_SV
  predict_SQ_SV_ROEL_U
  (final BlackEncapsulatorHideStartSimQueue queue, final Set<SimEntityEvent> queueEvents)
   throws SimQueuePredictionException
  {
    if (queue == null || queue.getEncapsulatedQueue () == null)
      throw new IllegalArgumentException ();
    if (queueEvents == null)
      return new DefaultSimQueuePrediction_SQ_SV
                   (queue, Collections.EMPTY_MAP, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    final SimQueue encQueue = queue.getEncapsulatedQueue ();
    final Set<SimEntityEvent> encQueueEvents = new LinkedHashSet<> ();
    int compSac = Integer.MAX_VALUE;
    final List<Map<Double, Boolean>> compSacLog = new ArrayList<>  ();
    for (SimEntityEvent e : queueEvents)
    {
      if (e == null)
        throw new IllegalArgumentException ();
      if (e.getQueue () == encQueue)
        throw new IllegalArgumentException ();
      if (e.getQueue () == queue)
      {
        final double time = e.getTime ();
        final SimJob job = e.getJob ();
        if (e instanceof SimQueueAccessVacationEvent)
        {
          final boolean vacation = ((SimQueueAccessVacationEvent) e).getVacation ();
          encQueueEvents.add (new SimQueueAccessVacationEvent (encQueue, time, vacation));
        }
        else if (e instanceof SimQueueJobArrivalEvent)
        {
          if (! (job instanceof DefaultSimJob))
            throw new UnsupportedOperationException ();
          ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (encQueue, job.getServiceTime (queue));
          encQueueEvents.add (new SimQueueJobArrivalEvent (job, encQueue, time));
        }
        else if (e instanceof SimQueueJobRevocationEvent)
          // The interruptService argument is irrelevant; on an EncHS queue, real jobs are always in the waiting area,
          // hence a revocation cannot fail.
          // We must therefore revoke the delegate job unconditionally from the encapsulated queue.
          encQueueEvents.add (new SimQueueJobRevocationEvent (job, encQueue, time, true));
        else if (e instanceof SimQueueServerAccessCreditsEvent)
        {
          // We must not pass the server-access credits events to the encapsulated queue, but process them ourselves.
          final int oldCredits = compSac;
          final int newCredits = ((SimQueueServerAccessCreditsEvent) e).getCredits ();
          if (oldCredits == 0 && newCredits > 0)
            compSacLog.add (Collections.singletonMap (time, true));
          if (oldCredits > 0 && newCredits == 0)
            compSacLog.add (Collections.singletonMap (time, false));
          compSac = newCredits;
        }
        else
          throw new SimQueuePredictionInvalidInputException ();
      }
    }
    final SimQueuePrediction_SQ_SV encPrediction = this.encQueuePredictor.predict_SQ_SV_ROEL_U (encQueue, encQueueEvents);
    final Map<SimJob, JobQueueVisitLog<SimJob, SimQueue>> encVisitLogs = encPrediction.getVisitLogs ();
    final Map<SimJob, JobQueueVisitLog<SimJob, BlackEncapsulatorHideStartSimQueue>> visitLogs = new HashMap<> ();
    for (final Entry<SimJob, JobQueueVisitLog<SimJob, SimQueue>> entry : encVisitLogs.entrySet ())
    {
      final SimJob j = entry.getKey ();
      if (j == null)
        throw new UnsupportedOperationException ();
      final JobQueueVisitLog<SimJob, SimQueue> jvl = entry.getValue ();
      if (jvl == null)
        throw new UnsupportedOperationException ();
      if (jvl.job != j || jvl.queue != encQueue)
        throw new RuntimeException ();
      visitLogs.put (j,
        new JobQueueVisitLog (j, queue,
          jvl.arrived, jvl.arrivalTime, jvl.sequenceNumber,
          // Ignore the start of the delegate job!
          false, Double.NaN,
          jvl.dropped, jvl.dropTime,
          jvl.revoked, jvl.revocationTime,
          jvl.departed, jvl.departureTime));
    }
    final List<Map<Double, Boolean>> encQavLog = encPrediction.getQueueAccessVacationLog ();
    final List<Map<Double, Boolean>> encSacLog = encPrediction.getServerAccessCreditsAvailabilityLog ();
    if (! encSacLog.isEmpty ())
      throw new IllegalStateException ();
    final List<Map<Double, Boolean>> encStaLog = encPrediction.getStartArmedLog ();
    return new DefaultSimQueuePrediction_SQ_SV<> (queue, visitLogs, encQavLog, compSacLog, Collections.EMPTY_LIST);
  }
  
  @Override
  public SimQueuePrediction_SQ_SV
  predict_SQ_SV_IOEL_U
  (final BlackEncapsulatorHideStartSimQueue queue,
   final NavigableMap<Double, Set<SimEntityEvent>> workloadEventsMap,
   final NavigableMap<Double, Set<SimEntityEvent>> processedEventsMap)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }  

  @Override
  public double getNextQueueEventTimeBeyond
  (final BlackEncapsulatorHideStartSimQueue queue,
   final SimQueueState<SimJob, BlackEncapsulatorHideStartSimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void updateToTime
  (final BlackEncapsulatorHideStartSimQueue queue,
   final SimQueueState queueState,
   final double newTime)
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final BlackEncapsulatorHideStartSimQueue queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, BlackEncapsulatorHideStartSimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, BlackEncapsulatorHideStartSimQueue>> visitLogsSet)
  throws SimQueuePredictionException, WorkloadScheduleException
  {
    throw new UnsupportedOperationException ();
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final BlackEncapsulatorHideStartSimQueue queue,
   final SimQueueState<SimJob, BlackEncapsulatorHideStartSimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, BlackEncapsulatorHideStartSimQueue>> visitLogsSet)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }
  
}