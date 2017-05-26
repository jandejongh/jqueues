package nl.jdj.jqueues.r5.entity.jq.job.qos;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultSimJobQoS}s.
 *
 * @param <Q> The queue type for jobs.
 * @param <P> The type used for QoS.
 * 
 * @see DefaultSimJobQoS
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
public class DefaultSimJobQoSFactory<Q extends SimQueue, P extends Comparable>
implements SimJobQoSFactory<DefaultSimJobQoS, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns a new {@link DefaultSimJobQoS} without QoS support with given parameters.
   * 
   * @return A new {@link DefaultSimJobQoS} without QoS support with given parameters.
   * 
   * @see DefaultSimJobQoS#DefaultSimJobQoS
   * 
   */
  @Override
  public DefaultSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSimJobQoS (eventList, name, requestedServiceTimeMap, null, null);
  }

  /** Returns a new {@link DefaultSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @return A new {@link DefaultSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @see DefaultSimJobQoS#DefaultSimJobQoS
   * 
   */
  @Override
  public DefaultSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap,
    final Class<? extends P> qosClass,
    final P qos)
  {
    return new DefaultSimJobQoS (eventList, name, requestedServiceTimeMap, qosClass, qos);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
