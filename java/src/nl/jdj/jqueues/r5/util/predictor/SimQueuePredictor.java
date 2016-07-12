package nl.jdj.jqueues.r5.util.predictor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;
import nl.jdj.jqueues.r5.event.SimEntityEvent;

/** An object capable of predicting the behavior of one or more {@link SimQueue}s under user-supplied workload and conditions.
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueuePredictor<Q extends SimQueue>
{

  /** Creates the unique prediction, if possible, of job-visits (at most one) to a given queue under a Random-Order Event List.
   * 
   * @param queue       The queue, non-{@code null}.
   * @param queueEvents The queue events; events related to other queues are allowed and are to be ignored.
   * 
   * @return A single {@link JobQueueVisitLog} for every job that visits the given queue.
   * 
   * @throws IllegalArgumentException              If {@code queue == null} or the workload parameters are somehow illegal.
   * @throws UnsupportedOperationException         If the queue type or the workload is (partially) unsupported.
   * @throws SimQueuePredictionException           If a prediction is (e.g.) too complex to generate
   *                                               ({@link SimQueuePredictionComplexityException}),
   *                                               if invalid input has been supplied to the predictor
   *                                               ({@link SimQueuePredictionInvalidInputException}),
   *                                               or if a <i>unique</i> prediction cannot be generated
   *                                               ({@link SimQueuePredictionAmbiguityException}).
   * 
   */
  public Map<SimJob, JobQueueVisitLog<SimJob, Q>> predictVisitLogs_SQ_SV_ROEL_U
  (Q queue, Set<SimEntityEvent> queueEvents)
    throws SimQueuePredictionException;
 
  /** Creates the unique prediction, if possible, of job-visits (at most one) to a given queue under a Random-Order Event List.
   * 
   * <p>
   * A variant of {@link #predictVisitLogs_SQ_SV_ROEL_U(nl.jdj.jqueues.r5.SimQueue, java.util.Set)} using a map
   * from event time onto the (unordered) set of events occurring at that time.
   * 
   * <p>
   * The default implementation puts the events in the map in proper order into a {@code LinkedHashSet},
   * and relies on {@link #predictVisitLogs_SQ_SV_ROEL_U(nl.jdj.jqueues.r5.SimQueue, java.util.Set)} for further processing.
   * 
   * @param queue          The queue, non-{@code null}.
   * @param queueEventsMap The queue events as a map from event time onto the
   *                       (unordered) set of events occurring at that time;
   *                       events related to other queues are allowed and are to be ignored.
   * 
   * @return A single {@link JobQueueVisitLog} for every job that visits the given queue.
   * 
   */
  public default Map<SimJob, JobQueueVisitLog<SimJob, Q>> predictVisitLogs_SQ_SV_U
  (final Q queue, final NavigableMap<Double, Set<SimEntityEvent>> queueEventsMap)
    throws SimQueuePredictionException
  {
    if (queueEventsMap == null)
      throw new IllegalArgumentException ();
    final Set<SimEntityEvent> queueEvents = new LinkedHashSet<> ();
    for (final Set<SimEntityEvent> queueEventsAtTime : queueEventsMap.values ())
      queueEvents.addAll (queueEventsAtTime);
    return predictVisitLogs_SQ_SV_ROEL_U (queue, queueEvents);
  }
  
  /** Compares two maps of predicted and actual {@link JobQueueVisitLog}s for equality, within given accuracy.
   * 
   * <p>
   * The {@code actual} argument holds all (allowing multiple) job visits, and may contain visits to other {@link SimQueue}s;
   * the latter of which are (to be) ignored.
   * The map has the jobs as keys, and each value holds another map from arrival times (of the
   * particular job) to numbered {@link JobQueueVisitLog} of that job at that particular arrival time
   * (this allows multiple arrivals of the same job at the same time).
   * 
   * @param queue     The queue, non-{@code null}.
   * @param predicted The predicted {@link JobQueueVisitLog}s, indexed by job-arrival time; arrival at other queues
   *                  are (to be) ignored.
   * @param actual    The actual {@link JobQueueVisitLog}s, see above.
   * @param accuracy  The accuracy (maximum  deviation of times in a {@link JobQueueVisitLog}), non-negative.
   * @param stream    An optional stream for mismatch reporting.
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
      final PrintStream stream)
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
                stream.println ("[matchVisitLogs_SQ_SV] Found multiple visits of job " + job + " to queue " + queue +".");
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
          stream.println ("[matchVisitLogs_SQ_SV] Absent predicted visit of job " + job + " to queue " + queue +":");
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
          stream.println ("[matchVisitLogs_SQ_SV] Found mismatch for visit of job " + job + " to queue " + queue +":");
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
