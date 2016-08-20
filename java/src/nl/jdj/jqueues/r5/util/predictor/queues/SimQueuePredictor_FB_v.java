package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.composite.single.feedback.BlackNumVisitsFeedbackSimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite;
import nl.jdj.jqueues.r5.extensions.visitscounter.SimQueueVisitsCounterStateHandler;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.DefaultSimQueueState;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link BlackNumVisitsFeedbackSimQueue}.
 *
 */
public class SimQueuePredictor_FB_v
extends AbstractSimQueuePredictor_Composite<BlackNumVisitsFeedbackSimQueue>
implements SimQueuePredictor<BlackNumVisitsFeedbackSimQueue>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public SimQueuePredictor_FB_v (final AbstractSimQueuePredictor encQueuePredictor)
  {
    super (Collections.singletonList (encQueuePredictor));
  }

  /** Registers a new {@link SimQueueVisitsCounterStateHandler} at the object created by super method.
   * 
   */
  @Override
  public SimQueueState<SimJob, BlackNumVisitsFeedbackSimQueue> createQueueState
  (final BlackNumVisitsFeedbackSimQueue queue, final boolean isROEL)
  {
    final DefaultSimQueueState queueState = (DefaultSimQueueState) super.createQueueState (queue, isROEL);
    queueState.registerHandler (new SimQueueVisitsCounterStateHandler ());
    return queueState;
  }

  @Override
  public String toString ()
  {
    return "Predictor[FB_?[?]]";
  }

  @Override
  public boolean isStartArmed
  (final BlackNumVisitsFeedbackSimQueue queue, final SimQueueState<SimJob, BlackNumVisitsFeedbackSimQueue> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    return true;
  }

  @Override
  protected void dropJobs
  (final double time,
   final BlackNumVisitsFeedbackSimQueue queue,
   final SimQueueState<SimJob, BlackNumVisitsFeedbackSimQueue> queueState,
   final Set<SimJob> drops,
   final Set<JobQueueVisitLog<SimJob, BlackNumVisitsFeedbackSimQueue>> visitLogsSet)
  {
    super.dropJobs (time, queue, queueState, drops, visitLogsSet);
    final SimQueueVisitsCounterStateHandler queueStateHandler =
      (SimQueueVisitsCounterStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueVisitsCounterStateHandler");
    for (final SimJob job : drops)
      queueStateHandler.removeJob (job);
  }

  @Override
  protected void revokeJobs
  (final double time,
   final BlackNumVisitsFeedbackSimQueue queue,
   final SimQueueState<SimJob, BlackNumVisitsFeedbackSimQueue> queueState,
   final Set<SimJob> revokers,
   final Set<JobQueueVisitLog<SimJob, BlackNumVisitsFeedbackSimQueue>> visitLogsSet)
  {
    super.revokeJobs (time, queue, queueState, revokers, visitLogsSet);
    final SimQueueVisitsCounterStateHandler queueStateHandler =
      (SimQueueVisitsCounterStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueVisitsCounterStateHandler");
    for (final SimJob job : revokers)
      queueStateHandler.removeJob (job);
  }

  @Override
  protected void startJobs
  (final double time,
   final BlackNumVisitsFeedbackSimQueue queue,
   final SimQueueState<SimJob, BlackNumVisitsFeedbackSimQueue> queueState,
   final Set<SimJob> starters,
   final Set<JobQueueVisitLog<SimJob, BlackNumVisitsFeedbackSimQueue>> visitLogsSet)
  throws SimQueuePredictionException
  {
    // Actually, this is always the case, but let's prepare for predictable extensions...
    if (queue.getNumberOfVisits () > 0)
    {
      final SimQueueVisitsCounterStateHandler queueStateHandler =
        (SimQueueVisitsCounterStateHandler)
          ((DefaultSimQueueState) queueState).getHandler ("SimQueueVisitsCounterStateHandler");
      for (final SimJob job : starters)
        if (queueStateHandler.containsJob (job))
        {
          // Revisit due to feedback.
          if (! queueState.getJobs ().contains (job))
            throw new IllegalStateException ();
          final SubQueueSimpleEvent encQueueEvent =
            new SubQueueSimpleEvent (queue.getEncapsulatedQueue (), SimEntitySimpleEventType.ARRIVAL, null, job, null);
          final Set<JobQueueVisitLog<SimJob, BlackNumVisitsFeedbackSimQueue>> encQueueVisitLogsSet = new HashSet<> ();
          doQueueEvents_SQ_SV_ROEL_U (queue, queueState, asSet (encQueueEvent), encQueueVisitLogsSet);
        }
        else
        {
          // First visit.
          queueStateHandler.newJob (job);
          super.startJobs (time, queue, queueState, Collections.singleton (job), visitLogsSet);
        }
      // Check whether job did not already leave!
      for (final SimJob job : starters)
        if (! queueState.getJobs ().contains (job))
          // XXX Why do I need this additional check???
          if (queueStateHandler.containsJob (job))
            queueStateHandler.removeJob (job);
    }
    else
      dropJobs (time, queue, queueState, starters, visitLogsSet);
  }

  @Override
  protected void departJobs
  (final double time,
   final BlackNumVisitsFeedbackSimQueue queue,
   final SimQueueState<SimJob, BlackNumVisitsFeedbackSimQueue> queueState,
   final Set<SimJob> departers,
   final Set<JobQueueVisitLog<SimJob, BlackNumVisitsFeedbackSimQueue>> visitLogsSet)
  throws SimQueuePredictionException
  {
    final SimQueueVisitsCounterStateHandler queueStateHandler =
      (SimQueueVisitsCounterStateHandler)
        ((DefaultSimQueueState) queueState).getHandler ("SimQueueVisitsCounterStateHandler");
    final Set<SimJob> realDeparters = new LinkedHashSet<> ();
    final Set<SimJob> feedbackers = new LinkedHashSet<> ();
    for (final SimJob job : departers)
      if (queueStateHandler.getNumberOfVisits (job) > queue.getNumberOfVisits ())
        throw new IllegalStateException ();
      else if (queueStateHandler.getNumberOfVisits (job) == queue.getNumberOfVisits ())
      {
        realDeparters.add (job);
        queueStateHandler.removeJob (job);
      }
      else
      {
        feedbackers.add (job);
        queueStateHandler.incNumberOfVisits (job);        
      }
    if (! realDeparters.isEmpty ())
      super.departJobs (time, queue, queueState, realDeparters, visitLogsSet);
    if (! feedbackers.isEmpty ())
      startJobs (time, queue, queueState, feedbackers, null);
  }

}