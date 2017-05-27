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

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue}.
 *
 * <p>
 * This composite queue mimics the {@link SimQueue} interface of the encapsulated queue.
 * 
 * <p>The main purpose of this apparently rather futile {@link SimQueue}
 * is to test the maturity of the {@link SimQueue} interface and its notifications:
 * Can we reconstruct a {@link SimQueue} interface by acting on and monitoring another {@link SimQueue}?.
 * It is, however, also useful to extract a bare {@link SimQueue} interface at the {@code Java} level
 * from a much more complicated queue implementation.
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the composite queue.
 * For more details, refer to {@link StartModel#ENCAPSULATOR_QUEUE}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
 * @see #setStartModel
 * @see EncapsulatorHideStartSimQueue
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
public class EncapsulatorSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends EncapsulatorSimQueue>
  extends AbstractSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue given an event list and a queue.
   *
   * <p>
   * The constructor sets the {@link StartModel} to {@link StartModel#ENCAPSULATOR_QUEUE}.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list or the queue is <code>null</code>.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see StartModel
   * @see StartModel#ENCAPSULATOR_QUEUE
   * @see #setStartModel
   * 
   */
  public EncapsulatorSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
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
      delegateSimJobFactory);
    setStartModel (StartModel.ENCAPSULATOR_QUEUE);
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
  
  /** Returns a new {@link EncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link EncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *         queue and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public EncapsulatorSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new EncapsulatorSimQueue (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
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
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Enc[encapsulated queue]".
   * 
   * @return "Enc[encapsulated queue]".
   * 
   * @see #getEncapsulatedQueue
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "Enc[" + getEncapsulatedQueue () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QoS / QoS CLASS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return super.getServiceTimeForJob (job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
