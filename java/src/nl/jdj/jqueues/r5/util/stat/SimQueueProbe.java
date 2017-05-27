package nl.jdj.jqueues.r5.util.stat;

import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** Object capable of obtaining a value (<code>double</code>) from a {@link SimQueue}.
 * 
 * @param <Q> The type of {@link SimQueue}s supported.
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
public interface SimQueueProbe<Q extends SimQueue>
{
  
  /** Gets the value from given queue.
   * 
   * @param queue The queue.
   * 
   * @return The value.
   * 
   * @throws IllegalArgumentException If the argument is <code>null</code> or of invalid type for this probe.
   * 
   */
  public double get (Q queue);
  
}
