package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.composite.single.encap.BlackEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimQueueAccessVacationEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.SimQueueServerAccessCreditsEvent;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionInvalidInputException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link BlackEncapsulatorSimQueue}.
 *
 */
public class SimQueuePredictor_Enc
implements SimQueuePredictor<BlackEncapsulatorSimQueue>
{
  
  final SimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_Enc (final SimQueuePredictor encQueuePredictor)
  {
    if (encQueuePredictor == null)
      throw new IllegalArgumentException ();
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public Map<SimJob, JobQueueVisitLog<SimJob, BlackEncapsulatorSimQueue>>
  predictVisitLogs_SQ_SV_ROEL_U
  (final BlackEncapsulatorSimQueue queue, final Set<SimEntityEvent> queueEvents)
   throws SimQueuePredictionException
  {
    if (queue == null || queue.getEncapsulatedQueue () == null)
      throw new IllegalArgumentException ();
    if (queueEvents == null)
      return Collections.EMPTY_MAP;
    final SimQueue encQueue = queue.getEncapsulatedQueue ();
    final Set<SimEntityEvent> encQueueEvents = new LinkedHashSet<> ();
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
        {
          final boolean interruptService = ((SimQueueJobRevocationEvent) e).isInterruptService ();
          encQueueEvents.add (new SimQueueJobRevocationEvent (job, encQueue, time, interruptService));
        }
        else if (e instanceof SimQueueServerAccessCreditsEvent)
        {
          final int credits = ((SimQueueServerAccessCreditsEvent) e).getCredits ();
          encQueueEvents.add (new SimQueueServerAccessCreditsEvent (encQueue, time, credits));
        }
        else
          throw new SimQueuePredictionInvalidInputException ();
      }
    }
    final Map<SimJob, JobQueueVisitLog<SimJob, SimQueue>> encVisitLogs =
      this.encQueuePredictor.predictVisitLogs_SQ_SV_ROEL_U (encQueue, encQueueEvents);
    final Map<SimJob, JobQueueVisitLog<SimJob, BlackEncapsulatorSimQueue>> visitLogs = new HashMap<> ();
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
          jvl.started, jvl.startTime,
          jvl.dropped, jvl.dropTime,
          jvl.revoked, jvl.revocationTime,
          jvl.departed, jvl.departureTime));
    }
    return visitLogs;
  }
  
  @Override
  public Map<SimJob, JobQueueVisitLog<SimJob, BlackEncapsulatorSimQueue>>
  predictVisitLogs_SQ_SV_IOEL_U
  (final BlackEncapsulatorSimQueue queue,
   final NavigableMap<Double, Set<SimEntityEvent>> workloadEventsMap,
   final NavigableMap<Double, Set<SimEntityEvent>> processedEventsMap)
  throws SimQueuePredictionException
  {
    throw new UnsupportedOperationException ();
  }  
  
}