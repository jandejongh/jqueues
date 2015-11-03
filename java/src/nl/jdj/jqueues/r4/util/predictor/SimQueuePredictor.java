package nl.jdj.jqueues.r4.util.predictor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.util.jobfactory.JobQueueVisitLog;
import nl.jdj.jqueues.r4.util.schedule.QueueExternalEvent;

/** An object capable of predicting the behavior of one or more {@link SimQueue}s under user-supplied workload and conditions.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueuePredictor<J extends SimJob, Q extends SimQueue>
{

  /** Creates the unique prediction, if possible, of job-visits (at most one) to a given queue.
   * 
   * @param queue               The queue, non-{@code null}.
   * @param queueExternalEvents The external events, indexed by event time and for a given event time,
   *                            ordered by occurrence on the event list;
   *                            events related to other queues are allowed and are to be ignored.
   * 
   * @return A single {@link JobQueueVisitLog} for every job that visits the given queue.
   * 
   * @throws IllegalArgumentException              If {@code queue == null} or the workload parameters are somehow illegal.
   * @throws UnsupportedOperationException         If the queue type or the workload is (partially) unsupported.
   * 
   * @throws SimQueuePredictionException           If a prediction is (e.g.) too complex to generate
   *                                               ({@link SimQueuePredictionComplexityException},
   *                                               if invalid input has been supplied to the predictor
   *                                               ({@link SimQueuePredictionInvalidInputException}),
   *                                               or if a <i>unique</i> prediction cannot be generated
   *                                               ({@link SimQueuePredictionAmbiguityException}).
   * 
   */
  public Map<J, JobQueueVisitLog<J, Q>> predictUniqueJobQueueVisitLogs_SingleVisit
  (Q queue, TreeMap<Double, Set<QueueExternalEvent<J, Q>>> queueExternalEvents)
    throws SimQueuePredictionException;
 
  /** Compares two maps of predicted and actual {@link JobQueueVisitLog}s for equality, within given accuracy.
   * 
   * <p>
   * The {@code actual} argument holds all (allowing multiple) job visits, and may contain visits to other {@link SimQueue}s;
   * these are (to be) ignored. The map has the jobs as keys, and each value holds another map from arrival times (of the
   * particular job) to numbered {@link JobQueueVisitLog} of that job at that particular arrival time
   * (this allows multiple arrivals of the same job at the same time).
   * 
   * @param queue     The queue, non-{@code null}.
   * @param predicted The predicted {@link JobQueueVisitLog}s, indexed by job-arrival time; arrival at other queues
   *                  are (to be) ignored.
   * @param actual    The actual {@link JobQueueVisitLog}s, see above.
   * @param accuracy  The accuracy (maximum  deviation of times in a {@link JobQueueVisitLog}, non-negative.
   * 
   * @return Whether the predicted and actual maps map within the given accuracy.
   * 
   * @throws IllegalArgumentException If any of the arguments has {@code null} value, is illegally structured, or if the
   *                                  accuracy argument is negative.
   * 
   */
  public default boolean matchUniqueJobQueueVisitLogs_SingleVisit
    (final Q queue,
      final Map<J, JobQueueVisitLog<J, Q>> predicted,
      final Map<J, TreeMap<Double, TreeMap<Integer, JobQueueVisitLog<J, Q>>>> actual,
      final double accuracy)
  {
    if (queue == null || predicted == null || actual == null || accuracy < 0)
      throw new IllegalArgumentException ();
    final Map<J, JobQueueVisitLog<J, Q>> predictedAtQueue = new HashMap<> ();
    for (final Entry<J, JobQueueVisitLog<J, Q>> entry : predicted.entrySet ())
      if (entry.getValue ().queue == queue)
        predictedAtQueue.put (entry.getKey (), entry.getValue ());
    final Map<J, JobQueueVisitLog<J, Q>> actualAtQueue = new HashMap<> ();
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
              return false;
            else
              actualAtQueue.put (job, sequenceEntry.getValue ());
          }
      }
    }
    for (final J job : predictedAtQueue.keySet ())
    {
      final JobQueueVisitLog<J, Q> predictedVisitLog = predictedAtQueue.get (job);
      final JobQueueVisitLog<J, Q> actualVisitLog = actualAtQueue.get (job);
      if (! actualVisitLog.equals (predictedVisitLog, accuracy))
        return false;
    }
    return true;
  }
    
}
