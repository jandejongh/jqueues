package nl.jdj.jqueues.r4.event.map;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.event.SimEntityEvent;

/** A representation of a (possibly ordered) set of {@link SimEntityEvent}s with indexes in time and
 *  in {@link SimQueue} or {@link SimJob}.
 * 
 * <p>
 * This object holds a set of {@link SimEntityEvent}s, accessible with {@link #getEntityEvents},
 * and maintains different map view on that set.
 * The contents of the set may change, but the map views have to be kept consistent.
 * 
 * <p>
 * Each {@link SimEntityEvent} with non-{@code null} queue {@link SimEntityEvent#getQueue},
 * must be in the {@link #getTimeSimQueueSimEntityEventMap} and {@link #getSimQueueTimeSimEntityEventMap}
 * structures.
 * Likewise, each {@link SimEntityEvent} with non-{@code null} job {@link SimEntityEvent#getJob},
 * must be in the {@link #getTimeSimJobSimEntityEventMap} and {@link #getSimJobTimeSimEntityEventMap}
 * structures.
 * 
 * <p>
 * Beware that most {@link SimEntityEvent}s will be present in both the queue-maps and the job-maps.
 * 
 * <p>
 * No ordering is pre-specified for the various sets holding {@link SimEntityEvent}s occurring simultaneously,
 * except that <i>if</i> a meaningful ordering structure is specified on {@link #getEntityEvents} in implementations,
 * all sets in the various maps must use the same ordering.
 * 
 * @see SimEntityEvent#getQueue
 * @see SimEntityEvent#getJob
 *
 */
public interface SimEntityEventMap
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the set of all {@link SimEntityEvent}s this object represents.
   * 
   * @return The set of all {@link SimEntityEvent}s this object represents.
   * 
   */
  public Set<SimEntityEvent> getEntityEvents ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENTITY EVENTS MAPS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the {@link SimEntityEvent}s indexed by (in that order) time and queue.
   * 
   * @return The {@link SimEntityEvent}s indexed by (in that order) time and queue.
   * 
   */
  public NavigableMap<Double, Map<SimQueue, Set<SimEntityEvent>>> getTimeSimQueueSimEntityEventMap ();
  
  /** Returns the {@link SimEntityEvent}s indexed by (in that order) queue and time.
   * 
   * @return The {@link SimEntityEvent}s indexed by (in that order) queue and time.
   * 
   */
  public Map<SimQueue, NavigableMap<Double, Set<SimEntityEvent>>> getSimQueueTimeSimEntityEventMap ();
  
  /** Returns the {@link SimEntityEvent}s indexed by (in that order) time and job.
   * 
   * @return The {@link SimEntityEvent}s indexed by (in that order) time and job.
   * 
   */
  public NavigableMap<Double, Map<SimJob, Set<SimEntityEvent>>> getTimeSimJobSimEntityEventMap ();
  
  /** Returns the {@link SimEntityEvent}s indexed by (in that order) job and time.
   * 
   * @return The {@link SimEntityEvent}s indexed by (in that order) job and time.
   * 
   */
  public Map<SimJob, NavigableMap<Double, Set<SimEntityEvent>>> getSimJobTimeSimEntityEventMap ();
  
}
