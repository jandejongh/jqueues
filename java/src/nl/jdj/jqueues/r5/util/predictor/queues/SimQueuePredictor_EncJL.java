package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.EncJL;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite_Enc;
import nl.jdj.jqueues.r5.extensions.composite.SimQueueCompositeStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link EncJL}.
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
public class SimQueuePredictor_EncJL
extends AbstractSimQueuePredictor_Composite_Enc<EncJL>
implements SimQueuePredictor<EncJL>
{
  
  final AbstractSimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_EncJL (final AbstractSimQueuePredictor encQueuePredictor)
  {
    super (encQueuePredictor);
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public String toString ()
  {
    return "Predictor[EncJL[?]]";
  }

  @Override
  public boolean isStartArmed
  (final EncJL queue,
   final SimQueueState<SimJob, EncJL> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    if (queueState == null)
      throw new IllegalArgumentException ();
    return this.encQueuePredictor.isStartArmed (queue.getEncapsulatedQueue (), queueStateHandler.getSubQueueState (0))
           && (queue.getMaxJobs () == Integer.MAX_VALUE
               || queueState.getJobs ().size () < queue.getMaxJobs ())
           && (queue.getMaxJobsInServiceArea () == Integer.MAX_VALUE
               || queueState.getJobsInServiceArea ().size () < queue.getMaxJobsInServiceArea ());
  }

  private void setServerAccessCreditsOnEncQueue
  (final EncJL queue,
   final SimQueueState<SimJob, EncJL> queueState,
   final SimQueue subQueue,
   final SimQueueState subQueueState,
   final Set<JobQueueVisitLog<SimJob, EncJL>> visitLogsSet)
  throws SimQueuePredictionException
  {
    int sacRequired = Integer.MAX_VALUE;
    if (queue.getMaxJobsInServiceArea () < Integer.MAX_VALUE)
      sacRequired = queue.getMaxJobsInServiceArea () - subQueueState.getJobsInServiceArea ().size ();
    if (queueState.getServerAccessCredits () < Integer.MAX_VALUE
    && queueState.getServerAccessCredits () < sacRequired)
      sacRequired = queueState.getServerAccessCredits ();
    if (subQueueState.getServerAccessCredits () != sacRequired)
    {
//      System.err.println ("Setting SACs on encQueue to " + sacRequired + ".");
      final SubQueueSimpleEvent encQueueSacEvent =
        new SubQueueSimpleEvent
          (subQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, sacRequired);
      doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (encQueueSacEvent)), visitLogsSet);
    }      
  }
    
  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final EncJL queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, EncJL> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncJL>> visitLogsSet)
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
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final SimQueue subQueue = queue.getEncapsulatedQueue ();
    final SimQueueState subQueueState = queueStateHandler.getSubQueueState (0);
    //
    // Set the [initial] SAC on the encQueue if needed.
    //
    setServerAccessCreditsOnEncQueue (queue, queueState, subQueue, subQueueState, visitLogsSet);
    final SimEntitySimpleEventType.Member eventType = (workloadEventTypes.isEmpty ()
      ? null
      : workloadEventTypes.iterator ().next ());
    if (eventType == SimQueueSimpleEventType.ARRIVAL)
    {
      final SimJob job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      boolean mustDrop = (queue.getMaxJobs () == 0);
      mustDrop = mustDrop || (queue.getMaxJobsInWaitingArea () < Integer.MAX_VALUE
                              && subQueueState.getJobsInWaitingArea ().size () == queue.getMaxJobsInWaitingArea ()
                              && ! (this.subQueuePredictor.isStartArmed (subQueue, subQueueState)
                                    && subQueueState.getJobsInWaitingArea ().isEmpty ()
                                    && subQueueState.getServerAccessCredits () > 0));
      if (mustDrop)
      {
//        System.err.println ("Dropping " + job + ", at t=" + time + ".");
        final Set<SimJob> arrivals = new HashSet<> ();
        arrivals.add (job);
        queueState.doArrivals (time, arrivals, visitLogsSet); // Takes care of qav.
        if (queueState.getJobs ().contains (job))
          queueState.doExits (time, arrivals, null, null, null, visitLogsSet);
        workloadEventTypes.remove (eventType);
      }
      else
      {
//        System.err.println ("Amitting " + job + ", at t=" + time + ".");
//        System.err.println (" -> Jw : " + subQueueState.getJobsInWaitingArea ().size ());
//        System.err.println (" -> StA: " + this.subQueuePredictor.isStartArmed (subQueue, subQueueState));
//        System.err.println (" -> SAC: " + subQueueState.getServerAccessCredits ());
        super.doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule, queueState, workloadEventTypes, visitLogsSet);
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      // super.doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule, queueState, workloadEventTypes, visitLogsSet);
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
      setServerAccessCreditsOnEncQueue (queue, queueState, subQueue, subQueueState, visitLogsSet);
    }
    else
      super.doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule, queueState, workloadEventTypes, visitLogsSet);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final EncJL queue,
   final SimQueueState<SimJob, EncJL> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncJL>> visitLogsSet)
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
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final SimQueue subQueue = queue.getEncapsulatedQueue ();
    final SimQueueState subQueueState = queueStateHandler.getSubQueueState (0);
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    super.doQueueEvents_SQ_SV_ROEL_U (queue, queueState, queueEventTypes, visitLogsSet);
    if (eventType != null)
      setServerAccessCreditsOnEncQueue (queue, queueState, subQueue, subQueueState, visitLogsSet);
  }

}