package nl.jdj.jqueues.r5.event.map;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jsimulation.r5.SimEvent;

/** A default {@link SimEntityEventMap}.
 * 
 * <p>
 * A {@link DefaultSimEntityEventMap} takes a set of {@link SimEvent}s as input to its constructor,
 * and constructs the maps as specified in {@link SimEntityEventMap}
 * from all {@link SimEntityEvent} it finds that have non-{@code null} job or queue (or both).
 * 
 * <p>
 * This implementation constructs an internal copy of the input set,
 * and, for what it's worth,
 * maintains the order of simultaneous {@link SimEntityEvent}s in the input
 * throughput all its sets and maps
 * by using {@link LinkedHashSet}s.
 * 
 * <p>
 * All getters in this implementation provide direct access to the internal sets and maps,
 * and thus allows for external modifications.
 * If users prefer to modify the internal data structures, they are themselves responsible
 * for maintaining consistency.
 * 
 * @see SimEntityEvent#getQueue
 * @see SimEntityEvent#getJob
 *
 */
public class DefaultSimEntityEventMap
implements SimEntityEventMap
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private /* final */ void addSimEntityEvent (final SimEntityEvent simEntityEvent)
  {
    if (simEntityEvent == null)
      throw new IllegalArgumentException ();
    final double time = simEntityEvent.getTime ();
    final SimQueue queue = simEntityEvent.getQueue ();
    if (queue != null)
    {
      if (! this.timeSimQueueSimEntityEventMap.containsKey (time))
        this.timeSimQueueSimEntityEventMap.put (time, new HashMap<> ());
      if (! this.timeSimQueueSimEntityEventMap.get (time).containsKey (queue))
        this.timeSimQueueSimEntityEventMap.get (time).put (queue, new LinkedHashSet<> ());
      this.timeSimQueueSimEntityEventMap.get (time).get (queue).add (simEntityEvent);
      if (! this.simQueueTimeSimEntityEventMap.containsKey (queue))
        this.simQueueTimeSimEntityEventMap.put (queue, new TreeMap<> ());
      if (! this.simQueueTimeSimEntityEventMap.get (queue).containsKey (time))
        this.simQueueTimeSimEntityEventMap.get (queue).put (time, new LinkedHashSet<> ());
      this.simQueueTimeSimEntityEventMap.get (queue).get (time).add (simEntityEvent);
    }    
    final SimJob job = simEntityEvent.getJob ();
    if (job != null)
    {
      if (! this.timeSimJobSimEntityEventMap.containsKey (time))
        this.timeSimJobSimEntityEventMap.put (time, new HashMap<> ());
      if (! this.timeSimJobSimEntityEventMap.get (time).containsKey (job))
        this.timeSimJobSimEntityEventMap.get (time).put (job, new LinkedHashSet<> ());
      this.timeSimJobSimEntityEventMap.get (time).get (job).add (simEntityEvent);
      if (! this.simJobTimeSimEntityEventMap.containsKey (job))
        this.simJobTimeSimEntityEventMap.put (job, new TreeMap<> ());
      if (! this.simJobTimeSimEntityEventMap.get (job).containsKey (time))
        this.simJobTimeSimEntityEventMap.get (job).put (time, new LinkedHashSet<> ());
      this.simJobTimeSimEntityEventMap.get (job).get (time).add (simEntityEvent);
    }    
  }
  
  /** Creates a new {@link DefaultSimEntityEventMap}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimEvent}s.
   * 
   * @param events The set of events to parse (parsing is actually done in this constructor).
   * 
   */
  public DefaultSimEntityEventMap (final Set<? extends SimEvent> events)
  {
    if (events != null)
      for (final SimEvent event : events)
        if (event != null && (event instanceof SimEntityEvent))
          addSimEntityEvent ((SimEntityEvent) event);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimEntityEvent> entityEvents = new LinkedHashSet<> ();
  
  @Override
  public final Set<SimEntityEvent> getEntityEvents ()
  {
    return this.entityEvents;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS MAPS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final NavigableMap<Double, Map<SimQueue, Set<SimEntityEvent>>> timeSimQueueSimEntityEventMap = new TreeMap<> ();
  
  private final Map<SimQueue, NavigableMap<Double, Set<SimEntityEvent>>> simQueueTimeSimEntityEventMap = new HashMap<> ();
  
  private final NavigableMap<Double, Map<SimJob, Set<SimEntityEvent>>> timeSimJobSimEntityEventMap = new TreeMap<> ();
  
  private final Map<SimJob, NavigableMap<Double, Set<SimEntityEvent>>> simJobTimeSimEntityEventMap = new HashMap<> ();

  @Override
  public final NavigableMap<Double, Map<SimQueue, Set<SimEntityEvent>>> getTimeSimQueueSimEntityEventMap ()
  {
    return this.timeSimQueueSimEntityEventMap;
  }

  @Override
  public final Map<SimQueue, NavigableMap<Double, Set<SimEntityEvent>>> getSimQueueTimeSimEntityEventMap ()
  {
    return this.simQueueTimeSimEntityEventMap;
  }

  @Override
  public final NavigableMap<Double, Map<SimJob, Set<SimEntityEvent>>> getTimeSimJobSimEntityEventMap ()
  {
    return this.timeSimJobSimEntityEventMap;
  }

  @Override
  public final Map<SimJob, NavigableMap<Double, Set<SimEntityEvent>>> getSimJobTimeSimEntityEventMap ()
  {
    return this.simJobTimeSimEntityEventMap;
  }
  
}
