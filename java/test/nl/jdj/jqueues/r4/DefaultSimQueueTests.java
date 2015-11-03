package nl.jdj.jqueues.r4;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.util.jobfactory.DefaultVisitsLoggingSimJob;
import nl.jdj.jqueues.r4.util.jobfactory.DefaultVisitsLoggingSimJobFactory;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.loadfactory.pattern.LoadFactorySingleQueueSingleVisit_01;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictionAmbiguityException;
import nl.jdj.jqueues.r4.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r4.util.schedule.QueueExternalEvent;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class DefaultSimQueueTests
{

  public final static double ACCURACY = 1.0e-12;
  
  public final static int NUMBER_OF_PASSES = 2;
  
  public static <Q extends SimQueue> boolean doTest_01
    (final Q queue,
     final SimQueuePredictor<DefaultVisitsLoggingSimJob, Q> predictor,
     final int numberOfJobs,
     final boolean silent)
  throws SimQueuePredictionAmbiguityException
  {
    if (queue == null || predictor == null || numberOfJobs < 0 || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    System.out.println ("================================================================================");
    System.out.println (queue);
    System.out.println ("================================================================================");
    final SimEventList<SimEvent> el = queue.getEventList ();
    if ((! silent) && (queue instanceof AbstractSimQueue))
      ((AbstractSimQueue) queue).registerStdOutSimQueueListener ();
    for (int pass = 1; pass <= NUMBER_OF_PASSES; pass++)
    {
      if (! silent)
        System.out.println ("===== PASS " + pass + " =====");
      final SimJobFactory<DefaultVisitsLoggingSimJob, Q> jobFactory
        = new DefaultVisitsLoggingSimJobFactory<> ();
      final TreeMap<Double, Set<QueueExternalEvent<DefaultVisitsLoggingSimJob, Q>>> queueExternalEvents
        = new TreeMap<> ();
      final Set<DefaultVisitsLoggingSimJob> jobs = 
        (new LoadFactorySingleQueueSingleVisit_01 ()).generate
          (el, false, queue, jobFactory, numberOfJobs, true, 0.0, queueExternalEvents);
      final Map<DefaultVisitsLoggingSimJob, JobQueueVisitLog<DefaultVisitsLoggingSimJob, Q>> predictedJobQueueVisitLogs
        = predictor.predictUniqueJobQueueVisitLogs_SingleVisit (queue, queueExternalEvents);
      el.run ();
      if (! el.isEmpty ())
        return false;
      final Map<DefaultVisitsLoggingSimJob, TreeMap<Double,TreeMap<Integer,JobQueueVisitLog<DefaultVisitsLoggingSimJob, Q>>>>
        actualJobQueueVisitLogs = new HashMap<> ();
      for (final DefaultVisitsLoggingSimJob j : jobs) 
        actualJobQueueVisitLogs.put (j, j.getVisitLogs ());
      if (! predictor.matchUniqueJobQueueVisitLogs_SingleVisit
        (queue, predictedJobQueueVisitLogs, actualJobQueueVisitLogs, DefaultSimQueueTests.ACCURACY))
        return false;
      el.reset ();
    }
    return true;
  }
    
}