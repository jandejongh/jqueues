package nl.jdj.jqueues.r5.extensions.qos;

import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.qos.HOL;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.queues.SimQueuePredictor_FCFS;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link HOL}.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public class SimQueuePredictor_HOL<J extends SimJob, Q extends HOL, P extends Comparable>
extends SimQueuePredictor_FCFS
{
  
  /** Registers a new {@link SimQueueQoSStateHandler} at the object created by super method.
   *
   * @return The object created by the super method with a new registered {@link SimQueueQoSStateHandler}.
   * 
   */
  @Override
  public SimQueueState<SimJob, SimQueue> createQueueState (final SimQueue queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueQoSStateHandler<> (true));
    return queueState;
  }

  @Override
  public boolean isStartArmed (final SimQueue queue, final SimQueueState<SimJob, SimQueue> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return queueState.getJobsInServiceArea ().isEmpty ();
  }
  
  @Override
  protected SimJob getJobToStart
  (final SimQueue queue,
   final SimQueueState<SimJob, SimQueue> queueState)
   throws SimQueuePredictionException
  {
    final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
      (SimQueueQoSStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
    queueStateHandler.updateJobsQoSMap ();
    return ((NavigableMap<P, Set<J>>) queueStateHandler.getJobsQoSMap ()).firstEntry ().getValue ().iterator ().next ();
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (SimQueue queue,
    WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
    SimQueueState<SimJob, SimQueue> queueState,
    Set<SimEntitySimpleEventType.Member> workloadEventTypes,
    Set<JobQueueVisitLog<SimJob, SimQueue>> visitLogsSet)
    throws SimQueuePredictionException, WorkloadScheduleException
  {
    final SimEntitySimpleEventType.Member eventType = (workloadEventTypes.isEmpty ()
      ? null
      : workloadEventTypes.iterator ().next ());
    super.doWorkloadEvents_SQ_SV_ROEL_U (queue, workloadSchedule, queueState, workloadEventTypes, visitLogsSet);
    if (eventType == SimEntitySimpleEventType.ARRIVAL || eventType == SimEntitySimpleEventType.REVOCATION)
    {
      final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
        (SimQueueQoSStateHandler)
          ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
      queueStateHandler.updateJobsQoSMap ();
    }
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final SimQueue queue,
    final SimQueueState<SimJob, SimQueue> queueState,
    final Set<SimEntitySimpleEventType.Member> queueEventTypes,
    final Set<JobQueueVisitLog<SimJob, SimQueue>> visitLogsSet)
    throws SimQueuePredictionException
  {
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    super.doQueueEvents_SQ_SV_ROEL_U (queue, queueState, queueEventTypes, visitLogsSet);
    if (eventType == SimEntitySimpleEventType.DEPARTURE)
    {
      final SimQueueQoSStateHandler<J, Q, P> queueStateHandler =
        (SimQueueQoSStateHandler)
          ((DefaultSimQueueState) queueState).getHandler ("SimQueueQoSHandler");
      queueStateHandler.updateJobsQoSMap ();
    }
  }

}