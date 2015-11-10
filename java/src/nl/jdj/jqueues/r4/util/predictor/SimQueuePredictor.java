package nl.jdj.jqueues.r4.util.predictor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.event.SimEntityEvent;

/** An object capable of predicting the behavior of one or more {@link SimQueue}s under user-supplied workload and conditions.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueuePredictor<J extends SimJob, Q extends SimQueue>
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
  public Map<J, JobQueueVisitLog<J, Q>> predictVisitLogs_SQ_SV_ROEL_U
  (Q queue, Set<SimEntityEvent<J, Q>> queueEvents)
    throws SimQueuePredictionException;
 
  public default Map<J, JobQueueVisitLog<J, Q>> predictVisitLogs_SQ_SV_U
  (final Q queue, final NavigableMap<Double, Set<SimEntityEvent<J, Q>>> queueEventsMap)
    throws SimQueuePredictionException
  {
    if (queueEventsMap == null)
      throw new IllegalArgumentException ();
    final Set<SimEntityEvent<J, Q>> queueEvents = new LinkedHashSet<> ();
    for (final Set<SimEntityEvent<J, Q>> queueEventsAtTime : queueEventsMap.values ())
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
   * @param accuracy  The accuracy (maximum  deviation of times in a {@link JobQueueVisitLog}, non-negative.
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
      final Map<J, JobQueueVisitLog<J, Q>> predicted,
      final Map<J, TreeMap<Double, TreeMap<Integer, JobQueueVisitLog<J, Q>>>> actual,
      final double accuracy,
      final PrintStream stream)
  {
    if (queue == null || predicted == null || actual == null || accuracy < 0)
      throw new IllegalArgumentException ();
    final Map<J, JobQueueVisitLog<J, Q>> predictedAtQueue = new HashMap<> ();
    for (final Entry<J, JobQueueVisitLog<J, Q>> entry : predicted.entrySet ())
      if (entry.getValue ().queue == queue)
        predictedAtQueue.put (entry.getKey (), entry.getValue ());
    final Map<J, JobQueueVisitLog<J, Q>> actualAtQueue = new HashMap<> ();
    boolean success = true;
    for (final Entry<J, TreeMap<Double, TreeMap<Integer, JobQueueVisitLog<J, Q>>>> entry : actual.entrySet ())
    {
      if (entry == null)
        throw new IllegalArgumentException ();
      final J job = entry.getKey ();
      for (final Entry<Double, TreeMap<Integer, JobQueueVisitLog<J, Q>>> timeEntry : entry.getValue ().entrySet ())
      {
        if (timeEntry == null)
          throw new IllegalArgumentException ();
        for (final Entry<Integer, JobQueueVisitLog<J, Q>> sequenceEntry : timeEntry.getValue ().entrySet ())
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
    for (final J job : predictedAtQueue.keySet ())
    {
      final JobQueueVisitLog<J, Q> predictedVisitLog = predictedAtQueue.get (job);
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
      final JobQueueVisitLog<J, Q> actualVisitLog = actualAtQueue.get (job);
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
