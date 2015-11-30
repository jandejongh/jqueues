package nl.jdj.jqueues.r5.entity.job.selflistening;

import nl.jdj.jqueues.r5.entity.job.qos.*;
import java.util.Map;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultSelfListeningSimJobQoS}s.
 *
 * @param <Q> The queue type for jobs.
 * @param <P> The type used for QoS.
 * 
 * @see DefaultSelfListeningSimJobQoS
 * 
 */
public class DefaultSelfListeningSimJobQoSFactory<Q extends SimQueue, P extends Comparable>
implements SimJobQoSFactory<DefaultSelfListeningSimJobQoS, Q, P>
{

  /** Returns a new {@link DefaultSelfListeningSimJobQoS} without QoS support with given parameters.
   * 
   * @return A new {@link DefaultSelfListeningSimJobQoS} without QoS support with given parameters.
   * 
   * @see DefaultSelfListeningSimJobQoS#DefaultSelfListeningSimJobQoS
   * 
   */
  @Override
  public DefaultSelfListeningSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSelfListeningSimJobQoS (eventList, name, requestedServiceTimeMap, null, null);
  }

  /** Returns a new {@link DefaultSelfListeningSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @return A new {@link DefaultSelfListeningSimJobQoS} with explicit QoS support with given parameters.
   * 
   * @see DefaultSelfListeningSimJobQoS#DefaultSelfListeningSimJobQoS
   * 
   */
  @Override
  public DefaultSelfListeningSimJobQoS newInstance
  (final SimEventList eventList,
    final String name,
    final Map<Q, Double> requestedServiceTimeMap,
    final Class<? extends P> qosClass,
    final P qos)
  {
    return new DefaultSelfListeningSimJobQoS (eventList, name, requestedServiceTimeMap, qosClass, qos);
  }
  
}
