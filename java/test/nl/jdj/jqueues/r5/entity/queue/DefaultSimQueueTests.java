package nl.jdj.jqueues.r5.entity.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r5.entity.job.visitslogging.DefaultVisitsLoggingSimJobFactory;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictionException;
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
    for (final KnownLoadFactory_SQ_SV klf : KnownLoadFactory_SQ_SV.values ())
      if (omit == null || ! omit.contains (klf))
        for (int pass = 1; pass <= NUMBER_OF_PASSES; pass++)
        {
          if (! deadSilent)
            System.out.println ("===== Test: " + klf + ", pass " + pass + " =====");
          final SimJobFactory jobFactory = new DefaultVisitsLoggingSimJobFactory<> ();
          final NavigableMap<Double, Set<SimEntityEvent>> queueEvents = new TreeMap<> ();
          final LoadFactory_SQ_SV loadFactory = klf.getLoadFactory ();
          final Set<SimJob> jobs = loadFactory.generate
            (el, false, queue, jobFactory, numberOfJobs, true, 0.0, queueEvents);
          final Map<SimJob, JobQueueVisitLog<SimJob, Q>> predictedJobQueueVisitLogs
            = predictor.predictVisitLogs_SQ_SV_U (queue, queueEvents);
          el.run ();
          assert el.isEmpty ();
          final Map<SimJob, TreeMap<Double,TreeMap<Integer,JobQueueVisitLog<SimJob, Q>>>>
            actualJobQueueVisitLogs = new HashMap<> ();
          for (final SimJob j : jobs) 
            actualJobQueueVisitLogs.put (j, ((DefaultVisitsLoggingSimJob) j).getVisitLogs ());
          assert predictor.matchVisitLogs_SQ_SV
            (queue, predictedJobQueueVisitLogs, actualJobQueueVisitLogs, accuracy, System.err);
          el.reset ();
          }
    return true;
  }
    
}