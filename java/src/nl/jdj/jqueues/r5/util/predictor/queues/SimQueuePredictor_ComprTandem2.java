package nl.jdj.jqueues.r5.util.predictor.queues;

import nl.jdj.jqueues.r5.extensions.composite.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r5.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r5.event.SimQueueServerAccessCreditsEvent;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link BlackCompressedTandem2SimQueue}.
 *
 * @param <Q> The type of queue supported.
 * 
 */
public class SimQueuePredictor_ComprTandem2<Q extends  BlackCompressedTandem2SimQueue>
extends AbstractSimQueuePredictor_Composite<Q>
{

  private final AbstractSimQueuePredictor waitQueuePredictor;
  
  private final AbstractSimQueuePredictor serveQueuePredictor;
  
  private static List<AbstractSimQueuePredictor> asList (final AbstractSimQueuePredictor p1, final AbstractSimQueuePredictor p2)
  {
    if (p1 == null || p2 == null)
      throw new IllegalArgumentException ();
    final List<AbstractSimQueuePredictor> list = new ArrayList<> ();
    list.add (p1);
    list.add (p2);
    return list;
  }
  
  public SimQueuePredictor_ComprTandem2
  (final AbstractSimQueuePredictor waitQueuePredictor, final AbstractSimQueuePredictor serveQueuePredictor)
  {
    super (asList (waitQueuePredictor, serveQueuePredictor));
    this.waitQueuePredictor = waitQueuePredictor;
    this.serveQueuePredictor = serveQueuePredictor;
  }

  private SimQueue getWaitQueue (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Iterator<? extends SimQueue> i_queues = queue.getQueues ().iterator ();
    return i_queues.next ();
  }
  
  private SimQueue getServeQueue (final Q queue)
  {
    if (queue == null)
      throw new IllegalArgumentException ();
    final Iterator<? extends SimQueue> i_queues = queue.getQueues ().iterator ();
    i_queues.next ();
    return i_queues.next ();
  }

  private DefaultSimQueueState getWaitQueueState (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    return queueStateHandler.getSubQueueState (0);
  }
  
  private DefaultSimQueueState getServeQueueState (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    return queueStateHandler.getSubQueueState (1);
  }
  
  @Override
  public boolean isNoWaitArmed (final Q queue, final SimQueueState<SimJob, Q> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return this.serveQueuePredictor.isNoWaitArmed (getServeQueue (queue), getServeQueueState (queue, queueState));
  }

  private class SimQueueAndSimQueueState
  {
    public final SimQueue queue;
    public final DefaultSimQueueState queueState;
    public SimQueueAndSimQueueState (final SimQueue queue, final DefaultSimQueueState simQueueState)
    {
      this.queue = queue;
      this.queueState = simQueueState;
    }
  }
  
  @Override
  public SimQueueState<SimJob, Q> createQueueState (Q queue, boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    ((DefaultSimQueueState) getWaitQueueState (queue, queueState)).registerPostStartHook
      (this::waitQueuePostStartHook, new SimQueueAndSimQueueState (queue, queueState));
    return queueState;
  }

  private Set<JobQueueVisitLog<SimJob, Q>> cachedVisitLogsSet;
  
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
    this.cachedVisitLogsSet = visitLogsSet;
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
    else if (eventType == SimEntitySimpleEventType.ARRIVAL)
    {
      final SimJob job = workloadSchedule.getJobArrivalsMap_SQ_SV_ROEL_U ().get (time);
      final Set<SimJob> arrivals = new HashSet<> ();
      arrivals.add (job);
      queueState.doArrivals (time, arrivals, visitLogsSet); // Takes care of qav.
      if (queueState.getJobs ().contains (job))
      {
        // Let the job arrive immediately and unconditionally arrive at the waitQueue.
        if (! (job instanceof DefaultSimJob))
          throw new UnsupportedOperationException ();
        final SimQueue waitQueue = getWaitQueue (queue);
        ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (waitQueue, job.getServiceTime (queue));
        final SubQueueSimpleEvent waitQueueEvent =
          new SubQueueSimpleEvent (waitQueue, SimEntitySimpleEventType.ARRIVAL, null, job, null);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (waitQueueEvent), visitLogsSet);
      }
    }
    else if (eventType == SimEntitySimpleEventType.REVOCATION)
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
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
          for (int i = 0; i < queue.getQueues ().size (); i++)
          {
            final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (i);
            if (subQueueState.getJobs ().contains (job))
            {
              final SimQueue subQueue = new ArrayList<SimQueue> (queue.getQueues ()).get (i);
              final SubQueueSimpleEvent subQueueEvent =
                new SubQueueSimpleEvent (subQueue, SimEntitySimpleEventType.REVOCATION, null, job, null);
              doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (subQueueEvent), visitLogsSet);
              break;
            }
          }
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      // System.err.println ("SimQueuePredictor_ComprTandem2; t=" + time + ": Setting sac on " + queue + " to " + newSac + ".");
      queueState.setServerAccessCredits (time, newSac);
      final SimQueue waitQueue = getWaitQueue (queue);
      if (oldSac == 0
          && newSac > 0
          && this.serveQueuePredictor.isNoWaitArmed (getServeQueue (queue), getServeQueueState (queue, queueState)))
      {
        // System.err.println ("SimQueuePredictor_ComprTandem2; t=" + time + ": Setting sac on " + waitQueue + " to " + 1 + ".");
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 1);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (waitQueueSacEvent), visitLogsSet);
        newSac = queueState.getServerAccessCredits ();
      }
      else if (oldSac > 0 && newSac == 0)
      {
        // System.err.println ("SimQueuePredictor_ComprTandem2; t=" + time + ": Setting sac on " + waitQueue + " to " + 0 + ".");
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 0);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (waitQueueSacEvent), visitLogsSet);      
      }
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
    this.cachedVisitLogsSet = visitLogsSet;
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
      final DefaultSimQueueState subQueueState = queueStateHandler.getSubQueueState (subQueueIndex);
      final AbstractSimQueuePredictor subQueuePredictor = this.subQueuePredictors.get (subQueueIndex);
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
          final SimEntityEvent subQueueEvent;
          if (subQueueWorkloadEvent == SimEntitySimpleEventType.ARRIVAL)
            subQueueEvent = new SimQueueJobArrivalEvent (job, subQueue, time);
          else if (subQueueWorkloadEvent == SimEntitySimpleEventType.REVOCATION)
            subQueueEvent = new SimQueueJobRevocationEvent (job, subQueue, time, true);
          else if (subQueueWorkloadEvent == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
            subQueueEvent = new SimQueueServerAccessCreditsEvent (subQueue, time, (Integer) argument);
          else
            throw new RuntimeException ();
          final WorkloadSchedule_SQ_SV_ROEL_U subQueueWorkloadSchedule =
            subQueuePredictor.createWorkloadSchedule_SQ_SV_ROEL_U (subQueue, Collections.singleton (subQueueEvent));
          subQueuePredictor.doWorkloadEvents_SQ_SV_ROEL_U
            (subQueue,
            subQueueWorkloadSchedule,
            subQueueState,
            asSet (subQueueWorkloadEvent),
            subQueueVisitLogsSet);
        }
        else
          subQueuePredictor.doQueueEvents_SQ_SV_ROEL_U
            (subQueue, subQueueState, asSet (subQueueQueueEvent), subQueueVisitLogsSet);
      }
      catch (WorkloadScheduleException e)
      {
        throw new RuntimeException (e);
      }
      // Check the visit logs for drops and departures.
      for (JobQueueVisitLog<SimJob,Q> jvl : subQueueVisitLogsSet)
        if (jvl.dropped)
          queueState.doExits (time, Collections.singleton (jvl.job), null, null, null, visitLogsSet);
        else if (jvl.departed)
          queueState.doExits (time, null, null, Collections.singleton (jvl.job), null, visitLogsSet);
      // Reassess the SAC value on the wait queue.
      final SimQueue waitQueue = getWaitQueue (queue);
      final SimQueue serveQueue = getServeQueue (queue);
      final int sac = queueState.getServerAccessCredits ();
      final int waitQueueSac = getWaitQueueState (queue, queueState).getServerAccessCredits ();
      final DefaultSimQueueState serveQueueState = getServeQueueState (queue, queueState);
      if (sac > 0 && this.serveQueuePredictor.isNoWaitArmed (serveQueue, serveQueueState) && waitQueueSac == 0)
      {
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 1);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (waitQueueSacEvent), visitLogsSet);
      }
      else if ((sac == 0 || ! this.serveQueuePredictor.isNoWaitArmed (serveQueue, serveQueueState)) && waitQueueSac > 0)
      {
        final SubQueueSimpleEvent waitQueueSacEvent =
          new SubQueueSimpleEvent (waitQueue, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS, null, null, (Integer) 0);
        doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (waitQueueSacEvent), visitLogsSet);        
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      queueEventTypes.remove (eventType);
  }

  protected void waitQueuePostStartHook (final double time, final Set<SimJob> starters, final Object userData)
    throws SimQueuePredictionException
  {
    if (starters == null || starters.size () != 1 || userData == null)
      throw new RuntimeException ();
    final SimJob job = starters.iterator ().next ();
    final Q queue = (Q) ((SimQueueAndSimQueueState) userData).queue;
    final DefaultSimQueueState queueState = ((SimQueueAndSimQueueState) userData).queueState;
    if (queue == null || queueState == null)
      throw new RuntimeException ();
    final SimQueue waitQueue = getWaitQueue (queue);
    final SimQueue serveQueue = getServeQueue (queue);
    final DefaultSimQueueState waitQueueState = getWaitQueueState (queue, queueState);
    final DefaultSimQueueState serveQueueState = getServeQueueState (queue, queueState);
    // Remove the job from the wait queue (as revocations, but we will not record it anyway).
    waitQueueState.doExits (time, null, Collections.singleton (job), null, null, null);
    // Let the job start on the main queue.
    queueState.doStarts (time, starters);
    //System.err.println ("SimQueuePredictor_ComprTandem2; t=" + time + ": Starting job " + job + " on " + queue
    //  + ", remaining SAC=" + queueState.getServerAccessCredits () + ".");
    // Check whether job did not already leave!
    if (queueState.getJobs ().contains (job))
    {
      // Let the job arrive at the serve queue, but his time, use the predictor.
      if (! (job instanceof DefaultSimJob))
        throw new UnsupportedOperationException ();
      ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (serveQueue, job.getServiceTime (queue));
      final SubQueueSimpleEvent serveQueueEvent =
        new SubQueueSimpleEvent (serveQueue, SimEntitySimpleEventType.ARRIVAL, null, job, null);
      doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (serveQueueEvent), this.cachedVisitLogsSet);
    }
  }
  
}