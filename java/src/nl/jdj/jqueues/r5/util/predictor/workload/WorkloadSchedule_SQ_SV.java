package nl.jdj.jqueues.r5.util.predictor.workload;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A representation of a schedule of workload and state-setting events for a single {@link SimQueue} (SQ) with jobs visiting that
 *  queue exactly once (SV).
 *
 * <p>
 * The {@link SimQueue} to which the workload applies must be fixed upon construction.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface WorkloadSchedule_SQ_SV<J extends SimJob, Q extends SimQueue>
extends WorkloadSchedule_SQ<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * @see #isSingleQueue
   * @see #isSingleVisit
   * @see #getArrivalTimesMap_SQ
   * 
   */
  public default Map<J, Double> getArrivalTimesMap_SQ_SV ()
  throws WorkloadScheduleInvalidException
  {
    if (! (isSingleQueue () && isSingleVisit ()))
      throw new WorkloadScheduleInvalidException ();
    final Map<J, Double> arrivalTimesMap_SQ_SV = new HashMap<> ();
    for (final Entry<J, List<Double>> entry : getArrivalTimesMap_SQ ().entrySet ())
      arrivalTimesMap_SQ_SV.put (entry.getKey (), entry.getValue ().get (0));
    return Collections.unmodifiableMap (arrivalTimesMap_SQ_SV);
  }
  
}
