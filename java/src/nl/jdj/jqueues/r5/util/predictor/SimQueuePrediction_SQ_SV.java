package nl.jdj.jqueues.r5.util.predictor;

import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.visitslogging.JobQueueVisitLog;

/** A prediction of the behavior of a single {@link SimQueue}
 *  under a (presumed) workload in which each {@link SimJob} visits the queue at most once.
 *
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueuePrediction_SQ_SV<Q extends SimQueue>
{
  
  /** Returns the {@link SimQueue} for which this prediction was generated.
   * 
   * @return The {@link SimQueue} for which this prediction was generated.
   * 
   */
  Q getQueue ();
  
  /** Gets the predicted of job-visits (at most one per job).
   * 
   * @return A map from every job predicted to visit the queue onto its {@link JobQueueVisitLog} .
   * 
   */
  Map<SimJob, JobQueueVisitLog<SimJob, Q>> getVisitLogs ();

  /** Returns the predicted queue-access vacation (changes).
   * 
   * @return A list with singleton maps holding the time of change in the QAV state (key) and the new state (value).
   *         The list must be ordered non-decreasing in event time.
   * 
   */
  List<Map<Double, Boolean>> getQueueAccessVacationLog ();

  /** Returns the predicted server-access-credits availability.
   * 
   * @return A list with singleton maps holding the time of change in the SAC availability state (key) and the new state (value).
   *         The list must be ordered non-decreasing in event time.
   * 
   */
  List<Map<Double, Boolean>> getServerAccessCreditsAvailabilityLog ();
  
  /** Returns the predicted {@code NoWaitArmed} (changes).
   * 
   * @return A list with singleton maps holding the time of change in the NWA state (key) and the new state (value).
   *         The list must be ordered non-decreasing in event time.
   * 
   */
  List<Map<Double, Boolean>> getNoWaitArmedLog ();

}
