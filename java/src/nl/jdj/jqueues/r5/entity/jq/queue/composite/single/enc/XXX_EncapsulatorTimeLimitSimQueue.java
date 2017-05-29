package nl.jdj.jqueues.r5.entity.jq.queue.composite.single.enc;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite.StartModel;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link SimQueueComposite} encapsulating a single {@link SimQueue}
 *  equipped with limits on waiting time, service time and sojourn time.
 *
 * <p>
 * XXX UNFINISHED
 * 
 * <p>
 * This composite queue mimics (precisely) the {@link SimQueue} interface of the encapsulated queue,
 * auto-revoking real jobs if their waiting, service of sojourn time exceeds a given, fixed limit.
 * Auto-revocation is done through {@link #autoRevoke}. 
 * On the encapsulated queue, the job is removed through {@link SimQueue#revoke}.
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
 * @see #setStartModel
 * @see EncapsulatorSimQueue
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
public class XXX_EncapsulatorTimeLimitSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends EncapsulatorSimQueue>
  extends AbstractEncapsulatorSimQueue<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an encapsulator queue given an event list and a queue and limits on waiting time, service time and sojourn time.
   *
   * <p>
   * The constructor sets the {@link StartModel} to {@link StartModel#ENCAPSULATOR_QUEUE}.
   * 
   * @param eventList             The event list to use.
   * @param queue                 The encapsulated queue.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * @param maxWaitingTime        The maximum waiting time, non-negative, {@link Double#POSITIVE_INFINITY} is allowed.
   * @param maxServiceTime        The maximum service time, non-negative, {@link Double#POSITIVE_INFINITY} is allowed.
   * @param maxSojournTime        The maximum sojourn time, non-negative, {@link Double#POSITIVE_INFINITY} is allowed.
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
  public XXX_EncapsulatorTimeLimitSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory,
   final double maxWaitingTime,
   final double maxServiceTime,
   final double maxSojournTime)
  {
    super (eventList, queue, delegateSimJobFactory, false);
    if (maxWaitingTime < 0 || maxServiceTime < 0 || maxSojournTime < 0)
      throw new IllegalArgumentException ();
    this.maxWaitingTime = maxWaitingTime;
    this.maxServiceTime = maxServiceTime;
    this.maxSojournTime = maxSojournTime;
    throw new UnsupportedOperationException ();
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
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "EncTL(maxWai,maxSer,maxSoj)[encapsulated queue]".
   * 
   * @return "EncTL(maxWai,maxSer,maxSoj)[encapsulated queue]".
   * 
   * @see #getEncapsulatedQueue
   * @see #getMaxWaitingTime
   * @see #getMaxServiceTime
   * @see #getMaxSojournTime
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "EncTL("
      + getMaxWaitingTime ()
      + ","
      + getMaxServiceTime ()
      + ","
      + getMaxSojournTime ()
      + ")"
      + "[" + getEncapsulatedQueue () + "]";
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
  // MAX WAITING TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double maxWaitingTime;
  
  public final double getMaxWaitingTime ()
  {
    return this.maxWaitingTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double maxServiceTime;
  
  public final double getMaxServiceTime ()
  {
    return this.maxServiceTime;
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAX SOJOURN TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double maxSojournTime;
  
  public final double getMaxSojournTime ()
  {
    return this.maxSojournTime;
  }  
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
