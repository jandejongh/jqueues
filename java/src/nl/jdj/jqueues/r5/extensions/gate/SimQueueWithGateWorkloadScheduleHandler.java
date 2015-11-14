package nl.jdj.jqueues.r5.extensions.gate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.workload.DefaultWorkloadSchedule;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleException;
import nl.jdj.jqueues.r5.util.predictor.workload.WorkloadScheduleHandler;

/** A {@link WorkloadScheduleHandler} for a {@link SimQueueWithGate}.
 *
 * <p>
 * Scans for and takes responsibility for (all) {@link SimQueueGateEvent} type(s) at registration,
 * and registers the {@link SimQueueWithGateSimpleEventType} simple event type(s).
 * 
 */
public final class SimQueueWithGateWorkloadScheduleHandler
implements WorkloadScheduleHandler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WorkloadScheduleHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns "SimQueueWithGateHandler".
   * 
   * @return "SimQueueWithGateHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueWithGateHandler";
  }

  private final static Map<Class<? extends SimEntityEvent>, SimEntitySimpleEventType.Member> EVENT_MAP = new HashMap<> ();
  
  static
  {
    SimQueueWithGateWorkloadScheduleHandler.EVENT_MAP.put (SimQueueGateEvent.class, SimQueueWithGateSimpleEventType.GATE);
  }
  
  /**
   * @see SimQueueGateEvent
   * @see SimQueueWithGateSimpleEventType
   * 
   */
  @Override
  public final Map<Class<? extends SimEntityEvent>, SimEntitySimpleEventType.Member> getEventMap ()
  {
    return SimQueueWithGateWorkloadScheduleHandler.EVENT_MAP;
  }

  /** Returns {@code true}.
   * 
   * @return {@code true}.
   * 
   */
  @Override
  public final boolean needsScan ()
  {
    return true;
  }

  @Override
  public final Set<SimEntityEvent> scan (final DefaultWorkloadSchedule workloadSchedule)
  throws WorkloadScheduleException
  {
    if (workloadSchedule == null)
      throw new IllegalArgumentException ();
    if (this.workloadSchedule != null)
      throw new IllegalStateException ();
    this.workloadSchedule = workloadSchedule;
    final Set<SimEntityEvent> processedQueueEvents = new HashSet<> ();
    for (SimQueue q : workloadSchedule.getQueues ())
      this.gatePassageCreditsTimesMap.put (q, new TreeMap<> ());
    if (workloadSchedule.getQueueEvents () != null)
    {
      for (final SimEntityEvent event : workloadSchedule.getQueueEvents ())
      {
        final double time = event.getTime ();
        final SimQueue queue = event.getQueue ();
        if (workloadSchedule.getQueues ().contains (queue))
        {
          if (event instanceof SimQueueGateEvent)
          {
            processedQueueEvents.add (event);
            final int credits = ((SimQueueGateEvent) event).getGatePassageCredits ();
            final NavigableMap<Double, List<Integer>> gateTimesMap_q = this.gatePassageCreditsTimesMap.get (queue);
            if (! gateTimesMap_q.containsKey (time))
              gateTimesMap_q.put (time, new ArrayList<> ());
            gateTimesMap_q.get (time).add (credits);
          }
        }
      }
    }
    return processedQueueEvents;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT QUEUE STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DefaultWorkloadSchedule workloadSchedule = null;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // GATE-PASSAGE CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<SimQueue, NavigableMap<Double, List<Integer>>> gatePassageCreditsTimesMap = new HashMap<> ();

  /** Returns the gate-passage-credits settings in time for a specific queue.
   * 
   * <p>
   * A unmodifiable view must be returned.
   * 
   * @param queue The queue, may be {@code null}, unknown or or not in {@link DefaultWorkloadSchedule#getQueues}
   *                on the schedule we registered at,
   *                in which case an empty map is returned.
   * 
   * @return The gate-passage-credits settings in time for the queue, indexed by time and subsequently ordered
   *         according to appearance in the source event list upon construction.
   * 
   * @throws IllegalStateException If we are not registered at a {@link DefaultWorkloadSchedule}.
   * 
   */
  public final NavigableMap<Double, List<Integer>> getGatePassageCreditsMap (final SimQueue queue)
  {
    if (this.workloadSchedule == null)
      throw new IllegalStateException ();
    if (queue == null
      || (! this.workloadSchedule.getQueues ().contains (queue))
      || ! this.gatePassageCreditsTimesMap.containsKey (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.gatePassageCreditsTimesMap.get (queue));
  }
  
}
