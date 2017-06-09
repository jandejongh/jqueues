package nl.jdj.jqueues.r5.entity.jq.queue.composite.single.enc;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntityOperation;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite.StartModel;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue} (abstract implementation).
 *
 * <p>
 * This composite queue takes as single {@link SimQueue} as argument (the <i>encapsulated</i> queue, or <i>the</i> sub-queue),
 * and mimics that queue's interface in all its aspects (by default).
 * Specific concrete implementations seek to (1) subtly modify the behavior of the embedded queue,
 * and/or (2) subtly transform the view on the embedded queue.
 * 
 * <p>
 * This base class implements the case in which the behavior of the composite queue precisely matches that of the
 * encapsulated queue (including non-standard operations and notifications), with the single option of hiding the start-events
 * on the delegate queue.
 * It inherits the QoS structure from the encapsulated queue.
 * 
 * <p>
 * The main purpose of this apparently rather futile {@link SimQueue}
 * is to test the maturity of the {@link SimQueue} interface and its notifications:
 * Can we reconstruct a {@link SimQueue} interface by acting on and monitoring another {@link SimQueue}?.
 * It is, however, also useful to extract a bare {@link SimQueue} interface at the {@code Java} level
 * from a much more complicated queue implementation.
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the composite queue.
 * It sets its {@link StartModel} to either
 * {@link StartModel#ENCAPSULATOR_QUEUE} or {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
 * depending upon its parameters upon construction.
 * 
 * <p>
 * Most of the complexity of the encapsulator queue is dealt with by {@link AbstractSimQueueComposite},
 * through the two start models mentioned above.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
 * @see StartModel#ENCAPSULATOR_HIDE_START_QUEUE
 * @see Enc
 * @see EncHS
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
public abstract class AbstractEncapsulatorSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractEncapsulatorSimQueue>
  extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue given an event list and a sub-queue, mimicking the sub-queue's behavior.
   *
   * <p>
   * This composite queue mimics the QoS behavior of the encapsulated queue,
   * and all its operation and notification types.
   * 
   * <p>
   * The constructor sets the {@link StartModel} to
   * {@link StartModel#ENCAPSULATOR_QUEUE}
   * or {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   * depending on the {@code hideStart} argument.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param hideStart             Whether or not to hide job-starts to the composite queue.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see StartModel
   * @see StartModel#ENCAPSULATOR_QUEUE
   * @see StartModel#ENCAPSULATOR_HIDE_START_QUEUE
   * @see #getRegisteredOperations
   * @see #registerDelegatedOperation
   * @see #getRegisteredNotificationTypes
   * @see #registerNotificationType
   * 
   */
  public AbstractEncapsulatorSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory,
   final boolean hideStart)
  {
    super (eventList,
      Collections.singleton (queue),
      new SimQueueSelector<J, DQ> ()
      {
        @Override
        public void resetSimQueueSelector ()
        {
        }
        @Override
        public DQ selectFirstQueue (final double time, final J job)
        {
          return queue;
        }
        @Override
        public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
        {
          if (previousQueue != queue)
            throw new IllegalArgumentException ();
          return null;
        }
      },
      delegateSimJobFactory,
      hideStart ? StartModel.ENCAPSULATOR_HIDE_START_QUEUE : StartModel.ENCAPSULATOR_QUEUE);
    // Find the operations on the encapsulated queue that we do not know, and install a delegate for it.
    for (final SimEntityOperation oDQueue : (Set<SimEntityOperation>) queue.getRegisteredOperations ())
      if (! getRegisteredOperations ().contains (oDQueue))
        registerDelegatedOperation (oDQueue, new DelegatedSimQueueOperation (this, queue, oDQueue, this.realDelegateJobMapper));
    // Register unknown notifications from the encapsulated queue.
    for (final SimEntitySimpleEventType.Member nDQueue :
      (Set<SimEntitySimpleEventType.Member>) queue.getRegisteredNotificationTypes ())
      if (! getRegisteredNotificationTypes ().contains (nDQueue))
        registerNotificationType (nDQueue, null);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ENCAPSULATED QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the encapsulated queue.
   * 
   * @return The encapsulated queue, non-{@code null}.
   * 
   */
  public final SimQueue<DJ, DQ> getEncapsulatedQueue ()
  {
    return getQueue (0);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the QoS value of the encapsulated queue.
   * 
   * @return The QoS value of the encapsulated queue.
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return getEncapsulatedQueue ().getQoS ();
  }

  /** Returns the QoS class of the encapsulated queue.
   * 
   * @return The QoS class of the encapsulated queue.
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return getEncapsulatedQueue ().getQoSClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
