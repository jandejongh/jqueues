/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.entity.jq.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.job.SimJobFactory;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.DefaultVisitsLoggingSimJob;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.DefaultVisitsLoggingSimJobQoSFactory;
import org.javades.jqueues.r5.entity.jq.job.visitslogging.JobQueueVisitLog;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import org.javades.jqueues.r5.listener.SimQueueAccessVacationLogger;
import org.javades.jqueues.r5.listener.SimQueueServerAccessCreditsAvailabilityLogger;
import org.javades.jqueues.r5.listener.SimQueueStartArmedLogger;
import org.javades.jqueues.r5.listener.StdOutSimQueueListener;
import org.javades.jqueues.r5.util.loadfactory.LoadFactoryHint;
import org.javades.jqueues.r5.util.loadfactory.LoadFactory_SQ_SV;
import org.javades.jqueues.r5.util.loadfactory.pattern.KnownLoadFactory_SQ_SV;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictionException;
import org.javades.jqueues.r5.util.predictor.SimQueuePrediction_SQ_SV;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventList;

/** Default (static) tests for {@link SimQueue}s.
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
public class DefaultSimQueueTests
{

  /** Prevents instantiation.
   * 
   */
  private DefaultSimQueueTests ()
  {
  }
  
  public final static int NUMBER_OF_PASSES = 2;  
  
  public static <Q extends SimQueue> boolean doSimQueueTests_SQ_SV
    (final Q queue,
     final SimQueuePredictor<Q> predictor,
     final SimQueue predictorQueue,
     final int numberOfJobs,
     final Set<LoadFactoryHint> hints,
     final boolean silent,
     final boolean deadSilent,
     final double accuracy,
     final Set<KnownLoadFactory_SQ_SV> omit,
     final Set<KnownLoadFactory_SQ_SV> restrict,
     final String message)
  throws SimQueuePredictionException
  {
    if (queue == null
    || (predictor == null && predictorQueue == null)
    || numberOfJobs < 0
    || queue.getEventList () == null
    || (predictorQueue != null && predictorQueue.getEventList () != queue.getEventList ())
    || accuracy < 0
    || (omit != null && restrict != null && ! Collections.disjoint (omit, restrict)))
      throw new IllegalArgumentException ();
    if (predictor != null && predictorQueue != null)
    {
      return
        doSimQueueTests_SQ_SV
          (queue, predictor, null, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message)
     && doSimQueueTests_SQ_SV
          (queue, null, predictorQueue, numberOfJobs, hints, silent, deadSilent, accuracy, omit, restrict, message);
    }
    System.out.print ("SimQueue Test [SQ/SV]: ");
    if (predictorQueue == null)
      System.out.print (queue + " == " + predictor);
    else
      System.out.print (queue + " == " + predictorQueue);
    if (message != null)
      System.out.println (" [" + message + "]");
    else
      System.out.println ();
    final SimEventList<SimEvent> el = queue.getEventList ();
    // If requested, register some listeners that will generate some output...
    if (! (silent || deadSilent))
    {
      final StdOutSimQueueListener listener = new StdOutSimQueueListener ();
      listener.setOnlyResetsAndUpdatesAndStateChanges (true);
      queue.registerSimEntityListener (listener);
      if (queue instanceof SimQueueComposite)
        for (final SimQueue subQueue : (Set<SimQueue>) ((SimQueueComposite) queue).getQueues ())
          subQueue.registerSimEntityListener (listener);
    }
    // Create the loggers (qav/sac/sta) for the queue, and register them as listeners.
    final SimQueueAccessVacationLogger qavLogger = new SimQueueAccessVacationLogger ();
    queue.registerSimEntityListener (qavLogger);
    final SimQueueServerAccessCreditsAvailabilityLogger sacLogger = new SimQueueServerAccessCreditsAvailabilityLogger ();
    queue.registerSimEntityListener (sacLogger);
    final SimQueueStartArmedLogger staLogger = new SimQueueStartArmedLogger ();
    queue.registerSimEntityListener (staLogger);
    // Create the loggers (qav/sac/sta) for the predictorQueue (even if non-present).
    final SimQueueAccessVacationLogger predictorQueueQavLogger
      = new SimQueueAccessVacationLogger ();
    final SimQueueServerAccessCreditsAvailabilityLogger predictorQueueSacLogger =
      new SimQueueServerAccessCreditsAvailabilityLogger ();
    final SimQueueStartArmedLogger predictorQueueStaLogger
      = new SimQueueStartArmedLogger ();
    // Register the loggers (qav/sac/sta) for the predictorQueue if non-present.
    if (predictorQueue != null)
    {
      predictorQueue.registerSimEntityListener (predictorQueueQavLogger);
      predictorQueue.registerSimEntityListener (predictorQueueSacLogger);
      predictorQueue.registerSimEntityListener (predictorQueueStaLogger);
    }
    // Loop over (1) the known load factories and (2) the consequetive passes.
    for (final KnownLoadFactory_SQ_SV klf : KnownLoadFactory_SQ_SV.values ())
      if ((omit == null || ! omit.contains (klf))
       && (restrict == null || restrict.contains (klf)))
        for (int pass = 1; pass <= NUMBER_OF_PASSES; pass++)
        {
          if (! deadSilent)
            System.out.println ("===== Test: " + klf + ", pass " + pass + " =====");
          assert ! queue.isQueueAccessVacation ();
          // Make sure we use the right 'type' of job.
          final SimJobFactory jobFactory = new DefaultVisitsLoggingSimJobQoSFactory<> ();
          final NavigableMap<Double, Set<SimJQEvent>> queueEventsAsMap = new TreeMap<> ();
          final LoadFactory_SQ_SV loadFactory = klf.getLoadFactory ();
          // Generate the load; events will be scheduled on the event list for queue, but also put into queueEventsAsMap.
          final Set<SimJob> jobs = loadFactory.generate
            (el, false, queue, jobFactory, numberOfJobs, true, 0.0, hints, queueEventsAsMap);
          // Create a set holding the events in proper order.
          final Set<SimJQEvent> queueEventsAsSet = new LinkedHashSet<> ();
          for (final Set<SimJQEvent> queueEventsAtTime : queueEventsAsMap.values ())
            queueEventsAsSet.addAll (queueEventsAtTime);
          // Prediction objects; either created by predictor or by running a simulation with predictorQueue.
          final Map<SimJob, JobQueueVisitLog<SimJob, Q>> predictedJobQueueVisitLogs;
          final List<Map<Double, Boolean>> predictedQavLogs;
          final List<Map<Double, Boolean>> predictedSacLogs;
          final List<Map<Double, Boolean>> predictedStaLogs;
          // The actual data to be obtained from running a simulation with queue.
          final Map<SimJob, TreeMap<Double,TreeMap<Integer,JobQueueVisitLog<SimJob, Q>>>> actualJobQueueVisitLogs
            = new HashMap<> ();
          final List<Map<Double, Boolean>> actualQavLogs;
          final List<Map<Double, Boolean>> actualSacLogs;
          final List<Map<Double, Boolean>> actualStaLogs;
          if (predictor != null)
          {
            // Use predictor; note that this does not disturb the event list.
            // Hence we can invoke the predictor first.
            // Also, the prediction object returned is not altered here or later, so we do not need to make a copy.
            final SimQueuePrediction_SQ_SV<Q> prediction = predictor.predict_SQ_SV_ROEL_U (queue, queueEventsAsSet);
            predictedJobQueueVisitLogs = prediction.getVisitLogs ();
            predictedQavLogs = prediction.getQueueAccessVacationLog ();
            predictedSacLogs = prediction.getServerAccessCreditsAvailabilityLog ();
            predictedStaLogs = prediction.getStartArmedLog ();
            // Run the simulation with queue.
            el.run ();
            assert el.isEmpty ();
            // Note: We reset at a finite time, and workloads should not and queues MUST NEVER schedule at +/- infinity.
            // [This holds even in the presence of jobs with infinite service-time requirement.]
            assert Double.isFinite (el.getTime ());
            // Gather the (actual) logs.
            for (final SimJob j : jobs) 
              actualJobQueueVisitLogs.put (j, ((DefaultVisitsLoggingSimJob) j).getVisitLogs ());
            actualQavLogs = qavLogger.getQueueAccessVacationLog ();
            actualSacLogs = sacLogger.getServerAccessCreditsAvailabilityLog ();
            actualStaLogs = staLogger.getStartArmedLog ();
          }
          else if (predictorQueue != null)
          {
            // Use predictorQueue.
            // We first run the simulation with queue, because events are already scheduled on the event list.
            // But make sure we obtain a copy of them, and make the copied events use predictorQueue instead of queue.
            final Set<SimJQEvent> predictorQueueEvents = new LinkedHashSet<> ();
            for (final SimJQEvent e : queueEventsAsSet)
              predictorQueueEvents.add (e.copyForQueueAndJob (predictorQueue, null));
            // Set the requested service time from predictorQueue for each job to that requested from queue.
            for (final SimJob j : jobs)
              ((DefaultVisitsLoggingSimJob) j).setRequestedServiceTimeMappingForQueue (predictorQueue, j.getServiceTime (queue));
            // Run the simulation with queue.
            el.run ();
            assert el.isEmpty ();
            // Note: We reset at a finite time, and workloads should not and queues MUST NEVER schedule at +/- infinity.
            // [This holds even in the presence of jobs with infinite service-time requirement.]
            assert Double.isFinite (el.getTime ());
            // Store the visit logs and reset the visit-logging on all jobs (in preparation of another simulation).
            for (final SimJob j : jobs)
            {
              actualJobQueueVisitLogs.put (j, ((DefaultVisitsLoggingSimJob) j).getVisitLogs ());
              ((DefaultVisitsLoggingSimJob) j).reset ();
            }
            // Gather the other logs; make shallow copies because the loggers are about to be reset.
            actualQavLogs = new ArrayList<> (qavLogger.getQueueAccessVacationLog ());
            actualSacLogs = new ArrayList<> (sacLogger.getServerAccessCreditsAvailabilityLog ());
            actualStaLogs = new ArrayList<> (staLogger.getStartArmedLog ());
            // Reset the event list (and its queues and its loggers).
            el.reset ();
            // Populate the event list with the copied events for predictorQueue.
            el.addAll (predictorQueueEvents);
            // Run the simulation with predictorQueue.
            el.run ();
            assert el.isEmpty ();
            // Gather the job-queue visit log (per job), but make sure we replace the visited queue (i.e., predictorQueue)
            // with our actual queue.
            predictedJobQueueVisitLogs = new HashMap<> ();
            for (final SimJob j : jobs)
            {
              final TreeMap<Double,TreeMap<Integer,JobQueueVisitLog>> visitLogs_j
                = ((DefaultVisitsLoggingSimJob) j).getVisitLogs ();
              if (visitLogs_j.size () != 1 || visitLogs_j.values ().iterator ().next ().size () != 1)
                throw new RuntimeException ();
              final JobQueueVisitLog visitLog_j = visitLogs_j.values ().iterator ().next ().values ().iterator ().next ();
              predictedJobQueueVisitLogs.put (j, visitLog_j.copyForQueue (queue));
            }
            // Obtain the qav/sac/sta logs (no need to copy here anymore).
            predictedQavLogs = predictorQueueQavLogger.getQueueAccessVacationLog ();
            predictedSacLogs = predictorQueueSacLogger.getServerAccessCreditsAvailabilityLog ();
            predictedStaLogs = predictorQueueStaLogger.getStartArmedLog ();
          }
          else
            throw new RuntimeException ();
          // Create a test string to be passed to the matching functions.
          final String testString =
                                           "    Load Factory   : " + klf
            + "\n"
            +                              "      [Description]: " + klf.getLoadFactory ().getDescription ()
            + "\n"
            +                              "    Pass           : " + pass
            + "\n"
            +                              "    Queue          : " + queue
            + "\n"
            + ((predictor != null) ?      ("    Predictor      : " + predictor) : "")
            + ((predictorQueue != null) ? ("    Predictor Queue: " + predictorQueue) : "")
            ;
          // Confront the predicted and actual logs for job-visits, qav, sac and sta.
          assert SimQueuePredictor.matchVisitLogs_SQ_SV
            (queue, predictedJobQueueVisitLogs, actualJobQueueVisitLogs, accuracy, System.err, testString);
          assert SimQueueAccessVacationLogger.matchQueueAccessVacationLogs
                   (predictedQavLogs, actualQavLogs, accuracy, testString);
          assert SimQueueServerAccessCreditsAvailabilityLogger.matchServerAccessCreditsAvailabilityLogs
                   (predictedSacLogs, actualSacLogs, accuracy, testString);
          assert SimQueueStartArmedLogger.matchStartArmedLogs
                   (predictedStaLogs, actualStaLogs, accuracy, testString);
          // Reset the event list (and its queues and its loggers) for the next run.
          el.reset ();
        }
      else if (! deadSilent)
        // Report omitted test.
        System.out.println ("===== Omitting: " + klf + ".");
    return true;
  }
    
}