package nl.jdj.jqueues.r5.entity.jq;

import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;


/** A (tagging interface for a) {@link SimEntity} with QoS support.
 *
 * <p>
 * Every {@link SimQueue} and {@link SimJob}
 * implements this interface through
 * inheritance of all its default methods.
 * 
 * @param <J> The type of {@link SimJob}s supported.
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
public interface SimQoS<J extends SimJob, Q extends SimQueue>
{
  
  /** Returns the Java class used for QoS behavior.
   * 
   * <p>
   * If the {@link SimEntity} does not support QoS, {@code null} should be returned.
   * 
   * <p>
   * The default implementation returns {@code null}.
   * 
   * @return The Java class used for QoS behavior, may be {@code null}.
   * 
   */
  default Class getQoSClass ()
  {
    return null;
  }

  /** Gets the QoS value.
   * 
   * <p>
   * If the {@link SimEntity} does not support QoS, {@code null} should be returned.
   * 
   * <p>
   * The value returned, if non-{@code null} must be an object of the class or interface
   * returned by {@link #getQoSClass}.
   * 
   * <p>
   * The default implementation returns {@code null}.
   * 
   * @return The QoS value, may be {@code null}.
   * 
   */
  default Object getQoS ()
  {
    return null;
  }
  
}
