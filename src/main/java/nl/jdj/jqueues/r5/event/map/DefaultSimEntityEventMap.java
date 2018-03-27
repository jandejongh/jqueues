package nl.jdj.jqueues.r5.event.map;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jsimulation.r5.SimEvent;

/** A default {@link SimEntityEventMap}.
 * 
 * <p>
 * A {@link DefaultSimEntityEventMap} takes a set of {@link SimEvent}s as input to its (set-argument) constructor,
 * and constructs the maps as specified in {@link SimEntityEventMap}
 * from all {@link SimJQEvent} it finds that have non-{@code null} job or queue (or both).
 * 
 * <p>
 * This implementation constructs an internal copy of the input set,
 * and, for what it's worth,
 * maintains the order of simultaneous {@link SimJQEvent}s in the input of the set-argument constructor
 * throughput all its sets and maps
 * by using {@link LinkedHashSet}s.
 * 
 * <p>
 * A map-argument constructor allows for the construction of objects from an unambiguous event schedule.
 * 
 * <p>
 * All getters in this implementation provide direct access to the internal sets and maps,
 * and thus allows for external modifications.
 * If users prefer to modify the internal data structures, they are themselves responsible
 * for maintaining consistency.
 * 
 * @see SimJQEvent
 * @see SimJQEvent#getQueue
 * @see SimJQEvent#getJob
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class DefaultSimEntityEventMap
implements SimEntityEventMap
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private void addSimEntityEvent (final SimJQEvent simEntityEvent)
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
        if (event != null && (event instanceof SimJQEvent))
          addSimEntityEvent ((SimJQEvent) event);
  }
  
  /** Creates a new {@link DefaultSimEntityEventMap}, filling out all the internal sets and maps from scanning an unambiguous
   *  schedule of {@link SimEvent}s represented as a {@code Map}.
   * 
   * <p>
   * The order of {@link SimEvent}s in the value sets must be deterministic and must represent the "processing order"
   * for the simultaneous events in the set.
   * 
   * @param <E>    The event type.
   * @param events The events to parse represented as a {@code Map} (parsing is actually done in this constructor).
   * 
   * @throws IllegalArgumentException If the time on an event does not match its corresponding key value.
   * 
   * @see SimEvent#getTime
   * 
   */
  public <E extends SimEvent> DefaultSimEntityEventMap (final Map<Double, Set<E>> events)
  {
    if (events != null)
      for (final Entry<Double, Set<E>> entry : events.entrySet ())
        for (final SimEvent event : entry.getValue ())
          if (event != null && (event instanceof SimJQEvent))
          {
            if (entry.getKey () != event.getTime ())
              throw new IllegalArgumentException ();
             else
              // We can safely use {@link #addSimEntityEvent} because it preserves insertion order for simulateneous events.
               addSimEntityEvent ((SimJQEvent) event);
          }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimJQEvent> entityEvents = new LinkedHashSet<> ();
  
  @Override
  public final Set<SimJQEvent> getEntityEvents ()
  {
    return this.entityEvents;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS MAPS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final NavigableMap<Double, Map<SimQueue, Set<SimJQEvent>>> timeSimQueueSimEntityEventMap = new TreeMap<> ();
  
  private final Map<SimQueue, NavigableMap<Double, Set<SimJQEvent>>> simQueueTimeSimEntityEventMap = new HashMap<> ();
  
  private final NavigableMap<Double, Map<SimJob, Set<SimJQEvent>>> timeSimJobSimEntityEventMap = new TreeMap<> ();
  
  private final Map<SimJob, NavigableMap<Double, Set<SimJQEvent>>> simJobTimeSimEntityEventMap = new HashMap<> ();

  @Override
  public final NavigableMap<Double, Map<SimQueue, Set<SimJQEvent>>> getTimeSimQueueSimEntityEventMap ()
  {
    return this.timeSimQueueSimEntityEventMap;
  }

  @Override
  public final Map<SimQueue, NavigableMap<Double, Set<SimJQEvent>>> getSimQueueTimeSimEntityEventMap ()
  {
    return this.simQueueTimeSimEntityEventMap;
  }

  @Override
  public final NavigableMap<Double, Map<SimJob, Set<SimJQEvent>>> getTimeSimJobSimEntityEventMap ()
  {
    return this.timeSimJobSimEntityEventMap;
  }

  @Override
  public final Map<SimJob, NavigableMap<Double, Set<SimJQEvent>>> getSimJobTimeSimEntityEventMap ()
  {
    return this.simJobTimeSimEntityEventMap;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
