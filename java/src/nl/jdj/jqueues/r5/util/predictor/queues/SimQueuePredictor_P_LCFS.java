package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadSchedule_SQ_SV_ROEL_U;

/** A {@link SimQueuePredictor} for {@link P_LCFS}.
 * 
 */
public class SimQueuePredictor_P_LCFS
extends SimQueuePredictor_Preemptive<P_LCFS>
{

  protected SimJob getExecutingJob (final P_LCFS queue, final SimQueueState<SimJob, P_LCFS> queueState)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (queueState.getJobsInServiceArea ().isEmpty ())
      return null;
    else
    {
      final Set<SimJob> lastStartedJobs = queueState.getJobsInServiceAreaMap ().lastEntry ().getValue ();
      final List<SimJob> lastStartedJobsAsList = new ArrayList<> (lastStartedJobs);
      return lastStartedJobsAsList.get (lastStartedJobsAsList.size () - 1);
    }
  }
  
  @Override
  public double getNextQueueEventTimeBeyond
  (final P_LCFS queue,
   final SimQueueState<SimJob, P_LCFS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes)
  {
    if ( queue == null
      || queueState == null
      || queueEventTypes == null)
      throw new IllegalArgumentException ();
    queueEventTypes.clear ();
    final double time = queueState.getTime ();
    if (queueState.getRemainingServiceMap ().isEmpty ())
      return Double.NaN;
    if (Double.isNaN (time))
      throw new IllegalStateException ();
    final SimJob executingJob = getExecutingJob (queue, queueState);
    final double rs = queueState.getJobRemainingServiceTimeMap ().get (executingJob);
    if (rs < 0)
      throw new RuntimeException ();
    queueEventTypes.add (SimEntitySimpleEventType.DEPARTURE);
    return time + rs;
  }

  @Override
  public void doWorkloadEvents_SQ_SV_ROEL_U
  (final P_LCFS queue,
   final WorkloadSchedule_SQ_SV_ROEL_U workloadSchedule,
   final SimQueueState<SimJob, P_LCFS> queueState,
   final Set<SimEntitySimpleEventType.Member> workloadEventTypes,
   final Set<JobQueueVisitLog<SimJob, P_LCFS>> visitLogsSet)
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
    final SimJob executingJob = getExecutingJob (queue, queueState);
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
      // Takes care of dropping jobs during queue-acces vacations!
      queueState.doArrivals (time, arrivals, visitLogsSet);
      if ((! queueState.isQueueAccessVacation ()) && queueState.getServerAccessCredits () >= 1)
      {
        queueState.doStarts (time, arrivals);
        if (executingJob != null)
          preemptJob (queue, queueState, executingJob, visitLogsSet);
      }
    }
    else if (eventType == SimEntitySimpleEventType.REVOCATION)
    {
      final SimJob job =
        workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).entrySet ().iterator ().next ().getKey ();
      // Check whether job is actually present.
      if (queueState.getJobs ().contains (job))
      {
        final boolean interruptService =
          workloadSchedule.getJobRevocationsMap_SQ_SV_ROEL_U ().get (time).get (job);
        // Make sure we do not revoke an executing job without the interruptService flag.
        if (interruptService || ! queueState.getJobsInServiceArea ().contains (job))
        {
          final Set<SimJob> revocations = new HashSet<> ();
          revocations.add (job);
          queueState.doExits (time, null, revocations, null, null, visitLogsSet);
        }
      }
    }
    else if (eventType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS)
    {
      final int oldSac = queueState.getServerAccessCredits ();
      final int newSac = workloadSchedule.getServerAccessCreditsMap_SQ_SV_ROEL_U ().get (time);
      queueState.setServerAccessCredits (time, newSac);
      if (oldSac == 0 && newSac > 0 && ! queueState.getJobsInWaitingArea ().isEmpty ())
      {
        // Only start the waiting job that arrived last.
        final Set<SimJob> starters = new LinkedHashSet<> ();
        final List<SimJob> waiters = new ArrayList<> (queueState.getJobsInWaitingAreaOrdered ());
        final SimJob youngestWaiter = waiters.get (waiters.size () - 1);
        if (executingJob == null
          || queueState.getArrivalTimesMap ().get (youngestWaiter) >= queueState.getArrivalTimesMap ().get (executingJob))
        {
          starters.add (youngestWaiter);
          queueState.doStarts (time, starters);
          if (executingJob != null)
            preemptJob (queue, queueState, executingJob, visitLogsSet);
        }
      }
    }
    else
      throw new RuntimeException ();
    if (eventType != null)
      workloadEventTypes.remove (eventType);
  }

  @Override
  public void doQueueEvents_SQ_SV_ROEL_U
  (final P_LCFS queue,
   final SimQueueState<SimJob, P_LCFS> queueState,
   final Set<SimEntitySimpleEventType.Member> queueEventTypes,
   final Set<JobQueueVisitLog<SimJob, P_LCFS>> visitLogsSet)
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
    final SimEntitySimpleEventType.Member eventType = (queueEventTypes.isEmpty ()
      ? null
      : queueEventTypes.iterator ().next ());
    if (eventType == null)
    {
      /* NOTHING (LEFT) TO DO */      
    }
    else if (eventType == SimEntitySimpleEventType.DEPARTURE)
    {
      final Set<SimJob> departures = new HashSet<> ();
      final SimJob departingJob = getExecutingJob (queue, queueState);
      departures.add (departingJob);
      queueState.doExits (time, null, null, departures, null, visitLogsSet);
      // Abuse getExecutingJob here in order to figure out what the NEXT job to execute WOULD BE (if any)
      // in absence of jobs starting from the waiting area.
      // We need to be careful NOT to preempt this job later on;
      // we haven't decided yet on it being served for the next chunk.
      final SimJob executingJob = getExecutingJob (queue, queueState);
      // Find the "youngest eligible waiter" from the waiting area.
      final SimJob youngestWaiter;
      if ((! queueState.getJobsInWaitingArea ().isEmpty ())
        && queueState.getServerAccessCredits () > 0)
      {
        // Only start the waiting job that arrived last.
        final List<SimJob> waiters = new ArrayList<> (queueState.getJobsInWaitingAreaOrdered ());
        youngestWaiter = waiters.get (waiters.size () - 1);
      }
      else
        youngestWaiter = null;
      // Decide whether to start the "youngest eligible waiter".
      if (youngestWaiter != null)
      {
        if (executingJob == null
          || queueState.getArrivalTimesMap ().get (youngestWaiter) >= queueState.getArrivalTimesMap ().get (executingJob))
        {
          final Set<SimJob> starters = new LinkedHashSet<> ();
          starters.add (youngestWaiter);
          queueState.doStarts (time, starters);  
        }
      }
    }
    else
      throw new RuntimeException ();
    queueEventTypes.remove (eventType);
  }  
  
  @Override
  public void updateToTime (final P_LCFS queue, final SimQueueState queueState, final double newTime)
  {
    if (queue == null || queueState == null)
      throw new IllegalArgumentException ();
    if (Double.isNaN (newTime))
      throw new IllegalArgumentException ();
    final double oldTime = queueState.getTime ();
    if (! Double.isNaN (oldTime))
    {
      final double dT = newTime - oldTime;
      if (dT < 0)
        throw new RuntimeException ();
      final Map<SimJob, Double> rsTimeMap = queueState.getJobRemainingServiceTimeMap ();
      final NavigableMap<Double,List<SimJob>> rsMap = queueState.getRemainingServiceMap ();
      if (dT > 0 && ! rsTimeMap.isEmpty ())
      { 
        final SimJob executingJob = getExecutingJob (queue, queueState);
        final double dS = dT;
        final double oldRs = rsTimeMap.get (executingJob);
        final double newRs =  oldRs - dS;
        rsTimeMap.put (executingJob, newRs);
        if (! rsMap.containsKey (oldRs))
          throw new IllegalStateException ();
        if (rsMap.get (oldRs).size () == 1)
        {
          rsMap.put (newRs, rsMap.get (oldRs));
          rsMap.remove (oldRs);
        }
        else
        {
          rsMap.get (oldRs).remove (0);
          rsMap.put (newRs, new ArrayList<> ());
          rsMap.get (newRs).add (executingJob);
        }
      }
    }
    queueState.setTime (newTime);
  }

}