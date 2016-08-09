package nl.jdj.jqueues.r5.util.predictor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.entity.queue.composite.tandem.BlackTandemSimQueue;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.FCFS;
import nl.jdj.jqueues.r5.entity.queue.nonpreemptive.IC;
import nl.jdj.jqueues.r5.entity.queue.preemptive.P_LCFS;
import nl.jdj.jqueues.r5.entity.queue.processorsharing.CUPS;
import nl.jdj.jqueues.r5.entity.queue.serverless.ZERO;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList_IOEL;
import nl.jdj.jsimulation.r5.DefaultSimEventList_ROEL;
import nl.jdj.jsimulation.r5.SimEventList;

/** An object capable of predicting the behavior of one or more {@link SimQueue}s under user-supplied workload and conditions.
 *
 * <p>
 * A {@link SimQueuePredictor} is a stateless object that is capable of predicting the behavior of a specific {@link SimQueue}
 * class or (if applicable) of (some of) its subclasses;
 * the (base) class of queues supported is present as generic type argument {@code Q}.
 * Objects like this are extensively used in the test sub-system of {@code jqueues}.
 * 
 * <p>
 * The most important feature of a {@link SimQueuePredictor} is the prediction of job visits to a given (stateless!)
 * {@link SimQueue} under a given external workload (e.g., arrivals, revocations, setting server-access credits,
 * or queue-specific external operations). The workload consists of a collection of {@link SimEntityEvent}s, and this collection
 * may contain events scheduled at the same time. Depending on the method invoked on the predictor,
 * such simultaneous events are to be interpreted as occurring in "random order"
 * (as if processed by a ROEL {@link SimEventList} like {@link DefaultSimEventList_ROEL})
 * or as occurring in the strict and deterministic order (somehow) imposed by the collection 
 * (as if processed by a IOEL {@link SimEventList} like {@link DefaultSimEventList_IOEL}).
 * 
 * <p>
 * In the first case,
 * see {@link #predict_SQ_SV_ROEL_U},
 * the workload schedule itself can easily lead to ambiguities that prevent the delivery of a prediction,
 * for instance in the case of simultaneous arrivals (of jobs with non-zero required service time) at a {@link FCFS} queue.
 * On the other hand, queues like {@link ZERO} appear to be robust against simultaneous arrivals under ROEL,
 * and for queues like {@link IC}, simultaneous arrivals under ROEL are unambiguous <i>only</i> if sufficient
 * server-access credits are available, hence, the ambiguity of simultaneous arrivals for this queue is state-dependent.
 * However, for all {@link SimQueue}s, the simultaneous start of a queue-access vacation (again, state-dependent) and
 * an arrival <i>always</i> leads to ambiguities.
 * 
 * <p>
 * In the seconds case,
 * see {@link #predict_SQ_SV_IOEL_U},
 * workload events do not cause ambiguities among themselves, but they may still interfere with queue-internal
 * events like departures, for instance the simultaneous occurrence of an arrival and a scheduled departure in a {@link P_LCFS}
 * queue that is otherwise empty. Even worse, queues may exhibit internal ambiguities, for instance, the simultaneous
 * occurrence of a "catch-up" and a departure (both "internal events") in a {@link CUPS} queue.
 * Note that even with a ROEL {@link SimEventList}, certain {@link SimQueue} implementations
 * may process "simultaneous events" in a specific sequence, and heavily rely on their sequential execution.
 * For instance, the {@link BlackTandemSimQueue} lets (delegate) jobs arrive at their first sub-queue if
 * server-access credits become available, yet it processes these arrivals in a specific order (the arrival order of the
 * corresponding "real" jobs) and it effectuates these arrivals immediately, without using the underlying event list.
 * 
 * <p>
 * In any case, implementations must provide a collection of visit logs, see {@link JobQueueVisitLog},
 * or throw an exception upon determining an ambiguity, see {@link SimQueuePredictionAmbiguityException}.
 * 
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueuePredictor<Q extends SimQueue>
extends SimQueueEventPredictor<Q>, SimQueueStatePredictor<Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PREDICT (EVERYTHING)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates the unique prediction, if possible,
   *  resulting from subjecting a given queue to a given workload
   *  under a Random-Order Event List.
   * 
   * @param queue          The queue, non-{@code null}.
   * @param workloadEvents The workload events; events related to other queues are allowed and are to be ignored.
   * 
   * @return The prediction.
   * 
   * @throws IllegalArgumentException      If {@code queue == null} or the workload parameters are somehow illegal.
   * @throws UnsupportedOperationException If the queue type or the workload is (partially) unsupported.
   * @throws SimQueuePredictionException   If a prediction is (e.g.) too complex to generate
   *                                       ({@link SimQueuePredictionComplexityException}),
   *                                       if invalid input has been supplied to the predictor
   *                                       ({@link SimQueuePredictionInvalidInputException}),
   *                                       or if a <i>unique</i> prediction cannot be generated
   *                                       ({@link SimQueuePredictionAmbiguityException}).
   * 
   */
  SimQueuePrediction_SQ_SV<Q>
  predict_SQ_SV_ROEL_U
  (Q queue, Set<SimEntityEvent> workloadEvents)
  throws SimQueuePredictionException;
    
  /** Creates the unique prediction, if possible,
   *  resulting from subjecting a given queue to a given workload
   *  under an Insertion-Order Event List.
   * 
   * <p>
   * Note that processed-events map parameter may be equal to the workload events map,
   * in which case processed (internal) events are inserted in situ.
   * If a different map is provided, it is cleared upon entry.
   * 
   * @param queue              The queue, non-{@code null}.
   * @param workloadEventsMap  The workload events as a map from event time onto the
   *                           (ordered!) set of events occurring at that time;
   *                           events related to other queues are allowed and are to be ignored.
   * @param processedEventsMap An optional map in which all events processed at the queue (including workload events)
   *                           are stored unambiguously; the events in a value set are in processing ordered
   *                           (you can use this to resolve ambiguities in the visit logs like equal departure times).
   * 
   * @return The prediction.
   * 
   * @throws IllegalArgumentException      If {@code queue == null} or the workload parameters are somehow illegal.
   * @throws UnsupportedOperationException If the queue type or the workload is (partially) unsupported.
   * @throws SimQueuePredictionException   If a prediction is (e.g.) too complex to generate
   *                                       ({@link SimQueuePredictionComplexityException}),
   *                                       if invalid input has been supplied to the predictor
   *                                       ({@link SimQueuePredictionInvalidInputException}),
   *                                       or if a <i>unique</i> prediction cannot be generated
   *                                       ({@link SimQueuePredictionAmbiguityException}).
   * 
   */
  SimQueuePrediction_SQ_SV<Q>
  predict_SQ_SV_IOEL_U
  (Q queue,
   NavigableMap<Double, Set<SimEntityEvent>> workloadEventsMap,
   NavigableMap<Double, Set<SimEntityEvent>> processedEventsMap)
  throws SimQueuePredictionException;
  
  /** Compares two maps of predicted and actual {@link JobQueueVisitLog}s for equality, within given accuracy.
   * 
   * <p>
   * The {@code actual} argument holds all (allowing multiple) job visits, and may contain visits to other {@link SimQueue}s;
   * the latter of which are (to be) ignored.
   * The map has the jobs as keys, and each value holds another map from arrival times (of the
   * particular job) to numbered {@link JobQueueVisitLog} of that job at that particular arrival time
   * (this allows multiple arrivals of the same job at the same time).
   * 
   * @param queue      The queue, non-{@code null}.
   * @param predicted  The predicted {@link JobQueueVisitLog}s, indexed by job-arrival time; arrival at other queues
   *                   are (to be) ignored.
   * @param actual     The actual {@link JobQueueVisitLog}s, see above.
   * @param accuracy   The accuracy (maximum  deviation of times in a {@link JobQueueVisitLog}), non-negative.
   * @param stream     An optional stream for mismatch reporting.
   * @param testString An optional String identifying the test in place.
   * 
   * @return Whether the predicted and actual maps map within the given accuracy.
   * 
   * @throws IllegalArgumentException If any of the arguments except the stream has {@code null} value,
   *                                  is illegally structured, or if the
   *                                  accuracy argument is negative.
   * 
   */
  public default boolean matchVisitLogs_SQ_SV
    (final Q queue,
      final Map<SimJob, JobQueueVisitLog<SimJob, Q>> predicted,
      final Map<SimJob, TreeMap<Double, TreeMap<Integer, JobQueueVisitLog<SimJob, Q>>>> actual,
      final double accuracy,
      final PrintStream stream,
      final String testString)
  {
    if (queue == null || predicted == null || actual == null || accuracy < 0)
      throw new IllegalArgumentException ();
    final Map<SimJob, JobQueueVisitLog<SimJob, Q>> predictedAtQueue = new HashMap<> ();
    for (final Entry<SimJob, JobQueueVisitLog<SimJob, Q>> entry : predicted.entrySet ())
      if (entry.getValue ().queue == queue)
        predictedAtQueue.put (entry.getKey (), entry.getValue ());
    final Map<SimJob, JobQueueVisitLog<SimJob, Q>> actualAtQueue = new HashMap<> ();
    boolean success = true;
    for (final Entry<SimJob, TreeMap<Double, TreeMap<Integer, JobQueueVisitLog<SimJob, Q>>>> entry : actual.entrySet ())
    {
      if (entry == null)
        throw new IllegalArgumentException ();
      final SimJob job = entry.getKey ();
      for (final Entry<Double, TreeMap<Integer, JobQueueVisitLog<SimJob, Q>>> timeEntry : entry.getValue ().entrySet ())
      {
        if (timeEntry == null)
          throw new IllegalArgumentException ();
        for (final Entry<Integer, JobQueueVisitLog<SimJob, Q>> sequenceEntry : timeEntry.getValue ().entrySet ())
          if (sequenceEntry.getValue ().queue == queue)
          {
            if (actualAtQueue.containsKey (job))
            {
              success = false;
              if (stream != null)
                stream.println ("[matchVisitLogs_SQ_SV: " + testString
                  + "] Found multiple visits of job " + job + " to queue " + queue +".");
              else
                return false;
            }
            else
              actualAtQueue.put (job, sequenceEntry.getValue ());
          }
      }
    }
    for (final SimJob job : predictedAtQueue.keySet ())
    {
      final JobQueueVisitLog<SimJob, Q> predictedVisitLog = predictedAtQueue.get (job);
      if (! actualAtQueue.containsKey (job))
      {
        success = false;
        if (stream != null)
        {
          stream.println ("[matchVisitLogs_SQ_SV: " + testString
                  + "] Absent predicted visit of job " + job + " to queue " + queue +":");
          stream.println ("Predicted visit log: ");
          predictedVisitLog.print (stream);
        }
        else
          return false;        
      }
      final JobQueueVisitLog<SimJob, Q> actualVisitLog = actualAtQueue.get (job);
      if (! actualVisitLog.equals (predictedVisitLog, accuracy))
      {
        success = false;
        if (stream != null)
        {
          stream.println ("[matchVisitLogs_SQ_SV: " + testString
                  + "] Found mismatch for visit of job " + job + " to queue " + queue +":");
          stream.println ("Accuracy = " + accuracy + ".");
          stream.println ("Predicted and actual visit logs: ");
          predictedVisitLog.print (stream);
          actualVisitLog.print (stream);
        }
        else
          return false;
      }
    }
    return success;
  }
    
}
