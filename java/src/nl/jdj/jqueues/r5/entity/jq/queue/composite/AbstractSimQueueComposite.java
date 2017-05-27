package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.dual.collector.DropCollectorSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.dual.ctandem2.CompressedTandem2SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.single.enc.EncapsulatorHideStartSimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.single.enc.EncapsulatorSimQueue;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Notification;
import nl.jdj.jqueues.r5.listener.MultiSimQueueNotificationProcessor.Processor;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueueComposite}.
 * 
 * <p>
 * This abstract base class registers the sub-queues, sub-queue selector, start model
 * and the drop destination queue,
 * takes care of all administration related to job visits,
 * creating delegate jobs and taking care of of mapping between real and delegate jobs,
 * and deals with all {@link SimQueue} operations and all sub-queue notifications
 * (through the use of a {@link MultiSimQueueNotificationProcessor}).
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
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
public abstract class AbstractSimQueueComposite
<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends AbstractSimQueueComposite>
extends AbstractSimQueue<J, Q>
implements SimQueueComposite<DJ, DQ, J, Q>,
  SimEntityListener, SimQueueSelector<J, DQ>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract network of queues.
   * 
   * <p>
   * After calling the super constructor,
   * this methods sets the delegate job factory,
   * and inhibits future automatic resets from the event list on all sub-queues through {@link SimQueue#setIgnoreEventListReset},
   * since this object will take care of that (and depends on the absence of "independent" resets
   * of the sub-queues).
   * In addition, it registers as a {@link SimEntityListener} on each sub-queue.
   * It then creates a single {@link MultiSimQueueNotificationProcessor} for all sub-queues,
   * and registers method {@link #processSubQueueNotifications} as its processor.
   * Finally, it resets the local part of the object through a (private) variant of {@link #resetEntitySubClass}
   * that does not invoke its super method.
   * This, among others, will reset the sub-queues.
   * 
   * @param eventList             The event list to be shared between this queue and the embedded queues.
   * @param queues                A set holding the "embedded" queues.
   * @param simQueueSelector      The object for routing jobs through the network of embedded queues;
   *                                if {@code null}, no sub-queues will be visited.
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   * 
   * @throws IllegalArgumentException If the event list is {@code null},
   *                                    the <code>queue</code> argument is <code>null</code>, has <code>null</code> members,
   *                                    or contains this composite queue. 
   * 
   * @see SimQueueSelector
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * @see SimEntity#setIgnoreEventListReset
   * @see SimEntity#registerSimEntityListener
   * @see MultiSimQueueNotificationProcessor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #processSubQueueNotifications
   * @see #resetEntitySubClass
   * 
   */
  protected AbstractSimQueueComposite
  (final SimEventList eventList,
   final Set<DQ> queues,
   final SimQueueSelector<J, DQ> simQueueSelector,
   final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList);
    if (queues == null || queues.contains (null) || queues.contains ((DQ) this))
      throw new IllegalArgumentException ();
    this.queues = queues;
    this.simQueueSelector = simQueueSelector;
    this.delegateSimJobFactory =
      ((delegateSimJobFactory == null) ? new DefaultDelegateSimJobFactory () : delegateSimJobFactory);
    for (final DQ q : this.queues)
    {
      q.setIgnoreEventListReset (true);
      q.registerSimEntityListener (this);
    }
    final MultiSimQueueNotificationProcessor<DJ, DQ>  subQueueEventProcessor =
      new MultiSimQueueNotificationProcessor<> (getQueues ());
    subQueueEventProcessor.setProcessor (this::processSubQueueNotifications);
    resetEntitySubClassLocal ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // (SUB)QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The queues, all non-null.
   * 
   * Set is also non-null and final.
   * 
   */
  private final Set<DQ> queues;
  
  @Override
  public final Set<DQ> getQueues ()
  {
    return this.queues;
  }

  /** Returns the index of a given queue in a set.
   * 
   * <p>
   * Note that elements in a {@link Set} are unordered,
   * unless the specific implementation (like {@link LinkedHashSet}) takes care of that.
   * Be careful with this method!
   * 
   * @param queues The set, must be non-{@code null}.
   * @param queue  The queue; must be present in the set and non-{@code null}.
   * 
   * @return The index of the queue in the set (by iteration order).
   * 
   * @throws IllegalArgumentException If the set or the queue is {@code null},
   *                                  or if the queue is not present in the set.
   * 
   * @param <DQ> The queue-type.
   * 
   */
  public static <DQ extends SimQueue> int getIndex (final Set<DQ> queues, final DQ queue)
  {
    if (queues == null || queue == null || ! queues.contains (queue))
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = queues.iterator ();
    for (int q = 0; q < queues.size (); q++)
      if (iterator.next () == queue)
        return q;
    throw new IllegalArgumentException ();
  }
  
  /** Returns the index of given sub-queue.
   * 
   * @param queue The sub-queue; must be present in {@link #getQueues}.
   * 
   * @return The index of the sub-queue in {@link #getQueues}.
   * 
   * @throws IllegalArgumentException If the <code>queue</code> is <code>null</code> or not present in {@link #getQueues}.
   * 
   */
  protected final int getIndex (final DQ queue)
  {
    return AbstractSimQueueComposite.getIndex (getQueues (), queue);
  }
  
  /** Returns a queue in a set by its index.
   * 
   * <p>
   * Note that elements in a {@link Set} are unordered,
   * unless the specific implementation (like {@link LinkedHashSet}) takes care of that.
   * Be careful with this method!
   * 
   * @param queues The queues.
   * @param q      The index.
   * 
   * @return The queue in the set with given index (by iteration order).
   * 
   * @throws IllegalArgumentException If the set is {@code null}
   *                                  or the index is (strictly) negative or larger or equal than the size of the set.
   *
   * @param <DQ> The queue-type.
   * 
   */
  public static <DQ extends SimQueue> DQ getQueue (final Set<DQ> queues, final int q)
  {
    if (queues == null || q < 0 || q >= queues.size ())
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = queues.iterator ();
    int i = 0;
    DQ dq = iterator.next ();
    while (i < q)
    {
      i++;
      dq = iterator.next ();
    }
    return dq;    
  }
  
  /** Returns a sub-queue by its index.
   * 
   * @param q The index.
   * 
   * @return The (sub-)queue in {@link #getQueues} with given index.
   * 
   * @throws IllegalArgumentException If the index is (strictly) negative or larger or equal than the size of {@link #getQueues}.
   * 
   */
  protected final DQ getQueue (final int q)
  {
    return AbstractSimQueueComposite.getQueue (getQueues (), q);
  }
  
  /** Returns a copy of a given set of {@link SimQueue}s, each of which is copied in itself.
   * 
   * @param queues The set of {@link SimQueue}s.
   * 
   * @return A copy of the given set of {@link SimQueue}s, each of which is copied in itself.
   * 
   * @see SimQueue#getCopySimQueue
   * 
   * @throws IllegalArgumentException      If <code>queues == null </code> or contains a <code>null</code> element.
   * @throws UnsupportedOperationException If copying any of the sub-queues is unsupported; this should be considered as a
   *                                       software error.
   * 
   */
  public static Set<SimQueue> getCopySimQueues (final Set<SimQueue> queues) throws UnsupportedOperationException
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    final Set<SimQueue> set = new LinkedHashSet<> ();
    for (final SimQueue q : queues)
      set.add (q.getCopySimQueue ());
    return set;    
  }
  
  /** Returns a copy of the set of sub-queues, each of which is copied in itself.
   * 
   * @return A copy of the set of sub-queues, each of which is copied in itself.
   * 
   * @see #getQueues
   * @see SimQueue#getCopySimQueue
   * 
   * @throws UnsupportedOperationException If copying any of the sub-queues is unsupported; this should be considered as a
   *                                       software error.
   * 
   */
  protected final Set<DQ> getCopySubSimQueues () throws UnsupportedOperationException
  {
    final Set<DQ> set = new LinkedHashSet<> ();
    for (final DQ dq : getQueues ())
      set.add ((DQ) dq.getCopySimQueue ());
    return set;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The (user-supplied) {@link SimQueueSelector} for selecting the sub-queue to visit.
   * 
   */
  private final SimQueueSelector<J, DQ> simQueueSelector;
  
  /** Returns the (user-supplied) {@link SimQueueSelector} for selecting the sub-queue to visit.
   * 
   * @return The {@link SimQueueSelector} for selecting the sub-queue to visit.
   * 
   */
  @Override
  public final SimQueueSelector<J, DQ> getSimQueueSelector ()
  {
    return this.simQueueSelector;
  }

  /** Does nothing.
   * 
   */
  @Override
  public final void resetSimQueueSelector ()
  {
  }

  /** Selects the first sub-queue to visit from the user-supplied {@link SimQueueSelector},
   *  or none if no such object was supplied.
   * 
   * @param time The time of arrival of the job.
   * @param job  The job, non-<code>null</code>.
   * 
   * @return The first sub-queue to visit, if <code>null</code>, the job is to depart immediately.
   *         If no selector is supplied upon construction, this method returns {@code null}.
   * 
   * @see #getSimQueueSelector
   * 
   */
  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    if (this.simQueueSelector != null && this.simQueueSelector != this)
      return this.simQueueSelector.selectFirstQueue (time, job);
    else
      return null;
  }
  
  /** Selects the next sub-queue to visit from the user-supplied {@link SimQueueSelector},
   *  or none if no such object was supplied.
   * 
   * @param time          The current time, i.e., the departure time of the job at its previous queue.
   * @param job           The job, non-<code>null</code>.
   * @param previousQueue The previous queue the job visited, and just departed from.
   * 
   * @return The next sub-queue to visit, if <code>null</code>, the job is to depart immediately.
   *         If no selector is supplied upon construction, this method returns {@code null}.
   * 
   * @see #getSimQueueSelector
   * 
   */
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (this.simQueueSelector != null && this.simQueueSelector != this)
      return this.simQueueSelector.selectNextQueue (time, job, previousQueue);
    else
      return null;
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
  
  /** Sets the start model of this composite queue.
   * 
   * <p>
   * The method should only be used by sub-classes upon construction, if needed, and should not
   * be called afterwards. The start-model setting should survive queue resets.
   * 
   * <p>
   * Upon exit, this method invokes {@link #resetEntitySubClass} to ensure the initial
   * state of the sub-queues.
   * 
   * @param startModel The new start model (non-{@code null}).
   * 
   * @throws IllegalArgumentException If the argument is {@code null},
   *                                  or {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE}
   *                                  or {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE} is chosen
   *                                  while there are fewer or more than <i>one</i> sub-queues,
   *                                  or {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE} is chosen
   *                                  while there are fewer or more than <i>two</i> sub-queues.
   * 
   * @see EncapsulatorSimQueue
   * @see EncapsulatorHideStartSimQueue
   * @see CompressedTandem2SimQueue
   * 
   */
  protected final void setStartModel (final StartModel startModel)
  {
    if (startModel == null)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.ENCAPSULATOR_QUEUE && getQueues ().size () != 1)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.ENCAPSULATOR_HIDE_START_QUEUE && getQueues ().size () != 1)
      throw new IllegalArgumentException ();
    if (startModel == StartModel.COMPRESSED_TANDEM_2_QUEUE && getQueues ().size () != 2)
      throw new IllegalArgumentException ();
    this.startModel = startModel;
    resetEntitySubClassLocal ();
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
   * Normally, dropping a delegate job results in dropping the corresponding real job.
   * By setting the "drop queue", the default behavior can be changed (but only from sub-classes),
   * and such jobs can be sent to one of the sub-queues as an arrival.
   * 
   * <p>
   * The default value is <code>null</code>, implying that the real job is to be dropped if its delegate job is dropped.
   * 
   * @return Any {@link SimQueue} in {@link #getQueues} to which the dropped delegate job is to be sent as an arrival,
   *           or <code>null</code> if the corresponding real job is to be dropped as well.
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
   * @param queue The destination sub-queue for dropped delegate jobs; non-{@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null} or not a sub-queue of this composite queue.
   * 
   * @see #getDropDestinationQueue
   * @see DropCollectorSimQueue
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

  /** Checks for the presence of a real job (in the administration of this composite queue).
   * 
   * <p>
   * The result of this method is equivalent to {@code this.jobQueue.contains (realJob)},
   * with additional sanity checks on the internal administration,
   * in particular on the mapping between real and delegate jobs.
   * 
   * <p>
   * This method does not check the presence of the delegate job on a sub-queue.
   * 
   * @param realJob The real {@link SimJob}; if {@code null}, false is returned.
   * 
   * @return {@code True} if and only if the argument is a real job currently visiting this composite queue.
   * 
   * @throws IllegalStateException If sanity checks on the internal job administrations fail.
   * 
   * @see #jobQueue
   * 
   */
  protected final boolean isRealJob (final J realJob)
  {
    if (realJob == null)
      return false;
    if (this.jobQueue.contains (realJob))
    {
      final DJ delegateJob = this.delegateSimJobMap.get (realJob);
      if (delegateJob == null)
        throw new IllegalStateException ();
      if (this.realSimJobMap.get (delegateJob) != realJob)
        throw new IllegalStateException ();
      return true;
    }
    else
      return false;
  }
  
  /** Checks for the presence of a delegate job (in the administration of this composite queue).
   * 
   * <p>
   * This method performs sanity checks on the internal administration,
   * in particular on the mapping between real and delegate jobs.
   * 
   * @param delegateJob The delegate {@link SimJob}; if {@code null}, false is returned.
   * 
   * @return {@code True} if and only if the argument is a delegate job of a real job currently visiting this composite queue.
   * 
   * @throws IllegalStateException If sanity checks on the internal job administrations fail.
   * 
   * @see #getDelegateJob
   * 
   */
  protected final boolean isDelegateJob (final DJ delegateJob)
  {
    if (delegateJob == null)
      return false;
    if (this.realSimJobMap.containsKey (delegateJob))
    {
      final J realJob = this.realSimJobMap.get (delegateJob);
      if (realJob == null)
        throw new IllegalStateException ();
      if (this.delegateSimJobMap.get (realJob) != delegateJob)
        throw new IllegalStateException ();
      if (! this.jobQueue.contains (realJob))
        throw new IllegalStateException ();
      return true;
    }
    else
      return false;
  }
  
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
   * @see #isRealJob
   * @see #isDelegateJob
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
   *                    {@code null} if it is supposed to reside on <i>none</i> of the (sub-)queues..
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found,
   *                               or where assumption on the delegate-job whereabout proves to be wrong.
   * 
   * @see #getRealJob(SimJob)
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
   * <p>
   * This method does <i>not</i> check the presence (nor absence) of the delegate job on a (or any) sub-queue.
   * 
   * @param delegateJob The delegate job.
   * 
   * @return The real job.
   * 
   * @throws IllegalStateException If sanity checks fail, including the case where a corresponding real job could not be found.
   * 
   * @see #getRealJob(SimJob, SimQueue)
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
   * <p>
   * This method does <i>not</i> remove the delegate job from a sub-queue it might currently be visiting.
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
    if (delegateJob != null)
      this.realSimJobMap.remove (delegateJob);
  }
  
  /** A mapper between real and delegate jobs (for use by sub-classes).
   * 
   * @param <J>  The job type.
   * @param <DJ> The delegate-job type.
   * 
   */
  public interface RealDelegateJobMapper<J extends SimJob, DJ extends SimJob>
  {
    
    /** Returns the delegate job for a real job.
     * 
     * @param realJob The real job.
     * 
     * @return The delegate job.
     * 
     * @throws IllegalArgumentException If the real job argument is {@code null} or not found.
     * 
     */
    DJ getDelegateJob (J realJob);
    
    /** Returns the real job for a delegate job.
     * 
     * @param delegateJob The delegate job.
     * 
     * @return The real job.
     * 
     * @throws IllegalArgumentException If the delegate job argument is {@code null} or not found.
     * 
     */
    J getRealJob (DJ delegateJob);
    
  }
  
  /** A mapper object for mapping real and delegate jobs of this composite queue (for use by sub-classes).
   * 
   */
  protected final RealDelegateJobMapper realDelegateJobMapper = new RealDelegateJobMapper<J, DJ> ()
  {
    
    @Override
    public final DJ getDelegateJob (final J realJob)
    {
      return AbstractSimQueueComposite.this.getDelegateJob (realJob);
    }

    @Override
    public final J getRealJob (final DJ delegateJob)
    {
      return AbstractSimQueueComposite.this.getRealJob ((DJ) delegateJob);
    }
    
  };
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Resets this {@link AbstractSimQueueComposite}.
   * 
   * <p>
   * Calls super method (not if called from constructor, for which a private variant for local resets is used),
   * resets the sub-queue selector,
   * clears the pending revocation event for a sub-queue,
   * clears the internal mapping between real and delegate {@link SimJob}s (removing all real and delegate jobs),
   * and resets all sub-queues in the order in which they appear in {@link #getQueues}.
   * (Note: some sub-classes rely on this order!)
   * 
   * <p>
   * Finally, if the start model is {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * it sets the server-access credits on the wait (first) sub-queue to unity
   * if the serve (second) queue has {@link SimQueue#isStartArmed} value {@code true},
   * and zero if not.
   * 
   * @see #removeJobFromQueueUponRevokation
   * @see #rescheduleAfterRevokation
   * @see SimQueue#resetEntity
   * @see SimQueueSelector#resetSimQueueSelector
   * @see #getStartModel
   * @see SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
   * 
   * @see SimQueue#resetEntity
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
    //
    // NOTE: This method is invoked from the constructor instead of resetEntitySubClass ().
    //
    this.simQueueSelector.resetSimQueueSelector ();
    this.delegateSimJobMap.clear ();
    this.realSimJobMap.clear ();
    this.pendingDelegateRevocationEvent = null;
    for (final DQ q : getQueues ())
      q.resetEntity ();
    if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE)
      getQueue (0).setServerAccessCredits (getLastUpdateTime (), getQueue (1).isStartArmed () ? 1 : 0);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // isStartArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns the {@code startArmed} state of the composite queue.
   * 
   * <p>
   * The result depends on the start model of the queue
   * and possibly on the state of its sub-queues:
   * <ul>
   *   <li>
   *     For {@link SimQueueComposite.StartModel#LOCAL}, this method returns {@code true}.
   *   <li>
   *     For {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE}, this method returns the {@link SimQueue#isStartArmed}
   *     state of the encapsulated queue.
   *   <li>
   *     For {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE}, this method returns {@code false}.
   *   <li>
   *     For {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE}, this method returns the {@link SimQueue#isStartArmed}
   *       state of the serve (i.e., second) queue.
   * </ul>
   * 
   * @return The {@code startArmed} state of the composite queue.
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see SimQueue#isStartArmed
   * 
   */
  @Override
  public final boolean isStartArmed ()
  {
    switch (getStartModel ())
    {
      case LOCAL:
        return true;
      case ENCAPSULATOR_QUEUE:
        return getQueue (0).isStartArmed ();
      case ENCAPSULATOR_HIDE_START_QUEUE:
        return false;
      case COMPRESSED_TANDEM_2_QUEUE:
        return getQueue (1).isStartArmed ();
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SET SERVER-ACCESS CREDITS ON WAIT QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Sets the server-access credits on the wait queue, based upon our server-access credits
   *  and the {@link SimQueue#isStartArmed} state on the server queue.
   * 
   * <p>
   * This method can only be used with {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * at the expense of an exception.
   * 
   * <p>
   * With {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * at all times (well, if we have a consistent queue state),
   * the server-access credits on the wait queue should be unity if and only if
   * the local (this) queue {@link #hasServerAcccessCredits}
   * AND the serve queue has {@link SimQueue#isStartArmed}.
   * In all other cases, the server-access credits on the wait queue should be zero.
   * 
   * <p>
   * This method sets the server-access credits on the wait queue appropriately, but only if needed.
   * 
   * <p>
   * Caution is advised for the use of this method.
   * For one, because of its immediate side effects on the wait (and possibly serve) queue,
   * you <i>should not</i> use it from within a sub-queue notification listener
   * (at least, not without taking special measures,
   * as is done in this class through a {@link MultiSimQueueNotificationProcessor}).
   * 
   * <p>
   * This method is (left) protected for documentation (javadoc) purposes.
   * 
   * @throws IllegalStateException If the start-model of this queue is other than
   *                               {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   *                               or if the current server-access credits value on the wait queue is not zero or unity.
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see #getQueue(int)
   * @see #getLastUpdateTime
   * @see #hasServerAcccessCredits
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueue#isStartArmed
   * 
   */
  protected final void setServerAccessCreditsOnWaitQueue ()
  {
    if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE)
    {
      final DQ waitQueue = getQueue (0);
      final DQ serveQueue = getQueue (1);
      final int oldWaitQueueSac = waitQueue.getServerAccessCredits ();
      if (oldWaitQueueSac < 0 || oldWaitQueueSac > 1)
        throw new IllegalStateException ();
      final int newWaitQueueSac = (hasServerAcccessCredits () && serveQueue.isStartArmed ()) ? 1 : 0;
      if (newWaitQueueSac != oldWaitQueueSac)
        waitQueue.setServerAccessCredits (getLastUpdateTime (), newWaitQueueSac);
    }
    else
      throw new IllegalStateException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates the delegate job, administers it and puts the (real) job into {@link #jobQueue}.
   * 
   * @see #addRealJobLocal
   * @see #rescheduleAfterArrival
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    addRealJobLocal (job);
  }

  /** Depending on the start model, starts the arrived job if possible or sends its delegate job to the first queue.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#LOCAL}, this method invokes {@link #start} if there are (local) server-access credits.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE}
   * and {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   * this method lets the delegate job arrive at the encapsulated queue.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * this method lets the delegate job arrive at the wait queue.
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see #hasServerAcccessCredits
   * @see #start
   * @see SimQueue#arrive
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    switch (getStartModel ())
    {
      case LOCAL:
        if (hasServerAcccessCredits ())
          start (time, job);
        break;
      case ENCAPSULATOR_QUEUE:
      case ENCAPSULATOR_HIDE_START_QUEUE:
      case COMPRESSED_TANDEM_2_QUEUE:
        final DJ delegateJob = getDelegateJob (job);
        getQueue (0).arrive (time, delegateJob);
        break;
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Drops the given (real) job.
   * 
   * <p>
   * In the {@link AbstractSimQueueComposite},
   * a (real) job can <i>only</i> be dropped because
   * its delegate job is dropped on one of the sub-queues, or perhaps due to some other sub-queue-related event,
   * see {@link #processSubQueueNotifications},
   * and the semantics of this composite queue require the real job to be dropped as well
   * (this is not a general requirement, but we impose it on {@link AbstractSimQueueComposite}).
   * The notification callback relies on {@link #drop} to perform the drop.
   * 
   * <p>
   * The essential notion is that the delegate job has already left the sub-queue system when we are called,
   * hence no action is required to remove it from there.
   * Another way of saying this is that an {@link AbstractSimQueueComposite}
   * <i>never</i> decides <i>by itself</i> to drop a job,
   * but only in response to event notifications (drops) from its sub-queues.
   * If the feature of "locally initiated drops" is desired,
   * the drop call-backs need to be retrofitted conform the approach with the
   * {@link #removeJobFromQueueUponRevokation} and {@link #rescheduleAfterRevokation} combo.
   * 
   * <p>
   * This method ignores the drop-destination queue, see {@link #getDropDestinationQueue}.
   * Following the contract of {@link AbstractSimQueue#drop}, the drop of the (real) job is inevitable (by) now.
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * 
   * @throws IllegalStateException If the delegate job is still visiting a (any) queue.
   * 
   * @see #drop
   * @see #removeJobsFromQueueLocal
   * @see #rescheduleAfterDrop
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      throw new IllegalStateException ();
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Empty, nothing to do.
   * 
   * @see #removeJobFromQueueUponDrop
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** The pending delegate revocation event is a single event used to store the need to revoke a delegate job
   *  on the sub queue it resides on.
   * 
   * <p>
   * It is needed in-between {@link #removeJobFromQueueUponRevokation} and {@link #rescheduleAfterRevokation}.
   * 
   */
  private SimJQEvent.Revocation<DJ, DQ> pendingDelegateRevocationEvent = null;

  /** Removes a job if revocation is successful.
   * 
   * <p>
   * In a {@link AbstractSimQueueComposite}, revocations on real jobs can occur either
   * through external requests, in other words, through {@link #revoke}, or because of auto-revocations
   * on the composite (this) queue through {@link #autoRevoke}.
   * In both cases, the delegate job is still present on a sub-queue,
   * and we have to forcibly revoke it.
   * Because we cannot perform the revocation here (we are <i>not</i> allowed to reschedule!),
   * we defer until {@link #removeJobFromQueueUponRevokation} by raising an internal flag
   * (in fact a newly created, though not scheduled {@link Revocation}).
   * We have to use this method in order to remember the delegate job to be revoked,
   * and the queue from which to revoke it,
   * both of which are wiped from the internal administration by {@link #removeJobsFromQueueLocal},
   * which is invoked last.
   * 
   * <p>
   * Note that even though a {@link Revocation} is used internally to flag the
   * required delegate-job revocation, it is never actually scheduled on the event list!
   * 
   * @throws IllegalStateException If the delegate job is <i>not</i> visiting a sub-queue,
   *                               or if a pending delegate revocation has already been flagged (or been forgotten to clear).
   * 
   * @see #revoke
   * @see #autoRevoke
   * @see Revocation
   * @see #rescheduleAfterRevokation
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    final DJ delegateJob = getDelegateJob (job);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue == null)
      // XXX Is this really impossible?
      // XXX I means, we could have a job still in our waiting area, right, i.e., not present on any sub-queue...
      throw new IllegalStateException ();
    if (this.pendingDelegateRevocationEvent != null)
      throw new IllegalStateException ();
    this.pendingDelegateRevocationEvent = new SimJQEvent.Revocation<> (delegateJob, subQueue, time, true);
    removeJobsFromQueueLocal (job, delegateJob);
  }

  /** Performs the pending revocation on the applicable sub-queue, and check whether that succeeded.
   * 
   * <p>
   * Upon return, the pending revocation event has been reset to {@code null}.
   * 
   * @throws IllegalStateException If no pending delegate revocation was found, or revoking the delegate job failed.
   * 
   * @see Revocation
   * @see #removeJobFromQueueUponRevokation
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    if (this.pendingDelegateRevocationEvent == null)
      throw new IllegalStateException ();
    final SimJQEvent.Revocation event = this.pendingDelegateRevocationEvent;
    // We set the pendingDelegateRevocationEvent to null in the sub-queue event processor
    // upon receiving the revocation acknowledgement!
    // this.pendingDelegateRevocationEvent = null;
    // Invoking the event's action equivalent to next line:
    // event.getQueue ().revoke (event.getTime (), event.getJob (), event.isInterruptService ());
    event.getEventAction ().action (event);
    // Check that sub-queue actually confirmed the revocation.
    if (this.pendingDelegateRevocationEvent != null)
      throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Takes appropriate action if needed on the server-access credits of sub-queues.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#LOCAL},
   * this method does nothing, since server-access credits with this model are managed locally.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE},
   * this method copies the new server-access credits into the encapsulated queue.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE}, this method does nothing,
   * since (real) jobs cannot start on the composite queue, and the number of server-access credits on the
   * encapsulated queue in always infinite.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * this method sets the server-access credits on the wait queue
   * <i>only</i> if we run out of local server-access credits.
   * Note that the case in which we regain them is dealt with by {@link #rescheduleForNewServerAccessCredits}.
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see #getLastUpdateTime
   * @see #getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #rescheduleForNewServerAccessCredits
   * 
   */
  @Override
  protected final void setServerAccessCreditsSubClass ()
  {
    switch (getStartModel ())
    {
      case LOCAL:
        break;
      case ENCAPSULATOR_QUEUE:
        getQueue (0).setServerAccessCredits (getLastUpdateTime (), getServerAccessCredits ());
        break;
      case ENCAPSULATOR_HIDE_START_QUEUE:
        break;
      case COMPRESSED_TANDEM_2_QUEUE:
        if (getServerAccessCredits () == 0)
          setServerAccessCreditsOnWaitQueue ();
        break;
      default:
        throw new RuntimeException ();
    }
  }
  
  /** Depending on the start model,
   *  takes appropriate action if needed on waiting jobs or setting the server-access credits of sub-queues.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#LOCAL},
   * this method starts waiting jobs (in the local waiting area)
   * as long as there are such jobs and there are (local) server-access credits available.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE},
   * this method does nothing (we follow the server-access credits on the
   * encapsulated queue, and only set them upon external request).
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   * this method does nothing (the server-access credits on the
   * encapsulated queue is always infinite,
   * and on the composite queue there are no job starts).
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * this method sets the server-access credits on the wait queue.
   * Note that the case in which we lose them is dealt with by {@link #setServerAccessCreditsSubClass}.
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see #hasServerAcccessCredits
   * @see #hasJobsInWaitingArea
   * @see #start
   * @see #getFirstJobInWaitingArea
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #setServerAccessCreditsSubClass
   * 
   */
  @Override
  protected final void rescheduleForNewServerAccessCredits (final double time)
  {
    switch (getStartModel ())
    {
      case LOCAL:
        while (hasServerAcccessCredits () && hasJobsInWaitingArea ())
          start (time, getFirstJobInWaitingArea ());
        break;
      case ENCAPSULATOR_QUEUE:
      case ENCAPSULATOR_HIDE_START_QUEUE:
        break;
      case COMPRESSED_TANDEM_2_QUEUE:
        setServerAccessCreditsOnWaitQueue ();
        break;
      default:
        throw new RuntimeException ();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job in the service area (after sanity checks).
   * 
   * @throws IllegalStateException If the start model is {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   *                               or if other sanity checks on internal consistency fail.
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE
   * @see #jobsInServiceArea
   * @see #rescheduleAfterStart
   * 
   */
  @Override
  protected final void insertJobInQueueUponStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || this.jobsInServiceArea.contains (job))
      throw new IllegalArgumentException ();
    if (getStartModel () == StartModel.ENCAPSULATOR_HIDE_START_QUEUE)
      // Real jobs cannot start; so a call of this method should not happen!
      throw new IllegalStateException ();
    getDelegateJob (job); // Sanity on existence of delegate job.
    this.jobsInServiceArea.add (job);
  }

  /** Depending on the start model,
   *  lets the delegate job arrive at its first queue, or make it depart immediately if no such queue is provided.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#LOCAL}, this method selects the first sub-queue for the delegate job to arrive on
   *                               through {@link #selectFirstQueue}. If a sub-queue is provided,
   *                               it makes the delegate job arrive on that sub-queue;
   *                               otherwise it invokes {@link #depart} on the real job.
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE}, this method does nothing
   *                                            (we are merely being notified of the start of a delegate
   *                                            job on the encapsulated queue, and our own notification will be dealt with by
   *                                            our caller, {@link #start}).
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   *                                                       this method throws an {@link IllegalStateException}
   *                                                       because (real) jobs cannot start and an invocation of this method
   *                                                       is therefore unexpected (illegal).
   * 
   * <p>
   * For {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * lets the delegate job arrive on the serve queue (the second queue).
   * 
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see #getDelegateJob
   * @see #selectFirstQueue
   * @see #arrive
   * @see #depart
   * @see #getQueue
   * @see #insertJobInQueueUponStart
   * 
   */
  @Override
  protected final void rescheduleAfterStart (final J job, final double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if ((! this.jobQueue.contains (job)) || (! this.jobsInServiceArea.contains (job)))
      throw new IllegalArgumentException ();
    final DJ delegateJob = getDelegateJob (job);
    switch (getStartModel ())
    {
      case LOCAL:
        // Arrive at first queue, if provided.
        final SimQueue<DJ, DQ> firstQueue = selectFirstQueue (time, job);
        if (firstQueue != null && ! getQueues ().contains ((DQ) firstQueue))
          throw new IllegalArgumentException ();
        if (firstQueue != null)
          firstQueue.arrive (time, delegateJob);          
        else
          // We do not get a queue to arrive at.
          // So we depart; without having been executed!
          depart (time, job);
        break;
      case ENCAPSULATOR_QUEUE:
        break;
      case ENCAPSULATOR_HIDE_START_QUEUE:
        // Real jobs cannot start; so a call of this method should not happen!
        throw new IllegalStateException ();
      case COMPRESSED_TANDEM_2_QUEUE:
        getQueue (1).arrive (time, delegateJob);
        break;
      default:
        throw new RuntimeException ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Departure of the given (real) job.
   * 
   * <p>
   * In the {@link AbstractSimQueueComposite},
   * a (real) job can only depart as a result from an event (notification) of one of its sub-queues.
   * Refer to {@link #removeJobFromQueueUponDrop} for a more detailed explanation
   * on the rationale behind this and behind the action taken here...
   * 
   * <p>
   * All we have to do is invoke {@link #removeJobsFromQueueLocal}.
   * 
   * @throws IllegalStateException If the delegate job is still visiting a (any) queue.
   * 
   * @see #depart
   * @see #removeJobsFromQueueLocal
   * @see #rescheduleAfterDeparture
   * @see #getDelegateJob(SimJob)
   * @see SimJob#getQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    final DJ delegateJob = getDelegateJob (departingJob);
    final DQ subQueue = (DQ) delegateJob.getQueue ();
    if (subQueue != null)
      throw new IllegalStateException ();
    removeJobsFromQueueLocal (departingJob, delegateJob);
  }

  /** Empty, nothing to do.
   * 
   * @see #removeJobFromQueueUponDeparture
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    ; /* NOTHING TO DO */
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
   * see {@link Processor}
   * and {@link MultiSimQueueNotificationProcessor#setProcessor}.
   * 
   * <p>
   * This method takes one notification at a time, starting at the head of the list, removes it,
   * and processes the notification as described below.
   * While processing, new notifications may be added to the list; the list is processed until it is empty.
   * 
   * <p>
   * However, before processing any event, it checks for {@link SimEntitySimpleEventType#RESET}
   * (sub-)notifications. If it finds <i>any</i>, the notifications list is cleared and immediate return from this method follows.
   * 
   * <p>
   * Otherwise, this method processes the notifications as described below;
   * the remainder of the method is encapsulated in a
   * {@link #clearAndUnlockPendingNotificationsIfLocked} and {@link #fireAndLockPendingNotifications} pair,
   * to make sure we create atomic notifications in case of a top-level event.
   * 
   * <p>
   * With {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * we check that {@link SimQueueSimpleEventType#START} and {@link SimQueueSimpleEventType#AUTO_REVOCATION}
   * always come in pairs from the wait queue
   * and allow at most one such pair in a (atomic) notification.
   * 
   * <p>
   * A notification consists of a (fixed) sequence of sub-notifications,
   * see {@link Notification#getSubNotifications},
   * each of which is processed in turn as follows:
   * <ul>
   * <li>With {@link SimEntitySimpleEventType#RESET}, impossible, see above; throws an {@link IllegalStateException}.
   * <li>With queue-access vacation related events,
   *          {@link SimQueueSimpleEventType#QUEUE_ACCESS_VACATION},
   *          {@link SimQueueSimpleEventType#QAV_START},
   *          {@link SimQueueSimpleEventType#QAV_END},
   *          we throw an {@link IllegalStateException}.
   * <li>We ignore server-access credits related events
   *          {@link SimQueueSimpleEventType#SERVER_ACCESS_CREDITS},
   *          {@link SimQueueSimpleEventType#REGAINED_SAC},
   *          {@link SimQueueSimpleEventType#OUT_OF_SAC},
   *          as these are dealt with (if at all) by the outer loop.
   * <li>We ignore start-armed related events
   *          {@link SimQueueSimpleEventType#STA_FALSE},
   *          {@link SimQueueSimpleEventType#STA_TRUE},
   *          as these are dealt with (if at all) by the outer loop.
   * <li>With {@link SimQueueSimpleEventType#DROP}, we let the dropped delegate job arrive on a drop-destination queue,
   *                                                if provided through {@link #getDropDestinationQueue},
   *                                                otherwise, drops the real job through {@link #drop}.
   * <li>With {@link SimQueueSimpleEventType#REVOCATION}, we check for the presence of a corresponding real job through
   *                                                      {@link #getRealJob}, and throw an {@link IllegalStateException}
   *                                                      if we found one. Revocation notifications must always be the result
   *                                                      of the composite queue's {@link #revoke} operation, and at this stage,
   *                                                      the real job has already been removed from the composite queue.
   *                                                      Subsequently, we perform sanity checks on the pending revocation event,
   *                                                      again throwing an {@link IllegalStateException} in case of an error.
   *                                                      If all is well, we simply clear the pending revocation event
   *                                                      on the composite queue.
   * <li>With {@link SimQueueSimpleEventType#AUTO_REVOCATION}, we first check the start model
   *                                                          (must be
   *                                                          {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE})
   *                                                          and the source queue (must be the wait, or first, queue),
   *                                                          and throw an exception if the check fails.
   *                                                          Subsequently, we start the real job with {@link #start}.
   * <li>With {@link SimQueueSimpleEventType#START}, we start the real job if the start model
   *                                                 is {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE},
   *                                                 but we do nothing otherwise.
   * <li>With {@link SimQueueSimpleEventType#DEPARTURE}, we invoke {@link #selectNextQueue} on the real job,
   *                                                     and let the delegate job arrive at the next queue if provided,
   *                                                     or makes the real job depart if not through {@link #depart}.
   * <li>With any non-standard notification type, and start model {@link SimQueueComposite.StartModel#ENCAPSULATOR_QUEUE}
   *                                              or {@link SimQueueComposite.StartModel#ENCAPSULATOR_HIDE_START_QUEUE},
   *                                              we add the notification from the sub-queue to our own notification list
   *                                              (through {@link #addPendingNotification}),
   *                                              replacing a job in the sub-queue notification with its corresponding real job
   *                                              in our own notification.
   * </ul>
   * 
   * <p>
   * After all sub-notifications have been processed, and if the start model is
   * {@link SimQueueComposite.StartModel#COMPRESSED_TANDEM_2_QUEUE},
   * we make sure the server-access credits on the wait queue are set properly with {@link #setServerAccessCreditsOnWaitQueue},
   * unless the notification was a (single) {@link SimEntitySimpleEventType#RESET},
   * and we can rely on the composite queue reset logic.
   * 
   * <p>
   * After all notifications have been processed, and the notification list is empty,
   * we invoke {@link #triggerPotentialNewStartArmed} on the composite queue,
   * in order to make sure we are not missing an autonomous change in {@link SimQueue#isStartArmed}
   * on a sub-queue.
   * Since we do not expect any back-fire notifications from sub-queues from that method,
   * we check again the notification list, and throw an exception if it is non-empty.
   * 
   * <p>
   * A full description of the sanity checks would make this entry uninterestingly large(r), hence we refer to the source code.
   * Most checks are trivial checks on the allowed sub-notifications from the sub-queues depending
   * on the start model and on the presence or absence of real and delegate jobs
   * (and their expected presence or absence on a sub-queue).
   * 
   * @param notifications The sub-queue notifications, will be modified; empty upon return.
   * 
   * @throws IllegalArgumentException If the list is {@code null} or empty, or contains a notification from another queue
   *                                  than the a sub-queue,
   *                                  or if other sanity checks fail.
   * 
   * @see MultiSimQueueNotificationProcessor
   * @see Processor
   * @see MultiSimQueueNotificationProcessor#setProcessor
   * @see #getStartModel
   * @see SimQueueComposite.StartModel
   * @see SimEntitySimpleEventType#RESET
   * @see SimQueueSimpleEventType#ARRIVAL
   * @see SimQueueSimpleEventType#QUEUE_ACCESS_VACATION
   * @see SimQueueSimpleEventType#QAV_START
   * @see SimQueueSimpleEventType#QAV_END
   * @see SimQueueSimpleEventType#SERVER_ACCESS_CREDITS
   * @see SimQueueSimpleEventType#OUT_OF_SAC
   * @see SimQueueSimpleEventType#REGAINED_SAC
   * @see SimQueueSimpleEventType#STA_FALSE
   * @see SimQueueSimpleEventType#STA_TRUE
   * @see SimQueueSimpleEventType#DROP
   * @see SimQueueSimpleEventType#REVOCATION
   * @see SimQueueSimpleEventType#AUTO_REVOCATION
   * @see SimQueueSimpleEventType#START
   * @see SimQueueSimpleEventType#DEPARTURE
   * @see #addPendingNotification
   * @see #getDropDestinationQueue
   * @see SimQueue#arrive
   * @see #start
   * @see #selectNextQueue
   * @see #depart
   * @see #setServerAccessCreditsOnWaitQueue
   * @see #triggerPotentialNewStartArmed
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
   * 
   */
  protected final void processSubQueueNotifications
  (final List<MultiSimQueueNotificationProcessor.Notification<DJ, DQ>> notifications)
  {
    //
    // SANITY: Should receive at least one notification.
    //
    if (notifications == null || notifications.isEmpty ())
      throw new IllegalArgumentException ();
    //
    // Special treatment of RESET notifications: clear the list of notifications, ignore them, and return immediately.
    //
    if (MultiSimQueueNotificationProcessor.contains (notifications, SimEntitySimpleEventType.RESET))
    {
      notifications.clear ();
      return;
    }
    //
    // XXX There used to be an additional sanity check to make sure that a RESET notification would be isolated; something like
    //       if (notification.getSubNotifications ().size () > 1)
    //         throw new IllegalStateException ();
    // XXX Not sure why this check was needed...
    //
    //
    // Make sure we capture a top-level event, so we can keep our own notifications atomic.
    //
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    //
    // Iterate over all notifications, noting that additional notifications may be added as a result of our processing.
    //
    while (! notifications.isEmpty ())
    {
      //
      // Remove the notification at the head of the list.
      //
      final MultiSimQueueNotificationProcessor.Notification<DJ, DQ> notification = notifications.remove (0);
      //
      // Sanity check on the notification time (should be our last update time, i.e.,, out current time).
      //
      final double notificationTime = notification.getTime ();
      if (notification.getTime () != getLastUpdateTime ())
        throw new IllegalArgumentException ("on " + this + ": notification time [" + notification.getTime ()
        + "] != last update time [" + getLastUpdateTime () + "], subnotifications: "
        + notification.getSubNotifications () + ".");
      //
      // Sanity check on the queue that issued the notification; should be one of our sub-queues.
      //
      final DQ subQueue = notification.getQueue ();
      if (subQueue == null || ! getQueues ().contains (subQueue))
        throw new IllegalStateException ();
      //
      // SANITY CHECK ON STARTED/AUTO-REVOKED PAIRS
      //
      int nrStarted = 0;
      DJ lastJobStarted = null;
      int nrAutoRevocations = 0;
      DJ lastJobAutoRevoked = null;
      //
      // Iterate over all sub-notifications from this queue.
      //
      for (final Map<SimEntitySimpleEventType.Member, SimJQEvent<DJ, DQ>> subNotification
        : notification.getSubNotifications ())
      {
        if (subNotification == null || subNotification.size () != 1)
          throw new RuntimeException ();
        final SimEntitySimpleEventType.Member notificationType = subNotification.keySet ().iterator ().next ();
        final SimJQEvent<DJ, DQ> notificationEvent = subNotification.values ().iterator ().next ();
        if (notificationEvent == null)
          throw new RuntimeException ();
        final DJ job = subNotification.values ().iterator ().next ().getJob ();
        //
        // Sanity check on the (delegate) job (if any) to which the notification applies.
        //
        if (job != null)
        {
          //
          // We must have a real job corresponding to the delegate job,
          // except in case of a revocation (the composite queue has already disposed the real job from its administration).
          //
          if (notificationType != SimQueueSimpleEventType.REVOCATION)
            getRealJob (job);
        }
        if (notificationType == SimEntitySimpleEventType.RESET)
          //
          // If we receive a RESET notification at this point, we throw an exception, since we already took care of RESET
          // notifications earlier on the initial set of notifications.
          // Their appearance here indicates the RESET was due to our own actions on the sub-queue(s),
          // and added later, which is unexpected.
          //
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.QUEUE_ACCESS_VACATION
             ||  notificationType == SimQueueSimpleEventType.QAV_START
             ||  notificationType == SimQueueSimpleEventType.QAV_END)
          //
          // Queue-Access Vacations (or, in fact, any state-change reports regarding QAV's
          // are forbidden on sub-queues.
          //
          throw new IllegalStateException ();
        else if (notificationType == SimQueueSimpleEventType.SERVER_ACCESS_CREDITS
             ||  notificationType == SimQueueSimpleEventType.OUT_OF_SAC
             ||  notificationType == SimQueueSimpleEventType.REGAINED_SAC)
          //
          // Server-Acess Credits events are, if even relevant, taken care of by the outer loop
          // and the AbstractSimQueue implementation automatically,
          // so we must ignore them here.
          //
          ; /* NOTHING TO DO */
        else if (notificationType == SimQueueSimpleEventType.STA_FALSE
             ||  notificationType == SimQueueSimpleEventType.STA_TRUE)
          //
          // StartArmed events are taken care of by the outer loop
          // and the AbstractSimQueue implementation automatically,
          // so we must ignore them here.
          //
          ; /* NOTHING TO DO */
        else if (notificationType == SimQueueSimpleEventType.ARRIVAL)
        {
          //
          // A (delegate) job (pseudo) arrives at a sub-queue.
          // In any case, at this point, we sent the (delegate) job to that sub-queue ourselves.
          //
          ; /* NOTHING TO DO */
        }
        else if (notificationType == SimQueueSimpleEventType.DROP)
        {
          //
          // A (delegate) job is (pseudo-)dropped on a sub-queue.
          //
          if (getDropDestinationQueue () != null)
          {
            //
            // We have a drop-destination queue; migrate the job there.
            //
            if (! getQueues ().contains (getDropDestinationQueue ()))
              throw new RuntimeException ();
            //
            // XXX Special care is required with white composite queues here...
            // What is another white composite queue with this sub-queue has a drop destination queue as well?
            //
            getDropDestinationQueue ().arrive (notificationTime, job);
          }
          else
          {
            //
            // We do not have a drop destination queue, hence we must (pseudo-)drop the (real) job.
            //
            final J realJob = getRealJob (job);
            drop (realJob, notificationTime);
          }
        }
        else if (notificationType == SimQueueSimpleEventType.REVOCATION)
        {
          //
          // A (delegate) job is revoked on a sub-queue. This should always be the result from a revocation request on
          // the composite queue, and the real job should already have left the latter queue.
          // We perform sanity checks and clear the pending revocation event.
          //
          if (isDelegateJob (job))
            throw new IllegalStateException ();
          if (this.pendingDelegateRevocationEvent == null
          || this.pendingDelegateRevocationEvent.getQueue () != subQueue
          || this.pendingDelegateRevocationEvent.getJob () != job)
            throw new IllegalStateException ();
          this.pendingDelegateRevocationEvent = null;
        }
        else if (notificationType == SimQueueSimpleEventType.AUTO_REVOCATION)
        {
          if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE && subQueue == getQueue (0))
          {
            nrAutoRevocations++;
            lastJobAutoRevoked = job;
            start (notificationTime, getRealJob (job, null));
          }
          else
            throw new IllegalStateException ();
        }
        else if (notificationType == SimQueueSimpleEventType.START)
        {
          nrStarted++;
          lastJobStarted = job;
          if (getStartModel () == StartModel.ENCAPSULATOR_QUEUE)
            start (notificationTime, getRealJob (job));          
        }
        else if (notificationType == SimQueueSimpleEventType.DEPARTURE)
        {
          final J realJob = getRealJob (job);
          final SimQueue<DJ, DQ> nextQueue = selectNextQueue (notificationTime, realJob, subQueue);
          if (nextQueue != null)
            nextQueue.arrive (notificationTime, job);
          else
            depart (notificationTime, realJob);
        }
        else
        {
          //
          // We received a "non-standard" SimQueue event.
          // We can only process and report this if we are an ecapsulator queue,
          // in which case we have to replace both queue (always) and job (if applicable).
          // 
          if ((getStartModel () == StartModel.ENCAPSULATOR_QUEUE || getStartModel () == StartModel.ENCAPSULATOR_HIDE_START_QUEUE))
          {
            final J realJob = (job != null ? getRealJob (job) : null);
            // XXX Shouldn't we check if this notification type was actually registered at the composite queue?
            addPendingNotification (notificationType,
              (SimJQEvent<J, Q>) notificationEvent.copyForQueueAndJob ((DQ) this, (DJ) realJob));
          }
        }
      }
      if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE && notification.getQueue () == getQueue (0))
      {
        //
        // START and AUTO_REVOCATION have to come in pairs on the wait queue,
        // apply to the same job and at most one pair is allowed in a sub-notification.
        //
        if (nrStarted > 1 || nrAutoRevocations > 1 || nrStarted != nrAutoRevocations || lastJobStarted != lastJobAutoRevoked)
           throw new IllegalStateException ();
      }
      if (getStartModel () == StartModel.COMPRESSED_TANDEM_2_QUEUE && getIndex (subQueue) == 1)
        //
        // We received a state-changing event on the second ("serve") queue in a COMPRESSED_TANDEM_2_QUEUE.
        // We must reevaluate the server-access credits on the first ("wait") queue.
        //
        setServerAccessCreditsOnWaitQueue ();
    }
    triggerPotentialNewStartArmed (getLastUpdateTime ());
    if (! notifications.isEmpty ())
      throw new IllegalStateException ();
    if (isTopLevel)
      fireAndLockPendingNotifications ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE NOTIFICATIONS
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
  
  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override
  public final void notifyResetEntity (final SimEntity entity)
  {
  }

  /** Does nothing.
   * 
   * @see #processSubQueueNotifications
   * 
   */
  @Override  
  public final void notifyStateChanged
  (final double time,
   final SimEntity entity,
   final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
  {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
