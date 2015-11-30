package nl.jdj.jqueues.r5.entity.job.qos;

import java.util.Map;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJobFactory;
import nl.jdj.jqueues.r5.extensions.qos.SimJobQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link SimJobQoS}s.
 * 
 * @param <J> The job type.
 * @param <Q> The queue type for jobs.
 * @param <P> The type used for QoS.
 * 
 * @see DefaultSimJobFactory
 * 
 */
public interface SimJobQoSFactory<J extends SimJobQoS, Q extends SimQueue, P extends Comparable>
extends SimJobFactory<J, Q>
{
  
  /** Creates a new {@link SimJobQoS} with given name, requested-service time map, and QoS maps.
   * 
   * @param eventList               The event list to use; may be (and in most case should be) <code>null</code>.
   * @param name                    The name of the new job; may be <code>null</code>.
   * @param requestedServiceTimeMap The requested service time for each key {@link SimQueue},
   *                                a <code>null</code> key can be used for unlisted {@link SimQueue}s.
   *                                If the map is <code>null</code> or no value could be found,
   *                                a factory default is used for the requested service time.
   * @param qosClass                The QoS class, may be {@code null}.
   * @param qos                     The QoS value, may be {@code null}.
   *                                The QoS value must be {@code null} or an instance of the QoS class.
   *                                The QoS value must be {@code null} is the QoS class is {@code null}.
   * 
   * @return The new job.
   * 
   */
  public J newInstance (SimEventList eventList,
    String name,
    Map<Q, Double> requestedServiceTimeMap,
    Class<? extends P> qosClass,
    P qos);
  
}
