package nl.jdj.jqueues.r5.entity.queue.composite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.collector.BlackDropCollectorSimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.single.enc.BlackEncapsulatorSimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.event.simple.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link BlackSimQueueComposite}.
 *
 * <p>
 * This abstract base class registers the start model, the drop destination queue
 * and all administration related to creating delegate jobs and mapping between
 * real and delegate jobs.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public abstract class AbstractBlackSimQueueComposite
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractBlackSimQueueComposite>
extends AbstractSimQueueComposite<DJ, DQ, J, Q>
implements BlackSimQueueComposite<DJ, DQ, J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract black network of queues.
   * 
   * <p>
   * After calling the super constructor,
   * this methods sets the delegate job factory,
   * and inhibits future automatic resets from the event list on all sub-queues,
   * since this object will take care of that (and depends on the absence of "independent" resets
   * of the sub-queues).
   * It then creates a new {@link MultiSimQueueNotificationProcessor} for all sub-queues,
   * and registers (the abstract) {@link #processSubQueueNotifications} as its processor.
   * Finally, it invokes {@link #resetEntitySubClassLocal}.
   * 
   * @param eventList             The event list to be shared between this queue and the inner queues.
   * @param queues                A set holding the "inner" queues.
   * @param simQueueSelector      The object for routing jobs through the network of embedded queues;
   *                                if {@code null}, no sub-queues will be visited.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    or the <code>queue</code> argument is <code>null</code> or has <code>null</code> members.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimEntity#setIgnoreEventListReset
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processSubQueueNotifications
   * @see #resetEntitySubClassLocal
   * 
   */
  protected AbstractBlackSimQueueComposite
  (final SimEventList eventList,
    final Set<DQ> queues,
    final SimQueueSelector simQueueSelector,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, simQueueSelector);
    this.delegateSimJobFactory = ((delegateSimJobFactory == null) ? new DefaultDelegateSimJobFactory () : delegateSimJobFactory);
    for (final DQ q : getQueues ())
      q.setIgnoreEventListReset (true);
    final MultiSimQueueNotificationProcessor<DJ, DQ>  subQueueEventProcessor =
      new MultiSimQueueNotificationProcessor<> (getQueues ());
    subQueueEventProcessor.setProcessor (this::processSubQueueNotifications);
    resetEntitySubClassLocal ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START MODEL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private StartModel startModel = StartModel.LOCAL;
  
  @Override
  public final StartModel getStartModel ()
  {
    return this.startModel;
  }
  
  /** Sets the start model of this composite queue (for use by sub-classes only).
   * 
   * <p>
   * This method should only be used by sub-classes upon construction, if needed, and should not
   * be called afterwards. The start-model setting should survive queue resets.
   * 
   * @param startModel The new start model (non-{@code null}).
   * 
   * @throws IllegalArgumentException If the argument is {@code null},
   *                                  or {@link BlackSimQueueComposite.StartModel#ENCAPSULATOR_QUEUE} is chosen
   *                                  while there are fewer or more than <i>one</i> sub-queues,
   *                                  or {@link BlackSimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE} is chosen
   *                                  while there are fewer or more than <i>two</i> sub-queues,
   * 
   * @see BlackEncapsulatorSimQueue
   * @see BlackCompressedTandem2SimQueue
   * 
   */
  protected final void setStartModel (final StartModel startModel)
  {
    if (startModel == null)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.ENCAPSULATOR_QUEUE && getQueues ().size () != 1)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.COMPRESSED_TANDEM_2_QUEUE && getQueues ().size () != 2)
      throw new IllegalArgumentException ();
    this.startModel = startModel;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP DESTINATION QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private DQ dropDestinationQueue = null;
  
  /** Returns an optional destination (delegate) {@link SimQueue} for dropped jobs.
   * 
   * <p>
   * Normally, dropping a delegate job as noted by {@link #notifyDrop} results in dropping the corresponding real job.
   * By setting the "drop queue", the default behavior can be changed (but only from sub-classes),
   * and such jobs can be sent to one of the sub-queues as an arrival.
   * 
   * <p>
   * The default value is <code>null</code>, implying that the real job is to be dropped if its delegate job is dropped.
   * 
   * @return Any {@link SimQueue} in {@link #getQueues} to which the dropped delegate job is to be sent as an arrival,
   *           or <code>null</code> if the corresponding real job is to be dropped as well.
   * 
   * @see #notifyDrop
   * 
   */
  protected final DQ getDropDestinationQueue ()
  {
    return this.dropDestinationQueue;
  }
  
  /** Sets the destination sub-queue for dropped delegate jobs.
   * 
   * <p>
   * The drop destination queue is only to be used by sub-classes for specific behavior related to dropping of jobs
   * on sub-queues.
   * It should be set at most once (upon construction) and it should survive entity resets.
   * 
   * <p>
   * Note, however, that some (abstract) sub-classes simply do not support the notion of drop-destination queues.
   * So, if you want to use this feature in a class, make sure your direct super-class supports it;
   * {@link BlackEncapsulatorSimQueue} and {@link BlackCompressedTandem2SimQueue} do not support this notion, for instance.
   * If drop-destination queues are not supported by the super-class, consider the use of {@link BlackDropCollectorSimQueue}.
   * 
   * <p>
   * In other words, this method is not for general-purpose use and its effects may be nil,
   * unless explicitly stated by the super-class you use.
   * 
   * @param queue The destination sub-queue for dropped delegate jobs; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or not a sub-queue of this composite queue.
   * 
   * @see #getDropDestinationQueue
   * @see BlackDropCollectorSimQueue
   * 
   */
  protected final void setDropDestinationQueue (final DQ queue)
  {
    if (queue == null || ! getQueues ().contains (queue))
      throw new IllegalArgumentException ();
    this.dropDestinationQueue = queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELEGATE SIMJOBS AND REAL/DELEGATE SIMJOB MAPPINGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The factory to create delegate {@link SimJob}s, non-<code>null</code>.
   * 
   */
  private DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory;
  
  @Override
  public final DelegateSimJobFactory<? extends DJ, DQ, J, Q> getDelegateSimJobFactory ()
  {
    return this.delegateSimJobFactory;
  }
  
  @Override
  public final void setDelegateSimJobFactory (final DelegateSimJobFactory<? extends DJ, DQ, J, Q> delegateSimJobFactory)
  {
    if (delegateSimJobFactory == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobFactory = delegateSimJobFactory;
  }
  
  /** Maps "real" jobs onto delegate jobs.
   * 
   * Kept in sync with {@link realSimJobMap}.
   * 
   */
  private final Map<J, DJ> delegateSimJobMap = new HashMap<> ();
  
  /** Maps delegate jobs onto "real" jobs.
   * 
   * Kept in sync with {@link realSimJobMap}.
   * 
   */
  private final Map<DJ, J> realSimJobMap = new HashMap<> ();
  
  /** Returns the delegate job for given real job.
   * 
   * Performs various sanity checks on the argument and the internal administration consistency.
   * 
   * @param realJob The real job.
   * 
   * @return The delegate job.
   * 
   * @throws IllegalStateException If sanity checks fail.
   * 
   */
  protected final DJ getDelegateJob (final J realJob)
  {
    if (realJob == null)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    final DJ delegateJob = this.delegateSimJobMap.get (realJob);
    if (delegateJob == null)
      throw new IllegalStateException ();
    if (this.realSimJobMap.get (delegateJob) != realJob)
      throw new IllegalStateException ();
    return delegateJob;
  }

  /** Returns the real job for given delegate job, and asserts its presence on the given (sub-)queue, or on no (sub-)queue at all.
   * 
   * <p>
   * By using this method, you assume that the delegate job is present on the given sub-{@code queue},
   * or, if passing a {@code null} argument for the {@code queue},
   * on no sub-queue at all.
   * This method will rigorously check your assumption and happily throw an {@link IllegalStateException}
   * if your assumption proves wrong.
   * Clearly, this method is primarily intended for internal consistency checking.
   * 
   * <p>
   * Performs various additional sanity checks on the arguments and the internal administration consistency.
   * 
   * @param delegateJob The delegate job.
   * @param queue       The queue at which the delegate job currently resides,
   *                    {@code null} if it is supposed to reside on <i>none</i> of the )sub-)queues..
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found,
   *                               or where assumption on the delegate-job whereabout proves to be wrong.
   * 
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob)
   * 
   */
  protected final J getRealJob (final DJ delegateJob, final DQ queue)
  {
    if (delegateJob == null || (queue != null && ! getQueues ().contains (queue)))
      throw new IllegalStateException ();
    final J realJob = this.realSimJobMap.get (delegateJob);
    if (realJob == null)
      throw new IllegalStateException ();
    if (this.delegateSimJobMap.get (realJob) != delegateJob)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    if (queue == null)
    {
      for (final DQ subQueue : getQueues ())
        if (subQueue.getJobs ().contains (delegateJob))
          throw new IllegalStateException ();
    }
    else if (! queue.getJobs ().contains (delegateJob))
      throw new IllegalStateException ();
    return realJob;
  }
  
  /** Returns the real job for given delegate job.
   * 
   * <p>
   * Performs various sanity checks on the arguments and the internal administration consistency.
   * 
   * @param delegateJob The delegate job.
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found.
   * 
   * @see #getRealJob(nl.jdj.jqueues.r5.SimJob, nl.jdj.jqueues.r5.SimQueue)
   * 
   */
  protected final J getRealJob (final DJ delegateJob)
  {
    if (delegateJob == null)
      throw new IllegalStateException ();
    final J realJob = this.realSimJobMap.get (delegateJob);
    if (realJob == null)
      throw new IllegalStateException ();
    if (this.delegateSimJobMap.get (realJob) != delegateJob)
      throw new IllegalStateException ();
    if (! this.jobQueue.contains (realJob))
      throw new IllegalStateException ();
    return realJob;
  }
  
  /** Adds a real job, creating its delegate job.
   * 
   * @param realJob The real job, non-{@code null}.
   * 
   * @throws IllegalArgumentException If the real job is already present,
   *                                  or if no delegate job for it could be created.
   * @throws IllegalStateException    If the internal administration is found inconsistent.
   * 
   */
  protected final void addRealJobLocal (final J realJob)
  {
    if (realJob == null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (realJob) || this.jobsInServiceArea.contains (realJob))
      throw new IllegalArgumentException ();
    if (this.delegateSimJobMap.containsKey (realJob))
      throw new IllegalStateException ();
    if (this.realSimJobMap.containsValue (realJob))
      throw new IllegalStateException ();
    final DJ delegateSimJob = this.delegateSimJobFactory.newInstance (getLastUpdateTime (), realJob, (Q) this);
    if (delegateSimJob == null)
      throw new IllegalArgumentException ();
    this.delegateSimJobMap.put (realJob, delegateSimJob);
    this.realSimJobMap.put (delegateSimJob, realJob);
    this.jobQueue.add (realJob);
    
  }

  /** Removes a real job and a delegate job from the internal data structures.
   * 
   * <p>
   * The jobs do not have to be present; if not, this method has (with respect to that job) no effect.
   * 
   * @param realJob     The real job     (may be {@code null} meaning no real job is to be removed).
   * @param delegateJob The delegate job (may be {@code null} meaning no delegate job is to be removed).
   * 
   */
  protected final void removeJobsFromQueueLocal (final J realJob, final DJ delegateJob)
  {
    this.jobQueue.remove (realJob);
    this.jobsInServiceArea.remove (realJob);
    this.delegateSimJobMap.remove (realJob);
    this.realSimJobMap.remove (delegateJob);    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractBlackSimQueueComposite}.
   * 
   * <p>
   * Calls super method,
   * clears the internal mapping between real and delegate {@link SimJob}s
   * and resets all sub-queues in the order in which they appear in {@link #getQueues}.
   * (Note: some sub-classes rely on this order!)
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    // Implemented in private method to make it accessible from the constructor(s).
    resetEntitySubClassLocal ();
  }
  
  private void resetEntitySubClassLocal ()
  {
    this.delegateSimJobMap.clear ();
    this.realSimJobMap.clear ();
    for (final DQ q : getQueues ())
      q.resetEntity ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESS (AND SANITY ON) SUB-QUEUE STATE-CHANGE NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Processes the pending atomic notifications from the sub-queues, one at a time (core sub-queue notification processor).
   * 
   * <p>
   * Core method for reacting to {@link SimEntityListener#notifyStateChanged} notifications from all sub-queues.
   * This method is registered as the processor for an anonymous {@link MultiSimQueueNotificationProcessor}
   * (for all sub-queues) created upon construction,
   * see {@link MultiSimQueueNotificationProcessor.Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * Implementations should take one notification from the list at a time, starting at the head of the list,
   * remove it and processes it.
   * While processing, new notifications may be added to the list; the list is to be processed until it is empty.
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty, or contains a notification from another queue
   *                                  than the a sub-queue,
   *                                  or if other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor.Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * 
   */
  protected abstract void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications);
  
  /** Performs sanity checks on a notification from a sub-queue (irrespective of which one).
   * 
   * <p>
   * Convenience method for sub-classes.
   * 
   * @param notification The notification.
   * 
   * @throws IllegalArgumentException If the time of the notification differs from our last update time.
   * @throws IllegalStateException    If a {@link SimEntitySimpleEventType#RESET} notification was found in combination
   *                                  with other sub-notifications (i.e., as part of an atomic notification that includes
   *                                  other sub-notifications next to the reset),
   *                                  or if a Queue-Access Vacation notification was found.
   * 
   * @see MultiSimQueueNotificationProcessor.Notification#getTime
   * @see #getLastUpdateTime
   * @see SimQueueSimpleEventType#RESET
   * @see MultiSimQueueNotificationProcessor.Notification#getSubNotifications
   * 
   */
  protected final void sanitySubQueueNotification
  (final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification)
  {
    if (notification.getTime () != getLastUpdateTime ())
      throw new IllegalArgumentException ("on " + this + ": notification time [" + notification.getTime ()
      + "] != last update time [" + getLastUpdateTime () + "], subnotifications: "
      + notification.getSubNotifications () + ".");
    for (final Map<SimEntitySimpleEventType.Member, DJ> subNotification : notification.getSubNotifications ())
    {
      final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
      if (notificationType == SimEntitySimpleEventType.RESET)
      {
        if (notification.getSubNotifications ().size () > 1)
          throw new IllegalStateException ();
      }
      else if (notificationType == SimQueueSimpleEventType.QAV_START
            || notificationType == SimQueueSimpleEventType.QAV_END)
        // Queue-Access Vacations are forbidden on sub-queues.
        throw new IllegalStateException ();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE UPDATE NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls {@link #update} in order to update our own time in response to an increase in time on one of the sub-queues.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * @throws IllegalStateException    If time is in the past.
   * 
   * @see #getQueues
   * @see #update
   * @see #getLastUpdateTime
   * 
   */
  @Override
  public final void notifyUpdate (final double time, final SimEntity entity)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
    update (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE STATE CHANGED NOTIFICATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   * @throws IllegalArgumentException If the entity is {@code null} or not one of our sub-queues.
   * 
   */
  @Override
  public final void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, DJ>> notifications)
  {
    if (entity == null || ! getQueues ().contains ((DQ) entity))
      throw new IllegalArgumentException ();
  }
  
}
