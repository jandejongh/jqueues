package nl.jdj.jqueues.r5.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;
import nl.jdj.jqueues.r5.entity.queue.composite.dual.ctandem2.BlackCompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;

/** A {@link SimQueueListener} (of atomic events) to a fixed set of {@link SimQueue}s with features to
 *  operate on the target queues once they are finished with their listener notifications.
 * 
 * <p>
 * By contract of {@link SimEntity} and {@link SimEntityListener},
 * it is illegal to operate on an entity from within the context of a listener (notification).
 * However, in certain cases this feature is highly desired, like in {@link BlackCompressedTandem2SimQueue}.
 * In order to facilitate this to some extent,
 * objects of the current class listen to state-change and update notifications from a fixed set of queues,
 * and store (copies of) these notifications temporarily as {@link Notification} objects.
 * Through the use of {@link SimEntity#doAfterNotifications}, objects of this class then invoke a registered {@link Processor}
 * to act upon the stored notifications, and since the queue is no longer notifying listeners by then,
 * the processor can initiate operations on the entity.
 * If such operations backfire, and new notifications arrive (perhaps from other registered queues),
 * these are simple appended to the stored notifications
 * in the object of this class; the processor can simply proceed to "eat them" and do its thing
 * until there are no more notifications left.
 * Actually, any pending notifications upon return from the processor are discarded.
 * 
 * <p>
 * This class was designed in order to meet some requirements highly specific to implementing,
 * e.g., {@link BlackCompressedTandem2SimQueue},
 * and not really for general-purpose use.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public final class MultiSimQueueNotificationProcessor<J extends SimJob, Q extends SimQueue>
extends DefaultSimQueueListener<J, Q>
{

  /** Creates the listener (without registered processor).
   * 
   * <p>
   * An internal copy of the supplied set is created, hence future changes to the set do not have any effect.
   * 
   * @param queues The set of {@link SimQueue}s to which the object listens.
   * 
   * @throws IllegalArgumentException If the argument is {@code null} or contains {@code null}.
   * 
   * @see SimQueue#registerSimEntityListener
   * 
   */
  public MultiSimQueueNotificationProcessor (final Set<Q> queues)
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    this.queues = new ArrayList<> (queues);
    for (final SimQueue queue : queues)
      queue.registerSimEntityListener (this);
  }
  
  private final List<Q> queues;

  /** A representation of an (atomic) notification from a {@link SimQueue}, consisting of sub-notifications.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class Notification<J extends SimJob, Q extends SimQueue>
  {
    
    private final double time;
    
    /** Gets the time of the (atomic) notification.
     * 
     * @return The time of the (atomic) notification.
     * 
     */
    public final double getTime ()
    {
      return this.time;
    }
    
    private final Q queue;
    
    /** Gets the {@link SimQueue} to which the (atomic) notification applies.
     * 
     * @return The {@link SimQueue} to which the (atomic) notification applies.
     * 
     */
    public final Q getQueue ()
    {
      return this.queue;
    }
    
    /** A string annotation used in reporting, see {@link #toString}.
     * 
     */
    private final String queueAnnotation;
    
    private final List<Map<SimEntitySimpleEventType.Member, J>> subNotifications;
    
    /** Gets the sub-notifications of which this (atomic) notification consists.
     * 
     * @return The sub-notifications of which this (atomic) notification consists.
     * 
     */
    public final List<Map<SimEntitySimpleEventType.Member, J>> getSubNotifications ()
    {
      return this.subNotifications;
    }
    
    /** Creates the (atomic) notification.
     * 
     * @param time             The time of the (atomic) notification (immutable).
     * @param queue            The {@link SimQueue} to which the (atomic) notification applies (non-{@code null} and immutable).
     * @param queueAnnotation  An optional string annotation used in reporting, see {@link #toString}.
     * @param subNotifications The sub-notifications of which this (atomic) notification consists (non-{@code null}).
     * 
     */
    protected Notification
    ( final double time,
      final Q queue,
      final String queueAnnotation,
      final List<Map<SimEntitySimpleEventType.Member, J>> subNotifications)
    {
      if (queue == null || subNotifications == null)
        throw new IllegalArgumentException ();
      this.time = time;
      this.queue = queue;
      this.queueAnnotation = queueAnnotation;
      this.subNotifications = new ArrayList<> (subNotifications);
    }

    @Override
    public final String toString ()
    {
      if (this.queueAnnotation != null)
        return "@" + this.queue + "[" + this.queueAnnotation + "]: " + this.subNotifications;
      else
        return "@" + this.queue + this.subNotifications;
    }
    
  }
  
  final List<Notification<J, Q>> notifications = new ArrayList<> ();
  
  /** A processor for a list of {@link Notification}s.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  @FunctionalInterface
  public interface Processor<J extends SimJob, Q extends SimQueue>
  {
    
    /** Process the list of {@link Notification}s (which may change as a result).
     * 
     * <p>
     * In most cases, the processor should remove the first element from the list,
     * process it, and repeat until the list is empty (minding the fact that the list can change while processing).
     * This, however, is not a strict requirement, and a processor may decide to ignore certain notifications
     * (which are then discarded upon return from the processor).
     * 
     * @param notifications The (atomic) notifications, non-{@code null}.
     * 
     * @throws IllegalArgumentException If the argument is {@code null} or illegally structured.
     * 
     */
    public abstract void process (final List<Notification<J, Q>> notifications);
    
  }

  private Processor processor = null;
  
  /** Sets the (single) processor.
   * 
   * @param processor The processor, may be {@code null}, in which case no processor is invoked.
   * 
   * @throws IllegalStateException If notifications are currently being processed.
   * 
   */
  public final void setProcessor (final Processor processor)
  {
    if (this.processing)
      throw new IllegalStateException ();
    this.processor = processor;
  }
  
  private boolean processing = false;
  
  /** Stores the atomic notification from the issuing {@link SimQueue}, and triggers the processor.
   * 
   * @param time          The time of the notification.
   * @param entity        The entity (queue) that issues the notification.
   * @param notifications The (atomic) notification.
   * 
   * @throws IllegalArgumentException If the entity is not one of the monitored {@link SimQueue}s,
   *                                  or if the notifications are {@code null}, empty or illegally structured.
   * 
   * @see SimEntity#doAfterNotifications
   * @see #afterNotificationsTrigger
   * 
   */
  @Override
  public final void notifyStateChanged
  ( final double time,
    final SimEntity entity,
    final List<Map<SimEntitySimpleEventType.Member, J>> notifications)
  {
    if (entity == null || ! (entity instanceof SimQueue))
      throw new IllegalArgumentException ("Null entity or entity that is not a queue: " + entity + ".");
    if (! this.queues.contains ((Q) entity))
      throw new IllegalArgumentException ("Queue supplied is not a sub-queue: " + entity + ".");
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ("Null or empty notifications: " + notifications + ".");
    for (final Map<SimEntitySimpleEventType.Member, J> notification : notifications)
      if (notification == null || notification.size () != 1 || notification.containsKey (null))
        throw new IllegalArgumentException ();
    this.notifications.add
      (new Notification (time, (SimQueue) entity, "" + this.queues.indexOf ((SimQueue) entity), notifications));
    entity.doAfterNotifications (this::afterNotificationsTrigger);
  }

  /** Invokes the processor if present, if not already processing and if there are notifications to process.
   * 
   * <p>
   * Clears the notifications upon return from the processor.
   * 
   * <p>
   * Non-private for documentation purposes.
   * 
   * @see Processor#process
   * 
   */
  protected final void afterNotificationsTrigger ()
  {
    if ((! this.processing) && this.processor != null && ! this.notifications.isEmpty ())
    {
      this.processing = true;
      this.processor.process (this.notifications);
      this.notifications.clear ();
      this.processing = false;
    }
  }
  
}
