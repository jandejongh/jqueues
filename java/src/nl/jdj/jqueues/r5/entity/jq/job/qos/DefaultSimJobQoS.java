package nl.jdj.jqueues.r5.entity.jq.job.qos;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.DefaultSimJob;
import nl.jdj.jqueues.r5.extensions.qos.SimJobQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link DefaultSimJob} with explicit QoS support.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 * @see DefaultSimJob
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
public class DefaultSimJobQoS<J extends DefaultSimJobQoS, Q extends SimQueue, P extends Comparable>
extends DefaultSimJob<J, Q>
implements SimJobQoS<J, Q, P>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link DefaultSimJobQoS} with given event list, name, requested service-time map, and QoS structure.
   * 
   * @param eventList               The event list to use, may be {@code null}.
   * @param name                    The name of the job, may be <code>null</code>.
   * @param requestedServiceTimeMap See {@link DefaultSimJob#DefaultSimJob}.
   * @param qosClass                The QoS class, may be {@code null}.
   * @param qos                     The QoS value, may be {@code null}.
   *                                The QoS value must be {@code null} or an instance of the QoS class.
   *                                The QoS value must be {@code null} if the QoS class is {@code null}.
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If the QoS class is {@code null} and the QoS value is <i>not</i>,
   *                                  or if the QoS value is not an instance of the QoS class.
   * 
   * @see Class#isInstance
   * 
   */
  public DefaultSimJobQoS (final SimEventList eventList, final String name,
    final Map<Q, Double> requestedServiceTimeMap,
    final Class<P> qosClass, final P qos)
  {
    super (eventList, name, requestedServiceTimeMap);
    if (qosClass == null && qos != null)
      throw new IllegalArgumentException ();
    if (qosClass != null && qos != null && ! qosClass.isInstance (qos))
      throw new IllegalArgumentException ();
    this.qosClass = qosClass;
    this.qos = qos;
  }
  
  /** Creates a new {@link DefaultSimJobQoS} with given event list, name, fixed requested service time, and QoS structure.
   * 
   * @param eventList            The event list to use, may be {@code null}.
   * @param name                 The name of the job, may be <code>null</code>.
   * @param requestedServiceTime See {@link DefaultSimJob#DefaultSimJob}.
   * @param qosClass             The QoS class, may be {@code null}.
   * @param qos                  The QoS value, may be {@code null}.
   *                             The QoS value must be {@code null} or an instance of the QoS class.
   *                             The QoS value must be {@code null} if the QoS class is {@code null}.
   * 
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If the QoS class is {@code null} and the QoS value is <i>not</i>,
   *                                  or if the QoS value is not an instance of the QoS class.
   * 
   * @see Class#isInstance
   * 
   */
  public DefaultSimJobQoS (final SimEventList eventList, final String name,
    final double requestedServiceTime,
    final Class<P> qosClass, final P qos)
  {
    super (eventList, name, requestedServiceTime);
    if (qosClass == null && qos != null)
      throw new IllegalArgumentException ();
    if (qosClass != null && qos != null && ! qosClass.isInstance (qos))
      throw new IllegalArgumentException ();
    this.qosClass = qosClass;
    this.qos = qos;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private Class<? extends P> qosClass = null;

  @Override
  public final Class<? extends P> getQoSClass ()
  {
    return this.qosClass;
  }

  @Override
  public final void setQoSClass (final Class qosClass)
  {
    this.qosClass = qosClass;
    this.qos = null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private P qos = null;
  
  @Override
  public final P getQoS ()
  {
    return this.qos;
  }

  @Override
  public final void setQoS (final P qos)
  {
    if (this.qosClass == null && qos != null)
      throw new IllegalArgumentException ();
    if (qos != null && ! this.qosClass.isInstance (qos))
      throw new IllegalArgumentException ();
    this.qos = qos;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
