package nl.jdj.jqueues.r5.entity.queue.composite.single.enc;

import java.util.Collections;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntityOperation;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite.StartModel;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link BlackSimQueueComposite} encapsulating a single {@link SimQueue} of which job starts are hidden.
 *
 * <p>
 * This composite queue mimics the {@link SimQueue} interface of the encapsulated queue.
 * 
 * <p>
 * The main purpose of this {@link SimQueue} is to provide a means to "hide" job starts on the encapsulated queue
 * and concentrate on arrival, drops, revocations and departures only.
 * This allows for instance to test the equality of job visits of {@link SimQueue} implementations
 * that are equal in terms of sojourn times, but not in terms of start times (or in terms of the occurrence of job starts).
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the black composite queue.
 * For more details, refer to {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see BlackSimQueueComposite
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_HIDE_START_QUEUE
 * @see #setStartModel
 * @see BlackEncapsulatorSimQueue
 * 
 */
public class BlackEncapsulatorHideStartSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackEncapsulatorHideStartSimQueue>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black encapsulator queue that hides its sub-queue starts given an event list and a queue.
   *
   * <p>
   * The constructor sets the {@link StartModel} to {@link StartModel#ENCAPSULATOR_HIDE_START_QUEUE}.
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
   * @see StartModel#ENCAPSULATOR_HIDE_START_QUEUE
   * @see #setStartModel
   * 
   */
  public BlackEncapsulatorHideStartSimQueue
  (final SimEventList eventList,
   final DQ queue,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList,
      Collections.singleton (queue),
      new SimQueueSelector<J, DQ> ()
      {
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
    setStartModel (StartModel.ENCAPSULATOR_HIDE_START_QUEUE);
    // Find the operations on the encapsulated queue that we do not know, and install a delegate for it.
    for (final SimEntityOperation oDQueue : (Set<SimEntityOperation>) queue.getRegisteredOperations ())
      if (! getRegisteredOperations ().contains (oDQueue))
        registerDelegatedOperation (oDQueue, new DelegatedSimQueueOperation (this, queue, oDQueue));
    // Register unknown notifications from the encapsulated queue.
    for (final SimEntitySimpleEventType.Member nDQueue :
      (Set<SimEntitySimpleEventType.Member>) queue.getRegisteredNotificationTypes ())
      if (! getRegisteredNotificationTypes ().contains (nDQueue))
        registerNotificationType (nDQueue, null);
  }
  
  /** Returns a new {@link BlackEncapsulatorHideStartSimQueue} object on the same {@link SimEventList}
   *  with a copy of the encapsulated queue and the same delegate-job factory.
   * 
   * @return A new {@link BlackEncapsulatorHideStartSimQueue} object on the same {@link SimEventList}
   *         with a copy of the encapsulated queue and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queue could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getEncapsulatedQueue
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackEncapsulatorHideStartSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new BlackEncapsulatorHideStartSimQueue (getEventList (), encapsulatedQueueCopy, getDelegateSimJobFactory ());
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
    return getQueues ().iterator ().next ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "EncHS[encapsulated queue]".
   * 
   * @return "EncHS[encapsulated queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "EncHS[" + getQueues ().iterator ().next () + "]";
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
  
}
