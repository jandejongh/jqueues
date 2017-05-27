package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.List;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.parallel.pattern.PatternParallelSimQueues;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite;
import nl.jdj.jqueues.r5.extensions.visitscounter.SimQueueVisitsCounterStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link PatternParallelSimQueues}.
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
public class SimQueuePredictor_Pattern
extends AbstractSimQueuePredictor_Composite<PatternParallelSimQueues>
implements SimQueuePredictor<PatternParallelSimQueues>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public SimQueuePredictor_Pattern (final List<AbstractSimQueuePredictor> subQueuePredictors)
  {
    super (subQueuePredictors);
  }

  /** Registers a new {@link SimQueueVisitsCounterStateHandler} at the object created by super method.
   * 
   */
  @Override
  public SimQueueState<SimJob, PatternParallelSimQueues> createQueueState
  (final PatternParallelSimQueues queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueVisitsCounterStateHandler ());
    return queueState;
  }

  @Override
  public String toString ()
  {
    return "Predictor[Pattern[?]]";
  }

  @Override
  public boolean isStartArmed
  (final PatternParallelSimQueues queue, final SimQueueState<SimJob, PatternParallelSimQueues> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return true;
  }

  @Override
  protected void startJobs
  (final double time,
   final PatternParallelSimQueues queue,
   final SimQueueState<SimJob, PatternParallelSimQueues> queueState,
   final Set<SimJob> starters,
   final Set<JobQueueVisitLog<SimJob, PatternParallelSimQueues>> visitLogsSet)
  throws SimQueuePredictionException
  {
    queueState.doStarts (time, starters);
    final SimQueueVisitsCounterStateHandler queueStateHandler =
      (SimQueueVisitsCounterStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueVisitsCounterStateHandler");
    final int[] pattern = queue.getPattern ();
    if (pattern == null || pattern.length == 0)
      departJobs (time, queue, queueState, starters, visitLogsSet);
    else
      for (final SimJob job : starters)
      {
        if (! (job instanceof DefaultSimJob))
          throw new UnsupportedOperationException ();
        final int modCounter = queueStateHandler.getTotalNumberOfVisits ();
        if (modCounter >= pattern.length)
          throw new IllegalStateException ();
        final int index = pattern[modCounter];
        if (modCounter == pattern.length - 1)
          queueStateHandler.resetTotalNumberOfVisits ();
        else
          queueStateHandler.incTotalNumberOfVisits ();
        final SimQueue subQueue;
        if (index < 0 || index >= queue.getQueues ().size ())
          subQueue = null;
        else
          subQueue = AbstractSimQueueComposite.getQueue (queue.getQueues (), index);
        final SubQueueSimpleEvent subQueueEvent;
        if (subQueue != null)
        {
          // Check whether job did not already leave!
          if (queueState.getJobs ().contains (job))
          {
            ((DefaultSimJob) job).setRequestedServiceTimeMappingForQueue (subQueue, job.getServiceTime (queue));
            subQueueEvent = new SubQueueSimpleEvent (subQueue, SimQueueSimpleEventType.ARRIVAL, null, job, null);
            doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (subQueueEvent), visitLogsSet);
          }
        }
        else
          departJobs (time, queue, queueState, asSet (job), visitLogsSet);
      }
  }
    
}