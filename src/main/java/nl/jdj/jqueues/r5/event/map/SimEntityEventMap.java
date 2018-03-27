package nl.jdj.jqueues.r5.event.map;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;

/** A representation of a (possibly ordered) set of {@link SimJQEvent}s with indexes in time and
 *  in {@link SimQueue} or {@link SimJob}.
 * 
 * <p>
 * This object holds a set of {@link SimJQEvent}s, accessible with {@link #getEntityEvents},
 * and maintains different map view on that set.
 * The contents of the set may change, but the map views have to be kept consistent.
 * 
 * <p>
 * Each {@link SimJQEvent} with non-{@code null} queue {@link SimJQEvent#getQueue},
 * must be in the {@link #getTimeSimQueueSimEntityEventMap} and {@link #getSimQueueTimeSimEntityEventMap}
 * structures.
 * Likewise, each {@link SimJQEvent} with non-{@code null} job {@link SimJQEvent#getJob},
 * must be in the {@link #getTimeSimJobSimEntityEventMap} and {@link #getSimJobTimeSimEntityEventMap}
 * structures.
 * 
 * <p>
 * Beware that most {@link SimJQEvent}s will be present in both the queue-maps and the job-maps.
 * 
 * <p>
 * No ordering is pre-specified for the various sets holding {@link SimJQEvent}s occurring simultaneously,
 * except that <i>if</i> a meaningful ordering structure is specified on {@link #getEntityEvents} in implementations,
 * all sets in the various maps must use the same ordering.
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
public interface SimEntityEventMap
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the set of all {@link SimJQEvent}s this object represents.
   * 
   * @return The set of all {@link SimJQEvent}s this object represents.
   * 
   */
  Set<SimJQEvent> getEntityEvents ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS MAPS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the {@link SimJQEvent}s indexed by (in that order) time and queue.
   * 
   * @return The {@link SimJQEvent}s indexed by (in that order) time and queue.
   * 
   */
  NavigableMap<Double, Map<SimQueue, Set<SimJQEvent>>> getTimeSimQueueSimEntityEventMap ();
  
  /** Returns the {@link SimJQEvent}s indexed by (in that order) queue and time.
   * 
   * @return The {@link SimJQEvent}s indexed by (in that order) queue and time.
   * 
   */
  Map<SimQueue, NavigableMap<Double, Set<SimJQEvent>>> getSimQueueTimeSimEntityEventMap ();
  
  /** Returns the {@link SimJQEvent}s indexed by (in that order) time and job.
   * 
   * @return The {@link SimJQEvent}s indexed by (in that order) time and job.
   * 
   */
  NavigableMap<Double, Map<SimJob, Set<SimJQEvent>>> getTimeSimJobSimEntityEventMap ();
  
  /** Returns the {@link SimJQEvent}s indexed by (in that order) job and time.
   * 
   * @return The {@link SimJQEvent}s indexed by (in that order) job and time.
   * 
   */
  Map<SimJob, NavigableMap<Double, Set<SimJQEvent>>> getSimJobTimeSimEntityEventMap ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
