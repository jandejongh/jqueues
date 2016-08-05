package nl.jdj.jqueues.r5.entity.queue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.DefaultVisitsLoggingSimJobQoSFactory;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.listener.SimQueueAccessVacationLogger;
import nl.jdj.jqueues.r5.listener.SimQueueNoWaitArmedLogger;
import nl.jdj.jqueues.r5.listener.SimQueueServerAccessCreditsAvailabilityLogger;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactoryHint;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePrediction_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventList;

/** Default tests for {@link SimQueue}s.
 *
 */
public class DefaultSimQueueTests
{

  public final static int NUMBER_OF_PASSES = 2;
  
  public static <Q extends SimQueue> boolean doSimQueueTests_SQ_SV
    (final Q queue,
     final SimQueuePredictor<Q> predictor,
     final int numberOfJobs,
     final Set<LoadFactoryHint> hints,
     final boolean silent,
     final boolean deadSilent,
     final double accuracy,
     final Set<KnownLoadFactory_SQ_SV> omit)
  throws SimQueuePredictionException
  {
    if (queue == null || predictor == null || numberOfJobs < 0 || queue.getEventList () == null || accuracy < 0)
      throw new IllegalArgumentException ();
    System.out.println ("========== SimQueue Tests [SQ/SV] ==============================================");
    System.out.println (queue);
    System.out.println ("================================================================================");
    final SimEventList<SimEvent> el = queue.getEventList ();
    if ((! (silent || deadSilent)) && (queue instanceof AbstractSimQueue))
      ((AbstractSimQueue) queue).registerStdOutSimQueueListener ();
    final SimQueueAccessVacationLogger qavLogger = new SimQueueAccessVacationLogger ();
    queue.registerSimEntityListener (qavLogger);
    final SimQueueServerAccessCreditsAvailabilityLogger sacLogger = new SimQueueServerAccessCreditsAvailabilityLogger ();
    queue.registerSimEntityListener (sacLogger);
    final SimQueueNoWaitArmedLogger nwaLogger = new SimQueueNoWaitArmedLogger ();
    queue.registerSimEntityListener (nwaLogger);
    for (final KnownLoadFactory_SQ_SV klf : KnownLoadFactory_SQ_SV.values ())
      if (omit == null || ! omit.contains (klf))
        for (int pass = 1; pass <= NUMBER_OF_PASSES; pass++)
        {
          if (! deadSilent)
            System.out.println ("===== Test: " + klf + ", pass " + pass + " =====");
          assert ! queue.isQueueAccessVacation ();
          final boolean predictedInitNwa = predictor.isNoWaitArmed (queue, predictor.createQueueState (queue, true));
          final SimJobFactory jobFactory = new DefaultVisitsLoggingSimJobQoSFactory<> ();
          final NavigableMap<Double, Set<SimEntityEvent>> queueEventsAsMap = new TreeMap<> ();
          final LoadFactory_SQ_SV loadFactory = klf.getLoadFactory ();
          final Set<SimJob> jobs = loadFactory.generate
            (el, false, queue, jobFactory, numberOfJobs, true, 0.0, hints, queueEventsAsMap);
          final Set<SimEntityEvent> queueEventsAsSet = new HashSet<> ();
          for (final Set<SimEntityEvent> queueEventsAtTime : queueEventsAsMap.values ())
            queueEventsAsSet.addAll (queueEventsAtTime);
          final SimQueuePrediction_SQ_SV<Q> prediction = predictor.predict_SQ_SV_ROEL_U (queue, queueEventsAsSet);
          final Map<SimJob, JobQueueVisitLog<SimJob, Q>> predictedJobQueueVisitLogs = prediction.getVisitLogs ();
          final List<Map<Double, Boolean>> predictedQavLogs = prediction.getQueueAccessVacationLog ();
          final List<Map<Double, Boolean>> predictedSacLogs = prediction.getServerAccessCreditsAvailabilityLog ();
          final List<Map<Double, Boolean>> predictedNwaLogs = prediction.getNoWaitArmedLog ();
          el.run ();
          assert el.isEmpty ();
          final Map<SimJob, TreeMap<Double,TreeMap<Integer,JobQueueVisitLog<SimJob, Q>>>>
            actualJobQueueVisitLogs = new HashMap<> ();
          for (final SimJob j : jobs) 
            actualJobQueueVisitLogs.put (j, ((DefaultVisitsLoggingSimJob) j).getVisitLogs ());
          assert predictor.matchVisitLogs_SQ_SV
            (queue, predictedJobQueueVisitLogs, actualJobQueueVisitLogs, accuracy, System.err);
          final List<Map<Double, Boolean>> actualQavLogs = qavLogger.getQueueAccessVacationLog ();
          final List<Map<Double, Boolean>> actualSacLogs = sacLogger.getServerAccessCreditsAvailabilityLog ();
          final List<Map<Double, Boolean>> actualNwaLogs = nwaLogger.getNoWaitArmedLog ();
          // XXX SHOULD DO THE SAME FOR QAV!!
          assert SimQueueAccessVacationLogger.matchQueueAccessVacationLogs
                   (predictedQavLogs, actualQavLogs, accuracy);
          assert SimQueueServerAccessCreditsAvailabilityLogger.matchServerAccessCreditsAvailabilityLogs
                   (predictedSacLogs, actualSacLogs, accuracy);
          assert SimQueueNoWaitArmedLogger.matchNoWaitArmedLogs
                   (predictedNwaLogs, actualNwaLogs, accuracy);
          el.reset ();
        }
      else if (! deadSilent)
        System.out.println ("===== Omitting: " + klf + ".");
    return true;
  }
    
}