package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A representation of a schedule of workload and state-setting events for a single {@link SimQueue}.
 *
 * <p>
 * The {@link SimQueue} to which the workload applies must be fixed upon construction.
 * 
 */
public interface WorkloadSchedule_SQ
extends WorkloadSchedule
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the queue to which this workload representation applies.
   * 
   * <p>
   * The queue must be fixed upon construction.
   * 
   * @return The queue to which this workload representation applies.
   * 
   * @throws WorkloadScheduleInvalidException If the object is invalid (e.g., due to internal inconsistencies).
   * 
   */
  public default SimQueue getQueue ()
  throws WorkloadScheduleInvalidException
  {
    final Set<? extends SimQueue> queues = getQueues ();
    if (queues.size () != 1 || queues.contains (null))
      throw new WorkloadScheduleInvalidException ();
    return queues.iterator ().next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * 
   * @see #getQueueAccessVacationMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<Boolean>> getQueueAccessVacationMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getQueueAccessVacationMap (getQueue ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * @see #getArrivalTimesMap(SimQueue) 
   * @see #getQueue
   * 
   */
  public default Map<SimJob, List<Double>> getArrivalTimesMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getArrivalTimesMap (getQueue ());
  }
  
  /**
   * 
   * @see #getJobArrivalsMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<SimJob>> getJobArrivalsMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getJobArrivalsMap (getQueue ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * @see #getRevocationTimesMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default Map<SimJob, List<Map<Double, Boolean>>> getRevocationTimesMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getRevocationTimesMap (getQueue ());
  }
  
  /**
   * 
   * @see #getJobRevocationsMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<Map<SimJob, Boolean>>> getJobRevocationsMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getJobRevocationsMap (getQueue ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * 
   * @see #getServerAccessCreditsMap(SimQueue)
   * @see #getQueue
   * 
   */
  public default NavigableMap<Double, List<Integer>> getServerAccessCreditsMap_SQ ()
  throws WorkloadScheduleInvalidException
  {
    return getServerAccessCreditsMap (getQueue ());
  }
  
}
