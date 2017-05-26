package nl.jdj.jqueues.r5.entity.jq.job.visitslogging;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.qos.SimJobQoSFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultVisitsLoggingSimJobQoS}s.
 *
 * @param <Q> The queue type for jobs.
 * @param <P> The type used for QoS.
 * 
 * @see DefaultVisitsLoggingSimJobQoS
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
public class DefaultVisitsLoggingSimJobQoSFactory<Q extends SimQueue, P extends Comparable>
implements SimJobQoSFactory<DefaultVisitsLoggingSimJobQoS, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a new {@link DefaultVisitsLoggingSimJobQoS} without QoS support with given parameters.
   * 
   * @return A new {@link DefaultVisitsLoggingSimJobQoS} without QoS support with given parameters.
   * 
   * @see DefaultVisitsLoggingSimJobQoS#DefaultVisitsLoggingSimJobQoS
   * 
   */
  @Override
  public DefaultVisitsLoggingSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultVisitsLoggingSimJobQoS (eventList, name, requestedServiceTimeMap, null, null);
  }

  /** Returns a new {@link DefaultVisitsLoggingSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @return A new {@link DefaultVisitsLoggingSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @see DefaultVisitsLoggingSimJobQoS#DefaultVisitsLoggingSimJobQoS
   * 
   */
  @Override
  public DefaultVisitsLoggingSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap,
    final Class<? extends P> qosClass,
    final P qos)
  {
    return new DefaultVisitsLoggingSimJobQoS (eventList, name, requestedServiceTimeMap, qosClass, qos);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
