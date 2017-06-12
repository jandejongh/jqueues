package nl.jdj.jqueues.r5.extensions.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEvent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.enc.AbstractEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link AbstractEncapsulatorSimQueue} and derived queues.
 *
 * @param <Q> The type of queue supported.
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
public abstract class AbstractSimQueuePredictor_Composite_Enc<Q extends AbstractEncapsulatorSimQueue>
extends AbstractSimQueuePredictor_Composite<Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public AbstractSimQueuePredictor_Composite_Enc (final AbstractSimQueuePredictor subQueuePredictor)
  {
    super (Collections.singletonList (subQueuePredictor));
    if (subQueuePredictor == null)
      throw new IllegalArgumentException ();
    this.subQueuePredictor = subQueuePredictor;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE PREDICTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final AbstractSimQueuePredictor subQueuePredictor;
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE PREDICTOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Registers a new {@link SimQueueCompositeStateHandler} at the object created by super method,
   *  creating appropriate empty {@link DefaultSimQueueState}s for the sub-queues.
   * 
   */
  @Override
  public SimQueueState<SimJob, Q> createQueueState (final Q queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    final List<SimQueue> subQueues = new ArrayList (queue.getQueues ());
    if (subQueues.size () != 1)
      throw new IllegalArgumentException ();
    final DefaultSimQueueState subQueueState =
      (DefaultSimQueueState) this.subQueuePredictor.createQueueState (subQueues.get (0), isROEL);
    final Set<DefaultSimQueueState> subQueueStates = new LinkedHashSet<> ();
    queueState.registerHandler (new SimQueueCompositeStateHandler (queue.getQueues (), Collections.singleton (subQueueState)));
    return queueState;
  }

  @Override
  public void updateToTime (final Q queue, final SimQueueState queueState, final double newTime)
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final SimQueue subQueue = queue.getEncapsulatedQueue ();
    final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (0);
    this.subQueuePredictor.updateToTime (subQueue, subQueueState, newTime);
    queueState.setTime (newTime);
  }

  @Override
  public double getNextQueueEventTimeBeyond
  (final Q queue, final SimQueueState<SimJob, Q> queueState, final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  throws SimQueuePredictionException
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    final SimQueue subQueue = queue.getEncapsulatedQueue ();
    final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (0);
    final Set<SimEntitySimpleEventType.Member> subQueueEventTypes = new LinkedHashSet<> ();
    final double subQueueNextEventTime =
      this.subQueuePredictor.getNextQueueEventTimeBeyond (subQueue, subQueueState, subQueueEventTypes);
    if (! Double.isNaN (subQueueNextEventTime))
    {
      if (subQueueEventTypes.size () != 1)
        throw new SimQueuePredictionAmbiguityException ();
      final SimEntitySimpleEventType.Member nextEvent = subQueueEventTypes.iterator ().next ();
      queueEventTypes.add (new SubQueueSimpleEvent (subQueue, null, nextEvent, null, null));
    }
    return subQueueNextEventTime;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final Q queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
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
      queueState.doArrivals (time, arrivals, visitLogsSet); // Takes care of qav.
      if (queueState.getJobs ().contains (job))
      {
        final SimQueue encQueue = (SimQueue) queue.getQueues ().iterator ().next ();
        if (! (job instanceof DefaultSimJob))
          throw new UnsupportedOperationException ();
        ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (encQueue, job.getServiceTime (queue));
        final SubQueueSimpleEvent encQueueEvent =
          new SubQueueSimpleEvent (encQueue, SimQueueSimpleEventType.ARRIVAL, null, job, null); 
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (encQueueEvent)), visitLogsSet);
      }
    }
    else if (eventType == SimQueueSimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present and eligible for revocation.
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
          revokeJobs (time, queue, queueState, revocations, visitLogsSet);
          for (int i = 0; i < queue.getQueues ().size (); i++)
          {
            final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (i);
            if (subQueueState.getJobs ().contains (job))
            {
              final SimQueue subQueue = new ArrayList<SimQueue> (queue.getQueues ()).get (i);
              final SubQueueSimpleEvent subQueueEvent =
                new SubQueueSimpleEvent (subQueue, SimQueueSimpleEventType.REVOCATION, null, job, null);
              doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (subQueueEvent)), visitLogsSet);
              break;
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
      final SimQueue encQueue = (SimQueue) queue.getQueues ().iterator ().next ();
      final SubQueueSimpleEvent encQueueEvent =
        new SubQueueSimpleEvent (encQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, newSac);
      doQueueEvents_SQ_SV_ROEL_U (queue, queueState, new HashSet<> (Collections.singleton (encQueueEvent)), visitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
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
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType instanceof SubQueueSimpleEvent)
    {
      final List<SimQueue> subQueues = new ArrayList<> (queue.getQueues ());
      final SimQueue subQueue = ((SubQueueSimpleEvent) eventType).subQueue;
      final int subQueueIndex = subQueues.indexOf (subQueue);
      final DefaultSimQueueState<SimJob, SimQueue> subQueueState = queueStateHandler.getSubQueueState (subQueueIndex);
      final SimEntitySimpleEventType.Member subQueueWorkloadEvent = ((SubQueueSimpleEvent) eventType).subQueueWorkloadEvent;
      final SimEntitySimpleEventType.Member subQueueQueueEvent = ((SubQueueSimpleEvent) eventType).subQueueQueueEvent;
      final SimJob job = ((SubQueueSimpleEvent) eventType).job;
      final Object argument = ((SubQueueSimpleEvent) eventType).argument;
      // Apply the event at the sub-queue, and capture its visit logs generated.
      final Set<JobQueueVisitLog<SimJob,Q>> subQueueVisitLogsSet = new HashSet<> ();
      try
      {
        if (subQueueWorkloadEvent != null)
        {
          final SimJQEvent subQueueEvent;
          if (subQueueWorkloadEvent == SimQueueSimpleEventType.ARRIVAL)
            subQueueEvent = new SimJQEvent.Arrival<> (job, subQueue, time);
          else if (subQueueWorkloadEvent == SimQueueSimpleEventType.REVOCATION)
            subQueueEvent = new SimJQEvent.Revocation<> (job, subQueue, time, true);
          else if (subQueueWorkloadEvent == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
            subQueueEvent = new SimQueueEvent.ServerAccessCredits<> (subQueue, time, (int) argument);
          else
            throw new RuntimeException ();
          final WorkloadSchedule_SQ_SV_ROEL_U subQueueWorkloadSchedule =
            this.subQueuePredictor.createWorkloadSchedule_SQ_SV_ROEL_U
              (subQueue, new HashSet<> (Collections.singleton (subQueueEvent)));
          this.subQueuePredictor.doWorkloadEvents_SQ_SV_ROEL_U
            (subQueue,
            subQueueWorkloadSchedule,
            subQueueState,
            new HashSet<> (Collections.singleton (subQueueWorkloadEvent)),
            subQueueVisitLogsSet);
        }
        else
          this.subQueuePredictor.doQueueEvents_SQ_SV_ROEL_U
            (subQueue, subQueueState, new HashSet<> (Collections.singleton (subQueueQueueEvent)), subQueueVisitLogsSet);
      }
      catch (WorkloadScheduleException e)
      {
        throw new RuntimeException (e);
      }
      checkSubQueueVisitLogsSet
        (time,
         queue, queueState, visitLogsSet,
         subQueue, subQueueIndex, subQueueState, subQueueVisitLogsSet);
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE STATE SUPPORT METHODS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  protected void dropJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> drops,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  {
    queueState.doExits (time, drops, null, null, null, visitLogsSet);
  }
  
  protected void revokeJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> revokers,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  {
    queueState.doExits (time, null, revokers, null, null, visitLogsSet);
  }
  
  protected void startJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> starters,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException
  {
    queueState.doStarts (time, starters);
  }
  
  protected void departJobs
  (final double time,
   final Q queue,
   final SimQueueState<SimJob, Q> queueState,
   final Set<SimJob> departers,
   final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet)
  throws SimQueuePredictionException
  {
    queueState.doExits (time, null, null, departers, null, visitLogsSet);
  }
  
  protected void checkSubQueueVisitLogsSet
  (
    final double time,
    final Q queue,
    final SimQueueState<SimJob, Q> queueState,
    final Set<JobQueueVisitLog<SimJob, Q>> visitLogsSet,
    final SimQueue subQueue,
    final int subQueueIndex,
    final DefaultSimQueueState<SimJob, SimQueue> subQueueState,
    final Set<JobQueueVisitLog<SimJob, Q>> subQueueVisitLogsSet
  )
    throws SimQueuePredictionException
  {
    final List<SimQueue> subQueues = new ArrayList<> (queue.getQueues ());
    // Check a (true) encapsulated queue for (missed) job starts at this time.
    if (subQueueState.getJobsInServiceAreaMap ().containsKey (time))
    {
      final Set<SimJob> started = subQueueState.getJobsInServiceAreaMap ().get (time);
      for (final SimJob j : started)
        if (! queueState.getJobsInServiceArea ().contains (j))
          // Delegate job has started, but real job hasn't;
          // must be propagated to the composite queue since it is a true encapsulator.
          startJobs (time, queue, queueState, Collections.singleton (j), visitLogsSet);
    }
    // Check (again) a (true) encapsulated queue for (missed) job starts at this time.
    // Note that our first test failed since the job has left the encapsulated queue by now.
    for (JobQueueVisitLog<SimJob,Q> jvl : subQueueVisitLogsSet)
      if (jvl.started
      &&  (queueState.getJobs ().contains (jvl.job) || ! jvl.revoked)
      &&  ! queueState.getJobsInServiceArea ().contains (jvl.job))
        startJobs (time, queue, queueState, Collections.singleton (jvl.job), visitLogsSet);
    // Check the visit logs for drops and departures.
    for (JobQueueVisitLog<SimJob,Q> jvl : subQueueVisitLogsSet)
    {
      if (jvl.dropped)
        dropJobs (time, queue, queueState, Collections.singleton (jvl.job), visitLogsSet);
      else if (jvl.departed)
        departJobs (time, queue, queueState, Collections.singleton (jvl.job), visitLogsSet);
      else if (jvl.revoked)
      {
        /* EMPTY */
      }
      else
        // Job has left due to some other unknown reason? Should specify here!
        throw new UnsupportedOperationException ();
    }
  
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}