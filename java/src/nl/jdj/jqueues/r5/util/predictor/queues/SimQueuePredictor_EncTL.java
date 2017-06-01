package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.single.enc.EncapsulatorTimeLimitSimQueue;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite;
import nl.jdj.jqueues.r5.extensions.composite.SimQueueCompositeStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link EncapsulatorTimeLimitSimQueue}.
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
public class SimQueuePredictor_EncTL
extends AbstractSimQueuePredictor_Composite<EncapsulatorTimeLimitSimQueue>
implements SimQueuePredictor<EncapsulatorTimeLimitSimQueue>
{
  
  final AbstractSimQueuePredictor encQueuePredictor;
  
  public SimQueuePredictor_EncTL (final AbstractSimQueuePredictor encQueuePredictor)
  {
    super (Collections.singletonList (encQueuePredictor));
    this.encQueuePredictor = encQueuePredictor;
  }

  @Override
  public String toString ()
  {
    return "Predictor[EncTL[?]]";
  }

  @Override
  public boolean hasServerAccessCredits
  (final EncapsulatorTimeLimitSimQueue queue,
   final SimQueueState<SimJob, EncapsulatorTimeLimitSimQueue> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    if (queueState == null)
      throw new IllegalArgumentException ();
    return this.encQueuePredictor.hasServerAccessCredits (queue.getEncapsulatedQueue (), queueStateHandler.getSubQueueState (0));
  }

  @Override
  public boolean isStartArmed
  (final EncapsulatorTimeLimitSimQueue queue,
   final SimQueueState<SimJob, EncapsulatorTimeLimitSimQueue> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    final SimQueueCompositeStateHandler queueStateHandler =
      (SimQueueCompositeStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueCompositeHandler");
    if (queueState == null)
      throw new IllegalArgumentException ();
    return this.encQueuePredictor.isStartArmed (queue.getEncapsulatedQueue (), queueStateHandler.getSubQueueState (0));
  }

  private static class ForcedDeparture extends SimEntitySimpleEventType.Member
  {

    private final Set<SimJob> jobs;
    
    public ForcedDeparture (final String name, final Set<SimJob> jobs)
    {
      super (name);
      if (jobs == null || jobs.isEmpty () || jobs.contains (null))
        throw new IllegalArgumentException ();
      this.jobs = jobs;
    }
    
  }
  
  @Override
  public double getNextQueueEventTimeBeyond
  (final EncapsulatorTimeLimitSimQueue queue,
    final SimQueueState<SimJob, EncapsulatorTimeLimitSimQueue> queueState,
    final Set<SimEntitySimpleEventType.Member> queueEventTypes)
    throws SimQueuePredictionException
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double encTime = super.getNextQueueEventTimeBeyond (queue, queueState, queueEventTypes);
    if (queueState.getJobs ().isEmpty ())
      return encTime;
    double schedTime = encTime;
    Set<SimJob> expJobs = null;
    if ((! Double.isInfinite (queue.getMaxWaitingTime ()))
    &&  (! queueState.getJobsInWaitingArea ().isEmpty ())
    &&  (Double.isNaN (schedTime)
         || queueState.getArrivalTimesMap ().get (queueState.getJobsInWaitingAreaOrdered ().iterator ().next ())
            + queue.getMaxWaitingTime () < schedTime))
    {
      schedTime = queueState.getArrivalTimesMap ().get (queueState.getJobsInWaitingAreaOrdered ().iterator ().next ())
                  + queue.getMaxWaitingTime ();
      expJobs = new LinkedHashSet<> ();
      for (final SimJob job : queueState.getJobsInWaitingAreaOrdered ())
        if (queueState.getArrivalTimesMap ().get (job) + queue.getMaxWaitingTime () < schedTime)
          throw new RuntimeException ();
        else if (queueState.getArrivalTimesMap ().get (job) + queue.getMaxWaitingTime () == schedTime)
          expJobs.add (job);
        else if (expJobs.isEmpty ())
          throw new RuntimeException ();
        else
          break;
    }
    if ((! Double.isInfinite (queue.getMaxServiceTime ()))
    &&  (! queueState.getJobsInServiceArea ().isEmpty ())
    &&   (Double.isNaN (schedTime)
          || queueState.getJobsInServiceAreaMap ().firstKey () + queue.getMaxServiceTime () < schedTime))
    {
      schedTime = queueState.getJobsInServiceAreaMap ().firstKey () + queue.getMaxServiceTime ();
      expJobs = new LinkedHashSet<> (queueState.getJobsInServiceAreaMap ().firstEntry ().getValue ());
    }
    if ((! Double.isInfinite (queue.getMaxSojournTime ()))
    &&  (! queueState.getJobs ().isEmpty ())
    &&   (Double.isNaN (schedTime)
          || queueState.getJobArrivalsMap ().firstKey () + queue.getMaxSojournTime () < schedTime))
    {
      schedTime = queueState.getJobArrivalsMap ().firstKey () + queue.getMaxSojournTime ();
      expJobs = new LinkedHashSet<> (queueState.getJobArrivalsMap ().firstEntry ().getValue ());
    }
    if (expJobs != null)
    {
      queueEventTypes.clear ();
      queueEventTypes.add (new ForcedDeparture ("FORCED_DEPARTURE", expJobs));
    }
    return schedTime;
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final EncapsulatorTimeLimitSimQueue queue,
   final SimQueueState<SimJob, EncapsulatorTimeLimitSimQueue> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, EncapsulatorTimeLimitSimQueue>> visitLogsSet)
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
    if (eventType != null && (eventType instanceof ForcedDeparture))
    {
      final Set<SimJob> jobs = ((ForcedDeparture) eventType).jobs;
      for (final SimJob job : jobs)
      {
          final SimQueue encQueue = (SimQueue) queue.getQueues ().iterator ().next ();
          if (! (job instanceof DefaultSimJob))
            throw new UnsupportedOperationException ();
          final SubQueueSimpleEvent encQueueEvent =
            new SubQueueSimpleEvent (encQueue, SimQueueSimpleEventType.REVOCATION, null, job, true);
          doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (encQueueEvent), /* visitLogsSet */ new HashSet<> ());
      }
      departJobs (time, queue, queueState, jobs, visitLogsSet);
    }
    else
      super.doQueueEvents_SQ_SV_ROEL_U (queue, queueState, queueEventTypes, visitLogsSet);
  }

}