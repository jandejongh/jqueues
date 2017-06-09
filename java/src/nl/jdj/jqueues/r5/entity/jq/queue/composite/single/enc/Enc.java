package nl.jdj.jqueues.r5.entity.jq.queue.composite.single.enc;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite.StartModel;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue}.
 *
 * <p>
 * This composite queue (precisely) mimics the {@link SimQueue} interface of the encapsulated queue.
 * 
 * <p>
 * The start model is set to (fixed) {@link StartModel#ENCAPSULATOR_QUEUE}.
 * 
 * <p>
 * Refer to {@link AbstractEncapsulatorSimQueue},
 * {@link AbstractSimQueueComposite}
 * and {@link SimQueueComposite}
 * for more details on encapsulated queues.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see SimQueueComposite
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
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
public class Enc
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends Enc>
  extends AbstractEncapsulatorSimQueue<DJ, DQ, J, Q>
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
   * 
   */
  public Enc
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queue, delegateSimJobFactory, false);
  }
  
  /** Returns a new {@link Enc} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link Enc} object on the same {@link SimEventList} with a copy of the encapsulated
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
  public Enc<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new Enc (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
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
