package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A representation of an unambiguous (U) schedule of workload and state-setting events for a single {@link SimQueue}
 *  (SQ) with jobs visiting that queue exactly once (SV) and assuming an underlying Random-Order Event List (ROEL).
 *
 */
public interface WorkloadSchedule_SQ_SV_ROEL_U
extends WorkloadSchedule_SQ_SV
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #isUnambiguous_ROEL
   * @see #getQueueAccessVacationMap_SQ
   * 
   */
  public default NavigableMap<Double, Boolean> getQueueAccessVacationMap_SQ_SV_ROEL_U ()
  throws WorkloadScheduleInvalidException
  {
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleInvalidException ();
    final NavigableMap<Double, Boolean> queueAccessVacationMap_SQ_SV_ROEL_U = new TreeMap<> ();
    for (final Entry<Double, List<Boolean>> entry : getQueueAccessVacationMap_SQ ().entrySet ())
      queueAccessVacationMap_SQ_SV_ROEL_U.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableNavigableMap (queueAccessVacationMap_SQ_SV_ROEL_U);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #isUnambiguous_ROEL
   * @see #getJobArrivalsMap_SQ
   * 
   */
  public default NavigableMap<Double, SimJob> getJobArrivalsMap_SQ_SV_ROEL_U ()
  throws WorkloadScheduleInvalidException
  {
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleInvalidException ();
    final NavigableMap<Double, SimJob> jobArrivalsMap_SQ_SV_ROEL_U = new TreeMap<> ();
    for (final Entry<Double, List<SimJob>> entry : getJobArrivalsMap_SQ ().entrySet ())
      jobArrivalsMap_SQ_SV_ROEL_U.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableNavigableMap (jobArrivalsMap_SQ_SV_ROEL_U);    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #isUnambiguous_ROEL
   * @see #getRevocationTimesMap_SQ
   * 
   */
  public default Map<SimJob, Map<Double, Boolean>> getRevocationTimesMap_SQ_SV_ROEL_U ()
  throws WorkloadScheduleInvalidException
  {
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleInvalidException ();
    final Map<SimJob, Map<Double, Boolean>> revocationTimesMap_SQ_SV_ROEL_U = new HashMap<> ();
    for (final Entry<SimJob, List<Map<Double, Boolean>>> entry : getRevocationTimesMap_SQ ().entrySet ())
      revocationTimesMap_SQ_SV_ROEL_U.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableMap (revocationTimesMap_SQ_SV_ROEL_U);
  }
  
  /**
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #isUnambiguous_ROEL
   * @see #getJobRevocationsMap_SQ
   * 
   */
  public default NavigableMap<Double, Map<SimJob, Boolean>> getJobRevocationsMap_SQ_SV_ROEL_U ()
  throws WorkloadScheduleInvalidException
  {
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleInvalidException ();
    final NavigableMap<Double, Map<SimJob, Boolean>> jobRevocationsMap_SQ_SV_ROEL_U = new TreeMap<> ();
    for (final Entry<Double, List<Map<SimJob, Boolean>>> entry : getJobRevocationsMap_SQ ().entrySet ())
      jobRevocationsMap_SQ_SV_ROEL_U.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableNavigableMap (jobRevocationsMap_SQ_SV_ROEL_U);    
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #isUnambiguous_ROEL
   * @see #getServerAccessCreditsMap_SQ
   * 
   */
  public default NavigableMap<Double, Integer> getServerAccessCreditsMap_SQ_SV_ROEL_U ()
  throws WorkloadScheduleInvalidException
  {
    if (! isUnambiguous_ROEL ())
      throw new WorkloadScheduleInvalidException ();
    final NavigableMap<Double, Integer> serverAccessCreditsMap_SQ_SV_ROEL_U = new TreeMap<> ();
    for (final Entry<Double, List<Integer>> entry : getServerAccessCreditsMap_SQ ().entrySet ())
      serverAccessCreditsMap_SQ_SV_ROEL_U.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableNavigableMap (serverAccessCreditsMap_SQ_SV_ROEL_U);
  }
  
}
