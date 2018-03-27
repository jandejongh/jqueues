package nl.jdj.jqueues.r5.entity.jq.queue.qos;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.AbstractPreemptiveSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.preemptive.PreemptionStrategy;
import nl.jdj.jqueues.r5.extensions.qos.SimQueueQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** An abstract base class for preemptive single-server queueing disciplines with explicit QoS support.
 * 
 * <p>
 * This class extends from {@link AbstractPreemptiveSimQueue} for preemption support, but
 * would also want to inherit from (the rather small) {@link AbstractSimQueueQoS}.
 * Hence, some code duplication is present.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
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
public abstract class AbstractPreemptiveSimQueueQoS
  <J extends SimJob, Q extends AbstractPreemptiveSimQueueQoS, P extends Comparable>
  extends AbstractPreemptiveSimQueue<J, Q>
  implements SimQueueQoS<J, Q, P>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a non-preemptive queue with given buffer size and number of servers, with explicit QoS support given an event list.
   *
   * @param eventList          The event list to use.
   * @param bufferSize         The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param numberOfServers    The number of servers (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param preemptionStrategy The preemption strategy, if {@code null}, the default is used (preemptive-resume).
   * @param qosClass           The Java class to use for QoS behavior, non-{@code null}.
   * @param defaultJobQoS      The default QoS value to use for non-QoS jobs, non-{@code null}. 
   * 
   * @throws IllegalArgumentException If the event list or one or both of the QoS arguments is <code>null</code>.
   *
   */
  protected AbstractPreemptiveSimQueueQoS
  (final SimEventList eventList,
    final int bufferSize,
    final int numberOfServers,
    final PreemptionStrategy preemptionStrategy,
    final Class<P> qosClass,
    final P defaultJobQoS)
  {
    super (eventList, bufferSize, numberOfServers, preemptionStrategy);
    this.qosClass = qosClass;
    this.defaultJobQoS = defaultJobQoS;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CODE BELOW IS COPY/PASTE FROM AbstractSimQueueQoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Class<P> qosClass;

  @Override
  public final Class<? extends P> getQoSClass ()
  {
    return this.qosClass;
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoSClass (final Class<? extends P> qosClass)
  {
    SimQueueQoS.super.setQoSClass (qosClass);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns {@code null}, since the QoS value of a queue has no meaning.
   * 
   * @return {@code null}.
   * 
   */
  @Override
  public final P getQoS ()
  {
    return null;
  }
  
  /** Calls super method on {@link SimQueueQoS} (in order to make implementation final).
   * 
   */
  @Override
  public final void setQoS (P qos)
  {
    SimQueueQoS.super.setQoS (qos);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (DEFAULT) JOB QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final P defaultJobQoS;

  @Override
  public final P getDefaultJobQoS ()
  {
    return this.defaultJobQoS;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}