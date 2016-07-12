package nl.jdj.jqueues.r5.entity.queue.composite.single.encap;

import java.util.Collections;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractBlackSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.DefaultDelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.DelegateSimJobFactory;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;
import nl.jdj.jsimulation.r5.SimEventList;

/** A black {@link AbstractEncapsulatorSimQueue} encapsulating a single {@link SimQueue}.
 *
 * <p>
 * This composite queue mimics the {@link SimQueue} interface of the encapsulated queue.
 * 
 * <p>The main purpose of this rather futile {@link SimQueue} is to test the maturity of the {@link SimQueue} interface and
 * its notifications: Can we reconstruct a {@link SimQueue} interface by acting on and monitoring another {@link SimQueue}?.
 * It is, however, also useful to extract a bare {@link SimQueue} interface at the {@code Java} level
 * from a much more complicated queue implementation.
 * 
 * <p>
 * This queue has non-default semantics for the waiting and service area of the black composite queue.
 * For more details, refer to {@link StartModel#ENCAPSULATOR_QUEUE}.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see StartModel
 * @see StartModel#ENCAPSULATOR_QUEUE
 * @see #setStartModel
 * 
 */
public class BlackEncapsulatorSimQueue
  <DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackEncapsulatorSimQueue>
  extends AbstractBlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a black encapsulator queue given an event list and a queue.
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
   * @see StartModel#COMPRESSED_TANDEM_2_QUEUE
   * @see #setStartModel
   * 
   */
  public BlackEncapsulatorSimQueue
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
    setStartModel (StartModel.ENCAPSULATOR_QUEUE);
  }
  
  /** Returns a new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
   *  queue and the same delegate-job factory.
   * 
   * @return A new {@link BlackEncapsulatorSimQueue} object on the same {@link SimEventList} with a copy of the encapsulated
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
  public BlackEncapsulatorSimQueue<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final SimQueue<DJ, DQ> encapsulatedQueueCopy = getEncapsulatedQueue ().getCopySimQueue ();
    return new BlackEncapsulatorSimQueue
      (getEventList (),
        encapsulatedQueueCopy,
        getDelegateSimJobFactory ());
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
  
  /** Returns "Enc[encapsulated queue]".
   * 
   * @return "Enc[encapsulated queue]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "Enc[" + getQueues ().iterator ().next () + "]";
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

}
